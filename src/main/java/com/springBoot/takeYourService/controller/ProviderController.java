package com.springBoot.takeYourService.controller;

import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.service.ServiceManagementService;
import com.springBoot.takeYourService.service.ServiceProviderService;
import com.springBoot.takeYourService.service.UserService;
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
 * Controller per la gestione dei fornitori di servizi.
 * Gestisce la visualizzazione e la configurazione dei profili dei fornitori.
 */
@Controller
@RequestMapping("/providers")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {

    private final ServiceProviderService serviceProviderService;
    private final UserService userService;
    private final ServiceManagementService serviceManagementService;

    /**
     * Visualizza tutti i fornitori di servizi attivi.
     * 
     * @param page Numero di pagina
     * @param size Dimensione della pagina
     * @param keyword Termine di ricerca
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping
    public String listProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceProviderDto> providers;
        
        if (keyword != null && !keyword.isEmpty()) {
            providers = serviceProviderService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            providers = serviceProviderService.findAllActive(pageable);
        }
        
        model.addAttribute("providers", providers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", providers.getTotalPages());
        
        return "providers/list";
    }

    /**
     * Visualizza i dettagli di un fornitore di servizi.
     * 
     * @param id ID del fornitore
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/view/{id}")
    public String viewProvider(@PathVariable Long id, Model model) {
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findById(id);
        if (providerOpt.isEmpty()) {
            return "redirect:/providers?error=Fornitore di servizi non trovato";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        model.addAttribute("provider", provider);
        
        return "providers/view";
    }

    /**
     * Configura il profilo di un fornitore per l'utente corrente.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/setup")
    @PreAuthorize("hasRole('PROVIDER')")
    public String setupProvider(Model model) {
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        // Verifica se l'utente ha già un profilo fornitore
        Optional<ServiceProviderDto> existingProviderOpt = serviceProviderService.findByUserId(currentUser.getId());
        if (existingProviderOpt.isPresent()) {
            return "redirect:/provider/profile";
        }
        
        // Crea un nuovo DTO per il form
        ServiceProviderDto newProvider = new ServiceProviderDto();
        model.addAttribute("provider", newProvider);
        model.addAttribute("user", currentUser);
        
        return "providers/setup";
    }

    /**
     * Salva il nuovo profilo del fornitore.
     * 
     * @param providerDto Dati del profilo
     * @param result Risultato della validazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/setup")
    @PreAuthorize("hasRole('PROVIDER')")
    public String saveProviderProfile(
            @Valid @ModelAttribute("provider") ServiceProviderDto providerDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "providers/setup";
        }
        
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        try {
            // Crea il profilo del fornitore
            serviceProviderService.createServiceProvider(currentUser.getId(), providerDto.getDescription());
            redirectAttributes.addFlashAttribute("success", "Profilo fornitore creato con successo.");
            return "redirect:/provider/profile";
        } catch (Exception e) {
            log.error("Errore durante la creazione del profilo fornitore: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante la creazione del profilo.");
            return "redirect:/providers/setup";
        }
    }

    /**
     * Visualizza e gestisce il profilo del fornitore dell'utente corrente.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public String providerProfile(Model model) {
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        // Ottieni il profilo del fornitore
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        model.addAttribute("provider", provider);
        
        // Ottieni tutti i servizi disponibili per aggiungerli al profilo
        List<ServiceDto> allServices = serviceManagementService.findAllActive();
        model.addAttribute("availableServices", allServices);
        
        return "providers/profile";
    }

    /**
     * Aggiorna il profilo del fornitore.
     * 
     * @param providerDto Dati aggiornati del profilo
     * @param result Risultato della validazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/profile/update")
    @PreAuthorize("hasRole('PROVIDER')")
    public String updateProviderProfile(
            @Valid @ModelAttribute("provider") ServiceProviderDto providerDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "providers/profile";
        }
        
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        // Ottieni il profilo del fornitore
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto existingProvider = providerOpt.get();
        
        try {
            // Aggiorna il profilo
            serviceProviderService.updateServiceProvider(existingProvider.getId(), providerDto);
            redirectAttributes.addFlashAttribute("success", "Profilo aggiornato con successo.");
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento del profilo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante l'aggiornamento del profilo.");
        }
        
        return "redirect:/provider/profile";
    }

    /**
     * Aggiorna gli orari di lavoro del fornitore.
     * 
     * @param workingHours Orari di lavoro in formato JSON
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/profile/hours")
    @PreAuthorize("hasRole('PROVIDER')")
    public String updateWorkingHours(
            @RequestParam String workingHours,
            RedirectAttributes redirectAttributes) {
        
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        // Ottieni il profilo del fornitore
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        
        try {
            // Aggiorna gli orari di lavoro
            serviceProviderService.updateWorkingHours(provider.getId(), workingHours);
            redirectAttributes.addFlashAttribute("success", "Orari di lavoro aggiornati con successo.");
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento degli orari di lavoro: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante l'aggiornamento degli orari di lavoro.");
        }
        
        return "redirect:/provider/profile";
    }

    /**
     * Aggiunge un servizio al profilo del fornitore.
     * 
     * @param serviceId ID del servizio da aggiungere
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/profile/services/add")
    @PreAuthorize("hasRole('PROVIDER')")
    public String addService(
            @RequestParam Long serviceId,
            RedirectAttributes redirectAttributes) {
        
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        // Ottieni il profilo del fornitore
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        
        try {
            // Aggiungi il servizio
            boolean added = serviceProviderService.addService(provider.getId(), serviceId);
            
            if (added) {
                redirectAttributes.addFlashAttribute("success", "Servizio aggiunto con successo.");
            } else {
                redirectAttributes.addFlashAttribute("info", "Il servizio è già presente nel tuo profilo.");
            }
        } catch (Exception e) {
            log.error("Errore durante l'aggiunta del servizio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante l'aggiunta del servizio.");
        }
        
        return "redirect:/provider/profile";
    }

    /**
     * Rimuove un servizio dal profilo del fornitore.
     * 
     * @param serviceId ID del servizio da rimuovere
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/profile/services/remove")
    @PreAuthorize("hasRole('PROVIDER')")
    public String removeService(
            @RequestParam Long serviceId,
            RedirectAttributes redirectAttributes) {
        
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        // Ottieni il profilo del fornitore
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        
        try {
            // Rimuovi il servizio
            boolean removed = serviceProviderService.removeService(provider.getId(), serviceId);
            
            if (removed) {
                redirectAttributes.addFlashAttribute("success", "Servizio rimosso con successo.");
            } else {
                redirectAttributes.addFlashAttribute("info", "Il servizio non è presente nel tuo profilo.");
            }
        } catch (Exception e) {
            log.error("Errore durante la rimozione del servizio: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore durante la rimozione del servizio.");
        }
        
        return "redirect:/provider/profile";
    }
}