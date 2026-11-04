
// LABTRACK API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// GET DASHBOARD ELEMENTS

const availableCount = document.getElementById("availableCount");
const activeCount = document.getElementById("activeCount");
const historyCount = document.getElementById("historyCount");

const studentName = document.getElementById("studentName");
const studentRole = document.getElementById("studentRole");
const welcomeHeading = document.getElementById("welcomeHeading");

const borrowingTable = document.getElementById("borrowingTable");
const categoryGrid = document.getElementById("categoryGrid");
const dashboardMessage = document.getElementById("dashboardMessage");


// GET SAVED LOGIN DETAILS

const token = localStorage.getItem("labtrackToken");

let user = null;

try {
    user = JSON.parse(localStorage.getItem("labtrackUser"));
} catch (error) {
    console.error("Unable to read saved user details:", error);
}


// CHECK LOGIN SESSION

if (!token || !user) {

    dashboardMessage.textContent =
        "Your login session is missing. Please log in again.";

} else {

    // DISPLAY ACTUAL STUDENT INFORMATION

    const name = user.name || "Student";
    const role = user.role || "STUDENT";

    studentName.textContent = name;
    studentRole.textContent = role;

    welcomeHeading.textContent =
        `Welcome back, ${name}!`;


    // FETCH DATA FROM JAVA REST API

    loadStudentDashboard();

}


// COMMON API FUNCTION

async function fetchDashboardData(endpoint, requiresAuth = false) {

    const headers = {
        "Accept": "application/json"
    };

    if (requiresAuth) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(
        `${API_BASE_URL}${endpoint}`,
        {
            method: "GET",
            headers: headers
        }
    );

    const data = await response.json();

    if (!response.ok || data.status !== "success") {

        throw new Error(
            data.message || `Request failed: ${endpoint}`
        );

    }

    return data;

}


// LOAD DASHBOARD INFORMATION

async function loadStudentDashboard() {

    dashboardMessage.textContent =
        "Loading dashboard information...";

    // Make all three API requests.

    const results = await Promise.allSettled([

        fetchDashboardData("/equipment"),

        fetchDashboardData("/borrow/active", true),

        fetchDashboardData("/borrow/history", true)

    ]);

    const errors = [];


    // 1. AVAILABLE EQUIPMENT

    if (results[0].status === "fulfilled") {

        const equipment = results[0].value.equipment;

        if (Array.isArray(equipment)) {

            // Calculate total available units.

            const totalAvailable = equipment.reduce(
                (total, item) => {

                    return total +
                        Number(item.availableQuantity || 0);

                },
                0
            );

            availableCount.textContent = totalAvailable;

            // Display categories from actual backend records.

            displayCategories(equipment);

        } else {

            errors.push("Equipment data format is invalid.");

        }

    } else {

        errors.push(
            "Equipment: " + results[0].reason.message
        );

    }


    // 2. ACTIVE BORROWINGS

    if (results[1].status === "fulfilled") {

        const activeRecords =
            results[1].value.activeRecords;

        if (Array.isArray(activeRecords)) {

            activeCount.textContent = activeRecords.length;

        } else {

            errors.push("Active borrowing data format is invalid.");

        }

    } else {

        errors.push(
            "Active borrowings: " + results[1].reason.message
        );

    }


    // 3. BORROWING HISTORY

    if (results[2].status === "fulfilled") {

        const history =
            results[2].value.history;

        if (Array.isArray(history)) {

            historyCount.textContent = history.length;

            displayRecentBorrowings(history);

        } else {

            errors.push("Borrowing history data format is invalid.");

        }

    } else {

        errors.push(
            "Borrowing history: " + results[2].reason.message
        );

    }


    // DISPLAY RESULT MESSAGE

    if (errors.length === 0) {

        dashboardMessage.textContent =
            "Dashboard information loaded successfully.";

    } else {

        dashboardMessage.textContent =
            errors.join(" | ");

        console.error("Dashboard API errors:", errors);

    }

}


// DISPLAY ACTUAL LABORATORY CATEGORIES

function displayCategories(equipment) {

    categoryGrid.innerHTML = "";

    const categories = [
        ...new Set(
            equipment
                .map(item => item.category)
                .filter(category =>
                    typeof category === "string" &&
                    category.trim() !== ""
                )
        )
    ];

    if (categories.length === 0) {

        categoryGrid.textContent =
            "No laboratory categories are available.";

        return;

    }

    categories.forEach(category => {

        const card = document.createElement("div");
        card.className = "category-card";

        const icon = document.createElement("div");
        icon.className = "category-icon";
        icon.textContent = "⚗";

        const heading = document.createElement("h3");
        heading.textContent = `${category} Laboratory`;

        const description = document.createElement("p");
        description.textContent =
            `Browse ${category} equipment.`;

        const link = document.createElement("a");

        link.href =
            `equipment.html?category=${encodeURIComponent(category)}`;

        link.textContent = "View Equipment →";

        card.appendChild(icon);
        card.appendChild(heading);
        card.appendChild(description);
        card.appendChild(link);

        categoryGrid.appendChild(card);

    });

}


// DISPLAY RECENT BORROWING RECORDS

function displayRecentBorrowings(history) {

    borrowingTable.innerHTML = "";

    if (history.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 5;
        cell.textContent = "No borrowing records found.";

        row.appendChild(cell);
        borrowingTable.appendChild(row);

        return;

    }

    // Backend returns records in descending record ID order.
    // Display the five most recent records.

    const recentRecords = history.slice(0, 5);

    recentRecords.forEach(record => {

        const row = document.createElement("tr");

        const values = [
            record.recordId,
            record.equipmentId,
            record.borrowDate,
            record.dueDate,
            record.status
        ];

        values.forEach(value => {

            const cell = document.createElement("td");

            cell.textContent =
                value === null || value === undefined
                    ? "—"
                    : String(value);

            row.appendChild(cell);

        });

        borrowingTable.appendChild(row);

    });

}
