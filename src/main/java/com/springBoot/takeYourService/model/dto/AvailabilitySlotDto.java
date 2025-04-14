package com.springBoot.takeYourService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO per la rappresentazione degli slot di disponibilità.
 * Utilizzato per trasferire i dati degli slot di disponibilità tra i vari strati dell'applicazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySlotDto {
    
    private Long id;
    
    private Long providerId;
    
    private String providerName;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private boolean booked;
    
    /**
     * Ottiene la data formattata dello slot.
     * @return La data formattata
     */
    public String getFormattedDate() {
        if (startTime == null) {
            return "N/A";
        }
        
        return startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    /**
     * Ottiene l'ora di inizio formattata dello slot.
     * @return L'ora di inizio formattata
     */
    public String getFormattedStartTime() {
        if (startTime == null) {
            return "N/A";
        }
        
        return startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    /**
     * Ottiene l'ora di fine formattata dello slot.
     * @return L'ora di fine formattata
     */
    public String getFormattedEndTime() {
        if (endTime == null) {
            return "N/A";
        }
        
        return endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    /**
     * Ottiene una rappresentazione completa dell'intervallo di tempo dello slot.
     * @return Rappresentazione formattata dell'intervallo di tempo
     */
    public String getFormattedTimeRange() {
        if (startTime == null || endTime == null) {
            return "N/A";
        }
        
        return String.format("%s - %s", 
            startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    
    /**
     * Verifica se lo slot è disponibile (non prenotato).
     * @return true se lo slot è disponibile, false altrimenti
     */
    public boolean isAvailable() {
        return !booked;
    }
}