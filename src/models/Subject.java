package models;

import config.config;
import java.util.*;

public class Subject {

    public static void viewSubjects(config db) {
        String query = "SELECT * FROM subjects";
        String[] headers = {"ID", "Code", "Description"};
        String[] cols = {"sbj_id", "sbj_code", "sbj_desc"};
        db.viewRecords(query, headers, cols);
    }

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
            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    addSubjectValidated(db, sc);
                    break;
                case 2:
                    viewSubjects(db);
                    break;
                case 3:
                    updateSubjectValidated(db, sc);
                    break;
                case 4:
                    deleteSubjectValidated(db, sc);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (true);
    }

    private static void addSubjectValidated(config db, Scanner sc) {
        String code = promptNonEmpty(sc, "Enter subject code: ");
        String desc = promptNonEmpty(sc, "Enter description: ");
        db.addRecord("INSERT INTO subjects(sbj_code, sbj_desc) VALUES (?, ?)", code, desc);
        System.out.println("Subject added successfully!");
    }

    private static void updateSubjectValidated(config db, Scanner sc) {
        int sid = promptInt(sc, "Enter Subject ID to update: ");
        String ncode = promptNonEmpty(sc, "New code: ");
        String ndesc = promptNonEmpty(sc, "New description: ");
        db.updateRecord("UPDATE subjects SET sbj_code = ?, sbj_desc = ? WHERE sbj_id = ?", ncode, ndesc, sid);
        System.out.println("Subject updated successfully!");
    }

    private static void deleteSubjectValidated(config db, Scanner sc) {
        int did = promptInt(sc, "Enter Subject ID to delete: ");
        db.deleteRecord("DELETE FROM subjects WHERE sbj_id = ?", did);
        System.out.println("Subject deleted successfully!");
    }

    // Helpers
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
}
