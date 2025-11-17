document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    
    // Convert FormData to JSON
    const data = {
        username: formData.get('username'),
        password: formData.get('password'),
        email: formData.get('email'),
        fullName: formData.get('fullName'),
        location: formData.get('location'),
        role: formData.get('role')
    };
    
    try {
        const response = await fetch('/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        const result = await response.json();
        
        const messageDiv = document.getElementById('message');
        if (result.success) {
            messageDiv.className = 'message success';
            messageDiv.textContent = 'Registration successful! Redirecting to login...';
            setTimeout(() => {
                window.location.href = '/login';
            }, 1500);
        } else {
            messageDiv.className = 'message error';
            messageDiv.textContent = result.message || 'Registration failed';
        }
    } catch (error) {
        console.error('Error:', error);
    }
});
