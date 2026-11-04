
// ==========================================
// LABTRACK API CONFIGURATION
// ==========================================

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const equipmentSelect =
    document.getElementById("equipmentSelect");

const categoryInput =
    document.getElementById("category");

const equipmentIdInput =
    document.getElementById("equipmentId");

const availableInput =
    document.getElementById("availableQuantity");

const dueDateInput =
    document.getElementById("dueDate");

const borrowForm =
    document.getElementById("borrowForm");

const borrowMessage =
    document.getElementById("borrowMessage");

const submitButton =
    document.getElementById("borrowButton");

const studentName =
    document.getElementById("studentName");

const studentRole =
    document.getElementById("studentRole");

const profileAvatar =
    document.getElementById("profileAvatar");


// ==========================================
// APPLICATION STATE
// ==========================================

let equipmentList = [];

let isSubmitting = false;


// ==========================================
// AUTHENTICATION DETAILS
// ==========================================

const token = localStorage.getItem("labtrackToken");

let user = null;

try {

    user = JSON.parse(
        localStorage.getItem("labtrackUser")
    );

} catch (error) {

    console.error(
        "Unable to read user information:",
        error
    );

}


const isAuthenticatedStudent =
    Boolean(token) &&
    user !== null &&
    String(user.role).toUpperCase() === "STUDENT";


// ==========================================
// DISPLAY STUDENT INFORMATION
// ==========================================

function loadStudentInformation() {

    if (!user) {
        return;
    }

    const name = user.name || "Student";

    studentName.textContent = name;

    studentRole.textContent = user.role || "STUDENT";

    profileAvatar.textContent =
        name.charAt(0).toUpperCase();

}


// ==========================================
// SHOW MESSAGE
// ==========================================

function showBorrowMessage(message, color) {

    borrowMessage.textContent = message;

    borrowMessage.style.color = color;

}


// ==========================================
// LOAD EQUIPMENT FROM JAVA BACKEND
// ==========================================

async function loadEquipment() {

    equipmentSelect.innerHTML = `
        <option value="">Loading equipment...</option>
    `;

    try {

        const response = await fetch(
            `${API_BASE_URL}/equipment`,
            {
                method: "GET",

                headers: {
                    "Accept": "application/json"
                }
            }
        );


        const data = await response.json();


        if (!response.ok || data.status !== "success") {

            throw new Error(
                data.message || "Unable to load equipment."
            );

        }


        // BACKEND RETURNS data.equipment

        if (!Array.isArray(data.equipment)) {

            throw new Error(
                "Invalid equipment response."
            );

        }


        equipmentList = data.equipment;


        // RESET DROPDOWN

        equipmentSelect.innerHTML = "";

        const defaultOption =
            document.createElement("option");

        defaultOption.value = "";

        defaultOption.textContent =
            "Select equipment";

        equipmentSelect.appendChild(defaultOption);


        // ADD ACTUAL DATABASE EQUIPMENT

        equipmentList.forEach(item => {

            const option =
                document.createElement("option");

            option.value = item.equipmentId;

            const available =
                Number(item.availableQuantity);

            option.textContent =
                `${item.name} (${item.category}) - ${available} available`;

            // Prevent selecting equipment with no stock.

            option.disabled = available <= 0;

            equipmentSelect.appendChild(option);

        });


        if (isAuthenticatedStudent) {

            showBorrowMessage(
                "Equipment inventory loaded successfully.",
                "#4ADE80"
            );

        } else {

            showBorrowMessage(
                "Please log in with a student account to borrow equipment.",
                "#F87171"
            );

        }


        updateBorrowButton();


    } catch (error) {

        equipmentSelect.innerHTML = `
            <option value="">Equipment unavailable</option>
        `;

        showBorrowMessage(
            error.message ||
            "Unable to connect to the Java backend.",
            "#F87171"
        );

        console.error(
            "Equipment loading error:",
            error
        );

    }

}


// ==========================================
// UPDATE SELECTED EQUIPMENT DETAILS
// ==========================================

function updateSelectedEquipment() {

    const selected = equipmentList.find(
        item =>
            String(item.equipmentId) ===
            equipmentSelect.value
    );


    if (!selected) {

        equipmentIdInput.value = "";

        categoryInput.value = "";

        availableInput.textContent = "—";

        dueDateInput.value = "";

        updateBorrowButton();

        return;

    }


    equipmentIdInput.value =
        selected.equipmentId;

    categoryInput.value =
        selected.category;

    availableInput.textContent =
        selected.availableQuantity;


    // The actual due date will be supplied
    // by the Java backend after successful borrowing.

    dueDateInput.value = "";


    if (Number(selected.availableQuantity) <= 0) {

        showBorrowMessage(
            "This equipment is currently unavailable.",
            "#F87171"
        );

    } else {

        borrowMessage.textContent = "";

    }


    updateBorrowButton();

}


// ==========================================
// UPDATE BUTTON AVAILABILITY
// ==========================================

function updateBorrowButton() {

    const selected = equipmentList.find(
        item =>
            String(item.equipmentId) ===
            equipmentSelect.value
    );


    const hasStock =
        selected &&
        Number(selected.availableQuantity) > 0;


    submitButton.disabled =
        !isAuthenticatedStudent ||
        !hasStock ||
        isSubmitting;

}


// ==========================================
// BORROW EQUIPMENT API
// ==========================================

borrowForm.addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();


        if (!isAuthenticatedStudent) {

            showBorrowMessage(
                "Please log in with a valid student account.",
                "#F87171"
            );

            return;

        }


        const equipmentId =
            Number(equipmentSelect.value);


        if (!equipmentId) {

            showBorrowMessage(
                "Please select equipment.",
                "#F87171"
            );

            return;

        }


        const selected = equipmentList.find(
            item =>
                Number(item.equipmentId) === equipmentId
        );


        if (
            !selected ||
            Number(selected.availableQuantity) <= 0
        ) {

            showBorrowMessage(
                "This equipment is currently unavailable.",
                "#F87171"
            );

            return;

        }


        if (isSubmitting) {
            return;
        }


        isSubmitting = true;

        submitButton.textContent = "Processing Request...";

        updateBorrowButton();


        try {

            // SEND BORROW REQUEST TO JAVA BACKEND

            const response = await fetch(
                `${API_BASE_URL}/borrow`,
                {
                    method: "POST",

                    headers: {

                        "Content-Type": "application/json",

                        "Authorization": `Bearer ${token}`

                    },

                    body: JSON.stringify({

                        equipmentId: equipmentId

                    })

                }
            );


            const data = await response.json();


            // CHECK BACKEND RESPONSE

            if (!response.ok || data.status !== "success") {

                throw new Error(
                    data.message || "Borrowing request failed."
                );

            }


            // READ ACTUAL BACKEND BORROW RECORD

            const record = data.borrowRecord;

            if (!record) {

                throw new Error(
                    "Borrowing response did not contain a borrow record."
                );

            }


            // DISPLAY THE ACTUAL DUE DATE FROM JAVA

            if (record.dueDate) {

                dueDateInput.value = record.dueDate;

            }


            // REFRESH EQUIPMENT STOCK FROM DATABASE

            await loadEquipment();


            // CLEAR SELECTION AFTER SUCCESS

            equipmentSelect.value = "";

            equipmentIdInput.value = "";

            categoryInput.value = "";

            availableInput.textContent = "—";


            // RESTORE CONFIRMED DUE DATE

            dueDateInput.value =
                record.dueDate || "";


            // SHOW SUCCESS ONLY AFTER BACKEND CONFIRMATION

            showBorrowMessage(
                data.message || "Equipment borrowed successfully.",
                "#4ADE80"
            );


            console.log(
                "Borrowing record received from backend:",
                record
            );


        } catch (error) {

            showBorrowMessage(
                error.message ||
                "Unable to complete borrowing request.",
                "#F87171"
            );

            console.error(
                "Borrowing API error:",
                error
            );

        } finally {

            isSubmitting = false;

            submitButton.textContent =
                "Submit Borrow Request";

            updateBorrowButton();

        }

    }
);


// ==========================================
// EQUIPMENT SELECTION EVENT
// ==========================================

equipmentSelect.addEventListener(
    "change",
    updateSelectedEquipment
);


// ==========================================
// INITIALIZE BORROW PAGE
// ==========================================

loadStudentInformation();

loadEquipment();
