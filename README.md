<a id="readme-top"></a>

<!-- BADGES -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]

<br />
<p align="center">
  <img src="papa 1.png" alt="BudgetBuddy Logo" width="160" height="160"/>
  <h1 align="center">BudgetBuddy</h1>
  <p align="center">
    Mobile Budget Tracking Application
    <br />
    <a href="https://github.com/st10251759/Prog7313_POE_Part_2"><strong>View Repository »</strong></a>
<br><br>
<a href="https://youtu.be/COrigaYn4M8" target="_blank">
  <img src="https://img.shields.io/badge/Watch%20on-YouTube-red?style=for-the-badge&logo=youtube&logoColor=white" alt="Watch video demo on YouTube">
</a>
  </p>
</p>

## Table of Contents

- [Team Details](#team-details)
- [App Overview](#app-overview)
- [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation (Installing and Running the APK)](#installation-installing-and-running-the-apk)
    - [Method 1: Using Android Studio Emulator](#method-1-using-android-studio-emulator)
    - [Method 2: Using Command Line](#method-2-using-command-line)
      - [For Windows](#for-windows)
      - [For macOS/Linux](#for-macoslinux)
    - [Troubleshooting](#troubleshooting)
- [Repository Contents](#repository-contents)
- [Sprints](#sprints)
  - [Sprint Part 1: Research and Planning](#sprint-part-1-research-and-planning)
  - [Sprint Part 2: Prototype Development](#sprint-part-2-prototype-development)
  - [Sprint Part 3: Final Application Build](#sprint-part-3-final-application-build)
- [Cloning the Project and Restoring the Database](#cloning-the-project-and-restoring-the-database)
  - [Prerequisites](#prerequisites-1)
  - [Cloning Steps](#cloning-steps)
- [Lecturer Feedback](#lecturer-feedback)
- [Functionality and App Usage](#functionality-and-app-usage)
  - [User Registration and Authentication](#user-registration-and-authentication)
  - [Expense Management](#expense-management)
  - [Reporting and Insights](#reporting-and-insights)
- [Future Requirements](#future-requirements)
- [Contributors](#contributors)

---

## Team Details

- **Student Numbers:** ST10251759 | ST10252746 | ST10266994
- **Student Names:** Cameron Chetty | Theshara Narain | Alyssia Sookdeo
- **Course:** BCAD Year 3
- **Module:** Programming 3C (PROG7313)
- **Assessment:** Portfolio of Evidence (POE) Part 2
- **GitHub Repository:** [https://github.com/st10251759/Prog7313_POE_Part_2](https://github.com/st10251759/Prog7313_POE_Part_2)

---

## App Overview

BudgetBuddy is a mobile application that simplifies personal finance management through expense tracking, budgeting, and financial goal management. It features a clean, approachable interface with a mint green colour scheme, allowing users to create customized expense categories, set monthly budget limits, and log transactions. The homepage provides a quick overview of budget status, spending trends, and category-wise expenditure breakdowns.

Users can track spending patterns through visualizations and earn badges for consistent expense logging. The app also incorporates gamification elements, making financial discipline more engaging. Users can customize categories with images or icons for visual impact. The Budget Assistant chatbot offers financial guidance and assistance. The app also offers a smart transaction search, detailed expense listings, and savings tracking feature.

---

## Built With

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?logo=android-studio&logoColor=white&style=for-the-badge" alt="Android Studio"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black&style=for-the-badge" alt="Firebase"/>
  <img src="https://img.shields.io/badge/Room-307CF7?logo=sqlite&logoColor=white&style=for-the-badge" alt="RoomDB"/>
</p>

- **Kotlin**
- **Android Studio (v32)**
- **Firebase Authentication**
- **RoomDB** (local database)

---

## Getting Started

This is an example of how you may give instructions on setting up your project locally. To get a local copy up and running follow these simple example steps.

### Prerequisites

- Android Studio (latest version)
- Sufficient disk space
- Stable internet connection
- Java Development Kit (JDK)
- Git

---

### Installation (Installing and Running the APK)

#### Method 1: Using Android Studio Emulator

1. **Download the APK**
    - Navigate to the project's GitHub repository
    - Locate the APK file in the releases or builds section
    - Download the APK to your computer

2. **Open Android Studio**
    - Launch Android Studio
    - Open the Virtual Device Manager via "Tools" > "Device Manager"
    - If no emulator exists, click "Create Device"

3. **Create an Emulator (If Needed)**
    - Select a device definition (e.g., Pixel 4)
    - Choose a system image (recommended: latest stable Android version)
    - Name your virtual device
    - Click "Finish" to create the emulator

4. **Install the APK**
    - Start the emulator
    - Drag and drop the downloaded APK file onto the emulator screen
    - If prompted, click "Install"
    - Click "Open" to launch the app

#### Method 2: Using Command Line

##### For Windows

```sh
cd C:\Users\YourUsername\AppData\Local\Android\Sdk\platform-tools
adb install "path\to\your\app-release.apk"

```
##### For macOS/Linux

```sh
cd ~/Library/Android/sdk/platform-tools
./adb install /path/to/app-release.apk
```

#### Troubleshooting

- Ensure Android Studio SDK is up to date
- Check emulator settings and system requirements
- Verify APK compatibility with emulator's Android version

---

## Repository Contents

- Complete Android Studio project source code
- README file (current document)
- Kotlin source files
- Resource files
- Configuration files
- GitHub Actions workflow with automated tests for continuous integration
- Build APK for the app

---

## Sprints

### Sprint Part 1: Research and Planning
- Conduct comprehensive research on existing budgeting applications
- Develop initial project design
- Create detailed project documentation
- Establish project requirements and scope

### Sprint Part 2: Prototype Development
- User authentication system
- Expense category management
- Expense entry creation
- Receipt photo attachment
- Budget goal setting
- Basic expense tracking and reporting

### Sprint Part 3: Final Application Build
- Graphical spending insights
- Progress dashboard
- Cloud data synchronization
- Improved user interface
- Performance optimizations

---

## Cloning the Project and Restoring the Database

### Prerequisites
- Android Studio (latest version)
- Git
- Java Development Kit (JDK)

### Cloning Steps

1. Open terminal or command prompt  
2. Navigate to your desired directory  
3. Run:
   ```sh
   git clone https://github.com/st10251759/Prog7313_POE_Part_2.git
   ```
4. Open the project in Android Studio  
5. Sync Gradle files  
6. Build and run the project  

---

## Lecturer Feedback

No specific actionable feedback was received for Part 1 of the project.  
The team achieved a 99% score in the previous submission.

---

## Functionality and App Usage

### User Registration and Authentication
- Create a new account
- Log in securely
- Manage user profile

### Expense Management
- Create and customize expense categories
- Add detailed expense entries
- Attach receipt photos
- Set monthly budget goals

### Reporting and Insights
- View expense lists by date range
- Analyze spending by category
- Track progress against budget goals

---

## Future Requirements

- Advanced data visualization
- Multi-currency support
- Spending Predictions
- Enhanced reporting capabilities
- Social sharing of financial insights

---

## Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/st10251759">
        <img src="https://avatars.githubusercontent.com/u/101059675?s=400&v=4" width="100px;" alt="Cameron Chetty"/>
        <br />
        <sub><b>Cameron Chetty</b></sub>
      </a>
      <br/>
      <a href="mailto:ST10251759@vcconnect.edu.za">ST10251759@vcconnect.edu.za</a>
    </td>
    <td align="center">
      <a href="https://github.com/ST10252746">
        <img src="https://avatars.githubusercontent.com/u/158014693?s=400&v=4" width="100px;" alt="Theshara Narain"/>
        <br />
        <sub><b>Theshara Narain</b></sub>
      </a>
      <br/>
      <a href="mailto:ST10252746@vcconnect.edu.za">ST10252746@vcconnect.edu.za</a>
    </td>
    <td align="center">
      <a href="https://github.com/ST10266994">
        <img src="https://avatars.githubusercontent.com/u/158015110?s=400&v=4" width="100px;" alt="Alyssia Sookdeo"/>
        <br />
        <sub><b>Alyssia Sookdeo</b></sub>
      </a>
      <br/>
      <a href="mailto:ST10266994@vcconnect.edu.za">ST10266994@vcconnect.edu.za</a>
    </td>
  </tr>
</table>

---

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/st10251759/Prog7313_POE_Part_2.svg?style=for-the-badge
[contributors-url]: https://github.com/st10251759/Prog7313_POE_Part_2/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/st10251759/Prog7313_POE_Part_2.svg?style=for-the-badge
[forks-url]: https://github.com/st10251759/Prog7313_POE_Part_2/network/members
[stars-shield]: https://img.shields.io/github/stars/st10251759/Prog7313_POE_Part_2.svg?style=for-the-badge
[stars-url]: https://github.com/st10251759/Prog7313_POE_Part_2/stargazers
[issues-shield]: https://img.shields.io/github/issues/st10251759/Prog7313_POE_Part_2.svg?style=for-the-badge
[issues-url]: https://github.com/st10251759/Prog7313_POE_Part_2/issues
