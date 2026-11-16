package cs_26_sw_3_10.P3Project.ui;

import java.time.LocalDate;
import java.util.ArrayList;
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
import cs_26_sw_3_10.P3Project.ui.form.JournalEntryForm;
import jakarta.validation.Valid;

/**
 * Journalnotater for en patient: et nyt notat og at fortsætte en kladde.
 * Begge bruger samme formular (journal-entry-form.html).
 */
@Controller
@RequestMapping("/patienter/{patientId}/notater")
public class JournalController {

    private static final String LOCKED_MESSAGE = "Et signeret notat er låst og kan ikke ændres.";

    // Midlertidige dummy-data, som deles med de andre sider. Erstattes af JournalService senere.
    private final DemoData data;

    public JournalController(DemoData data) {
        this.data = data;
    }

    // ---------------------------------------------------------------- nyt notat

    // Viser en tom formular med dagens dato udfyldt
    @GetMapping("/ny")
    public String newNote(@PathVariable long patientId, Model model) {
        PatientRow patient = findPatient(patientId);

        JournalEntryForm form = new JournalEntryForm();
        form.setDate(LocalDate.now());

        model.addAttribute("journalEntryForm", form);
        addPageData(model, patient, null);
        return "journal-entry-form";
    }

    // Modtager et nyt notat. action er "draft" (Gem kladde) eller "sign" (Signér og lås).
    @PostMapping
    public String create(@PathVariable long patientId,
                         @Valid @ModelAttribute JournalEntryForm journalEntryForm,
                         BindingResult result,
                         @RequestParam(defaultValue = "draft") String action,
                         Model model,
                         RedirectAttributes redirect) {
        PatientRow patient = findPatient(patientId);
        boolean sign = "sign".equals(action);

        if (hasErrors(journalEntryForm, result)) {
            addPageData(model, patient, null);
            return "journal-entry-form";
        }

        NoteView note = data.addNote(patientId, journalEntryForm.getDate(), journalEntryForm.getAppointment(), sign,
            journalEntryForm.getSymptoms(), journalEntryForm.getObservations(),
            journalEntryForm.getTreatment(), journalEntryForm.getPlan());

        redirect.addFlashAttribute("message", sign ? "Notatet er signeret og låst." : "Notatet er gemt som kladde.");
        return redirectToNote(patientId, note.id());
    }

    // ---------------------------------------------------------------- fortsæt en kladde

    // Viser en kladde i formularen, så den kan gøres færdig. Signerede notater kan ikke åbnes her.
    @GetMapping("/{noteId}/rediger")
    public String editDraft(@PathVariable long patientId,
                            @PathVariable long noteId,
                            Model model,
                            RedirectAttributes redirect) {
        PatientRow patient = findPatient(patientId);
        NoteView note = findNote(patientId, noteId);

        if (note.signed()) {
            redirect.addFlashAttribute("message", LOCKED_MESSAGE);
            return redirectToNote(patientId, noteId);
        }

        JournalEntryForm form = new JournalEntryForm();
        form.setDate(note.date());
        form.setAppointment(note.appointment());
        form.setSymptoms(note.symptoms());
        form.setObservations(note.observations());
        form.setTreatment(note.treatment());
        form.setPlan(note.plan());

        model.addAttribute("journalEntryForm", form);
        addPageData(model, patient, note);
        return "journal-entry-form";
    }

    // Gemmer ændringerne i en kladde, enten som kladde igen eller signeret
    @PostMapping("/{noteId}")
    public String updateDraft(@PathVariable long patientId,
                              @PathVariable long noteId,
                              @Valid @ModelAttribute JournalEntryForm journalEntryForm,
                              BindingResult result,
                              @RequestParam(defaultValue = "draft") String action,
                              Model model,
                              RedirectAttributes redirect) {
        PatientRow patient = findPatient(patientId);
        NoteView note = findNote(patientId, noteId);
        boolean sign = "sign".equals(action);

        // Beskytter også mod at sende formularen igen, efter notatet er blevet signeret
        if (note.signed()) {
            redirect.addFlashAttribute("message", LOCKED_MESSAGE);
            return redirectToNote(patientId, noteId);
        }

        if (hasErrors(journalEntryForm, result)) {
            addPageData(model, patient, note);
            return "journal-entry-form";
        }

        data.updateDraft(noteId, journalEntryForm.getDate(), journalEntryForm.getAppointment(), sign,
            journalEntryForm.getSymptoms(), journalEntryForm.getObservations(),
            journalEntryForm.getTreatment(), journalEntryForm.getPlan());

        redirect.addFlashAttribute("message", sign ? "Notatet er signeret og låst." : "Kladden er gemt.");
        return redirectToNote(patientId, noteId);
    }

    // ---------------------------------------------------------------- hjælpemetoder

    // Fælles regel: et notat uden tekst giver ingen mening, hverken som kladde eller signeret
    private boolean hasErrors(JournalEntryForm form, BindingResult result) {
        if (form.hasNoText()) {
            result.reject("empty", "Skriv i mindst ét af de fire felter.");
        }
        return result.hasErrors();
    }

    private PatientRow findPatient(long patientId) {
        return data.patient(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patienten findes ikke"));
    }

    private NoteView findNote(long patientId, long noteId) {
        return data.findNote(patientId, noteId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notatet findes ikke"));
    }

    // Tilbage til journalen med notatet valgt
    private static String redirectToNote(long patientId, long noteId) {
        return "redirect:/patienter/" + patientId + "?notat=" + noteId;
    }

    /**
     * Data, siden skal bruge, både første gang og når formularen vises igen med fejl.
     * note er kladden, der redigeres, eller null for et nyt notat.
     */
    private void addPageData(Model model, PatientRow patient, NoteView note) {
        // Indtil aftaler kommer fra databasen, kan man vælge patientens næste aftale
        // og den aftale, kladden allerede er knyttet til
        List<String> appointments = new ArrayList<>();
        if (patient.nextAppointment() != null) {
            appointments.add(patient.nextAppointment());
        }
        if (note != null && note.appointment() != null && !appointments.contains(note.appointment())) {
            appointments.add(note.appointment());
        }

        boolean editing = note != null;
        String formAction = editing
            ? "/patienter/" + patient.id() + "/notater/" + note.id()
            : "/patienter/" + patient.id() + "/notater";

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("editing", editing);
        model.addAttribute("formAction", formAction);
    }
}
