package com.springBoot.takeYourService.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO per la rappresentazione dei fornitori di servizi.
 * Utilizzato per trasferire i dati dei fornitori di servizi tra i vari strati dell'applicazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderDto {
    
    private Long id;
    
    private UserDto user;
    
    @NotEmpty(message = "La descrizione è obbligatoria")
    @Size(max = 2000, message = "La descrizione non può superare {max} caratteri")
    private String description;
    
    /**
     * Orari di lavoro in formato JSON.
     * Esempio: 
     * {
     *   "monday": [{"start": "09:00", "end": "13:00"}, {"start": "14:00", "end": "18:00"}],
     *   "tuesday": [{"start": "09:00", "end": "13:00"}, {"start": "14:00", "end": "18:00"}],
     *   ...
     * }
     */
    private String workingHours;
    
    /**
     * IDs dei servizi offerti dal fornitore.
     */
    private Set<Long> serviceIds = new HashSet<>();
    
    /**
     * Lista dei servizi offerti dal fornitore.
     */
    private List<ServiceDto> services = new ArrayList<>();
    
    private boolean active;
    
    /**
     * Ottiene il nome completo del fornitore di servizi.
     * @return Nome e cognome del fornitore
     */
    public String getProviderName() {
        return user != null ? user.getFullName() : "";
    }
    
    /**
     * Ottiene l'ID dell'utente associato al fornitore di servizi.
     * @return ID dell'utente o null se non presente
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * Verifica se il fornitore offre un determinato servizio.
     * @param serviceId ID del servizio
     * @return true se il fornitore offre il servizio, false altrimenti
     */
    public boolean offersService(Long serviceId) {
        return serviceIds != null && serviceIds.contains(serviceId);
    }
}