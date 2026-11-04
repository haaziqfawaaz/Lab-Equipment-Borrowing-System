
// ==========================================
// LABTRACK - ADMIN DASHBOARD
// Java REST API Integration
// ==========================================


// API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const totalEquipment =
    document.getElementById("totalEquipment");

const totalQuantity =
    document.getElementById("totalQuantity");

const availableQuantity =
    document.getElementById("availableQuantity");

const equipmentTable =
    document.getElementById("adminEquipmentTable");

const adminMessage =
    document.getElementById("adminMessage");

const adminName =
    document.getElementById("adminName");

const adminRole =
    document.getElementById("adminRole");

const adminAvatar =
    document.getElementById("adminAvatar");

const adminWelcome =
    document.getElementById("adminWelcome");


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


// Check that the saved session belongs to an administrator.
// This is a frontend navigation check, not server-side security.
// Protected admin operations are independently checked by Java.

const isAuthenticatedAdmin =
    Boolean(token) &&
    user !== null &&
    String(user.role).toUpperCase() === "ADMIN";


// ==========================================
// APPLICATION STATE
// ==========================================

let equipmentList = [];


// ==========================================
// SHOW MESSAGE
// ==========================================

function showAdminMessage(message, color) {

    adminMessage.textContent = message;

    adminMessage.style.color = color;

}


// ==========================================
// DISPLAY ADMINISTRATOR INFORMATION
// ==========================================

function loadAdminInformation() {

    if (!user) {
        return;
    }

    const name =
        user.name || "Administrator";

    const role =
        user.role || "ADMIN";

    adminName.textContent = name;

    adminRole.textContent = role;

    adminAvatar.textContent =
        name.charAt(0).toUpperCase();

    adminWelcome.textContent =
        `Welcome back, ${name}!`;

}


// ==========================================
// INITIALIZE EMPTY DASHBOARD
// ==========================================

function showEmptyDashboard() {

    totalEquipment.textContent = "—";

    totalQuantity.textContent = "—";

    availableQuantity.textContent = "—";

    equipmentTable.innerHTML = `
        <tr>
            <td colspan="6">
                Inventory information is unavailable.
            </td>
        </tr>
    `;

}


// ==========================================
// LOAD EQUIPMENT FROM JAVA BACKEND
// ==========================================

async function loadAdminDashboard() {

    if (!isAuthenticatedAdmin) {

        showEmptyDashboard();

        showAdminMessage(
            "Please log in using an authenticated administrator account.",
            "#F87171"
        );

        return;

    }


    showAdminMessage(
        "Loading inventory information...",
        "#94A3B8"
    );


    equipmentTable.innerHTML = `
        <tr>
            <td colspan="6">
                Loading equipment inventory...
            </td>
        </tr>
    `;


    try {

        // GET ALL EQUIPMENT FROM JAVA REST API

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


        // CHECK API RESPONSE

        if (!response.ok || data.status !== "success") {

            throw new Error(
                data.message ||
                "Unable to retrieve equipment inventory."
            );

        }


        // VALIDATE EQUIPMENT ARRAY

        if (!Array.isArray(data.equipment)) {

            throw new Error(
                "Invalid equipment response from backend."
            );

        }


        // STORE ACTUAL MYSQL RECORDS

        equipmentList = data.equipment;


        // UPDATE INVENTORY SUMMARY

        updateInventorySummary();


        // DISPLAY INVENTORY TABLE

        displayEquipmentInventory();


        showAdminMessage(
            "Inventory information loaded successfully.",
            "#4ADE80"
        );


    } catch (error) {

        showEmptyDashboard();

        showAdminMessage(
            error.message ||
            "Unable to connect to the Java backend.",
            "#F87171"
        );

        console.error(
            "Admin dashboard API error:",
            error
        );

    }

}


// ==========================================
// UPDATE INVENTORY SUMMARY
// ==========================================

function updateInventorySummary() {

    // TOTAL EQUIPMENT TYPES

    totalEquipment.textContent =
        equipmentList.length;


    // TOTAL QUANTITY

    const totalUnits = equipmentList.reduce(
        (total, item) => {

            return total +
                Number(item.totalQuantity || 0);

        },
        0
    );


    totalQuantity.textContent =
        totalUnits;


    // AVAILABLE QUANTITY

    const availableUnits = equipmentList.reduce(
        (total, item) => {

            return total +
                Number(item.availableQuantity || 0);

        },
        0
    );


    availableQuantity.textContent =
        availableUnits;

}


// ==========================================
// DISPLAY EQUIPMENT INVENTORY
// ==========================================

function displayEquipmentInventory() {

    equipmentTable.innerHTML = "";


    // CHECK WHETHER EQUIPMENT EXISTS

    if (equipmentList.length === 0) {

        const row =
            document.createElement("tr");

        const cell =
            document.createElement("td");

        cell.colSpan = 6;

        cell.textContent =
            "No equipment records found.";

        row.appendChild(cell);

        equipmentTable.appendChild(row);

        return;

    }


    // DISPLAY ACTUAL EQUIPMENT RECORDS

    equipmentList.forEach(item => {

        const row =
            document.createElement("tr");


        // EQUIPMENT ID

        const idCell =
            createTableCell(item.equipmentId);


        // NAME

        const nameCell =
            createTableCell(item.name);


        // CATEGORY

        const categoryCell =
            createTableCell(item.category);


        // TOTAL QUANTITY

        const totalCell =
            createTableCell(item.totalQuantity);


        // AVAILABLE QUANTITY

        const availableCell =
            createTableCell(item.availableQuantity);


        // CONDITION STATUS

        const conditionCell =
            createTableCell(item.conditionStatus);


        row.append(
            idCell,
            nameCell,
            categoryCell,
            totalCell,
            availableCell,
            conditionCell
        );


        equipmentTable.appendChild(row);

    });

}


// ==========================================
// CREATE SAFE TABLE CELL
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
// START ADMIN DASHBOARD
// ==========================================

loadAdminInformation();

loadAdminDashboard();
