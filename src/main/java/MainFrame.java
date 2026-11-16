import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainFrame extends JFrame {

    private JTable tableClienti;
    private DefaultTableModel modelClienti;
    private JTextField txtNume, txtTelefon, txtEmail, txtCauta;

    // Setarile pentru paginarea din SQL (Limit & Offset)
    private int paginaCurenta = 1;
    private final int randuriPePagina = 15;

    private DefaultTableModel modelProgramari;

    public MainFrame() {
        setTitle("Barber Shop - Gestiune (Modul Administrator)");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Folosim tab-uri pentru a pastra interfata curata
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clienti (Operatii CRUD)", creareTabClienti());
        tabs.addTab("Agenda Programari (JOIN-uri)", creareTabProgramari());
        add(tabs);

        incarcaClienti();
        incarcaProgramari();
    }

    // =====================================================================
    // TAB-UL 1: GESTIUNE CLIENTI
    // =====================================================================
    private JPanel creareTabClienti() {
        JPanel panel = new JPanel(new BorderLayout());

        // Partea de sus: Bara de cautare si controalele de paginare
        JPanel sus = new JPanel();
        txtCauta = new JTextField(15);
        JButton btnCauta = new JButton("Cauta nume");
        JButton btnPrev = new JButton("<< Inapoi");
        JButton btnNext = new JButton("Inainte >>");

        sus.add(new JLabel("Filtrare:"));
        sus.add(txtCauta);
        sus.add(btnCauta);
        sus.add(btnPrev);
        sus.add(btnNext);
        panel.add(sus, BorderLayout.NORTH);

        // Partea de mijloc: Tabelul cu date
        modelClienti = new DefaultTableModel(new String[]{"ID", "Nume", "Telefon", "Email"}, 0);
        tableClienti = new JTable(modelClienti);
        panel.add(new JScrollPane(tableClienti), BorderLayout.CENTER);

        // Partea de jos: Formularul pentru operatii CRUD
        JPanel jos = new JPanel();
        txtNume = new JTextField(10);
        txtTelefon = new JTextField(10);
        txtEmail = new JTextField(15);
        JButton btnAdauga = new JButton("Adauga");
        JButton btnModifica = new JButton("Modifica");
        JButton btnSterge = new JButton("Sterge");

        jos.add(new JLabel("Nume:")); jos.add(txtNume);
        jos.add(new JLabel("Tel:")); jos.add(txtTelefon);
        jos.add(new JLabel("Email:")); jos.add(txtEmail);
        jos.add(btnAdauga); jos.add(btnModifica); jos.add(btnSterge);
        panel.add(jos, BorderLayout.SOUTH);

        // --- Evenimente Butoane ---

        // La cautare noua, resetam mereu vizualizarea la prima pagina
        btnCauta.addActionListener(e -> {
            paginaCurenta = 1;
            incarcaClienti();
        });

        btnPrev.addActionListener(e -> {
            if (paginaCurenta > 1) {
                paginaCurenta--;
                incarcaClienti();
            }
        });

        btnNext.addActionListener(e -> {
            paginaCurenta++;
            incarcaClienti();
            // Protectie: daca da 'Next' dar pagina e goala, il aruncam un pas inapoi
            if (modelClienti.getRowCount() == 0 && paginaCurenta > 1) {
                paginaCurenta--;
                incarcaClienti();
            }
        });

        // Cand utilizatorul da click pe un rand in tabel, aducem datele in casutele de jos
        tableClienti.getSelectionModel().addListSelectionListener(e -> {
            int rand = tableClienti.getSelectedRow();
            if (rand >= 0) {
                txtNume.setText(String.valueOf(modelClienti.getValueAt(rand, 1)));
                txtTelefon.setText(String.valueOf(modelClienti.getValueAt(rand, 2)));
                txtEmail.setText(String.valueOf(modelClienti.getValueAt(rand, 3)));
            }
        });

        // Apelam aceeasi metoda de procesare atat pentru Insert cat si pentru Update
        btnAdauga.addActionListener(e -> {
            String sql = "INSERT INTO Clienti (nume, telefon, email) VALUES (?, ?, ?)";
            executaUpdateClient(sql, -1); // -1 marcheaza ca e o inregistrare noua (fara ID existent)
        });

        btnModifica.addActionListener(e -> {
            int rand = tableClienti.getSelectedRow();
            if (rand == -1) {
                JOptionPane.showMessageDialog(this, "Te rog selecteaza un client din tabel mai intai!");
                return;
            }
            int id = Integer.parseInt(modelClienti.getValueAt(rand, 0).toString());
            String sql = "UPDATE Clienti SET nume=?, telefon=?, email=? WHERE id_client=?";
            executaUpdateClient(sql, id);
        });

        btnSterge.addActionListener(e -> {
            int rand = tableClienti.getSelectedRow();
            if (rand == -1) {
                JOptionPane.showMessageDialog(this, "Te rog selecteaza un client din tabel mai intai!");
                return;
            }
            int id = Integer.parseInt(modelClienti.getValueAt(rand, 0).toString());
            stergeClient(id);
        });

        return panel;
    }

    // =====================================================================
    // TAB-UL 2: AGENDA PROGRAMARI
    // =====================================================================
    private JPanel creareTabProgramari() {
        JPanel panel = new JPanel(new BorderLayout());

        modelProgramari = new DefaultTableModel(
                new String[]{"ID Prog.", "Client", "Frizer", "Serviciu", "Data / Ora", "Status"}, 0);
        JTable tableProgramari = new JTable(modelProgramari);
        panel.add(new JScrollPane(tableProgramari), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Reincarca Agenda");
        btnRefresh.addActionListener(e -> incarcaProgramari());

        JPanel jos = new JPanel();
        jos.add(btnRefresh);
        panel.add(jos, BorderLayout.SOUTH);

        return panel;
    }

    // =====================================================================
    // LOGICA DE BAZA DE DATE (CRUD)
    // =====================================================================

    // Am grupat INSERT-ul si UPDATE-ul aici pentru a nu duplica cod (DRY principle).
    private void executaUpdateClient(String sql, int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtNume.getText());
            pstmt.setString(2, txtTelefon.getText());
            pstmt.setString(3, txtEmail.getText());

            // Daca id e diferit de -1, inseamna ca facem UPDATE si setam parametrul al 4-lea
            if (id != -1) {
                pstmt.setInt(4, id);
            }

            pstmt.executeUpdate();
            incarcaClienti(); // Face refresh vizual la tabel
            curataCasute();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Eroare la salvarea in baza de date: " + ex.getMessage());
        }
    }

    private void stergeClient(int id) {
        String sql = "DELETE FROM Clienti WHERE id_client=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            incarcaClienti();
            curataCasute();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Stergerea a esuat (Posibil clientul are programari active): " + ex.getMessage());
        }
    }

    private void incarcaClienti() {
        modelClienti.setRowCount(0); // Curata tabelul inainte de noul fetch

        // Calcul matematic pentru a sti de unde incepem citirea in baza de date (Paginare)
        int offset = (paginaCurenta - 1) * randuriPePagina;

        String filtru = "%" + txtCauta.getText() + "%";
        String sql = "SELECT id_client, nume, telefon, email FROM Clienti "
                + "WHERE nume LIKE ? LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, filtru);
            pstmt.setInt(2, randuriPePagina);
            pstmt.setInt(3, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                modelClienti.addRow(new Object[]{
                        rs.getInt("id_client"),
                        rs.getString("nume"),
                        rs.getString("telefon"),
                        rs.getString("email")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Eroare la incarcarea clientilor: " + ex.getMessage());
        }
    }

    private void incarcaProgramari() {
        modelProgramari.setRowCount(0);

        // Baza de date pastreaza doar ID-uri, asa ca folosim INNER JOIN pentru a lega
        // programarile de tabelele de baza si a extrage numele reale ale persoanelor si serviciilor.
        String sql = "SELECT p.id_programare, c.nume AS nume_client, f.nume AS nume_frizer, "
                + "s.denumire AS nume_serviciu, p.data_ora, p.status "
                + "FROM Programari p "
                + "JOIN Clienti c ON p.id_client = c.id_client "
                + "JOIN Frizeri f ON p.id_frizer = f.id_frizer "
                + "JOIN Servicii s ON p.id_serviciu = s.id_serviciu "
                + "ORDER BY p.data_ora DESC LIMIT 50";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                modelProgramari.addRow(new Object[]{
                        rs.getInt("id_programare"),
                        rs.getString("nume_client"),
                        rs.getString("nume_frizer"),
                        rs.getString("nume_serviciu"),
                        rs.getString("data_ora"),
                        rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Eroare la aducerea agendei: " + ex.getMessage());
        }
    }

    // Metoda utilitara pentru a curata formularul dupa o actiune cu succes
    private void curataCasute() {
        txtNume.setText("");
        txtTelefon.setText("");
        txtEmail.setText("");
    }
}