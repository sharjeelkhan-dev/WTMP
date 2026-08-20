### 🔒 WTMP – Who Touched My Phone (AI-Powered)
![Status](https://img.shields.io/badge/Code-181717?style=for-the-badge&logo=github&logoColor=white) ![AI](https://img.shields.io/badge/Google_Gemini-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white) ![UI](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white) ![DI](https://img.shields.io/badge/Dagger_Hilt-2C3E50?style=for-the-badge&logo=android&logoColor=white) ![Storage](https://img.shields.io/badge/Room_Database-003B57?style=for-the-badge&logo=sqlite&logoColor=white)

> High-performance Android security engine designed to detect unauthorized access attempts, capture intruder snapshots via background CameraX services, and generate AI-driven security reports.

| Subsystem | Technical Execution Architecture |
| :--- | :--- |
| 🤖 **AI Threat Intelligence** | Integrated Google Gemini via Firebase AI Logic to analyze intruder attempt logs, detecting intrusion patterns and generating real-time risk scores. |
| 📸 **Silent Capture Engine** | Asynchronous background capture pipeline powered by CameraX API and Foreground Services, snapping high-res front-camera photos on failed or successful unlock triggers. |
| 🏗️ **Clean Architecture** | Decoupled multi-layered structure engineered with MVVM, Dagger-Hilt dependency injection, and reactive Kotlin Flow/Coroutines streams. |
| 💾 **Encrypted Local Storage** | Robust local log persistence utilizing Room Database with Room-paging and encrypted file storage to safeguard intruder snapshots locally. |
| 🔔 **Intrusion Event Monitoring** | Low-overhead Broadcast Receivers and Device Admin APIs listening for screen state changes, wrong PIN/Pattern attempts, and power button interactions. |
| 🎨 **Material 3 Interface** | Modern, dark-themed UI built entirely with Jetpack Compose, featuring interactive capture timelines, detail view modals, and custom animations. |
| 🛡️ **Stealth & Protection** | Enhanced security layer featuring App-Lock protection, dynamic notification masking, and PIN protection to prevent unauthorized app termination. |

<details>
<summary><b>✨ View Interface Design (Click to Expand)</b></summary>
<br/>
<table width="100%">
 <tr>
    <td width="33.3%" align="center"><img src="https://github.com/user-attachments/assets/YOUR_IMAGE_ID_1" width="100%" alt="Screen 1 - Dashboard / Security Toggle" /></td>
    <td width="33.3%" align="center"><img src="https://github.com/user-attachments/assets/YOUR_IMAGE_ID_2" width="100%" alt="Screen 2 - Intruder Logs List" /></td>
    <td width="33.3%" align="center"><img src="https://github.com/user-attachments/assets/YOUR_IMAGE_ID_3" width="100%" alt="Screen 3 - Intrusion Log Detail Screen" /></td>
 </tr>
 
  <tr>
    <td width="33.3%" align="center"><img src="https://github.com/user-attachments/assets/YOUR_IMAGE_ID_4" width="100%" alt="Screen 4 - AI Threat Analysis" /></td>
    <td width="33.3%" align="center"><img src="https://github.com/user-attachments/assets/YOUR_IMAGE_ID_5" width="100%" alt="Screen 5 - Captured Media Gallery" /></td>
    <td width="33.3%" align="center"><img src="https://github.com/user-attachments/assets/YOUR_IMAGE_ID_6" width="100%" alt="Screen 6 - App Settings & Security" /></td>
 </tr>
 
</table>
</details>

## 🛠 Setup & Installation

### 📋 Prerequisites
* Android Studio Ladybug (or newer)
* JDK 17+
* Android SDK 28 (Android 9.0) or higher
