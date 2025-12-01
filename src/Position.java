import java.sql.*;

public class Position {
    private Connection conn;

    public Position(Connection conn) {
        this.conn = conn;
    }

    public boolean insertColisPosition(String colisId, double latitude, double longitude) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        String query = "INSERT INTO position (latitude, longitude, colis_id) VALUES (?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, latitude);
            stmt.setDouble(2, longitude);
            stmt.setString(3, colisId);
            int rowsAffected = stmt.executeUpdate();;
            return rowsAffected == 1;
        }
    }
}
