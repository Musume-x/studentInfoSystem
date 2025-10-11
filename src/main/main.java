package main;

import config.config;
import java.util.*;

public class main {

    public static void viewStudents() {
        String query = "SELECT s_id, s_name, s_age, s_gender FROM students";
        String[] headers = {"ID", "Name", "Age", "Gender"};
        String[] cols = {"s_id", "s_name", "s_age", "s_gender"};
        new config().viewRecords(query, headers, cols);
    }

    public static void viewSubjects() {
        String query = "SELECT * FROM subjects";
        String[] headers = {"ID", "Code", "Description"};
        String[] cols = {"sbj_id", "sbj_code", "sbj_desc"};
        new config().viewRecords(query, headers, cols);
    }

    public static void viewGrades() {
        String query = "SELECT g_id, s_name, sbj_desc, final, " +
                       "CASE WHEN final >= 75 THEN 'Pass' ELSE 'Fail' END AS remarks " +
                       "FROM grades " +
                       "JOIN students ON grades.s_id = students.s_id " +
                       "JOIN subjects ON grades.sbj_id = subjects.sbj_id";
        String[] headers = {"ID", "Student", "Subject", "Grade", "Remarks"};
        String[] cols = {"g_id", "s_name", "sbj_desc", "final", "remarks"};
        new config().viewRecords(query, headers, cols);
    }

    public static void viewUsers() {
        String query = "SELECT * FROM users";
        String[] headers = {"ID", "Name", "Email", "Type", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        new config().viewRecords(query, headers, cols);
    }

    public static void main(String[] args) {
        config db = new config();
        db.connectDB();
        Scanner sc = new Scanner(System.in);
        char cont;

        do {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("3. Manage Students");
            System.out.println("4. Manage Subjects");
            System.out.println("5. Manage Grades");
            System.out.println("6. View Users");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1: // Register user
                    System.out.print("Enter name: ");
                    sc.nextLine();
                    String name = sc.nextLine();
                    System.out.print("Enter email: ");
                    String email = sc.nextLine();
                    System.out.print("Enter password: ");
                    String pass = sc.nextLine();
                    System.out.print("Enter type (Admin/Teacher/Student): ");
                    String type = sc.nextLine();
                    String regSql = "INSERT INTO users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
                    db.addRecord(regSql, name, email, type, "Pending", pass);
                    System.out.println("User registered successfully!");
                    break;

                case 2: // Login
                    System.out.print("Enter email: ");
                    sc.nextLine();
                    String em = sc.nextLine();
                    System.out.print("Enter password: ");
                    String pw = sc.nextLine();
                    String qry = "SELECT * FROM users WHERE u_email = ? AND u_pass = ?";
                    List<Map<String, Object>> result = db.fetchRecords(qry, em, pw);

                    if (result.isEmpty()) {
                        System.out.println("Invalid credentials!");
                    } else {
                        Map<String, Object> user = result.get(0);
                        String role = (String) user.get("u_type");
                        System.out.println("Welcome, " + user.get("u_name") + " (" + role + ")");
                        
                        if (role.equalsIgnoreCase("Admin")) adminMenu(db, sc);
                        else if (role.equalsIgnoreCase("Teacher")) teacherMenu(db, sc);
                        else if (role.equalsIgnoreCase("Student")) studentMenu(db, sc, em);
                    }
                    break;

                case 3: manageStudents(db, sc); break;
                case 4: manageSubjects(db, sc); break;
                case 5: manageGrades(db, sc); break;
                case 6: viewUsers(); break;
                case 0: System.exit(0); break;
                default: System.out.println("Invalid choice!");
            }

            System.out.print("\nDo you want to continue? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');

        System.out.println("Program Ended. Goodbye!");
    }

    // ===================== ADMIN FUNCTIONS ===========================
    public static void adminMenu(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Manage Students");
            System.out.println("2. Manage Subjects");
            System.out.println("3. Manage Grades");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1: manageStudents(db, sc); break;
                case 2: manageSubjects(db, sc); break;
                case 3: manageGrades(db, sc); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ===================== TEACHER MENU ===========================
    public static void teacherMenu(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- TEACHER MENU ---");
            System.out.println("1. View Students");
            System.out.println("2. View Subjects");
            System.out.println("3. View Grades");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1: viewStudents(); break;
                case 2: viewSubjects(); break;
                case 3: viewGrades(); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ===================== STUDENT MENU ===========================
    public static void studentMenu(config db, Scanner sc, String email) {
    int choice;
    do {
        System.out.println("\n--- STUDENT MENU ---");
        System.out.println("1. View My Info");
        System.out.println("2. View My Grades");
        System.out.println("0. Logout");
        System.out.print("Choice: ");
        choice = sc.nextInt();

        switch (choice) {
            case 1:
                String infoQuery = "SELECT u_name, u_email, u_type FROM users WHERE u_email = ?";
                List<Map<String, Object>> infoResult = db.fetchRecords(infoQuery, email);

                if (infoResult.isEmpty()) {
                    System.out.println("No information found.");
                } else {
                    Map<String, Object> userInfo = infoResult.get(0);
                    System.out.println("\n--- My Information ---");
                    System.out.println("Name   : " + userInfo.get("u_name"));
                    System.out.println("Email  : " + userInfo.get("u_email"));
                    System.out.println("Type   : " + userInfo.get("u_type"));
                }
                break;

            case 2:
                String gradeQuery = "SELECT sbj_desc AS Subject, final AS Grade, " +
                                    "CASE WHEN final >= 75 THEN 'Pass' ELSE 'Fail' END AS Remarks " +
                                    "FROM grades " +
                                    "JOIN students ON grades.s_id = students.s_id " +
                                    "JOIN subjects ON grades.sbj_id = subjects.sbj_id " +
                                    "JOIN users ON students.s_name = users.u_name " +
                                    "WHERE users.u_email = ?";

                List<Map<String, Object>> gradeResult = db.fetchRecords(gradeQuery, email);

                if (gradeResult.isEmpty()) {
                    System.out.println("No grades found.");
                } else {
                    System.out.println("\n--- My Grades ---");
                    for (Map<String, Object> row : gradeResult) {
                        System.out.println("Subject: " + row.get("Subject"));
                        System.out.println("Grade  : " + row.get("Grade"));
                        System.out.println("Remarks: " + row.get("Remarks"));
                        System.out.println("----------------------");
                    }
                }
                break;

            case 0:
                return;

            default:
                System.out.println("Invalid choice!");
        }
    } while (choice != 0);
}


    // ===================== STUDENT CRUD ===========================
    public static void manageStudents(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- MANAGE STUDENTS ---");
            System.out.println("1. Add Student");
            System.out.println("2. View Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    sc.nextLine();
                    System.out.print("Enter student name: ");
                    String sname = sc.nextLine();
                    System.out.print("Enter age: ");
                    int age = sc.nextInt();
                    System.out.print("Enter gender: ");
                    sc.nextLine();
                    String gen = sc.nextLine();
                    db.addRecord("INSERT INTO students(s_name, s_age, s_gender) VALUES (?, ?, ?)", sname, age, gen);
                    break;

                case 2: viewStudents(); break;

                case 3:
                    System.out.print("Enter Student ID to update: ");
                    int sid = sc.nextInt(); sc.nextLine();
                    System.out.print("New name: ");
                    String newName = sc.nextLine();
                    System.out.print("New age: ");
                    int newAge = sc.nextInt();
                    System.out.print("New gender: ");
                    sc.nextLine();
                    String newGender = sc.nextLine();
                    db.updateRecord("UPDATE students SET s_name = ?, s_age = ?, s_gender = ? WHERE s_id = ?", newName, newAge, newGender, sid);
                    break;

                case 4:
                    System.out.print("Enter Student ID to delete: ");
                    int delId = sc.nextInt();
                    db.deleteRecord("DELETE FROM students WHERE s_id = ?", delId);
                    break;

                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ===================== SUBJECT CRUD ===========================
    public static void manageSubjects(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- MANAGE SUBJECTS ---");
            System.out.println("1. Add Subject");
            System.out.println("2. View Subjects");
            System.out.println("3. Update Subject");
            System.out.println("4. Delete Subject");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    sc.nextLine();
                    System.out.print("Enter subject code: ");
                    String code = sc.nextLine();
                    System.out.print("Enter description: ");
                    String desc = sc.nextLine();
                    db.addRecord("INSERT INTO subjects(sbj_code, sbj_desc) VALUES (?, ?)", code, desc);
                    break;

                case 2: viewSubjects(); break;

                case 3:
                    System.out.print("Enter Subject ID to update: ");
                    int sid = sc.nextInt(); sc.nextLine();
                    System.out.print("New code: ");
                    String ncode = sc.nextLine();
                    System.out.print("New description: ");
                    String ndesc = sc.nextLine();
                    db.updateRecord("UPDATE subjects SET sbj_code = ?, sbj_desc = ? WHERE sbj_id = ?", ncode, ndesc, sid);
                    break;

                case 4:
                    System.out.print("Enter Subject ID to delete: ");
                    int did = sc.nextInt();
                    db.deleteRecord("DELETE FROM subjects WHERE sbj_id = ?", did);
                    break;

                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ===================== GRADE CRUD ===========================
    public static void manageGrades(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- MANAGE GRADES ---");
            System.out.println("1. Add Grade");
            System.out.println("2. View Grades");
            System.out.println("3. Update Grade");
            System.out.println("4. Delete Grade");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Student ID: ");
                    int sid = sc.nextInt();
                    System.out.print("Subject ID: ");
                    int subid = sc.nextInt();
                    System.out.print("Final Grade: ");
                    double grd = sc.nextDouble();
                    db.addRecord("INSERT INTO grades(s_id, sbj_id, final) VALUES (?, ?, ?)", sid, subid, grd);
                    break;

                case 2: viewGrades(); break;

                case 3:
                    System.out.print("Enter Grade ID to update: ");
                    int gid = sc.nextInt();
                    System.out.print("Enter new final grade: ");
                    double newGrade = sc.nextDouble();
                    db.updateRecord("UPDATE grades SET final = ? WHERE g_id = ?", newGrade, gid);
                    break;

                case 4:
                    System.out.print("Enter Grade ID to delete: ");
                    int delGid = sc.nextInt();
                    db.deleteRecord("DELETE FROM grades WHERE g_id = ?", delGid);
                    break;

                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
}
