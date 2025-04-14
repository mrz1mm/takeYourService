package com.springBoot.takeYourService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO per la rappresentazione delle prenotazioni.
 * Utilizzato per trasferire i dati delle prenotazioni tra i vari strati dell'applicazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    
    private Long id;
    
    private UserDto client;
    
    private ServiceProviderDto provider;
    
    private ServiceDto service;
    
    private AvailabilitySlotDto availabilitySlot;
    
    private LocalDateTime bookingDate;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String status;
    
    private String notes;
    
    private BigDecimal finalPrice;
    
    private Boolean clientReviewed;
    
    /**
     * Ottiene il nome del cliente che ha effettuato la prenotazione.
     * @return Nome del cliente
     */
    public String getClientName() {
        return client != null ? client.getFullName() : "N/A";
    }
    
    /**
     * Ottiene il nome del fornitore di servizi.
     * @return Nome del fornitore
     */
    public String getProviderName() {
        return provider != null ? provider.getProviderName() : "N/A";
    }
    
    /**
     * Ottiene il nome del servizio prenotato.
     * @return Nome del servizio
     */
    public String getServiceName() {
        return service != null ? service.getName() : "N/A";
    }
    
    /**
     * Ottiene la data formattata della prenotazione.
     * @return Data formattata
     */
    public String getFormattedDate() {
        LocalDateTime time = startTime != null ? startTime : 
                             (availabilitySlot != null ? availabilitySlot.getStartTime() : null);
        
        if (time == null) {
            return "N/A";
        }
        
        return time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    /**
     * Ottiene l'ora formattata della prenotazione.
     * @return Ora formattata
     */
    public String getFormattedTime() {
        LocalDateTime start = startTime != null ? startTime : 
                              (availabilitySlot != null ? availabilitySlot.getStartTime() : null);
        LocalDateTime end = endTime != null ? endTime : 
                            (availabilitySlot != null ? availabilitySlot.getEndTime() : null);
        
        if (start == null || end == null) {
            return "N/A";
        }
        
        return String.format("%s - %s", 
            start.format(DateTimeFormatter.ofPattern("HH:mm")),
            end.format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    
    /**
     * Ottiene il prezzo finale formattato.
     * @return Prezzo formattato
     */
    public String getFormattedPrice() {
        if (finalPrice == null) {
            return service != null ? service.getFormattedPrice() : "N/A";
        }
        
        return String.format("€ %.2f", finalPrice);
    }
    
    /**
     * Verifica se la prenotazione è in stato di attesa.
     * @return true se la prenotazione è in attesa, false altrimenti
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }
    
    /**
     * Verifica se la prenotazione è stata confermata.
     * @return true se la prenotazione è confermata, false altrimenti
     */
    public boolean isConfirmed() {
        return "CONFIRMED".equalsIgnoreCase(status);
    }
    
    /**
     * Verifica se la prenotazione è stata completata.
     * @return true se la prenotazione è completata, false altrimenti
     */
    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }
    
    /**
     * Verifica se la prenotazione è stata annullata.
     * @return true se la prenotazione è annullata, false altrimenti
     */
    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }
    
    /**
     * Verifica se la prenotazione è revisionabile dal cliente.
     * @return true se la prenotazione è revisionabile, false altrimenti
     */
    public boolean isReviewable() {
        return isCompleted() && (clientReviewed == null || !clientReviewed);
    }
}