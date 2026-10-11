package com.scc;


import com.scc.dao.Userdao;

import java.util.Scanner;

public class Main {


public static final Scanner sc = new Scanner(System.in);
    private static final Userdao userDao = new Userdao();
public static void main(String[] args){

    System.out.println("\n================================");
    System.out.println(" STUDENT RECORDS MANAGEMENT");
    System.out.println("================================");
    System.out.println("1. Login");
    System.out.println("2. Register");
    System.out.println("3. Exit");
    System.out.print("Choose an option: ");
    String choice = sc.nextLine();

    switch (choice) {
        case "1":
            break;
        case "2":
            System.out.print("Enter new username:");
            String user = sc.nextLine();
            System.out.print("Enter Password:");
            String password = sc.nextLine();

            System.out.print("Select a Identity  [1]Nurse [2]Clinic Staff [3]Regestrar [4]Student : ");
            int identityChoice = sc.nextInt();
            sc.nextLine();
            String identity;

            switch (identityChoice) {
                case 1:
                    identity = "Nurse";
                    break;
                case 2:
                    identity = "Clinic Staff";
                    break;
                case 3:
                    identity = "Registrar";
                    break;
                case 4:
                    identity = "Student";
                    break;
                default:
                    identity = null;
            }

            if (identity != null) {
                userDao.registerUser(user, password, identity);


            } else {
                System.out.println("Invalid identity selection!");
            }



            }




    }

}




