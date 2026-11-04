
// ==========================================
// LABTRACK - ADMIN REGISTRATION
// Java REST API Integration
// ==========================================


// API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const adminRegisterForm =
    document.getElementById("adminRegisterForm");

const adminRegisterButton =
    document.getElementById("adminRegisterButton");

const adminRegisterMessage =
    document.getElementById("adminRegisterMessage");

const adminNameInput =
    document.getElementById("adminName");

const adminEmailInput =
    document.getElementById("adminEmail");

const adminContactInput =
    document.getElementById("adminContact");

const adminPasswordInput =
    document.getElementById("adminPassword");

const confirmAdminPasswordInput =
    document.getElementById("confirmAdminPassword");


// PROFILE ELEMENTS

const adminNameDisplay =
    document.getElementById("adminNameDisplay");

const adminRoleDisplay =
    document.getElementById("adminRoleDisplay");

const adminAvatar =
    document.getElementById("adminAvatar");


// ==========================================
// AUTHENTICATION DETAILS
// ==========================================

const token =
    localStorage.getItem("labtrackToken");

let currentUser = null;

try {

    currentUser = JSON.parse(
        localStorage.getItem("labtrackUser")
    );

} catch (error) {

    console.error(
        "Unable to read administrator information:",
        error
    );

}


// VERIFY ADMIN ROLE

const isAuthenticatedAdmin =
    Boolean(token) &&
    currentUser !== null &&
    String(currentUser.role).toUpperCase() === "ADMIN";


// ==========================================
// APPLICATION STATE
// ==========================================

let isRegistering = false;


// ==========================================
// DISPLAY ADMINISTRATOR INFORMATION
// ==========================================

function loadAdminInformation() {

    if (!currentUser) {
        return;
    }

    const name =
        currentUser.name || "Administrator";

    const role =
        currentUser.role || "ADMIN";

    adminNameDisplay.textContent = name;

    adminRoleDisplay.textContent = role;

    adminAvatar.textContent =
        name.charAt(0).toUpperCase();

}


// ==========================================
// DISPLAY MESSAGE
// ==========================================

function showMessage(message, color) {

    adminRegisterMessage.textContent = message;

    adminRegisterMessage.style.color = color;

}


// ==========================================
// ENABLE / DISABLE SUBMIT BUTTON
// ==========================================

function updateRegisterButton() {

    adminRegisterButton.disabled =
        !isAuthenticatedAdmin ||
        isRegistering;

}


// ==========================================
// ADMIN REGISTRATION API
// ==========================================

adminRegisterForm.addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();

        showMessage("", "#94A3B8");


        // CHECK ADMIN AUTHENTICATION

        if (!isAuthenticatedAdmin) {

            showMessage(
                "Please log in using an authenticated administrator account.",
                "#F87171"
            );

            return;

        }


        if (isRegistering) {
            return;
        }


        // GET FORM VALUES

        const name =
            adminNameInput.value.trim();

        const email =
            adminEmailInput.value.trim();

        const contact =
            adminContactInput.value.trim();

        const password =
            adminPasswordInput.value;

        const confirmPassword =
            confirmAdminPasswordInput.value;


        // CHECK REQUIRED FIELDS

        if (
            !name ||
            !email ||
            !password ||
            !confirmPassword
        ) {

            showMessage(
                "Please fill in all required fields.",
                "#F87171"
            );

            return;

        }


        // CHECK EMAIL FORMAT

        if (!adminEmailInput.checkValidity()) {

            showMessage(
                "Please enter a valid email address.",
                "#F87171"
            );

            return;

        }


        // CHECK PASSWORD LENGTH

        if (password.length < 8) {

            showMessage(
                "Password must contain at least 8 characters.",
                "#F87171"
            );

            return;

        }


        // CHECK PASSWORD MATCH

        if (password !== confirmPassword) {

            showMessage(
                "Passwords do not match.",
                "#F87171"
            );

            return;

        }


        // START REQUEST

        isRegistering = true;

        adminRegisterButton.textContent =
            "Registering Administrator...";

        updateRegisterButton();


        try {

            // SEND REQUEST TO CONFIRMED JAVA API

            const response = await fetch(
                `${API_BASE_URL}/auth/register/admin`,
                {
                    method: "POST",

                    headers: {

                        "Content-Type": "application/json",

                        "Authorization": `Bearer ${token}`

                    },

                    body: JSON.stringify({

                        name: name,

                        email: email,

                        contact: contact,

                        password: password

                    })

                }
            );


            const data = await response.json();


            // CHECK BACKEND RESPONSE

            if (!response.ok || data.status !== "success") {

                throw new Error(
                    data.message ||
                    "Administrator registration failed."
                );

            }


            // VERIFY RETURNED USER DETAILS

            if (
                !data.user ||
                String(data.user.role).toUpperCase() !== "ADMIN"
            ) {

                throw new Error(
                    "The backend did not confirm an administrator account."
                );

            }


            // REGISTRATION SUCCESS

            showMessage(
                data.message ||
                "Administrator registered successfully.",
                "#4ADE80"
            );


            console.log(
                "Administrator registered:",
                data.user
            );


            // CLEAR FORM AFTER SUCCESS

            adminRegisterForm.reset();


        } catch (error) {

            showMessage(
                error.message ||
                "Unable to connect to the Java backend.",
                "#F87171"
            );

            console.error(
                "Administrator registration API error:",
                error
            );

        } finally {

            isRegistering = false;

            adminRegisterButton.textContent =
                "Register Administrator";

            updateRegisterButton();

        }

    }
);


// ==========================================
// INITIALIZE ADMIN REGISTRATION
// ==========================================

loadAdminInformation();

updateRegisterButton();


// SHOW AUTHENTICATION MESSAGE WHEN NECESSARY

if (!isAuthenticatedAdmin) {

    showMessage(
        "Please log in using an authenticated administrator account.",
        "#F87171"
    );

}
