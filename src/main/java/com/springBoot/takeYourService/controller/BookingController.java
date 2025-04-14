package com.springBoot.takeYourService.controller;

import com.springBoot.takeYourService.model.dto.AvailabilitySlotDto;
import com.springBoot.takeYourService.model.dto.BookingDto;
import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.model.entity.Booking.BookingStatus;
import com.springBoot.takeYourService.service.AvailabilitySlotService;
import com.springBoot.takeYourService.service.BookingService;
import com.springBoot.takeYourService.service.ServiceManagementService;
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
import java.util.List;
import java.util.Optional;

/**
 * Controller per la gestione delle prenotazioni.
 */
@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final ServiceProviderService serviceProviderService;
    private final ServiceManagementService serviceManagementService;
    private final AvailabilitySlotService availabilitySlotService;

    /**
     * Visualizza tutte le prenotazioni dell'utente corrente.
     * 
     * @param tab Tab selezionato (upcoming, past, pending, completed, cancelled)
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public String listBookings(
            @RequestParam(defaultValue = "upcoming") String tab,
            Model model) {
        
        // Ottieni l'utente corrente
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        LocalDateTime now = LocalDateTime.now();
        
        // Verifica se l'utente è un fornitore o un cliente
        if (currentUser.getRoleNames().contains("ROLE_PROVIDER")) {
            Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
            if (providerOpt.isEmpty()) {
                return "redirect:/providers/setup";
            }
            
            ServiceProviderDto provider = providerOpt.get();
            
            switch (tab) {
                case "upcoming":
                    model.addAttribute("bookings", bookingService.findFutureBookingsByProviderId(provider.getId(), now));
                    break;
                case "past":
                    model.addAttribute("bookings", bookingService.findPastBookingsByProviderId(provider.getId(), now));
                    break;
                case "pending":
                    model.addAttribute("bookings", bookingService.findByProviderIdAndStatus(provider.getId(), BookingStatus.PENDING));
                    break;
                case "confirmed":
                    model.addAttribute("bookings", bookingService.findByProviderIdAndStatus(provider.getId(), BookingStatus.CONFIRMED));
                    break;
                case "completed":
                    model.addAttribute("bookings", bookingService.findByProviderIdAndStatus(provider.getId(), BookingStatus.COMPLETED));
                    break;
                case "cancelled":
                    model.addAttribute("bookings", bookingService.findByProviderIdAndStatus(provider.getId(), BookingStatus.CANCELLED));
                    break;
                default:
                    model.addAttribute("bookings", bookingService.findAllByProviderId(provider.getId()));
            }
            
            model.addAttribute("isProvider", true);
        } else {
            // L'utente è un cliente
            switch (tab) {
                case "upcoming":
                    model.addAttribute("bookings", bookingService.findFutureBookingsByClientId(currentUser.getId(), now));
                    break;
                case "past":
                    model.addAttribute("bookings", bookingService.findPastBookingsByClientId(currentUser.getId(), now));
                    break;
                case "pending":
                    model.addAttribute("bookings", bookingService.findByClientIdAndStatus(currentUser.getId(), BookingStatus.PENDING));
                    break;
                case "confirmed":
                    model.addAttribute("bookings", bookingService.findByClientIdAndStatus(currentUser.getId(), BookingStatus.CONFIRMED));
                    break;
                case "completed":
                    model.addAttribute("bookings", bookingService.findByClientIdAndStatus(currentUser.getId(), BookingStatus.COMPLETED));
                    break;
                case "cancelled":
                    model.addAttribute("bookings", bookingService.findByClientIdAndStatus(currentUser.getId(), BookingStatus.CANCELLED));
                    break;
                default:
                    model.addAttribute("bookings", bookingService.findAllByClientId(currentUser.getId()));
            }
            
            model.addAttribute("isProvider", false);
        }
        
        model.addAttribute("currentTab", tab);
        model.addAttribute("user", currentUser);
        
        return "bookings/list";
    }

    /**
     * Visualizza i dettagli di una prenotazione.
     * 
     * @param id ID della prenotazione
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public String viewBooking(@PathVariable Long id, Model model) {
        Optional<BookingDto> bookingOpt = bookingService.findById(id);
        if (bookingOpt.isEmpty()) {
            return "redirect:/bookings?error=Prenotazione non trovata";
        }
        
        BookingDto booking = bookingOpt.get();
        
        // Verifica che l'utente corrente sia il cliente o il fornitore della prenotazione
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        boolean isAdmin = currentUser.getRoleNames().contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            boolean isAuthorized = false;
            
            if (currentUser.getId().equals(booking.getClient().getId())) {
                // L'utente è il cliente della prenotazione
                isAuthorized = true;
                model.addAttribute("isClient", true);
            } else if (currentUser.getRoleNames().contains("ROLE_PROVIDER")) {
                // Verifica se l'utente è il fornitore della prenotazione
                Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
                if (providerOpt.isPresent() && providerOpt.get().getId().equals(booking.getProvider().getId())) {
                    isAuthorized = true;
                    model.addAttribute("isProvider", true);
                }
            }
            
            if (!isAuthorized) {
                return "redirect:/dashboard?error=Non sei autorizzato a visualizzare questa prenotazione";
            }
        } else {
            model.addAttribute("isAdmin", true);
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("user", currentUser);
        
        return "bookings/view";
    }

    /**
     * Visualizza il form per creare una nuova prenotazione.
     * 
     * @param providerId ID del fornitore
     * @param serviceId ID del servizio
     * @param date Data selezionata
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('CLIENT')")
    public String createBookingForm(
            @RequestParam Long providerId,
            @RequestParam Long serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        
        // Se data non specificata, usa oggi
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        
        // Ottieni il fornitore
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findById(providerId);
        if (providerOpt.isEmpty()) {
            return "redirect:/providers?error=Fornitore non trovato";
        }
        
        // Ottieni il servizio
        Optional<ServiceDto> serviceOpt = serviceManagementService.findById(serviceId);
        if (serviceOpt.isEmpty()) {
            return "redirect:/services?error=Servizio non trovato";
        }
        
        // Verifica che il fornitore offra il servizio
        ServiceProviderDto provider = providerOpt.get();
        ServiceDto service = serviceOpt.get();
        if (provider.getServiceIds() == null || !provider.getServiceIds().contains(service.getId())) {
            return "redirect:/providers?error=Il fornitore non offre questo servizio";
        }
        
        // Ottieni gli slot disponibili per la data selezionata
        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.plusDays(1).atStartOfDay().minusSeconds(1);
        List<AvailabilitySlotDto> availableSlots = availabilitySlotService.findAvailableSlotsByDateRange(
                provider.getId(), startOfDay, endOfDay);
        
        model.addAttribute("provider", provider);
        model.addAttribute("service", service);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableSlots", availableSlots);
        
        // Ottieni le date con slot disponibili (per il datepicker)
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(2);
        List<LocalDate> datesWithSlots = availabilitySlotService.findDatesWithAvailableSlots(
                provider.getId(), startDate, endDate);
        model.addAttribute("datesWithSlots", datesWithSlots);
        
        return "bookings/create";
    }

    /**
     * Crea una nuova prenotazione.
     * 
     * @param providerId ID del fornitore
     * @param serviceId ID del servizio
     * @param slotId ID dello slot di disponibilità
     * @param notes Note sulla prenotazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CLIENT')")
    public String createBooking(
            @RequestParam Long providerId,
            @RequestParam Long serviceId,
            @RequestParam Long slotId,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        // Ottieni l'utente corrente
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        
        try {
            // Crea la prenotazione
            BookingDto booking = bookingService.createBooking(
                    currentUser.getId(),
                    providerId,
                    serviceId,
                    slotId,
                    notes);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Prenotazione creata con successo. Ti verrà inviata una conferma quando il fornitore accetterà la prenotazione.");
            
            return "redirect:/bookings/" + booking.getId();
        } catch (Exception e) {
            log.error("Errore durante la creazione della prenotazione: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            
            return "redirect:/bookings/create?providerId=" + providerId + "&serviceId=" + serviceId;
        }
    }

    /**
     * Conferma una prenotazione (solo per fornitori).
     * 
     * @param id ID della prenotazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('PROVIDER')")
    public String confirmBooking(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            BookingDto booking = bookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("success", "Prenotazione confermata con successo.");
            return "redirect:/bookings/" + id;
        } catch (Exception e) {
            log.error("Errore durante la conferma della prenotazione: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            return "redirect:/bookings/" + id;
        }
    }

    /**
     * Completa una prenotazione (solo per fornitori).
     * 
     * @param id ID della prenotazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    public String completeBooking(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            BookingDto booking = bookingService.completeBooking(id);
            redirectAttributes.addFlashAttribute("success", "Prenotazione completata con successo.");
            return "redirect:/bookings/" + id;
        } catch (Exception e) {
            log.error("Errore durante il completamento della prenotazione: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            return "redirect:/bookings/" + id;
        }
    }

    /**
     * Cancella una prenotazione.
     * 
     * @param id ID della prenotazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public String cancelBooking(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            BookingDto booking = bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Prenotazione cancellata con successo.");
            return "redirect:/bookings/" + id;
        } catch (Exception e) {
            log.error("Errore durante la cancellazione della prenotazione: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            return "redirect:/bookings/" + id;
        }
    }

    /**
     * Aggiorna le note di una prenotazione.
     * 
     * @param id ID della prenotazione
     * @param notes Nuove note
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public String updateNotes(
            @PathVariable Long id,
            @RequestParam String notes,
            RedirectAttributes redirectAttributes) {
        
        try {
            BookingDto booking = bookingService.updateNotes(id, notes);
            redirectAttributes.addFlashAttribute("success", "Note aggiornate con successo.");
            return "redirect:/bookings/" + id;
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento delle note: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            return "redirect:/bookings/" + id;
        }
    }

    /**
     * Segna una prenotazione come revisionata dal cliente.
     * 
     * @param id ID della prenotazione
     * @param redirectAttributes Attributi per il redirect
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/{id}/reviewed")
    @PreAuthorize("hasRole('CLIENT')")
    public String markAsReviewed(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            boolean success = bookingService.markAsReviewed(id);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Prenotazione segnata come revisionata.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Impossibile segnare la prenotazione come revisionata.");
            }
            return "redirect:/bookings/" + id;
        } catch (Exception e) {
            log.error("Errore durante il marking della recensione: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Si è verificato un errore: " + e.getMessage());
            return "redirect:/bookings/" + id;
        }
    }
}