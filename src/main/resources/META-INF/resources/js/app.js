// CertHub Application

class CertHubApp {
    constructor() {
        this.currentEditingId = null;
        this.init();
    }

    // Go to home page
    goHome() {
        const shareableId = window.location.pathname.split('/view/')[1];
        if (shareableId) {
            // On view page, reload the view page
            window.location.reload();
        } else {
            // On dashboard, go to dashboard
            this.checkAuthStatus();
        }
    }

    init() {
        this.bindEvents();
        
        // Check if this is a shareable link first
        const shareableId = window.location.pathname.split('/view/')[1];
        if (shareableId) {
            this.handleShareableView(shareableId);
        } else {
            this.checkAuthStatus();
        }
    }

    // Check authentication status
    async checkAuthStatus() {
        try {
            const response = await fetch('/api/auth/status');
            const data = await response.json();
            
            if (data.authenticated) {
                this.showDashboard();
            } else {
                this.showLoginPage();
            }
        } catch (error) {
            console.error('Error checking auth status:', error);
            this.showLoginPage();
        }
    }

    // Show different pages

    showLoginPage() {
        this.hideAllPages();
        document.getElementById('loginPage').style.display = 'block';
    }

    showDashboard() {
        this.hideAllPages();
        document.getElementById('dashboardPage').style.display = 'block';
        document.getElementById('logoutBtn').style.display = 'block';
        this.loadCertificates();
    }

    showViewPage() {
        this.hideAllPages();
        document.getElementById('viewPage').style.display = 'block';
        document.getElementById('logoutBtn').style.display = 'none';
    }

    hideAllPages() {
        document.getElementById('loginPage').style.display = 'none';
        document.getElementById('dashboardPage').style.display = 'none';
        document.getElementById('viewPage').style.display = 'none';
    }

    // Bind event listeners
    bindEvents() {
        // Login form
        document.getElementById('loginForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });

        // Upload form
        document.getElementById('uploadForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleUpload();
        });

        // Edit form
        document.getElementById('editForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleEdit();
        });

        // Logout button
        document.getElementById('logoutBtn').addEventListener('click', () => {
            this.handleLogout();
        });

        // View certificate button
        document.getElementById('viewCertificateBtn').addEventListener('click', () => {
            this.handleViewCertificate();
        });

        // Shareable link check is now handled in init()
    }

    // Handle login
    async handleLogin() {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    password: password,
                    recaptchaResponse: 'disabled' // Placeholder since reCAPTCHA is disabled
                }),
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    // No success message, just redirect
                    this.showDashboard();
                } else {
                    this.showFormMessage('loginMessage', data.message || 'Login failed', 'error');
                }
            } else {
                const errorData = await response.json();
                this.showFormMessage('loginMessage', errorData.message || 'Login failed', 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showFormMessage('loginMessage', 'Login failed. Please try again.', 'error');
        }
    }

    // Handle logout
    async handleLogout() {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
            });
        } catch (error) {
            console.error('Logout error:', error);
        }
        this.showLoginPage();
    }

    // Load certificates
    async loadCertificates() {
        try {
            const response = await fetch('/api/certificates');

            if (response.ok) {
                const certificates = await response.json();
                this.renderCertificates(certificates);
            } else {
                this.showAlert('Failed to load certificates', 'danger');
            }
        } catch (error) {
            console.error('Error loading certificates:', error);
            this.showAlert('Failed to load certificates', 'danger');
        }
    }

    // Render certificates
    renderCertificates(certificates) {
        const container = document.getElementById('certificatesList');
        
        if (certificates.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="empty-state">
                        <i class="material-icons">description</i>
                        <h5>No certificates yet</h5>
                        <p>Upload your first certificate to get started.</p>
                    </div>
                </div>
            `;
            return;
        }

        container.innerHTML = certificates.map(cert => `
            <div class="col-md-4 mb-4">
                <div class="card certificate-card">
                    <div class="certificate-preview" onclick="app.previewCertificate(${cert.id})">
                        ${this.generatePreviewContent(cert)}
                    </div>
                    <div class="card-body">
                        <h6 class="card-title text-truncate">${cert.title}</h6>
                        ${cert.credentialLink && cert.credentialLink.trim() ? 
                            `<p class="card-text small">
                                <a href="${cert.credentialLink}" target="_blank" class="text-decoration-none">
                                    <i class="material-icons" style="font-size: 14px; vertical-align: text-top;">link</i>
                                    View Credential
                                </a>
                            </p>` : 
                            `<p class="card-text text-muted small">No credential link</p>`
                        }
                        <small class="text-muted">Uploaded: ${new Date(cert.uploadedAt).toLocaleDateString()}</small>
                    </div>
                    <div class="certificate-actions">
                        <button class="btn btn-sm btn-outline-primary" onclick="app.editCertificate(${cert.id})">
                            <i class="material-icons">edit</i>
                        </button>
                        <button class="btn btn-sm btn-outline-success" onclick="app.shareCertificate('${cert.shareableId}')">
                            <i class="material-icons">share</i>
                        </button>
                        <button class="btn btn-sm btn-outline-info" onclick="app.downloadCertificateFromDashboard(${cert.id})">
                            <i class="material-icons">download</i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="app.deleteCertificate(${cert.id})">
                            <i class="material-icons">delete</i>
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    }

    // Generate preview content for certificate tiles
    generatePreviewContent(cert) {
        if (cert.fileType.includes('pdf')) {
            return `
                <div class="pdf-preview">
                    <iframe src="/api/certificates/${cert.id}/preview" 
                            style="width: 100%; height: 180px; border: none; pointer-events: none;">
                    </iframe>
                    <div class="preview-overlay">
                        <i class="material-icons">picture_as_pdf</i>
                        <span>PDF</span>
                    </div>
                </div>
            `;
        } else if (cert.fileType.includes('image')) {
            return `
                <div class="image-preview">
                    <img src="/api/certificates/${cert.id}/preview" 
                         style="width: 100%; height: 180px; object-fit: cover;"
                         alt="${cert.title}">
                    <div class="preview-overlay">
                        <i class="material-icons">image</i>
                        <span>IMAGE</span>
                    </div>
                </div>
            `;
        } else {
            return `
                <div class="generic-preview">
                    <i class="material-icons">description</i>
                    <span>FILE</span>
                </div>
            `;
        }
    }

    // Preview certificate in modal
    async previewCertificate(id) {
        try {
            const response = await fetch(`/api/certificates/${id}`);
            if (response.ok) {
                const certificate = await response.json();
                this.showCertificatePreviewModal(certificate);
            } else {
                this.showAlert('Failed to load certificate details', 'danger');
            }
        } catch (error) {
            console.error('Error loading certificate:', error);
            this.showAlert('Failed to load certificate details', 'danger');
        }
    }

    // Download certificate from dashboard
    async downloadCertificateFromDashboard(id) {
        try {
            const response = await fetch(`/api/certificates/${id}`);
            if (response.ok) {
                const certificate = await response.json();
                // Create download URL for authenticated users
                const downloadUrl = `/api/certificates/${id}/download`;
                window.open(downloadUrl, '_blank');
            } else {
                this.showAlert('Failed to download certificate', 'danger');
            }
        } catch (error) {
            console.error('Error downloading certificate:', error);
            this.showAlert('Failed to download certificate', 'danger');
        }
    }

    // Handle certificate upload
    async handleUpload() {
        const title = document.getElementById('certTitle').value;
        const credentialLink = document.getElementById('certCredentialLink').value;
        const file = document.getElementById('certFile').files[0];

        if (!file) {
            this.showAlert('Please select a file', 'danger');
            return;
        }

        if (file.size > 15 * 1024 * 1024) {
            this.showAlert('File size exceeds 15MB limit', 'danger');
            return;
        }

        // Convert file to base64
        const fileBase64 = await this.fileToBase64(file);

        try {
            const response = await fetch('/api/certificates/upload', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    title: title,
                    credentialLink: credentialLink,
                    fileName: file.name,
                    fileData: fileBase64
                }),
            });

            if (response.ok) {
                this.showFormMessage('uploadMessage', 'Certificate uploaded successfully!', 'success');
                document.getElementById('uploadForm').reset();
                setTimeout(() => {
                    bootstrap.Modal.getInstance(document.getElementById('uploadModal')).hide();
                    this.loadCertificates();
                }, 1500);
            } else {
                const error = await response.json();
                this.showFormMessage('uploadMessage', error.message || 'Upload failed', 'error');
            }
        } catch (error) {
            console.error('Upload error:', error);
            this.showFormMessage('uploadMessage', 'Upload failed. Please try again.', 'error');
        }
    }

    // Convert file to base64
    fileToBase64(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => {
                // Remove the data:image/jpeg;base64, or data:application/pdf;base64, prefix
                const base64 = reader.result.split(',')[1];
                resolve(base64);
            };
            reader.onerror = error => reject(error);
        });
    }

    // Edit certificate
    async editCertificate(id) {
        try {
            const response = await fetch(`/api/certificates/${id}`);

            if (response.ok) {
                const certificate = await response.json();
                document.getElementById('editTitle').value = certificate.title;
                document.getElementById('editCredentialLink').value = certificate.credentialLink || '';
                this.currentEditingId = id;
                new bootstrap.Modal(document.getElementById('editModal')).show();
            } else {
                this.showAlert('Failed to load certificate details', 'danger');
            }
        } catch (error) {
            console.error('Error loading certificate:', error);
            this.showAlert('Failed to load certificate details', 'danger');
        }
    }

    // Handle certificate edit
    async handleEdit() {
        const title = document.getElementById('editTitle').value;
        const credentialLink = document.getElementById('editCredentialLink').value;

        try {
            const response = await fetch(`/api/certificates/${this.currentEditingId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    title: title,
                    credentialLink: credentialLink,
                }),
            });

            if (response.ok) {
                this.showFormMessage('editMessage', 'Certificate updated successfully!', 'success');
                setTimeout(() => {
                    bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
                    this.loadCertificates();
                }, 1500);
            } else {
                const error = await response.json();
                this.showFormMessage('editMessage', error.message || 'Update failed', 'error');
            }
        } catch (error) {
            console.error('Update error:', error);
            this.showFormMessage('editMessage', 'Update failed. Please try again.', 'error');
        }
    }

    // Delete certificate
    async deleteCertificate(id) {
        if (!confirm('Are you sure you want to delete this certificate?')) {
            return;
        }

        try {
            const response = await fetch(`/api/certificates/${id}`, {
                method: 'DELETE',
            });

            if (response.ok) {
                this.showAlert('Certificate deleted successfully!', 'success');
                this.loadCertificates();
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Delete failed', 'danger');
            }
        } catch (error) {
            console.error('Delete error:', error);
            this.showAlert('Delete failed. Please try again.', 'danger');
        }
    }

    // Share certificate
    shareCertificate(shareableId) {
        const shareUrl = `${window.location.origin}/view/${shareableId}`;
        
        // Check if clipboard API is available (requires HTTPS)
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(shareUrl).then(() => {
                this.showShareMessage('Share link copied!', shareableId);
            }).catch(() => {
                this.fallbackCopyToClipboard(shareUrl, shareableId);
            });
        } else {
            this.fallbackCopyToClipboard(shareUrl, shareableId);
        }
    }

    // Fallback copy method for HTTP environments
    fallbackCopyToClipboard(text, shareableId) {
        // Create a temporary text area
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        
        try {
            const successful = document.execCommand('copy');
            if (successful) {
                this.showShareMessage('Share link copied!', shareableId);
            } else {
                // Last resort - show prompt
                prompt('Copy this link to share:', text);
            }
        } catch (err) {
            // Fallback to prompt
            prompt('Copy this link to share:', text);
        } finally {
            document.body.removeChild(textArea);
        }
    }

    // Handle shareable view
    async handleShareableView(shareableId) {
        this.showViewPage();
        
        // Store shareable ID for later use
        this.currentShareableId = shareableId;
        
        // Load certificate info to get the title
        try {
            const response = await fetch(`/api/public/certificate/${shareableId}`);
            if (response.ok) {
                const certificate = await response.json();
                // Set the certificate title immediately
                document.getElementById('viewTitle').textContent = certificate.title;
            }
        } catch (error) {
            console.error('Error loading certificate info:', error);
        }
        
        // Initially hide certificate details
        const certificateDetails = document.getElementById('certificateDetails');
        if (certificateDetails) {
            certificateDetails.style.display = 'none';
        }
    }

    // Handle view certificate
    async handleViewCertificate() {
        try {
            const response = await fetch(`/api/public/certificate/${this.currentShareableId}`);
            
            if (response.ok) {
                const certificate = await response.json();
                this.renderCertificateView(certificate);
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Failed to load certificate', 'danger');
            }
        } catch (error) {
            console.error('Error loading certificate:', error);
            this.showAlert('Failed to load certificate', 'danger');
        }
    }

    // Render certificate view
    renderCertificateView(certificate) {
        document.getElementById('viewTitle').textContent = certificate.title;
        
        // Hide the view button after successful loading
        const viewButton = document.getElementById('viewCertificateBtn');
        if (viewButton) {
            viewButton.style.display = 'none';
        }
        
        const detailsContainer = document.getElementById('certificateDetails');
        detailsContainer.innerHTML = `
            <div class="certificate-view">
                <div class="certificate-preview-large">
                    ${this.generateFullPreviewContent(certificate)}
                </div>
                
                ${certificate.credentialLink && certificate.credentialLink.trim() ? 
                    `<div class="credential-link-section">
                        <a href="${certificate.credentialLink}" target="_blank" class="btn btn-outline-primary">
                            <i class="material-icons me-1">link</i>
                            View Credential
                        </a>
                    </div>` : ''}
                
                <div class="mt-3">
                    <p><strong>File:</strong> ${certificate.fileName}</p>
                    <p><strong>Size:</strong> ${(certificate.fileSize / 1024 / 1024).toFixed(2)} MB</p>
                    <p><strong>Uploaded:</strong> ${new Date(certificate.uploadedAt).toLocaleDateString()}</p>
                </div>
                <div class="mt-4">
                    <button class="btn btn-primary" onclick="app.downloadCertificate('${certificate.shareableId}')">
                        <i class="material-icons me-1">download</i>
                        Download Certificate
                    </button>
                </div>
            </div>
        `;
        
        detailsContainer.style.display = 'block';
    }

    // Generate full preview content for certificate view
    generateFullPreviewContent(certificate) {
        if (certificate.fileType.includes('pdf')) {
            return `
                <div class="pdf-preview-large">
                    <iframe src="/api/public/certificate/${certificate.shareableId}/preview#toolbar=0&navpanes=0&scrollbar=0" 
                            style="width: 100%; height: 600px; border: 1px solid #ddd; border-radius: 4px; pointer-events: none;">
                    </iframe>
                </div>
            `;
        } else if (certificate.fileType.includes('image')) {
            return `
                <div class="image-preview-large text-center">
                    <img src="/api/public/certificate/${certificate.shareableId}/preview" 
                         style="max-width: 100%; height: auto; max-height: 600px; border: 1px solid #ddd; border-radius: 4px;"
                         alt="${certificate.title}">
                </div>
            `;
        } else {
            return `
                <div class="generic-preview-large text-center p-5">
                    <i class="material-icons" style="font-size: 64px; color: #ccc;">description</i>
                    <p class="text-muted mt-2">Preview not available for this file type</p>
                </div>
            `;
        }
    }

    // Show certificate preview modal
    showCertificatePreviewModal(certificate) {
        // Create modal if it doesn't exist
        let modal = document.getElementById('previewModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.className = 'modal fade';
            modal.id = 'previewModal';
            modal.innerHTML = `
                <div class="modal-dialog modal-xl">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="previewModalTitle">Certificate Preview</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body" id="previewModalBody">
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button type="button" class="btn btn-primary" id="previewDownloadBtn">
                                <i class="material-icons me-1">download</i>
                                Download
                            </button>
                        </div>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
        }

        // Update modal content
        document.getElementById('previewModalTitle').textContent = certificate.title;
        document.getElementById('previewModalBody').innerHTML = `
            <div class="certificate-preview-modal">
                ${certificate.credentialLink && certificate.credentialLink.trim() ? 
                    `<div class="mb-3">
                        <a href="${certificate.credentialLink}" target="_blank" class="btn btn-outline-primary">
                            <i class="material-icons me-1">link</i>
                            View Credential
                        </a>
                    </div>` : ''}
                ${this.generateFullPreviewContent(certificate)}
                <div class="mt-3">
                    <p><strong>File:</strong> ${certificate.fileName}</p>
                    <p><strong>Size:</strong> ${(certificate.fileSize / 1024 / 1024).toFixed(2)} MB</p>
                    <p><strong>Uploaded:</strong> ${new Date(certificate.uploadedAt).toLocaleDateString()}</p>
                </div>
            </div>
        `;

        // Update download button
        document.getElementById('previewDownloadBtn').onclick = () => {
            this.downloadCertificateFromDashboard(certificate.id);
        };

        // Show modal
        new bootstrap.Modal(modal).show();
    }

    // Download certificate
    downloadCertificate(shareableId) {
        // Create download URL without reCAPTCHA
        const downloadUrl = `/api/public/certificate/${shareableId}/download`;
        
        // Open download in new tab/window
        window.open(downloadUrl, '_blank');
    }

    // Show form message
    showFormMessage(elementId, message, type) {
        const messageElement = document.getElementById(elementId);
        messageElement.className = `form-message ${type}`;
        messageElement.innerHTML = `
            <i class="material-icons">${type === 'success' ? 'check_circle' : 'error'}</i>
            ${message}
        `;
        messageElement.style.display = 'flex';
        
        // Auto hide after 5 seconds for success messages
        if (type === 'success') {
            setTimeout(() => {
                messageElement.style.display = 'none';
            }, 5000);
        }
    }

    // Show share message on certificate tile
    showShareMessage(message, shareableId) {
        // Find the specific certificate card by shareableId
        const shareButton = document.querySelector(`button[onclick*="${shareableId}"]`);
        if (shareButton) {
            const card = shareButton.closest('.certificate-card');
            let shareMsg = card.querySelector('.share-message');
            
            if (!shareMsg) {
                shareMsg = document.createElement('div');
                shareMsg.className = 'share-message';
                card.style.position = 'relative';
                card.appendChild(shareMsg);
            }
            
            shareMsg.textContent = message;
            shareMsg.classList.add('show');
            
            setTimeout(() => {
                shareMsg.classList.remove('show');
            }, 2000);
        }
    }

    // Show alert (fallback for other messages)
    showAlert(message, type) {
        console.log(`${type.toUpperCase()}: ${message}`);
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Simple test - if we're on a view URL, show the view page immediately
    const shareableId = window.location.pathname.split('/view/')[1];
    if (shareableId) {
        const loginPage = document.getElementById('loginPage');
        const dashboardPage = document.getElementById('dashboardPage');
        const viewPage = document.getElementById('viewPage');
        
        if (viewPage) {
            // Hide all pages first
            if (loginPage) loginPage.style.display = 'none';
            if (dashboardPage) dashboardPage.style.display = 'none';
            
            // Show view page
            viewPage.style.display = 'block';
        }
    }
    
    try {
        window.app = new CertHubApp();
    } catch (error) {
        console.error('Error initializing CertHub app:', error);
    }
});