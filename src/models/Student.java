package models;

import config.config;
import java.util.*;

public class Student {

    public static void viewStudents(config db) {
        String query = "SELECT s_id, s_name, s_age, s_gender FROM students";
        String[] headers = {"ID", "Name", "Age", "Gender"};
        String[] cols = {"s_id", "s_name", "s_age", "s_gender"};
        db.viewRecords(query, headers, cols);
    }

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
            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    addStudentValidated(db, sc);
                    break;
                case 2:
                    viewStudents(db);
                    break;
                case 3:
                    updateStudentValidated(db, sc);
                    break;
                case 4:
                    deleteStudentValidated(db, sc);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (true);
    }

    private static void addStudentValidated(config db, Scanner sc) {
        String sname = promptNonEmpty(sc, "Enter student name: ");
        int age = promptIntRange(sc, "Enter age: ", 1, 120);
        String gen = promptNonEmpty(sc, "Enter gender: ");
        db.addRecord("INSERT INTO students(s_name, s_age, s_gender) VALUES (?, ?, ?)", sname, age, gen);
        System.out.println("Student added successfully!");
    }

    private static void updateStudentValidated(config db, Scanner sc) {
        int sid = promptInt(sc, "Enter Student ID to update: ");
        String newName = promptNonEmpty(sc, "New name: ");
        int newAge = promptIntRange(sc, "New age: ", 1, 120);
        String newGender = promptNonEmpty(sc, "New gender: ");
        db.updateRecord("UPDATE students SET s_name = ?, s_age = ?, s_gender = ? WHERE s_id = ?", newName, newAge, newGender, sid);
        System.out.println("Student updated successfully!");
    }

    private static void deleteStudentValidated(config db, Scanner sc) {
        int delId = promptInt(sc, "Enter Student ID to delete: ");
        db.deleteRecord("DELETE FROM students WHERE s_id = ?", delId);
        System.out.println("Student deleted successfully!");
    }

    // ===== Teacher-Limited Method =====
    public static void manageStudentsForTeacher(config db, Scanner sc) {
        System.out.println("\n=== Add Student (Teacher) ===");
        addStudentValidated(db, sc);
    }

    // Helper prompts
    private static String promptNonEmpty(Scanner sc, String prompt) {
        String s;
        do {
            System.out.print(prompt);
            s = sc.nextLine().trim();
            if (s.isEmpty()) System.out.println("This field cannot be empty. Try again.");
        } while (s.isEmpty());
        return s;
    }

    private static int promptInt(Scanner sc, String prompt) {
        int val;
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            try {
                val = Integer.parseInt(line);
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static int promptIntRange(Scanner sc, String prompt, int min, int max) {
        int v;
        while (true) {
            v = promptInt(sc, prompt);
            if (v < min || v > max) System.out.println("Value must be between " + min + " and " + max + ".");
            else return v;
        }
    }
}
