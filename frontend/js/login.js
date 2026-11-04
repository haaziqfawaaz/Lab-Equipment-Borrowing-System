
// LABTRACK API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// GET HTML ELEMENTS

const loginForm = document.getElementById("loginForm");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const togglePassword = document.getElementById("togglePassword");
const message = document.getElementById("message");


// SHOW / HIDE PASSWORD

if (togglePassword) {
    togglePassword.addEventListener("click", function () {

        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            togglePassword.textContent = "Hide";
        } else {
            passwordInput.type = "password";
            togglePassword.textContent = "Show";
        }

    });
}


// LOGIN FORM VALIDATION AND API INTEGRATION

loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    message.textContent = "";

    if (email === "" || password === "") {
        message.textContent = "Please fill all fields.";
        return;
    }

    if (!emailInput.checkValidity()) {
        message.textContent = "Enter a valid email address.";
        return;
    }

    const loginButton = loginForm.querySelector(
        'button[type="submit"]'
    );

    if (loginButton) {
        loginButton.disabled = true;
        loginButton.textContent = "Logging in...";
    }

    try {

        const response = await fetch(
            `${API_BASE_URL}/auth/login`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    password: password
                })
            }
        );

        const data = await response.json();

        if (!response.ok || data.status !== "success") {
            message.textContent =
                data.message || "Login failed. Please try again.";
            return;
        }

        // SAVE AUTHENTICATION DETAILS

        localStorage.setItem("labtrackToken", data.token);

        localStorage.setItem(
            "labtrackUser",
            JSON.stringify(data.user)
        );

        message.textContent = "Login successful!";

        // REDIRECT BASED ON BACKEND USER ROLE

        const role = data.user.role.toUpperCase();

        if (role === "ADMIN") {
            window.location.href = "admin-dashboard.html";

        } else if (role === "STUDENT") {
            window.location.href = "student-dashboard.html";

        } else {
            localStorage.removeItem("labtrackToken");
            localStorage.removeItem("labtrackUser");

            message.textContent = "Unrecognized user role.";
        }

    } catch (error) {

        console.error("Login error:", error);

        message.textContent =
            "Unable to connect to the server. Make sure the Java API is running.";

    } finally {

        if (loginButton) {
            loginButton.disabled = false;
            loginButton.textContent = "Login";
        }

    }

});


// FORGOT PASSWORD

const forgotPassword = document.getElementById("forgotPassword");

if (forgotPassword) {

    forgotPassword.addEventListener("click", function (event) {

        event.preventDefault();

        message.textContent =
            "Please contact your administrator for password assistance.";

    });

}
