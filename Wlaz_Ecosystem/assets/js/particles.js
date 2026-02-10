class ParticleNetwork {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) return;
        this.ctx = this.canvas.getContext('2d');
        this.particles = [];
        this.hoverParticle = null;

        this.resize();
        this.init();

        window.addEventListener('resize', () => this.resize());
        window.addEventListener('mousemove', (e) => this.handleMouseMove(e));
        window.addEventListener('mouseleave', () => this.hoverParticle = null);

        this.animate();
    }

    resize() {
        this.canvas.width = window.innerWidth;
        this.canvas.height = window.innerHeight;
    }

    init() {
        this.particles = [];
        // Slight reduction in density for cleaner look on the new deep dark background
        const particleCount = Math.min(80, (window.innerWidth * window.innerHeight) / 12000);

        for (let i = 0; i < particleCount; i++) {
            this.particles.push(new Particle(this.canvas));
        }
    }

    handleMouseMove(e) {
        if (!this.hoverParticle) {
            this.hoverParticle = new Particle(this.canvas);
        }
        this.hoverParticle.x = e.clientX;
        this.hoverParticle.y = e.clientY;
        this.hoverParticle.isMouse = true;
    }

    animate() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Update and draw particles
        this.particles.forEach(p => {
            p.update();
            p.draw(this.ctx);
        });

        // Draw connections
        this.drawConnections();

        requestAnimationFrame(() => this.animate());
    }

    drawConnections() {
        const allParticles = [...this.particles];
        if (this.hoverParticle) allParticles.push(this.hoverParticle);

        for (let i = 0; i < allParticles.length; i++) {
            for (let j = i + 1; j < allParticles.length; j++) {
                const p1 = allParticles[i];
                const p2 = allParticles[j];
                const dx = p1.x - p2.x;
                const dy = p1.y - p2.y;
                const dist = Math.sqrt(dx * dx + dy * dy);

                // Increased connection distance for more "net-like" feel
                const maxDist = p1.isMouse || p2.isMouse ? 200 : 140;

                if (dist < maxDist) {
                    const opacity = 1 - (dist / maxDist);
                    this.ctx.beginPath();
                    // BRAND COLOR REFINEMENT: Primary Purple #9333ea (147, 51, 234)
                    this.ctx.strokeStyle = `rgba(147, 51, 234, ${opacity * 0.25})`;
                    this.ctx.lineWidth = 1;
                    this.ctx.moveTo(p1.x, p1.y);
                    this.ctx.lineTo(p2.x, p2.y);
                    this.ctx.stroke();
                }
            }
        }
    }
}

class Particle {
    constructor(canvas) {
        this.canvas = canvas;
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;
        this.vx = (Math.random() - 0.5) * 0.3; // Slower, more elegant movement
        this.vy = (Math.random() - 0.5) * 0.3;
        this.size = Math.random() * 2 + 0.5;
        this.isMouse = false;
    }

    update() {
        if (this.isMouse) return;

        this.x += this.vx;
        this.y += this.vy;

        if (this.x < 0 || this.x > this.canvas.width) this.vx *= -1;
        if (this.y < 0 || this.y > this.canvas.height) this.vy *= -1;
    }

    draw(ctx) {
        ctx.beginPath();
        // BRAND COLOR REFINEMENT: Accent Teal #2dd4bf (45, 212, 191) for particles
        const color = this.isMouse ? 'rgba(255, 255, 255, 0.8)' : 'rgba(45, 212, 191, 0.4)';
        ctx.fillStyle = color;
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
    }
}

// Auto-initialize if canvas exists
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('particle-canvas')) {
        new ParticleNetwork('particle-canvas');
    }
});
