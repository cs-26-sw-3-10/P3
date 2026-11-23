package cs_26_sw_3_10.P3Project.platform.twofactorauthentication;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/2fa")
public class TwoFactorController {

    private static final int MAX_ATTEMPTS = 5;
    private static final String APP_NAME = "Tina_KST Journal Website";
    
    private final TotpService totpService;
    private final UserService userService;

    public TwoFactorController(TotpService totpService, UserService userService) {
        this.totpService = totpService;
        this.userService = userService;
    }

    @GetMapping("/setup")
    public String setup(HttpSession session, Model model, Principal principal) throws Exception {
        if (userService.hasTotp(principal.getName())) return "redirect:/2fa/verify";
        
        //genererer kun et secret hvis den ikke eksistere
        String secret = (String) session.getAttribute("pending2faSecret");
        if (secret == null) 
        {   
        secret = totpService.generateSecret();
        session.setAttribute("pending2faSecret", secret);
        }
        session.setAttribute("pending2faSecret", secret);
        model.addAttribute("qrHtml", totpService.getQrCodeHtml(APP_NAME, principal.getName(), secret));
        model.addAttribute("secret", secret);
        return "authenticationsetup";
    }

    @PostMapping("/setup")
    public String confirmSetup(@RequestParam String pin, HttpSession session,
                               Model model, Principal principal) throws Exception {
        String secret = (String) session.getAttribute("pending2faSecret");
        if (secret == null) return "redirect:/2fa/setup";

        if (totpService.validatePin(pin, secret)) {
            userService.saveTotpSecret(principal.getName(), secret);
            session.removeAttribute("pending2faSecret");
            session.setAttribute("2faVerified", true);
            return "redirect:/kalender";
        }
        model.addAttribute("error", "Invalid code - please try again.");
        model.addAttribute("secret", secret);
        model.addAttribute("qrHtml", totpService.getQrCodeHtml(APP_NAME, principal.getName(), secret));
        return "authenticationsetup";
    }

    @GetMapping("/verify")
    public String verifyForm(Principal principal) {
        if (!userService.hasTotp(principal.getName())) return "redirect:/2fa/setup";
        return "authenticationverification";
    }
    

    @PostMapping("/verify")
    public String verify(@RequestParam String pin, HttpSession session,
                         Model model, Principal principal) throws Exception {
        String secret = userService.getTotpSecret(principal.getName());
        if (secret == null) return "redirect:/2fa/setup";

        if (totpService.validatePin(pin, secret)) {
            session.setAttribute("2faVerified", true);
            session.removeAttribute("2faAttempts");
            return "redirect:/kalender";
        }

        int attempts = session.getAttribute("2faAttempts") == null
                ? 1 : (int) session.getAttribute("2faAttempts") + 1;
        session.setAttribute("2faAttempts", attempts);
        if (attempts >= MAX_ATTEMPTS) {
            session.invalidate();
            return "redirect:/login?locked";
        }
        model.addAttribute("error", "Invalid code - please try again.");
        return "authenticationverification";
    }
}