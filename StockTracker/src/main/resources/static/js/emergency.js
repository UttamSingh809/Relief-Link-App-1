async function loadEmergencyContacts() {
    try {
        // Get user info to check role
        const userRes = await fetch('/api/user');
        const user = await userRes.json();
        const userRole = user.role;

        // Show appropriate navbar links based on role
        showNavbarLinks(userRole);

        // Show admin controls if user is admin
        if (userRole === 'ADMIN') {
            document.getElementById('adminControls').style.display = 'block';
        }

        // Load emergency content
        const contentRes = await fetch('/api/emergency');
        const contentData = await contentRes.json();
        document.getElementById('emergencyContent').innerHTML = contentData.content;

    } catch (error) {
        console.error('Error loading emergency contacts:', error);
        document.getElementById('emergencyContent').innerHTML = '<p>Error loading emergency contacts content.</p>';
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

function enableEditing() {
    const contentDiv = document.getElementById('emergencyContent');
    contentDiv.contentEditable = 'true';
    contentDiv.style.border = '2px solid #007bff';
    contentDiv.style.padding = '10px';
    contentDiv.style.minHeight = '200px';
    contentDiv.focus();

    // Show save/cancel buttons, hide edit button
    document.getElementById('editEmergencyBtn').style.display = 'none';
    document.getElementById('saveEmergencyBtn').style.display = 'inline';
    document.getElementById('cancelEmergencyBtn').style.display = 'inline';
}

async function saveEmergencyContacts() {
    const contentDiv = document.getElementById('emergencyContent');
    const newContent = contentDiv.innerHTML;

    try {
        const response = await fetch('/api/admin/emergency', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ content: newContent })
        });

        if (response.ok) {
            alert('Emergency contacts updated successfully!');
            disableEditing();
        } else {
            alert('Error updating emergency contacts: ' + response.statusText);
        }
    } catch (error) {
        console.error('Error saving emergency contacts:', error);
        alert('Error saving emergency contacts: ' + error.message);
    }
}

function cancelEditing() {
    // Reload the original content
    loadEmergencyContacts();
    disableEditing();
}

function disableEditing() {
    const contentDiv = document.getElementById('emergencyContent');
    contentDiv.contentEditable = 'false';
    contentDiv.style.border = 'none';
    contentDiv.style.padding = '0';
    contentDiv.style.minHeight = 'auto';

    // Show edit button, hide save/cancel buttons
    document.getElementById('editEmergencyBtn').style.display = 'inline';
    document.getElementById('saveEmergencyBtn').style.display = 'none';
    document.getElementById('cancelEmergencyBtn').style.display = 'none';
}

// Event listeners
document.getElementById('editEmergencyBtn').addEventListener('click', enableEditing);
document.getElementById('saveEmergencyBtn').addEventListener('click', saveEmergencyContacts);
document.getElementById('cancelEmergencyBtn').addEventListener('click', cancelEditing);

// Load content on page load
loadEmergencyContacts();
