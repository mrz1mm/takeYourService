package com.springBoot.takeYourService.service.impl;

import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.model.entity.Role;
import com.springBoot.takeYourService.model.entity.Service;
import com.springBoot.takeYourService.model.entity.ServiceProvider;
import com.springBoot.takeYourService.model.entity.User;
import com.springBoot.takeYourService.repository.RoleRepository;
import com.springBoot.takeYourService.repository.ServiceProviderRepository;
import com.springBoot.takeYourService.repository.ServiceRepository;
import com.springBoot.takeYourService.repository.UserRepository;
import com.springBoot.takeYourService.service.ServiceManagementService;
import com.springBoot.takeYourService.service.ServiceProviderService;
import com.springBoot.takeYourService.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementazione del service per la gestione dei fornitori di servizi.
 * Si occupa di tutte le operazioni relative ai fornitori di servizi nel sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceProviderServiceImpl implements ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final ServiceManagementService serviceManagementService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ServiceProviderDto createServiceProvider(Long userId, String description) {
        // Verifica se l'utente esiste
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        // Verifica se l'utente ha già un profilo di fornitore di servizi
        if (serviceProviderRepository.existsByUserId(userId)) {
            throw new RuntimeException("L'utente ha già un profilo di fornitore di servizi");
        }
        
        // Crea il profilo di fornitore di servizi
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setUser(user);
        serviceProvider.setDescription(description);
        serviceProvider.setActive(true);
        
        // Assegna il ruolo PROVIDER all'utente se non ce l'ha già
        Role providerRole = roleRepository.findByName("ROLE_PROVIDER")
                .orElseThrow(() -> new RuntimeException("Ruolo PROVIDER non trovato"));
        
        if (!user.getRoles().contains(providerRole)) {
            user.addRole(providerRole);
            userRepository.save(user);
        }
        
        // Salva il fornitore di servizi
        ServiceProvider savedServiceProvider = serviceProviderRepository.save(serviceProvider);
        log.info("Profilo fornitore di servizi creato per l'utente: {}", user.getUsername());
        
        return convertToDto(savedServiceProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ServiceProviderDto updateServiceProvider(Long id, ServiceProviderDto serviceProviderDto) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + id));
        
        // Aggiorna la descrizione
        serviceProvider.setDescription(serviceProviderDto.getDescription());
        
        // Aggiorna gli orari di lavoro se forniti
        if (serviceProviderDto.getWorkingHours() != null) {
            serviceProvider.setWorkingHours(serviceProviderDto.getWorkingHours());
        }
        
        ServiceProvider updatedServiceProvider = serviceProviderRepository.save(serviceProvider);
        log.info("Fornitore di servizi aggiornato con successo, ID: {}", id);
        
        return convertToDto(updatedServiceProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ServiceProviderDto> findById(Long id) {
        return serviceProviderRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ServiceProviderDto> findByUserId(Long userId) {
        return serviceProviderRepository.findByUserId(userId)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ServiceProviderDto> findAllActive(Pageable pageable) {
        return serviceProviderRepository.findByActiveTrue(pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ServiceProviderDto> findAllByServiceId(Long serviceId, Pageable pageable) {
        return serviceProviderRepository.findAllByServiceId(serviceId, pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ServiceProviderDto> searchByName(String keyword, Pageable pageable) {
        return serviceProviderRepository.searchByName(keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ServiceProviderDto updateWorkingHours(Long id, String workingHours) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + id));
        
        serviceProvider.setWorkingHours(workingHours);
        ServiceProvider updatedServiceProvider = serviceProviderRepository.save(serviceProvider);
        log.info("Orari di lavoro aggiornati per il fornitore di servizi ID: {}", id);
        
        return convertToDto(updatedServiceProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean addService(Long providerId, Long serviceId) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + providerId));
        
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Servizio non trovato con ID: " + serviceId));
        
        // Verifica se il servizio è già offerto dal fornitore
        if (serviceProvider.getServicesOffered().contains(service)) {
            return false;
        }
        
        serviceProvider.addService(service);
        serviceProviderRepository.save(serviceProvider);
        log.info("Servizio {} aggiunto al fornitore di servizi ID: {}", service.getName(), providerId);
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean removeService(Long providerId, Long serviceId) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + providerId));
        
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Servizio non trovato con ID: " + serviceId));
        
        // Verifica se il servizio è offerto dal fornitore
        if (!serviceProvider.getServicesOffered().contains(service)) {
            return false;
        }
        
        serviceProvider.removeService(service);
        serviceProviderRepository.save(serviceProvider);
        log.info("Servizio {} rimosso dal fornitore di servizi ID: {}", service.getName(), providerId);
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deactivateServiceProvider(Long id) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + id));
        
        serviceProvider.setActive(false);
        serviceProviderRepository.save(serviceProvider);
        log.info("Fornitore di servizi disattivato, ID: {}", id);
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean activateServiceProvider(Long id) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornitore di servizi non trovato con ID: " + id));
        
        serviceProvider.setActive(true);
        serviceProviderRepository.save(serviceProvider);
        log.info("Fornitore di servizi attivato, ID: {}", id);
        
        return true;
    }

    /**
     * {@inheritDoc}
     * Converte un'entità ServiceProvider in un DTO ServiceProviderDto.
     */
    @Override
    public ServiceProviderDto convertToDto(ServiceProvider serviceProvider) {
        if (serviceProvider == null) {
            return null;
        }
        
        UserDto userDto = userService.convertToDto(serviceProvider.getUser());
        
        List<ServiceDto> serviceDtos = new ArrayList<>();
        Set<Long> serviceIds = new HashSet<>();
        
        for (Service service : serviceProvider.getServicesOffered()) {
            serviceDtos.add(serviceManagementService.convertToDto(service));
            serviceIds.add(service.getId());
        }
        
        return ServiceProviderDto.builder()
                .id(serviceProvider.getId())
                .user(userDto)
                .description(serviceProvider.getDescription())
                .workingHours(serviceProvider.getWorkingHours())
                .serviceIds(serviceIds)
                .services(serviceDtos)
                .active(serviceProvider.isActive())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ServiceProvider> findEntityById(Long id) {
        return serviceProviderRepository.findById(id);
    }
}