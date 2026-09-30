// Staff Dashboard JavaScript

function verifyToken() {
    const tokenInput = document.getElementById('tokenInput');
    const tokenNumber = tokenInput.value.trim();
    const resultDiv = document.getElementById('verifyResult');

    if (!tokenNumber) {
        resultDiv.innerHTML = '<div class="alert alert-warning">Please enter a token number</div>';
        return;
    }

    fetch('/staff/verify', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ tokenNumber: tokenNumber })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            resultDiv.innerHTML = `
                <div class="alert alert-success">
                    <h6>Token Verified</h6>
                    <p class="mb-1"><strong>Token:</strong> ${data.tokenNumber}</p>
                    <p class="mb-1"><strong>Customer:</strong> ${data.customerName}</p>
                    <button class="btn btn-success btn-sm mt-2" onclick="serveToken(${data.tokenId})">
                        Mark as Served
                    </button>
                </div>
            `;
        } else {
            resultDiv.innerHTML = `<div class="alert alert-danger">${data.error}</div>`;
        }
    })
    .catch(error => {
        resultDiv.innerHTML = '<div class="alert alert-danger">Network error. Please try again.</div>';
    });
}

function serveToken(tokenId) {
    fetch('/staff/serve/' + tokenId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            document.getElementById('verifyResult').innerHTML = `
                <div class="alert alert-success">
                    <h6>Token Served</h6>
                    <p class="mb-0">Token <strong>${data.tokenNumber}</strong> has been served.</p>
                </div>
            `;
            // Refresh queue after 1 second
            setTimeout(() => location.reload(), 1000);
        } else {
            document.getElementById('verifyResult').innerHTML = `
                <div class="alert alert-danger">${data.error}</div>
            `;
        }
    })
    .catch(error => {
        document.getElementById('verifyResult').innerHTML = `
            <div class="alert alert-danger">Network error. Please try again.</div>
        `;
    });
}

function callNextToken() {
    if (!counterId) return;

    fetch('/staff/next/' + counterId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Show notification
            const resultDiv = document.getElementById('verifyResult');
            resultDiv.innerHTML = `
                <div class="alert alert-info">
                    <h6>Next Token Called</h6>
                    <p class="mb-0">Token <strong>${data.tokenNumber}</strong> - ${data.customerName}</p>
                </div>
            `;
            setTimeout(() => location.reload(), 1000);
        } else {
            alert(data.error || 'No waiting tokens');
        }
    })
    .catch(error => {
        alert('Network error. Please try again.');
    });
}

function pauseCounter() {
    if (!counterId) return;

    fetch('/staff/pause/' + counterId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            location.reload();
        } else {
            alert(data.error || 'Failed to pause counter');
        }
    })
    .catch(error => {
        alert('Network error. Please try again.');
    });
}

function resumeCounter() {
    if (!counterId) return;

    fetch('/staff/resume/' + counterId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            location.reload();
        } else {
            alert(data.error || 'Failed to resume counter');
        }
    })
    .catch(error => {
        alert('Network error. Please try again.');
    });
}

// Allow Enter key to verify
document.getElementById('tokenInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        verifyToken();
    }
});
