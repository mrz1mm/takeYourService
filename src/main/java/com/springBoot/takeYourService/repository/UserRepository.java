package com.springBoot.takeYourService.repository;

import com.springBoot.takeYourService.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per l'accesso ai dati degli utenti.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Trova un utente tramite username.
     * @param username Username da cercare
     * @return Optional contenente l'utente trovato o vuoto se non esiste
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Trova un utente tramite email.
     * @param email Email da cercare
     * @return Optional contenente l'utente trovato o vuoto se non esiste
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se esiste un utente con l'username specificato.
     * @param username Username da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica se esiste un utente con l'email specificata.
     * @param email Email da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByEmail(String email);
}