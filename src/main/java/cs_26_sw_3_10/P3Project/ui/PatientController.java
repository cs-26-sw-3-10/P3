package cs_26_sw_3_10.P3Project.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cs_26_sw_3_10.P3Project.ui.DemoData.NoteView;
import cs_26_sw_3_10.P3Project.ui.DemoData.PatientRow;
import cs_26_sw_3_10.P3Project.ui.form.PatientForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/patienter")
public class PatientController {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Midlertidige dummy-data, som deles med de andre sider. Erstattes af PatientService senere.
    private final DemoData data;

    // Spring giver os DemoData automatisk (constructor injection)
    public PatientController(DemoData data) {
        this.data = data;
    }

    // Viser patientlisten. q er søgeteksten, og status er filteret: alle, aktiv eller afsluttet.
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "alle") String status,
                       Model model) {
        String search = q.trim().toLowerCase();

        List<PatientRow> result = data.patients().stream()
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

        // Fødselsdato er valgfri, så den kan være tom
        String birthDate = patientForm.getBirthDate() == null ? null : patientForm.getBirthDate().format(DATE);

        data.addPatient(
            patientForm.getName().trim(),
            birthDate,
            patientForm.getCpr(),
            patientForm.getPhone());

        // Beskeden vises én gang på listen efter omdirigeringen
        redirect.addFlashAttribute("message", "Patienten " + patientForm.getName().trim() + " er oprettet.");
        return "redirect:/patienter";
    }

    // Viser patientens journal. notat er id på det valgte notat; uden det vises det nyeste.
    @GetMapping("/{id}")
    public String journal(@PathVariable long id,
                          @RequestParam(required = false) Long notat,
                          Model model) {
        PatientRow patient = data.patient(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patienten findes ikke"));

        List<NoteView> notes = data.notesFor(id);
        NoteView selected = notes.stream()
            .filter(n -> notat != null && n.id() == notat)
            .findFirst()
            .orElse(notes.isEmpty() ? null : notes.get(0));

        model.addAttribute("patient", patient);
        model.addAttribute("notes", notes);
        model.addAttribute("selected", selected);
        return "patient-journal";
    }
}
