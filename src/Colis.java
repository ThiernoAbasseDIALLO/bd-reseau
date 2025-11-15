import java.sql.*;

public class Colis {
    private  Connection conn;

    public Colis(Connection conn) {
        this.conn = conn;
    }

    public boolean isValidColis(String colisId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        String query = "SELECT 1 FROM colis WHERE colis_id =  ?";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()){
            return rs.next();
        }
    }

    public String getColis(String colisId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        String query = "SELECT colis_id, numero_suivi, poids, taille, etat, client_id " +
                "FROM colis " +
                "WHERE colis_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, colisId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("colis_id");
                    String suivi = rs.getString("numero_suivi");
                    double poids = rs.getDouble("poids");
                    String taille = rs.getString("taille");
                    String etat = rs.getString("etat");
                    String clientId = rs.getString("client_id");

                    return String.format(
                            "Colis %s → suivi=%s, poids=%.2fkg, taille=%s, état=%s, client=%s",
                            id, suivi, poids, taille, etat, clientId
                    );
                } else {
                    return "Colis " + colisId + " inexistant";
                }
            }
        }
    }

    public boolean updateEtatColis(String colisId, String etat) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        String query = "UPDATE colis SET etat = ? WHERE colis_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, etat);
            stmt.setString(2, colisId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected == 1;
        }
    }

    public boolean takeColis(String colisId, String livreurId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        boolean updated = updateEtatColis(colisId, "pris en charge");
        if (!updated) {
            return false;
        }

        String insertLivraison = "INSERT INTO livraison (colis_id, livreur_id, etat) VALUES (?, ?, 'prise en charge')";
        try (PreparedStatement stmt = conn.prepareStatement(insertLivraison)) {
            stmt.setString(1, colisId);
            stmt.setString(2, livreurId);
            stmt.executeUpdate();
        }

        return true;
    }

    public boolean insertNotification(String colisId, String typeNotification, String message) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requête sans connexion à la base de données");
        }

        String query = "INSERT INTO notification (notification_id, type_notification, message, colis_id) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            String notificationId = "NO" + System.currentTimeMillis();

            stmt.setString(1, notificationId);
            stmt.setString(2, typeNotification);
            stmt.setString(3, message);
            stmt.setString(4, colisId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected == 1;
        }
    }
}
