
// ==========================================
// LABTRACK - BORROWING HISTORY
// Java REST API Integration
// ==========================================


// API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const historyTable =
    document.getElementById("historyTable");

const totalRecords =
    document.getElementById("totalRecords");

const returnedRecords =
    document.getElementById("returnedRecords");

const activeRecords =
    document.getElementById("activeRecords");

const historySearch =
    document.getElementById("historySearch");

const historyRefresh =
    document.getElementById("historyRefresh");

const historyMessage =
    document.getElementById("historyMessage");

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

let allHistoryRecords = [];

let isLoading = false;


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
// DISPLAY MESSAGE
// ==========================================

function showHistoryMessage(message, color) {

    historyMessage.textContent = message;

    historyMessage.style.color = color;

}


// ==========================================
// LOAD BORROWING HISTORY FROM JAVA BACKEND
// ==========================================

async function loadBorrowingHistory() {

    if (isLoading) {
        return;
    }

    if (!isAuthenticatedStudent) {

        totalRecords.textContent = "—";
        returnedRecords.textContent = "—";
        activeRecords.textContent = "—";

        historyTable.innerHTML = `
            <tr>
                <td colspan="7">
                    Please log in with a valid student account.
                </td>
            </tr>
        `;

        showHistoryMessage(
            "Student authentication is required to view borrowing history.",
            "#F87171"
        );

        return;

    }


    isLoading = true;

    historyRefresh.disabled = true;

    historyRefresh.textContent = "Loading...";


    historyTable.innerHTML = `
        <tr>
            <td colspan="7">
                Loading borrowing history...
            </td>
        </tr>
    `;

    showHistoryMessage(
        "Fetching your borrowing history from the database...",
        "#94A3B8"
    );


    try {

        // SEND AUTHENTICATED REQUEST

        const response = await fetch(
            `${API_BASE_URL}/borrow/history`,
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
                data.message || "Unable to retrieve borrowing history."
            );

        }


        // READ HISTORY ARRAY

        if (!Array.isArray(data.history)) {

            throw new Error(
                "Invalid borrowing history response."
            );

        }


        // STORE ACTUAL DATABASE RECORDS

        allHistoryRecords = data.history;


        // UPDATE SUMMARY

        updateHistorySummary();


        // DISPLAY TRANSACTION RECORDS

        displayHistoryRecords();


        showHistoryMessage(
            "Borrowing history loaded successfully.",
            "#4ADE80"
        );


    } catch (error) {

        totalRecords.textContent = "—";
        returnedRecords.textContent = "—";
        activeRecords.textContent = "—";

        historyTable.innerHTML = `
            <tr>
                <td colspan="7">
                    Unable to load borrowing history.
                </td>
            </tr>
        `;

        showHistoryMessage(
            error.message ||
            "Unable to connect to the Java backend.",
            "#F87171"
        );

        console.error(
            "Borrowing history API error:",
            error
        );


    } finally {

        isLoading = false;

        historyRefresh.disabled = false;

        historyRefresh.textContent = "↻ Refresh";

    }

}


// ==========================================
// UPDATE HISTORY SUMMARY
// ==========================================

function updateHistorySummary() {

    // TOTAL TRANSACTIONS

    totalRecords.textContent =
        allHistoryRecords.length;


    // RETURNED TRANSACTIONS

    const returnedCount = allHistoryRecords.filter(
        record =>
            String(record.status).toUpperCase() === "RETURNED"
    ).length;


    returnedRecords.textContent =
        returnedCount;


    // ACTIVE TRANSACTIONS

    const activeCount = allHistoryRecords.filter(
        record =>
            String(record.status).toUpperCase() === "BORROWED"
    ).length;


    activeRecords.textContent =
        activeCount;

}


// ==========================================
// DISPLAY BORROWING HISTORY
// ==========================================

function displayHistoryRecords() {

    const searchValue =
        historySearch.value.trim().toLowerCase();


    // FILTER BY RECORD ID OR EQUIPMENT ID

    const filteredRecords = allHistoryRecords.filter(
        record => {

            const matchesRecordId =
                String(record.recordId ?? "")
                    .toLowerCase()
                    .includes(searchValue);


            const matchesEquipmentId =
                String(record.equipmentId ?? "")
                    .toLowerCase()
                    .includes(searchValue);


            return matchesRecordId || matchesEquipmentId;

        }
    );


    historyTable.innerHTML = "";


    // EMPTY RESULT

    if (filteredRecords.length === 0) {

        const row = document.createElement("tr");

        const cell = document.createElement("td");

        cell.colSpan = 7;

        cell.textContent =
            searchValue
                ? "No matching borrowing records found."
                : "You have no borrowing history yet.";

        row.appendChild(cell);

        historyTable.appendChild(row);

        return;

    }


    // DISPLAY ACTUAL BACKEND RECORDS

    filteredRecords.forEach(record => {

        const row = document.createElement("tr");


        // RECORD ID

        const recordIdCell =
            document.createElement("td");

        recordIdCell.textContent =
            record.recordId ?? "—";


        // EQUIPMENT ID

        const equipmentIdCell =
            document.createElement("td");

        equipmentIdCell.textContent =
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


        // RETURN DATE

        const returnDateCell =
            document.createElement("td");

        returnDateCell.textContent =
            record.returnDate ?? "—";


        // STATUS

        const statusCell =
            document.createElement("td");

        statusCell.textContent =
            record.status ?? "—";


        // FINE AMOUNT

        const fineCell =
            document.createElement("td");

        if (
            record.fineAmount === null ||
            record.fineAmount === undefined
        ) {

            fineCell.textContent = "—";

        } else {

            const fine = Number(record.fineAmount);

            fineCell.textContent =
                Number.isFinite(fine)
                    ? `₹${fine.toFixed(2)}`
                    : "—";

        }


        // ADD ALL CELLS

        row.append(
            recordIdCell,
            equipmentIdCell,
            borrowDateCell,
            dueDateCell,
            returnDateCell,
            statusCell,
            fineCell
        );


        historyTable.appendChild(row);

    });

}


// ==========================================
// SEARCH FUNCTION
// ==========================================

historySearch.addEventListener(
    "input",
    function() {

        displayHistoryRecords();

    }
);


// ==========================================
// REFRESH BUTTON
// ==========================================

historyRefresh.addEventListener(
    "click",
    function() {

        loadBorrowingHistory();

    }
);


// ==========================================
// INITIALIZE HISTORY PAGE
// ==========================================

loadStudentInformation();

loadBorrowingHistory();
