package com.springBoot.takeYourService.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO per la rappresentazione degli utenti.
 * Utilizzato per trasferire i dati degli utenti tra i vari strati dell'applicazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    
    @NotEmpty(message = "Il nome utente è obbligatorio")
    @Size(min = 3, max = 50, message = "Il nome utente deve essere compreso tra {min} e {max} caratteri")
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
    
    private Set<String> roleNames = new HashSet<>();
    
    private boolean active;
    
    /**
     * Ottiene il nome completo dell'utente.
     * @return Nome e cognome
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Verifica se l'utente ha un determinato ruolo.
     * @param roleName Nome del ruolo
     * @return true se l'utente ha il ruolo, false altrimenti
     */
    public boolean hasRole(String roleName) {
        return roleNames != null && roleNames.contains(roleName);
    }
    
    /**
     * Verifica se l'utente è un amministratore.
     * @return true se l'utente è un amministratore, false altrimenti
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    /**
     * Verifica se l'utente è un fornitore di servizi.
     * @return true se l'utente è un fornitore di servizi, false altrimenti
     */
    public boolean isProvider() {
        return hasRole("ROLE_PROVIDER");
    }
    
    /**
     * Verifica se l'utente è un cliente.
     * @return true se l'utente è un cliente, false altrimenti
     */
    public boolean isClient() {
        return hasRole("ROLE_CLIENT");
    }
}