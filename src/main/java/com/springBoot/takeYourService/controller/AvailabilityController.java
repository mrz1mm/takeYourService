package com.springBoot.takeYourService.controller;

import com.springBoot.takeYourService.model.dto.AvailabilitySlotDto;
import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.service.AvailabilitySlotService;
import com.springBoot.takeYourService.service.ServiceProviderService;
import com.springBoot.takeYourService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller per la gestione degli slot di disponibilità dei fornitori di servizi.
 */
@Controller
@RequestMapping("/availability")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PROVIDER')")
public class AvailabilityController {

    private final AvailabilitySlotService availabilitySlotService;
    private final ServiceProviderService serviceProviderService;
    private final UserService userService;

    /**
     * Visualizza il calendario delle disponibilità del fornitore corrente.
     * 
     * @param date Data selezionata (opzionale)
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/calendar")
    public String viewCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        
        // Se data non specificata, usa oggi
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        
        // Ottieni il fornitore corrente
        Optional<ServiceProviderDto> providerOpt = getCurrentProvider();
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        model.addAttribute("provider", provider);
        
        // Calcola l'inizio e la fine della settimana (da lunedì a domenica)
        LocalDate monday = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);
        
        // Ottieni gli slot per la settimana selezionata
        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek = sunday.atTime(23, 59, 59);
        List<AvailabilitySlotDto> slots = availabilitySlotService.findAllSlotsByDateRange(
                provider.getId(), startOfWeek, endOfWeek);
        
        model.addAttribute("slots", slots);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("startDate", monday);
        model.addAttribute("endDate", sunday);
        
        return "availability/calendar";
    }

    /**
     * Visualizza il form per creare un nuovo slot di disponibilità.
     * 
     * @param date Data preselezionata
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/create")
    public String createSlotForm(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        
        // Se data non specificata, usa oggi
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        
        // Ottieni il fornitore corrente
        Optional<ServiceProviderDto> providerOpt = getCurrentProvider();
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("provider", providerOpt.get());
        
        return "availability/create";
    }

    /**
     * Crea un nuovo slot di disponibilità.
     * 
     * @param date Data selezionata
     * @param startTime Ora di inizio
     * @param endTime Ora di fine
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/create")
    public String createSlot(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            RedirectAttributes redirectAttributes) {
        
        // Ottieni il fornitore corrente
        Optional<ServiceProviderDto> providerOpt = getCurrentProvider();
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        
        // Combina data e ora
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
        
        // Validazione: verifica che l'ora di inizio sia prima dell'ora di fine
        if (startDateTime.isAfter(endDateTime) || startDateTime.equals(endDateTime)) {
            redirectAttributes.addFlashAttribute("error", "L'ora di inizio deve essere precedente all'ora di fine.");
            return "redirect:/availability/create?date=" + date;
        }
        
        // Verifica che lo slot sia nel futuro
        if (startDateTime.isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Non è possibile creare slot nel passato.");
            return "redirect:/availability/create?date=" + date;
        }
        
        try {
            // Crea lo slot
            availabilitySlotService.createSlot(provider.getId(), startDateTime, endDateTime);
            redirectAttributes.addFlashAttribute("success", "Slot di disponibilità creato con successo.");
            
            return "redirect:/availability/calendar?date=" + date;
        } catch (Exception e) {
            log.error("Errore durante la creazione dello slot: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            
            return "redirect:/availability/create?date=" + date;
        }
    }

    /**
     * Crea più slot di disponibilità in base ai parametri specificati.
     * 
     * @param date Data selezionata
     * @param startHour Ora di inizio
     * @param endHour Ora di fine
     * @param slotDuration Durata di ogni slot in minuti
     * @param breakDuration Durata della pausa tra slot in minuti
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/create-multiple")
    public String createMultipleSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int startHour,
            @RequestParam int endHour,
            @RequestParam int slotDuration,
            @RequestParam int breakDuration,
            RedirectAttributes redirectAttributes) {
        
        // Ottieni il fornitore corrente
        Optional<ServiceProviderDto> providerOpt = getCurrentProvider();
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        
        // Validazione
        if (startHour >= endHour) {
            redirectAttributes.addFlashAttribute("error", "L'ora di inizio deve essere precedente all'ora di fine.");
            return "redirect:/availability/create?date=" + date;
        }
        
        if (slotDuration <= 0 || breakDuration < 0) {
            redirectAttributes.addFlashAttribute("error", "La durata dello slot deve essere positiva e la pausa non negativa.");
            return "redirect:/availability/create?date=" + date;
        }
        
        try {
            // Crea gli slot
            List<AvailabilitySlotDto> slots = availabilitySlotService.createMultipleSlots(
                    provider.getId(), date, startHour, endHour, slotDuration, breakDuration);
            
            redirectAttributes.addFlashAttribute("success", "Creati " + slots.size() + " slot di disponibilità.");
            
            return "redirect:/availability/calendar?date=" + date;
        } catch (Exception e) {
            log.error("Errore durante la creazione degli slot: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            
            return "redirect:/availability/create?date=" + date;
        }
    }

    /**
     * Genera automaticamente gli slot di disponibilità in base agli orari di lavoro del fornitore.
     * 
     * @param startDate Data di inizio
     * @param endDate Data di fine
     * @param slotDuration Durata di ogni slot in minuti
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/generate")
    public String generateSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam int slotDuration,
            RedirectAttributes redirectAttributes) {
        
        // Ottieni il fornitore corrente
        Optional<ServiceProviderDto> providerOpt = getCurrentProvider();
        if (providerOpt.isEmpty()) {
            return "redirect:/providers/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        
        // Validazione
        if (endDate.isBefore(startDate)) {
            redirectAttributes.addFlashAttribute("error", "La data di fine deve essere successiva alla data di inizio.");
            return "redirect:/availability/create?date=" + startDate;
        }
        
        if (slotDuration <= 0) {
            redirectAttributes.addFlashAttribute("error", "La durata dello slot deve essere positiva.");
            return "redirect:/availability/create?date=" + startDate;
        }
        
        try {
            // Genera gli slot
            List<AvailabilitySlotDto> slots = availabilitySlotService.generateSlotsFromWorkingHours(
                    provider.getId(), startDate, endDate, slotDuration);
            
            redirectAttributes.addFlashAttribute("success", "Generati " + slots.size() + " slot di disponibilità.");
            
            return "redirect:/availability/calendar?date=" + startDate;
        } catch (Exception e) {
            log.error("Errore durante la generazione degli slot: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            
            return "redirect:/availability/create?date=" + startDate;
        }
    }

    /**
     * Elimina uno slot di disponibilità.
     * 
     * @param id ID dello slot da eliminare
     * @param date Data corrente per il redirect
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/delete/{id}")
    public String deleteSlot(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            RedirectAttributes redirectAttributes) {
        
        try {
            boolean deleted = availabilitySlotService.deleteSlot(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Slot eliminato con successo.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Impossibile eliminare lo slot.");
            }
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione dello slot: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
        }
        
        return "redirect:/availability/calendar?date=" + date;
    }

    /**
     * Helper method per ottenere il fornitore di servizi associato all'utente corrente.
     * 
     * @return Optional contenente il provider se esiste, vuoto altrimenti
     */
    private Optional<ServiceProviderDto> getCurrentProvider() {
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return Optional.empty();
        }
        
        UserDto currentUser = currentUserOpt.get();
        return serviceProviderService.findByUserId(currentUser.getId());
    }
}