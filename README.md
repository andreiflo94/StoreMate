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

## 🏗 Demo
---

<img src="https://github.com/user-attachments/assets/5dc32317-06ce-4590-8bab-2f4bcc2ea868" width="300"/>
<img src="https://github.com/user-attachments/assets/b60cffd4-c1dc-4e36-99fc-adc8bf6fa89d" width="300"/>
<img src="https://github.com/user-attachments/assets/5643de4c-2c8e-4ac7-b214-f24092edf066" width="300"/>
<img src="https://github.com/user-attachments/assets/4b230745-358c-4cd1-ba59-f0c136491a74" width="300"/>
<img src="https://github.com/user-attachments/assets/e16a051d-32a8-40d1-9d90-bce79ce75a95" width="300"/>
<img src="https://github.com/user-attachments/assets/75e216c4-30be-454b-a0fb-412e3efe7dfe" width="300"/>
<img src="https://github.com/user-attachments/assets/4ba6f2fd-3cb6-4f07-961d-882e92cfbaa4" width="300"/>
<img src="https://github.com/user-attachments/assets/edfb81d5-c2ac-45d5-a3fc-a3d2e1c7ce29" width="300"/>
<img src="https://github.com/user-attachments/assets/3f8dd94e-f957-4118-ac9b-1d140e273bfd" width="300"/>
<img src="https://github.com/user-attachments/assets/8c89ea35-a4bd-43db-9e25-85b15d84a96c" width="300"/>
<img src="https://github.com/user-attachments/assets/a5e00a28-5243-43c8-84e5-d92213054e4f" width="300"/>
<img src="https://github.com/user-attachments/assets/4d98fd22-0af5-4602-93c5-7c4f722ffd3a" width="300"/>
<img src="https://github.com/user-attachments/assets/d10df6a8-e522-4ef9-9742-b6d0b5fe602d" width="300"/>


## 🤝 Contributing

Contributions, suggestions, or bug reports are welcome!  
Feel free to open an **issue** or submit a **pull request**.

---
