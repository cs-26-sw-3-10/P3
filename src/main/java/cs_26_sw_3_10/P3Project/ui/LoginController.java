package cs_26_sw_3_10.P3Project.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Viser login-siden. Selve login (POST /login) håndteres af Spring Security,
 * som tjekker brugernavn og kodeord ud fra SecurityConfig.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // = src/main/resources/templates/login.html
    }
}