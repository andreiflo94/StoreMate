# 📦 StoreMate

**StoreMate** is an Android application built to streamline inventory and transaction management for small businesses or personal use. It provides an intuitive UI for managing products, suppliers, and transactions — with powerful filtering, barcode scanning, and a real-time dashboard that keeps you informed about low stock levels and recent activity.

---


➡️ [Download StoreMate APK](https://github.com/andreiflo94/StoreMate/raw/main/builds/app-debug.apk)

## 🔑 Key Features

- Full **CRUD operations** for Products and Suppliers  
- **Dashboard** displaying:
  - Products with **low stock**
  - **Recent transactions**
- Support for both **Purchase (Restock)** and **Sale** transactions  
- **Barcode scanning** to add products quickly without manual barcode entry  
- Advanced **filtering options** for products, suppliers, and transactions  
- **On-prem sync**: the app signs in to your shop's own server, so every device in the
  shop shares the same data, and still works read-only if the server goes offline  

---

## 🖥️ Setting Up the On-Prem Server

StoreMate is a client for its own backend, **StoreMate REST API**
(`StoreMateRestApi-master/`), which you run on a computer in your shop. Every device then
signs in to that one server.

### Option A — Docker (recommended for a real install)

```bash
cd StoreMateRestApi-master
export STOREMATE_JWT_SECRET="$(openssl rand -base64 48)"
docker compose up -d --build
```

This starts the API on port `8080` backed by PostgreSQL, persisted in a Docker volume.

### Option B — Plain Java (quick single-shop install, no Docker)

```bash
cd StoreMateRestApi-master
./gradlew bootJar
java -jar build/libs/*.jar
```

Defaults to a file-backed H2 database in `./data`, so data survives restarts without any
extra setup. Set `STOREMATE_JWT_SECRET` (32+ characters) before letting other devices reach
it — otherwise anyone who's read the source can mint valid login tokens.

On first run, the server seeds a `demo` / `demo` account you can use to try things out; set
`STOREMATE_SEED_DEMO=false` for a real shop deployment.

The server listens on `0.0.0.0:8080` by default so phones/tablets on the shop Wi-Fi can
reach it at `http://<this-computer's-LAN-IP>:8080`.

## 📱 Connecting the App to Your Server

1. Install StoreMate on each device (see the APK link above, or build it yourself with
   `./gradlew assembleDebug`).
2. On first launch, the Login screen asks for:
   - **Server address** — the computer's LAN IP or hostname, e.g. `192.168.1.10` or
     `storemate.local`. The port defaults to `8080` if you don't type one; a full URL
     (`http://...`) also works.
   - **Username / Password** — sign in with an existing account, or tap **"First time
     here? Create a store"** to register the shop's first account.
3. The app remembers the server address and stays signed in across restarts. Reads work
   even if the server is briefly unreachable (an "Offline — showing last synced data"
   banner appears); writes and the manual sync button need the server to be reachable.
4. Sign-in tokens expire after a while (12 hours by default). When that happens, the app
   automatically signs you out and returns you to the Login screen with a "Your session
   expired — please sign in again" message — just sign back in to continue.

> Testing locally on one machine: run the server as above, then either use `10.0.2.2:8080`
> from an Android emulator, or `adb reverse tcp:8080 tcp:8080` and use `localhost:8080` from
> a device plugged in over USB.

---

## 🔍 Filtering Capabilities

- **Products** can be filtered by:
  - **Search query** (by name)
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
