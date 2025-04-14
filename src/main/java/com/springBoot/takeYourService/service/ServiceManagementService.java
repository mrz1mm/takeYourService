package com.springBoot.takeYourService.service;

import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione dei servizi offerti.
 * Fornisce metodi per creare, aggiornare, trovare e gestire i servizi nel sistema.
 */
public interface ServiceManagementService {
    
    /**
     * Crea un nuovo servizio.
     * @param serviceDto Dati del servizio da creare
     * @return Il servizio creato
     */
    ServiceDto createService(ServiceDto serviceDto);
    
    /**
     * Aggiorna un servizio esistente.
     * @param id ID del servizio da aggiornare
     * @param serviceDto Nuovi dati del servizio
     * @return Il servizio aggiornato
     * @throws RuntimeException Se il servizio non esiste
     */
    ServiceDto updateService(Long id, ServiceDto serviceDto);
    
    /**
     * Trova un servizio tramite il suo ID.
     * @param id ID del servizio
     * @return Optional contenente il servizio trovato o vuoto se non esiste
     */
    Optional<ServiceDto> findById(Long id);
    
    /**
     * Trova tutti i servizi attivi.
     * @return Lista dei servizi attivi
     */
    List<ServiceDto> findAllActive();
    
    /**
     * Trova tutti i servizi attivi con paginazione.
     * @param pageable Informazioni di paginazione
     * @return Pagina di servizi attivi
     */
    Page<ServiceDto> findAllActive(Pageable pageable);
    
    /**
     * Trova tutti i servizi (attivi e inattivi).
     * @return Lista di tutti i servizi
     */
    List<ServiceDto> findAll();
    
    /**
     * Trova tutti i servizi (attivi e inattivi) con paginazione.
     * @param pageable Informazioni di paginazione
     * @return Pagina di tutti i servizi
     */
    Page<ServiceDto> findAll(Pageable pageable);
    
    /**
     * Cerca servizi per nome contenente il termine specificato.
     * @param name Termine da cercare nel nome
     * @param pageable Informazioni di paginazione
     * @return Pagina di servizi che corrispondono alla ricerca
     */
    Page<ServiceDto> searchByName(String name, Pageable pageable);
    
    /**
     * Trova tutti i servizi offerti da un determinato fornitore.
     * @param providerId ID del fornitore
     * @return Lista dei servizi offerti dal fornitore
     */
    List<ServiceDto> findAllByProviderId(Long providerId);
    
    /**
     * Disattiva un servizio.
     * @param id ID del servizio da disattivare
     * @return true se il servizio è stato disattivato con successo, false altrimenti
     */
    boolean deactivateService(Long id);
    
    /**
     * Attiva un servizio.
     * @param id ID del servizio da attivare
     * @return true se il servizio è stato attivato con successo, false altrimenti
     */
    boolean activateService(Long id);
    
    /**
     * Elimina un servizio.
     * @param id ID del servizio da eliminare
     * @return true se il servizio è stato eliminato con successo, false altrimenti
     */
    boolean deleteService(Long id);
    
    /**
     * Converte un'entità Service in un DTO.
     * @param service Entità Service
     * @return DTO ServiceDto
     */
    ServiceDto convertToDto(Service service);
    
    /**
     * Converte un DTO ServiceDto in un'entità Service.
     * @param serviceDto DTO ServiceDto
     * @return Entità Service
     */
    Service convertToEntity(ServiceDto serviceDto);
    
    /**
     * Trova un'entità Service tramite il suo ID.
     * @param id ID del servizio
     * @return Optional contenente l'entità trovata o vuoto se non esiste
     */
    Optional<Service> findEntityById(Long id);
}