package com.springBoot.takeYourService.controller;

import com.springBoot.takeYourService.model.dto.ServiceDto;
import com.springBoot.takeYourService.model.dto.UserRegistrationDto;
import com.springBoot.takeYourService.service.ServiceManagementService;
import com.springBoot.takeYourService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller per la home page e le operazioni di autenticazione.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UserService userService;
    private final ServiceManagementService serviceManagementService;

    /**
     * Visualizza la pagina iniziale dell'applicazione.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/")
    public String home(Model model) {
        // Ottieni i servizi in evidenza
        List<ServiceDto> featuredServices = serviceManagementService.findAllActive();
        model.addAttribute("featuredServices", featuredServices);
        
        return "home/index";
    }

    /**
     * Visualizza il modulo di login.
     * 
     * @param error Parametro che indica un errore di login
     * @param logout Parametro che indica un logout avvenuto
     * @param expired Parametro che indica una sessione scaduta
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Nome utente o password non validi.");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Logout effettuato con successo.");
        }
        
        if (expired != null) {
            model.addAttribute("message", "La sessione è scaduta. Effettua nuovamente il login.");
        }
        
        return "auth/login";
    }

    /**
     * Visualizza il modulo di registrazione.
     * 
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "auth/register";
    }

    /**
     * Gestisce la registrazione di un nuovo utente.
     * 
     * @param userDto DTO con i dati di registrazione
     * @param result Risultato della validazione
     * @param model Model per la vista
     * @return Nome della vista da renderizzare
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
                              BindingResult result,
                              Model model) {
        
        // Verifica errori di validazione
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        // Verifica se username è già in uso
        if (userService.isUsernameAlreadyInUse(userDto.getUsername())) {
            result.rejectValue("username", null, "Username già in uso");
            return "auth/register";
        }
        
        // Verifica se email è già in uso
        if (userService.isEmailAlreadyInUse(userDto.getEmail())) {
            result.rejectValue("email", null, "Email già in uso");
            return "auth/register";
        }
        
        // Verifica se le password corrispondono
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Le password non corrispondono");
            return "auth/register";
        }
        
        try {
            // Registra l'utente
            userService.registerUser(userDto);
            
            // Aggiungi messaggio di successo
            model.addAttribute("success", true);
            model.addAttribute("message", "Registrazione completata con successo! Ora puoi effettuare il login.");
            
            return "auth/login";
        } catch (Exception e) {
            log.error("Errore durante la registrazione dell'utente: {}", e.getMessage());
            model.addAttribute("error", "Si è verificato un errore durante la registrazione. Riprova più tardi.");
            return "auth/register";
        }
    }
}