/**
 * Matrix Background Effect
 * Creates a falling code/matrix animation similar to Velum Labs
 */

class MatrixBackground {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) {
            console.warn('Matrix canvas not found');
            return;
        }
        
        this.ctx = this.canvas.getContext('2d');
        this.fontSize = 14;
        this.columns = 0;
        this.drops = [];
        this.animationId = null;
        
        // Characters to display - mix of numbers, letters, and symbols
        this.characters = '01アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン';
        this.charactersArray = this.characters.split('');
        
        this.init();
        this.setupResizeListener();
    }
    
    init() {
        this.resizeCanvas();
        this.initDrops();
        this.animate();
    }
    
    resizeCanvas() {
        // Set canvas size to fill parent container
        const parent = this.canvas.parentElement;
        this.canvas.width = parent.offsetWidth;
        this.canvas.height = parent.offsetHeight;
        
        // Recalculate columns
        this.columns = Math.floor(this.canvas.width / this.fontSize);
        this.initDrops();
    }
    
    initDrops() {
        // Initialize drop positions
        this.drops = [];
        for (let i = 0; i < this.columns; i++) {
            // Randomize initial positions for a more dynamic start
            this.drops[i] = Math.random() * -100;
        }
    }
    
    setupResizeListener() {
        let resizeTimeout;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(() => {
                this.resizeCanvas();
            }, 250);
        });
    }
    
    draw() {
        // Create fade effect by drawing semi-transparent black rectangle
        this.ctx.fillStyle = 'rgba(10, 10, 10, 0.05)';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        // Set text style
        this.ctx.font = `${this.fontSize}px monospace`;
        
        // Draw characters
        for (let i = 0; i < this.drops.length; i++) {
            // Random character
            const char = this.charactersArray[Math.floor(Math.random() * this.charactersArray.length)];
            
            // Create gradient for each character (purple to blue)
            const x = i * this.fontSize;
            const y = this.drops[i] * this.fontSize;
            
            // Use different colors based on position for variety
            const colorChoice = Math.random();
            if (colorChoice < 0.3) {
                this.ctx.fillStyle = '#7c3aed'; // Purple
            } else if (colorChoice < 0.6) {
                this.ctx.fillStyle = '#3b82f6'; // Blue
            } else {
                this.ctx.fillStyle = '#06b6d4'; // Cyan
            }
            
            // Draw the character
            this.ctx.fillText(char, x, y);
            
            // Reset drop to top randomly
            if (y > this.canvas.height && Math.random() > 0.975) {
                this.drops[i] = 0;
            }
            
            // Increment Y coordinate
            this.drops[i]++;
        }
    }
    
    animate() {
        this.draw();
        this.animationId = requestAnimationFrame(() => this.animate());
    }
    
    stop() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
    }
    
    start() {
        if (!this.animationId) {
            this.animate();
        }
    }
}

// Initialize matrix background when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    const matrixCanvas = document.getElementById('matrix-bg');
    if (matrixCanvas) {
        const matrix = new MatrixBackground('matrix-bg');
        
        // Optional: Pause animation when tab is not visible to save resources
        document.addEventListener('visibilitychange', function() {
            if (document.hidden) {
                matrix.stop();
            } else {
                matrix.start();
            }
        });
        
        console.log('Matrix background initialized');
    }
});
