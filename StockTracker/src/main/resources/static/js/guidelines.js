async function loadGuidelines() {
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

        // Load guidelines content
        const contentRes = await fetch('/api/guidelines');
        const contentData = await contentRes.json();
        document.getElementById('guidelinesContent').innerHTML = contentData.content;

    } catch (error) {
        console.error('Error loading guidelines:', error);
        document.getElementById('guidelinesContent').innerHTML = '<p>Error loading guidelines content.</p>';
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
    const contentDiv = document.getElementById('guidelinesContent');
    contentDiv.contentEditable = 'true';
    contentDiv.style.border = '2px solid #007bff';
    contentDiv.style.padding = '10px';
    contentDiv.style.minHeight = '200px';
    contentDiv.focus();

    // Show save/cancel buttons, hide edit button
    document.getElementById('editGuidelinesBtn').style.display = 'none';
    document.getElementById('saveGuidelinesBtn').style.display = 'inline';
    document.getElementById('cancelGuidelinesBtn').style.display = 'inline';
}

async function saveGuidelines() {
    const contentDiv = document.getElementById('guidelinesContent');
    const newContent = contentDiv.innerHTML;

    try {
        const response = await fetch('/api/admin/guidelines', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ content: newContent })
        });

        if (response.ok) {
            alert('Guidelines updated successfully!');
            disableEditing();
        } else {
            alert('Error updating guidelines: ' + response.statusText);
        }
    } catch (error) {
        console.error('Error saving guidelines:', error);
        alert('Error saving guidelines: ' + error.message);
    }
}

function cancelEditing() {
    // Reload the original content
    loadGuidelines();
    disableEditing();
}

function disableEditing() {
    const contentDiv = document.getElementById('guidelinesContent');
    contentDiv.contentEditable = 'false';
    contentDiv.style.border = 'none';
    contentDiv.style.padding = '0';
    contentDiv.style.minHeight = 'auto';

    // Show edit button, hide save/cancel buttons
    document.getElementById('editGuidelinesBtn').style.display = 'inline';
    document.getElementById('saveGuidelinesBtn').style.display = 'none';
    document.getElementById('cancelGuidelinesBtn').style.display = 'none';
}

// Event listeners
document.getElementById('editGuidelinesBtn').addEventListener('click', enableEditing);
document.getElementById('saveGuidelinesBtn').addEventListener('click', saveGuidelines);
document.getElementById('cancelGuidelinesBtn').addEventListener('click', cancelEditing);

// Load content on page load
loadGuidelines();
