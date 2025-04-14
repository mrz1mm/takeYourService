package com.springBoot.takeYourService.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Rappresenta una prenotazione nel sistema.
 * Ogni prenotazione è associata a un cliente, un fornitore di servizi, un servizio
 * e uno slot di disponibilità.
 */
@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private AvailabilitySlot availabilitySlot;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Conferma la prenotazione.
     * @return true se la prenotazione è stata confermata con successo
     */
    public boolean confirm() {
        if (this.status != BookingStatus.PENDING) {
            return false;
        }
        this.status = BookingStatus.CONFIRMED;
        return true;
    }

    /**
     * Annulla la prenotazione.
     * @return true se la prenotazione è stata annullata con successo
     */
    public boolean cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            return false;
        }
        this.status = BookingStatus.CANCELLED;
        if (this.availabilitySlot != null) {
            this.availabilitySlot.cancelBooking();
        }
        return true;
    }

    /**
     * Completa la prenotazione.
     * @return true se la prenotazione è stata completata con successo
     */
    public boolean complete() {
        if (this.status != BookingStatus.CONFIRMED) {
            return false;
        }
        this.status = BookingStatus.COMPLETED;
        return true;
    }

    /**
     * Enumeration per lo stato della prenotazione.
     */
    public enum BookingStatus {
        PENDING,     // In attesa di conferma
        CONFIRMED,   // Confermata
        COMPLETED,   // Completata
        CANCELLED    // Annullata
    }
}