# Lotus Student Management System 📘

The **Lotus Student Management System** is a console-based application built with C# to manage student, course, and teacher-related operations. This project demonstrates the principles of Object-Oriented Programming (OOP) such as encapsulation, inheritance, polymorphism, and abstraction, along with a layered architecture for improved code organization and scalability.

---

## Features ✨

- **Student Management**  
  - Add, update, and delete student records  
  - View student details, including GPA and assigned courses  

- **Course Management**  
  - Create, update, and remove courses  
  - Assign students to courses  
  - Manage course-specific grades  

- **Teacher Management**  
  - Add, update, and delete teacher records  
  - Assign teachers to courses  

- **Other Functionalities**  
  - GPA calculation and grade updates  
  - Modular system with separate handlers for students, courses, and teachers  
  - Validation for user input to ensure data integrity  

---

## Project Structure 🏗️

The application follows a **layered architecture** for better separation of concerns:

1. **Presentation Layer**  
   - Handles user interaction and input/output via the console interface.  
   - Provides menus for navigating the system.  

2. **Business Layer**  
   - Contains the logic for handling core operations such as student enrollment, GPA updates, and course management.  

3. **Data Layer**  
   - Manages data storage and retrieval within the application.  
   - Implements in-memory data handling (no external database).  

4. **Models**  
   - Defines the core objects of the system such as `Student`, `Teacher`, and `Course`.  
   - Implements properties and methods for each model.  

5. **Helpers**  
   - Provides utility functions for validation and input handling.  

---

## Technologies Used 🛠️

- **Programming Language:** C#  
- **IDE:** Visual Studio  
- **Paradigm:** Object-Oriented Programming (OOP)  
- **Architecture:** Layered Architecture  

---

## How to Run 🚀

### Prerequisites
- **.NET SDK** installed on your system.  
  Download it [here](https://dotnet.microsoft.com/download).  

### Steps to Run
1. Clone the repository to your local machine:  
   ```bash
   git clone https://github.com/dogaaydinn/lotus-student-management-system.git

2. Open the project in Visual Studio or your preferred C# IDE.
3. Build the project to restore dependencies and compile the code.
4. Run the application:
- Use the dotnet run command in the terminal, or
- Start the project through your IDE.

## Usage 📖
### Main Menu

1. Student Management

- Add, edit, or delete student records.
- View students' details and assign them to courses.

2. Course Management

- Create or delete courses.
- Assign students and teachers to specific courses.

3. Teacher Management

- Add, update, or delete teacher records.

4. Data Validation
The system includes input validation for:

- Non-empty names and valid numerical values for IDs.
- Proper GPA ranges (e.g., 0.0 to 4.0).

## Contribution 🤝
Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a feature branch:
    ```bash
    git checkout -b feature/your-feature-name

3. Commit your changes and push them to your fork.
4. Open a pull request.

### License 📜
This project is licensed under the MIT License. Feel free to use and modify the code as needed.


