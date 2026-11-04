
// ==========================================
// LABTRACK - RETURN EQUIPMENT
// Java REST API Integration
// ==========================================


// API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const returnTable =
    document.getElementById("returnTable");

const activeReturnsCount =
    document.getElementById("activeReturnsCount");

const overdueCount =
    document.getElementById("overdueCount");

const returnMessage =
    document.getElementById("returnMessage");

const refreshButton =
    document.getElementById("refreshButton");

const studentName =
    document.getElementById("studentName");

const studentRole =
    document.getElementById("studentRole");

const profileAvatar =
    document.getElementById("profileAvatar");


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
        "Unable to read saved user information:",
        error
    );

}


const isAuthenticatedStudent =
    Boolean(token) &&
    user !== null &&
    String(user.role).toUpperCase() === "STUDENT";


// ==========================================
// APPLICATION STATE
// ==========================================

let activeRecords = [];

let isRefreshing = false;

let returningRecordIds = new Set();


// ==========================================
// DISPLAY STUDENT INFORMATION
// ==========================================

function loadStudentInformation() {

    if (!user) {
        return;
    }

    const name = user.name || "Student";

    studentName.textContent = name;

    studentRole.textContent =
        user.role || "STUDENT";

    profileAvatar.textContent =
        name.charAt(0).toUpperCase();

}


// ==========================================
// SHOW STATUS MESSAGE
// ==========================================

function showReturnMessage(message, color) {

    returnMessage.textContent = message;

    returnMessage.style.color = color;

}


// ==========================================
// GET LOCAL DATE
// ==========================================

function getTodayDate() {

    const today = new Date();

    const year = today.getFullYear();

    const month = String(
        today.getMonth() + 1
    ).padStart(2, "0");

    const day = String(
        today.getDate()
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;

}


// ==========================================
// CHECK WHETHER RECORD IS OVERDUE
// ==========================================

function isOverdue(record) {

    if (!record.dueDate) {
        return false;
    }

    const dueDate = String(record.dueDate);

    const today = getTodayDate();

    return dueDate < today;

}


// ==========================================
// LOAD ACTIVE BORROWING RECORDS
// ==========================================

async function loadActiveRecords(showLoading = true) {

    if (!isAuthenticatedStudent) {

        activeReturnsCount.textContent = "—";

        overdueCount.textContent = "—";

        returnTable.innerHTML = `
            <tr>
                <td colspan="6">
                    Please log in with a valid student account.
                </td>
            </tr>
        `;

        showReturnMessage(
            "Student authentication is required to view borrowing records.",
            "#F87171"
        );

        return false;

    }


    if (showLoading) {

        returnTable.innerHTML = `
            <tr>
                <td colspan="6">
                    Loading your active borrowing records...
                </td>
            </tr>
        `;

        showReturnMessage(
            "Fetching records from the Java backend...",
            "#94A3B8"
        );

    }


    try {

        const response = await fetch(
            `${API_BASE_URL}/borrow/active`,
            {
                method: "GET",

                headers: {
                    "Accept": "application/json",
                    "Authorization": `Bearer ${token}`
                }
            }
        );


        const data = await response.json();


        // CHECK BACKEND RESPONSE

        if (!response.ok || data.status !== "success") {

            throw new Error(
                data.message || "Unable to retrieve borrowing records."
            );

        }


        // READ ACTUAL ACTIVE RECORDS

        if (!Array.isArray(data.activeRecords)) {

            throw new Error(
                "Invalid active borrowing response."
            );

        }


        activeRecords = data.activeRecords;


        // UPDATE SUMMARY

        updateReturnSummary();


        // DISPLAY RECORDS

        displayActiveRecords();


        if (showLoading) {

            showReturnMessage(
                "Active borrowing records loaded successfully.",
                "#4ADE80"
            );

        }

        return true;


    } catch (error) {

        activeReturnsCount.textContent = "—";

        overdueCount.textContent = "—";

        returnTable.innerHTML = `
            <tr>
                <td colspan="6">
                    Unable to load borrowing records.
                </td>
            </tr>
        `;

        showReturnMessage(
            error.message ||
            "Unable to connect to the Java backend.",
            "#F87171"
        );

        console.error(
            "Active borrowing API error:",
            error
        );

        return false;

    }

}


// ==========================================
// UPDATE RETURN SUMMARY
// ==========================================

function updateReturnSummary() {

    activeReturnsCount.textContent =
        activeRecords.length;


    const overdueRecords = activeRecords.filter(
        record => isOverdue(record)
    );


    overdueCount.textContent =
        overdueRecords.length;

}


// ==========================================
// DISPLAY ACTIVE BORROWING TABLE
// ==========================================

function displayActiveRecords() {

    returnTable.innerHTML = "";


    // NO ACTIVE BORROWINGS

    if (activeRecords.length === 0) {

        const row = document.createElement("tr");

        const cell = document.createElement("td");

        cell.colSpan = 6;

        cell.textContent =
            "You currently have no active borrowings.";

        row.appendChild(cell);

        returnTable.appendChild(row);

        return;

    }


    // CREATE ROW FOR EACH REAL DATABASE RECORD

    activeRecords.forEach(record => {

        const row = document.createElement("tr");


        // RECORD ID

        const recordCell =
            document.createElement("td");

        recordCell.textContent =
            record.recordId ?? "—";


        // EQUIPMENT ID

        const equipmentCell =
            document.createElement("td");

        equipmentCell.textContent =
            record.equipmentId ?? "—";


        // BORROW DATE

        const borrowDateCell =
            document.createElement("td");

        borrowDateCell.textContent =
            record.borrowDate ?? "—";


        // DUE DATE

        const dueDateCell =
            document.createElement("td");

        dueDateCell.textContent =
            record.dueDate ?? "—";


        // STATUS

        const statusCell =
            document.createElement("td");

        statusCell.textContent =
            record.status ?? "—";


        // RETURN ACTION

        const actionCell =
            document.createElement("td");


        const returnButton =
            document.createElement("button");

        returnButton.type = "button";

        returnButton.className = "return-refresh-btn";

        returnButton.textContent = "Return";


        const recordId = Number(record.recordId);


        returnButton.disabled =
            returningRecordIds.has(recordId) ||
            !isAuthenticatedStudent;


        returnButton.title =
            "Return this borrowing record";


        returnButton.addEventListener(
            "click",
            function() {

                returnEquipment(recordId, returnButton);

            }
        );


        actionCell.appendChild(returnButton);


        // ADD ALL CELLS

        row.append(
            recordCell,
            equipmentCell,
            borrowDateCell,
            dueDateCell,
            statusCell,
            actionCell
        );


        returnTable.appendChild(row);

    });

}


// ==========================================
// RETURN EQUIPMENT API
// ==========================================

async function returnEquipment(recordId, button) {

    if (!isAuthenticatedStudent) {

        showReturnMessage(
            "Please log in with a valid student account.",
            "#F87171"
        );

        return;

    }


    const record = activeRecords.find(
        item => Number(item.recordId) === recordId
    );


    if (!record) {

        showReturnMessage(
            "The selected borrowing record could not be found.",
            "#F87171"
        );

        return;

    }


    if (returningRecordIds.has(recordId)) {
        return;
    }


    // ASK FOR CONFIRMATION

    const confirmed = window.confirm(
        `Are you sure you want to return borrowing record #${recordId}?`
    );


    if (!confirmed) {
        return;
    }


    returningRecordIds.add(recordId);

    button.disabled = true;

    button.textContent = "Returning...";


    showReturnMessage(
        "Processing your equipment return request...",
        "#38BDF8"
    );


    try {

        // SEND RETURN REQUEST TO JAVA BACKEND

        const response = await fetch(
            `${API_BASE_URL}/borrow/return`,
            {
                method: "POST",

                headers: {

                    "Content-Type": "application/json",

                    "Authorization": `Bearer ${token}`

                },

                body: JSON.stringify({

                    recordId: recordId

                })

            }
        );


        const data = await response.json();


        // CHECK BACKEND RESPONSE

        if (!response.ok || data.status !== "success") {

            throw new Error(
                data.message || "Equipment return failed."
            );

        }


        // VERIFY BORROW RECORD IN RESPONSE

        const returnedRecord = data.borrowRecord;


        if (!returnedRecord) {

            throw new Error(
                "Return response did not contain a borrowing record."
            );

        }


        if (
            Number(returnedRecord.recordId) !== recordId ||
            String(returnedRecord.status).toUpperCase() !== "RETURNED"
        ) {

            throw new Error(
                "The backend response does not confirm this return."
            );

        }


        // REFRESH ACTIVE BORROWINGS FROM DATABASE

        const refreshed =
            await loadActiveRecords(false);


        if (!refreshed) {

            showReturnMessage(
                "Equipment returned successfully, but the active records could not be refreshed. Use Refresh to reload them.",
                "#4ADE80"
            );

            return;

        }


        // SHOW SUCCESS AFTER BACKEND CONFIRMATION

        const fineAmount =
            returnedRecord.fineAmount ?? "0.00";


        showReturnMessage(
            `${data.message || "Equipment returned successfully."} Fine amount: ₹${fineAmount}`,
            "#4ADE80"
        );


        console.log(
            "Returned borrowing record:",
            returnedRecord
        );


    } catch (error) {

        returningRecordIds.delete(recordId);

        showReturnMessage(
            error.message ||
            "Unable to complete the return request.",
            "#F87171"
        );

        console.error(
            "Return API error:",
            error
        );

        // Re-display the current active records.

        await loadActiveRecords(false);

    } finally {

        returningRecordIds.delete(recordId);

    }

}


// ==========================================
// REFRESH BUTTON
// ==========================================

refreshButton.addEventListener(
    "click",
    async function() {

        if (isRefreshing) {
            return;
        }

        isRefreshing = true;

        refreshButton.disabled = true;

        refreshButton.textContent = "Refreshing...";


        await loadActiveRecords(true);


        isRefreshing = false;

        refreshButton.disabled = false;

        refreshButton.textContent = "↻ Refresh";

    }
);


// ==========================================
// INITIALIZE RETURN PAGE
// ==========================================

loadStudentInformation();

loadActiveRecords();
