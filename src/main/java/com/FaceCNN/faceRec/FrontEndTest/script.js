function registerUser() {
    const registrationForm = document.getElementById('registrationForm');
    const resultMessage = document.getElementById('resultMessage');

    const userData = {
        name: registrationForm.name.value,
        email: registrationForm.email.value,
        password: registrationForm.password.value
    };

    fetch('http://localhost:9090/user', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
    })
    .then(response => {
        if (response.ok) {
            resultMessage.textContent = 'User registered successfully!';
        } else {
            resultMessage.textContent = 'Email already registered';
        }
    })
    .catch(error => console.error('Error:', error));
}

function userLogin() {
    const loginForm = document.getElementById('loginForm');
    const resultMessage = document.getElementById('resultMessage');

    const loginData = {
        email: loginForm.loginEmail.value,
        password: loginForm.loginPassword.value
    };

    fetch('http://localhost:9090/user/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginData),
    })
    .then(response => {
        if (response.ok) {
            resultMessage.textContent = 'Login successful!';
        } else {
            resultMessage.textContent = 'Incorrect email or password';
        }
    })
    .catch(error => console.error('Error:', error));
}
