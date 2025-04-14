package com.springBoot.takeYourService.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Rappresenta uno slot di disponibilità di un fornitore di servizi.
 * Ogni slot ha un orario di inizio e fine e può essere prenotato.
 */
@Entity
@Table(name = "availability_slots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "serviceProvider", "startTime", "endTime"})
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_booked", nullable = false)
    private boolean booked = false;

    @OneToOne(mappedBy = "availabilitySlot", cascade = CascadeType.ALL)
    private Booking booking;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Verifica se lo slot è disponibile in un dato intervallo di tempo.
     * @param start Orario di inizio della verifica
     * @param end Orario di fine della verifica
     * @return true se lo slot è disponibile, false altrimenti
     */
    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        if (this.booked) {
            return false;
        }
        
        // Controlla se l'intervallo si sovrappone con questo slot
        return !this.startTime.isAfter(end) && !this.endTime.isBefore(start);
    }
    
    /**
     * Prenota lo slot di disponibilità.
     * @return true se lo slot è stato prenotato con successo, false se era già prenotato
     */
    public boolean book() {
        if (this.booked) {
            return false;
        }
        this.booked = true;
        return true;
    }
    
    /**
     * Annulla la prenotazione dello slot.
     * @return true se l'annullamento è avvenuto con successo, false se lo slot non era prenotato
     */
    public boolean cancelBooking() {
        if (!this.booked) {
            return false;
        }
        this.booked = false;
        this.booking = null;
        return true;
    }
}