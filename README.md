# Tugas Praktikum 5 - Desain dan Pemrograman Berorientasi Objek (DPBO) 2026

## Janji
Saya Luthfi Naufal Alfareza dengan NIM 2511437 mengerjakan Tugas Praktikum 5 dalam mata kuliah Desain dan Pemrograman Berorientasi Objek (DPBO) untuk keberkahan-Nya maka saya tidak akan melakukan kecurangan seperti yang telah dispesifikasikan. Aamiin.

---

## Struktur Direktori Proyek
```
TP5DPBO2526C1/
├── Dokumentasi/
│   └── record_crud.gif
├── PeopleData/
│   ├── lib/
│   │   └── mysql-connector-j-8.4.0.jar
│   ├── src/
│   │   ├── Database.java
│   │   ├── PeopleMenu.form
│   │   ├── PeopleMenu.java
│   │   └── Person.java
│   └── PeopleData.iml
├── db_people.sql
└── README.md
```

---

## Desain Program

### 1. Database
Mengelola koneksi JDBC ke MySQL dan mengeksekusi perintah SQL:
- `Connection connection` : Objek koneksi MySQL ke database `db_people`.
- `Statement statement` : Objek untuk eksekusi query.
- `selectQuery(String sql)` : Eksekusi `SELECT` dan mengembalikan `ResultSet`.
- `insertUpdateDeleteQuery(String sql)` : Eksekusi perintah `INSERT`, `UPDATE`, `DELETE` (`executeUpdate`).
- `getStatement()` : Getter untuk objek statement.

### 2. Person
Model entitas civitas akademika:
- `id` : String (Primary Key)
- `nama` : String
- `tahunLahir` : int
- `kategori` : String
- `jenisKelamin` : String

### 3. PeopleMenu
Antarmuka GUI berbasis Java Swing terhubung langsung ke database:
- `Database database` : Instansiasi class koneksi database.
- Komponen input: `idField`, `namaField`, `tahunLahirField`, `kategoriComboBox`, `genderGroup` (`rbLaki`, `rbPerempuan`).
- Komponen aksi: `addUpdateButton`, `cancelButton`, `deleteButton`.
- Komponen tabel: `peopleTable` (menampilkan data hasil query dari tabel `person`).

---

## Alur Program

Aplikasi tidak lagi menggunakan penyimpanan memori (`ArrayList`), melainkan berinteraksi langsung dengan MySQL database via JDBC:

1. **Inisialisasi & Pembacaan Data (Read):**
   - Saat jendela aplikasi dibuka, objek `Database` menginisialisasi koneksi ke `jdbc:mysql://localhost:3306/db_people`.
   - Method `setTable()` menjalankan query `SELECT * FROM person` dan memuat baris data ke `peopleTable`.

2. **Tambah Data (Create):**
   - Pengguna mengisi data formulir lalu menekan tombol **Add**.
   - **Validasi Field Kosong**: Menampilkan pesan error jika ada field yang belum diisi atau kategori belum dipilih.
   - **Validasi ID Duplikat**: Mengecek ke database apakah ID sudah ada (`SELECT COUNT(*)`). Jika sudah ada, menampilkan error dialog dan membatalkan proses insert.
   - Jika validasi lolos, query `INSERT INTO person ...` dieksekusi dan tabel disegarkan kembali dari database.

3. **Ubah Data (Update):**
   - Pengguna mengklik salah satu baris tabel. Data baris diambil via `getValueAt(selectedIndex, col)` dan ditampilkan pada form.
   - Tombol **Add** berubah menjadi **Update** dan tombol **Delete** muncul.
   - Pengguna mengubah data lalu menekan tombol **Update**.
   - Tervalidasi terhadap kolom kosong dan keunikan ID sebelum perintah `UPDATE person SET ... WHERE id = ...` dieksekusi.

4. **Hapus Data (Delete):**
   - Saat baris dipilih dan tombol **Delete** ditekan, muncul kotak konfirmasi (*confirmation prompt*).
   - Jika pengguna memilih **Yes**, query `DELETE FROM person WHERE id = ...` dieksekusi ke database.

5. **Batal / Reset Form (Cancel):**
   - Menekan tombol **Cancel** akan mengosongkan seluruh formulir, membersihkan pilihan baris tabel, mengembalikan tombol ke **Add**, dan menyembunyikan tombol **Delete**.

---

## Dokumentasi

[<video src="Dokumentasi/record.mp4" controls width="100%"></video>](https://github.com/user-attachments/assets/ef0cadad-7bf4-49de-8290-3f276232a933)

