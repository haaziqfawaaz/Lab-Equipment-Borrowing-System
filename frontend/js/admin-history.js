
// ==========================================
// LABTRACK - ADMIN BORROWING RECORDS
// Java REST API Integration
// ==========================================


// API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const adminBorrowingTable =
    document.getElementById("adminBorrowingTable");

const adminHistoryMessage =
    document.getElementById("adminHistoryMessage");

const adminHistoryRefresh =
    document.getElementById("adminHistoryRefresh");

const adminName =
    document.getElementById("adminName");

const adminRole =
    document.getElementById("adminRole");

const adminAvatar =
    document.getElementById("adminAvatar");


// ==========================================
// AUTHENTICATION DETAILS
// ==========================================

const token =
    localStorage.getItem("labtrackToken");

let user = null;

try {

    user = JSON.parse(
        localStorage.getItem("labtrackUser")
    );

} catch (error) {

    console.error(
        "Unable to read saved administrator information:",
        error
    );

}


// CHECK ADMIN ROLE

const isAuthenticatedAdmin =
    Boolean(token) &&
    user !== null &&
    String(user.role).toUpperCase() === "ADMIN";


// ==========================================
// APPLICATION STATE
// ==========================================

let allBorrowingRecords = [];

let isLoading = false;


// ==========================================
// DISPLAY ADMIN INFORMATION
// ==========================================

function loadAdminInformation() {

    if (!user) {
        return;
    }

    const name =
        user.name || "Administrator";

    adminName.textContent = name;

    adminRole.textContent =
        user.role || "ADMIN";

    adminAvatar.textContent =
        name.charAt(0).toUpperCase();

}


// ==========================================
// DISPLAY STATUS MESSAGE
// ==========================================

function showAdminHistoryMessage(message, color) {

    adminHistoryMessage.textContent = message;

    adminHistoryMessage.style.color = color;

}


// ==========================================
// CREATE TABLE CELL
// ==========================================

function createTableCell(value) {

    const cell =
        document.createElement("td");

    cell.textContent =
        value === null || value === undefined
            ? "—"
            : String(value);

    return cell;

}


// ==========================================
// FORMAT FINE AMOUNT
// ==========================================

function formatFineAmount(value) {

    if (value === null || value === undefined) {
        return "—";
    }

    const fine = Number(value);

    if (!Number.isFinite(fine)) {
        return "—";
    }

    return `₹${fine.toFixed(2)}`;

}


// ==========================================
// LOAD ALL BORROWING RECORDS
// ==========================================

async function loadAllBorrowingRecords() {

    if (isLoading) {
        return;
    }


    // CHECK AUTHENTICATION

    if (!isAuthenticatedAdmin) {

        adminBorrowingTable.innerHTML = `
            <tr>
                <td colspan="8">
                    Please log in using an authenticated administrator account.
                </td>
            </tr>
        `;

        showAdminHistoryMessage(
            "Admin authentication is required to view all borrowing records.",
            "#F87171"
        );

        return;

    }


    isLoading = true;

    adminHistoryRefresh.disabled = true;

    adminHistoryRefresh.textContent = "Loading...";


    adminBorrowingTable.innerHTML = `
        <tr>
            <td colspan="8">
                Loading borrowing records from MySQL...
            </td>
        </tr>
    `;


    showAdminHistoryMessage(
        "Fetching all borrowing records from the Java backend...",
        "#94A3B8"
    );


    try {

        // REQUEST ALL BORROWING RECORDS

        const response = await fetch(
            `${API_BASE_URL}/borrow/all`,
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
                data.message ||
                "Unable to retrieve borrowing records."
            );

        }


        // VALIDATE RESPONSE ARRAY

        if (!Array.isArray(data.history)) {

            throw new Error(
                "Invalid borrowing history response from backend."
            );

        }


        // STORE ACTUAL DATABASE RECORDS

        allBorrowingRecords = data.history;


        // DISPLAY RECORDS

        displayAllBorrowingRecords();


        showAdminHistoryMessage(
            "All borrowing records loaded successfully.",
            "#4ADE80"
        );


    } catch (error) {

        adminBorrowingTable.innerHTML = `
            <tr>
                <td colspan="8">
                    Unable to load borrowing records.
                </td>
            </tr>
        `;


        showAdminHistoryMessage(
            error.message ||
            "Unable to connect to the Java backend.",
            "#F87171"
        );


        console.error(
            "Admin borrowing records API error:",
            error
        );


    } finally {

        isLoading = false;

        adminHistoryRefresh.disabled = false;

        adminHistoryRefresh.textContent = "↻ Refresh";

    }

}


// ==========================================
// DISPLAY ALL BORROWING RECORDS
// ==========================================

function displayAllBorrowingRecords() {

    adminBorrowingTable.innerHTML = "";


    // CHECK WHETHER RECORDS EXIST

    if (allBorrowingRecords.length === 0) {

        const row =
            document.createElement("tr");

        const cell =
            document.createElement("td");

        cell.colSpan = 8;

        cell.textContent =
            "No borrowing records found.";

        row.appendChild(cell);

        adminBorrowingTable.appendChild(row);

        return;

    }


    // DISPLAY ACTUAL BACKEND RECORDS

    allBorrowingRecords.forEach(record => {

        const row =
            document.createElement("tr");


        // RECORD ID

        const recordIdCell =
            createTableCell(record.recordId);


        // USER ID

        const userIdCell =
            createTableCell(record.userId);


        // EQUIPMENT ID

        const equipmentIdCell =
            createTableCell(record.equipmentId);


        // BORROW DATE

        const borrowDateCell =
            createTableCell(record.borrowDate);


        // DUE DATE

        const dueDateCell =
            createTableCell(record.dueDate);


        // RETURN DATE

        const returnDateCell =
            createTableCell(record.returnDate);


        // STATUS

        const statusCell =
            createTableCell(record.status);


        // FINE AMOUNT

        const fineCell =
            createTableCell(
                formatFineAmount(record.fineAmount)
            );


        // ADD ALL CELLS

        row.append(
            recordIdCell,
            userIdCell,
            equipmentIdCell,
            borrowDateCell,
            dueDateCell,
            returnDateCell,
            statusCell,
            fineCell
        );


        adminBorrowingTable.appendChild(row);

    });

}


// ==========================================
// REFRESH BUTTON
// ==========================================

adminHistoryRefresh.addEventListener(
    "click",
    function() {

        loadAllBorrowingRecords();

    }
);


// ==========================================
// INITIALIZE ADMIN BORROWING RECORDS PAGE
// ==========================================

loadAdminInformation();

loadAllBorrowingRecords();
