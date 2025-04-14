package com.springBoot.takeYourService.repository;

import com.springBoot.takeYourService.model.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository per l'accesso ai dati degli slot di disponibilità.
 */
@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    
    /**
     * Trova tutti gli slot disponibili per un determinato fornitore in un intervallo di date.
     * @param providerId ID del fornitore
     * @param startDate Data di inizio della ricerca
     * @param endDate Data di fine della ricerca
     * @return Lista degli slot disponibili
     */
    @Query("SELECT a FROM AvailabilitySlot a WHERE a.serviceProvider.id = :providerId " +
           "AND a.booked = false " +
           "AND a.startTime >= :startDate " +
           "AND a.endTime <= :endDate " +
           "ORDER BY a.startTime ASC")
    List<AvailabilitySlot> findAvailableSlotsByProviderAndDateRange(
            Long providerId, 
            LocalDateTime startDate, 
            LocalDateTime endDate);
    
    /**
     * Trova tutti gli slot (disponibili e prenotati) per un determinato fornitore in un intervallo di date.
     * @param providerId ID del fornitore
     * @param startDate Data di inizio della ricerca
     * @param endDate Data di fine della ricerca
     * @return Lista di tutti gli slot nel periodo
     */
    List<AvailabilitySlot> findByServiceProviderIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
            Long providerId, 
            LocalDateTime startDate, 
            LocalDateTime endDate);
    
    /**
     * Trova tutti gli slot per un determinato fornitore.
     * @param providerId ID del fornitore
     * @return Lista di tutti gli slot del fornitore
     */
    List<AvailabilitySlot> findByServiceProviderIdOrderByStartTimeAsc(Long providerId);
    
    /**
     * Trova tutti gli slot prenotati per un determinato fornitore.
     * @param providerId ID del fornitore
     * @return Lista degli slot prenotati
     */
    List<AvailabilitySlot> findByServiceProviderIdAndBookedTrueOrderByStartTimeAsc(Long providerId);
    
    /**
     * Controlla se esiste uno slot che si sovrappone con l'intervallo specificato per un fornitore.
     * @param providerId ID del fornitore
     * @param startTime Orario di inizio
     * @param endTime Orario di fine
     * @return true se esiste una sovrapposizione, false altrimenti
     */
    @Query("SELECT COUNT(a) > 0 FROM AvailabilitySlot a WHERE a.serviceProvider.id = :providerId " +
           "AND ((a.startTime <= :endTime AND a.endTime >= :startTime))")
    boolean existsOverlappingSlot(Long providerId, LocalDateTime startTime, LocalDateTime endTime);
}