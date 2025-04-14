package com.springBoot.takeYourService.service;

import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.model.entity.ServiceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione dei fornitori di servizi.
 * Fornisce metodi per creare, aggiornare e gestire i fornitori di servizi nel sistema.
 */
public interface ServiceProviderService {
    
    /**
     * Crea un nuovo profilo di fornitore di servizi per un utente esistente.
     * @param userId ID dell'utente
     * @param description Descrizione del fornitore di servizi
     * @return Il fornitore di servizi creato
     * @throws RuntimeException Se l'utente non esiste o ha già un profilo di fornitore di servizi
     */
    ServiceProviderDto createServiceProvider(Long userId, String description);
    
    /**
     * Aggiorna un fornitore di servizi esistente.
     * @param id ID del fornitore di servizi
     * @param serviceProviderDto Nuovi dati del fornitore di servizi
     * @return Il fornitore di servizi aggiornato
     * @throws RuntimeException Se il fornitore di servizi non esiste
     */
    ServiceProviderDto updateServiceProvider(Long id, ServiceProviderDto serviceProviderDto);
    
    /**
     * Trova un fornitore di servizi tramite il suo ID.
     * @param id ID del fornitore di servizi
     * @return Optional contenente il fornitore di servizi trovato o vuoto se non esiste
     */
    Optional<ServiceProviderDto> findById(Long id);
    
    /**
     * Trova un fornitore di servizi tramite l'ID dell'utente associato.
     * @param userId ID dell'utente
     * @return Optional contenente il fornitore di servizi trovato o vuoto se non esiste
     */
    Optional<ServiceProviderDto> findByUserId(Long userId);
    
    /**
     * Trova tutti i fornitori di servizi attivi.
     * @param pageable Informazioni di paginazione
     * @return Pagina di fornitori di servizi attivi
     */
    Page<ServiceProviderDto> findAllActive(Pageable pageable);
    
    /**
     * Trova tutti i fornitori di servizi che offrono un determinato servizio.
     * @param serviceId ID del servizio
     * @param pageable Informazioni di paginazione
     * @return Pagina di fornitori di servizi che offrono il servizio
     */
    Page<ServiceProviderDto> findAllByServiceId(Long serviceId, Pageable pageable);
    
    /**
     * Cerca fornitori di servizi per nome o cognome dell'utente associato.
     * @param keyword Termine da cercare nel nome o cognome
     * @param pageable Informazioni di paginazione
     * @return Pagina di fornitori di servizi che corrispondono alla ricerca
     */
    Page<ServiceProviderDto> searchByName(String keyword, Pageable pageable);
    
    /**
     * Aggiorna gli orari di lavoro di un fornitore di servizi.
     * @param id ID del fornitore di servizi
     * @param workingHours Orari di lavoro in formato JSON
     * @return Il fornitore di servizi aggiornato
     * @throws RuntimeException Se il fornitore di servizi non esiste
     */
    ServiceProviderDto updateWorkingHours(Long id, String workingHours);
    
    /**
     * Aggiunge un servizio a un fornitore di servizi.
     * @param providerId ID del fornitore di servizi
     * @param serviceId ID del servizio da aggiungere
     * @return true se il servizio è stato aggiunto con successo, false altrimenti
     */
    boolean addService(Long providerId, Long serviceId);
    
    /**
     * Rimuove un servizio da un fornitore di servizi.
     * @param providerId ID del fornitore di servizi
     * @param serviceId ID del servizio da rimuovere
     * @return true se il servizio è stato rimosso con successo, false altrimenti
     */
    boolean removeService(Long providerId, Long serviceId);
    
    /**
     * Disattiva un fornitore di servizi.
     * @param id ID del fornitore di servizi
     * @return true se il fornitore di servizi è stato disattivato con successo, false altrimenti
     */
    boolean deactivateServiceProvider(Long id);
    
    /**
     * Attiva un fornitore di servizi.
     * @param id ID del fornitore di servizi
     * @return true se il fornitore di servizi è stato attivato con successo, false altrimenti
     */
    boolean activateServiceProvider(Long id);
    
    /**
     * Converte un'entità ServiceProvider in un DTO.
     * @param serviceProvider Entità ServiceProvider
     * @return DTO ServiceProviderDto
     */
    ServiceProviderDto convertToDto(ServiceProvider serviceProvider);
    
    /**
     * Trova un'entità ServiceProvider tramite il suo ID.
     * @param id ID del fornitore di servizi
     * @return Optional contenente l'entità trovata o vuoto se non esiste
     */
    Optional<ServiceProvider> findEntityById(Long id);
}