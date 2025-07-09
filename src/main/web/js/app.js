// CertHub Application
class CertHubApp {
    constructor() {
        this.currentEditingId = null;
        this.init();
    }

    init() {
        this.checkAuthStatus();
        this.bindEvents();
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

        // Check for shareable link
        const urlParams = new URLSearchParams(window.location.search);
        const shareableId = window.location.pathname.split('/view/')[1];
        if (shareableId) {
            this.handleShareableView(shareableId);
        }
    }

    // Handle login
    async handleLogin() {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const recaptchaResponse = grecaptcha.getResponse();

        if (!recaptchaResponse) {
            this.showAlert('Please complete the reCAPTCHA', 'danger');
            return;
        }

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    password: password,
                    recaptchaResponse: recaptchaResponse
                }),
            });

            const data = await response.json();

            if (data.success) {
                this.showAlert('Login successful!', 'success');
                setTimeout(() => this.showDashboard(), 1000);
            } else {
                this.showAlert(data.message, 'danger');
                grecaptcha.reset();
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showAlert('Login failed. Please try again.', 'danger');
            grecaptcha.reset();
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
                    <div class="certificate-preview">
                        <i class="material-icons">${cert.fileType.includes('pdf') ? 'picture_as_pdf' : 'image'}</i>
                    </div>
                    <div class="card-body">
                        <h6 class="card-title text-truncate">${cert.title}</h6>
                        <p class="card-text text-muted small">${cert.description || 'No description'}</p>
                        <small class="text-muted">Uploaded: ${new Date(cert.uploadedAt).toLocaleDateString()}</small>
                    </div>
                    <div class="certificate-actions">
                        <button class="btn btn-sm btn-outline-primary" onclick="app.editCertificate(${cert.id})">
                            <i class="material-icons">edit</i>
                        </button>
                        <button class="btn btn-sm btn-outline-success" onclick="app.shareCertificate('${cert.shareableId}')">
                            <i class="material-icons">share</i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="app.deleteCertificate(${cert.id})">
                            <i class="material-icons">delete</i>
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    }

    // Handle certificate upload
    async handleUpload() {
        const title = document.getElementById('certTitle').value;
        const description = document.getElementById('certDescription').value;
        const file = document.getElementById('certFile').files[0];

        if (!file) {
            this.showAlert('Please select a file', 'danger');
            return;
        }

        if (file.size > 15 * 1024 * 1024) {
            this.showAlert('File size exceeds 15MB limit', 'danger');
            return;
        }

        const formData = new FormData();
        formData.append('title', title);
        formData.append('description', description);
        formData.append('file', file);

        try {
            const response = await fetch('/api/certificates/upload', {
                method: 'POST',
                body: formData,
            });

            if (response.ok) {
                this.showAlert('Certificate uploaded successfully!', 'success');
                document.getElementById('uploadForm').reset();
                bootstrap.Modal.getInstance(document.getElementById('uploadModal')).hide();
                this.loadCertificates();
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Upload failed', 'danger');
            }
        } catch (error) {
            console.error('Upload error:', error);
            this.showAlert('Upload failed. Please try again.', 'danger');
        }
    }

    // Edit certificate
    async editCertificate(id) {
        try {
            const response = await fetch(`/api/certificates/${id}`);

            if (response.ok) {
                const certificate = await response.json();
                document.getElementById('editTitle').value = certificate.title;
                document.getElementById('editDescription').value = certificate.description || '';
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
        const description = document.getElementById('editDescription').value;

        try {
            const response = await fetch(`/api/certificates/${this.currentEditingId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    title: title,
                    description: description,
                }),
            });

            if (response.ok) {
                this.showAlert('Certificate updated successfully!', 'success');
                bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
                this.loadCertificates();
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Update failed', 'danger');
            }
        } catch (error) {
            console.error('Update error:', error);
            this.showAlert('Update failed. Please try again.', 'danger');
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
        navigator.clipboard.writeText(shareUrl).then(() => {
            this.showAlert('Share link copied to clipboard!', 'success');
        }).catch(() => {
            prompt('Copy this link to share:', shareUrl);
        });
    }

    // Handle shareable view
    async handleShareableView(shareableId) {
        this.showViewPage();
        
        // Store shareable ID for later use
        this.currentShareableId = shareableId;
        
        // Initially hide certificate details
        document.getElementById('certificateDetails').style.display = 'none';
    }

    // Handle view certificate with reCAPTCHA
    async handleViewCertificate() {
        const recaptchaResponse = grecaptcha.getResponse();
        
        if (!recaptchaResponse) {
            this.showAlert('Please complete the reCAPTCHA', 'danger');
            return;
        }

        try {
            const response = await fetch(`/api/public/certificate/${this.currentShareableId}?recaptcha=${recaptchaResponse}`);
            
            if (response.ok) {
                const certificate = await response.json();
                this.renderCertificateView(certificate);
            } else {
                const error = await response.json();
                this.showAlert(error.message || 'Failed to load certificate', 'danger');
                grecaptcha.reset();
            }
        } catch (error) {
            console.error('Error loading certificate:', error);
            this.showAlert('Failed to load certificate', 'danger');
            grecaptcha.reset();
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
                ${certificate.description && certificate.description.trim() ? `<p class="text-muted mb-4">${certificate.description}</p>` : ''}
                
                <div class="certificate-preview-large">
                    ${this.generateFullPreviewContent(certificate)}
                </div>
                
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

    // Download certificate
    downloadCertificate(shareableId) {
        // Create download URL without reCAPTCHA
        const downloadUrl = `/api/public/certificate/${shareableId}/download`;
        
        // Open download in new tab/window
        window.open(downloadUrl, '_blank');
    }

    // Show alert
    showAlert(message, type) {
        const alertContainer = document.querySelector('.container');
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        alertContainer.insertBefore(alertDiv, alertContainer.firstChild);
        
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new CertHubApp();
});