import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";
import { getFirestore, collection, addDoc, onSnapshot, query, where, orderBy, updateDoc, doc, arrayUnion, serverTimestamp, getDoc, setDoc } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js";

// --- CONFIGURATION ---
const firebaseConfig = {
    apiKey: "AIzaSyAMX0axuOKlSfVnV1gNDPUPcWFJY_B4DtQ",
    authDomain: "wlaz-academy.firebaseapp.com",
    projectId: "wlaz-academy",
    storageBucket: "wlaz-academy.firebasestorage.app",
    messagingSenderId: "465806573948",
    appId: "1:465806573948:web:ba2eedc8ccfb32274624f9"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// --- STATE ---
let currentUser = null; // { name: string, id: string, isAdmin: boolean }
let currentSessionId = localStorage.getItem('wlaz_chat_session_id');
let unsubscribe = null;
const ADMIN_PHONE = "966552520";
const ADMIN_WHATSAPP_LINK = `https://wa.me/51${ADMIN_PHONE}`;
const AUTO_REPLY_DELAY = 15000; // 15 seconds for testing, maybe longer in prod
let autoReplyTimeout = null;

// --- DOM ELEMENTS (Dynamically found to avoid errors if loaded early) ---
const getEl = (id) => document.getElementById(id);

// --- INITIALIZATION ---
export function initChat() {
    renderChatWidget();
    checkAdminMode();
    const sessionId = localStorage.getItem('wlaz_chat_session_id');
    if (sessionId) {
        // ID exists, but we need the name. 
        // For simplicity, we might force login again or try to fetch name.
        // Let's just restore view if we recall it.
    }
}

// --- CORE LOGIC ---

async function login(name) {
    if (!name.trim()) return;

    // Create or Resume Session
    if (!currentSessionId) {
        // New Session
        try {
            const docRef = await addDoc(collection(db, "chat_sessions"), {
                userName: name,
                createdAt: serverTimestamp(),
                lastMessageAt: serverTimestamp(),
                status: 'active',
                messages: [] // We'll store messages in a subcollection or array. Array is cheaper for small chats.
            });
            currentSessionId = docRef.id;
            localStorage.setItem('wlaz_chat_session_id', currentSessionId);
            localStorage.setItem('wlaz_chat_user_name', name);
        } catch (e) {
            console.error("Error creating session", e);
            alert("Error connecting to secure server.");
            return;
        }
    }

    currentUser = { name: name, id: currentSessionId, isAdmin: false };
    showChatInterface();
    listenToMessages(currentSessionId);
}

function listenToMessages(sessionId) {
    if (unsubscribe) unsubscribe();

    const sessionRef = doc(db, "chat_sessions", sessionId);

    unsubscribe = onSnapshot(sessionRef, (docSnap) => {
        if (docSnap.exists()) {
            const data = docSnap.data();
            renderMessages(data.messages || []);

            // Auto Reply Logic Check
            const msgs = data.messages || [];
            if (msgs.length > 0) {
                const lastMsg = msgs[msgs.length - 1];
                if (lastMsg.sender === 'user' && !currentUser.isAdmin) {
                    // Start timer for auto-reply if not already replied
                    clearTimeout(autoReplyTimeout);
                    autoReplyTimeout = setTimeout(() => sendAutoReply(sessionId), AUTO_REPLY_DELAY);
                } else if (lastMsg.sender === 'admin' || lastMsg.sender === 'system') {
                    clearTimeout(autoReplyTimeout);
                }
            }
        }
    });
}

async function sendMessage(text, isAdmin = false) {
    if (!text.trim()) return;
    if (!currentSessionId) return;

    const msgObj = {
        text: text,
        sender: isAdmin ? 'admin' : 'user', // 'user' is the visitor
        timestamp: Date.now(),
        read: false
    };

    const sessionRef = doc(db, "chat_sessions", currentSessionId);

    try {
        await updateDoc(sessionRef, {
            messages: arrayUnion(msgObj),
            lastMessageAt: serverTimestamp(),
            lastMessagePreview: text
        });
    } catch (e) {
        console.error("Send failed", e);
    }
}

async function sendAutoReply(sessionId) {
    // Only the client (user side) should trigger this to avoid serverless complexity
    // But this means if the user closes tab, no reply. That's acceptable for this simple req.
    const sessionRef = doc(db, "chat_sessions", sessionId);

    const replyMsg = {
        text: `Joseph no está disponible en este momento. Por favor, contáctalo directamente en WhatsApp: ${ADMIN_WHATSAPP_LINK} o espera a que revise este chat.`,
        sender: 'system',
        timestamp: Date.now(),
        isLink: true
    };

    try {
        await updateDoc(sessionRef, {
            messages: arrayUnion(replyMsg)
        });
    } catch (e) { console.log(e); }
}


// --- UI RENDERING ---

function renderChatWidget() {
    // Inject CSS
    const style = document.createElement('style');
    style.innerHTML = `
        .wlaz-chat-fab {
            position: fixed;
            bottom: 24px;
            right: 24px;
            width: 60px;
            height: 60px;
            background: #9333ea;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            box-shadow: 0 10px 30px rgba(147, 51, 234, 0.4);
            z-index: 9999;
            transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }
        .wlaz-chat-fab:hover { transform: scale(1.1); }
        .wlaz-chat-window {
            position: fixed;
            bottom: 100px;
            right: 24px;
            width: 350px;
            height: 500px;
            background: #0A0A0A;
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 20px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            box-shadow: 0 20px 50px rgba(0,0,0,0.5);
            z-index: 9999;
            opacity: 0;
            pointer-events: none;
            transform: translateY(20px) scale(0.95);
            transition: all 0.3s ease;
        }
        .wlaz-chat-window.open {
            opacity: 1;
            pointer-events: all;
            transform: translateY(0) scale(1);
        }
        .chat-header {
            background: rgba(20, 20, 20, 0.9);
            padding: 16px;
            border-bottom: 1px solid rgba(255,255,255,0.05);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .chat-body {
            flex: 1;
            overflow-y: auto;
            padding: 16px;
            display: flex;
            flex-direction: column;
            gap: 12px;
            background: #050505;
        }
        .chat-footer {
            padding: 12px;
            background: #0A0A0A;
            border-top: 1px solid rgba(255,255,255,0.05);
            display: flex;
            gap: 8px;
        }
        .chat-input {
            flex: 1;
            background: #111;
            border: 1px solid #333;
            color: #fff;
            padding: 8px 12px;
            border-radius: 99px;
            outline: none;
            font-size: 14px;
        }
        .chat-input:focus { border-color: #9333ea; }
        .msg-bubble {
            max-width: 80%;
            padding: 10px 14px;
            border-radius: 16px;
            font-size: 13px;
            line-height: 1.4;
            position: relative;
        }
        .msg-own {
            background: #9333ea;
            color: #fff;
            align-self: flex-end;
            border-bottom-right-radius: 4px;
        }
        .msg-other {
            background: #1a1a1a;
            color: #ccc;
            align-self: flex-start;
            border-bottom-left-radius: 4px;
        }
        .msg-system {
            background: #111;
            border: 1px solid #333;
            color: #aaa;
            align-self: center;
            font-size: 11px;
            text-align: center;
            max-width: 90%;
            font-family: monospace;
        }
        .sys-link { color: #9333ea; text-decoration: underline; }
    `;
    document.head.appendChild(style);

    // HTML Component
    const div = document.createElement('div');
    div.innerHTML = `
        <div class="wlaz-chat-fab" id="chatFab">
            <i class="fas fa-comment-dots text-2xl text-white"></i>
        </div>

        <div class="wlaz-chat-window" id="chatWindow">
            <div class="chat-header">
                <div class="flex items-center gap-3">
                    <button id="backToAdmin" class="hidden text-white/50 hover:text-white mr-1"><i class="fas fa-arrow-left"></i></button>
                    <div class="w-8 h-8 rounded-full bg-white/10 border border-white/10 flex items-center justify-center overflow-hidden">
                        <i class="fas fa-user-astronaut text-white/50 text-xs"></i>
                    </div>
                    <div>
                        <h4 class="text-sm font-bold text-white">Joseph Wlaz</h4>
                        <span id="statusText" class="text-[10px] text-gray-400 block flex items-center gap-1">
                            <span class="w-1.5 h-1.5 rounded-full bg-white animate-pulse"></span> Online
                        </span>
                    </div>
                </div>
                <button id="closeChat" class="text-gray-500 hover:text-white"><i class="fas fa-times"></i></button>
            </div>
            
            <div class="chat-body" id="chatBody">
                <!-- Login View -->
                <div id="loginView" class="h-full flex flex-col items-center justify-center text-center p-4">
                    <i class="fas fa-shield-alt text-4xl text-purple-500 mb-4"></i>
                    <h3 class="text-white font-bold mb-2">Secure Channel</h3>
                    <p class="text-gray-500 text-xs mb-6">Enter your name to establish an encrypted connection.</p>
                    <input type="text" id="nameInput" placeholder="Your Name" class="w-full bg-[#111] border border-[#333] text-white p-3 rounded-lg mb-4 text-center focus:border-purple-600 outline-none transition-colors">
                    <button id="startChatBtn" class="w-full bg-purple-600 hover:bg-purple-700 text-white font-bold py-3 rounded-lg transition-all">Start Chat</button>
                </div>

                <!-- Admin List View -->
                <div id="adminListView" class="hidden h-full flex-col overflow-y-auto"></div>

                <!-- Messages View -->
                <div id="msgList" class="hidden flex-col gap-3 w-full"></div>
            </div>

            <div class="chat-footer hidden" id="chatFooter">
                <input type="text" id="msgInput" class="chat-input" placeholder="Type a message...">
                <button id="sendBtn" class="w-10 h-10 rounded-full bg-purple-600 flex items-center justify-center text-white hover:bg-purple-500 transition-colors">
                    <i class="fas fa-paper-plane text-xs"></i>
                </button>
            </div>
        </div>
    `;
    document.body.appendChild(div);

    // Event Listeners
    const fab = getEl('chatFab');
    const window = getEl('chatWindow');
    const close = getEl('closeChat');
    const backBtn = getEl('backToAdmin'); // New back button
    const startBtn = getEl('startChatBtn');
    const sendBtn = getEl('sendBtn');
    const msgInput = getEl('msgInput');
    const nameInput = getEl('nameInput');

    fab.addEventListener('click', () => {
        window.classList.toggle('open');
        const icon = fab.querySelector('i');
        if (window.classList.contains('open')) {
            icon.className = 'fas fa-chevron-down text-2xl text-white';
            // Auto focus logic
            if (!currentUser && !localStorage.getItem('wlaz_admin_mode')) setTimeout(() => nameInput.focus(), 100);
        } else {
            icon.className = 'fas fa-comment-dots text-2xl text-white';
        }
    });

    close.addEventListener('click', () => {
        window.classList.remove('open');
        fab.querySelector('i').className = 'fas fa-comment-dots text-2xl text-white';
    });

    if (backBtn) {
        backBtn.addEventListener('click', () => {
            // Go back to list
            loadAdminDashboard();
        });
    }

    startBtn.addEventListener('click', () => {
        const name = nameInput.value;
        if (name.length > 1) {
            login(name);
        }
    });

    sendBtn.addEventListener('click', () => {
        const txt = msgInput.value;
        sendMessage(txt, currentUser.isAdmin);
        msgInput.value = '';
    });

    msgInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            sendMessage(e.target.value, currentUser.isAdmin);
            e.target.value = '';
        }
    });

    // Restore session check (Only if NOT admin)
    const savedName = localStorage.getItem('wlaz_chat_user_name');
    const isAdminMode = localStorage.getItem('wlaz_admin_mode') === 'true';

    if (savedName && !isAdminMode) {
        login(savedName);
    }
}

function showChatInterface() {
    getEl('loginView').classList.add('hidden');
    getEl('adminListView').classList.add('hidden'); // Hide list if visible
    getEl('msgList').classList.remove('hidden');
    getEl('msgList').style.display = 'flex';
    getEl('chatFooter').classList.remove('hidden');

    // If Admin, show back button
    if (currentUser && currentUser.isAdmin) {
        getEl('backToAdmin').classList.remove('hidden');
        getEl('chatWindow').style.borderColor = '#2dd4bf'; // Teal for admin
    } else {
        getEl('backToAdmin').classList.add('hidden');
    }
}

function renderMessages(messages) {
    const list = getEl('msgList');
    list.innerHTML = '';

    // Welcome message if empty
    if (messages.length === 0) {
        list.innerHTML = `
            <div class="msg-system">
                Locked connection established.<br>
                Identity Verified: <span class="text-white">${currentUser.name}</span>
            </div>
        `;
    }

    messages.forEach(msg => {
        const div = document.createElement('div');
        let cls = 'msg-system';

        // Logic for styling based on who 'currentUser' is
        if (currentUser.isAdmin) {
            // Admin View: 'user' is OTHER (left), 'admin' is OWN (right)
            if (msg.sender === 'user') cls = 'msg-other';
            if (msg.sender === 'admin') cls = 'msg-own';
        } else {
            // User View: 'user' is OWN (right), 'admin' is OTHER (left)
            if (msg.sender === 'user') cls = 'msg-own';
            if (msg.sender === 'admin') cls = 'msg-other';
        }

        if (msg.sender === 'system') cls = 'msg-system';

        div.className = `msg-bubble ${cls}`;

        if (msg.isLink) {
            div.innerHTML = msg.text.replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank" class="sys-link">WhatsApp</a>');
        } else {
            div.textContent = msg.text;
        }

        list.appendChild(div);
    });

    // Scroll to bottom
    const body = getEl('chatBody');
    body.scrollTop = body.scrollHeight;
}

// --- ADMIN ---
function checkAdminMode() {
    // If already authenticated in session
    if (localStorage.getItem('wlaz_admin_mode') === 'true') {
        loadAdminDashboard();
        return;
    }

    // URL param ?admin=true
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('admin') === 'true') {
        // Simple auth
        const pass = prompt("Wlaz Security Core\nEnter Access Code:");
        if (pass === 'wlaz2026') {
            localStorage.setItem('wlaz_admin_mode', 'true');
            loadAdminDashboard();
        } else {
            alert("Access Denied");
            window.location.href = window.location.pathname; // Clear param
        }
    }
}

async function loadAdminDashboard() {
    currentUser = { name: 'Joseph (Admin)', id: 'admin', isAdmin: true };

    // Update UI for Admin
    const win = getEl('chatWindow');
    win.classList.add('open'); // Auto open
    getEl('chatFab').classList.add('hidden'); // Hide FAB
    // getEl('logoImg').src = '...'; // Removed profile img update
    getEl('statusText').innerHTML = '<span class="text-white font-bold">ADMIN CONSOLE</span>';

    // Views
    getEl('loginView').classList.add('hidden');
    getEl('msgList').classList.add('hidden');
    getEl('chatFooter').classList.add('hidden');
    getEl('backToAdmin').classList.add('hidden');

    const list = getEl('adminListView');
    list.classList.remove('hidden');
    list.innerHTML = '<div class="text-center text-gray-500 mt-4"><i class="fas fa-circle-notch fa-spin"></i> Loading Streams...</div>';

    // Listen to ALL sessions
    const q = query(collection(db, "chat_sessions"), orderBy("lastMessageAt", "desc"));

    onSnapshot(q, (snapshot) => {
        list.innerHTML = '';
        if (snapshot.empty) {
            list.innerHTML = '<div class="text-center text-gray-600 mt-10">No active signals.</div>';
            return;
        }

        snapshot.forEach(docSnap => {
            const data = docSnap.data();
            const sid = docSnap.id;

            // Create Item
            const item = document.createElement('div');
            item.className = 'p-3 border-b border-white/5 hover:bg-white/5 cursor-pointer transition-colors flex justify-between items-center group';

            // Time format
            let timeStr = '';
            if (data.lastMessageAt) {
                const date = data.lastMessageAt.toDate();
                timeStr = date.getHours() + ':' + date.getMinutes().toString().padStart(2, '0');
            }

            item.innerHTML = `
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-gradient-to-br from-gray-800 to-black border border-white/10 flex items-center justify-center text-white font-bold text-xs">
                        ${data.userName.substring(0, 2).toUpperCase()}
                    </div>
                    <div class="flex flex-col">
                        <span class="text-white font-bold text-sm group-hover:text-teal-400 transition-colors">${data.userName}</span>
                        <span class="text-gray-500 text-[10px] truncate max-w-[120px]">${data.lastMessagePreview || 'No messages'}</span>
                    </div>
                </div>
                <div class="flex flex-col items-end gap-1">
                     <span class="text-gray-600 text-[9px] font-mono">${timeStr}</span>
                     ${data.status === 'active' ? '<span class="w-1.5 h-1.5 bg-green-500 rounded-full"></span>' : ''}
                </div>
            `;

            item.addEventListener('click', () => {
                enterAdminChat(sid, data.userName);
            });

            list.appendChild(item);
        });
    });
}

function enterAdminChat(sessionId, userName) {
    currentSessionId = sessionId;
    // We remain admin
    currentUser = { name: 'Joseph (Admin)', id: 'admin', isAdmin: true };

    // Update Header
    getEl('statusText').innerHTML = `Chatting with: <span class="text-white">${userName}</span>`;

    showChatInterface();
    listenToMessages(sessionId);
}

// Auto init
document.addEventListener('DOMContentLoaded', initChat);
