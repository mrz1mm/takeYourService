package com.springBoot.takeYourService.controller;

import com.springBoot.takeYourService.model.dto.BookingDto;
import com.springBoot.takeYourService.model.dto.ServiceProviderDto;
import com.springBoot.takeYourService.model.dto.UserDto;
import com.springBoot.takeYourService.model.entity.Booking.BookingStatus;
import com.springBoot.takeYourService.service.BookingService;
import com.springBoot.takeYourService.service.ServiceProviderService;
import com.springBoot.takeYourService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller per la dashboard dell'utente.
 * Gestisce la visualizzazione delle dashboard per clienti, fornitori e amministratori.
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserService userService;
    private final BookingService bookingService;
    private final ServiceProviderService serviceProviderService;

    /**
     * Visualizza la dashboard appropriata in base al ruolo dell'utente.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping
    public String dashboard(Model model) {
        // Ottieni l'utente corrente
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        model.addAttribute("user", currentUser);
        
        // Verifica ruolo e reindirizza alla dashboard appropriata
        if (currentUser.getRoleNames().contains("ROLE_ADMIN")) {
            return "redirect:/dashboard/admin";
        } else if (currentUser.getRoleNames().contains("ROLE_PROVIDER")) {
            return "redirect:/dashboard/provider";
        } else {
            return "redirect:/dashboard/client";
        }
    }
    
    /**
     * Visualizza la dashboard del cliente.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/client")
    public String clientDashboard(Model model) {
        // Ottieni l'utente corrente
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        model.addAttribute("user", currentUser);
        
        // Ottieni le prenotazioni future del cliente
        List<BookingDto> upcomingBookings = bookingService.findFutureBookingsByClientId(
                currentUser.getId(), LocalDateTime.now());
        model.addAttribute("upcomingBookings", upcomingBookings);
        
        // Ottieni le prenotazioni in attesa di conferma
        List<BookingDto> pendingBookings = bookingService.findByClientIdAndStatus(
                currentUser.getId(), BookingStatus.PENDING);
        model.addAttribute("pendingBookings", pendingBookings);
        
        // Ottieni le ultime prenotazioni completate (per recensioni)
        List<BookingDto> completedBookings = bookingService.findByClientIdAndStatus(
                currentUser.getId(), BookingStatus.COMPLETED);
        model.addAttribute("completedBookings", completedBookings.stream()
                .filter(booking -> !booking.getClientReviewed())
                .limit(5)
                .toList());
        
        return "dashboard/client";
    }
    
    /**
     * Visualizza la dashboard del fornitore di servizi.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/provider")
    public String providerDashboard(Model model) {
        // Ottieni l'utente corrente
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        model.addAttribute("user", currentUser);
        
        // Ottieni il profilo del fornitore
        Optional<ServiceProviderDto> providerOpt = serviceProviderService.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) {
            // L'utente ha il ruolo PROVIDER ma non ha ancora un profilo di fornitore
            return "redirect:/provider/setup";
        }
        
        ServiceProviderDto provider = providerOpt.get();
        model.addAttribute("provider", provider);
        
        // Ottieni le prenotazioni in attesa di conferma
        List<BookingDto> pendingBookings = bookingService.findByProviderIdAndStatus(
                provider.getId(), BookingStatus.PENDING);
        model.addAttribute("pendingBookings", pendingBookings);
        
        // Ottieni le prenotazioni confermate per oggi
        List<BookingDto> todayBookings = bookingService.findFutureBookingsByProviderId(
                provider.getId(), LocalDateTime.now());
        model.addAttribute("todayBookings", todayBookings.stream()
                .filter(b -> b.getStartTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())
                        && b.getStatus().equals(BookingStatus.CONFIRMED.name()))
                .toList());
        
        // Ottieni le prossime prenotazioni confermate
        model.addAttribute("upcomingBookings", todayBookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.CONFIRMED.name()))
                .limit(5)
                .toList());
        
        return "dashboard/provider";
    }
    
    /**
     * Visualizza la dashboard dell'amministratore.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        // Ottieni l'utente corrente
        Optional<UserDto> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty() || !currentUserOpt.get().getRoleNames().contains("ROLE_ADMIN")) {
            return "redirect:/login";
        }
        
        UserDto currentUser = currentUserOpt.get();
        model.addAttribute("user", currentUser);
        
        // Qui possiamo aggiungere statistiche o altre informazioni per gli admin
        
        return "dashboard/admin";
    }
}