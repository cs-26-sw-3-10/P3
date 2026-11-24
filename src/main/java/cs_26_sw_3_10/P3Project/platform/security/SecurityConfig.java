package cs_26_sw_3_10.P3Project.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import cs_26_sw_3_10.P3Project.platform.twofactorauthentication.UserService;
import jakarta.servlet.http.HttpSession;
/**
 * Sikkerhedsopsætning for systemet: hvem der må se hvad, hvordan man logger ind,
 * og hvad der sker, når man logger ud.
 *
 * Spring Security tilføjer selv CSRF-beskyttelse, sessionshåndtering og
 * sikkerheds-headers, så dem skal vi ikke sætte op her.
 */
@Configuration
public class SecurityConfig {

    /**
     * Kæden af sikkerhedstjek, som alle forespørgsler går igennem,
     * før de når frem til vores controllers.
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, UserService userService) throws Exception {
        http
            // Hvem må se hvad. Reglerne tjekkes oppefra, og den første, der passer, gælder.
            .authorizeHttpRequests(auth -> auth
                // Login-siden og CSS/JS er åbne for alle, så login-siden kan vises korrekt
                .requestMatchers("/login", "/error", "/css/**", "/js/**").permitAll()
                 // password logged in brugere kan tilgå 2FA siderne
                .requestMatchers("/2fa/**").authenticated() 
                 // Alt andet kræver login OG færdig 2FA, også nye sider, vi tilføjer senere
                .anyRequest().access((authSupplier, request) -> {
                    Authentication a = authSupplier.get();
                    boolean loggedIn = a != null && a.isAuthenticated()
                            && !(a instanceof AnonymousAuthenticationToken);
                    HttpSession session = request.getRequest().getSession(false);
                    boolean verified = session != null
                            && Boolean.TRUE.equals(session.getAttribute("2faVerified"));
                    return new AuthorizationDecision(loggedIn && verified);
                 }))
                    
            // Login med vores egen formular i login.html
            .formLogin(form -> form
                // Vores egen login-side. Spring tjekker selv brugernavn og kodeord ved POST /login
                .loginPage("/login")
                   // Efter login sendes brugeren til 2FA verify (hvis de har en hemmelighed) eller 2FA setup
                .successHandler((req, res, auth) -> {
                    req.getSession().setAttribute("2faVerified", false);
                    res.sendRedirect(userService.hasTotp(auth.getName())
                            ? "/2fa/verify" : "/2fa/setup");
                })
                .permitAll())
                 // Logget ind men 2FA ikke færdig: send til den rigtige 2FA side
            .exceptionHandling(e -> e.accessDeniedHandler((req, res, ex) -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                res.sendRedirect(auth != null && userService.hasTotp(auth.getName())
                        ? "/2fa/verify" : "/2fa/setup");
            }))
            // Ved logud (POST /logout) afsluttes sessionen, og brugeren sendes til login-siden
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"));

        return http.build();
    }
}
