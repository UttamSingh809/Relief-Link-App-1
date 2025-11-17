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
            e.target.reset();
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1500);
        }
    } catch (error) {
        console.error('Error:', error);
    }
});
