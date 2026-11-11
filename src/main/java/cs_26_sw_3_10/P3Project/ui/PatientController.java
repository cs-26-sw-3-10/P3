package cs_26_sw_3_10.P3Project.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cs_26_sw_3_10.P3Project.ui.form.PatientForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/patienter")
public class PatientController {

    // Én række i patientlisten. Midlertidig, indtil patienter kommer fra databasen.
    record PatientRow(long id, String name, String birthDate, String phone,
                      String nextAppointment, String status) {}

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Midlertidig liste i hukommelsen med opdigtede patienter. Nulstilles ved genstart
    // og erstattes af PatientService og databasen senere.
    private final List<PatientRow> patients = new CopyOnWriteArrayList<>(List.of(
        new PatientRow(1, "Patient A", "12-03-1985", "20 11 22 33", "Man. 2. nov. · 09:00", "Aktiv"),
        new PatientRow(2, "Patient B", "04-07-1972", "30 44 55 66", "Man. 2. nov. · 13:00", "Aktiv"),
        new PatientRow(3, "Patient C", "23-11-1990", "40 77 88 99", "Tir. 3. nov. · 10:00", "Aktiv"),
        new PatientRow(4, "Patient D", "15-01-1968", "50 12 34 56", null, "Inaktiv"),
        new PatientRow(5, "Patient E", "30-09-2001", "60 98 76 54", "Fre. 6. nov. · 11:00", "Aktiv"),
        new PatientRow(6, "Patient F", "08-05-1979", "70 24 68 10", null, "Afsluttet")));

    private final AtomicLong nextId = new AtomicLong(7);

    // Viser patientlisten. q er søgeteksten, og status er filteret: alle, aktiv eller afsluttet.
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "alle") String status,
                       Model model) {
        String search = q.trim().toLowerCase();

        List<PatientRow> result = patients.stream()
            .filter(p -> status.equals("alle") || p.status().equalsIgnoreCase(status))
            .filter(p -> search.isEmpty()
                || p.name().toLowerCase().contains(search)
                || (p.phone() != null && p.phone().replace(" ", "").contains(search.replace(" ", ""))))
            .toList();

        model.addAttribute("patients", result);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "patients";
    }

    // Viser en tom formular til en ny patient
    @GetMapping("/ny")
    public String newPatient(Model model) {
        model.addAttribute("patientForm", new PatientForm());
        return "patient-form";
    }

    // Modtager formularen. Ved fejl vises den igen med fejlbeskeder; ellers gemmes patienten.
    @PostMapping
    public String create(@Valid @ModelAttribute PatientForm patientForm,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "patient-form";
        }

        // En ny patient starter som inaktiv, indtil der bookes en aftale (jf. tilstandsdiagrammet)
        patients.add(new PatientRow(
            nextId.getAndIncrement(),
            patientForm.getName().trim(),
            patientForm.getBirthDate().format(DATE),
            patientForm.getPhone(),
            null,
            "Inaktiv"));

        // Beskeden vises én gang på listen efter omdirigeringen
        redirect.addFlashAttribute("message", "Patienten " + patientForm.getName().trim() + " er oprettet.");
        return "redirect:/patienter";
    }
}