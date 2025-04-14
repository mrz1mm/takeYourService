# TakeYourService

TakeYourService è una piattaforma web per la prenotazione di servizi che connette clienti e fornitori di servizi in vari settori come salute, bellezza, consulenza e altro.

## Caratteristiche Principali

- **Gestione Account**: Registrazione e login per clienti, fornitori di servizi e amministratori
- **Prenotazione Servizi**: Semplice sistema di prenotazione per i clienti
- **Gestione Disponibilità**: I fornitori possono configurare i propri slot di disponibilità
- **Dashboard Personalizzate**: Pannelli di controllo specifici per ogni tipo di utente
- **Gestione Servizi**: Creazione, aggiornamento ed eliminazione di servizi offerti
- **Sicurezza**: Autenticazione e autorizzazione basate su ruoli

## Tecnologie Utilizzate

- **Backend**: Java Spring Boot
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Database**: MySQL/PostgreSQL
- **Build Tool**: Maven
- **Altre librerie**: Spring Security, Spring Data JPA, Flyway per le migrazioni del database

## Requisiti di Sistema

- Java 17 o versione successiva
- Maven 3.6 o versione successiva
- MySQL 8.0 o PostgreSQL 12 (o versioni successive)

## Installazione e Avvio

### Prerequisiti

- JDK installato
- Maven installato
- Database MySQL/PostgreSQL configurato

### Passaggi per l'installazione

1. Clona il repository:

   ```
   git clone https://github.com/YourUsername/takeYourService.git
   cd takeYourService
   ```

2. Configura il database in `src/main/resources/application.properties`

3. Compila il progetto:

   ```
   mvn clean install
   ```

4. Avvia l'applicazione:

   ```
   mvn spring-boot:run
   ```

   Oppure esegui:

   ```
   java -jar target/takeYourService-0.0.1-SNAPSHOT.jar
   ```

5. Accedi all'applicazione dal browser all'indirizzo `http://localhost:8080`

## Struttura del Progetto

```
src/
├── main/
│   ├── java/com/springBoot/takeYourService/
│   │   ├── controller/         # Gestisce le richieste HTTP
│   │   ├── model/              # Entità e DTO
│   │   ├── repository/         # Interfacce per l'accesso ai dati
│   │   ├── security/           # Configurazione sicurezza
│   │   └── service/            # Logica di business
│   └── resources/
│       ├── static/             # Asset statici (CSS, JS, immagini)
│       ├── templates/          # Template Thymeleaf
│       └── application.properties # Configurazione applicazione
```

## Utilizzo

### Per i Clienti

1. Registrarsi come client
2. Sfogliare i servizi disponibili
3. Prenotare un servizio selezionando data e ora
4. Visualizzare e gestire le prenotazioni dalla dashboard

### Per i Fornitori di Servizi

1. Registrarsi come fornitore di servizi
2. Configurare i servizi offerti e i relativi prezzi
3. Impostare gli slot di disponibilità
4. Gestire le prenotazioni dalla dashboard

### Per gli Amministratori

1. Accedere con le credenziali di amministratore
2. Gestire utenti, servizi e prenotazioni
3. Monitorare le attività della piattaforma

## Contribuzione

Se desideri contribuire al progetto:

1. Forka il repository
2. Crea un branch per le tue modifiche (`git checkout -b feature/amazing-feature`)
3. Commit delle tue modifiche (`git commit -m 'Add some amazing feature'`)
4. Push al branch (`git push origin feature/amazing-feature`)
5. Apri una Pull Request

## Licenza

Questo progetto è distribuito con licenza MIT. Vedi il file `LICENSE` per maggiori dettagli.

## Contatti

Nome - [email@example.com](mailto:email@example.com)

Project Link: [https://github.com/YourUsername/takeYourService](https://github.com/YourUsername/takeYourService)

---

© 2025 TakeYourService
