# 🕵️‍♂️ CallInspector
[![Android CI](https://github.com/paulmathew/CallInspector/actions/workflows/android.yml/badge.svg)](https://github.com/paulmathew/CallInspector/actions/workflows/android.yml)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-Dependency%20Injection-orange?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Production%20Ready-success?style=for-the-badge)

**CallInspector** is a modular, production-grade diagnostic engine designed to validate Android device hardware and network integrity before critical video calls.

It goes beyond simple API checks by implementing **low-level socket probes**, **automated hardware analysis**, and **mathematical scoring models** to grade device health.

---

## 🚀 Key Features

### 📡 Network Diagnostic Engine
Instead of relying on simple HTTP checks, CallInspector implements a custom networking stack:
* **TCP Socket Probes:** Measures raw TCP handshake latency to bypass ICMP restrictions.
* **Jitter Calculation:** Computes variance between packet arrivals in real-time.
* **Throughput Testing:** Streams data from a dedicated CDN to measure true bandwidth.
* **Packet Loss Estimation:** Detects dropped socket connections.

### 📸 Automated Sensor Validation
* **CameraX ImageAnalysis:** Uses background threads to analyze pixel luminosity buffers (`YUV_420_888`) to mathematically verify if the lens is functioning or covered.
* **Microphone Amplitude:** Visualizes raw audio input levels using `MediaRecorder`.

### 📦 Dynamic Feature Module
Demonstrates advanced modularization capabilities:
* **Service Status Dashboard:** A completely separate module (`:dynamic_status_module`) delivered via **Play Feature Delivery**.
* **Architecture:** Implements the **"Logic in Base, UI in Dynamic"** pattern to solve Hilt dependency graph constraints while keeping the APK size optimized.

### 📊 Health Scoring Algorithm
* Aggregates 15+ metrics into a weighted score (0-100).
* Generates a final "Device Grade" (A+ to F).
* Logic verified by **Unit Tests** using Mockk.

---

## 🛠 Tech Stack

| Category | Library / Technology | Usage |
| :--- | :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) | 100% Codebase |
| **UI** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | Material3 Design System |
| **Architecture** | **MVI / Clean Architecture** | Unidirectional Data Flow |
| **Injection** | [Hilt](https://dagger.dev/hilt/) | Dependency Injection |
| **Concurrency** | [Coroutines + Flow](https://kotlinlang.org/docs/coroutines-overview.html) | Reactive Data Streams |
| **Networking** | [Retrofit](https://square.github.io/retrofit/) + **Java Sockets** | API & Latency Probes |
| **Hardware** | [CameraX](https://developer.android.com/training/camerax) | Hardware Abstraction |
| **Testing** | [Mockk](https://mockk.io/) + [Turbine](https://github.com/cashapp/turbine) | Unit Testing Flows & Logic |
| **Persistence** | [Room](https://developer.android.com/training/data-storage/room) | Local SQLite Database |

---
## 🏗 Architecture & Modularization

The project follows a scalable **MVVM + Clean Architecture** pattern, designed to separate concerns and ensure testability.

### Module Structure
The app is modularized by feature to support **Dynamic Delivery** and **Strict Dependency Boundaries**.

```text
:app (Base Module)
 ├── core/              # Shared Logic (Network, Dispatchers, Utils)
 ├── diagnostics/       # Feature: Hardware Tests (Mic, Cam, Socket Probe)
 ├── navigation/        # App Navigation Graph
 └── status/            # Feature Logic: Service Status (ViewModel/Repo)

:dynamic-status-module (Dynamic Feature)
 └── presentation/      # UI: Service Status Screen (Compose)
     └── Loaded via Reflection at runtime to reduce initial APK size.
```
---
### 📜 Audit History & Persistence
* **Local Database (Room):** Automatically persists every diagnostic run for compliance and historical comparison.
* **Structured Reporting:** Saves granular details (latency, jitter, sensor pass/fail status) alongside the final grade.
* **Offline Access:** Review past audit results without network connectivity.

---
## 🏗 Architecture Overview

The app follows strict **Clean Architecture** principles to ensure testability and separation of concerns.

```mermaid
graph TD
    subgraph "Base Module (:app)"
        UI["Presentation Layer<br>(ViewModels + Compose)"]
        Domain["Domain Layer<br>(UseCases + Interfaces)"]
        Data["Data Layer<br>(Repositories + Implementations)"]
    end

    subgraph "Dynamic Feature (:dynamic_status_module)"
        DFM["Dynamic UI<br>(Status Dashboard)"]
    end

    %% Architecture Flow
    UI --> Domain
    Domain --> Data
    
    %% Dynamic Feature Logic (Logic in Base, UI in Dynamic)
    DFM -->|Depends on| UI
    UI -.->|Loads via Reflection| DFM

    %% Data Sources
    Data --> Remote["Remote Data<br>(TCP Sockets / Retrofit)"]
    Data --> Hardware["Device Hardware<br>(CameraX / Mic)"]
    Data --> Local["Persistence<br>(Room Database)"]
```
---
## 📸 Screenshots

| **Dashboard** | **Diagnostics** |
|:---:|:---:|
| <img src="docs/screenshots/dashboard.png" width="300" /> | <img src="docs/screenshots/network_test.png" width="300" /> |
| *Status Check & Features* | *Real-time Socket Analysis* |

| **Device Capabilities** | **Service Status (DFM)** |
|:---:|:---:|
| <img src="docs/screenshots/device_specs.png" width="300" /> | <img src="docs/screenshots/service_status.png" width="300" /> |
| *Hardware Audit* | *Dynamic Feature Module* |

| **Final Report** |
|:---:|
| <img src="docs/screenshots/final_grade.png" width="300" /> |
| *Automated Scoring Engine* |

---


