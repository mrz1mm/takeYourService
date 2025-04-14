package com.springBoot.takeYourService.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la registrazione degli utenti.
 * Contiene i dati necessari per la creazione di un nuovo account utente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
    
    @NotEmpty(message = "Il nome utente è obbligatorio")
    @Size(min = 3, max = 50, message = "Il nome utente deve essere compreso tra {min} e {max} caratteri")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Il nome utente può contenere solo lettere, numeri, punti, trattini e underscore")
    private String username;
    
    @NotEmpty(message = "L'email è obbligatoria")
    @Email(message = "Il formato dell'email non è valido")
    private String email;
    
    @NotEmpty(message = "Il nome è obbligatorio")
    @Size(max = 50, message = "Il nome non può superare {max} caratteri")
    private String firstName;
    
    @NotEmpty(message = "Il cognome è obbligatorio")
    @Size(max = 50, message = "Il cognome non può superare {max} caratteri")
    private String lastName;
    
    @NotEmpty(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve contenere almeno {min} caratteri")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", 
             message = "La password deve contenere almeno un numero, una lettera minuscola, una lettera maiuscola e un carattere speciale")
    private String password;
    
    @NotEmpty(message = "La conferma della password è obbligatoria")
    private String confirmPassword;
    
    /**
     * Flag che indica se l'utente si sta registrando anche come fornitore di servizi.
     */
    private boolean asServiceProvider;
    
    /**
     * Descrizione del fornitore di servizi, obbligatoria solo se asServiceProvider è true.
     */
    private String providerDescription;
}