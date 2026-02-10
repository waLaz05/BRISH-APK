/**
 * Wlaz Ecosystem - Advanced Animations v3.0
 * Cinematic interactions, magnetic buttons.
 */

document.addEventListener('DOMContentLoaded', () => {

    // --- TEXT REVEAL (SplitType equivalent) ---
    // Finds elements with .animate-text-reveal and splits them
    // Note: The CSS handles the gradient, here we handle stagger if class is .split-text
    const splitTargets = document.querySelectorAll('.split-reveal');
    splitTargets.forEach(target => {
        const text = target.innerText;
        target.innerHTML = '';
        [...text].forEach((char, i) => {
            const span = document.createElement('span');
            span.innerText = char;
            span.className = 'char';
            span.style.animationDelay = `${i * 0.05}s`;
            if (char === ' ') span.style.width = '0.5em'; // Preserve spaces
            target.appendChild(span);
        });
    });


    // --- MAGNETIC BUTTONS ---
    // Elements with class .btn-magnetic will have a magnetic pull
    // --- MAGNETIC BUTTONS (Optimized with LERP) ---
    // Elements with class .btn-magnetic will have a smooth magnetic pull
    const magnets = document.querySelectorAll('.btn-magnetic');

    magnets.forEach(btn => {
        let bounds;
        let targetX = 0;
        let targetY = 0;
        let currentX = 0;
        let currentY = 0;
        let isHovering = false;
        let rafId = null;

        // Configuration
        const STRENGTH = 0.2; // Reduced from 0.3 for subtler movement
        const SMOOTHING = 0.1; // Lower = smoother/slower catchup

        function update() {
            if (!isHovering && Math.abs(currentX) < 0.1 && Math.abs(currentY) < 0.1) {
                // Stop animation when close to 0 and not hovering
                btn.style.transform = '';
                if (btn.children[0]) btn.children[0].style.transform = '';
                rafId = null;
                return;
            }

            // LERP
            currentX += (targetX - currentX) * SMOOTHING;
            currentY += (targetY - currentY) * SMOOTHING;

            btn.style.transform = `translate3d(${currentX}px, ${currentY}px, 0)`;

            // Subtle parallax for child content
            if (btn.children[0]) {
                const childX = currentX * 0.5;
                const childY = currentY * 0.5;
                btn.children[0].style.transform = `translate3d(${childX}px, ${childY}px, 0)`;
            }

            rafId = requestAnimationFrame(update);
        }

        btn.addEventListener('mouseenter', () => {
            isHovering = true;
            bounds = btn.getBoundingClientRect();
            if (!rafId) update();
        });

        btn.addEventListener('mousemove', (e) => {
            if (!isHovering) return;
            const x = e.clientX - bounds.left - bounds.width / 2;
            const y = e.clientY - bounds.top - bounds.height / 2;

            targetX = x * STRENGTH;
            targetY = y * STRENGTH;
        });

        btn.addEventListener('mouseleave', () => {
            isHovering = false;
            targetX = 0;
            targetY = 0;
        });
    });

    console.log('Wlaz Erasure Engine v3.0 // Ready (Cursor removed)');
});
