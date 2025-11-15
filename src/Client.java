import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.ArrayList;

public class Client {
    private final DBConnect db;

    public Client(DBConnect db){
        this.db = db;
    }

    public ArrayList<String> getAllClients() throws SQLException {
        ArrayList<String> clients = new ArrayList<>();
        Connection conn = db.connect();

        if (conn == null) {
            throw new SQLException("Tentative de requete en étant déconnecté de la base de donnée");
        }

        String query = "SELECT * FROM client NATURAL JOIN utilisateur";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int colCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    sb.append(rs.getString(i));
                    if (i < colCount) sb.append(" | ");
                }
                clients.add(sb.toString());
            }
        }

        return clients;
    }
}
