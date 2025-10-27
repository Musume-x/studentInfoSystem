package main;

import config.config;
import models.*;
import java.util.*;

public class main {

    public static void main(String[] args) {
        config db = new config();
        db.connectDB();
        Scanner sc = new Scanner(System.in);
        char cont;

        do {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Register User");
            System.out.println("2. Login");
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
                    User.registerUser(db, name, email, pass, type);
                    break;

                case 2: // Login
                    System.out.print("Enter email: ");
                    sc.nextLine();
                    String em = sc.nextLine();
                    System.out.print("Enter password: ");
                    String pw = sc.nextLine();
                    User.loginUser(db, sc, em, pw);
                    break;

                case 0:
                    System.out.println("Exiting program... Goodbye!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nDo you want to continue? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');

        System.out.println("Program Ended. Goodbye!");
    }
}
