import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static User currentUser = null;
    private static SessionTracker sessionTracker = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ChatRoom room = new ChatRoom();

        DatabaseManager.initializeDatabase();

        while (true) {
            if (currentUser == null) {
                // Logged-out Menu
                System.out.println("\n1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choice: ");
                
                int choice;
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine(); // consume newline
                } else {
                    sc.nextLine(); // clear invalid input
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        registerUser(sc);
                        break;
                    case 2:
                        loginUser(sc);
                        break;
                    case 3:
                        System.out.println("Exiting chat application. Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid Choice");
                }
            } else {
                // Logged-in Menu
                if (sessionTracker.isTimeUp()) {
                    System.out.println("\n[Remaining Chat Time Today: 0 min 0 sec]");
                    System.out.println("Your daily 30-minute chat limit has ended. Logging you out.");
                    sessionTracker.saveSessionTime();
                    currentUser = null;
                    sessionTracker = null;
                    continue;
                }

                System.out.println("\n[Remaining Chat Time Today: " + sessionTracker.getFormattedRemainingTime() + "]");
                System.out.println("1. Start One-to-One Chat");
                System.out.println("2. Show Chat History");
                System.out.println("3. Save Chats to File");
                System.out.println("4. Logout");
                System.out.print("Choice: ");

                int choice;
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine(); // consume newline
                } else {
                    sc.nextLine(); // clear invalid input
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        startChat(sc, room);
                        break;
                    case 2:
                        showHistory(sc, room);
                        break;
                    case 3:
                        exportChat(sc, room);
                        break;
                    case 4:
                        sessionTracker.saveSessionTime();
                        System.out.println("Logged out successfully.");
                        currentUser = null;
                        sessionTracker = null;
                        break;
                    default:
                        System.out.println("Invalid Choice");
                }
            }
        }
    }

    private static void registerUser(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        System.out.print("Enter password: ");
        String password = sc.nextLine();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        // Check if username already exists
        if (findUserByUsername(username) != null) {
            System.out.println("Username already exists!");
            return;
        }

        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, User.hashPassword(password));
            stmt.executeUpdate();
            System.out.println("Registration successful!");
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
    }

    private static void loginUser(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "SELECT id, password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String storedHash = rs.getString("password_hash");
                    if (storedHash.equals(User.hashPassword(password))) {
                        currentUser = new User(id, username);
                        sessionTracker = new SessionTracker(id);
                        System.out.println("Login successful! Welcome, " + username + ".");
                    } else {
                        System.out.println("Invalid credentials.");
                    }
                } else {
                    System.out.println("Invalid credentials.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
    }

    private static void startChat(Scanner sc, ChatRoom room) {
        if (sessionTracker.isTimeUp()) {
            System.out.println("Your daily chat limit has ended. Messaging blocked.");
            return;
        }

        System.out.print("Enter username of the recipient: ");
        String recipientName = sc.nextLine().trim();
        User recipient = findUserByUsername(recipientName);

        if (recipient == null) {
            System.out.println("User not found.");
            return;
        }

        if (recipient.getId() == currentUser.getId()) {
            System.out.println("You cannot chat with yourself.");
            return;
        }

        System.out.println("Starting chat with " + recipient.getUsername() + ". Type '/exit' to return to menu.");

        java.util.List<Message> printed = new java.util.ArrayList<>();

        while (true) {
            if (sessionTracker.isTimeUp()) {
                System.out.println("\nDaily time limit of 30 minutes reached! Chat session terminated.");
                sessionTracker.saveSessionTime();
                break;
            }

            // Fetch and display only new messages before prompting
            java.util.List<Message> history = room.getChatHistory(currentUser, recipient);
            for (Message m : history) {
                boolean alreadyPrinted = false;
                for (Message p : printed) {
                    if (p.getSender().getId() == m.getSender().getId() &&
                        p.getText().equals(m.getText()) &&
                        p.getTimestamp().equals(m.getTimestamp())) {
                        alreadyPrinted = true;
                        break;
                    }
                }
                if (!alreadyPrinted) {
                    m.display();
                    printed.add(m);
                }
            }

            System.out.print("\n[Time Left: " + sessionTracker.getFormattedRemainingTime() + "]\nMessage: ");
            String msg = sc.nextLine().trim();

            if (msg.equalsIgnoreCase("/exit")) {
                sessionTracker.saveSessionTime();
                break;
            }

            if (msg.isEmpty()) {
                continue;
            }

            // Verify time again (just in case they took a while to type)
            if (sessionTracker.isTimeUp()) {
                System.out.println("\nDaily time limit reached! Message blocked.");
                sessionTracker.saveSessionTime();
                break;
            }

            room.sendMessage(currentUser, recipient, msg);
            sessionTracker.saveSessionTime(); // Save progress after each sent message
        }
    }

    private static void showHistory(Scanner sc, ChatRoom room) {
        System.out.print("Enter username of the other user: ");
        String otherName = sc.nextLine().trim();
        User otherUser = findUserByUsername(otherName);

        if (otherUser == null) {
            System.out.println("User not found.");
            return;
        }

        room.showChat(currentUser, otherUser);
    }

    private static void exportChat(Scanner sc, ChatRoom room) {
        System.out.print("Enter username of the other user: ");
        String otherName = sc.nextLine().trim();
        User otherUser = findUserByUsername(otherName);

        if (otherUser == null) {
            System.out.println("User not found.");
            return;
        }

        String defaultFilename = "chat_" + currentUser.getUsername() + "_to_" + otherUser.getUsername() + ".txt";
        System.out.print("Enter filename to export (press Enter for default: " + defaultFilename + "): ");
        String inputFilename = sc.nextLine().trim();
        String filename = inputFilename.isEmpty() ? defaultFilename : inputFilename;

        room.exportChatToFile(currentUser, otherUser, filename);
    }

    private static User findUserByUsername(String username) {
        String sql = "SELECT id, username FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error looking up user: " + e.getMessage());
        }
        return null;
    }
}