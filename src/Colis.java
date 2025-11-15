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

        String query = "SELECT 1 FROM colis WHERE colis_id = '" + colisId + "'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)){
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
}
