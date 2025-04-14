package com.springBoot.takeYourService.security;

import com.springBoot.takeYourService.model.entity.User;
import com.springBoot.takeYourService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementazione personalizzata del servizio UserDetailsService di Spring Security.
 * Carica gli utenti dal database e crea oggetti UserDetails con le relative autorizzazioni.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carica un utente dal database tramite il suo username (o email).
     * 
     * @param usernameOrEmail Username o email dell'utente
     * @return UserDetails dell'utente trovato
     * @throws UsernameNotFoundException Se l'utente non viene trovato
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Cerca prima per username
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElse(null);
        
        // Se non trovato, cerca per email
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Utente non trovato con username o email: " + usernameOrEmail));
        }
        
        // Verifica se l'utente è attivo
        if (!user.isActive()) {
            throw new UsernameNotFoundException("L'account non è attivo: " + usernameOrEmail);
        }
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true,               // enabled
                true,               // accountNonExpired
                true,               // credentialsNonExpired
                true,               // accountNonLocked
                getAuthorities(user)
        );
    }
    
    /**
     * Estrae le autorizzazioni (ruoli) dell'utente.
     * 
     * @param user L'utente di cui estrarre le autorizzazioni
     * @return Collezione di GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}