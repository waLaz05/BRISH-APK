/**
 * Wlaz Ecosystem Command Palette (Nexus)
 * A unified navigation interface accessible via Ctrl+K / Cmd+K
 */

class CommandNexus {
    constructor() {
        this.isOpen = false;
        this.user = JSON.parse(localStorage.getItem('upn_user') || 'null');
        this.notifications = [];
        this.lastTopScore = parseInt(localStorage.getItem('wlaz_last_top_score') || '0');
        this.currentTheme = localStorage.getItem('wlaz_theme') || 'ultraviolet';
        this.themes = {
            ultraviolet: { primary: '#9333ea', secondary: '#c084fc', accent: '#2dd4bf', name: 'Ultraviolet' },
            matrix: { primary: '#22c55e', secondary: '#4ade80', accent: '#fbbf24', name: 'Matrix' },
            crimson: { primary: '#e11d48', secondary: '#fb7185', accent: '#ffffff', name: 'Crimson' },
            deepsea: { primary: '#06b6d4', secondary: '#22d3ee', accent: '#f472b6', name: 'DeepSea' },
            amber: { primary: '#f59e0b', secondary: '#fbbf24', accent: '#2dd4bf', name: 'Amber' }
        };
        this.init();
        this.initRealTimeListeners();
        this.applyTheme(this.currentTheme);
        this.deferredInstallPrompt = null;
        this.registerPWA();
        this.initSocialHub();
    }

    init() {
        // this.showGlobalPreloader(); // DISABLED: Removed ugly loading screen
        this.injectStyles();
        this.createOverlay();
        this.bindEvents();
    }

    // showGlobalPreloader() removed to prevent unwanted loading screen

    injectStyles() {
        const style = document.createElement('style');
        style.textContent = `
            .nexus-overlay {
                position: fixed;
                inset: 0;
                background: rgba(5, 5, 5, 0.6);
                backdrop-filter: blur(8px);
                z-index: 99999;
                display: none;
                justify-content: center;
                align-items: flex-start;
                padding-top: 15vh;
                opacity: 0;
                transition: opacity 0.2s ease;
            }

            .nexus-overlay.active {
                display: flex;
                opacity: 1;
            }

            .nexus-modal {
                background: #0f0f12;
                width: 100%;
                max-width: 600px;
                border-radius: 12px;
                box-shadow: 0 20px 50px -10px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.1);
                overflow: hidden;
                transform: scale(0.98);
                transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
                font-family: 'Inter', sans-serif;
            }

            @media (max-width: 768px) {
                .nexus-overlay {
                    padding-top: 0;
                    align-items: flex-start; /* Top aligned */
                }

                /* HIDE SEARCH AND FEED ON MOBILE */
                .nexus-search, .nexus-feed-group {
                    display: none !important;
                }

                .nexus-modal {
                    border-radius: 0 0 24px 24px;
                    max-width: none;
                    transform: translateY(-100%);
                    max-height: 100dvh; /* Dynamic viewport */
                    height: auto;
                    display: flex;
                    flex-direction: column;
                    box-shadow: 0 20px 40px rgba(0,0,0,0.5);
                }

                .nexus-overlay.active .nexus-modal {
                    transform: translateY(0);
                }
                
                .nexus-content {
                    max-height: none;
                    flex: 1;
                    padding-bottom: env(safe-area-inset-bottom);
                }
            }

            .nexus-search {
                display: flex;
                align-items: center;
                padding: 16px 20px;
                border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            }

            .nexus-search i {
                color: #9ca3af;
                font-size: 18px;
                margin-right: 12px;
            }

            .nexus-input {
                background: transparent;
                border: none;
                color: white;
                font-size: 16px;
                width: 100%;
                outline: none;
                font-family: inherit;
            }

            .nexus-input::placeholder {
                color: #4b5563;
            }

            .nexus-content {
                padding: 8px;
                max-height: 400px;
                overflow-y: auto;
            }

            .nexus-section {
                padding: 8px 12px;
                font-size: 11px;
                font-weight: 600;
                text-transform: uppercase;
                letter-spacing: 1px;
                color: #6b7280;
                margin-top: 4px;
            }

            .nexus-item {
                display: flex;
                align-items: center;
                gap: 12px;
                padding: 10px 12px;
                color: #e5e7eb;
                cursor: pointer;
                border-radius: 8px;
                transition: all 0.1s;
                text-decoration: none;
            }

            .nexus-item:hover, .nexus-item.selected {
                background: rgba(124, 58, 237, 0.1);
                color: white;
            }

            .nexus-item i {
                width: 20px;
                text-align: center;
                color: #9ca3af;
            }

            .nexus-item:hover i {
                color: #a78bfa;
            }

            .nexus-shortcut {
                margin-left: auto;
                font-size: 11px;
                color: #6b7280;
                background: rgba(255,255,255,0.05);
                padding: 2px 6px;
                border-radius: 4px;
            }

            .nexus-footer {
                padding: 8px 16px;
                background: rgba(255,255,255,0.02);
                border-top: 1px solid rgba(255,255,255,0.05);
                display: flex;
                justify-content: space-between;
                font-size: 11px;
                color: #6b7280;
            }

            .nexus-feed {
                padding: 4px 12px 12px 12px;
                display: flex;
                flex-direction: column;
                gap: 8px;
            }

            .feed-item {
                display: flex;
                align-items: center;
                gap: 10px;
                padding: 8px 10px;
                background: rgba(255,255,255,0.02);
                border-radius: 6px;
                font-size: 11px;
                border: 1px solid rgba(255,255,255,0.03);
                animation: fadeInItem 0.4s ease-out;
            }

            @keyframes fadeInItem {
                from { opacity: 0; transform: translateY(10px); }
                to { opacity: 1; transform: translateY(0); }
            }

            .feed-icon {
                width: 24px;
                height: 24px;
                border-radius: 4px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 10px;
                background: rgba(147, 51, 234, 0.1);
                color: #9333ea;
            }

            .feed-time {
                margin-left: auto;
                color: #4b5563;
                font-family: 'JetBrains Mono', monospace;
                font-size: 9px;
            }

            .live-dot {
                width: 6px;
                height: 6px;
                background: #f87171;
                border-radius: 50%;
                display: inline-block;
                margin-right: 6px;
                animation: pulseLive 1.5s infinite;
            }

            @keyframes pulseLive {
                0% { opacity: 1; box-shadow: 0 0 0 0 rgba(248, 113, 113, 0.7); }
                70% { opacity: 0.5; box-shadow: 0 0 0 6px rgba(248, 113, 113, 0); }
                100% { opacity: 1; box-shadow: 0 0 0 0 rgba(248, 113, 113, 0); }
            }

            .theme-grid {
                display: grid;
                grid-template-columns: repeat(5, 1fr);
                gap: 8px;
                padding: 8px 12px;
            }

            @media (max-width: 480px) {
                .theme-grid {
                    grid-template-columns: repeat(3, 1fr);
                }
                .theme-dot { height: 40px; }
            }

            .theme-dot {
                height: 32px;
                border-radius: 6px;
                cursor: pointer;
                border: 1px solid rgba(255,255,255,0.1);
                transition: all 0.2s;
                display: flex;
                align-items: center;
                justify-content: center;
                position: relative;
            }

            .theme-dot:hover {
                transform: translateY(-2px);
                border-color: rgba(255,255,255,0.3);
            }

            .theme-dot.active {
                border-color: white;
                box-shadow: 0 0 10px rgba(255,255,255,0.2);
            }

            .theme-dot.active::after {
                content: '✓';
                color: white;
                font-size: 10px;
                font-weight: 900;
            }

            /* Toast Notifications */
            .wlaz-toast-container {
                position: fixed;
                top: 24px;
                right: 24px;
                z-index: 100000;
                display: flex;
                flex-direction: column;
                gap: 12px;
                pointer-events: none;
            }

            .wlaz-toast {
                background: rgba(10, 10, 12, 0.9);
                backdrop-filter: blur(12px);
                border-left: 3px solid #9333ea;
                padding: 12px 20px;
                border-radius: 4px;
                color: white;
                font-family: 'JetBrains Mono', monospace;
                font-size: 12px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.5);
                transform: translateX(120%);
                transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
                pointer-events: all;
                display: flex;
                align-items: center;
                gap: 12px;
                min-width: 280px;
            }

            @media (max-width: 768px) {
                .wlaz-toast-container {
                    top: env(safe-area-inset-top, 12px);
                    right: 12px;
                    left: 12px;
                }
                .wlaz-toast {
                    min-width: 0;
                    transform: translateY(-120%);
                    width: 100%;
                }
                .wlaz-toast.active { transform: translateY(0); }
            }

            .wlaz-toast.active {
                transform: translateX(0);
            }

            .wlaz-toast i {
                color: #9333ea;
                font-size: 16px;
            }

            .wlaz-toast .toast-content {
                display: flex;
                flex-direction: column;
            }

            .wlaz-toast .toast-title {
                font-weight: 800;
                text-transform: uppercase;
                letter-spacing: 1px;
                font-size: 10px;
                color: #9333ea;
            }
        `;
        document.head.appendChild(style);
    }

    createOverlay() {
        this.overlay = document.createElement('div');
        this.overlay.className = 'nexus-overlay';

        // --- ROBUST PATH SYSTEM v3 ---
        // Uses the location of THIS script (nexus.js) to determine the Project Root.
        // nexus.js is at: ROOT/assets/js/nexus.js
        // So we go up 2 levels relative to import.meta.url
        const ROOT_URL = new URL('../../', import.meta.url).href;

        const apps = [
            { name: 'Wlaz Home', icon: 'fa-home', url: new URL('Wlaz_Main/index.html', ROOT_URL).href },
            { name: 'AntiProcast (Beta)', icon: 'fa-brain', url: new URL('Wlaz_AntiProcast/index.html', ROOT_URL).href },
            { name: 'Asset Store', icon: 'fa-shopping-bag', url: new URL('Wlaz_Pages_Store/index.html', ROOT_URL).href },
            { name: 'Wlaz Arcade', icon: 'fa-gamepad', url: new URL('Wlaz_Arcade/index.html', ROOT_URL).href },
            { name: 'Wlaz Academy', icon: 'fa-graduation-cap', url: new URL('Wlaz_Academy/content/index.html', ROOT_URL).href }
        ];

        const isFocus = localStorage.getItem('wlaz_focus_active') === 'true';

        this.overlay.innerHTML = `
            <div class="nexus-modal">
                <div class="nexus-search">
                    <i class="fas fa-search"></i>
                    <input type="text" class="nexus-input" placeholder="What do you need?..." id="nexus-input">
                </div>
                <div class="nexus-content">
                    <div class="nexus-section">Modules / Apps</div>
                    ${apps.map((app, i) => `
                        <div class="nexus-item" onclick="window.wlazNavigate('${app.url}')" data-idx="${i}">
                            <i class="fas ${app.icon}"></i>
                            <span>${app.name}</span>
                            <span class="nexus-shortcut">Go</span>
                        </div>
                    `).join('')}

                    <div class="nexus-section">Quick Actions</div>
                    <div class="nexus-item" onclick="window.wlazNexus.toggleFocusMode()">
                        <i class="fas fa-moon"></i>
                        <span>Focus Mode</span>
                        <span class="nexus-shortcut" id="focus-shortcut-status">${isFocus ? 'ON' : 'OFF'}</span>
                    </div>

                    <div class="nexus-item" id="nexus-install-btn" style="display: none;" onclick="window.wlazNexus.installPWA()">
                        <i class="fas fa-download"></i>
                        <span>Install Ecosystem</span>
                        <span class="nexus-shortcut">App</span>
                    </div>

                    <div class="nexus-section">Wlaz Themes</div>
                    <div class="theme-grid">
                        ${Object.entries(this.themes).map(([key, theme]) => `
                            <div class="theme-dot ${this.currentTheme === key ? 'active' : ''}" 
                                 onclick="window.wlazNexus.setTheme('${key}')"
                                 style="background: ${theme.primary}"
                                 title="${theme.name}">
                            </div>
                        `).join('')}
                    </div>

                    <div class="nexus-feed-group">
                        <div class="nexus-section">
                            <span class="live-dot"></span>
                            Live Global Feed
                        </div>
                        <div class="nexus-feed" id="nexus-global-feed">
                            <div class="text-gray-600 text-[10px] text-center p-4 italic">Connecting to Neural Link...</div>
                        </div>
                    </div>
                    
                    <div class="nexus-section">System</div>
                    <div class="nexus-item" onclick="window.location.reload()">
                        <i class="fas fa-sync"></i>
                        <span>Reload System</span>
                        <span class="nexus-shortcut">R</span>
                    </div>
                </div>
                <div class="nexus-footer">
                    <span>${this.user ? 'Logged as ' + this.user.displayName : 'Guest User'}</span>
                    <span>WLAZ NEXUS v2.0</span>
                </div>
            </div>
        `;

        document.body.appendChild(this.overlay);
        this.input = this.overlay.querySelector('#nexus-input');

        // Create toast container
        this.toastContainer = document.createElement('div');
        this.toastContainer.className = 'wlaz-toast-container';
        document.body.appendChild(this.toastContainer);
    }

    notify(title, message, icon = 'fa-info-circle', duration = 5000) {
        const toast = document.createElement('div');
        toast.className = 'wlaz-toast';
        toast.innerHTML = `
            <i class="fas ${icon}"></i>
            <div class="toast-content">
                <span class="toast-title">${title}</span>
                <span class="toast-text">${message}</span>
            </div>
        `;

        this.toastContainer.appendChild(toast);

        // Trigger animation
        setTimeout(() => toast.classList.add('active'), 100);

        // Auto remove
        setTimeout(() => {
            toast.classList.remove('active');
            setTimeout(() => toast.remove(), 400);
        }, duration);

        // Update Dock activity indicator if exists
        const statusSignal = document.querySelector('.status-signal');
        if (statusSignal) {
            statusSignal.classList.add('activity');
            setTimeout(() => statusSignal.classList.remove('activity'), 2000);
        }
    }

    bindEvents() {
        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                this.toggle();
            }
            // Hacker Mode (Ctrl + Alt + H)
            if (e.ctrlKey && e.altKey && (e.key === 'h' || e.key === 'H')) {
                e.preventDefault();
                this.activateHackerMode();
            }
            if (e.key === 'Escape' && this.isOpen) {
                this.close();
            }
        });

        // Listen for install prompt
        window.addEventListener('beforeinstallprompt', (e) => {
            e.preventDefault();
            this.deferredInstallPrompt = e;
            const installBtn = document.getElementById('nexus-install-btn');
            if (installBtn) installBtn.style.display = 'flex';
        });

        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) this.close();
        });
    }

    toggle() {
        if (this.isOpen) this.close();
        else this.open();
    }

    open() {
        this.isOpen = true;
        this.overlay.classList.add('active');
        // Only auto-focus on desktop to prevent keyboard popup on mobile
        if (window.innerWidth > 768) {
            setTimeout(() => this.input.focus(), 50);
        }
    }

    close() {
        this.isOpen = false;
        this.overlay.classList.remove('active');
    }

    activateHackerMode() {
        this.notify('SYSTEM BREACH', 'Root access granted. Initializing Matrix Protocol...', 'fa-user-secret', 5000);
        this.setTheme('matrix');

        // Visual Effect
        const canvas = document.createElement('canvas');
        canvas.style.cssText = 'position:fixed;inset:0;z-index:999998;pointer-events:none;opacity:0.3;mix-blend-mode:screen;';
        document.body.appendChild(canvas);

        const ctx = canvas.getContext('2d');
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;

        const letters = "01010101WLAZSYSTEM";
        const fontSize = 14;
        const columns = canvas.width / fontSize;
        const drops = Array(Math.floor(columns)).fill(1);

        function draw() {
            ctx.fillStyle = "rgba(0, 0, 0, 0.05)";
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.fillStyle = "#0F0";
            ctx.font = fontSize + "px monospace";

            for (let i = 0; i < drops.length; i++) {
                const text = letters[Math.floor(Math.random() * letters.length)];
                ctx.fillText(text, i * fontSize, drops[i] * fontSize);
                if (drops[i] * fontSize > canvas.height && Math.random() > 0.975) drops[i] = 0;
                drops[i]++;
            }
        }

        const interval = setInterval(draw, 33);

        // Auto remove after 5s to not annoy
        setTimeout(() => {
            clearInterval(interval);
            canvas.remove();
        }, 5000);
    }

    async initRealTimeListeners() {
        // --- 1. LOCAL STORAGE LISTENERS (Cross-tab Sync) ---
        window.addEventListener('storage', (e) => {
            if (e.key === 'wlaz_focus_active') {
                this.updateFocusUI(e.newValue === 'true');
            }
        });

        // Check initial focus state
        this.updateFocusUI(localStorage.getItem('wlaz_focus_active') === 'true');

        // --- 2. FIREBASE REAL-TIME LISTENERS (Leaderboard / Global Events) ---
        try {
            // In Production (Firebase Hosting/GitHub), we use relative paths from THIS script
            const configPath = new URL('./firebase-config.js', import.meta.url).href;
            const { db, collection, query, orderBy, limit, onSnapshot } = await import(configPath);

            console.log("📡 [Nexus] Real-time Link Established");

            // --- 2. FIREBASE REAL-TIME LISTENERS (Auth Guarded) ---
            const { auth, onAuthStateChanged } = await import(configPath);

            onAuthStateChanged(auth, (user) => {
                if (user) {
                    console.log("📡 [Nexus] Authenticated. Starting Real-time Feed...");

                    // Watch top score
                    const q = query(collection(db, "arcade_users"), orderBy("highScores.firewallBreaker", "desc"), limit(1));

                    onSnapshot(q, (snapshot) => {
                        if (!snapshot.empty) {
                            const topUser = snapshot.docs[0].data();
                            const topScore = topUser.highScores?.firewallBreaker || 0;
                            const name = topUser.displayName ? topUser.displayName.split(' ')[0] : 'Agent';

                            if (topScore > this.lastTopScore && this.lastTopScore !== 0) {
                                this.notify('NEW GLOBAL RECORD', `${name} reached ${topScore.toLocaleString()}!`, 'fa-trophy', 8000);
                            }

                            this.lastTopScore = topScore;
                            localStorage.setItem('wlaz_last_top_score', topScore.toString());
                        }
                    }, (error) => {
                        console.warn("⚠️ [Nexus] Feed access denied (Guest or Rule Limiation)", error.code);
                    });
                } else {
                    console.log("🔒 [Nexus] Guest Mode using Cached Data (No Real-time)");
                }
            });

        } catch (err) {
            console.warn("⚠️ [Nexus] Remote Real-time disabled (Firebase config not found or accessible from this context)", err);
        }
    }

    updateFocusUI(isActive) {
        const signal = document.querySelector('.status-signal');
        const statusText = document.querySelector('.dock-status-text');

        if (!signal || !statusText) return;

        if (isActive) {
            signal.classList.add('focus-mode');
            statusText.classList.add('focus-mode');
            statusText.innerText = 'FOCUS SESSION ACTIVE';

            // Subtle notification only once
            if (!this.wasFocusActive) {
                this.notify('DEEP WORK', 'Focus Mode is currently active.', 'fa-brain', 3000);
            }
            this.wasFocusActive = true;
        } else {
            signal.classList.remove('focus-mode');
            statusText.classList.remove('focus-mode');
            statusText.innerText = 'SYSTEM ONLINE';
            this.wasFocusActive = false;
        }
    }

    toggleFocusMode() {
        const current = localStorage.getItem('wlaz_focus_active') === 'true';
        const newState = !current;
        localStorage.setItem('wlaz_focus_active', newState.toString());

        // Update local UI immediately
        this.updateFocusUI(newState);

        // Update shortcut text in modal
        const statusEl = document.getElementById('focus-shortcut-status');
        if (statusEl) statusEl.innerText = newState ? 'ON' : 'OFF';

        if (newState) {
            this.notify('FOCUS ENABLED', 'Notifications minimized. Deep work sequence started.', 'fa-moon');
        } else {
            this.notify('FOCUS DISABLED', 'System returning to standard mode.', 'fa-sun');
        }
    }

    setTheme(themeKey) {
        if (!this.themes[themeKey]) return;
        this.currentTheme = themeKey;
        localStorage.setItem('wlaz_theme', themeKey);
        this.applyTheme(themeKey);

        // Update UI
        document.querySelectorAll('.theme-dot').forEach(dot => dot.classList.remove('active'));
        const activeDot = document.querySelector(`.theme-dot[title="${this.themes[themeKey].name}"]`);
        if (activeDot) activeDot.classList.add('active');

        this.notify('THEME UPDATED', `Ecosystem identity set to ${this.themes[themeKey].name}.`, 'fa-palette');
    }

    applyTheme(themeKey) {
        const theme = this.themes[themeKey];
        if (!theme) return;

        // Inyect CSS variables override
        let overrideStyle = document.getElementById('wlaz-theme-overrides');
        if (!overrideStyle) {
            overrideStyle = document.createElement('style');
            overrideStyle.id = 'wlaz-theme-overrides';
            document.head.appendChild(overrideStyle);
        }

        // We target common Tailwind classes and custom ones
        overrideStyle.textContent = `
            :root {
                --wlaz-primary: ${theme.primary};
                --wlaz-secondary: ${theme.secondary};
                --wlaz-accent: ${theme.accent};
            }
            /* Override Tailwind text and bg classes (Brute force for speed) */
            .text-primary { color: ${theme.primary} !important; }
            .bg-primary { background-color: ${theme.primary} !important; }
            .border-primary { border-color: ${theme.primary} !important; }
            .text-secondary { color: ${theme.secondary} !important; }
            .bg-secondary { background-color: ${theme.secondary} !important; }
            .text-accent { color: ${theme.accent} !important; }
            .bg-accent { background-color: ${theme.accent} !important; }
            
            /* Custom components */
            .status-signal:not(.focus-mode):not(.activity) { 
                background: ${theme.accent} !important; 
                box-shadow: 0 0 10px ${theme.accent} !important; 
            }
            .dock-status-text:not(.focus-mode) { color: ${theme.accent} !important; }
            .wlaz-toast { border-left-color: ${theme.primary} !important; }
            .wlaz-toast i { color: ${theme.primary} !important; }
            .wlaz-toast .toast-title { color: ${theme.primary} !important; }
            .dock-glass:hover { border-color: ${theme.primary} !important; box-shadow: 0 0 0 1px ${theme.primary}, 0 15px 30px -5px ${theme.primary}44 !important; }
            .dock-glass:hover .key { border-color: ${theme.primary} !important; color: ${theme.secondary} !important; background: ${theme.primary}22 !important; }

            @media (max-width: 768px) {
                .wlaz-sys-dock {
                    bottom: calc(12px + env(safe-area-inset-bottom));
                }
            }

            /* Page overrides */
            .bento-card:hover { border-color: ${theme.primary}88 !important; }
            .glow-orb { background: radial-gradient(circle, ${theme.primary}22 0%, rgba(0, 0, 0, 0) 70%) !important; }
            .loader-bar::after { background: ${theme.primary} !important; }
            .btn-magnetic:hover div { background-color: ${theme.primary} !important; }
        `;
    }

    registerPWA() {
        // 1. Dynamic Manifest Insertion
        if (!document.querySelector('link[rel="manifest"]')) {
            const manifest = document.createElement('link');
            manifest.rel = 'manifest';
            manifest.href = '/manifest.json';
            document.head.appendChild(manifest);
        }

        // PWA Service Worker DISABLED to fix caching issues
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.getRegistrations().then(function (registrations) {
                for (let registration of registrations) {
                    registration.unregister();
                }
            });
        }
    }

    async installPWA() {
        if (!this.deferredInstallPrompt) return;

        this.deferredInstallPrompt.prompt();
        const { outcome } = await this.deferredInstallPrompt.userChoice;

        if (outcome === 'accepted') {
            this.notify('INSTALLING', 'Wlaz Ecosystem is being added to your device.', 'fa-download');
            this.deferredInstallPrompt = null;
            const installBtn = document.getElementById('nexus-install-btn');
            if (installBtn) installBtn.style.display = 'none';
        }
    }

    async initSocialHub() {
        try {
            const configPath = new URL('./firebase-config.js', import.meta.url).href;
            const { db, collection, query, orderBy, limit, onSnapshot, doc, setDoc, auth, where } = await import(configPath);
            this.db = db;
            this.collection = collection;
            this.setDoc = setDoc;
            this.doc = doc;

            const feedEl = document.getElementById('nexus-global-feed');
            if (!feedEl) return;

            // Listen to feed collection
            const q = query(collection(db, "global_feed"), orderBy("timestamp", "desc"), limit(5));

            onSnapshot(q, (snapshot) => {
                feedEl.innerHTML = '';
                if (snapshot.empty) {
                    feedEl.innerHTML = '<div class="text-gray-600 text-[10px] text-center p-4">No global activity yet.</div>';
                    return;
                }

                snapshot.forEach(doc => {
                    const data = doc.data();
                    const time = data.timestamp ? new Date(data.timestamp.seconds * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Now';

                    const item = document.createElement('div');
                    item.className = 'feed-item';
                    item.innerHTML = `
                        <div class="feed-icon" style="color: ${data.color || 'var(--wlaz-primary)'}; background: ${data.color || 'var(--wlaz-primary)'}22">
                            <i class="fas ${data.icon || 'fa-bolt'}"></i>
                        </div>
                        <div class="feed-text">
                            <span class="text-white font-bold">${data.user || 'Unknown User'}</span>
                            <span class="text-gray-500">${data.action}</span>
                        </div>
                        <span class="feed-time">${time}</span>
                    `;
                    feedEl.appendChild(item);
                });
            });

            // Log current user login if not logged yet in this session
            if (auth.currentUser && !sessionStorage.getItem('wlaz_logged_event')) {
                this.broadcastEvent(auth.currentUser.displayName, 'joined the ecosystem', 'fa-user-plus', '#2dd4bf');
                sessionStorage.setItem('wlaz_logged_event', 'true');
            }

        } catch (err) {
            console.warn("⚠️ [SocialHub] Connection failed", err);
        }
    }

    async broadcastEvent(user, action, icon, color) {
        if (!this.db) return;
        try {
            const { serverTimestamp } = await import('https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js');
            const eventId = `event_${Date.now()}`;
            await this.setDoc(this.doc(this.db, "global_feed", eventId), {
                user: user.split(' ')[0],
                action: action,
                icon: icon,
                color: color || '#9333ea',
                timestamp: serverTimestamp()
            });
        } catch (e) {
            console.error("Broadcast failed", e);
        }
    }
}

// Init Nexus
document.addEventListener('DOMContentLoaded', () => {
    window.wlazNexus = new CommandNexus();
    console.log("Wlaz Nexus Loaded (Ctrl+K)");

    // --- SIMPLE TRANSITION SYSTEM (Lightweight) ---
    const veil = document.createElement('div');
    veil.id = 'wlaz-transition-veil';
    veil.style.cssText = `
        position: fixed;
        inset: 0;
        background: #030303;
        z-index: 999999;
        opacity: 0;
        pointer-events: none;
        transition: opacity 0.25s ease;
    `;
    document.body.appendChild(veil);

    window.wlazNavigate = (url) => {
        if (window.isNavigating) return;
        window.isNavigating = true;

        veil.style.pointerEvents = 'all';
        veil.style.opacity = '1';

        setTimeout(() => {
            window.location.href = url;
        }, 250);
    };

    // --- WLAZ DOCK SYSTEM ---
    if (true) {
        const dock = document.createElement('div');
        dock.className = 'wlaz-sys-dock';
        dock.innerHTML = `
            <div class="dock-glass" onclick="window.wlazNexus.toggle()">
                <div class="dock-icon-box" style="background: transparent;">
                    <img src="/assets/img/wlaz_logo.png" style="width: 32px; height: 32px;">
                </div>
                <div class="dock-info">
                    <div class="flex items-center gap-2">
                        <span class="status-signal"></span>
                        <span class="dock-title">WLAZ NEXUS</span>
                    </div>
                    <span class="dock-status-text">SYSTEM ONLINE</span>
                </div>
                <div class="dock-shortcut">
                    <span class="key">CTRL</span> + <span class="key">K</span>
                </div>
            </div>
        `;

        const dockStyle = document.createElement('style');
        dockStyle.textContent = `
            .wlaz-sys-dock {
                position: fixed;
                bottom: 24px;
                left: 50%;
                transform: translateX(-50%) translateY(100px);
                z-index: 10000;
                animation: slideUpDock 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards 0.5s;
                font-family: 'JetBrains Mono', monospace;
            }

            @keyframes slideUpDock {
                to { transform: translateX(-50%) translateY(0); }
            }

            .dock-glass {
                display: flex;
                align-items: center;
                gap: 0;
                background: #050505;
                border: 1px solid rgba(255, 255, 255, 0.1);
                padding: 0;
                padding-right: 16px;
                box-shadow: 0 0 0 1px rgba(0,0,0,1), 0 10px 20px -5px rgba(0, 0, 0, 0.5);
                cursor: pointer;
                transition: all 0.2s ease;
            }

            .dock-glass:hover {
                border-color: #9333ea;
                transform: translateY(-2px);
                box-shadow: 0 0 0 1px #9333ea, 0 15px 30px -5px rgba(147, 51, 234, 0.2);
            }

            .dock-icon-box {
                width: 48px;
                height: 48px;
                background: #fff;
                color: #000;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 18px;
                margin-right: 16px;
                transition: all 0.2s ease;
            }

            .dock-glass:hover .dock-icon-box {
                transform: scale(1.1);
            }

            .dock-info {
                display: flex;
                flex-direction: column;
                justify-content: center;
            }

            .dock-title {
                color: #fff;
                font-weight: 700;
                font-size: 13px;
                letter-spacing: 1px;
                text-transform: uppercase;
            }

            .dock-shortcut {
                margin-left: 24px;
                display: flex;
                align-items: center;
                gap: 6px;
                color: #6b7280;
                font-size: 10px;
                font-family: 'JetBrains Mono', monospace;
                padding-left: 24px;
                border-left: 1px solid rgba(255,255,255,0.1);
                height: 24px;
            }

            .status-signal {
                width: 6px;
                height: 6px;
                background: #2dd4bf; /* Accent Teal */
                border-radius: 50%;
                display: inline-block;
                box-shadow: 0 0 10px #2dd4bf;
                animation: pulseSignal 2s infinite;
            }

            .status-signal.activity {
                background: #9333ea;
                box-shadow: 0 0 15px #9333ea;
                transform: scale(1.5);
            }

            .status-signal.focus-mode {
                background: #f472b6; /* Pink */
                box-shadow: 0 0 10px #f472b6;
            }

            @keyframes pulseSignal {
                0% { opacity: 1; transform: scale(1); }
                50% { opacity: 0.4; transform: scale(0.8); }
                100% { opacity: 1; transform: scale(1); }
            }

            .dock-status-text {
                font-size: 8px;
                color: #2dd4bf;
                letter-spacing: 0.5px;
                line-height: 1;
                margin-top: 2px;
            }

            .dock-status-text.focus-mode {
                color: #f472b6;
            }

            .key {
                background: rgba(255,255,255,0.05);
                padding: 2px 6px;
                border: 1px solid rgba(255,255,255,0.1);
                color: #9ca3af;
                transition: all 0.2s;
            }

            .dock-glass:hover .key {
                border-color: #9333ea;
                color: #d8b4fe;
                 background: rgba(147, 51, 234, 0.1);
            }

            .nexus-item, .arcade-card, .bento-card {
                will-change: transform, opacity;
            }

            @media (max-width: 768px) {
                .wlaz-sys-dock { bottom: 85px; } /* Lift above mobile navs */
                .dock-shortcut { display: none; }
                .dock-title { font-size: 11px; }
                .dock-glass { padding-right: 12px; }
                .dock-icon-box { width: 36px; height: 36px; font-size: 14px; margin-right: 8px; }
            }
        `;
        document.head.appendChild(dockStyle);
        document.body.appendChild(dock);
    }
});
