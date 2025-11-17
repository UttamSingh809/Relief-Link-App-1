async function loadAdminPage() {
    try {
        // Get user info to check role
        const userRes = await fetch('/api/user');
        const user = await userRes.json();
        const userRole = user.role;

        // Show appropriate navbar links based on role
        showNavbarLinks(userRole);

        // Load users
        await loadUsers();
    } catch (error) {
        console.error('Error loading admin page:', error);
    }
}

function showNavbarLinks(userRole) {
    // Hide all links first
    document.getElementById('donateLink').style.display = 'none';
    document.getElementById('requestLink').style.display = 'none';
    document.getElementById('matchesLink').style.display = 'none';
    document.getElementById('guidelinesLink').style.display = 'none';
    document.getElementById('emergencyLink').style.display = 'none';
    document.getElementById('adminLink').style.display = 'none';

    // Show appropriate links based on role
    if (userRole === 'ADMIN') {
        document.getElementById('matchesLink').style.display = 'inline';
        document.getElementById('guidelinesLink').style.display = 'inline';
        document.getElementById('emergencyLink').style.display = 'inline';
        document.getElementById('adminLink').style.display = 'inline';
    } else if (userRole === 'DONOR') {
        document.getElementById('donateLink').style.display = 'inline';
        document.getElementById('matchesLink').style.display = 'inline';
        document.getElementById('guidelinesLink').style.display = 'inline';
        document.getElementById('emergencyLink').style.display = 'inline';
    } else if (userRole === 'REQUESTER') {
        document.getElementById('requestLink').style.display = 'inline';
        document.getElementById('matchesLink').style.display = 'inline';
        document.getElementById('guidelinesLink').style.display = 'inline';
        document.getElementById('emergencyLink').style.display = 'inline';
    }
}

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

loadAdminPage();
