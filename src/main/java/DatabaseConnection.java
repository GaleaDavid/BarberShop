import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // Folosim Properties ca sa nu scriem parola direct (hardcoded) in cod.
    private static final Properties config = new Properties();

    // Bloc static - se executa o singura data cand aplicatia porneste,
    // ca sa nu citim fisierul de configurare la fiecare click din aplicatie.
    static {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                System.err.println("Atentie: Lipseste fisierul db.properties din folderul resources!");
                System.err.println("Copiaza fisierul db.properties.example, redenumeste-l si pune parola ta de MySQL.");
            } else {
                config.load(in);
            }
        } catch (IOException e) {
            System.err.println("Eroare la citirea setarilor bazei de date: " + e.getMessage());
        }
    }

    // Returneaza conexiunea. Daca pica, arunca exceptia mai departe catre interfata grafica
    // ca sa putem afisa un pop-up frumos utilizatorului, in loc sa crape aplicatia in spate.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.user"),
                config.getProperty("db.password")
        );
    }
    // --- TEST RAPID DE CONEXIUNE ---
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conexiune realizata cu succes la baza de date!");
        } catch (SQLException e) {
            System.out.println("❌ Conexiune esuata. Verifica parola.");
            System.out.println("Detalii eroare: " + e.getMessage());
        }
    }
}