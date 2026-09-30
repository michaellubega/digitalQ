// Customer Token Form Handler
document.getElementById('tokenForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const customerName = document.getElementById('customerName').value.trim();
    const counterId = document.getElementById('counterId').value;
    const errorAlert = document.getElementById('errorAlert');

    if (!customerName || customerName.length < 2) {
        errorAlert.textContent = 'Please enter a valid name (at least 2 characters).';
        errorAlert.classList.remove('d-none');
        return;
    }

    if (!counterId) {
        errorAlert.textContent = 'Please select a service type.';
        errorAlert.classList.remove('d-none');
        return;
    }

    errorAlert.classList.add('d-none');

    try {
        const response = await fetch('/api/tokens', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                customerName: customerName,
                counterId: parseInt(counterId)
            })
        });

        const data = await response.json();

        if (response.ok) {
            // Redirect to token page
            window.location.href = '/token/' + data.tokenNumber;
        } else {
            errorAlert.textContent = data.error || 'Failed to create token. Please try again.';
            errorAlert.classList.remove('d-none');
        }
    } catch (error) {
        errorAlert.textContent = 'Network error. Please try again.';
        errorAlert.classList.remove('d-none');
    }
});
