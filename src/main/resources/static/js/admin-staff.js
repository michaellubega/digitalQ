// Admin Staff Management

function createStaff() {
    const username = document.getElementById('staffUsername').value.trim();
    const password = document.getElementById('staffPassword').value;
    const counterId = document.getElementById('staffCounter').value;

    if (!username || !password) {
        alert('Please fill in username and password');
        return;
    }

    fetch('/admin/staff', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username,
            password,
            counterId: counterId || null
        })
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

function toggleStaff(id, activate) {
    const url = activate ? `/admin/staff/${id}/activate` : `/admin/staff/${id}/deactivate`;

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
