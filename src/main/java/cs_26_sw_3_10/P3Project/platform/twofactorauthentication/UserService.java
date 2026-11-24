package cs_26_sw_3_10.P3Project.platform.twofactorauthentication;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final String adminUsername;
    private final String adminPassword;

    // placeholder database: username -> password hash, username -> totp secret
    private final Map<String, String> passwordHashes = new ConcurrentHashMap<>();
    private final Map<String, String> totpSecrets = new ConcurrentHashMap<>();

    public UserService(
                       @Value("${ADMIN_USERNAME}") String adminUsername,
                       @Value("${ADMIN_PASSWORD}") String adminPassword) {
   
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    void init() {
        passwordHashes.put(adminUsername, "{noop}" + adminPassword);
    }

    public String getPasswordHash(String name) {
        return passwordHashes.get(name);          // null if user doesn't exist
    }

    public void saveTotpSecret(String name, String secret) {
        totpSecrets.put(name, secret);
    }

    public String getTotpSecret(String name) {
        return totpSecrets.get(name);             // null if 2FA not set up
    }

    public boolean hasTotp(String name) {
        return totpSecrets.containsKey(name);
    }
}