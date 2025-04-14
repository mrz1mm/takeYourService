package com.springBoot.takeYourService.repository;

import com.springBoot.takeYourService.model.entity.Booking;
import com.springBoot.takeYourService.model.entity.Booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository per la gestione delle prenotazioni.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Trova tutte le prenotazioni di un cliente ordinate per data di inizio dello slot.
     * 
     * @param clientId ID del cliente
     * @return Lista di prenotazioni
     */
    List<Booking> findByClientIdOrderByAvailabilitySlot_StartTimeDesc(Long clientId);
    
    /**
     * Trova tutte le prenotazioni di un fornitore di servizi ordinate per data di inizio dello slot.
     * 
     * @param providerId ID del fornitore di servizi
     * @return Lista di prenotazioni
     */
    List<Booking> findByServiceProviderIdOrderByAvailabilitySlot_StartTimeDesc(Long providerId);
    
    /**
     * Trova tutte le prenotazioni future di un cliente.
     * 
     * @param clientId ID del cliente
     * @param now Data e ora attuale
     * @return Lista di prenotazioni
     */
    @Query("SELECT b FROM Booking b JOIN b.availabilitySlot a WHERE b.client.id = :clientId AND a.startTime > :now ORDER BY a.startTime DESC")
    List<Booking> findFutureBookingsByClientId(@Param("clientId") Long clientId, @Param("now") LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni future di un fornitore di servizi.
     * 
     * @param providerId ID del fornitore di servizi
     * @param now Data e ora attuale
     * @return Lista di prenotazioni
     */
    @Query("SELECT b FROM Booking b JOIN b.availabilitySlot a WHERE b.serviceProvider.id = :providerId AND a.startTime > :now ORDER BY a.startTime DESC")
    List<Booking> findFutureBookingsByProviderId(@Param("providerId") Long providerId, @Param("now") LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni passate di un cliente.
     * 
     * @param clientId ID del cliente
     * @param now Data e ora attuale
     * @return Lista di prenotazioni
     */
    @Query("SELECT b FROM Booking b JOIN b.availabilitySlot a WHERE b.client.id = :clientId AND a.endTime <= :now ORDER BY a.startTime DESC")
    List<Booking> findPastBookingsByClientId(@Param("clientId") Long clientId, @Param("now") LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni passate di un fornitore di servizi.
     * 
     * @param providerId ID del fornitore di servizi
     * @param now Data e ora attuale
     * @return Lista di prenotazioni
     */
    @Query("SELECT b FROM Booking b JOIN b.availabilitySlot a WHERE b.serviceProvider.id = :providerId AND a.endTime <= :now ORDER BY a.startTime DESC")
    List<Booking> findPastBookingsByProviderId(@Param("providerId") Long providerId, @Param("now") LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni di un cliente con un determinato stato ordinate per data di inizio dello slot.
     * 
     * @param clientId ID del cliente
     * @param status Stato della prenotazione
     * @return Lista di prenotazioni
     */
    List<Booking> findByClientIdAndStatusOrderByAvailabilitySlot_StartTimeDesc(Long clientId, String status);
    
    /**
     * Trova tutte le prenotazioni di un fornitore di servizi con un determinato stato ordinate per data di inizio dello slot.
     * 
     * @param providerId ID del fornitore di servizi
     * @param status Stato della prenotazione
     * @return Lista di prenotazioni
     */
    List<Booking> findByServiceProviderIdAndStatusOrderByAvailabilitySlot_StartTimeDesc(Long providerId, String status);
    
    /**
     * Conta tutte le prenotazioni di un cliente.
     * 
     * @param clientId ID del cliente
     * @return Numero di prenotazioni
     */
    long countByClientId(Long clientId);
    
    /**
     * Conta tutte le prenotazioni di un fornitore di servizi.
     * 
     * @param providerId ID del fornitore di servizi
     * @return Numero di prenotazioni
     */
    long countByServiceProviderId(Long providerId);
    
    /**
     * Conta tutte le prenotazioni di un cliente con un determinato stato.
     * 
     * @param clientId ID del cliente
     * @param status Stato della prenotazione
     * @return Numero di prenotazioni
     */
    long countByClientIdAndStatus(Long clientId, String status);
    
    /**
     * Conta tutte le prenotazioni di un fornitore di servizi con un determinato stato.
     * 
     * @param providerId ID del fornitore di servizi
     * @param status Stato della prenotazione
     * @return Numero di prenotazioni
     */
    long countByServiceProviderIdAndStatus(Long providerId, String status);
    
    /**
     * Trova tutte le prenotazioni per un determinato servizio.
     * 
     * @param serviceId ID del servizio
     * @return Lista di prenotazioni
     */
    List<Booking> findByServiceId(Long serviceId);
    
    /**
     * Trova tutte le prenotazioni per uno slot di disponibilità.
     * 
     * @param availabilitySlotId ID dello slot di disponibilità
     * @return Lista di prenotazioni
     */
    List<Booking> findByAvailabilitySlotId(Long availabilitySlotId);
    
    /**
     * Trova tutte le prenotazioni di un cliente.
     * @param clientId ID del cliente
     * @return Lista delle prenotazioni del cliente
     */
    List<Booking> findByClientIdOrderByStartTimeDesc(Long clientId);
    
    /**
     * Trova tutte le prenotazioni di un cliente con paginazione.
     * @param clientId ID del cliente
     * @param pageable Informazioni di paginazione
     * @return Pagina di prenotazioni del cliente
     */
    Page<Booking> findByClientIdOrderByStartTimeDesc(Long clientId, Pageable pageable);
    
    /**
     * Trova tutte le prenotazioni di un fornitore di servizi.
     * @param providerId ID del fornitore
     * @return Lista delle prenotazioni del fornitore
     */
    List<Booking> findByServiceProviderIdOrderByStartTimeDesc(Long providerId);
    
    /**
     * Trova tutte le prenotazioni di un fornitore di servizi con paginazione.
     * @param providerId ID del fornitore
     * @param pageable Informazioni di paginazione
     * @return Pagina di prenotazioni del fornitore
     */
    Page<Booking> findByServiceProviderIdOrderByStartTimeDesc(Long providerId, Pageable pageable);
    
    /**
     * Trova tutte le prenotazioni di un cliente con uno stato specifico.
     * @param clientId ID del cliente
     * @param status Stato della prenotazione
     * @return Lista delle prenotazioni del cliente con lo stato specificato
     */
    List<Booking> findByClientIdAndStatusOrderByStartTimeDesc(Long clientId, BookingStatus status);
    
    /**
     * Trova tutte le prenotazioni di un fornitore con uno stato specifico.
     * @param providerId ID del fornitore
     * @param status Stato della prenotazione
     * @return Lista delle prenotazioni del fornitore con lo stato specificato
     */
    List<Booking> findByServiceProviderIdAndStatusOrderByStartTimeDesc(Long providerId, BookingStatus status);
    
    /**
     * Trova tutte le prenotazioni future di un cliente (a partire da ora).
     * @param clientId ID del cliente
     * @param now Data e ora corrente
     * @return Lista delle prenotazioni future del cliente
     */
    @Query("SELECT b FROM Booking b WHERE b.client.id = :clientId AND b.startTime >= :now AND b.status <> 'CANCELLED' ORDER BY b.startTime ASC")
    List<Booking> findUpcomingBookingsByClientId(Long clientId, LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni future di un fornitore (a partire da ora).
     * @param providerId ID del fornitore
     * @param now Data e ora corrente
     * @return Lista delle prenotazioni future del fornitore
     */
    @Query("SELECT b FROM Booking b WHERE b.serviceProvider.id = :providerId AND b.startTime >= :now AND b.status <> 'CANCELLED' ORDER BY b.startTime ASC")
    List<Booking> findUpcomingBookingsByProviderId(Long providerId, LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni passate di un cliente (fino a ora).
     * @param clientId ID del cliente
     * @param now Data e ora corrente
     * @return Lista delle prenotazioni passate del cliente
     */
    @Query("SELECT b FROM Booking b WHERE b.client.id = :clientId AND b.endTime < :now ORDER BY b.startTime DESC")
    List<Booking> findPastBookingsByClientId(Long clientId, LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni passate di un fornitore (fino a ora).
     * @param providerId ID del fornitore
     * @param now Data e ora corrente
     * @return Lista delle prenotazioni passate del fornitore
     */
    @Query("SELECT b FROM Booking b WHERE b.serviceProvider.id = :providerId AND b.endTime < :now ORDER BY b.startTime DESC")
    List<Booking> findPastBookingsByProviderId(Long providerId, LocalDateTime now);
    
    /**
     * Verifica se esiste una prenotazione per uno slot specifico.
     * @param slotId ID dello slot
     * @return true se esiste, false altrimenti
     */
    boolean existsByAvailabilitySlotId(Long slotId);
}