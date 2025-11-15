import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfig {
    private final Properties properties;

    public DBConfig(String propertiesFileName) {
        properties = new Properties();

        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(propertiesFileName)) {

            if (input == null) {
                throw new RuntimeException("Fichier " + propertiesFileName + " introuvable");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement des propriétés", e);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
