import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class SessionTracker {

    private static final int MAX_DAILY_SECONDS = 30 * 60; // 30 minutes

    private int userId;
    private long sessionStartTimeMillis;
    private int elapsedSecondsToday;
    private LocalDate sessionDate;

    public SessionTracker(int userId) {
        this.userId = userId;
        this.sessionDate = LocalDate.now();
        loadOrCreateDailyLimit();
    }

    private void loadOrCreateDailyLimit() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String insertSql = "INSERT IGNORE INTO user_time_limits (user_id, date, elapsed_seconds) VALUES (?, ?, 0)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, userId);
                insertStmt.setDate(2, java.sql.Date.valueOf(sessionDate));
                insertStmt.executeUpdate();
            }

            String selectSql = "SELECT elapsed_seconds FROM user_time_limits WHERE user_id = ? AND date = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, userId);
                selectStmt.setDate(2, java.sql.Date.valueOf(sessionDate));
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        this.elapsedSecondsToday = rs.getInt("elapsed_seconds");
                    } else {
                        this.elapsedSecondsToday = 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error loading session time limit: " + e.getMessage());
            this.elapsedSecondsToday = 0;
        }
        this.sessionStartTimeMillis = System.currentTimeMillis();
    }

    public int getRemainingSeconds() {
        if (!LocalDate.now().equals(sessionDate)) {
            saveSessionTime();
            this.sessionDate = LocalDate.now();
            loadOrCreateDailyLimit();
        }

        long sessionElapsed = (System.currentTimeMillis() - sessionStartTimeMillis) / 1000;
        int totalElapsed = elapsedSecondsToday + (int) sessionElapsed;
        int remaining = MAX_DAILY_SECONDS - totalElapsed;
        return Math.max(remaining, 0);
    }

    public boolean isTimeUp() {
        return getRemainingSeconds() <= 0;
    }

    public String getFormattedRemainingTime() {
        int remaining = getRemainingSeconds();
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        return minutes + " min " + seconds + " sec";
    }

    public void saveSessionTime() {
        long sessionElapsed = (System.currentTimeMillis() - sessionStartTimeMillis) / 1000;
        int totalElapsed = elapsedSecondsToday + (int) sessionElapsed;
        if (totalElapsed > MAX_DAILY_SECONDS) {
            totalElapsed = MAX_DAILY_SECONDS;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String updateSql = "UPDATE user_time_limits SET elapsed_seconds = ? WHERE user_id = ? AND date = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, totalElapsed);
                updateStmt.setInt(2, userId);
                updateStmt.setDate(3, java.sql.Date.valueOf(sessionDate));
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Database error saving session time: " + e.getMessage());
        }

        this.elapsedSecondsToday = totalElapsed;
        this.sessionStartTimeMillis = System.currentTimeMillis();
    }
}
