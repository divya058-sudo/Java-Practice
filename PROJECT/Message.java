

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private User sender;
    private User receiver;
    private String text;
    private LocalDateTime timestamp;

    public Message(User sender, User receiver, String text) {
        this(sender, receiver, text, LocalDateTime.now());
    }

    public Message(User sender, User receiver, String text, LocalDateTime timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = timestamp;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void display() {
        System.out.println(sender.getUsername() +
                " -> " +
                receiver.getUsername() +
                " : " +
                text);
    }

    public String toFileFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "[" + timestamp.format(formatter) + "] " +
                sender.getUsername() + " -> " + receiver.getUsername() + " : " + text;
    }
}