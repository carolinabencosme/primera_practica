/**
 * Tech Stack Interactive JavaScript
 * Y Combinator Style - Smooth animations and interactions
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // Intersection Observer for scroll animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                // Add staggered delay for cards in the same section
                setTimeout(() => {
                    entry.target.classList.add('fade-in');
                }, index * 50);
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Observe all tech cards for scroll animations
    const techCards = document.querySelectorAll('.tech-card');
    techCards.forEach(card => {
        card.classList.add('fade-in-delayed');
        observer.observe(card);
    });

    // Observe category sections
    const categorySections = document.querySelectorAll('.category-section');
    categorySections.forEach(section => {
        observer.observe(section);
    });

    // Copy to clipboard functionality for version numbers
    const versionElements = document.querySelectorAll('.tech-version');
    versionElements.forEach(versionEl => {
        versionEl.addEventListener('click', function(e) {
            e.stopPropagation();
            const version = this.getAttribute('data-version');
            const techName = this.closest('.tech-card').querySelector('.tech-name').textContent;
            
            // Create temporary text for clipboard
            const textToCopy = `${techName} ${version}`;
            
            // Modern clipboard API
            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(textToCopy).then(() => {
                    showCopyFeedback(`Copied: ${textToCopy}`);
                }).catch(err => {
                    console.error('Failed to copy:', err);
                });
            } else {
                // Fallback for older browsers
                const textArea = document.createElement('textarea');
                textArea.value = textToCopy;
                textArea.style.position = 'fixed';
                textArea.style.left = '-999999px';
                document.body.appendChild(textArea);
                textArea.select();
                try {
                    document.execCommand('copy');
                    showCopyFeedback(`Copied: ${textToCopy}`);
                } catch (err) {
                    console.error('Failed to copy:', err);
                }
                document.body.removeChild(textArea);
            }
        });

        // Add tooltip on hover
        versionEl.setAttribute('title', 'Click to copy version');
    });

    // Enhanced hover effect for cards
    techCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });

    // Keyboard navigation support
    techCards.forEach(card => {
        card.setAttribute('tabindex', '0');
        card.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                const versionEl = this.querySelector('.tech-version');
                if (versionEl) {
                    versionEl.click();
                }
            }
        });
    });

    // Show copy feedback message
    function showCopyFeedback(message) {
        // Remove existing feedback if any
        const existingFeedback = document.querySelector('.copy-feedback');
        if (existingFeedback) {
            existingFeedback.remove();
        }

        // Create feedback element
        const feedback = document.createElement('div');
        feedback.className = 'copy-feedback';
        feedback.innerHTML = `<i class="bi bi-check-circle-fill"></i> ${message}`;
        document.body.appendChild(feedback);

        // Remove after 2 seconds
        setTimeout(() => {
            feedback.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => {
                feedback.remove();
            }, 300);
        }, 2000);
    }

    // Add fadeOut animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeOut {
            from {
                opacity: 1;
                transform: translateY(0);
            }
            to {
                opacity: 0;
                transform: translateY(20px);
            }
        }
    `;
    document.head.appendChild(style);

    // Smooth scroll to sections (if we add navigation)
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Add parallax effect to hero section (subtle)
    const hero = document.querySelector('.tech-hero');
    if (hero) {
        window.addEventListener('scroll', function() {
            const scrolled = window.pageYOffset;
            const parallax = scrolled * 0.5;
            hero.style.transform = `translateY(${parallax}px)`;
        });
    }

    // Card click analytics (optional - can track which tech is most viewed)
    techCards.forEach(card => {
        card.addEventListener('click', function() {
            const techName = this.querySelector('.tech-name').textContent;
            const category = this.getAttribute('data-category');
            
            // Could send to analytics service
            console.log(`Tech card clicked: ${techName} (${category})`);
            
            // Add a subtle pulse effect
            this.style.animation = 'pulse 0.3s ease-in-out';
            setTimeout(() => {
                this.style.animation = '';
            }, 300);
        });
    });

    // Add pulse animation
    const pulseStyle = document.createElement('style');
    pulseStyle.textContent = `
        @keyframes pulse {
            0%, 100% {
                transform: scale(1);
            }
            50% {
                transform: scale(1.02);
            }
        }
    `;
    document.head.appendChild(pulseStyle);

    // Filter functionality (optional enhancement)
    // This could be activated if filter buttons are added to the UI
    window.filterTechStack = function(category) {
        techCards.forEach(card => {
            if (category === 'all' || card.getAttribute('data-category') === category) {
                card.style.display = 'block';
                setTimeout(() => {
                    card.style.opacity = '1';
                    card.style.transform = 'scale(1)';
                }, 50);
            } else {
                card.style.opacity = '0';
                card.style.transform = 'scale(0.8)';
                setTimeout(() => {
                    card.style.display = 'none';
                }, 300);
            }
        });
    };

    // Performance: Lazy load images if needed
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                        imageObserver.unobserve(img);
                    }
                }
            });
        });

        document.querySelectorAll('img[data-src]').forEach(img => {
            imageObserver.observe(img);
        });
    }

    // Log initialization
    console.log('Tech Stack page initialized successfully');
    console.log(`Loaded ${techCards.length} technology cards`);
});
