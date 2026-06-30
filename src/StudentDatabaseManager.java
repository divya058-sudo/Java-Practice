import java.sql.*;

public class StudentDatabaseManager {

    public void addStudent(Student s) {

        try {
            Connection con = MySQLConnectionManager.getConnection();

            String query = "INSERT INTO students(id,name,age,email,course,department) VALUES(?,?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, s.id);
            ps.setString(2, s.name);
            ps.setInt(3, s.age);
            ps.setString(4, s.email);
            ps.setString(5, s.course);
            ps.setString(6, s.department);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Student Added Successfully!");
            }

            ps.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void viewStudents() {

        try {
            Connection con = MySQLConnectionManager.getConnection();

            String query = "SELECT * FROM students";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {

                System.out.println(
                        rs.getInt("id") + " | "
                        + rs.getString("name") + " | "
                        + rs.getInt("age") + " | "
                        + rs.getString("email") + " | "
                        + rs.getString("course") + " | "
                        + rs.getString("department"));
            }

            rs.close();
            st.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void deleteStudent(int id) {

        try {
            Connection con = MySQLConnectionManager.getConnection();

            String query = "DELETE FROM students WHERE id=?";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Student Deleted Successfully!");
            } else {
                System.out.println("Student Not Found!");
            }

            ps.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateStudentCourse(int id, String course) {

        try {
            Connection con = MySQLConnectionManager.getConnection();

            String query = "UPDATE students SET course=? WHERE id=?";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, course);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Student Updated Successfully!");
            } else {
                System.out.println("Student Not Found!");
            }

            ps.close();
            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}