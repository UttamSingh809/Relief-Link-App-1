document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    
    try {
        const response = await fetch('/login', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        
        const messageDiv = document.getElementById('message');
        if (result.success) {
            messageDiv.className = 'message success';
            let roleMessage = `Logged in as ${result.role.toLowerCase()}`;
            messageDiv.textContent = `Login successful! ${roleMessage}`;
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1500);
        } else {
            messageDiv.className = 'message error';
            messageDiv.textContent = result.message || 'Login failed';
        }
    } catch (error) {
        console.error('Error:', error);
    }
});
