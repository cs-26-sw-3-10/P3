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
 * Journalnotater for en patient: et nyt notat, at fortsætte en kladde, at rette et
 * signeret notat og at se et notats versioner.
 * De tre første bruger samme formular (journal-entry-form.html) i hver sin tilstand.
 */
@Controller
@RequestMapping("/patienter/{patientId}/notater")
public class JournalController {

    // Formularens tre tilstande
    private static final String MODE_NEW = "new";
    private static final String MODE_DRAFT = "draft";
    private static final String MODE_CORRECTION = "correction";

    private static final String LOCKED_MESSAGE =
        "Et signeret notat er låst og kan ikke ændres. Brug \"Tilføj rettelse\" i stedet.";

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
        addPageData(model, patient, null, MODE_NEW);
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
            addPageData(model, patient, null, MODE_NEW);
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

        model.addAttribute("journalEntryForm", formFrom(note));
        addPageData(model, patient, note, MODE_DRAFT);
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
            addPageData(model, patient, note, MODE_DRAFT);
            return "journal-entry-form";
        }

        data.updateDraft(noteId, journalEntryForm.getDate(), journalEntryForm.getAppointment(), sign,
            journalEntryForm.getSymptoms(), journalEntryForm.getObservations(),
            journalEntryForm.getTreatment(), journalEntryForm.getPlan());

        redirect.addFlashAttribute("message", sign ? "Notatet er signeret og låst." : "Kladden er gemt.");
        return redirectToNote(patientId, noteId);
    }

    // ---------------------------------------------------------------- ret et signeret notat

    // Viser formularen med notatets nuværende tekst, så den kan rettes. Kun for signerede notater.
    @GetMapping("/{noteId}/ret")
    public String editCorrection(@PathVariable long patientId,
                                 @PathVariable long noteId,
                                 Model model,
                                 RedirectAttributes redirect) {
        PatientRow patient = findPatient(patientId);
        NoteView note = findNote(patientId, noteId);

        if (!note.signed()) {
            redirect.addFlashAttribute("message", "En kladde rettes med \"Fortsæt notat\".");
            return redirectToNote(patientId, noteId);
        }

        model.addAttribute("journalEntryForm", formFrom(note));
        addPageData(model, patient, note, MODE_CORRECTION);
        return "journal-entry-form";
    }

    // Gemmer rettelsen som en ny version. Den oprindelige tekst bevares.
    @PostMapping("/{noteId}/ret")
    public String saveCorrection(@PathVariable long patientId,
                                 @PathVariable long noteId,
                                 @Valid @ModelAttribute JournalEntryForm journalEntryForm,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirect) {
        PatientRow patient = findPatient(patientId);
        NoteView note = findNote(patientId, noteId);

        if (!note.signed()) {
            redirect.addFlashAttribute("message", "En kladde rettes med \"Fortsæt notat\".");
            return redirectToNote(patientId, noteId);
        }

        // En rettelse, der ikke ændrer noget, giver kun en tom version
        if (DemoData.sameText(note, journalEntryForm.getSymptoms(), journalEntryForm.getObservations(),
                journalEntryForm.getTreatment(), journalEntryForm.getPlan())) {
            result.reject("unchanged", "Rettelsen er magen til den nuværende tekst. Ret mindst ét felt.");
        }

        if (hasErrors(journalEntryForm, result)) {
            addPageData(model, patient, note, MODE_CORRECTION);
            return "journal-entry-form";
        }

        data.addCorrection(noteId, journalEntryForm.getSymptoms(), journalEntryForm.getObservations(),
            journalEntryForm.getTreatment(), journalEntryForm.getPlan(), journalEntryForm.getReason());

        redirect.addFlashAttribute("message", "Rettelsen er gemt som en ny version.");
        return redirectToNote(patientId, noteId);
    }

    // ---------------------------------------------------------------- versioner

    // Viser alle versioner af et signeret notat, nyeste først
    @GetMapping("/{noteId}/versioner")
    public String versions(@PathVariable long patientId,
                           @PathVariable long noteId,
                           Model model,
                           RedirectAttributes redirect) {
        PatientRow patient = findPatient(patientId);
        NoteView note = findNote(patientId, noteId);

        if (!note.signed()) {
            redirect.addFlashAttribute("message", "En kladde har ingen versioner, før den er signeret.");
            return redirectToNote(patientId, noteId);
        }

        model.addAttribute("patient", patient);
        model.addAttribute("note", note);
        model.addAttribute("versions", data.versionsFor(noteId));
        return "note-versions";
    }

    // ---------------------------------------------------------------- hjælpemetoder

    // Fælles regel: et notat uden tekst giver ingen mening, hverken som kladde, signeret eller rettet
    private boolean hasErrors(JournalEntryForm form, BindingResult result) {
        if (form.hasNoText()) {
            result.reject("empty", "Skriv i mindst ét af de fire felter.");
        }
        return result.hasErrors();
    }

    // Udfylder formularen med notatets nuværende tekst
    private static JournalEntryForm formFrom(NoteView note) {
        JournalEntryForm form = new JournalEntryForm();
        form.setDate(note.date());
        form.setAppointment(note.appointment());
        form.setSymptoms(note.symptoms());
        form.setObservations(note.observations());
        form.setTreatment(note.treatment());
        form.setPlan(note.plan());
        return form;
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
     * note er notatet, der redigeres eller rettes, eller null for et nyt notat.
     */
    private void addPageData(Model model, PatientRow patient, NoteView note, String mode) {
        // Indtil aftaler kommer fra databasen, kan man vælge patientens næste aftale
        // og den aftale, notatet allerede er knyttet til
        List<String> appointments = new ArrayList<>();
        if (patient.nextAppointment() != null) {
            appointments.add(patient.nextAppointment());
        }
        if (note != null && note.appointment() != null && !appointments.contains(note.appointment())) {
            appointments.add(note.appointment());
        }

        String base = "/patienter/" + patient.id() + "/notater";
        String formAction = switch (mode) {
            case MODE_DRAFT -> base + "/" + note.id();
            case MODE_CORRECTION -> base + "/" + note.id() + "/ret";
            default -> base;
        };
        String pageTitle = switch (mode) {
            case MODE_DRAFT -> "Fortsæt journalnotat";
            case MODE_CORRECTION -> "Ret journalnotat";
            default -> "Nyt journalnotat";
        };

        model.addAttribute("patient", patient);
        model.addAttribute("note", note);
        model.addAttribute("appointments", appointments);
        model.addAttribute("mode", mode);
        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
    }
}
