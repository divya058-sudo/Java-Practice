import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom {

    public void sendMessage(User sender, User receiver, String text) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sender.getId());
            stmt.setInt(2, receiver.getId());
            stmt.setString(3, text);
            stmt.executeUpdate();
            System.out.println("Message Sent!");
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    public List<Message> getChatHistory(User u1, User u2) {
        List<Message> history = new ArrayList<>();
        String sql = "SELECT m.sender_id, m.receiver_id, m.message_text, m.created_at " +
                     "FROM messages m " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                     "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.created_at ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, u1.getId());
            stmt.setInt(2, u2.getId());
            stmt.setInt(3, u2.getId());
            stmt.setInt(4, u1.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int senderId = rs.getInt("sender_id");
                    int receiverId = rs.getInt("receiver_id");
                    String text = rs.getString("message_text");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime timestamp = ts.toLocalDateTime();

                    User sender = (senderId == u1.getId()) ? u1 : u2;
                    User receiver = (receiverId == u1.getId()) ? u1 : u2;

                    history.add(new Message(sender, receiver, text, timestamp));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving chat history: " + e.getMessage());
        }
        return history;
    }

    public void showChat(User u1, User u2) {
        System.out.println("\n------ Chat History ------");
        List<Message> history = getChatHistory(u1, u2);
        for (Message m : history) {
            m.display();
        }
    }

    public void exportChatToFile(User u1, User u2, String filename) {
        List<Message> history = getChatHistory(u1, u2);
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Chat History between " + u1.getUsername() + " and " + u2.getUsername());
            writer.println("Exported on: " + LocalDateTime.now());
            writer.println("----------------------------------------");
            for (Message m : history) {
                writer.println(m.toFileFormat());
            }
            System.out.println("Chat History exported successfully to: " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting chat to file: " + e.getMessage());
        }
    }
}