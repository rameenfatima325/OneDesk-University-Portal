# OneDesk

OneDesk is a JavaFX-based desktop application designed to centralize and simplify university student service requests. The system allows students to submit, track, and manage different academic, document, and support-related requests through a single platform.

The project follows an organized layered architecture with separate business logic, database access, and UI modules. It also demonstrates the use of multiple Object-Oriented Programming principles and design patterns.

--

**Key Problem Addressed:**
Universities handle numerous daily academic and administrative requests through inefficient methods, causing delays, miscommunication, lack of transparency, and increased workload for administrative staff. OneDesk provides a structured, digital solution.

## Features

### Core Functionality

#### Academic Services
- **Attendance Correction** - Submit and track attendance correction requests
- **Course Registration** - Report enrollment and registration issues
- **Add/Drop/Withdrawal** - Request course modifications with deadline protection
- **Academic Record Correction** - Fix GPA, results, timetable, and section errors
- **Enrollment Verification** - Request official enrollment letters

#### Certification & Documentation
- **Transcript Issuance** - Request official transcripts with official seal
- **Degree Verification** - Verify and attest degrees for employment/further studies
- **Student ID Cards** - Issue or replace student ID cards with payment processing
- **Verification Letters** - Request official verification letters for banks, visas, employment

#### Financial & Administrative Services
- **Fee Support Requests** - Manage voucher issues and payment verification
- **Financial Aid Queries** - Submit scholarship and financial aid questions
- **Lost & Found** - Report lost items or claim found items with image uploads
- **General Complaints** - Submit formal complaints with severity levels and optional anonymity

### Advanced Features
- **Real-time Request Tracking** - Students can monitor request status with detailed updates
- **Request History** - View complete history of all submissions
- **Audit Logging** - Administrative audit trail for all actions and status changes
- **Role-Based Access Control** - Separate interfaces for students and administrators
- **File Upload Support** - Secure document attachment (PDF, JPG, PNG up to 5MB)
- **Dark Mode UI** - Professional dark theme reducing eye strain
- **Form Validation** - Real-time validation with user-friendly error messages

## System Requirements

### Client Requirements
- **Operating System:** Windows 10/11, macOS, or Linux
- **Java Runtime:** JRE 11 or higher
- **Disk Space:** 100MB minimum
- **RAM:** 2GB minimum

### Server Requirements
- **Database:** SQL Server, MySQL, or Oracle
- **Hosting:** University local intranet or secure cloud server
- **Network:** Standard TCP/IP connectivity

## Installation

### Prerequisites
Ensure you have Java 11+ installed on your system:
```bash
java -version
```

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/rameenfatima/OneDesk-Universty-Portal.git
   cd onedesk
   ```

2. **Build the project**
   ```bash
   # Using Maven
   mvn clean install
   
   # Or using Gradle
   gradle build
   ```

3. **Configure database connection**
   - Edit `config/database.properties`
   - Set your database connection details (host, port, username, password)

4. **Run the application**
   ```bash
   java -jar onedesk-1.0.jar
   ```

5. **Access the application**
   - Login window will open automatically
   - Default credentials format: `StudentID/EmployeeID` and password

## Usage

### For Students

1. **Login** with your Student ID and password
2. **Navigate** to the service category (Academic, Certification, Administrative)
3. **Select** the specific request type
4. **Fill** the request form with required information
5. **Upload** supporting documents if needed
6. **Submit** the request
7. **Track** progress in "My Request History"

### For Administrators

1. **Login** with your Employee ID and password
2. **View** all incoming requests in the dashboard
3. **Review** request details and supporting documents
4. **Approve, Reject, or Mark for Review** using action buttons
5. **Add remarks** for student communication
6. **Access** audit log for accountability and tracking

## Architecture

### Technology Stack
- **Frontend:** Java Swing/JavaFX (Desktop GUI)
- **Backend:** Java (Server-side logic)
- **Database:** SQL/Oracle (Persistent storage)
- **Design Pattern:** Object-Oriented Design with strict modularity

### Key Architectural Components

```
┌─────────────────────────────────────────┐
│        Desktop Client (Swing/JavaFX)    │
│    ├─ Student Dashboard                 │
│    ├─ Admin Portal                      │
│    └─ Request Forms                     │
└────────────┬────────────────────────────┘
             │ JDBC
┌────────────▼────────────────────────────┐
│      Application Logic Layer            │
│    ├─ Request Management                │
│    ├─ Validation Engine                 │
│    ├─ File Handling                     │
│    └─ Audit Logging                     │
└────────────┬────────────────────────────┘
             │ Database Driver
┌────────────▼────────────────────────────┐
│      Database Layer (SQL/Oracle)        │
│    ├─ Student Records                   │
│    ├─ Request Storage                   │
│    ├─ Audit Trail                       │
│    └─ Academic Data                     │
└─────────────────────────────────────────┘
```

## Project Structure

```
src/
│
├── business/
│   ├── academic/
│   ├── admin/
│   ├── document/
│   ├── support/
│   └── shared/
│
├── db/
│   ├── academic/
│   ├── document/
│   └── support/
│
├── database/
│   └── SQL setup scripts
│
├── ui/
│   ├── academic/
│   ├── document/
│   ├── support/
│   └── common/
│
└── Main.java
```

## Use Cases

The system implements 12 primary use cases:

| # | Use Case | Actor | Module |
|---|----------|-------|--------|
| 1 | Submit Attendance Correction Request | Student | Academic Services |
| 2 | Submit Course Registration/Enrollment Issues | Student | Academic Services |
| 3 | Submit Add/Drop/Withdrawal Request | Student | Academic Services |
| 4 | Submit Academic Record Correction Request | Student | Academic Services |
| 5 | Manage Fee Support Request | Student | Financial Services |
| 6 | Manage Lost and Found Request | Student | Campus Services |
| 7 | Submit Scholarship/Financial Aid Query | Student | Financial Services |
| 8 | Submit General Academic/Administrative Complaint | Student | Administrative Services |
| 9 | Request Transcript Issuance | Student | Certification & Documentation |
| 10 | Request Degree Verification/Attestation | Student | Certification & Documentation |
| 11 | Request Enrollment/Verification Letter | Student | Certification & Documentation |
| 12 | Request Student ID Card Issuance/Replacement | Student | Certification & Documentation |

## Security

### Authentication & Authorization
- University credential-based login (Student ID/Employee ID + Password)
- Role-Based Access Control (RBAC)
- Students can only view their own requests
- Admins can view and manage all requests

### Data Protection
- Passwords securely hashed in database
- Parameterized queries (JDBC) prevent SQL injection attacks
- Secure file upload validation (type and size checks)
- Compliance with university data privacy policies
- Sensitive document handling (transcripts, vouchers, IDs)

### Audit & Compliance
- Complete audit trail of all administrative actions
- Timestamp logging for request submissions
- Automatic deadline protection (prevents data loss)
- Transaction rollback on system failures

## Performance

### Response Times
- **UI Navigation:** <2 seconds
- **Database Queries:** <3 seconds
- **File Operations:** 5-10 seconds (documents up to 5MB)

### Concurrency
- Supports multiple concurrent users efficiently
- Optimized for peak university periods (registration week, grading periods)
- Connection pooling for database access

### Scalability
- Modular object-oriented design allows easy feature additions
- Database scaling through proper indexing
- Future support for API layer and mobile applications

## Business Rules

- Only "Active" or "Enrolled" students can submit standard requests
- Graduated/Alumni students can only submit certification requests
- Students with "Financial Hold" cannot request transcripts or degree verification
- Add/Drop requests after deadline are flagged for administrative review
- All requests logged with exact timestamp for deadline protection
- Anonymous complaint submission supported with identity hidden before storage

## OOP Concepts Used
The project demonstrates multiple Object-Oriented Programming concepts:

Encapsulation
Inheritance
Polymorphism
Abstraction
Composition

## Design Patterns Implemented
Several software design patterns are used throughout the project:

Factory Pattern: Used for dynamic request creation.

Observer Pattern: Used for notifications and status updates.

Strategy Pattern: Used for eligibility validation and request processing.

Decorator Pattern: Used for anonymous complaint functionality.

MVC Architecture: The application follows a Model-View-Controller inspired structure:

## Database
The project includes SQL setup and migration scripts:

One Desk Setup.sql
Migration_Holds_AndStatuses.sql
These scripts create the required database schema and tables for the system.

## Contributors

- **Rameen Fatima** 
- **Zahra Arshad** 
- **Aizah Atif** 
--

## 📄 Documentation

- **SRS (Software Requirements Specification):** See `OneDeskReport_docx.pdf`
- **Class Diagram:** System design with 30+ classes
- **Sequence Diagrams:** 12 detailed use case flows
- **Domain Model:** Complete entity relationships
