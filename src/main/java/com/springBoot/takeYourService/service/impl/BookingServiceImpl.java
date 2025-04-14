package com.springBoot.takeYourService.service.impl;

import com.springBoot.takeYourService.model.dto.AvailabilitySlotDto;
import com.springBoot.takeYourService.model.dto.BookingDto;
import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.model.entity.AvailabilitySlot;
import com.springBoot.takeYourService.model.entity.Booking;
import com.springBoot.takeYourService.model.entity.Booking.BookingStatus;
import com.springBoot.takeYourService.model.entity.Service;
import com.springBoot.takeYourService.model.entity.ServiceProvider;
import com.springBoot.takeYourService.model.entity.User;
import com.springBoot.takeYourService.repository.BookingRepository;
import com.springBoot.takeYourService.service.AvailabilitySlotService;
import com.springBoot.takeYourService.service.BookingService;
import com.springBoot.takeYourService.service.ServiceManagementService;
import com.springBoot.takeYourService.service.ServiceProviderService;
import com.springBoot.takeYourService.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio per la gestione delle prenotazioni.
 * Si occupa di tutte le operazioni relative alle prenotazioni nel sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ServiceProviderService providerService;
    private final ServiceManagementService serviceManagementService;
    private final AvailabilitySlotService availabilitySlotService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BookingDto createBooking(Long clientId, Long providerId, Long serviceId, Long slotId, String notes) {
        // Verifica che il cliente esista
        User client = userService.findEntityById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente non trovato con ID: " + clientId));
        
        // Verifica che il fornitore esista
        ServiceProvider provider = providerService.findEntityById(providerId)
                .orElseThrow(() -> new RuntimeException("Fornitore non trovato con ID: " + providerId));
        
        // Verifica che il servizio esista
        Service service = serviceManagementService.findEntityById(serviceId)
                .orElseThrow(() -> new RuntimeException("Servizio non trovato con ID: " + serviceId));
        
        // Verifica che il fornitore offra questo servizio
        if (!provider.getServices().contains(service)) {
            throw new RuntimeException("Il fornitore con ID " + providerId + 
                    " non offre il servizio con ID " + serviceId);
        }
        
        // Verifica che lo slot esista e sia disponibile
        AvailabilitySlot slot = availabilitySlotService.findEntityById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot non trovato con ID: " + slotId));
        
        if (slot.isBooked()) {
            throw new RuntimeException("Lo slot selezionato non è disponibile");
        }
        
        if (!slot.getServiceProvider().getId().equals(providerId)) {
            throw new RuntimeException("Lo slot non appartiene al fornitore selezionato");
        }
        
        // Prenota lo slot
        if (!availabilitySlotService.bookSlot(slotId)) {
            throw new RuntimeException("Impossibile prenotare lo slot");
        }
        
        // Crea la prenotazione
        Booking booking = new Booking();
        booking.setClient(client);
        booking.setServiceProvider(provider);
        booking.setService(service);
        booking.setAvailabilitySlot(slot);
        booking.setStartTime(slot.getStartTime());
        booking.setEndTime(slot.getEndTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setNotes(notes);
        booking.setFinalPrice(service.getPrice());
        booking.setClientReviewed(false);
        
        // Salva la prenotazione
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Creata prenotazione con ID: {} per il cliente {} con il fornitore {}", 
                savedBooking.getId(), clientId, providerId);
                
        return convertToDto(savedBooking);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BookingDto> findById(Long id) {
        return bookingRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findAllByClientId(Long clientId) {
        return bookingRepository.findByClientIdOrderByAvailabilitySlot_StartTimeDesc(clientId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findAllByProviderId(Long providerId) {
        return bookingRepository.findByServiceProviderIdOrderByAvailabilitySlot_StartTimeDesc(providerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findFutureBookingsByClientId(Long clientId, LocalDateTime now) {
        return bookingRepository.findFutureBookingsByClientId(clientId, now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findFutureBookingsByProviderId(Long providerId, LocalDateTime now) {
        return bookingRepository.findFutureBookingsByProviderId(providerId, now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findPastBookingsByClientId(Long clientId, LocalDateTime now) {
        return bookingRepository.findPastBookingsByClientId(clientId, now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findPastBookingsByProviderId(Long providerId, LocalDateTime now) {
        return bookingRepository.findPastBookingsByProviderId(providerId, now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findByClientIdAndStatus(Long clientId, BookingStatus status) {
        return bookingRepository.findByClientIdAndStatusOrderByAvailabilitySlot_StartTimeDesc(clientId, status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingDto> findByProviderIdAndStatus(Long providerId, BookingStatus status) {
        return bookingRepository.findByServiceProviderIdAndStatusOrderByAvailabilitySlot_StartTimeDesc(providerId, status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prenotazione non trovata con ID: " + id));
        
        // Se la prenotazione è completata o annullata, non può essere modificata
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Impossibile modificare una prenotazione completata o annullata");
        }
        
        // Aggiorna lo stato
        booking.setStatus(status);
        
        // Se la prenotazione viene annullata, libera lo slot
        if (status == BookingStatus.CANCELLED) {
            availabilitySlotService.freeSlot(booking.getAvailabilitySlot().getId());
        }
        
        // Salva la prenotazione
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Aggiornato stato della prenotazione con ID: {} a {}", id, status);
        
        return convertToDto(updatedBooking);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BookingDto confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prenotazione non trovata con ID: " + id));
        
        // Utilizza il metodo confirm() dell'entità
        if (!booking.confirm()) {
            throw new RuntimeException("Solo le prenotazioni in attesa possono essere confermate");
        }
        
        // Salva la prenotazione
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Confermata prenotazione con ID: {}", id);
        
        return convertToDto(updatedBooking);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BookingDto completeBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prenotazione non trovata con ID: " + id));
        
        // Utilizza il metodo complete() dell'entità
        if (!booking.complete()) {
            throw new RuntimeException("Solo le prenotazioni confermate possono essere completate");
        }
        
        // Salva la prenotazione
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Completata prenotazione con ID: {}", id);
        
        return convertToDto(updatedBooking);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BookingDto cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prenotazione non trovata con ID: " + id));
        
        // Utilizza il metodo cancel() dell'entità
        if (!booking.cancel()) {
            throw new RuntimeException("Impossibile annullare la prenotazione");
        }
        
        // Salva la prenotazione
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Annullata prenotazione con ID: {}", id);
        
        return convertToDto(updatedBooking);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BookingDto updateNotes(Long id, String notes) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prenotazione non trovata con ID: " + id));
        
        // Aggiorna le note
        booking.setNotes(notes);
        
        // Salva la prenotazione
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Aggiornate note della prenotazione con ID: {}", id);
        
        return convertToDto(updatedBooking);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean markAsReviewed(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prenotazione non trovata con ID: " + id));
        
        // Solo le prenotazioni completate possono essere revisionate
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Solo le prenotazioni completate possono essere revisionate");
        }
        
        // Imposta la prenotazione come revisionata
        booking.setClientReviewed(true);
        
        // Salva la prenotazione
        bookingRepository.save(booking);
        log.info("Marcata come revisionata la prenotazione con ID: {}", id);
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BookingDto convertToDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        
        // Converti le entità in DTO
        UserDto clientDto = userService.convertToDto(booking.getClient());
        ServiceProviderDto providerDto = providerService.convertToDto(booking.getServiceProvider());
        ServiceDto serviceDto = serviceManagementService.convertToDto(booking.getService());
        AvailabilitySlotDto slotDto = availabilitySlotService.convertToDto(booking.getAvailabilitySlot());
        
        // Crea il DTO della prenotazione
        return BookingDto.builder()
                .id(booking.getId())
                .client(clientDto)
                .provider(providerDto)
                .service(serviceDto)
                .availabilitySlot(slotDto)
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().name())
                .notes(booking.getNotes())
                .finalPrice(booking.getFinalPrice())
                .clientReviewed(booking.isClientReviewed())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Booking> findEntityById(Long id) {
        return bookingRepository.findById(id);
    }
}