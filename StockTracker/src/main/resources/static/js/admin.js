async function loadUsers() {
    try {
        const response = await fetch('/api/admin/users');
        const users = await response.json();
        
        const usersList = document.getElementById('usersList');
        usersList.innerHTML = users.map(u => `
            <div class="item-card">
                <div class="item-header">
                    <span class="item-title">${u.fullName} (@${u.username})</span>
                    <span class="item-badge badge-${u.role.toLowerCase()}">${u.role}</span>
                </div>
                <div class="item-details">
                    ${u.email} - ${u.location}
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error:', error);
    }
}

document.getElementById('backupBtn').addEventListener('click', async () => {
    try {
        const response = await fetch('/api/admin/backup');
        const data = await response.json();
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'relieflink-backup.json';
        a.click();
    } catch (error) {
        console.error('Error:', error);
    }
});

document.getElementById('resetBtn').addEventListener('click', async () => {
    if (confirm('Are you sure you want to reset the entire system? This cannot be undone!')) {
        try {
            await fetch('/api/admin/reset', { method: 'POST' });
            alert('System reset successfully!');
            loadUsers();
        } catch (error) {
            console.error('Error:', error);
        }
    }
});

loadUsers();
