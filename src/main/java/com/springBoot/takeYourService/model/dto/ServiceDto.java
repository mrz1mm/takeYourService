package com.springBoot.takeYourService.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO per la rappresentazione dei servizi offerti.
 * Utilizzato per trasferire i dati dei servizi tra i vari strati dell'applicazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {
    
    private Long id;
    
    @NotEmpty(message = "Il nome del servizio è obbligatorio")
    @Size(min = 3, max = 100, message = "Il nome del servizio deve essere compreso tra {min} e {max} caratteri")
    private String name;
    
    @Size(max = 1000, message = "La descrizione non può superare {max} caratteri")
    private String description;
    
    @NotNull(message = "La durata del servizio è obbligatoria")
    @Min(value = 5, message = "La durata minima del servizio è di {value} minuti")
    private Integer durationMinutes;
    
    @NotNull(message = "Il prezzo è obbligatorio")
    @Min(value = 0, message = "Il prezzo non può essere negativo")
    private BigDecimal price;
    
    private boolean active;
    
    /**
     * Ottiene la durata formattata del servizio (ore e minuti).
     * @return La durata formattata
     */
    public String getFormattedDuration() {
        if (durationMinutes == null) {
            return "N/A";
        }
        
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        
        if (hours > 0) {
            return String.format("%d ora%s %d min", hours, hours > 1 ? "e" : "", minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }
    
    /**
     * Ottiene il prezzo formattato del servizio.
     * @return Il prezzo formattato
     */
    public String getFormattedPrice() {
        if (price == null) {
            return "N/A";
        }
        
        return String.format("€ %.2f", price);
    }
}