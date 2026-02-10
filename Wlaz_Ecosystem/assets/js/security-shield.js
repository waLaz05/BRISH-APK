/**
 * ═══════════════════════════════════════════════════════════════════════════
 * 🛡️ WLAZ SECURITY SHIELD v2.1.0
 * Unified Security Layer for the Wlaz Ecosystem
 * This script should be loaded FIRST, before any other scripts.
 * ═══════════════════════════════════════════════════════════════════════════
 */

(function () {
    'use strict';

    // 🛡️ SECURITY CONFIGURATION
    const WLAZ_SECURITY = {
        version: '2.1.0',
        enabled: true,
        strictMode: true,
        debugMode: false // Set to true for development
    };

    // ═══════════════════════════════════════════════════════════════════
    // 1. ANTI-IFRAME EMBEDDING (Clickjacking Protection)
    // ═══════════════════════════════════════════════════════════════════
    if (window.self !== window.top) {
        try {
            window.top.location = window.self.location;
        } catch (e) {
            document.documentElement.style.display = 'none';
            document.body.innerHTML = '<div style="color:red;text-align:center;padding:50px;font-family:sans-serif;"><h1>⚠️ Acceso no autorizado</h1><p>Esta página no puede mostrarse en un iframe.</p></div>';
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. INPUT SANITIZATION UTILITIES
    // ═══════════════════════════════════════════════════════════════════
    window.WlazSecurity = {
        version: WLAZ_SECURITY.version,

        // Sanitizar HTML para prevenir XSS
        sanitizeHTML: function (str) {
            if (typeof str !== 'string') return '';
            const div = document.createElement('div');
            div.textContent = str;
            return div.innerHTML;
        },

        // Sanitizar para atributos
        sanitizeAttribute: function (str) {
            if (typeof str !== 'string') return '';
            return str.replace(/['"<>&]/g, function (char) {
                const entities = {
                    "'": '&#39;',
                    '"': '&quot;',
                    '<': '&lt;',
                    '>': '&gt;',
                    '&': '&amp;'
                };
                return entities[char] || char;
            });
        },

        // Sanitizar URLs
        sanitizeURL: function (url) {
            if (typeof url !== 'string') return '';
            const clean = url.trim().toLowerCase();
            if (clean.startsWith('javascript:') ||
                clean.startsWith('data:text') ||
                clean.startsWith('vbscript:') ||
                clean.includes('&#') ||
                clean.includes('%3c') ||
                clean.includes('%3e')) {
                console.warn('🛡️ URL maliciosa bloqueada:', url);
                return '#blocked';
            }
            return url;
        },

        // Validar y limpiar JSON
        safeJSONParse: function (str) {
            try {
                return JSON.parse(str);
            } catch (e) {
                console.warn('🛡️ JSON inválido bloqueado');
                return null;
            }
        },

        // Escapar para uso en regex
        escapeRegex: function (str) {
            return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        },

        // Sanitizar número (evitar NaN)
        sanitizeNumber: function (val, defaultVal = 0) {
            const num = parseFloat(val);
            return isNaN(num) ? defaultVal : num;
        }
    };

    // ═══════════════════════════════════════════════════════════════════
    // 3. PROTOTYPE POLLUTION PROTECTION
    // ═══════════════════════════════════════════════════════════════════
    try {
        Object.freeze(Object.prototype);
        Object.freeze(Array.prototype);
    } catch (e) {
        // Some environments don't allow freezing prototypes
    }

    // ═══════════════════════════════════════════════════════════════════
    // 4. DOM PROTECTION - Interceptar manipulaciones peligrosas
    // ═══════════════════════════════════════════════════════════════════
    const originalInnerHTML = Object.getOwnPropertyDescriptor(Element.prototype, 'innerHTML');

    if (originalInnerHTML && originalInnerHTML.set) {
        Object.defineProperty(Element.prototype, 'innerHTML', {
            set: function (value) {
                if (typeof value === 'string') {
                    // Detectar patrones peligrosos (más permisivo para templates legítimos)
                    const dangerous = /<script\b[^>]*>[\s\S]*?<\/script>/gi;
                    if (dangerous.test(value) && !this.hasAttribute('data-allow-scripts')) {
                        if (WLAZ_SECURITY.debugMode) {
                            console.warn('🛡️ Script tag detectado en innerHTML, removiendo...');
                        }
                        value = value.replace(dangerous, '<!-- script removed -->');
                    }
                }
                return originalInnerHTML.set.call(this, value);
            },
            get: originalInnerHTML.get,
            configurable: true // Allow reconfiguration for frameworks
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    // 5. EVAL BLOCKER (Opcional - puede romper algunas librerías)
    // ═══════════════════════════════════════════════════════════════════
    if (WLAZ_SECURITY.strictMode) {
        const originalEval = window.eval;
        window.eval = function (code) {
            console.error('🛡️ eval() bloqueado. Si necesitas esta funcionalidad, contacta al administrador.');
            throw new Error('eval() está deshabilitado por políticas de seguridad');
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    // 6. STORAGE PROTECTION - Secure Storage Wrapper
    // ═══════════════════════════════════════════════════════════════════
    window.WlazSecureStorage = {
        _prefix: 'wlaz_secure_',

        _encode: function (data) {
            try {
                return btoa(unescape(encodeURIComponent(JSON.stringify(data))));
            } catch (e) {
                return null;
            }
        },

        _decode: function (data) {
            try {
                return JSON.parse(decodeURIComponent(escape(atob(data))));
            } catch (e) {
                return null;
            }
        },

        setSecure: function (key, value) {
            const encoded = this._encode(value);
            if (encoded) {
                localStorage.setItem(this._prefix + key, encoded);
                return true;
            }
            return false;
        },

        getSecure: function (key) {
            const data = localStorage.getItem(this._prefix + key);
            return data ? this._decode(data) : null;
        },

        removeSecure: function (key) {
            localStorage.removeItem(this._prefix + key);
        }
    };

    // ═══════════════════════════════════════════════════════════════════
    // 7. NETWORK REQUEST PROTECTION
    // ═══════════════════════════════════════════════════════════════════
    const originalFetch = window.fetch;
    window.fetch = function (url, options) {
        const urlStr = typeof url === 'string' ? url : url.toString();
        const sanitizedURL = WlazSecurity.sanitizeURL(urlStr);

        if (sanitizedURL === '#blocked') {
            console.error('🛡️ Fetch a URL maliciosa bloqueado:', urlStr);
            return Promise.reject(new Error('URL bloqueada por seguridad'));
        }

        options = options || {};
        options.credentials = options.credentials || 'same-origin';

        return originalFetch.call(this, typeof url === 'string' ? sanitizedURL : url, options);
    };

    // ═══════════════════════════════════════════════════════════════════
    // 8. FORM PROTECTION (DOMContentLoaded)
    // ═══════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        // Agregar protección a formularios
        document.querySelectorAll('form').forEach(function (form) {
            const action = form.getAttribute('action') || '';
            if (action.toLowerCase().includes('javascript:')) {
                form.setAttribute('action', '#');
                console.warn('🛡️ Action de formulario sospechoso neutralizado');
            }

            form.addEventListener('submit', function (e) {
                try {
                    const formAction = new URL(form.action, window.location.href);
                    const allowedDomains = [
                        window.location.origin,
                        'https://wlaz-ecosystem.web.app',
                        'https://wlaz-ecosystem.firebaseapp.com'
                    ];

                    if (!allowedDomains.some(d => formAction.origin === d || formAction.origin.includes('localhost'))) {
                        e.preventDefault();
                        console.error('🛡️ Envío a dominio externo bloqueado:', formAction.origin);
                    }
                } catch (err) {
                    // URL parsing failed, likely a relative URL - allow
                }
            });
        });

        // Sanitizar inputs en tiempo real
        document.querySelectorAll('input[type="text"], textarea').forEach(function (input) {
            input.addEventListener('blur', function () {
                const dangerous = /<script|javascript:|on\w+\s*=/i;
                if (dangerous.test(this.value)) {
                    console.warn('🛡️ Contenido potencialmente peligroso detectado en input');
                    this.value = WlazSecurity.sanitizeHTML(this.value);
                }
            });
        });
    });

    // ═══════════════════════════════════════════════════════════════════
    // 9. TIMING ATTACK MITIGATION
    // ═══════════════════════════════════════════════════════════════════
    const originalNow = Date.now;
    Date.now = function () {
        return Math.round(originalNow() / 10) * 10; // Reduce precision
    };

    // ═══════════════════════════════════════════════════════════════════
    // 10. CONSOLE WARNING (Anti-Social Engineering)
    // ═══════════════════════════════════════════════════════════════════
    console.log('%c🛡️ WLAZ Security Shield v' + WLAZ_SECURITY.version + ' Activo',
        'color: #10b981; font-size: 14px; font-weight: bold; padding: 10px 0;');
    console.log('%c⚠️ ADVERTENCIA: Esta consola es solo para desarrolladores.',
        'color: #f59e0b; font-size: 12px;');
    console.log('%c🚫 NUNCA pegues código aquí si alguien te lo pide. Esto podría comprometer tu cuenta.',
        'color: #ef4444; font-size: 12px; font-weight: bold;');

    // Mark as loaded
    window.WLAZ_SECURITY_LOADED = WLAZ_SECURITY.version;

})();
