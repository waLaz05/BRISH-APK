/**
 * Wlaz Ecosystem Notification System
 * A shared, premium toast notification manager.
 */

class ToastManager {
    constructor() {
        this.container = null;
        this.queue = [];
        this.isProcessing = false;
        this.init();
    }

    init() {
        // Inject Styles dynamically
        const style = document.createElement('style');
        style.textContent = `
            .wlaz-toast-container {
                position: fixed;
                bottom: 24px;
                right: 24px;
                z-index: 10000;
                display: flex;
                flex-direction: column;
                gap: 12px;
                pointer-events: none;
            }

            .wlaz-toast {
                background: rgba(15, 15, 18, 0.95);
                backdrop-filter: blur(16px);
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-left: 4px solid #7c3aed; /* Default Primary */
                padding: 16px 24px;
                border-radius: 12px;
                color: #fff;
                font-family: 'Inter', system-ui, sans-serif;
                font-size: 14px;
                min-width: 300px;
                max-width: 400px;
                box-shadow: 0 10px 40px -10px rgba(0,0,0,0.5);
                display: flex;
                align-items: center;
                gap: 16px;
                
                transform: translateX(120%);
                opacity: 0;
                transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
                pointer-events: auto;
            }

            .wlaz-toast.show {
                transform: translateX(0);
                opacity: 1;
            }

            .wlaz-toast.hiding {
                transform: translateX(120%);
                opacity: 0;
            }

            .wlaz-toast-icon {
                font-size: 18px;
                flex-shrink: 0;
            }

            .wlaz-toast-content {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }

            .wlaz-toast-title {
                font-weight: 600;
                letter-spacing: 0.5px;
                text-transform: uppercase;
                font-size: 11px;
                opacity: 0.7;
            }

            .wlaz-toast-message {
                font-weight: 500;
                line-height: 1.4;
            }

            /* Types */
            .wlaz-toast-success { border-left-color: #10b981; }
            .wlaz-toast-success .wlaz-toast-icon { color: #10b981; }

            .wlaz-toast-error { border-left-color: #ef4444; }
            .wlaz-toast-error .wlaz-toast-icon { color: #ef4444; }

            .wlaz-toast-warning { border-left-color: #f59e0b; }
            .wlaz-toast-warning .wlaz-toast-icon { color: #f59e0b; }

            .wlaz-toast-info { border-left-color: #3b82f6; }
            .wlaz-toast-info .wlaz-toast-icon { color: #3b82f6; }
        `;
        document.head.appendChild(style);

        // Create Container
        this.container = document.createElement('div');
        this.container.className = 'wlaz-toast-container';
        document.body.appendChild(this.container);
    }

    /**
     * Show a toast notification
     * @param {string} message - The main message
     * @param {string} type - 'success', 'error', 'warning', 'info'
     * @param {string} title - Optional small title above message
     */
    show(message, type = 'info', title = '') {
        const icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            warning: 'fa-triangle-exclamation',
            info: 'fa-info-circle'
        };

        const iconClass = icons[type] || icons.info;
        const typeClass = `wlaz-toast-${type}`;

        // Auto title if not provided
        if (!title) {
            const titles = {
                success: 'Operation Successful',
                error: 'System Error',
                warning: 'Attention Needed',
                info: 'System Notice'
            };
            title = titles[type] || 'Notice';
        }

        const toast = document.createElement('div');
        toast.className = `wlaz-toast ${typeClass}`;
        toast.innerHTML = `
            <i class="fas ${iconClass} wlaz-toast-icon"></i>
            <div class="wlaz-toast-content">
                <span class="wlaz-toast-title">${title}</span>
                <span class="wlaz-toast-message">${message}</span>
            </div>
        `;

        this.container.appendChild(toast);

        // Trigger reflow for animation
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        // Auto remove
        setTimeout(() => {
            toast.classList.add('hiding');
            toast.addEventListener('transitionend', () => {
                toast.remove();
            });
        }, 4000);
    }
}

// Global Instance
window.wlazToast = new ToastManager();

// Overwrite native alert (optional, but good for catching strays)
window.alert = (msg) => {
    window.wlazToast.show(msg, 'info', 'System Alert');
};
