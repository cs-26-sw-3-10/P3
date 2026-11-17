package cs_26_sw_3_10.P3Project.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import cs_26_sw_3_10.P3Project.platform.twofactorauthentication.UserService;
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
                // Alt andet kræver login, også nye sider, vi tilføjer senere
                .anyRequest().authenticated())

            // Login med vores egen formular i login.html
            .formLogin(form -> form
                // Vores egen login-side. Spring tjekker selv brugernavn og kodeord ved POST /login
                .loginPage("/login")
                // Efter login sendes brugeren altid til kalenderen
                .defaultSuccessUrl("/kalender", true)
                .permitAll())
                .addFilterBefore(new TwoFactorFilter(userService), AuthorizationFilter.class)

            // Ved logud (POST /logout) afsluttes sessionen, og brugeren sendes til login-siden
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"));

        return http.build();
    }
    
static class TwoFactorFilter extends org.springframework.web.filter.OncePerRequestFilter {
        private final UserService userService;

        TwoFactorFilter(UserService userService) {
            this.userService = userService;
        }

        @Override
        protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest req,
                                        jakarta.servlet.http.HttpServletResponse res,
                                        jakarta.servlet.FilterChain chain)
                throws jakarta.servlet.ServletException, java.io.IOException {
            var auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            String path = req.getRequestURI();

            boolean loggedIn = auth != null && auth.isAuthenticated()
                    && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
            boolean exempt = path.startsWith("/2fa") || path.equals("/logout")
                    || path.equals("/login") || path.startsWith("/css/") || path.startsWith("/js/");

            if (loggedIn && !exempt
                    && !Boolean.TRUE.equals(req.getSession().getAttribute("2faVerified"))) {
                res.sendRedirect(userService.hasTotp(auth.getName()) ? "/2fa/verify" : "/2fa/setup");
                return;
            }
            chain.doFilter(req, res);
        }
    }

}
