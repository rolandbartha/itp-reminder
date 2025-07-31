// Ping every 10 minutes to keep the session alive
setInterval(() => {
    fetch('/ping', { method: 'GET', credentials: 'same-origin' });
}, 10 * 60 * 1000);