    function checkPasswordStrength(forceEnable) {
        const password = document.getElementById('password').value;
        const strengthBar = document.getElementById('passwordStrengthBar');
        const strengthText = document.getElementById('passwordStrengthText');
        const submitBtn = document.getElementById('submitBtn');

        let strength = 0;

        if (password.length >= 8) strength++;
        if (/[A-Z]/.test(password)) strength++;
        if (/[0-9]/.test(password)) strength++;
        if (/[^A-Za-z0-9]/.test(password)) strength++;
        if (password.length >= 12) strength++;

        let width = strength * 20;
        strengthBar.style.width = width + '%';

        const colors = ['#dc3545', '#fd7e14', '#ffc107', '#0d6efd', '#198754'];
        const texts = ['Very Weak', 'Weak', 'Moderate', 'Good', 'Strong'];

        strengthBar.className = 'progress-bar';
        if (strength > 0) {
            strengthBar.style.backgroundColor = colors[strength - 1];
            strengthText.textContent = texts[strength - 1];
        } else {
            strengthBar.style.width = '0%';
            strengthText.textContent = '';
        }
        submitBtn.disabled = !(forceEnable && password.length == 0 || strength >= 3);
    }