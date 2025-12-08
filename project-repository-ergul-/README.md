# Bahar Kiraathanesi - Cafe Management System

<p align="center">
  <img src="src/main/resources/images/cay_icon.png" alt="Bahar Kiraathanesi Logo" width="120"/>
</p>

<p align="center">
  <strong>Modern Point-of-Sale System for Cafes and Tea Houses</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21.0.6-007396?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Active-success?style=flat-square" alt="Status"/>
  <img src="https://img.shields.io/badge/License-Proprietary-blue?style=flat-square" alt="License"/>
  <img src="https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey?style=flat-square" alt="Platform"/>
</p>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Overview

**Bahar Kiraathanesi** is a comprehensive desktop application designed for cafes, tea houses, and similar hospitality businesses. It provides a complete point-of-sale (POS) solution with real-time table management, inventory tracking, and detailed reporting.

```
+------------------+     +------------------+     +------------------+
|                  |     |                  |     |                  |
|   Login Screen   | --> |    Main Menu     | --> |   Operations     |
|                  |     |                  |     |                  |
+------------------+     +------------------+     +------------------+
                                  |
                 +----------------+----------------+
                 |                |                |
                 v                v                v
          +-----------+    +-----------+    +-----------+
          |   Stock   |    |  Tables   |    |  Reports  |
          | Management|    |  & Orders |    | & Z-Report|
          +-----------+    +-----------+    +-----------+
```

---

## Features

<table>
<tr>
<td width="50%">

### Core Modules

| Module | Description |
|:-------|:------------|
| **Authentication** | Secure login with role-based access |
| **Table Management** | Real-time table status tracking |
| **Order Processing** | Quick order entry with auto-updates |
| **Stock Control** | Package-based inventory system |
| **Reporting** | Visual charts and analytics |
| **Z-Report** | End-of-day PDF generation |

</td>
<td width="50%">

### Key Highlights

- Real-time table occupancy visualization
- Automatic stock deduction on orders
- Low-stock alerts and notifications
- Weekly and monthly revenue charts
- PDF export for daily reports
- Multi-platform support

</td>
</tr>
</table>

---

## Screenshots

<table>
<tr>
<td align="center" width="50%">
<strong>Login Screen</strong><br/>
Secure authentication system
</td>
<td align="center" width="50%">
<strong>Main Menu</strong><br/>
Central navigation hub
</td>
</tr>
<tr>
<td align="center" width="50%">
<strong>Table Management</strong><br/>
Visual table status (Available/Occupied)
</td>
<td align="center" width="50%">
<strong>Order Screen</strong><br/>
Quick product selection and order management
</td>
</tr>
<tr>
<td align="center" width="50%">
<strong>Stock Control</strong><br/>
Inventory management with package tracking
</td>
<td align="center" width="50%">
<strong>Reports</strong><br/>
Daily, weekly, and monthly analytics
</td>
</tr>
</table>

---

## Technology Stack

<table>
<tr>
<th>Category</th>
<th>Technology</th>
<th>Version</th>
<th>Purpose</th>
</tr>
<tr>
<td><img src="https://img.shields.io/badge/-Language-orange?style=flat-square"/></td>
<td>Java</td>
<td>21+</td>
<td>Core programming language</td>
</tr>
<tr>
<td><img src="https://img.shields.io/badge/-UI-blue?style=flat-square"/></td>
<td>JavaFX</td>
<td>21.0.6</td>
<td>Desktop UI framework</td>
</tr>
<tr>
<td><img src="https://img.shields.io/badge/-Database-green?style=flat-square"/></td>
<td>MySQL</td>
<td>8.0+</td>
<td>Data persistence</td>
</tr>
<tr>
<td><img src="https://img.shields.io/badge/-Build-red?style=flat-square"/></td>
<td>Maven</td>
<td>3.8+</td>
<td>Dependency management</td>
</tr>
<tr>
<td><img src="https://img.shields.io/badge/-PDF-purple?style=flat-square"/></td>
<td>Apache PDFBox</td>
<td>3.x</td>
<td>PDF report generation</td>
</tr>
</table>

---

## Prerequisites

Before installation, ensure you have the following:

### Required Software

| Software | Minimum Version | Download Link |
|:---------|:----------------|:--------------|
| Java JDK | 21 | [Adoptium](https://adoptium.net/) |
| MySQL Server | 8.0 | [MySQL Downloads](https://dev.mysql.com/downloads/) |
| Maven | 3.8 (optional) | [Apache Maven](https://maven.apache.org/download.cgi) |

### Verification Commands

```bash
# Check Java version
java -version

# Check Maven version (optional)
mvn -version

# Check MySQL status
mysql --version
```

---

## Installation

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd project-repository-ergul-
```

### Step 2: Database Setup

Connect to MySQL and run the setup script:

```bash
# Option 1: Using MySQL CLI
mysql -u root -p < src/main/resources/db/setup_database.sql

# Option 2: Using MySQL Workbench
# Open and execute: src/main/resources/db/setup_database.sql
```

<details>
<summary><strong>What does the setup script create?</strong></summary>

- Database: `bahar_db`
- Tables: `users`, `tables`, `products`, `orders`, `order_items`
- Default admin user: `admin` / `admin`
- Sample product data

</details>

### Step 3: Configure Database Connection

```bash
# Copy the example configuration
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Edit `db.properties` with your credentials:

```properties
db.host=localhost
db.port=3306
db.name=bahar_db
db.user=root
db.password=YOUR_PASSWORD
```

> **Security Note:** The `db.properties` file is excluded from version control via `.gitignore`

### Step 4: Build the Project

```bash
# macOS / Linux
./mvnw clean install

# Windows
mvnw.cmd clean install
```

---

## Configuration

### Database Settings

The application supports two configuration methods:

<table>
<tr>
<th>Method</th>
<th>Priority</th>
<th>Use Case</th>
</tr>
<tr>
<td><code>db.properties</code> file</td>
<td>Primary</td>
<td>Development and local deployment</td>
</tr>
<tr>
<td>Environment Variables</td>
<td>Fallback</td>
<td>Production and containerized deployment</td>
</tr>
</table>

### Environment Variables

| Variable | Default Value |
|:---------|:--------------|
| `DB_HOST` | localhost |
| `DB_PORT` | 3306 |
| `DB_NAME` | bahar_db |
| `DB_USER` | root |
| `DB_PASSWORD` | (empty) |

---

## Running the Application

### From IDE

1. Import as Maven project
2. Run the `Launcher` class:
   ```
   src/main/java/com/baharkiraathanesi/kiraathane/Launcher.java
   ```

### From Command Line

```bash
# Using Maven wrapper
./mvnw javafx:run

# Using built JAR
java -jar target/Kiraathane-1.0-SNAPSHOT.jar
```

### Default Credentials

| Username | Password | Role |
|:---------|:---------|:-----|
| `admin` | `admin` | Administrator |

---

## Project Structure

```
project-repository-ergul-/
│
├── src/
│   └── main/
│       ├── java/com/baharkiraathanesi/kiraathane/
│       │   ├── dao/                    # Data Access Objects
│       │   │   ├── OrderDAO.java
│       │   │   ├── ProductDAO.java
│       │   │   ├── ReportDAO.java
│       │   │   ├── TableDAO.java
│       │   │   └── UserDAO.java
│       │   │
│       │   ├── database/               # Database utilities
│       │   │   ├── DatabaseConnection.java
│       │   │   └── DatabaseUpdater.java
│       │   │
│       │   ├── model/                  # Entity classes
│       │   │   ├── Order.java
│       │   │   ├── OrderItem.java
│       │   │   ├── Product.java
│       │   │   ├── Report.java
│       │   │   └── Table.java
│       │   │
│       │   └── *Controller.java        # JavaFX controllers
│       │
│       └── resources/
│           ├── db/                     # SQL scripts
│           ├── images/                 # Icons and images
│           └── com/.../                # FXML views and CSS
│
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

---

## Troubleshooting

<details>
<summary><strong>Database Connection Failed</strong></summary>

1. Verify MySQL is running:
   ```bash
   mysql -u root -p
   ```
2. Check credentials in `db.properties`
3. Ensure database exists:
   ```sql
   SHOW DATABASES;
   ```
4. Check network connectivity to database host

</details>

<details>
<summary><strong>Application Won't Start</strong></summary>

1. Verify Java version (must be 21+):
   ```bash
   java -version
   ```
2. Download dependencies:
   ```bash
   ./mvnw dependency:resolve
   ```
3. Check console for specific error messages

</details>

<details>
<summary><strong>Missing Fonts in PDF</strong></summary>

The Z-Report uses Arial fonts by default. If unavailable, it falls back to Helvetica.

For full Turkish character support, ensure Arial fonts are installed:
- **Windows:** Usually pre-installed
- **macOS:** Usually pre-installed
- **Linux:** Install `ttf-mscorefonts-installer`

</details>

<details>
<summary><strong>JavaFX Module Errors</strong></summary>

Ensure `module-info.java` is properly configured and JavaFX dependencies are in `pom.xml`.

```bash
./mvnw clean install -U
```

</details>

---

## License

This project is proprietary software. All rights reserved.

---

<p align="center">
  <strong>Bahar Kiraathanesi</strong><br/>
  <sub>Built with Java and JavaFX</sub>
</p>

<p align="center">
  <sub>For support, refer to <code>CREDENTIALS_GUIDE.txt</code></sub>
</p>

