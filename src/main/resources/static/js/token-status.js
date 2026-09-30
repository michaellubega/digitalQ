// Token Status Page - Real-time Updates
let refreshInterval;

function updateTokenStatus() {
    if (!tokenNumber) return;

    fetch('/api/tokens/' + tokenNumber)
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                console.error('Error fetching token status:', data.error);
                return;
            }

            // Update position
            const positionElement = document.querySelector('.alert p strong');
            if (positionElement && data.status === 'WAITING') {
                positionElement.textContent = data.position;
            }

            // Update status alert
            const alertDiv = document.querySelector('.alert');
            if (alertDiv) {
                if (data.status === 'CALLED') {
                    alertDiv.className = 'alert alert-success';
                    alertDiv.innerHTML = '<h5 class="alert-heading">Your Turn!</h5><p class="mb-0">Please proceed to the counter</p>';
                } else if (data.status === 'SERVED') {
                    alertDiv.className = 'alert alert-secondary';
                    alertDiv.innerHTML = '<h5 class="alert-heading">Served</h5><p class="mb-0">Thank you for using digitalQ</p>';
                    clearInterval(refreshInterval);
                } else if (data.status === 'EXPIRED') {
                    alertDiv.className = 'alert alert-danger';
                    alertDiv.innerHTML = '<h5 class="alert-heading">Expired</h5><p class="mb-0">Your token has expired. Please get a new one.</p>';
                    clearInterval(refreshInterval);
                }
            }
        })
        .catch(error => console.error('Error:', error));
}

// Poll every 3 seconds
refreshInterval = setInterval(updateTokenStatus, 3000);
