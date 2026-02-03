// Main JavaScript for Mockup API Server - YC-Level Enhanced

// === TOAST SYSTEM ===
function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        console.error('Toast container not found');
        return;
    }
    
    // Map type to Bootstrap color classes
    const colorMap = {
        'success': 'bg-success',
        'danger': 'bg-danger',
        'warning': 'bg-warning',
        'info': 'bg-info',
        'primary': 'bg-primary'
    };
    
    const bgClass = colorMap[type] || 'bg-info';
    
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white ${bgClass} border-0 toast-enter`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    // Add icon based on type
    const icons = {
        'success': 'bi-check-circle-fill',
        'danger': 'bi-exclamation-triangle-fill',
        'warning': 'bi-exclamation-circle-fill',
        'info': 'bi-info-circle-fill',
        'primary': 'bi-bell-fill'
    };
    const icon = icons[type] || icons['info'];
    
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <i class="bi ${icon} me-2"></i>${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" 
                    data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;
    
    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast, {
        autohide: true,
        delay: 3000
    });
    bsToast.show();
    
    // Remove toast after it's hidden
    toast.addEventListener('hidden.bs.toast', function() {
        toast.classList.add('toast-exit');
        setTimeout(() => toast.remove(), 200);
    });
}

// === COPY TO CLIPBOARD ===
function copyToClipboard(text, successMessage = 'Copied to clipboard!') {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(function() {
            showToast(successMessage, 'success');
        }).catch(function(err) {
            console.error('Failed to copy text: ', err);
            fallbackCopyToClipboard(text, successMessage);
        });
    } else {
        fallbackCopyToClipboard(text, successMessage);
    }
}

// Fallback copy method for older browsers
function fallbackCopyToClipboard(text, successMessage = 'Copied to clipboard!') {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    document.body.appendChild(textArea);
    textArea.select();
    
    try {
        document.execCommand('copy');
        showToast(successMessage, 'success');
    } catch (err) {
        console.error('Fallback: Failed to copy', err);
        showToast('Failed to copy to clipboard', 'danger');
    }
    
    document.body.removeChild(textArea);
}

// === INITIALIZE COPY BUTTONS ===
document.addEventListener('DOMContentLoaded', function() {
    // Initialize all elements with data-copy attribute
    document.querySelectorAll('[data-copy]').forEach(function(element) {
        element.style.cursor = 'pointer';
        element.addEventListener('click', function() {
            const textToCopy = this.getAttribute('data-copy');
            const message = this.getAttribute('data-copy-message') || 'Copied!';
            copyToClipboard(textToCopy, message);
            
            // Visual feedback
            this.classList.add('copy-success');
            setTimeout(() => {
                this.classList.remove('copy-success');
            }, 300);
        });
    });
});

// === HEADER MANAGEMENT (for mock endpoint forms) ===
let headerCount = 0;

function addHeader(headerName = '', headerValue = '') {
    const container = document.getElementById('headers-container');
    const headerRow = document.createElement('div');
    headerRow.className = 'header-row row mb-2';
    headerRow.id = `header-${headerCount}`;
    
    headerRow.innerHTML = `
        <div class="col-md-5">
            <input type="text" class="form-control form-control-clean" 
                   name="headers[${headerCount}].headerKey" 
                   placeholder="Header name (e.g., X-Custom-Header)" 
                   value="${headerName}" required>
        </div>
        <div class="col-md-6">
            <input type="text" class="form-control form-control-clean" 
                   name="headers[${headerCount}].headerValue" 
                   placeholder="Header value" 
                   value="${headerValue}" required>
        </div>
        <div class="col-md-1">
            <button type="button" class="btn btn-danger btn-sm w-100" 
                    onclick="removeHeader(${headerCount})">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;
    
    container.appendChild(headerRow);
    headerCount++;
}

function removeHeader(id) {
    const headerRow = document.getElementById(`header-${id}`);
    if (headerRow) {
        headerRow.remove();
    }
}

// === AUTO-DISMISS ALERTS ===
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert-clean:not(.alert-info)');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.3s';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });
});

// === SET DEFAULT EXPIRATION DATE ===
document.addEventListener('DOMContentLoaded', function() {
    const expirationDateInput = document.getElementById('expirationDate');
    if (expirationDateInput && !expirationDateInput.value) {
        const defaultDate = new Date();
        defaultDate.setDate(defaultDate.getDate() + 7);
        
        // Format date as YYYY-MM-DDTHH:mm for datetime-local input
        const year = defaultDate.getFullYear();
        const month = String(defaultDate.getMonth() + 1).padStart(2, '0');
        const day = String(defaultDate.getDate()).padStart(2, '0');
        const hours = String(defaultDate.getHours()).padStart(2, '0');
        const minutes = String(defaultDate.getMinutes()).padStart(2, '0');
        
        expirationDateInput.value = `${year}-${month}-${day}T${hours}:${minutes}`;
    }
});

// === FORMAT JSON UTILITY ===
function formatJSON() {
    const responseBodyTextarea = document.getElementById('responseBody');
    if (responseBodyTextarea) {
        try {
            const json = JSON.parse(responseBodyTextarea.value);
            responseBodyTextarea.value = JSON.stringify(json, null, 2);
            showToast('JSON formatted successfully', 'success');
        } catch (e) {
            showToast('Invalid JSON format', 'danger');
        }
    }
}

// Add format JSON button if response body exists
document.addEventListener('DOMContentLoaded', function() {
    const responseBodyTextarea = document.getElementById('responseBody');
    if (responseBodyTextarea) {
        const formatButton = document.createElement('button');
        formatButton.type = 'button';
        formatButton.className = 'btn-clean btn-soft btn-sm mt-2';
        formatButton.innerHTML = '<i class="bi bi-code"></i> Format JSON';
        formatButton.onclick = formatJSON;
        
        responseBodyTextarea.parentNode.insertBefore(formatButton, responseBodyTextarea.nextSibling);
    }
});

// === FORM VALIDATION ===
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;
    
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return false;
    }
    
    return true;
}

// === CONFIRM DELETE ===
function confirmDelete(message = 'Are you sure you want to delete this item?') {
    return confirm(message);
}

// === KEYBOARD SHORTCUTS ===
document.addEventListener('keydown', function(e) {
    // Ctrl+S or Cmd+S to save form
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        const submitButton = document.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.click();
        }
    }
});

// === MOBILE NAVBAR TOGGLE ===
document.addEventListener('DOMContentLoaded', function() {
    const navbarToggler = document.querySelector('.navbar-toggler');
    if (navbarToggler) {
        navbarToggler.addEventListener('click', function() {
            const navbar = document.querySelector('#navbarNav');
            if (navbar) {
                navbar.classList.toggle('show');
            }
        });
    }
});


// Form validation helper
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;
    
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return false;
    }
    
    return true;
}

// Confirm delete action
function confirmDelete(message = 'Are you sure you want to delete this item?') {
    return confirm(message);
}

// Format JSON in response body textarea
function formatJSON() {
    const responseBodyTextarea = document.getElementById('responseBody');
    if (responseBodyTextarea) {
        try {
            const json = JSON.parse(responseBodyTextarea.value);
            responseBodyTextarea.value = JSON.stringify(json, null, 2);
            showToast('JSON formatted successfully', 'success');
        } catch (e) {
            showToast('Invalid JSON format', 'danger');
        }
    }
}

// Add format JSON button if response body exists
document.addEventListener('DOMContentLoaded', function() {
    const responseBodyTextarea = document.getElementById('responseBody');
    if (responseBodyTextarea) {
        const formatButton = document.createElement('button');
        formatButton.type = 'button';
        formatButton.className = 'btn btn-sm btn-outline-secondary mt-1';
        formatButton.innerHTML = '<i class="bi bi-code"></i> Format JSON';
        formatButton.onclick = formatJSON;
        
        responseBodyTextarea.parentNode.insertBefore(formatButton, responseBodyTextarea.nextSibling);
    }
});

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // Ctrl+S or Cmd+S to save form
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        const submitButton = document.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.click();
        }
    }
});
