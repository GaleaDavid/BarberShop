# Barber Shop App

Aplicație desktop scrisă în Java pentru gestionarea clienților și a programărilor dintr-o frizerie. Am construit acest proiect pentru a implementa operații CRUD pe o bază de date MySQL și pentru a exersa lucrul cu interfețe grafice (Swing).

## Tehnologii
- Limbaj: Java (versiune 17+)
- UI: Java Swing & AWT
- Bază de date: MySQL
- Management proiect: Maven
- Securitate: Credențialele bazei de date sunt externalizate (nu sunt hardcodate în sursă), iar interogările folosesc PreparedStatement pentru a preveni SQL Injection.

## Funcționalități principale
- Login: Acces pe bază de cont (vezi datele de test în scriptul SQL).
- Gestiune clienți: Interfață pentru Adăugare, Modificare și Ștergere clienți (CRUD).
- Paginare SQL: Tabelul de clienți nu aduce toate înregistrările deodată ca să nu blocheze aplicația, ci le trage paginat folosind LIMIT și OFFSET.
- Programări: Un tabel separat care combină datele din 4 tabele diferite (Programări, Clienți, Frizeri, Servicii) folosind INNER JOIN.

## Cum să rulezi proiectul local

Dacă vrei să testezi aplicația la tine pe calculator, urmează pașii ăștia:

### 1. Baza de date
Ia codul din fișierul barbershop_db.sql aflat în rădăcina proiectului și rulează-l în MySQL Workbench (sau ce client folosești). Scriptul o să creeze baza de date BarberShopDB, tabelele necesare și va insera câteva date de test ca să ai cu ce să te loghezi (ex: user admin, parola admin123).

### 2. Conexiunea la MySQL
Pentru că nu am vrut să urc parola mea reală de la baza de date pe GitHub, am ignorat fișierul de configurare. Ca să faci aplicația să meargă:
- Mergi în folderul src/main/resources/.
- Găsești acolo un fișier numit db.properties.example. Fă o copie a acestui fișier și redenumește copia în db.properties.
- Deschide db.properties și modifică cu userul și parola serverului tău de MySQL.

### 3. Rularea din IntelliJ
- Deschide proiectul în IntelliJ IDEA.
- Fiind un proiect Maven, asigură-te că dai un "Reload Maven Project" ca să își descarce automat driver-ul de MySQL (mysql-connector-j).
- Mergi în src/main/java, caută clasa LoginFrame.java și dă-i Run de la metoda main.