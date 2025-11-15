import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
    private final String url;
    private final String user;
    private final String password;
    private final String driver;
    private Connection connection = null;

    public DBConnect(DBConfig config) {
        this.url = config.get("db.url");
        this.user = config.get("db.user");
        this.password = config.get("db.password");
        this.driver = config.get("db.driver");
    }

    public Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(driver);
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Connexion établie avec la base de donnée !");
            }
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de donnée Postgres", e);
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermée !");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}
