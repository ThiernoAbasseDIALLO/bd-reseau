import java.sql.*;

public class Livreur {
    private Connection conn;

    public Livreur(Connection conn) {
        this.conn = conn;
    }

    public boolean isValidLivreur(String livreurId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        String query = "SELECT * FROM livreur WHERE livreur_id =  ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, livreurId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String getNameLivreur(String livreurId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        String query = "SELECT nom FROM livreur NATURAL JOIN utilisateur WHERE livreur_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, livreurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("nom");
                else
                    return null;
            }
        }
    }
}
