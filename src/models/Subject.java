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

                case 2: viewSubjects(db); break;

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
}
