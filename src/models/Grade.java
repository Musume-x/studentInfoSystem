package models;

import config.config;
import java.util.*;

public class Grade {

    public static void viewGrades(config db) {
        String query = "SELECT g_id, s_name, sbj_desc, final, " +
                       "CASE WHEN final >= 75 THEN 'Pass' ELSE 'Fail' END AS remarks " +
                       "FROM grades " +
                       "JOIN students ON grades.s_id = students.s_id " +
                       "JOIN subjects ON grades.sbj_id = subjects.sbj_id";
        String[] headers = {"ID", "Student", "Subject", "Grade", "Remarks"};
        String[] cols = {"g_id", "s_name", "sbj_desc", "final", "remarks"};
        db.viewRecords(query, headers, cols);
    }

    public static void viewStudentGrades(config db, String email) {
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
    }

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
                    System.out.println("\n=== Add Grades ===");
                    List<Map<String, Object>> students = db.fetchRecords("SELECT s_id, s_name FROM students");
                    if (students.isEmpty()) {
                        System.out.println("No students found!");
                        break;
                    }

                    System.out.println("Available Students:");
                    for (Map<String, Object> s : students) {
                        System.out.println(s.get("s_id") + " - " + s.get("s_name"));
                    }

                    System.out.print("\nEnter Student ID: ");
                    int sId = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Assign all default subjects (Math, Science, English, Programming)? (Y/N): ");
                    char assignAll = sc.next().toUpperCase().charAt(0);

                    if (assignAll == 'Y') {
                        // Default subjects (use names or IDs)
                        String[] defaultSubjects = {"Math", "Science", "English", "Programming"};

                        for (String subj : defaultSubjects) {
                            // Check if subject exists
                            String checkQuery = "SELECT sbj_id FROM subjects WHERE sbj_desc LIKE ?";
                            List<Map<String, Object>> sbj = db.fetchRecords(checkQuery, "%" + subj + "%");

                            if (sbj.isEmpty()) {
                                System.out.println("⚠️ Subject not found in database: " + subj);
                                continue;
                            }

                            int sbjId = ((Number) sbj.get(0).get("sbj_id")).intValue();
                            System.out.print("Enter final grade for " + subj + ": ");
                            double grade = sc.nextDouble();

                            db.addRecord("INSERT INTO grades (s_id, sbj_id, final) VALUES (?, ?, ?)", sId, sbjId, grade);
                            System.out.println("Record added successfully for subject: " + subj);
                        }

                    } else {
                        // Manual subject selection
                        List<Map<String, Object>> subjects = db.fetchRecords("SELECT sbj_id, sbj_desc FROM subjects");
                        if (subjects.isEmpty()) {
                            System.out.println("No subjects found!");
                            break;
                        }

                        System.out.println("\nAvailable Subjects:");
                        for (Map<String, Object> subj : subjects) {
                            System.out.println(subj.get("sbj_id") + " - " + subj.get("sbj_desc"));
                        }

                        char addMore;
                        do {
                            System.out.print("\nEnter Subject ID: ");
                            int sbjId = sc.nextInt();
                            System.out.print("Enter Final Grade: ");
                            double grade = sc.nextDouble();

                            db.addRecord("INSERT INTO grades (s_id, sbj_id, final) VALUES (?, ?, ?)", sId, sbjId, grade);
                            System.out.println("Record added successfully!");

                            System.out.print("Add another subject? (Y/N): ");
                            addMore = sc.next().toUpperCase().charAt(0);
                        } while (addMore == 'Y');
                    }
                    break;

                case 2:
                    viewGrades(db);
                    break;

                case 3:
                    System.out.print("Enter Grade ID to update: ");
                    int gid = sc.nextInt();
                    System.out.print("Enter new grade: ");
                    double newGrade = sc.nextDouble();
                    db.updateRecord("UPDATE grades SET final = ? WHERE g_id = ?", newGrade, gid);
                    break;

                case 4:
                    System.out.print("Enter Grade ID to delete: ");
                    int delGid = sc.nextInt();
                    db.deleteRecord("DELETE FROM grades WHERE g_id = ?", delGid);
                    break;

                case 0:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
}
