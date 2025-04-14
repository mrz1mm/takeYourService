package com.springBoot.takeYourService.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springBoot.takeYourService.model.dto.AvailabilitySlotDto;
import com.springBoot.takeYourService.model.entity.AvailabilitySlot;
import com.springBoot.takeYourService.model.entity.ServiceProvider;
import com.springBoot.takeYourService.repository.AvailabilitySlotRepository;
import com.springBoot.takeYourService.repository.BookingRepository;
import com.springBoot.takeYourService.repository.ServiceProviderRepository;
import com.springBoot.takeYourService.service.AvailabilitySlotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio per la gestione degli slot di disponibilità.
 * Si occupa di tutte le operazioni relative agli slot di disponibilità nel sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilitySlotServiceImpl implements AvailabilitySlotService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AvailabilitySlotDto createSlot(Long providerId, LocalDateTime startTime, LocalDateTime endTime) {
        // Verifica se il fornitore di servizi esiste
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + providerId));
        
        // Verifica se lo slot si sovrappone con uno già esistente
        if (availabilitySlotRepository.existsOverlappingSlot(providerId, startTime, endTime)) {
            throw new RuntimeException("Lo slot si sovrappone con uno già esistente");
        }
        
        // Crea lo slot
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setServiceProvider(provider);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setBooked(false);
        
        // Salva lo slot
        AvailabilitySlot savedSlot = availabilitySlotRepository.save(slot);
        log.info("Slot di disponibilità creato per il fornitore ID: {}, da {} a {}", 
                providerId, startTime, endTime);
        
        return convertToDto(savedSlot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<AvailabilitySlotDto> createMultipleSlots(
            Long providerId,
            LocalDate date,
            int startHour,
            int endHour,
            int slotDuration,
            int breakDuration) {
        
        // Verifica se il fornitore di servizi esiste
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + providerId));
        
        List<AvailabilitySlot> createdSlots = new ArrayList<>();
        
        // Calcola il numero di slot che è possibile creare
        int totalMinutes = (endHour - startHour) * 60;
        int slotWithBreak = slotDuration + breakDuration;
        int numberOfSlots = totalMinutes / slotWithBreak;
        
        // Crea gli slot
        LocalDateTime currentStart = date.atTime(startHour, 0);
        
        for (int i = 0; i < numberOfSlots; i++) {
            LocalDateTime slotStart = currentStart;
            LocalDateTime slotEnd = slotStart.plusMinutes(slotDuration);
            
            // Verifica se lo slot si sovrappone con uno già esistente
            if (!availabilitySlotRepository.existsOverlappingSlot(providerId, slotStart, slotEnd)) {
                // Crea lo slot
                AvailabilitySlot slot = new AvailabilitySlot();
                slot.setServiceProvider(provider);
                slot.setStartTime(slotStart);
                slot.setEndTime(slotEnd);
                slot.setBooked(false);
                
                // Aggiungi lo slot alla lista
                createdSlots.add(slot);
            }
            
            // Aggiorna l'orario di inizio per il prossimo slot
            currentStart = slotEnd.plusMinutes(breakDuration);
        }
        
        // Salva tutti gli slot creati
        List<AvailabilitySlot> savedSlots = availabilitySlotRepository.saveAll(createdSlots);
        log.info("Creati {} slot di disponibilità per il fornitore ID: {} il giorno {}", 
                savedSlots.size(), providerId, date);
        
        // Converti gli slot in DTO
        return savedSlots.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AvailabilitySlotDto> findById(Long id) {
        return availabilitySlotRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AvailabilitySlotDto> findAllByProviderId(Long providerId) {
        return availabilitySlotRepository.findByServiceProviderIdOrderByStartTimeAsc(providerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AvailabilitySlotDto> findAvailableSlots(Long providerId, LocalDateTime startDate, LocalDateTime endDate) {
        return availabilitySlotRepository.findAvailableSlotsByProviderAndDateRange(providerId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AvailabilitySlotDto> findAllSlotsByDateRange(Long providerId, LocalDateTime startDate, LocalDateTime endDate) {
        return availabilitySlotRepository.findByServiceProviderIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
                providerId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<AvailabilitySlotDto> generateSlotsFromWorkingHours(Long providerId, LocalDate startDate, LocalDate endDate, int slotDuration) {
        // Verifica se il fornitore di servizi esiste
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + providerId));
        
        // Verifica se il fornitore ha orari di lavoro definiti
        if (provider.getWorkingHours() == null || provider.getWorkingHours().isEmpty()) {
            throw new RuntimeException("Il fornitore non ha orari di lavoro definiti");
        }
        
        List<AvailabilitySlot> createdSlots = new ArrayList<>();
        
        try {
            // Parsing degli orari di lavoro dal formato JSON
            JsonNode workingHours = objectMapper.readTree(provider.getWorkingHours());
            
            // Per ogni giorno tra startDate e endDate
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                String dayName = dayOfWeek.toString().toLowerCase();
                
                // Verifica se il fornitore lavora in questo giorno della settimana
                if (workingHours.has(dayName)) {
                    JsonNode daySchedule = workingHours.get(dayName);
                    
                    // Per ogni intervallo di ore lavorative in questo giorno
                    for (Iterator<JsonNode> it = daySchedule.elements(); it.hasNext(); ) {
                        JsonNode timeSlot = it.next();
                        
                        // Ottieni l'ora di inizio e fine
                        LocalTime startTime = LocalTime.parse(timeSlot.get("start").asText(), DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime endTime = LocalTime.parse(timeSlot.get("end").asText(), DateTimeFormatter.ofPattern("HH:mm"));
                        
                        // Calcola il numero di slot che è possibile creare
                        int totalMinutes = (endTime.getHour() * 60 + endTime.getMinute()) - 
                                          (startTime.getHour() * 60 + startTime.getMinute());
                        int numberOfSlots = totalMinutes / slotDuration;
                        
                        // Crea gli slot
                        LocalDateTime currentStart = date.atTime(startTime);
                        
                        for (int i = 0; i < numberOfSlots; i++) {
                            LocalDateTime slotStart = currentStart;
                            LocalDateTime slotEnd = slotStart.plusMinutes(slotDuration);
                            
                            // Verifica se lo slot si sovrappone con uno già esistente
                            if (!availabilitySlotRepository.existsOverlappingSlot(providerId, slotStart, slotEnd)) {
                                // Crea lo slot
                                AvailabilitySlot slot = new AvailabilitySlot();
                                slot.setServiceProvider(provider);
                                slot.setStartTime(slotStart);
                                slot.setEndTime(slotEnd);
                                slot.setBooked(false);
                                
                                // Aggiungi lo slot alla lista
                                createdSlots.add(slot);
                            }
                            
                            // Aggiorna l'orario di inizio per il prossimo slot
                            currentStart = slotEnd;
                        }
                    }
                }
            }
            
            // Salva tutti gli slot creati
            List<AvailabilitySlot> savedSlots = availabilitySlotRepository.saveAll(createdSlots);
            log.info("Generati {} slot di disponibilità per il fornitore ID: {} dal {} al {}", 
                    savedSlots.size(), providerId, startDate, endDate);
            
            // Converti gli slot in DTO
            return savedSlots.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
        } catch (JsonProcessingException e) {
            log.error("Errore nel parsing degli orari di lavoro: {}", e.getMessage());
            throw new RuntimeException("Errore nel parsing degli orari di lavoro", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deleteSlot(Long id) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot di disponibilità non trovato con ID: " + id));
        
        // Verifica se lo slot è già prenotato
        if (slot.isBooked()) {
            throw new RuntimeException("Impossibile eliminare uno slot già prenotato");
        }
        
        // Verifica se lo slot è associato a prenotazioni
        if (bookingRepository.existsByAvailabilitySlotId(id)) {
            throw new RuntimeException("Impossibile eliminare uno slot associato a prenotazioni");
        }
        
        try {
            availabilitySlotRepository.delete(slot);
            log.info("Slot di disponibilità eliminato con successo, ID: {}", id);
            return true;
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione dello slot con ID {}: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean bookSlot(Long slotId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot di disponibilità non trovato con ID: " + slotId));
        
        // Tenta di prenotare lo slot
        boolean success = slot.book();
        
        if (success) {
            availabilitySlotRepository.save(slot);
            log.info("Slot di disponibilità prenotato con successo, ID: {}", slotId);
        } else {
            log.warn("Impossibile prenotare lo slot con ID {}: già prenotato", slotId);
        }
        
        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean freeSlot(Long slotId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot di disponibilità non trovato con ID: " + slotId));
        
        // Verifica se lo slot è associato a prenotazioni
        if (bookingRepository.existsByAvailabilitySlotId(slotId)) {
            log.warn("Impossibile liberare lo slot con ID {}: associato a prenotazioni", slotId);
            return false;
        }
        
        // Tenta di liberare lo slot
        boolean success = slot.cancelBooking();
        
        if (success) {
            availabilitySlotRepository.save(slot);
            log.info("Slot di disponibilità liberato con successo, ID: {}", slotId);
        } else {
            log.warn("Impossibile liberare lo slot con ID {}: non era prenotato", slotId);
        }
        
        return success;
    }

    /**
     * {@inheritDoc}
     * Converte un'entità AvailabilitySlot in un DTO AvailabilitySlotDto.
     */
    @Override
    public AvailabilitySlotDto convertToDto(AvailabilitySlot slot) {
        if (slot == null) {
            return null;
        }
        
        String providerName = "";
        if (slot.getServiceProvider() != null && slot.getServiceProvider().getUser() != null) {
            providerName = slot.getServiceProvider().getUser().getFirstName() + " " + 
                          slot.getServiceProvider().getUser().getLastName();
        }
        
        return AvailabilitySlotDto.builder()
                .id(slot.getId())
                .providerId(slot.getServiceProvider().getId())
                .providerName(providerName)
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .booked(slot.isBooked())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AvailabilitySlot> findEntityById(Long id) {
        return availabilitySlotRepository.findById(id);
    }
}
