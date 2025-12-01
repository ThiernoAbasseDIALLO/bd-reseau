import java.sql.SQLException;
import java.util.ArrayList;

public class TestBD {
    public static void main(String[] args) {
        // 1. Créer la connexion
        DBConfig config = new DBConfig("config.properties");
        DBConnect db = new DBConnect(config);

        try {
            Client client = new Client(db);

            ArrayList<String> clients = client.getAllClients();
            System.out.println("Liste des clients :");
            for (String c : clients) {
                System.out.println("- " + c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.closeConnection();
        }
    }
}
