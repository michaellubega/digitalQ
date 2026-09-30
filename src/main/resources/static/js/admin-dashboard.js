// Admin Dashboard - Auto Refresh Stats

function refreshStats() {
    fetch('/admin/api/stats')
        .then(response => response.json())
        .then(data => {
            // Update stat cards
            const statCards = document.querySelectorAll('.card .card-body h3');
            if (statCards.length >= 4) {
                statCards[0].textContent = data.totalWaiting;
                statCards[1].textContent = data.totalServedToday;
                statCards[2].textContent = data.activeCounters;
                statCards[3].textContent = data.averageWaitTimeMinutes.toFixed(1);
            }

            // Update counter table
            const tbody = document.querySelector('tbody');
            if (tbody) {
                tbody.innerHTML = data.counterStats.map(cs => `
                    <tr>
                        <td>${cs.counterName}</td>
                        <td>${cs.serviceType}</td>
                        <td>
                            <span class="badge ${cs.active ? 'bg-success' : 'bg-secondary'}">
                                ${cs.active ? 'Active' : 'Inactive'}
                            </span>
                            ${cs.paused ? '<span class="badge bg-warning ms-1">Paused</span>' : ''}
                        </td>
                        <td>${cs.currentToken || '-'}</td>
                        <td>${cs.waitingCount}</td>
                        <td>${cs.servedToday}</td>
                    </tr>
                `).join('');
            }
        })
        .catch(error => console.error('Error fetching stats:', error));
}

// Refresh every 5 seconds
setInterval(refreshStats, 5000);
