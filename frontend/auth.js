"use strict";

const form = document.querySelector('#auth-form');
const message = document.querySelector('#message');
const submitButton = document.querySelector('#submit-button');
const passwordInput = document.querySelector('#password');
const showPasswordButton = document.querySelector('#show-password');
const nameInput = document.querySelector('#name');
const confirmInput = document.querySelector('#confirm');
const emailInput = document.querySelector('#email');

let mode = 'login';

// Allows user to change between login mode or sign up for the first time
function switchMode(nextMode) {
    mode = nextMode;
    const signup = mode === 'signup';

    document.querySelector('#name-wrap').classList.toggle('hidden', !signup);
    document.querySelector('#confirm-wrap').classList.toggle('hidden', !signup);
    document.querySelector('#login-prompt').classList.toggle('hidden', signup);
    document.querySelector('#signup-prompt').classList.toggle('hidden', !signup);

    nameInput.required = signup;
    confirmInput.required = signup;
    passwordInput.autocomplete = signup ? 'new-password' : 'current-password';

    document.querySelector('#form-title').textContent = signup ? 'Create an Account' : 'Log In';
    document.querySelector('#form-description').textContent = signup
        ? 'Study Buddy is a personal study assistant.'
        : 'Welcome back! Sign in to keep studying.';
    document.querySelector('#hero-tagline').textContent = signup
        ? 'Welcome to Study Buddy'
        : 'Your Personal Study Assistant';
    document.querySelector('#hero-description').textContent = signup
        ? 'Join Study Buddy to start studying smarter.'
        : 'Upload your course material and generate flashcards, quizzes, and get answers.';
    submitButton.textContent = signup ? 'Create Account' : 'Log In';
    document.title = signup ? 'Study Buddy | Sign Up' : 'Study Buddy | Log In';

    // Clears information the user previously type.
    passwordInput.value = '';
    confirmInput.value = '';
    passwordInput.type = 'password';
    showPasswordButton.textContent = 'Show';
    showPasswordButton.setAttribute('aria-pressed', 'false');
    showPasswordButton.setAttribute('aria-label', 'Show password');
    message.textContent = '';
    message.classList.remove('success');
}

document.querySelector('#login-tab').addEventListener('click', () => switchMode('login'));
document.querySelector('#signup-tab').addEventListener('click', () => switchMode('signup'));

// Allow for user to check what they type for their password
showPasswordButton.addEventListener('click', () => {
    const show = passwordInput.type === 'password';
    passwordInput.type = show ? 'text' : 'password';
    showPasswordButton.textContent = show ? 'Hide' : 'Show';
    showPasswordButton.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
    showPasswordButton.setAttribute('aria-pressed', String(show));
});

// User click the button then they will log in or create their account
form.addEventListener('submit', async (event) => {
    event.preventDefault();
    message.textContent = '';
    submitButton.disabled = true;

// Collects user information and is send to the backend
    const payload = {
        name: nameInput.value.trim(),
        email: emailInput.value.trim(),
        password: passwordInput.value,
        confirmPassword: confirmInput.value
    };

// Checks for password and confirm password are match
    if (mode === 'signup' && payload.password !== payload.confirmPassword) {
        message.textContent = 'Passwords do not match.';
        submitButton.disabled = false;
        return;
    }

// This how HTTP request to the backend
    try {
        const response = await fetch(`api/auth/${mode}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        });
        const body = await response.json();
        if (!response.ok) {
            throw new Error(body.error || 'Unable to authenticate. Please try again.');
        }
        window.location.assign('app');
// This handle the error if the server is not connecting
    } catch (error) {
        message.textContent = error.message || 'Unable to reach the server.';
    } finally {
        submitButton.disabled = false;
    }
});

// This redirects to the page
fetch('api/auth/me', {credentials: 'same-origin'})
    .then((response) => {
        if (response.ok) window.location.replace('app');
    })
    .catch(() => {
    });
