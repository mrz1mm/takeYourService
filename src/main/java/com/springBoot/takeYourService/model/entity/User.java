package com.springBoot.takeYourService.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Rappresenta un utente del sistema.
 * Ogni utente può essere un cliente, fornitore di servizi o amministratore, a seconda dei ruoli assegnati.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "username", "email"})
@ToString(exclude = {"password", "roles"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Aggiunge un ruolo all'utente.
     * @param role Il ruolo da aggiungere
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Rimuove un ruolo dall'utente.
     * @param role Il ruolo da rimuovere
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Verifica se l'utente ha un ruolo specifico.
     * @param roleName Nome del ruolo da verificare
     * @return true se l'utente ha il ruolo specificato, false altrimenti
     */
    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}