package com.springBoot.takeYourService.service;

import com.springBoot.takeYourService.model.dto.BookingDto;
import com.springBoot.takeYourService.model.entity.Booking;
import com.springBoot.takeYourService.model.entity.Booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servizio per la gestione delle prenotazioni.
 */
public interface BookingService {
    
    /**
     * Crea una nuova prenotazione.
     * 
     * @param clientId ID del cliente
     * @param providerId ID del fornitore di servizi
     * @param serviceId ID del servizio
     * @param slotId ID dello slot di disponibilità
     * @param notes Note sulla prenotazione
     * @return Il DTO della prenotazione creata
     */
    BookingDto createBooking(Long clientId, Long providerId, Long serviceId, Long slotId, String notes);
    
    /**
     * Trova una prenotazione per ID.
     * 
     * @param id L'ID della prenotazione da trovare
     * @return Optional contenente il DTO della prenotazione trovata o vuoto se non esiste
     */
    Optional<BookingDto> findById(Long id);
    
    /**
     * Trova tutte le prenotazioni di un cliente.
     * 
     * @param clientId L'ID del cliente
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findAllByClientId(Long clientId);
    
    /**
     * Trova tutte le prenotazioni di un fornitore di servizi.
     * 
     * @param providerId L'ID del fornitore di servizi
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findAllByProviderId(Long providerId);
    
    /**
     * Trova tutte le prenotazioni future di un cliente.
     * 
     * @param clientId L'ID del cliente
     * @param now Data e ora attuale
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findFutureBookingsByClientId(Long clientId, LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni future di un fornitore di servizi.
     * 
     * @param providerId L'ID del fornitore di servizi
     * @param now Data e ora attuale
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findFutureBookingsByProviderId(Long providerId, LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni passate di un cliente.
     * 
     * @param clientId L'ID del cliente
     * @param now Data e ora attuale
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findPastBookingsByClientId(Long clientId, LocalDateTime now);
    
    /**
     * Trova tutte le prenotazioni passate di un fornitore di servizi.
     * 
     * @param providerId L'ID del fornitore di servizi
     * @param now Data e ora attuale
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findPastBookingsByProviderId(Long providerId, LocalDateTime now);
    
    /**
     * Trova le prenotazioni di un cliente con uno stato specifico.
     * 
     * @param clientId L'ID del cliente
     * @param status Lo stato della prenotazione
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findByClientIdAndStatus(Long clientId, BookingStatus status);
    
    /**
     * Trova le prenotazioni di un fornitore con uno stato specifico.
     * 
     * @param providerId L'ID del fornitore
     * @param status Lo stato della prenotazione
     * @return Lista di DTO delle prenotazioni
     */
    List<BookingDto> findByProviderIdAndStatus(Long providerId, BookingStatus status);
    
    /**
     * Aggiorna lo stato di una prenotazione.
     * 
     * @param id L'ID della prenotazione
     * @param status Il nuovo stato
     * @return Il DTO della prenotazione aggiornata
     */
    BookingDto updateStatus(Long id, BookingStatus status);
    
    /**
     * Conferma una prenotazione.
     * 
     * @param id L'ID della prenotazione da confermare
     * @return Il DTO della prenotazione confermata
     */
    BookingDto confirmBooking(Long id);
    
    /**
     * Completa una prenotazione.
     * 
     * @param id L'ID della prenotazione da completare
     * @return Il DTO della prenotazione completata
     */
    BookingDto completeBooking(Long id);
    
    /**
     * Annulla una prenotazione.
     * 
     * @param id L'ID della prenotazione da annullare
     * @return Il DTO della prenotazione annullata
     */
    BookingDto cancelBooking(Long id);
    
    /**
     * Aggiorna le note di una prenotazione.
     * 
     * @param id L'ID della prenotazione
     * @param notes Le nuove note
     * @return Il DTO della prenotazione aggiornata
     */
    BookingDto updateNotes(Long id, String notes);
    
    /**
     * Segna una prenotazione come revisionata dal cliente.
     * 
     * @param id L'ID della prenotazione
     * @return true se la prenotazione è stata aggiornata con successo, false altrimenti
     */
    boolean markAsReviewed(Long id);
    
    /**
     * Converte un'entità Booking in un DTO BookingDto.
     * 
     * @param booking L'entità da convertire
     * @return Il DTO risultante
     */
    BookingDto convertToDto(Booking booking);
    
    /**
     * Trova un'entità Booking tramite il suo ID.
     * 
     * @param id L'ID della prenotazione
     * @return Optional contenente l'entità trovata o vuoto se non esiste
     */
    Optional<Booking> findEntityById(Long id);
}