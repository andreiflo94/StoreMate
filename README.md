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

## 🏗 Screenshots

![Screenshot_20250727_103341_StoreMate](https://github.com/user-attachments/assets/90060c1c-f7d9-41a4-a233-42b7764703a3)
![Screenshot_20250727_103328_StoreMate](https://github.com/user-attachments/assets/4b6b70fe-0db1-40c0-a6d6-5aab55f8f8e9)
![Screenshot_20250727_103321_StoreMate](https://github.com/user-attachments/assets/93624467-0584-40f8-a8f2-3adb8f1700ea)
![Screenshot_20250727_103313_StoreMate](https://github.com/user-attachments/assets/1e67781c-fb57-42ff-8970-70d5b7fe1b25)
![Screenshot_20250727_103307_StoreMate](https://github.com/user-attachments/assets/75933d17-03c2-4eb0-aaff-419ebc4a7c3f)
![Screenshot_20250727_103303_StoreMate](https://github.com/user-attachments/assets/d6148ed5-b9cb-4566-b1c1-6e8517ac2591)
![Screenshot_20250727_103257_StoreMate](https://github.com/user-attachments/assets/b838ee83-a29c-4c4f-804b-34fc33624e64)
![Screenshot_20250727_103254_StoreMate](https://github.com/user-attachments/assets/4515a92f-7f22-4000-8789-60929c906a6b)
![Screenshot_20250727_103247_StoreMate](https://github.com/user-attachments/assets/658db7c6-55d3-4d4c-8a29-6f779413c2fe)
![Screenshot_20250727_103244_StoreMate](https://github.com/user-attachments/assets/3e117357-61b1-4884-b186-6099cb645606)
![Screenshot_20250727_103238_StoreMate](https://github.com/user-attachments/assets/ae407430-282d-4f10-b02f-f33d9c052b9e)
![Screenshot_20250727_103234_StoreMate](https://github.com/user-attachments/assets/285ffa85-0589-42c5-a15b-5366a44712e7)
![Screenshot_20250727_103230_StoreMate](https://github.com/user-attachments/assets/35fb4e03-43a5-4448-abb2-2e5138deb1d3)

---

## 🤝 Contributing

Contributions, suggestions, or bug reports are welcome!  
Feel free to open an **issue** or submit a **pull request**.

---
