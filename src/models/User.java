package models;

import config.config;
import java.util.*;

public class User {

    public static void registerUser(config db, String name, String email, String pass, String type) {
        String sql = "INSERT INTO users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
        db.addRecord(sql, name, email, type, "Pending", pass);
        System.out.println("User registered successfully!");
    }

    public static void loginUser(config db, Scanner sc, String email, String pw) {
        String qry = "SELECT * FROM users WHERE u_email = ? AND u_pass = ?";
        List<Map<String, Object>> result = db.fetchRecords(qry, email, pw);

        if (result.isEmpty()) {
            System.out.println("Invalid credentials!");
        } else {
            Map<String, Object> user = result.get(0);
            String role = (String) user.get("u_type");
            System.out.println("Welcome, " + user.get("u_name") + " (" + role + ")");

            if (role.equalsIgnoreCase("Admin")) adminMenu(db, sc);
            else if (role.equalsIgnoreCase("Teacher")) teacherMenu(db, sc);
            else if (role.equalsIgnoreCase("Student")) studentMenu(db, sc, email);
        }
    }

    // ========== MENUS ==========
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
                case 1: Student.manageStudents(db, sc); break;
                case 2: Subject.manageSubjects(db, sc); break;
                case 3: Grade.manageGrades(db, sc); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

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
                case 1: Student.viewStudents(db); break;
                case 2: Subject.viewSubjects(db); break;
                case 3: Grade.viewGrades(db); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

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
                    Grade.viewStudentGrades(db, email);
                    break;

                case 0:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
}
