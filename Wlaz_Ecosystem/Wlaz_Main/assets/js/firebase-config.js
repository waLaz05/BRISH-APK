// WLAZ ECOSYSTEM - SHARED FIREBASE CONFIGURATION
import { initializeApp, getApps, getApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";
import { getAuth, GoogleAuthProvider, signInWithPopup, signOut, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js";
import { getFirestore, doc, setDoc, getDoc, getDocs, onSnapshot, collection, query, orderBy, limit } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js";

const firebaseConfig = {
    apiKey: "AIzaSyCb-HjxqVcHkuP8lCqEK2aYxL9ekFdqXag",
    authDomain: "wlaz-ecosystem.firebaseapp.com",
    projectId: "wlaz-ecosystem",
    storageBucket: "wlaz-ecosystem.firebasestorage.app",
    messagingSenderId: "979800416218",
    appId: "1:979800416218:web:77283fff97ed0f51372ae3"
};

// Initialize Firebase (Singleton Pattern)
let app;
if (getApps().length > 0) {
    app = getApp();
} else {
    app = initializeApp(firebaseConfig);
}

const auth = getAuth(app);
const db = getFirestore(app);
const googleProvider = new GoogleAuthProvider();

export {
    auth, db, googleProvider,
    signInWithPopup, signOut, onAuthStateChanged,
    doc, setDoc, getDoc, getDocs, onSnapshot, collection, query, orderBy, limit
};
