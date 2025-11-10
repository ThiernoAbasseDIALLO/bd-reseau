import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Properties;

public class Db_connect {
    public Db_connect() {
        Connection con = null;
        try{
            String dbURL3 = "jdbc:postgresql://185.31.40.68:5432/sae-tsm_dda";
            Properties parameters = new Properties();
            parameters.put("user", "sae-tsm_dda");
            parameters.put("password", "BdReseaux2025@");

            con = DriverManager.getConnection(dbURL3, parameters);
            if (con != null) {
                System.out.println("Connected to database #3");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Db_connect db = new Db_connect();
    }
}
