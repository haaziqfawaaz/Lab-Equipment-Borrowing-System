
// LABTRACK API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// GET FORM ELEMENTS

const registerForm = document.getElementById("registerForm");

const fullName = document.getElementById("fullName");

const email = document.getElementById("email");

const contact = document.getElementById("contact");

const password = document.getElementById("password");

const confirmPassword =
    document.getElementById("confirmPassword");

const registerMessage =
    document.getElementById("registerMessage");


// SHOW / HIDE PASSWORD

document.querySelectorAll(".toggle-password")
    .forEach(button => {

        button.addEventListener("click", function () {

            const targetId = this.dataset.target;

            const input = document.getElementById(targetId);

            if (input.type === "password") {
                input.type = "text";
                this.textContent = "Hide";
            } else {
                input.type = "password";
                this.textContent = "Show";
            }

        });

    });


// STUDENT REGISTRATION + BACKEND INTEGRATION

registerForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    registerMessage.textContent = "";
    registerMessage.style.color = "#F87171";

    const nameValue = fullName.value.trim();
    const emailValue = email.value.trim();
    const contactValue = contact.value.trim();
    const passwordValue = password.value;
    const confirmPasswordValue = confirmPassword.value;


    // CHECK REQUIRED FIELDS

    if (
        nameValue === "" ||
        emailValue === "" ||
        passwordValue === "" ||
        confirmPasswordValue === ""
    ) {

        registerMessage.textContent =
            "Please fill all required fields.";

        return;
    }


    // CHECK EMAIL

    if (!email.checkValidity()) {

        registerMessage.textContent =
            "Please enter a valid email address.";

        return;
    }


    // CHECK PASSWORD LENGTH

    if (passwordValue.length < 8) {

        registerMessage.textContent =
            "Password must contain at least 8 characters.";

        return;
    }


    // CHECK PASSWORD MATCH

    if (passwordValue !== confirmPasswordValue) {

        registerMessage.textContent =
            "Passwords do not match.";

        return;
    }


    // FIND REGISTER BUTTON

    const registerButton =
        registerForm.querySelector('button[type="submit"]');

    const originalButtonText = registerButton
        ? registerButton.textContent
        : "";


    if (registerButton) {
        registerButton.disabled = true;
        registerButton.textContent = "Registering...";
    }


    try {

        // SEND REGISTRATION REQUEST TO JAVA BACKEND

        const response = await fetch(
            `${API_BASE_URL}/auth/register`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({

                    name: nameValue,

                    email: emailValue,

                    contact: contactValue,

                    password: passwordValue

                })
            }
        );


        const data = await response.json();


        // HANDLE BACKEND RESPONSE

        if (!response.ok || data.status !== "success") {

            registerMessage.textContent =
                data.message || "Registration failed.";

            return;
        }


        // REGISTRATION SUCCESS

        registerMessage.style.color = "#4ADE80";

        registerMessage.textContent =
            data.message + ". You can now log in.";


        console.log("Student registration successful:", data.user);

        registerForm.reset();

    }


    catch (error) {

        console.error("Registration error:", error);

        registerMessage.textContent =
            "Unable to connect to the server. Make sure the Java API is running.";

    }


    finally {

        if (registerButton) {
            registerButton.disabled = false;
            registerButton.textContent = originalButtonText;
        }

    }

});
