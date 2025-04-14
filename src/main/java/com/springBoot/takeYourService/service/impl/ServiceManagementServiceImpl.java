package com.springBoot.takeYourService.service.impl;

import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.entity.Service;
import com.springBoot.takeYourService.repository.BookingRepository;
import com.springBoot.takeYourService.repository.ServiceRepository;
import com.springBoot.takeYourService.service.ServiceManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service as SpringService;

/**
 * Implementazione del service per la gestione dei servizi offerti.
 * Si occupa di tutte le operazioni relative ai servizi nel sistema.
 */
@SpringService
@RequiredArgsConstructor
@Slf4j
public class ServiceManagementServiceImpl implements ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ServiceDto createService(ServiceDto serviceDto) {
        Service service = convertToEntity(serviceDto);
        service.setActive(true);
        
        Service savedService = serviceRepository.save(service);
        log.info("Servizio creato con successo: {}", savedService.getName());
        
        return convertToDto(savedService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ServiceDto updateService(Long id, ServiceDto serviceDto) {
        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servizio non trovato con ID: " + id));
        
        // Aggiorna i dati del servizio
        existingService.setName(serviceDto.getName());
        existingService.setDescription(serviceDto.getDescription());
        existingService.setDurationMinutes(serviceDto.getDurationMinutes());
        existingService.setPrice(serviceDto.getPrice());
        
        Service updatedService = serviceRepository.save(existingService);
        log.info("Servizio aggiornato con successo: {}", updatedService.getName());
        
        return convertToDto(updatedService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ServiceDto> findById(Long id) {
        return serviceRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceDto> findAllActive() {
        return serviceRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ServiceDto> findAllActive(Pageable pageable) {
        return serviceRepository.findByActiveTrue(pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceDto> findAll() {
        return serviceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ServiceDto> findAll(Pageable pageable) {
        return serviceRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ServiceDto> searchByName(String name, Pageable pageable) {
        return serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceDto> findAllByProviderId(Long providerId) {
        return serviceRepository.findAllByServiceProviderId(providerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deactivateService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servizio non trovato con ID: " + id));
        
        service.setActive(false);
        serviceRepository.save(service);
        log.info("Servizio disattivato con successo: {}", service.getName());
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean activateService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servizio non trovato con ID: " + id));
        
        service.setActive(true);
        serviceRepository.save(service);
        log.info("Servizio attivato con successo: {}", service.getName());
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deleteService(Long id) {
        // Verifica se il servizio è usato in prenotazioni esistenti
        if (bookingRepository.findAll().stream()
                .anyMatch(booking -> booking.getService().getId().equals(id))) {
            log.warn("Impossibile eliminare il servizio con ID {}: è associato a prenotazioni esistenti", id);
            return false;
        }
        
        try {
            serviceRepository.deleteById(id);
            log.info("Servizio eliminato con successo, ID: {}", id);
            return true;
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione del servizio con ID {}: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * Converte un'entità Service in un DTO ServiceDto.
     */
    @Override
    public ServiceDto convertToDto(Service service) {
        if (service == null) {
            return null;
        }
        
        return ServiceDto.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .active(service.isActive())
                .build();
    }

    /**
     * {@inheritDoc}
     * Converte un DTO ServiceDto in un'entità Service.
     */
    @Override
    public Service convertToEntity(ServiceDto serviceDto) {
        if (serviceDto == null) {
            return null;
        }
        
        Service service = new Service();
        service.setId(serviceDto.getId());
        service.setName(serviceDto.getName());
        service.setDescription(serviceDto.getDescription());
        service.setDurationMinutes(serviceDto.getDurationMinutes());
        service.setPrice(serviceDto.getPrice());
        service.setActive(serviceDto.isActive());
        
        return service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Service> findEntityById(Long id) {
        return serviceRepository.findById(id);
    }
}