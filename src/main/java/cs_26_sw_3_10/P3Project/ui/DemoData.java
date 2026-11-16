package cs_26_sw_3_10.P3Project.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * MIDLERTIDIG: opdigtede patienter og journalnotater i hukommelsen, så siderne kan
 * bygges, før databasen er klar. Data nulstilles ved genstart.
 * Erstattes af PatientService og JournalService, når de findes.
 */
@Component
public class DemoData {

    // Én patient, som siderne viser den. cpr er kun de sidste 4 cifre.
    public record PatientRow(long id, String name, String birthDate, String cpr, String phone,
                             String nextAppointment, String status) {}

    // Ét journalnotat, som journalsiden viser det
    public record NoteView(long id, long patientId, LocalDate date, String dateLabel, String status,
                           String symptoms, String observations, String treatment, String plan,
                           String signedAt) {
        public boolean signed() {
            return "Signeret".equals(status);
        }
    }

    private static final Locale DANISH = Locale.forLanguageTag("da");
    private static final DateTimeFormatter NOTE_DATE = DateTimeFormatter.ofPattern("EEE d. MMM yyyy", DANISH);

    private final AtomicLong nextPatientId = new AtomicLong(7);

    private final List<PatientRow> patients = new CopyOnWriteArrayList<>(List.of(
        new PatientRow(1, "Patient A", "12-03-1985", "1111", "20 11 22 33", "Man. 2. nov. · 09:00", "Aktiv"),
        new PatientRow(2, "Patient B", "04-07-1972", "1111", "30 44 55 66", "Man. 2. nov. · 13:00", "Aktiv"),
        new PatientRow(3, "Patient C", "23-11-1990", "2222", "40 77 88 99", "Tir. 3. nov. · 10:00", "Aktiv"),
        new PatientRow(4, "Patient D", "15-01-1968", "2222", "50 12 34 56", null, "Inaktiv"),
        new PatientRow(5, "Patient E", "30-09-2001", "3333", "60 98 76 54", "Fre. 6. nov. · 11:00", "Aktiv"),
        new PatientRow(6, "Patient F", "08-05-1979", "3333", "70 24 68 10", null, "Afsluttet")));

    private final List<NoteView> notes = new CopyOnWriteArrayList<>(List.of(
        note(1, 1, LocalDate.of(2026, 10, 29), "Åbent",
            "Hovedpine og spænding i nakken siden sidst.",
            "Øget spænding i nakke og skuldre.",
            "Behandling af nakke og kranie, 60 min.",
            "Ny tid om en uge.", null),
        note(2, 1, LocalDate.of(2026, 10, 22), "Signeret",
            "Hovedpine 3-4 gange om ugen.",
            "Spænding i nakken, nedsat bevægelighed til venstre.",
            "Behandling af nakke og skuldre, 60 min.",
            "Følge op på hovedpinen næste gang.", "22. okt. 2026, 14:12"),
        note(3, 1, LocalDate.of(2026, 10, 15), "Signeret",
            "Første besøg. Hovedpine og dårlig søvn.",
            "Generel spænding i overkroppen.",
            "Introduktion til behandlingen og første behandling, 75 min.",
            "Behandling en gang om ugen i fire uger.", "15. okt. 2026, 15:40"),
        note(4, 2, LocalDate.of(2026, 10, 26), "Signeret",
            "Smerter i lænden efter havearbejde.",
            "Spænding i lænd og bækken.",
            "Behandling af lænd og bækken, 60 min.",
            "Ny tid om to uger.", "26. okt. 2026, 13:05")));

    // ---------------------------------------------------------------- patienter

    public List<PatientRow> patients() {
        return List.copyOf(patients);
    }

    public Optional<PatientRow> patient(long id) {
        return patients.stream().filter(p -> p.id() == id).findFirst();
    }

    public PatientRow addPatient(String name, String birthDate, String cpr, String phone) {
        // En ny patient starter som inaktiv, indtil der bookes en aftale (jf. tilstandsdiagrammet)
        PatientRow patient = new PatientRow(nextPatientId.getAndIncrement(), name, birthDate, cpr, phone,
            null, "Inaktiv");
        patients.add(patient);
        return patient;
    }

    // ---------------------------------------------------------------- journalnotater

    /** Patientens notater, nyeste først. */
    public List<NoteView> notesFor(long patientId) {
        return notes.stream()
            .filter(n -> n.patientId() == patientId)
            .sorted(Comparator.comparing(NoteView::date).reversed())
            .toList();
    }

    private static NoteView note(long id, long patientId, LocalDate date, String status, String symptoms,
                                 String observations, String treatment, String plan, String signedAt) {
        return new NoteView(id, patientId, date, label(date), status,
            symptoms, observations, treatment, plan, signedAt);
    }

    // "tor. 29. okt. 2026" -> "Tor. 29. okt. 2026"
    private static String label(LocalDate date) {
        String text = date.format(NOTE_DATE);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
