// Admin Counters Management

function createCounter() {
    const name = document.getElementById('counterName').value.trim();
    const serviceType = document.getElementById('serviceType').value.trim();

    if (!name || !serviceType) {
        alert('Please fill in all fields');
        return;
    }

    fetch('/admin/counters', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, serviceType })
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert(data.error);
        } else {
            location.reload();
        }
    })
    .catch(error => {
        alert('Network error. Please try again.');
    });
}

function toggleCounter(id, activate) {
    const url = activate ? `/admin/counters/${id}/activate` : `/admin/counters/${id}/deactivate`;

    fetch(url, {
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
            alert(data.error || 'Operation failed');
        }
    })
    .catch(error => {
        alert('Network error. Please try again.');
    });
}
