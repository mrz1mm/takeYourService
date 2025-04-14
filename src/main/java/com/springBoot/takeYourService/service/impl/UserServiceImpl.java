package com.springBoot.takeYourService.service.impl;

import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.model.dto.UserRegistrationDto;
import com.springBoot.takeYourService.model.entity.Role;
import com.springBoot.takeYourService.model.entity.ServiceProvider;
import com.springBoot.takeYourService.model.entity.User;
import com.springBoot.takeYourService.repository.RoleRepository;
import com.springBoot.takeYourService.repository.ServiceProviderRepository;
import com.springBoot.takeYourService.repository.UserRepository;
import com.springBoot.takeYourService.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementazione del service per la gestione degli utenti.
 * Si occupa di tutte le operazioni relative agli utenti del sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * {@inheritDoc}
     * Registra un nuovo utente nel sistema, con validazione dei dati e
     * assegnazione automatica del ruolo CLIENT.
     * Se richiesto, crea anche un profilo di fornitore di servizi.
     */
    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        // Validazione
        if (isUsernameAlreadyInUse(registrationDto.getUsername())) {
            throw new RuntimeException("Nome utente già in uso");
        }
        
        if (isEmailAlreadyInUse(registrationDto.getEmail())) {
            throw new RuntimeException("Email già in uso");
        }
        
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new RuntimeException("Le password non corrispondono");
        }
        
        // Creazione dell'utente
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setActive(true);
        
        // Assegnazione del ruolo CLIENT come default
        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Ruolo CLIENT non trovato"));
        user.getRoles().add(clientRole);
        
        // Salva l'utente
        User savedUser = userRepository.save(user);
        
        // Se richiesto, crea anche un profilo di fornitore di servizi
        if (registrationDto.isAsServiceProvider()) {
            // Assegnazione del ruolo PROVIDER
            Role providerRole = roleRepository.findByName("ROLE_PROVIDER")
                    .orElseThrow(() -> new RuntimeException("Ruolo PROVIDER non trovato"));
            savedUser.getRoles().add(providerRole);
            userRepository.save(savedUser);
            
            // Creazione del profilo di fornitore di servizi
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setUser(savedUser);
            serviceProvider.setDescription(registrationDto.getProviderDescription());
            serviceProvider.setActive(true);
            serviceProviderRepository.save(serviceProvider);
        }
        
        log.info("Utente registrato con successo: {}", savedUser.getUsername());
        return convertToDto(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Ottiene l'utente attualmente autenticato dal contesto di sicurezza.
     */
    @Override
    public Optional<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + id));
        
        // Verifica se l'email è cambiata e se è già usata
        if (!user.getEmail().equals(userDto.getEmail()) && 
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email già in uso");
        }
        
        // Aggiorna i dati dell'utente
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setActive(userDto.isActive());
        
        User savedUser = userRepository.save(user);
        log.info("Utente aggiornato con successo: {}", savedUser.getUsername());
        
        return convertToDto(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con ID: " + userId));
        
        // Verifica la password attuale
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        // Aggiorna la password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password cambiata con successo per l'utente: {}", user.getUsername());
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUsernameAlreadyInUse(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmailAlreadyInUse(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + id));
        
        user.setActive(false);
        userRepository.save(user);
        log.info("Utente disattivato con successo: {}", user.getUsername());
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + id));
        
        user.setActive(true);
        userRepository.save(user);
        log.info("Utente attivato con successo: {}", user.getUsername());
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean addRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Ruolo non trovato: " + roleName));
        
        // Verifica se l'utente ha già il ruolo
        if (user.getRoles().contains(role)) {
            return false;
        }
        
        user.addRole(role);
        userRepository.save(user);
        log.info("Ruolo {} aggiunto all'utente: {}", roleName, user.getUsername());
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Ruolo non trovato: " + roleName));
        
        // Verifica se l'utente ha il ruolo
        if (!user.getRoles().contains(role)) {
            return false;
        }
        
        user.removeRole(role);
        userRepository.save(user);
        log.info("Ruolo {} rimosso dall'utente: {}", roleName, user.getUsername());
        
        return true;
    }

    /**
     * {@inheritDoc}
     * Converte un'entità User in un DTO UserDto.
     */
    @Override
    public UserDto convertToDto(User user) {
        if (user == null) {
            return null;
        }
        
        Set<String> roleNames = new HashSet<>();
        user.getRoles().forEach(role -> roleNames.add(role.getName()));
        
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roleNames(roleNames)
                .active(user.isActive())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> findEntityById(Long id) {
        return userRepository.findById(id);
    }
}