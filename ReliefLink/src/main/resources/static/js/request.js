async function loadRequestPage() {
    try {
        // Get user info to check role
        const userRes = await fetch('/api/user');
        const user = await userRes.json();
        const userRole = user.role;

        // Show appropriate navbar links based on role
        showNavbarLinks(userRole);
    } catch (error) {
        console.error('Error loading request page:', error);
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

document.getElementById('requestForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);

    const request = {
        category: formData.get('category'),
        itemName: formData.get('itemName'),
        quantity: parseInt(formData.get('quantity')),
        location: formData.get('location'),
        urgency: formData.get('urgency'),
        description: formData.get('description')
    };

    try {
        const response = await fetch('/api/requests', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });

        if (response.ok) {
            const messageDiv = document.getElementById('message');
            messageDiv.className = 'message success';
            messageDiv.textContent = 'Request posted successfully!';
            messageDiv.style.display = 'block';
            e.target.reset();
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1500);
        } else {
            const messageDiv = document.getElementById('message');
            messageDiv.className = 'message error';
            messageDiv.textContent = 'Error posting request. Please try again.';
            messageDiv.style.display = 'block';
        }
    } catch (error) {
        console.error('Error:', error);
        const messageDiv = document.getElementById('message');
        messageDiv.className = 'message error';
        messageDiv.textContent = 'Error posting request. Please try again.';
        messageDiv.style.display = 'block';
    }
});

// Load page on startup
loadRequestPage();
