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

                case 2: viewStudents(db); break;

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
}
