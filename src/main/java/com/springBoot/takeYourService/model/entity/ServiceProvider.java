package com.springBoot.takeYourService.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Rappresenta un fornitore di servizi nel sistema.
 * Ogni fornitore è associato a un utente e può offrire diversi servizi.
 * I fornitori definiscono i propri orari di lavoro e la loro disponibilità per gli appuntamenti.
 */
@Entity
@Table(name = "service_providers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "user"})
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Campo JSON che memorizza gli orari di lavoro del fornitore.
     * Formato esempio: 
     * {
     *   "monday": [{"start": "09:00", "end": "13:00"}, {"start": "14:00", "end": "18:00"}],
     *   "tuesday": [{"start": "09:00", "end": "13:00"}, {"start": "14:00", "end": "18:00"}],
     *   ...
     * }
     */
    @Column(name = "working_hours", columnDefinition = "TEXT")
    private String workingHours;

    @ManyToMany
    @JoinTable(
        name = "provider_services",
        joinColumns = @JoinColumn(name = "provider_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<Service> servicesOffered = new HashSet<>();

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<AvailabilitySlot> availabilitySlots = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Aggiunge un servizio all'elenco dei servizi offerti dal fornitore.
     * @param service Il servizio da aggiungere
     */
    public void addService(Service service) {
        this.servicesOffered.add(service);
        service.getServiceProviders().add(this);
    }

    /**
     * Rimuove un servizio dall'elenco dei servizi offerti dal fornitore.
     * @param service Il servizio da rimuovere
     */
    public void removeService(Service service) {
        this.servicesOffered.remove(service);
        service.getServiceProviders().remove(this);
    }

    /**
     * Aggiunge uno slot di disponibilità al fornitore.
     * @param slot Lo slot di disponibilità da aggiungere
     */
    public void addAvailabilitySlot(AvailabilitySlot slot) {
        this.availabilitySlots.add(slot);
        slot.setServiceProvider(this);
    }
}