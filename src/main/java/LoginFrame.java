import javax.swing.*;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("Barber Shop - Autentificare");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Forteaza fereastra sa apara fix in centrul ecranului
        setLayout(new GridLayout(3, 2, 10, 10));

        userField = new JTextField();
        passField = new JPasswordField(); // Ascunde caracterele cand scrii parola
        JButton loginButton = new JButton("Login");

        add(new JLabel("  Username:"));
        add(userField);
        add(new JLabel("  Parola:"));
        add(passField);
        add(new JLabel("")); // Un mic hack de design: celula goala ca butonul sa pice in dreapta-jos
        add(loginButton);

        // Cand apesi butonul, declansam validarea in baza de date
        loginButton.addActionListener(e -> autentificare());
    }

    private void autentificare() {
        String username = userField.getText();
        String parola = new String(passField.getPassword());

        // Folosim PreparedStatement (?) obligatoriu. Daca am folosi concatenare simpla de string-uri,
        // cineva ar putea face SQL Injection (ex: scriind ' OR 1=1 -- in casuta de user).
        String sql = "SELECT rol FROM Utilizatori WHERE username = ? AND parola = ?";

        // Try-with-resources: inchide automat conexiunea la final, chiar daca da eroare
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, parola);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String rol = rs.getString("rol");
                JOptionPane.showMessageDialog(this,
                        "Autentificare reusita!\nConectat ca: " + rol.toUpperCase(),
                        "Succes", JOptionPane.INFORMATION_MESSAGE);

                // Deschidem fereastra principala si o inchidem pe asta de login
                new MainFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Username sau parola incorecte. Mai incearca!",
                        "Eroare", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Eroare de comunicare cu baza de date:\n" + ex.getMessage(),
                    "Eroare SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Pornim interfata grafica pe thread-ul corect (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}