// Main JavaScript for Mockup API Server

// Header management for mock endpoint forms
let headerCount = 0;

function addHeader(headerName = '', headerValue = '') {
    const container = document.getElementById('headers-container');
    const headerRow = document.createElement('div');
    headerRow.className = 'header-row row mb-2';
    headerRow.id = `header-${headerCount}`;
    
    headerRow.innerHTML = `
        <div class="col-md-5">
            <input type="text" class="form-control" 
                   name="headers[${headerCount}].headerKey" 
                   placeholder="Header name (e.g., X-Custom-Header)" 
                   value="${headerName}" required>
        </div>
        <div class="col-md-6">
            <input type="text" class="form-control" 
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

// Auto-dismiss alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert:not(.alert-info)');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
    
    // Set default expiration date to 7 days from now
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

// Copy to clipboard function
function copyToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(function() {
            showToast('Copied to clipboard!', 'success');
        }).catch(function(err) {
            console.error('Failed to copy text: ', err);
            fallbackCopyToClipboard(text);
        });
    } else {
        fallbackCopyToClipboard(text);
    }
}

// Fallback copy method for older browsers
function fallbackCopyToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    document.body.appendChild(textArea);
    textArea.select();
    
    try {
        document.execCommand('copy');
        showToast('Copied to clipboard!', 'success');
    } catch (err) {
        console.error('Fallback: Failed to copy', err);
        showToast('Failed to copy to clipboard', 'danger');
    }
    
    document.body.removeChild(textArea);
}

// Show toast notification
function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container') || createToastContainer();
    
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" 
                    data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;
    
    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    // Remove toast after it's hidden
    toast.addEventListener('hidden.bs.toast', function() {
        toast.remove();
    });
}

// Create toast container if it doesn't exist
function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
}

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
