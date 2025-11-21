# 💼 Payment and Payroll Management System

![Spring Boot](https://img.shields.io/badge/Backend-SpringBoot-green?logo=spring)
![Angular](https://img.shields.io/badge/Frontend-Angular-red?logo=angular)
![Database](https://img.shields.io/badge/Database-MySQL-blue?logo=mysql)

---

## 🧾 Overview

The **Payment and Payroll Management System** is a full-stack web application that automates salary processing and payment workflows.  
It simplifies how organizations manage employee salaries while allowing bank administrators to approve and track payments securely.  

---

## 🏗️ Project Structure

```
Payment-and-Payroll-Management-System/
├── Frontend/   # Angular-based user interface
└── Backend/    # Spring Boot backend APIs and services
```

---

## ⚙️ Features

### 👨‍💼 Organization Admin
- Add, edit, and manage employee records  
- Generate and send salary requests to bank admin  
- View payroll history and reports  

### 🏦 Bank Admin
- Approve or reject salary requests  
- Process payments and maintain transaction logs  

### 👥 Employee
- View payslips and payment history  
- Update personal details securely  

### 🔐 Security
- JWT-based authentication  
- Role-based access control (Employee, Organization Admin, Bank Admin)  

---

## 🛠️ Tech Stack

| Category        | Technologies Used                        |
|-----------------|-----------------------------------------|
| **Frontend**    | Angular, HTML, CSS, TypeScript           |
| **Backend**     | Java, Spring Boot, REST API              |
| **Database**    | MySQL                                    |
| **Build Tools** | Maven                                    |
| **Others**      | Git, Postman, VS Code / IntelliJ        |
| **Email Service** | SMTP                                   |

---

## 🚀 How to Run

### 🧩 Backend Setup
```bash
# Navigate to backend folder
cd Backend/Payroll-System

# Open 'application.properties' and set your MySQL credentials
spring.datasource.url=jdbc:mysql://localhost:3306/payroll_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Run the Spring Boot application
mvn spring-boot:run

# Backend runs on:
http://localhost:8080
```

### 💻 Frontend Setup
```bash
# Navigate to frontend folder
cd Frontend

# Install dependencies
npm install

# Start the development server
npm start

# Frontend runs on:
http://localhost:4200
```

---

## 📂 Folder Structure Summary

| Folder        | Description                                           |
|---------------|-------------------------------------------------------|
| **Frontend/** | All UI components, services, and routing logic       |
| **Backend/**  | Controllers, models, repositories, and configurations |

---


## 🧠 Future Enhancements
- 📈 Payroll analytics dashboard  
- 💬 Chatbot integration for salary queries  

---

## 📄 License

This project is developed **for educational and learning purposes only**.  
For any commercial use, please contact the repository owner.

---

## 🌐 Repository

🔗 **View on GitHub:** [Click Here](#)
