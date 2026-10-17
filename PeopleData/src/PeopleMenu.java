import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PeopleMenu extends JFrame {
    private JPanel mainPanel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tahunLahirField;
    private JComboBox<String> kategoriComboBox;
    private JRadioButton rbLaki;
    private JRadioButton rbPerempuan;
    private ButtonGroup genderGroup;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JTable peopleTable;
    private JLabel titleLabel;

    // objek koneksi database
    private Database database;

    private int selectedIndex = -1;
    private String selectedId = "";

    // constructor
    public PeopleMenu() {
        // inisialisasi komponen jika dijalankan di luar compiler GUI IntelliJ
        if (mainPanel == null) {
            initCustomComponents();
        }

        setContentPane(mainPanel);

        // inisialisasi ButtonGroup untuk RadioButton Jenis Kelamin (Bonus non-JTextField)
        if (genderGroup == null) {
            genderGroup = new ButtonGroup();
            genderGroup.add(rbLaki);
            genderGroup.add(rbPerempuan);
        }

        // atur styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box kategori
        String[] kategoriData = { "???", "Mahasiswa", "Dosen", "Tenaga Kependidikan", "Alumni" };
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(kategoriData));

        // sembunyikan button delete saat awal
        deleteButton.setVisible(false);

        // inisialisasi database dan muat tabel
        try {
            database = new Database();
            peopleTable.setModel(setTable());
        } catch (Exception ex) {
            System.err.println("Gagal koneksi ke database: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Gagal menghubungkan ke database 'db_people'!\nPastikan layanan MySQL sudah berjalan.\n\nDetail: " + ex.getMessage(),
                    "Koneksi Database Gagal",
                    JOptionPane.ERROR_MESSAGE);
        }

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == -1) {
                    insertData();
                } else {
                    updateData();
                }
            }
        });

        // saat tombol delete ditekan dengan confirmation prompt
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex != -1) {
                    int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Apakah Anda yakin ingin menghapus data ini?",
                            "Konfirmasi Hapus",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteData();
                    }
                }
            }
        });

        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // saat salah satu baris tabel ditekan
        peopleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = peopleTable.getSelectedRow();
                if (selectedIndex == -1) return;

                // simpan value textfield, combo box, dan radio button langsung dari tabel
                String curId = peopleTable.getModel().getValueAt(selectedIndex, 0).toString();
                String curNama = peopleTable.getModel().getValueAt(selectedIndex, 1).toString();
                String curTahunLahir = peopleTable.getModel().getValueAt(selectedIndex, 2).toString();
                String curKategori = peopleTable.getModel().getValueAt(selectedIndex, 3).toString();
                String curGender = peopleTable.getModel().getValueAt(selectedIndex, 4).toString();

                selectedId = curId;

                // ubah isi input form
                idField.setText(curId);
                namaField.setText(curNama);
                tahunLahirField.setText(curTahunLahir);
                kategoriComboBox.setSelectedItem(curKategori);

                if ("Laki-laki".equalsIgnoreCase(curGender)) {
                    rbLaki.setSelected(true);
                } else if ("Perempuan".equalsIgnoreCase(curGender)) {
                    rbPerempuan.setSelected(true);
                } else {
                    genderGroup.clearSelection();
                }

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] cols = { "ID", "Nama", "Tahun Lahir", "Kategori", "Jenis Kelamin" };

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel tmp = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only di tabel langsung
            }
        };

        if (database == null) {
            return tmp;
        }

        try {
            // ambil data langsung dari database
            ResultSet resultSet = database.selectQuery("SELECT * FROM person");

            while (resultSet.next()) {
                Object[] row = new Object[5];
                row[0] = resultSet.getString("id");
                row[1] = resultSet.getString("nama");
                row[2] = resultSet.getInt("thn_lahir");
                row[3] = resultSet.getString("kategori");
                row[4] = resultSet.getString("jenis_kelamin");
                tmp.addRow(row);
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat data dari database: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error memuat data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return tmp;
    }

    public void insertData() {
        if (database == null) {
            JOptionPane.showMessageDialog(this, "Database belum terhubung!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // ambil value dari form
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            String tahunStr = tahunLahirField.getText().trim();
            String kategori = (String) kategoriComboBox.getSelectedItem();
            String jenisKelamin = rbLaki.isSelected() ? "Laki-laki" : (rbPerempuan.isSelected() ? "Perempuan" : "");

            // 1. Validasi input kosong (Syarat TP 3)
            if (id.isEmpty() || nama.isEmpty() || tahunStr.isEmpty() || kategori == null || kategori.equals("???") || jenisKelamin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi lengkap!", "Error - Field Kosong", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Validasi ID duplikat (Syarat TP 4)
            ResultSet checkRs = database.selectQuery("SELECT COUNT(*) AS total FROM person WHERE id = '" + id + "'");
            if (checkRs.next() && checkRs.getInt("total") > 0) {
                JOptionPane.showMessageDialog(this, "ID '" + id + "' sudah terdaftar dalam database!", "Error - ID Duplikat", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int tahunLahir = Integer.parseInt(tahunStr);

            // query insert ke MySQL
            String sqlQuery = "INSERT INTO person (id, nama, thn_lahir, kategori, jenis_kelamin) VALUES ('"
                    + id + "', '" + nama + "', " + tahunLahir + ", '" + kategori + "', '" + jenisKelamin + "')";

            database.insertUpdateDeleteQuery(sqlQuery);

            // update tampilan tabel dari database
            peopleTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Insert berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tahun lahir harus berupa angka!", "Error - Format Angka", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateData() {
        if (selectedIndex == -1) return;
        if (database == null) {
            JOptionPane.showMessageDialog(this, "Database belum terhubung!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // ambil data dari form
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            String tahunStr = tahunLahirField.getText().trim();
            String kategori = (String) kategoriComboBox.getSelectedItem();
            String jenisKelamin = rbLaki.isSelected() ? "Laki-laki" : (rbPerempuan.isSelected() ? "Perempuan" : "");

            // 1. Validasi input kosong (Syarat TP 3)
            if (id.isEmpty() || nama.isEmpty() || tahunStr.isEmpty() || kategori == null || kategori.equals("???") || jenisKelamin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi lengkap!", "Error - Field Kosong", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Jika ID diubah, pastikan ID baru belum dipakai orang lain (Syarat TP 4)
            if (!id.equalsIgnoreCase(selectedId)) {
                ResultSet checkRs = database.selectQuery("SELECT COUNT(*) AS total FROM person WHERE id = '" + id + "'");
                if (checkRs.next() && checkRs.getInt("total") > 0) {
                    JOptionPane.showMessageDialog(this, "ID '" + id + "' sudah digunakan oleh civitas lain!", "Error - ID Duplikat", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int tahunLahir = Integer.parseInt(tahunStr);

            // query update ke MySQL
            String sqlQuery = "UPDATE person SET id = '" + id + "', nama = '" + nama + "', thn_lahir = " + tahunLahir
                    + ", kategori = '" + kategori + "', jenis_kelamin = '" + jenisKelamin + "' WHERE id = '" + selectedId + "'";

            database.insertUpdateDeleteQuery(sqlQuery);

            // update tampilan tabel dari database
            peopleTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Update berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil diubah!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tahun lahir harus berupa angka!", "Error - Format Angka", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteData() {
        if (selectedIndex == -1) return;
        if (database == null) {
            JOptionPane.showMessageDialog(this, "Database belum terhubung!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // hapus data dari database berdasarkan selectedId
            String sqlQuery = "DELETE FROM person WHERE id = '" + selectedId + "'";
            database.insertUpdateDeleteQuery(sqlQuery);

            // bersihkan seleksi sebelum ganti model
            peopleTable.clearSelection();

            // update tabel
            peopleTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Delete berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data dari database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearForm() {
        // kosongkan semua input form
        idField.setText("");
        namaField.setText("");
        tahunLahirField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        genderGroup.clearSelection();

        // bersihkan seleksi pada tabel
        peopleTable.clearSelection();

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
        selectedId = "";
    }

    // inisialisasi komponen manual untuk kompatibilitas kompilasi CLI & IDE
    private void initCustomComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        titleLabel = new JLabel("Daftar Civitas Akademik", SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Data Person"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        formPanel.add(new JLabel("ID / NIM / NIP:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.8;
        idField = new JTextField(20);
        formPanel.add(idField, gbc);

        // Nama
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.8;
        namaField = new JTextField(20);
        formPanel.add(namaField, gbc);

        // Tahun Lahir
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Tahun Lahir:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.8;
        tahunLahirField = new JTextField(20);
        formPanel.add(tahunLahirField, gbc);

        // Kategori
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.8;
        kategoriComboBox = new JComboBox<>();
        formPanel.add(kategoriComboBox, gbc);

        // Jenis Kelamin (Bonus non-JTextField: JRadioButton)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.8;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rbLaki = new JRadioButton("Laki-laki");
        rbPerempuan = new JRadioButton("Perempuan");
        genderGroup = new ButtonGroup();
        genderGroup.add(rbLaki);
        genderGroup.add(rbPerempuan);
        radioPanel.add(rbLaki);
        radioPanel.add(rbPerempuan);
        formPanel.add(radioPanel, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addUpdateButton = new JButton("Add");
        cancelButton = new JButton("Cancel");
        deleteButton = new JButton("Delete");
        buttonPanel.add(addUpdateButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteButton);
        formPanel.add(buttonPanel, gbc);

        // Center: Form + Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(formPanel, BorderLayout.NORTH);

        peopleTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(peopleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Tabel Data"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // buat object window
        PeopleMenu menu = new PeopleMenu();

        // atur ukuran window
        menu.setSize(750, 650);

        // letakkan window di tengah layar
        menu.setLocationRelativeTo(null);

        // isi window
        menu.setContentPane(menu.mainPanel);

        // ubah warna background
        menu.getContentPane().setBackground(Color.WHITE);

        // judul window
        menu.setTitle("PeopleData - Sistem Manajemen Civitas Akademika");

        // tampilkan window
        menu.setVisible(true);

        // agar program ikut berhenti saat window diclose
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
