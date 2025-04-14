package com.springBoot.takeYourService.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configurazione di sicurezza per l'applicazione.
 * Definisce le regole di autorizzazione, il processo di autenticazione
 * e altre impostazioni di sicurezza.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    
    /**
     * Configura l'encoder per le password.
     * 
     * @return L'encoder di password configurato
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Configura la catena di filtri di sicurezza.
     * 
     * @param http Il builder di configurazione HttpSecurity
     * @return La catena di filtri di sicurezza configurata
     * @throws Exception Se si verifica un errore durante la configurazione
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configura le regole di autorizzazione
            .authorizeHttpRequests(authorize -> authorize
                // Risorse pubbliche accessibili a tutti
                .requestMatchers(
                    "/",
                    "/home",
                    "/register",
                    "/login",
                    "/services/view/**",
                    "/providers/view/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/error",
                    "/webjars/**"
                ).permitAll()
                
                // Aree riservate ai fornitori di servizi
                .requestMatchers(
                    "/provider/**",
                    "/availability/**"
                ).hasRole("PROVIDER")
                
                // Aree riservate agli amministratori
                .requestMatchers(
                    "/admin/**",
                    "/services/manage/**",
                    "/users/manage/**"
                ).hasRole("ADMIN")
                
                // Aree riservate ai clienti o fornitori
                .requestMatchers(
                    "/bookings/**"
                ).hasAnyRole("CLIENT", "PROVIDER")
                
                // Tutte le altre richieste richiedono l'autenticazione
                .anyRequest().authenticated()
            )
            
            // Configura il form login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // Configura il logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Configura la gestione delle sessioni
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            
            // Configura la gestione del remember-me
            .rememberMe(remember -> remember
                .key("uniqueAndSecretKey")
                .tokenValiditySeconds(86400) // 1 giorno
            );
        
        return http.build();
    }
}