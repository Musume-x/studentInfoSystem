package models;

import config.config;
import java.util.*;

public class User {

    public static void registerUser(config db, String name, String email, String pass, String type) {
        // check uniqueness of email
        List<Map<String, Object>> exist = db.fetchRecords("SELECT u_id FROM users WHERE u_email = ?", email);
        if (!exist.isEmpty()) {
            System.out.println("Email already registered. Try login or use a different email.");
            return;
        }

        String sql = "INSERT INTO users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
        db.addRecord(sql, name, email, type, "Pending", pass);
        System.out.println("User registered successfully! Wait for Super Admin approval.");
    }

    public static void loginUser(config db, Scanner sc, String email, String pw) {
        String qry = "SELECT * FROM users WHERE u_email = ? AND u_pass = ?";
        List<Map<String, Object>> result = db.fetchRecords(qry, email, pw);

        if (result.isEmpty()) {
            System.out.println("Invalid credentials!");
            return;
        }

        Map<String, Object> user = result.get(0);

        // status check (case-insensitive)
        Object statusObj = user.get("u_status");
        String status = statusObj != null ? statusObj.toString() : "";
        if (!status.equalsIgnoreCase("Approved")) {
            System.out.println("Your account is still pending approval. Please wait for the Super Admin.");
            return;
        }

        String role = (user.get("u_type") != null) ? user.get("u_type").toString() : "";
        System.out.println("Welcome, " + user.get("u_name") + " (" + role + ")");

        // extract u_id safely
        int userId = -1;
        Object idObj = user.get("u_id");
        if (idObj instanceof Integer) userId = (Integer) idObj;
        else if (idObj instanceof Long) userId = ((Long) idObj).intValue();

        if (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("SuperAdmin")) adminMenu(db, sc, user, userId);
        else if (role.equalsIgnoreCase("Teacher")) teacherMenu(db, sc, userId);
        else if (role.equalsIgnoreCase("Student")) studentMenu(db, sc, email);
        else System.out.println("Unknown role. Contact admin.");
    }

    // ========== MENUS ==========
    // adminUser map is passed to allow role-check (SuperAdmin or Admin)
    public static void adminMenu(config db, Scanner sc, Map<String, Object> adminUser, int adminId) {
        int choice;
        String role = (adminUser.get("u_type") != null) ? adminUser.get("u_type").toString() : "";
        boolean isSuper = role.equalsIgnoreCase("SuperAdmin");

        do {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Manage Users (Approve Pending)");
            System.out.println("2. Manage Students");
            System.out.println("3. Manage Subjects");
            System.out.println("4. Manage Grades");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    if (isSuper) {
                        managePendingUsers(db, sc);
                    } else {
                        System.out.println("Only SuperAdmin can approve or reject accounts.");
                    }
                    break;
                case 2:
                    Student.manageStudents(db, sc);
                    break;
                case 3:
                    Subject.manageSubjects(db, sc);
                    break;
                case 4:
                    Grade.manageGrades(db, sc);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (true);
    }

    // List pending users and approve (SuperAdmin only)
    private static void managePendingUsers(config db, Scanner sc) {
        List<Map<String, Object>> pending = db.fetchRecords("SELECT u_id, u_name, u_email, u_type, u_status FROM users WHERE u_status = 'Pending'");
        if (pending.isEmpty()) {
            System.out.println("No pending accounts found.");
            return;
        }

        System.out.println("\n--- PENDING ACCOUNTS ---");
        for (Map<String, Object> u : pending) {
            System.out.println("ID: " + u.get("u_id") + " | Name: " + u.get("u_name") + " | Email: " + u.get("u_email") + " | Type: " + u.get("u_type"));
        }

        int idToApprove = promptInt(sc, "Enter user ID to approve (or 0 to cancel): ");
        if (idToApprove == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        db.updateRecord("UPDATE users SET u_status = ? WHERE u_id = ?", "Approved", idToApprove);
        System.out.println("User approved successfully!");
    }

    public static void teacherMenu(config db, Scanner sc, int teacherId) {
        int choice;
        do {
            System.out.println("\n--- TEACHER MENU ---");
            System.out.println("1. Add Student");
            System.out.println("2. Add Grade");
            System.out.println("3. View Students");
            System.out.println("4. View Subjects");
            System.out.println("5. View Grades");
            System.out.println("6. Update Grade");
            System.out.println("7. View Grade Report");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    Student.manageStudentsForTeacher(db, sc);
                    break;
                case 2:
                    Grade.manageGradesForTeacher(db, sc, teacherId);
                    break;
                case 3:
                    Student.viewStudents(db);
                    break;
                case 4:
                    Subject.viewSubjects(db);
                    break;
                case 5:
                    Grade.viewGrades(db);
                    break;
                case 6:
                    Grade.updateGrade(db, sc);
                    break;
                case 7:
                    // prompt for student id to view report
                    int sid = promptInt(sc, "Enter Student ID to view report: ");
                    Grade.viewStudentGradeReport(db, sid);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (true);
    }

    public static void studentMenu(config db, Scanner sc, String email) {
        int choice;
        do {
            System.out.println("\n--- STUDENT MENU ---");
            System.out.println("1. View My Info");
            System.out.println("2. View My Grades");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                choice = -1;
            }

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
        } while (true);
    }

    // ---------- Helpers ----------
    private static int promptInt(Scanner sc, String prompt) {
        int val;
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                val = Integer.parseInt(line);
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }
}
