package com.springBoot.takeYourService.repository;

import com.springBoot.takeYourService.model.entity.ServiceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per l'accesso ai dati dei fornitori di servizi.
 */
@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    
    /**
     * Trova un fornitore di servizi tramite l'ID dell'utente associato.
     * @param userId ID dell'utente
     * @return Optional contenente il fornitore trovato o vuoto se non esiste
     */
    Optional<ServiceProvider> findByUserId(Long userId);
    
    /**
     * Verifica se esiste un fornitore di servizi per l'utente specificato.
     * @param userId ID dell'utente
     * @return true se esiste, false altrimenti
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Trova tutti i fornitori attivi con paginazione.
     * @param pageable Informazioni di paginazione
     * @return Pagina di fornitori attivi
     */
    Page<ServiceProvider> findByActiveTrue(Pageable pageable);
    
    /**
     * Trova tutti i fornitori che offrono un determinato servizio.
     * @param serviceId ID del servizio
     * @param pageable Informazioni di paginazione
     * @return Pagina di fornitori che offrono il servizio
     */
    @Query("SELECT sp FROM ServiceProvider sp JOIN sp.servicesOffered s WHERE s.id = :serviceId AND sp.active = true")
    Page<ServiceProvider> findAllByServiceId(Long serviceId, Pageable pageable);
    
    /**
     * Cerca i fornitori per nome o cognome dell'utente associato.
     * @param keyword Termine da cercare nel nome o cognome
     * @param pageable Informazioni di paginazione
     * @return Pagina di fornitori che corrispondono alla ricerca
     */
    @Query("SELECT sp FROM ServiceProvider sp JOIN sp.user u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND sp.active = true")
    Page<ServiceProvider> searchByName(String keyword, Pageable pageable);
}