package com.springBoot.takeYourService.service;

import com.springBoot.takeYourService.model.dto.AvailabilitySlotDto;
import com.springBoot.takeYourService.model.entity.AvailabilitySlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione degli slot di disponibilità.
 * Fornisce metodi per creare, trovare e gestire gli slot di disponibilità dei fornitori di servizi.
 */
public interface AvailabilitySlotService {
    
    /**
     * Crea un nuovo slot di disponibilità.
     * @param providerId ID del fornitore di servizi
     * @param startTime Data e ora di inizio
     * @param endTime Data e ora di fine
     * @return Lo slot di disponibilità creato
     * @throws RuntimeException Se il provider non esiste o se lo slot si sovrappone con uno già esistente
     */
    AvailabilitySlotDto createSlot(Long providerId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Crea più slot di disponibilità per un fornitore di servizi.
     * @param providerId ID del fornitore di servizi
     * @param date Data per cui creare gli slot
     * @param startHour Ora di inizio (es. 9)
     * @param endHour Ora di fine (es. 17)
     * @param slotDuration Durata di ogni slot in minuti
     * @param breakDuration Durata della pausa tra slot in minuti
     * @return Lista degli slot creati
     * @throws RuntimeException Se il provider non esiste o se ci sono sovrapposizioni
     */
    List<AvailabilitySlotDto> createMultipleSlots(
            Long providerId,
            LocalDate date,
            int startHour,
            int endHour,
            int slotDuration,
            int breakDuration);
    
    /**
     * Trova uno slot di disponibilità tramite il suo ID.
     * @param id ID dello slot
     * @return Optional contenente lo slot trovato o vuoto se non esiste
     */
    Optional<AvailabilitySlotDto> findById(Long id);
    
    /**
     * Trova tutti gli slot di disponibilità di un fornitore di servizi.
     * @param providerId ID del fornitore di servizi
     * @return Lista degli slot
     */
    List<AvailabilitySlotDto> findAllByProviderId(Long providerId);
    
    /**
     * Trova tutti gli slot disponibili (non prenotati) di un fornitore di servizi in un intervallo di date.
     * @param providerId ID del fornitore di servizi
     * @param startDate Data di inizio
     * @param endDate Data di fine
     * @return Lista degli slot disponibili
     */
    List<AvailabilitySlotDto> findAvailableSlots(Long providerId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Trova tutti gli slot di un fornitore di servizi in un intervallo di date, inclusi quelli già prenotati.
     * @param providerId ID del fornitore di servizi
     * @param startDate Data di inizio
     * @param endDate Data di fine
     * @return Lista di tutti gli slot nel periodo
     */
    List<AvailabilitySlotDto> findAllSlotsByDateRange(Long providerId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Genera automaticamente gli slot di disponibilità per un fornitore di servizi in base ai suoi orari di lavoro.
     * @param providerId ID del fornitore di servizi
     * @param startDate Data di inizio
     * @param endDate Data di fine
     * @param slotDuration Durata di ogni slot in minuti
     * @return Lista degli slot generati
     */
    List<AvailabilitySlotDto> generateSlotsFromWorkingHours(
            Long providerId, 
            LocalDate startDate, 
            LocalDate endDate, 
            int slotDuration);
    
    /**
     * Elimina uno slot di disponibilità.
     * @param id ID dello slot da eliminare
     * @return true se lo slot è stato eliminato con successo, false altrimenti
     * @throws RuntimeException Se lo slot è già prenotato
     */
    boolean deleteSlot(Long id);
    
    /**
     * Prenota uno slot di disponibilità.
     * @param slotId ID dello slot da prenotare
     * @return true se lo slot è stato prenotato con successo, false altrimenti
     */
    boolean bookSlot(Long slotId);
    
    /**
     * Libera uno slot di disponibilità.
     * @param slotId ID dello slot da liberare
     * @return true se lo slot è stato liberato con successo, false altrimenti
     */
    boolean freeSlot(Long slotId);
    
    /**
     * Converte un'entità AvailabilitySlot in un DTO AvailabilitySlotDto.
     * @param slot Entità AvailabilitySlot
     * @return DTO AvailabilitySlotDto
     */
    AvailabilitySlotDto convertToDto(AvailabilitySlot slot);
    
    /**
     * Trova un'entità AvailabilitySlot tramite il suo ID.
     * @param id ID dello slot
     * @return Optional contenente l'entità trovata o vuoto se non esiste
     */
    Optional<AvailabilitySlot> findEntityById(Long id);
}