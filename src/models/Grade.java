package models;

import config.config;
import java.util.*;

public class Grade {

    // View all grades (Admin/Teacher)
    public static void viewGrades(config db) {
        String query = "SELECT g.gradeid, g.t_id, g.s_id, g.sbj_id, g.prelim, g.midterm, g.prefinal, g.final, " +
                       "CASE WHEN g.final IS NULL OR g.midterm IS NULL OR g.prefinal IS NULL THEN 'N/A' " +
                       "WHEN g.final <= 3.0 THEN 'PASSED' ELSE 'FAILED' END AS remarks " +
                       "FROM grades g";
        String[] headers = {"GradeID", "TeacherID", "StudentID", "SubjectID", "Prelim", "Midterm", "Prefinal", "Final", "Remarks"};
        String[] cols = {"gradeid", "t_id", "s_id", "sbj_id", "prelim", "midterm", "prefinal", "final", "remarks"};
        db.viewRecords(query, headers, cols);
    }

    // View a student's grades (for that student)
    public static void viewStudentGrades(config db, String email) {
        String gradeQuery = "SELECT g.gradeid, sb.sbj_desc AS Subject, g.prelim, g.midterm, g.prefinal, g.final, " +
                            "CASE WHEN g.final IS NULL OR g.midterm IS NULL OR g.prefinal IS NULL THEN 'N/A' " +
                            "WHEN g.final <= 3.0 THEN 'PASSED' ELSE 'FAILED' END AS Remarks " +
                            "FROM grades g " +
                            "JOIN students st ON g.s_id = st.s_id " +
                            "JOIN subjects sb ON g.sbj_id = sb.sbj_id " +
                            "JOIN users u ON st.s_name = u.u_name " +
                            "WHERE u.u_email = ?";
        List<Map<String, Object>> gradeResult = db.fetchRecords(gradeQuery, email);

        if (gradeResult.isEmpty()) {
            System.out.println("No grades found.");
        } else {
            System.out.println("\n--- My Grades ---");
            for (Map<String, Object> row : gradeResult) {
                System.out.println("GradeID : " + row.get("gradeid"));
                System.out.println("Subject : " + row.get("Subject"));
                System.out.println("Prelim  : " + (row.get("prelim") != null ? row.get("prelim") : ""));
                System.out.println("Midterm : " + (row.get("midterm") != null ? row.get("midterm") : ""));
                System.out.println("Prefinal: " + (row.get("prefinal") != null ? row.get("prefinal") : ""));
                System.out.println("Final   : " + (row.get("final") != null ? row.get("final") : ""));
                System.out.println("Remarks : " + row.get("Remarks"));
                System.out.println("----------------------");
            }
        }
    }

    // Admin full-access placeholder
    public static void manageGrades(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- MANAGE GRADES (ADMIN) ---");
            System.out.println("1. View Grades");
            System.out.println("2. Update Grade");
            System.out.println("3. View Student Grade Report");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1: viewGrades(db); break;
                case 2: updateGrade(db, sc); break;
                case 3:
                    int sid = promptInt(sc, "Enter Student ID to view report: ");
                    viewStudentGradeReport(db, sid);
                    break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (true);
    }

    // ===== Teacher-Limited Method =====
    public static void manageGradesForTeacher(config db, Scanner sc, int teacherId) {
        System.out.println("\n=== Add Grades (Teacher) ===");

        // Select Student
        List<Map<String, Object>> students = db.fetchRecords("SELECT s_id, s_name FROM students");
        if (students.isEmpty()) {
            System.out.println("No students found!");
            return;
        }
        System.out.println("Available Students:");
        for (Map<String, Object> s : students) {
            System.out.println(s.get("s_id") + " - " + s.get("s_name"));
        }

        int sId = promptExistingId(sc, "Enter Student ID: ", students, "s_id");

        // Select Subject list
        List<Map<String, Object>> subjects = db.fetchRecords("SELECT sbj_id, sbj_desc FROM subjects");
        if (subjects.isEmpty()) {
            System.out.println("No subjects found!");
            return;
        }
        System.out.println("Available Subjects:");
        for (Map<String, Object> subj : subjects) {
            System.out.println(subj.get("sbj_id") + " - " + subj.get("sbj_desc"));
        }

        char addMore;
        do {
            int sbjId = promptExistingId(sc, "Enter Subject ID: ", subjects, "sbj_id");

            // Prelim is required for this example (professor example uses prelim filled)
            Double prelim = getRequiredGradeInput(sc, "Prelim");
            Double midterm = getGradeInput(sc, "Midterm");
            Double prefinal = getGradeInput(sc, "Prefinal");
            Double finalGrade = getGradeInput(sc, "Final");

            // compute remarks (store 'N/A' if incomplete)
            String remarks = computeRemarks(prelim, midterm, prefinal, finalGrade);

            db.addRecord(
                "INSERT INTO grades (s_id, sbj_id, prelim, midterm, prefinal, final, t_id, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                sId, sbjId, prelim, midterm, prefinal, finalGrade, teacherId, remarks
            );

            System.out.println("Grade added successfully!");
            System.out.print("Add another subject for this student? (Y/N): ");
            String line = sc.nextLine().trim();
            addMore = (line.isEmpty() ? 'N' : Character.toUpperCase(line.charAt(0)));
        } while (addMore == 'Y');
    }

    // Compute remarks: if any of midterm/prefinal/final missing => N/A; else evaluate final average
    public static String computeRemarks(Double prelim, Double mid, Double prefi, Double fin) {
        if (prelim == null || mid == null || prefi == null || fin == null) {
            return "N/A";
        }
        // compute average — adjust rule as needed
        double average = (prelim + mid + prefi + fin) / 4.0;
        // using scale where lower is better: <=3.0 passed
        if (average <= 3.0) return "PASSED";
        else return "FAILED";
    }

    // Update an existing grade (admin/teacher)
    public static void updateGrade(config db, Scanner sc) {
        int gradeId = promptInt(sc, "Enter Grade ID to update: ");
        List<Map<String, Object>> rec = db.fetchRecords("SELECT * FROM grades WHERE gradeid = ?", gradeId);
        if (rec.isEmpty()) {
            System.out.println("Grade record not found.");
            return;
        }
        Map<String, Object> g = rec.get(0);

        Double prelim = (g.get("prelim") instanceof Number) ? ((Number) g.get("prelim")).doubleValue() : null;
        Double midterm = (g.get("midterm") instanceof Number) ? ((Number) g.get("midterm")).doubleValue() : null;
        Double prefinal = (g.get("prefinal") instanceof Number) ? ((Number) g.get("prefinal")).doubleValue() : null;
        Double finalGrade = (g.get("final") instanceof Number) ? ((Number) g.get("final")).doubleValue() : null;

        System.out.println("Leave blank to keep existing value.");

        String midInput;
        while (true) {
            System.out.print("Midterm (" + (midterm != null ? midterm : "empty") + "): ");
            midInput = sc.nextLine().trim();
            if (midInput.isEmpty()) break;
            try {
                midterm = Double.parseDouble(midInput);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        String preInput;
        while (true) {
            System.out.print("Prefinal (" + (prefinal != null ? prefinal : "empty") + "): ");
            preInput = sc.nextLine().trim();
            if (preInput.isEmpty()) break;
            try {
                prefinal = Double.parseDouble(preInput);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        String finInput;
        while (true) {
            System.out.print("Final (" + (finalGrade != null ? finalGrade : "empty") + "): ");
            finInput = sc.nextLine().trim();
            if (finInput.isEmpty()) break;
            try {
                finalGrade = Double.parseDouble(finInput);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        String remarks = computeRemarks(prelim, midterm, prefinal, finalGrade);

        db.updateRecord("UPDATE grades SET midterm = ?, prefinal = ?, final = ?, remarks = ? WHERE gradeid = ?",
                        midterm, prefinal, finalGrade, remarks, gradeId);

        System.out.println("Grade updated successfully!");
    }

    // Full grade report for a student (studentId)
    public static void viewStudentGradeReport(config db, int studentId) {
        String sql = "SELECT g.gradeid, sb.sbj_desc AS subject, g.prelim, g.midterm, g.prefinal, g.final, g.remarks " +
                     "FROM grades g JOIN subjects sb ON sb.sbj_id = g.sbj_id " +
                     "WHERE g.s_id = ?";
        List<Map<String, Object>> list = db.fetchRecords(sql, studentId);

        if (list.isEmpty()) {
            System.out.println("No grades found for this student.");
            return;
        }

        System.out.println("\n--- GRADE REPORT ---");
        for (Map<String, Object> g : list) {
            System.out.println("GradeID : " + g.get("gradeid"));
            System.out.println("Subject : " + g.get("subject"));
            System.out.println("Prelim  : " + (g.get("prelim") != null ? g.get("prelim") : ""));
            System.out.println("Midterm : " + (g.get("midterm") != null ? g.get("midterm") : ""));
            System.out.println("Prefinal: " + (g.get("prefinal") != null ? g.get("prefinal") : ""));
            System.out.println("Final   : " + (g.get("final") != null ? g.get("final") : ""));
            System.out.println("Remarks : " + g.get("remarks"));
            System.out.println("----------------------");
        }
    }

    // Helper: prompt for a grade value (nullable). Loops until valid number or blank.
    private static Double getGradeInput(Scanner sc, String gradeName) {
        Double value = null;
        boolean valid = false;
        while (!valid) {
            System.out.print("Enter " + gradeName + " (leave empty to skip): ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                value = null;
                valid = true;
            } else {
                try {
                    value = Double.parseDouble(input);
                    if (value < 0) {
                        System.out.println("Grade cannot be negative. Try again.");
                    } else {
                        valid = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number. Try again.");
                }
            }
        }
        return value;
    }

    // Prelim required (non-empty) input helper
    private static Double getRequiredGradeInput(Scanner sc, String gradeName) {
        Double value = null;
        while (true) {
            System.out.print("Enter " + gradeName + " (required): ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(gradeName + " is required. This cannot be blank.");
            } else {
                try {
                    value = Double.parseDouble(input);
                    if (value < 0) {
                        System.out.println("Grade cannot be negative. Try again.");
                    } else {
                        return value;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number. Try again.");
                }
            }
        }
    }

    // Helper: ensures provided ID exists in list of records
    private static int promptExistingId(Scanner sc, String prompt, List<Map<String, Object>> records, String idKey) {
        int id = -1;
        boolean ok = false;
        while (!ok) {
            System.out.print(prompt);
            String line = sc.nextLine();
            try {
                id = Integer.parseInt(line);
                for (Map<String, Object> r : records) {
                    Object val = r.get(idKey);
                    if (val instanceof Integer && ((Integer) val) == id) {
                        ok = true;
                        break;
                    } else if (val instanceof Long && ((Long) val).intValue() == id) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) System.out.println("ID not found. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
        return id;
    }

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
