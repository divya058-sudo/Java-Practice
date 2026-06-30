import java.util.Scanner;

public class StudentManagementSystem {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        StudentDatabaseManager manager = new StudentDatabaseManager();

        while (true) {

            System.out.println("\n===== STUDENT MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Student");
            System.out.println("2. View Students");
            System.out.println("3. Update Student Course");
            System.out.println("4. Delete Student");
            System.out.println("5. Exit");

            System.out.print("Enter Choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:

                    System.out.print("Enter ID: ");
                    int id = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Age: ");
                    int age = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Email: ");
                    String email = sc.nextLine();

                    System.out.print("Enter Course: ");
                    String course = sc.nextLine();

                    System.out.print("Enter Department: ");
                    String department = sc.nextLine();

                    Student student = new Student(
                            name,
                            age,
                            id,
                            email,
                            course,
                            department);

                    manager.addStudent(student);
                    break;

                case 2:
                    manager.viewStudents();
                    break;

                case 3:

                    System.out.print("Enter Student ID: ");
                    int updateId = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter New Course: ");
                    String newCourse = sc.nextLine();

                    manager.updateStudentCourse(updateId, newCourse);
                    break;

                case 4:

                    System.out.print("Enter Student ID to Delete: ");
                    int deleteId = sc.nextInt();

                    manager.deleteStudent(deleteId);
                    break;

                case 5:

                    System.out.println("Thank You!");
                    sc.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }
}