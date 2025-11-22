package main;

import config.config;
import utils.PasswordUtil;
import java.util.*;
import models.User;

public class main {

    public static void main(String[] args) {
        config db = new config();
        db.connectDB();
        Scanner sc = new Scanner(System.in);

        // Auto-create SuperAdmin if not exists
        autoCreateSuperAdmin(db);

        while (true) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("0. Exit");

            int choice = promptInt(sc, "Enter choice: ", 0, 2);

            switch (choice) {
                case 1:
                    String name = promptNonEmptyString(sc, "Enter name: ");
                    String email = promptNonEmptyString(sc, "Enter email: ");
                    String pass = promptNonEmptyString(sc, "Enter password: ");
                    String hashedPass = PasswordUtil.hashPassword(pass);
                    String type = promptNonEmptyString(sc, "Enter type (Admin/Teacher/Student): ");
                    User.registerUser(db, name, email, hashedPass, type);
                    break;

                case 2:
                    sc.nextLine();
                    String em = promptNonEmptyString(sc, "Enter email: ");
                    String pw = promptNonEmptyString(sc, "Enter password: ");
                    String hashedPw = PasswordUtil.hashPassword(pw);
                    User.loginUser(db, sc, em, hashedPw);
                    break;

                case 0:
                    System.out.println("Exiting program... Goodbye!");
                    sc.close();
                    System.exit(0);
            }
        }
    }

    private static void autoCreateSuperAdmin(config db) {
        List<Map<String, Object>> superAdmin = db.fetchRecords(
                "SELECT * FROM users WHERE u_type = 'SuperAdmin'"
        );
        if (superAdmin.isEmpty()) {
            String hashed = PasswordUtil.hashPassword("superadmin"); // default password
            db.addRecord("INSERT INTO users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)",
                    "SuperAdmin", "superadmin@example.com", "SuperAdmin", "Approved", hashed
            );
            System.out.println("SuperAdmin account created. Email: superadmin@example.com | Password: superadmin");
        }
    }

    private static int promptInt(Scanner sc, String prompt, int min, int max) {
        int val = -1;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            String line = sc.nextLine();
            try {
                val = Integer.parseInt(line);
                if (val < min || val > max) System.out.println("Choice must be between " + min + " and " + max + ".");
                else valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
        return val;
    }

    private static String promptNonEmptyString(Scanner sc, String prompt) {
        String s = "";
        do {
            System.out.print(prompt);
            s = sc.nextLine().trim();
            if (s.isEmpty()) System.out.println("This field cannot be empty. Try again.");
        } while (s.isEmpty());
        return s;
    }
}
