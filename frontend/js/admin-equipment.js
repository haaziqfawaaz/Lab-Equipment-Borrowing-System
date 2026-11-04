
// ==========================================
// LABTRACK - ADMIN EQUIPMENT
// Java REST API Integration
// ==========================================


// API CONFIGURATION

const API_BASE_URL = "http://localhost:8080/api";


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const addForm =
    document.getElementById("addEquipmentForm");

const updateForm =
    document.getElementById("updateEquipmentForm");

const addButton =
    document.getElementById("addEquipmentButton");

const updateButton =
    document.getElementById("updateEquipmentButton");

const equipmentTable =
    document.getElementById("equipmentTable");

const equipmentMessage =
    document.getElementById("equipmentAdminMessage");

const adminName =
    document.getElementById("adminName");

const adminRole =
    document.getElementById("adminRole");

const adminAvatar =
    document.getElementById("adminAvatar");


// ADD FORM INPUTS

const equipmentName =
    document.getElementById("equipmentName");

const equipmentCategory =
    document.getElementById("equipmentCategory");

const totalQuantity =
    document.getElementById("totalQuantity");

const conditionStatus =
    document.getElementById("conditionStatus");


// UPDATE FORM INPUTS

const updateEquipmentId =
    document.getElementById("updateEquipmentId");

const updateEquipmentName =
    document.getElementById("updateEquipmentName");

const updateCategory =
    document.getElementById("updateCategory");

const updateTotalQuantity =
    document.getElementById("updateTotalQuantity");

const updateAvailableQuantity =
    document.getElementById("updateAvailableQuantity");

const updateCondition =
    document.getElementById("updateCondition");


// ==========================================
// AUTHENTICATION
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


const isAuthenticatedAdmin =
    Boolean(token) &&
    user !== null &&
    String(user.role).toUpperCase() === "ADMIN";


// ==========================================
// APPLICATION STATE
// ==========================================

let equipmentList = [];

let isAddingEquipment = false;

let isUpdatingEquipment = false;


// ==========================================
// MESSAGE DISPLAY
// ==========================================

function showEquipmentMessage(message, color) {

    equipmentMessage.textContent = message;

    equipmentMessage.style.color = color;

}


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
// UPDATE BUTTON STATES
// ==========================================

function updateButtonStates() {

    addButton.disabled =
        !isAuthenticatedAdmin ||
        isAddingEquipment;

    updateButton.disabled =
        !isAuthenticatedAdmin ||
        isUpdatingEquipment;

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
// LOAD EQUIPMENT FROM BACKEND
// ==========================================

async function loadEquipment() {

    if (!isAuthenticatedAdmin) {

        equipmentTable.innerHTML = `
            <tr>
                <td colspan="6">
                    An authenticated administrator login is required.
                </td>
            </tr>
        `;

        showEquipmentMessage(
            "Please log in using an authenticated administrator account.",
            "#F87171"
        );

        return false;

    }


    equipmentTable.innerHTML = `
        <tr>
            <td colspan="6">
                Loading equipment inventory...
            </td>
        </tr>
    `;


    try {

        const response = await fetch(
            `${API_BASE_URL}/equipment`,
            {
                method: "GET",

                headers: {
                    "Accept": "application/json",
                    "Authorization": `Bearer ${token}`
                }
            }
        );


        const data = await response.json();


        if (!response.ok || data.status !== "success") {

            throw new Error(
                data.message ||
                "Unable to retrieve equipment inventory."
            );

        }


        if (!Array.isArray(data.equipment)) {

            throw new Error(
                "Invalid equipment response from backend."
            );

        }


        equipmentList = data.equipment;


        displayEquipmentInventory();


        showEquipmentMessage(
            "Equipment inventory loaded successfully.",
            "#4ADE80"
        );

        return true;


    } catch (error) {

        equipmentList = [];

        equipmentTable.innerHTML = `
            <tr>
                <td colspan="6">
                    Unable to load equipment inventory.
                </td>
            </tr>
        `;

        showEquipmentMessage(
            error.message ||
            "Unable to connect to the Java backend.",
            "#F87171"
        );

        console.error(
            "Equipment loading error:",
            error
        );

        return false;

    }

}


// ==========================================
// DISPLAY EQUIPMENT INVENTORY
// ==========================================

function displayEquipmentInventory() {

    equipmentTable.innerHTML = "";


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


    equipmentList.forEach(item => {

        const row =
            document.createElement("tr");


        const idCell =
            createTableCell(item.equipmentId);

        const nameCell =
            createTableCell(item.name);

        const categoryCell =
            createTableCell(item.category);

        const totalCell =
            createTableCell(item.totalQuantity);

        const availableCell =
            createTableCell(item.availableQuantity);

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
// ADD EQUIPMENT API
// ==========================================

addForm.addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();


        if (!isAuthenticatedAdmin) {

            showEquipmentMessage(
                "Please log in using an authenticated administrator account.",
                "#F87171"
            );

            return;

        }


        if (isAddingEquipment) {
            return;
        }


        const name = equipmentName.value.trim();

        const category = equipmentCategory.value.trim();

        const quantity = Number(totalQuantity.value);

        const condition = conditionStatus.value.trim();


        // FRONTEND VALIDATION

        if (!name || !category || !totalQuantity.value) {

            showEquipmentMessage(
                "Please fill all required equipment details.",
                "#F87171"
            );

            return;

        }


        if (
            !Number.isInteger(quantity) ||
            quantity <= 0
        ) {

            showEquipmentMessage(
                "Enter a valid total quantity.",
                "#F87171"
            );

            return;

        }


        isAddingEquipment = true;

        addButton.textContent = "Adding Equipment...";

        updateButtonStates();


        try {

            // POST REQUEST TO JAVA BACKEND

            const response = await fetch(
                `${API_BASE_URL}/equipment`,
                {
                    method: "POST",

                    headers: {

                        "Content-Type": "application/json",

                        "Authorization": `Bearer ${token}`

                    },

                    body: JSON.stringify({

                        name: name,

                        category: category,

                        totalQuantity: quantity,

                        conditionStatus: condition

                    })

                }
            );


            const data = await response.json();


            if (!response.ok || data.status !== "success") {

                throw new Error(
                    data.message || "Equipment addition failed."
                );

            }


            // ENSURE BACKEND RETURNED THE CREATED EQUIPMENT

            if (!data.equipment) {

                throw new Error(
                    "The backend did not return the created equipment record."
                );

            }


            // CLEAR FORM

            addForm.reset();


            // REFRESH ACTUAL INVENTORY

            const refreshed = await loadEquipment();


            if (!refreshed) {

                showEquipmentMessage(
                    "Equipment was added successfully, but the inventory refresh failed. Use the page refresh to reload it.",
                    "#4ADE80"
                );

                return;

            }


            showEquipmentMessage(
                `${data.message || "Equipment added successfully."} Equipment ID: ${data.equipment.equipmentId}`,
                "#4ADE80"
            );


            console.log(
                "Equipment added:",
                data.equipment
            );


        } catch (error) {

            showEquipmentMessage(
                error.message ||
                "Unable to add equipment.",
                "#F87171"
            );

            console.error(
                "Add equipment API error:",
                error
            );

        } finally {

            isAddingEquipment = false;

            addButton.textContent = "Add Equipment";

            updateButtonStates();

        }

    }
);


// ==========================================
// LOAD EXISTING EQUIPMENT DETAILS
// ==========================================

function loadExistingEquipmentDetails() {

    const id = Number(updateEquipmentId.value);


    if (!Number.isInteger(id) || id <= 0) {
        return;
    }


    const item = equipmentList.find(
        equipment =>
            Number(equipment.equipmentId) === id
    );


    if (!item) {

        updateEquipmentName.value = "";

        updateCategory.value = "";

        updateTotalQuantity.value = "";

        updateAvailableQuantity.value = "";

        updateCondition.value = "";

        showEquipmentMessage(
            "Equipment ID not found in the current inventory.",
            "#F87171"
        );

        return;

    }


    // POPULATE CURRENT DATABASE VALUES

    updateEquipmentName.value =
        item.name ?? "";

    updateCategory.value =
        item.category ?? "";

    updateTotalQuantity.value =
        item.totalQuantity ?? "";

    updateAvailableQuantity.value =
        item.availableQuantity ?? "";

    updateCondition.value =
        item.conditionStatus ?? "";


    showEquipmentMessage(
        `Loaded current details for Equipment ID ${id}.`,
        "#38BDF8"
    );

}


// ==========================================
// UPDATE EQUIPMENT API
// ==========================================

updateForm.addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();


        if (!isAuthenticatedAdmin) {

            showEquipmentMessage(
                "Please log in using an authenticated administrator account.",
                "#F87171"
            );

            return;

        }


        if (isUpdatingEquipment) {
            return;
        }


        const id = Number(updateEquipmentId.value);

        const name = updateEquipmentName.value.trim();

        const category = updateCategory.value.trim();

        const total = Number(updateTotalQuantity.value);

        const available = Number(
            updateAvailableQuantity.value
        );

        const condition = updateCondition.value.trim();


        // FRONTEND VALIDATION

        if (
            !updateEquipmentId.value ||
            !name ||
            !category ||
            !updateTotalQuantity.value ||
            !updateAvailableQuantity.value ||
            !condition
        ) {

            showEquipmentMessage(
                "Please fill all required update fields.",
                "#F87171"
            );

            return;

        }


        if (
            !Number.isInteger(id) ||
            id <= 0 ||
            !Number.isInteger(total) ||
            total <= 0 ||
            !Number.isInteger(available) ||
            available < 0
        ) {

            showEquipmentMessage(
                "Please enter valid equipment ID and quantity values.",
                "#F87171"
            );

            return;

        }


        // CHECK THAT THE ID EXISTS IN CURRENT INVENTORY

        const existingItem = equipmentList.find(
            item =>
                Number(item.equipmentId) === id
        );


        if (!existingItem) {

            showEquipmentMessage(
                "The equipment ID does not exist in the current inventory.",
                "#F87171"
            );

            return;

        }


        isUpdatingEquipment = true;

        updateButton.textContent = "Updating Equipment...";

        updateButtonStates();


        try {

            // PUT REQUEST TO JAVA BACKEND
            // Send all fields expected by Equipment model.

            const response = await fetch(
                `${API_BASE_URL}/equipment`,
                {
                    method: "PUT",

                    headers: {

                        "Content-Type": "application/json",

                        "Authorization": `Bearer ${token}`

                    },

                    body: JSON.stringify({

                        equipmentId: id,

                        name: name,

                        category: category,

                        totalQuantity: total,

                        availableQuantity: available,

                        conditionStatus: condition

                    })

                }
            );


            const data = await response.json();


            if (!response.ok || data.status !== "success") {

                throw new Error(
                    data.message || "Equipment update failed."
                );

            }


            // REFRESH ACTUAL INVENTORY

            const refreshed = await loadEquipment();


            if (!refreshed) {

                showEquipmentMessage(
                    "Equipment was updated successfully, but the inventory refresh failed. Refresh the page to reload it.",
                    "#4ADE80"
                );

                return;

            }


            // CLEAR UPDATE FORM

            updateForm.reset();


            showEquipmentMessage(
                data.message || "Equipment updated successfully.",
                "#4ADE80"
            );


            console.log(
                "Equipment updated:",
                {
                    equipmentId: id,
                    response: data
                }
            );


        } catch (error) {

            showEquipmentMessage(
                error.message ||
                "Unable to update equipment.",
                "#F87171"
            );

            console.error(
                "Update equipment API error:",
                error
            );

        } finally {

            isUpdatingEquipment = false;

            updateButton.textContent = "Update Equipment";

            updateButtonStates();

        }

    }
);


// ==========================================
// LOAD EQUIPMENT DETAILS WHEN ID CHANGES
// ==========================================

updateEquipmentId.addEventListener(
    "change",
    loadExistingEquipmentDetails
);


// ==========================================
// INITIALIZE ADMIN EQUIPMENT PAGE
// ==========================================

loadAdminInformation();

updateButtonStates();

loadEquipment();
