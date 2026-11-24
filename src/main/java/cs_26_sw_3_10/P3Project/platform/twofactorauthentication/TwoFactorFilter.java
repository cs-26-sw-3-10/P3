package cs_26_sw_3_10.P3Project.platform.twofactorauthentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TwoFactorFilter extends OncePerRequestFilter {

    private final UserService userService;

    public TwoFactorFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String path = req.getRequestURI();

        boolean loggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
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