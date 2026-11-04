
// ==========================================
// LABTRACK API CONFIGURATION
// ==========================================

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const equipmentGrid =
    document.getElementById("equipmentGrid");

const searchInput =
    document.getElementById("searchEquipment");

const totalEquipment =
    document.getElementById("totalEquipment");

const availableEquipment =
    document.getElementById("availableEquipment");

const equipmentHeading =
    document.getElementById("equipmentHeading");

const equipmentMessage =
    document.getElementById("equipmentMessage");

const equipmentFilters =
    document.getElementById("equipmentFilters");

const studentName =
    document.getElementById("studentName");

const studentRole =
    document.getElementById("studentRole");

const profileAvatar =
    document.getElementById("profileAvatar");


// ==========================================
// APPLICATION STATE
// ==========================================

let allEquipment = [];

let selectedCategory = "ALL";


// Read category from dashboard navigation, if supplied.

const requestedCategory =
    new URLSearchParams(window.location.search)
        .get("category");


// ==========================================
// LOAD USER INFORMATION
// ==========================================

function loadUserInformation() {

    try {

        const savedUser =
            JSON.parse(
                localStorage.getItem("labtrackUser")
            );

        if (savedUser) {

            const name = savedUser.name || "Student";

            const role = savedUser.role || "STUDENT";

            studentName.textContent = name;

            studentRole.textContent = role;

            profileAvatar.textContent =
                name.charAt(0).toUpperCase();

        }

    } catch (error) {

        console.error(
            "Unable to load saved user information:",
            error
        );

    }

}


// ==========================================
// LOAD EQUIPMENT FROM JAVA BACKEND
// ==========================================

async function loadEquipment() {

    equipmentGrid.innerHTML = `
        <div class="equipment-message">
            Loading equipment from database...
        </div>
    `;

    equipmentMessage.textContent = "";


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


        // CHECK BACKEND RESPONSE

        if (!response.ok || data.status !== "success") {

            throw new Error(
                data.message || "Unable to retrieve equipment."
            );

        }


        // BACKEND RETURNS data.equipment

        if (!Array.isArray(data.equipment)) {

            throw new Error(
                "Equipment data format is invalid."
            );

        }


        // STORE REAL DATABASE RECORDS

        allEquipment = data.equipment;


        // UPDATE PAGE INFORMATION

        updateSummary();

        createCategoryFilters();

        displayEquipment();


    } catch (error) {

        equipmentGrid.innerHTML = "";

        equipmentMessage.textContent =
            error.message ||
            "Unable to connect to the Java backend.";

        console.error(
            "Equipment API error:",
            error
        );

    }

}


// ==========================================
// UPDATE INVENTORY SUMMARY
// ==========================================

function updateSummary() {

    // Total number of equipment records

    totalEquipment.textContent =
        allEquipment.length;


    // Number of equipment types with stock available

    const availableCount = allEquipment.filter(
        item => Number(item.availableQuantity) > 0
    ).length;


    availableEquipment.textContent =
        availableCount;

}


// ==========================================
// CREATE DYNAMIC CATEGORY FILTERS
// ==========================================

function createCategoryFilters() {

    equipmentFilters.innerHTML = "";


    // Get unique categories from actual backend records

    const categories = [
        ...new Set(

            allEquipment
                .map(item => item.category)
                .filter(category =>
                    typeof category === "string" &&
                    category.trim() !== ""
                )

        )
    ];


    // Start with ALL

    const allButton = document.createElement("button");

    allButton.type = "button";

    allButton.className = "filter-btn";

    allButton.dataset.category = "ALL";

    allButton.textContent = "All";

    allButton.addEventListener("click", function () {

        selectCategory("ALL");

    });

    equipmentFilters.appendChild(allButton);


    // Create a button for each actual category

    categories.forEach(category => {

        const button = document.createElement("button");

        button.type = "button";

        button.className = "filter-btn";

        button.dataset.category = category;

        button.textContent = category;

        button.addEventListener("click", function () {

            selectCategory(category);

        });

        equipmentFilters.appendChild(button);

    });


    // Restore category selected from dashboard link,
    // but only when that category actually exists.

    const matchingCategory = categories.find(
        category =>
            category.toLowerCase() ===
            String(requestedCategory || "").toLowerCase()
    );


    selectedCategory = matchingCategory || "ALL";

    updateActiveFilter();

}


// ==========================================
// SELECT CATEGORY
// ==========================================

function selectCategory(category) {

    selectedCategory = category;

    updateActiveFilter();

    displayEquipment();

}


// ==========================================
// UPDATE ACTIVE FILTER BUTTON
// ==========================================

function updateActiveFilter() {

    const buttons =
        equipmentFilters.querySelectorAll(".filter-btn");

    buttons.forEach(button => {

        const isActive =
            button.dataset.category === selectedCategory;

        button.classList.toggle("active", isActive);

    });

}


// ==========================================
// DISPLAY EQUIPMENT
// ==========================================

function displayEquipment() {

    const searchValue =
        searchInput.value.trim().toLowerCase();


    // FILTER BY SEARCH AND CATEGORY

    const filteredEquipment = allEquipment.filter(item => {

        const matchesSearch =
            String(item.name || "")
                .toLowerCase()
                .includes(searchValue);


        const matchesCategory =
            selectedCategory === "ALL" ||
            String(item.category || "").toLowerCase() ===
            selectedCategory.toLowerCase();


        return matchesSearch && matchesCategory;

    });


    equipmentGrid.innerHTML = "";

    equipmentMessage.textContent = "";


    // UPDATE HEADING

    equipmentHeading.textContent =
        selectedCategory === "ALL"
            ? "All Equipment"
            : selectedCategory + " Laboratory Equipment";


    // NO MATCHING EQUIPMENT

    if (filteredEquipment.length === 0) {

        equipmentMessage.textContent =
            "No matching equipment found.";

        return;

    }


    // CREATE EQUIPMENT CARDS

    filteredEquipment.forEach(item => {

        const available =
            Number(item.availableQuantity) > 0;


        const card =
            document.createElement("article");

        card.className = "equipment-card";


        // CARD HEADER

        const top =
            document.createElement("div");

        top.className = "equipment-card-top";


        const symbol =
            document.createElement("div");

        symbol.className = "equipment-symbol";

        symbol.textContent = "⚗";


        const category =
            document.createElement("span");

        category.className = "equipment-category";

        category.textContent = item.category || "—";


        top.append(symbol, category);


        // EQUIPMENT NAME

        const name =
            document.createElement("h3");

        name.textContent = item.name || "—";


        // EQUIPMENT ID

        const id =
            document.createElement("p");

        id.className = "equipment-id";

        id.textContent =
            "Equipment ID: " + item.equipmentId;


        // QUANTITY DETAILS

        const details =
            document.createElement("div");

        details.className = "equipment-details";


        const total = createDetail(
            "Total Quantity",
            item.totalQuantity
        );


        const stock = createDetail(
            "Available Quantity",
            item.availableQuantity
        );


        details.append(total, stock);


        // CONDITION STATUS

        const condition =
            document.createElement("p");

        condition.className = "equipment-condition";

        condition.textContent = "Condition: ";


        const conditionValue =
            document.createElement("strong");

        conditionValue.textContent =
            item.conditionStatus || "—";


        condition.appendChild(conditionValue);


        // AVAILABILITY STATUS

        const status =
            document.createElement("p");

        status.className = available
            ? "equipment-status"
            : "equipment-status unavailable";


        status.textContent = available
            ? "● Available"
            : "● Currently unavailable";


        // BORROW BUTTON
        // Borrow transaction integration is still pending.

        const button =
            document.createElement("button");

        button.type = "button";

        button.className = "equipment-borrow-btn";


        button.textContent = available
            ? "Borrow"
            : "Unavailable";


        // Keep disabled until the Borrow module is integrated.
        // This prevents an unconnected button from pretending
        // to complete a real borrowing transaction.

        button.disabled = true;

        button.title = available
            ? "Borrowing will be enabled after Borrow API integration."
            : "No units are currently available.";


        // BUILD CARD

        card.append(
            top,
            name,
            id,
            details,
            condition,
            status,
            button
        );


        equipmentGrid.appendChild(card);

    });

}


// ==========================================
// CREATE QUANTITY DETAIL
// ==========================================

function createDetail(label, value) {

    const container =
        document.createElement("div");


    const title =
        document.createElement("span");

    title.textContent = label;


    const data =
        document.createElement("strong");

    data.textContent =
        value === null || value === undefined
            ? "—"
            : String(value);


    container.append(title, data);

    return container;

}


// ==========================================
// SEARCH FUNCTION
// ==========================================

searchInput.addEventListener("input", function () {

    displayEquipment();

});


// ==========================================
// START APPLICATION
// ==========================================

loadUserInformation();

loadEquipment();
