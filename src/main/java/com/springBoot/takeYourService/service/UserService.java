package com.springBoot.takeYourService.service;

import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.model.dto.UserRegistrationDto;
import com.springBoot.takeYourService.model.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione degli utenti.
 * Fornisce metodi per la registrazione, autenticazione e gestione degli utenti.
 */
public interface UserService {
    
    /**
     * Registra un nuovo utente nel sistema.
     * @param registrationDto DTO contenente i dati di registrazione
     * @return L'utente registrato
     * @throws RuntimeException Se il nome utente o l'email sono già in uso
     */
    UserDto registerUser(UserRegistrationDto registrationDto);
    
    /**
     * Trova un utente tramite il suo ID.
     * @param id ID dell'utente
     * @return Optional contenente l'utente trovato o vuoto se non esiste
     */
    Optional<UserDto> findById(Long id);
    
    /**
     * Trova un utente tramite il suo nome utente.
     * @param username Nome utente
     * @return Optional contenente l'utente trovato o vuoto se non esiste
     */
    Optional<UserDto> findByUsername(String username);
    
    /**
     * Trova un utente tramite la sua email.
     * @param email Email dell'utente
     * @return Optional contenente l'utente trovato o vuoto se non esiste
     */
    Optional<UserDto> findByEmail(String email);
    
    /**
     * Trova tutti gli utenti nel sistema.
     * @return Lista di tutti gli utenti
     */
    List<UserDto> findAll();
    
    /**
     * Trova l'utente attualmente autenticato.
     * @return Optional contenente l'utente corrente o vuoto se non autenticato
     */
    Optional<UserDto> getCurrentUser();
    
    /**
     * Aggiorna i dati di un utente.
     * @param id ID dell'utente da aggiornare
     * @param userDto DTO contenente i nuovi dati
     * @return L'utente aggiornato
     * @throws RuntimeException Se l'utente non esiste
     */
    UserDto updateUser(Long id, UserDto userDto);
    
    /**
     * Cambia la password di un utente.
     * @param userId ID dell'utente
     * @param currentPassword Password attuale
     * @param newPassword Nuova password
     * @return true se la password è stata cambiata con successo, false altrimenti
     */
    boolean changePassword(Long userId, String currentPassword, String newPassword);
    
    /**
     * Verifica se un nome utente è già in uso.
     * @param username Nome utente da verificare
     * @return true se il nome utente è già in uso, false altrimenti
     */
    boolean isUsernameAlreadyInUse(String username);
    
    /**
     * Verifica se un'email è già in uso.
     * @param email Email da verificare
     * @return true se l'email è già in uso, false altrimenti
     */
    boolean isEmailAlreadyInUse(String email);
    
    /**
     * Disattiva un utente.
     * @param id ID dell'utente da disattivare
     * @return true se l'utente è stato disattivato con successo, false altrimenti
     */
    boolean deactivateUser(Long id);
    
    /**
     * Attiva un utente.
     * @param id ID dell'utente da attivare
     * @return true se l'utente è stato attivato con successo, false altrimenti
     */
    boolean activateUser(Long id);
    
    /**
     * Assegna un ruolo a un utente.
     * @param userId ID dell'utente
     * @param roleName Nome del ruolo
     * @return true se il ruolo è stato assegnato con successo, false altrimenti
     */
    boolean addRoleToUser(Long userId, String roleName);
    
    /**
     * Rimuove un ruolo da un utente.
     * @param userId ID dell'utente
     * @param roleName Nome del ruolo
     * @return true se il ruolo è stato rimosso con successo, false altrimenti
     */
    boolean removeRoleFromUser(Long userId, String roleName);
    
    /**
     * Converte un'entità User in un DTO.
     * @param user Entità User
     * @return DTO UserDto
     */
    UserDto convertToDto(User user);
    
    /**
     * Trova un'entità User tramite il suo ID.
     * @param id ID dell'utente
     * @return Optional contenente l'entità trovata o vuoto se non esiste
     */
    Optional<User> findEntityById(Long id);
}