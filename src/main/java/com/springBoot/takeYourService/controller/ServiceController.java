package com.springBoot.takeYourService.controller;

import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.service.ServiceManagementService;
import com.springBoot.takeYourService.service.ServiceProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller per la gestione dei servizi offerti.
 * Permette la visualizzazione e la gestione dei servizi.
 */
@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceManagementService serviceManagementService;
    private final ServiceProviderService serviceProviderService;

    /**
     * Visualizza tutti i servizi attivi disponibili.
     * 
     * @param page Numero di pagina
     * @param size Dimensione della pagina
     * @param keyword Termine di ricerca
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping
    public String listServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ServiceDto> services;
        
        if (keyword != null && !keyword.isEmpty()) {
            services = serviceManagementService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            services = serviceManagementService.findAllActive(pageable);
        }
        
        model.addAttribute("services", services);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", services.getTotalPages());
        
        return "services/list";
    }

    /**
     * Visualizza i dettagli di un servizio.
     * 
     * @param id ID del servizio
     * @param page Numero di pagina per i fornitori
     * @param size Dimensione della pagina per i fornitori
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/view/{id}")
    public String viewService(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        Optional<ServiceDto> serviceOpt = serviceManagementService.findById(id);
        if (serviceOpt.isEmpty()) {
            return "redirect:/services?error=Servizio non trovato";
        }
        
        ServiceDto service = serviceOpt.get();
        model.addAttribute("service", service);
        
        // Ottieni i fornitori che offrono questo servizio
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceProviderDto> providers = serviceProviderService.findAllByServiceId(id, pageable);
        
        model.addAttribute("providers", providers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", providers.getTotalPages());
        
        return "services/view";
    }

    /**
     * Gestione dei servizi (solo admin).
     * 
     * @param page Numero di pagina
     * @param size Dimensione della pagina
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ServiceDto> services = serviceManagementService.findAll(pageable);
        
        model.addAttribute("services", services);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", services.getTotalPages());
        
        return "services/manage";
    }

    /**
     * Visualizza il form per creare un nuovo servizio (solo admin).
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createServiceForm(Model model) {
        model.addAttribute("service", new ServiceDto());
        return "services/form";
    }

    /**
     * Salva un nuovo servizio (solo admin).
     * 
     * @param serviceDto Dati del servizio
     * @param result Risultato della validazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveService(
            @Valid @ModelAttribute("service") ServiceDto serviceDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "services/form";
        }
        
        try {
            serviceManagementService.createService(serviceDto);
            redirectAttributes.addFlashAttribute("success", "Servizio creato con successo.");
        } catch (Exception e) {
            log.error("Errore durante la creazione del servizio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante la creazione del servizio.");
        }
        
        return "redirect:/services/manage";
    }

    /**
     * Visualizza il form per modificare un servizio esistente (solo admin).
     * 
     * @param id ID del servizio
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editServiceForm(@PathVariable Long id, Model model) {
        Optional<ServiceDto> serviceOpt = serviceManagementService.findById(id);
        if (serviceOpt.isEmpty()) {
            return "redirect:/services/manage?error=Servizio non trovato";
        }
        
        model.addAttribute("service", serviceOpt.get());
        model.addAttribute("isEdit", true);
        
        return "services/form";
    }

    /**
     * Aggiorna un servizio esistente (solo admin).
     * 
     * @param id ID del servizio
     * @param serviceDto Dati aggiornati del servizio
     * @param result Risultato della validazione
     * @param model Model per la vista
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateService(
            @PathVariable Long id,
            @Valid @ModelAttribute("service") ServiceDto serviceDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "services/form";
        }
        
        try {
            serviceManagementService.updateService(id, serviceDto);
            redirectAttributes.addFlashAttribute("success", "Servizio aggiornato con successo.");
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento del servizio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante l'aggiornamento del servizio.");
        }
        
        return "redirect:/services/manage";
    }

    /**
     * Attiva o disattiva un servizio (solo admin).
     * 
     * @param id ID del servizio
     * @param activate Se true, attiva il servizio, altrimenti lo disattiva
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/toggle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleServiceStatus(
            @PathVariable Long id,
            @RequestParam boolean activate,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (activate) {
                serviceManagementService.activateService(id);
                redirectAttributes.addFlashAttribute("success", "Servizio attivato con successo.");
            } else {
                serviceManagementService.deactivateService(id);
                redirectAttributes.addFlashAttribute("success", "Servizio disattivato con successo.");
            }
        } catch (Exception e) {
            log.error("Errore durante il cambio di stato del servizio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante il cambio di stato del servizio.");
        }
        
        return "redirect:/services/manage";
    }

    /**
     * Elimina un servizio (solo admin).
     * 
     * @param id ID del servizio
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = serviceManagementService.deleteService(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Servizio eliminato con successo.");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                        "Impossibile eliminare il servizio poiché è associato a prenotazioni esistenti.");
            }
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione del servizio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante l'eliminazione del servizio.");
        }
        
        return "redirect:/services/manage";
    }
}