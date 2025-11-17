async function loadMatches() {
    try {
        // Get user role to show/hide admin controls
        const userRes = await fetch('/api/user');
        const user = await userRes.json();
        const userRole = user.role;

        // Show appropriate navbar links based on role
        showNavbarLinks(userRole);

        // Show/hide admin controls based on role
        const adminControls = document.getElementById('adminControls');
        const nonAdminMessage = document.getElementById('nonAdminMessage');

        if (userRole === 'ADMIN') {
            adminControls.style.display = 'block';
            nonAdminMessage.style.display = 'none';
        } else {
            adminControls.style.display = 'none';
            nonAdminMessage.style.display = 'block';
        }

        // Load activity feed
        await loadActivityFeed();

        const response = await fetch('/api/matches');
        const matches = await response.json();

        const matchesList = document.getElementById('matchesList');
        matchesList.innerHTML = matches.map(m => `
            <div class="item-card">
                <div class="item-header">
                    <span class="item-title">${m.itemName}</span>
                    <span class="item-badge badge-matched">Matched</span>
                </div>
                <div class="item-details">
                    Donor: ${m.donorName} → Requester: ${m.requesterName}<br>
                    Item: ${m.itemName} | Quantity: ${m.quantity} | Location: ${m.location}<br>
                    <small>Matched: ${new Date(m.matchedAt).toLocaleString()}</small>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error:', error);
    }
}

async function loadActivityFeed() {
    try {
        const response = await fetch('/api/activity');
        const activities = await response.json();

        const activityFeed = document.getElementById('activityFeed');
        if (Array.isArray(activities)) {
            activityFeed.innerHTML = activities.slice(0, 20).map(activity => `
                <div class="activity-item">
                    <div class="activity-time">${new Date(activity.timestamp).toLocaleString()}</div>
                    <div class="activity-message">${activity.message}</div>
                    <div class="activity-status status-${activity.status}">${activity.status}</div>
                </div>
            `).join('');
        } else {
            activityFeed.innerHTML = '<p>No activity data available.</p>';
        }
    } catch (error) {
        console.error('Error loading activity feed:', error);
        document.getElementById('activityFeed').innerHTML = '<p>Error loading activity feed.</p>';
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

document.getElementById('findMatchesBtn').addEventListener('click', async () => {
    try {
        const response = await fetch('/api/matches/find', { method: 'POST' });
        if (response.ok) {
            const matches = await response.json();
            alert(`Matching completed! Found ${matches.length} new matches.`);
            loadMatches();
        } else {
            alert('Error finding matches: ' + response.statusText);
        }
    } catch (error) {
        alert('Error finding matches: ' + error.message);
    }
});

loadMatches();
