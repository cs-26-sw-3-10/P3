package cs_26_sw_3_10.P3Project.ui.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * De felter, formularen "Ny patient" sender. Reglerne her tjekkes af @Valid i controlleren,
 * og fejlbeskederne vises ved det felt, der er galt.
 */
public class PatientForm {

    @NotBlank(message = "Navn skal udfyldes")
    @Size(max = 100, message = "Navnet må højst være 100 tegn")
    private String name;

    @NotNull(message = "Fødselsdato skal udfyldes")
    @Past(message = "Fødselsdatoen skal ligge i fortiden")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    // Tomt er tilladt; ellers 8-15 cifre, mellemrum eller +
    @Pattern(regexp = "^$|^[0-9 +]{8,15}$", message = "Ugyldigt telefonnummer")
    private String phone;

    @Email(message = "Ugyldig e-mail")
    private String email;

    @Size(max = 200, message = "Adressen må højst være 200 tegn")
    private String address;

    // Om patienten har givet samtykke til behandling af sine oplysninger (GDPR)
    private boolean consent;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isConsent() { return consent; }
    public void setConsent(boolean consent) { this.consent = consent; }
}