-- Database: db_people

CREATE DATABASE IF NOT EXISTS db_people;
USE db_people;

DROP TABLE IF EXISTS person;

CREATE TABLE person (
  id varchar(255) PRIMARY KEY,
  nama varchar(255) NOT NULL,
  thn_lahir int NOT NULL,
  kategori varchar(255) NOT NULL,
  jenis_kelamin varchar(255) NOT NULL
);

INSERT INTO person (id, nama, thn_lahir, kategori, jenis_kelamin) VALUES
('2511437', 'Luthfi Naufal Alfareza', 2004, 'Mahasiswa', 'Laki-laki'),
('2501002', 'Aisyah Zahra Putri', 2005, 'Mahasiswa', 'Perempuan'),
('19850101', 'Dr. Budi Santoso, M.Kom.', 1985, 'Dosen', 'Laki-laki'),
('19900215', 'Dr. Siti Aminah, M.T.', 1990, 'Dosen', 'Perempuan'),
('19950817', 'Rian Hidayat, S.Kom.', 1995, 'Tenaga Kependidikan', 'Laki-laki');
