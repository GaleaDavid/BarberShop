DROP DATABASE IF EXISTS BarberShopDB;
CREATE DATABASE BarberShopDB;
USE BarberShopDB;

CREATE TABLE Clienti (
                         id_client INT AUTO_INCREMENT PRIMARY KEY,
                         nume      VARCHAR(100) NOT NULL,
                         telefon   VARCHAR(20),
                         email     VARCHAR(100)
);

CREATE TABLE Frizeri (
                         id_frizer INT AUTO_INCREMENT PRIMARY KEY,
                         nume      VARCHAR(100) NOT NULL
);

CREATE TABLE Servicii (
                          id_serviciu INT AUTO_INCREMENT PRIMARY KEY,
                          denumire    VARCHAR(100) NOT NULL,
                          pret        DECIMAL(8,2)
);

-- la stergerea unui client dispar si programarile lui
CREATE TABLE Programari (
                            id_programare INT AUTO_INCREMENT PRIMARY KEY,
                            id_client     INT NOT NULL,
                            id_frizer     INT NOT NULL,
                            id_serviciu   INT NOT NULL,
                            data_ora      DATETIME NOT NULL,
                            status        VARCHAR(20) DEFAULT 'programat',
                            FOREIGN KEY (id_client)   REFERENCES Clienti(id_client)   ON DELETE CASCADE,
                            FOREIGN KEY (id_frizer)   REFERENCES Frizeri(id_frizer),
                            FOREIGN KEY (id_serviciu) REFERENCES Servicii(id_serviciu)
);

CREATE TABLE Utilizatori (
                             id_utilizator INT AUTO_INCREMENT PRIMARY KEY,
                             username      VARCHAR(50) UNIQUE NOT NULL,
                             parola        VARCHAR(100) NOT NULL,
                             rol           VARCHAR(20) NOT NULL
);

-- date de test
INSERT INTO Utilizatori (username, parola, rol) VALUES ('admin', 'admin123', 'admin');
INSERT INTO Clienti (nume, telefon, email) VALUES
                                               ('Ion Popescu', '0722111222', 'ion@mail.com'),
                                               ('Mihai Ionescu', '0733444555', 'mihai@mail.com');
INSERT INTO Frizeri (nume) VALUES ('Andrei'), ('Vlad');
INSERT INTO Servicii (denumire, pret) VALUES ('Tuns', 50), ('Barba', 30), ('Tuns + Barba', 70);
INSERT INTO Programari (id_client, id_frizer, id_serviciu, data_ora, status) VALUES
                                                                                 (1, 1, 1, '2026-11-20 10:00:00', 'programat'),
                                                                                 (2, 2, 3, '2026-11-21 15:30:00', 'programat');