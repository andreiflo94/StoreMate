# 📦 StoreMate

**StoreMate** is an Android application built to streamline inventory and transaction management for small businesses or personal use. It provides an intuitive UI for managing products, suppliers, and transactions — with powerful filtering, barcode scanning, and a real-time dashboard that keeps you informed about low stock levels and recent activity.

---

## 🔑 Key Features

- Full **CRUD operations** for Products and Suppliers  
- **Dashboard** displaying:
  - Products with **low stock**
  - **Recent transactions**
- Support for both **Purchase (Restock)** and **Sale** transactions  
- **Barcode scanning** to add products quickly without manual barcode entry  
- Advanced **filtering options** for products, suppliers, and transactions  

---

## 🔍 Filtering Capabilities

- **Products** can be filtered by:
  - **Search query** (by name or description)
  - **Category**
  - **Supplier**
- **Suppliers** can be filtered by:
  - **Search query**
- **Transactions** can be filtered by:
  - **Transaction type** (Restock or Sale)
  - **Sort order** (ascending or descending by date)

---

## 🏗 Architecture & Technologies

- **MVI (Model-View-Intent)** architecture pattern  
- Modular structure following **Clean Architecture** (Presentation, Domain, Data layers)  
- Local persistence using **Room** database  
- **Koin** for Dependency Injection  
- Unit & integration testing with:
  - **Mockk** – mocking dependencies  
  - **Turbine** – testing Kotlin Flow emissions  
  - **Robolectric** – UI testing without emulator/device  

---

## 🧪 Testing

- **ViewModel** and **Repository** unit tests using **Mockk**  
- **Flow-based logic** verified with **Turbine**  
- **UI and behavior** tested using **Robolectric** and **Jetpack Compose testing** tools  

---

## 📋 Feature Overview

| Feature             | Description                                             |
|---------------------|---------------------------------------------------------|
| Product Management  | Add, edit, delete, and filter products                  |
| Supplier Management | Full CRUD and search functionality                      |
| Transactions        | Create sales and restocks, filter and sort by type/date |
| Dashboard           | Shows low-stock products and recent transaction list    |
| Barcode Scanner     | Quickly add products by scanning barcodes               |

---

## 🤝 Contributing

Contributions, suggestions, or bug reports are welcome!  
Feel free to open an **issue** or submit a **pull request**.

---
