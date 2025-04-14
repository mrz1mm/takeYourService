package com.springBoot.takeYourService.repository;

import com.springBoot.takeYourService.model.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository per l'accesso ai dati dei servizi.
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    /**
     * Trova tutti i servizi attivi.
     * @return Lista dei servizi attivi
     */
    List<Service> findByActiveTrue();
    
    /**
     * Trova tutti i servizi attivi con paginazione.
     * @param pageable Informazioni di paginazione
     * @return Pagina di servizi attivi
     */
    Page<Service> findByActiveTrue(Pageable pageable);
    
    /**
     * Cerca i servizi per nome contenente il termine specificato.
     * @param name Termine da cercare nel nome
     * @param pageable Informazioni di paginazione
     * @return Pagina di servizi che corrispondono alla ricerca
     */
    Page<Service> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    
    /**
     * Trova tutti i servizi offerti da un determinato fornitore.
     * @param providerId ID del fornitore
     * @return Lista dei servizi offerti dal fornitore
     */
    @Query("SELECT s FROM Service s JOIN s.serviceProviders sp WHERE sp.id = :providerId AND s.active = true")
    List<Service> findAllByServiceProviderId(Long providerId);
}