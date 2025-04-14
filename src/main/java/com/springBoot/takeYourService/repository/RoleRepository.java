package com.springBoot.takeYourService.repository;

import com.springBoot.takeYourService.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per l'accesso ai dati dei ruoli.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Trova un ruolo tramite il nome.
     * @param name Nome del ruolo da cercare
     * @return Optional contenente il ruolo trovato o vuoto se non esiste
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verifica se esiste un ruolo con il nome specificato.
     * @param name Nome del ruolo da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByName(String name);
}