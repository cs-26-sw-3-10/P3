package cs_26_sw_3_10.P3Project.ui.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * De felter, formularen "Nyt journalnotat" sender. De fire tekstfelter svarer til
 * skabelonen for et notat: symptomer, observationer, udført behandling og plan.
 */
public class JournalEntryForm {

    @NotNull(message = "Dato skal udfyldes")
    @PastOrPresent(message = "Datoen må ikke ligge i fremtiden")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    // Den aftale, notatet hører til. Tom, hvis notatet skrives uden en aftale.
    private String appointment;

    @Size(max = 4000, message = "Højst 4000 tegn")
    private String symptoms;

    @Size(max = 4000, message = "Højst 4000 tegn")
    private String observations;

    @Size(max = 4000, message = "Højst 4000 tegn")
    private String treatment;

    @Size(max = 4000, message = "Højst 4000 tegn")
    private String plan;

    // Bruges kun ved en rettelse: hvorfor notatet rettes. Valgfri.
    @Size(max = 500, message = "Begrundelsen må højst være 500 tegn")
    private String reason;

    /** Sand, hvis ingen af de fire tekstfelter er udfyldt. */
    public boolean hasNoText() {
        return isBlank(symptoms) && isBlank(observations) && isBlank(treatment) && isBlank(plan);
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getAppointment() { return appointment; }
    public void setAppointment(String appointment) { this.appointment = appointment; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
