async function loadDashboard() {
    try {
        // Get user info from API
        const userRes = await fetch('/api/user');
        const user = await userRes.json();
        const userRole = user.role;

        // Update welcome banner
        document.getElementById('userName').textContent = user.fullName || user.username;
        const roleEmoji = document.getElementById('roleEmoji');
        const roleMessage = document.getElementById('roleMessage');

        // Show/hide role-specific panels and navbar links
        const donorPanel1 = document.getElementById('donorPanel1');
        const donorPanel2 = document.getElementById('donorPanel2');
        const requesterPanel1 = document.getElementById('requesterPanel1');
        const requesterPanel2 = document.getElementById('requesterPanel2');
        const adminPanel = document.getElementById('adminPanel');
        const donorCTA = document.getElementById('donorCTA');
        const requesterCTA = document.getElementById('requesterCTA');

        // Hide all role-specific navbar links initially
        const matchesLink = document.getElementById('matchesLink');
        const guidelinesLink = document.getElementById('guidelinesLink');
        const emergencyLink = document.getElementById('emergencyLink');
        const donateLink = document.getElementById('donateLink');
        const requestLink = document.getElementById('requestLink');
        const adminLink = document.getElementById('adminLink');

        matchesLink.style.display = 'none';
        guidelinesLink.style.display = 'none';
        emergencyLink.style.display = 'none';
        donateLink.style.display = 'none';
        requestLink.style.display = 'none';
        adminLink.style.display = 'none';

        if (userRole === 'DONOR') {
            roleEmoji.textContent = '❤️';
            roleMessage.textContent = 'You\'re a Donor';
            donorPanel1.style.display = 'block';
            donorPanel2.style.display = 'block';
            donorCTA.style.display = 'block';
            // Show donor-specific navbar links
            donateLink.style.display = 'inline';
            matchesLink.style.display = 'inline';
            guidelinesLink.style.display = 'inline';
            emergencyLink.style.display = 'inline';
        } else if (userRole === 'REQUESTER') {
            roleEmoji.textContent = '🤝';
            roleMessage.textContent = 'You\'re a Requester';
            requesterPanel1.style.display = 'block';
            requesterPanel2.style.display = 'block';
            requesterCTA.style.display = 'block';
            // Show requester-specific navbar links
            requestLink.style.display = 'inline';
            matchesLink.style.display = 'inline';
            guidelinesLink.style.display = 'inline';
            emergencyLink.style.display = 'inline';
        } else if (userRole === 'ADMIN') {
            roleEmoji.textContent = '⚙️';
            roleMessage.textContent = 'You\'re an Administrator';
            adminPanel.style.display = 'block';
            // Show admin-specific navbar links (minimal set)
            adminLink.style.display = 'inline';
            matchesLink.style.display = 'inline';
            guidelinesLink.style.display = 'inline';
            emergencyLink.style.display = 'inline';
        }

        const [donationsRes, requestsRes, matchesRes, usersRes] = await Promise.all([
            fetch('/api/donations'),
            fetch('/api/requests'),
            fetch('/api/matches'),
            userRole === 'ADMIN' ? fetch('/api/admin/users') : Promise.resolve(null)
        ]);

        const donations = await donationsRes.json();
        const requests = await requestsRes.json();
        const matches = await matchesRes.json();

        // Role-specific content loading
        if (userRole === 'DONOR') {
            loadDonorDashboard(user, donations, requests, matches);
        } else if (userRole === 'REQUESTER') {
            loadRequesterDashboard(user, donations, requests, matches);
        } else if (userRole === 'ADMIN') {
            const users = await usersRes.json();
            loadAdminDashboard(users, donations, requests, matches);
        }

        // Load activity feed for all roles
        loadActivityFeed(matches);

    } catch (error) {
        console.error('Error:', error);
    }
}

async function loadDonorDashboard(user, donations, requests, matches) {
    // User's donations
    const userDonations = donations.filter(d => d.donorId === user.id);
    const donationsList = document.getElementById('donationsList');
    donationsList.innerHTML = userDonations.slice(0, 5).map(d => `
        <div class="item-card">
            <div class="item-header">
                <span class="item-title">${d.itemName}</span>
                <span class="item-badge ${d.matched ? 'badge-matched' : 'badge-pending'}">${d.matched ? 'Matched' : 'Pending'}</span>
            </div>
            <div class="item-details">
                ${d.category} - Quantity: ${d.quantity} - ${d.location}
            </div>
            <div class="item-status">
                Status: ${d.matched ? 'Successfully matched and delivered' : 'Waiting for matching'}
            </div>
        </div>
    `).join('');

    // Nearby requests (same location, high urgency)
    const nearbyRequests = requests.filter(r =>
        r.location === user.location &&
        (r.urgency === 'CRITICAL' || r.urgency === 'HIGH') &&
        !r.matched
    );
    const nearbyRequestsList = document.getElementById('nearbyRequests');
    nearbyRequestsList.innerHTML = nearbyRequests.slice(0, 5).map(r => `
        <div class="item-card">
            <div class="item-header">
                <span class="item-title">${r.itemName}</span>
                <span class="item-badge badge-${r.urgency.toLowerCase()}">${r.urgency}</span>
            </div>
            <div class="item-details">
                ${r.category} - Quantity: ${r.quantity} - ${r.location}
            </div>
        </div>
    `).join('');
}

async function loadRequesterDashboard(user, donations, requests, matches) {
    // User's requests
    const userRequests = requests.filter(r => r.requesterId === user.id);
    const requestsList = document.getElementById('requestsList');
    requestsList.innerHTML = userRequests.slice(0, 5).map(r => `
        <div class="item-card">
            <div class="item-header">
                <span class="item-title">${r.itemName}</span>
                <span class="item-badge ${r.matched ? 'badge-matched' : 'badge-' + r.urgency.toLowerCase()}">${r.matched ? 'Matched' : r.urgency}</span>
            </div>
            <div class="item-details">
                ${r.category} - Quantity: ${r.quantity} - ${r.location}
            </div>
            <div class="item-status">
                Status: ${r.matched ? 'Request fulfilled and delivered' : 'Waiting for donation match'}
            </div>
        </div>
    `).join('');

    // Nearby donations (same location)
    const nearbyDonations = donations.filter(d =>
        d.location === user.location && !d.matched
    );
    const nearbyDonationsList = document.getElementById('nearbyDonations');
    nearbyDonationsList.innerHTML = nearbyDonations.slice(0, 5).map(d => `
        <div class="item-card">
            <div class="item-header">
                <span class="item-title">${d.itemName}</span>
                <span class="item-badge badge-low">Available</span>
            </div>
            <div class="item-details">
                ${d.category} - Quantity: ${d.quantity} - ${d.location}
            </div>
        </div>
    `).join('');
}

async function loadAdminDashboard(users, donations, requests, matches) {
    document.getElementById('totalUsers').textContent = users.length;
    document.getElementById('pendingRequests').textContent = requests.filter(r => !r.matched).length;
    document.getElementById('totalMatches').textContent = matches.length;

    // Admin button handlers
    document.getElementById('findMatchesBtn').addEventListener('click', async () => {
        try {
            const response = await fetch('/api/matches/find', { method: 'POST' });
            if (response.ok) {
                const matches = await response.json();
                alert(`Matching completed! Found ${matches.length} new matches.`);
                loadDashboard(); // Refresh
            } else {
                alert('Error finding matches: ' + response.statusText);
            }
        } catch (error) {
            alert('Error finding matches: ' + error.message);
        }
    });

    document.getElementById('backupBtn').addEventListener('click', async () => {
        try {
            const response = await fetch('/api/admin/backup');
            const data = await response.json();
            console.log('Backup data:', data);
            alert('Data backed up successfully!');
        } catch (error) {
            alert('Error backing up data: ' + error.message);
        }
    });

    document.getElementById('resetBtn').addEventListener('click', async () => {
        if (confirm('Are you sure you want to reset the system? This will delete all data!')) {
            try {
                await fetch('/api/admin/reset', { method: 'POST' });
                alert('System reset successfully!');
                window.location.reload();
            } catch (error) {
                alert('Error resetting system: ' + error.message);
            }
        }
    });
}

async function loadActivityFeed(matches) {
    try {
        const response = await fetch('/api/activity');
        const activities = await response.json();

        const activityList = document.getElementById('activityList');

        activityList.innerHTML = activities.map(activity => {
            let icon = '📦';
            if (activity.type === 'donation') {
                icon = activity.status === 'matched' ? '✅' : '📦';
            } else if (activity.type === 'request') {
                icon = activity.status === 'matched' ? '✅' : '🆘';
            } else if (activity.type === 'match') {
                icon = '🤝';
            }

            const timestamp = new Date(activity.timestamp).toLocaleString();
            return `
                <div class="activity-item">
                    <span class="activity-icon">${icon}</span>
                    <span class="activity-message">${activity.message}</span>
                    <span class="activity-time">${timestamp}</span>
                </div>
            `;
        }).join('');
    } catch (error) {
        console.error('Error loading activity feed:', error);
        const activityList = document.getElementById('activityList');
        activityList.innerHTML = '<div class="activity-item">Unable to load recent activity</div>';
    }
}

loadDashboard();
