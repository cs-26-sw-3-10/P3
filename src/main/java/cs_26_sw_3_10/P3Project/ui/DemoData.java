package cs_26_sw_3_10.P3Project.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    /**
     * Ét journalnotat, som journalsiden viser det. Teksten er altid den nyeste version.
     * appointment er tom, hvis notatet ikke hører til en aftale, og correctedAt er tom,
     * hvis notatet aldrig er rettet.
     */
    public record NoteView(long id, long patientId, LocalDate date, String dateLabel, String status,
                           String symptoms, String observations, String treatment, String plan,
                           String signedAt, String appointment, String correctedAt) {
        public boolean signed() {
            return "Signeret".equals(status);
        }

        public boolean corrected() {
            return correctedAt != null;
        }
    }

    /**
     * Én version af et signeret notat. Version 1 er teksten, som den så ud, da notatet
     * blev signeret; hver rettelse giver en ny version. Versioner ændres eller slettes aldrig.
     */
    public record NoteVersion(long noteId, int number, String kind, String symptoms, String observations,
                              String treatment, String plan, String savedAt, String reason) {}

    private static final Locale DANISH = Locale.forLanguageTag("da");
    private static final DateTimeFormatter NOTE_DATE = DateTimeFormatter.ofPattern("EEE d. MMM yyyy", DANISH);
    private static final DateTimeFormatter SIGNED_AT = DateTimeFormatter.ofPattern("d. MMM yyyy, HH:mm", DANISH);

    private final AtomicLong nextPatientId = new AtomicLong(7);
    private final AtomicLong nextNoteId = new AtomicLong(5);

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

    private final List<NoteVersion> versions = new CopyOnWriteArrayList<>();

    public DemoData() {
        // De signerede eksempelnotater får deres oprindelige version
        notes.stream().filter(NoteView::signed).forEach(n -> versions.add(originalVersion(n, n.signedAt())));

        // Et eksempel på en rettelse, så versionshistorikken kan ses med det samme
        applyCorrection(2,
            "Hovedpine 3-4 gange om ugen. Sover dårligt i perioder med hovedpine.",
            "Spænding i nakken, nedsat bevægelighed til venstre.",
            "Behandling af nakke og skuldre, 60 min.",
            "Følge op på hovedpinen og søvnen næste gang.",
            "Tilføjet oplysning om søvn efter opfølgende samtale.",
            "24. okt. 2026, 09:30");
    }

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

    /** Patientens notater, nyeste først. Notater fra samme dag står med det senest oprettede øverst. */
    public List<NoteView> notesFor(long patientId) {
        return notes.stream()
            .filter(n -> n.patientId() == patientId)
            .sorted(Comparator.comparing(NoteView::date).thenComparingLong(NoteView::id).reversed())
            .toList();
    }

    /** Tilføjer et nyt notat. Et signeret notat får tidspunktet for signeringen og sin første version. */
    public NoteView addNote(long patientId, LocalDate date, String appointment, boolean sign,
                            String symptoms, String observations, String treatment, String plan) {
        String signedAt = sign ? now() : null;

        NoteView note = new NoteView(nextNoteId.getAndIncrement(), patientId, date, label(date),
            sign ? "Signeret" : "Åbent", symptoms, observations, treatment, plan, signedAt,
            blankToNull(appointment), null);
        notes.add(note);

        if (sign) {
            versions.add(originalVersion(note, signedAt));
        }
        return note;
    }

    /** Finder et notat, men kun hvis det hører til den angivne patient. */
    public Optional<NoteView> findNote(long patientId, long noteId) {
        return notes.stream()
            .filter(n -> n.id() == noteId && n.patientId() == patientId)
            .findFirst();
    }

    /**
     * Gemmer ændringer i en kladde. Et signeret notat er låst og kan ikke ændres,
     * så det giver en fejl, hvis man forsøger.
     */
    public NoteView updateDraft(long noteId, LocalDate date, String appointment, boolean sign,
                                String symptoms, String observations, String treatment, String plan) {
        int index = indexOf(noteId);
        NoteView existing = notes.get(index);
        if (existing.signed()) {
            throw new IllegalStateException("Et signeret notat kan ikke ændres");
        }

        String signedAt = sign ? now() : null;
        NoteView updated = new NoteView(existing.id(), existing.patientId(), date, label(date),
            sign ? "Signeret" : "Åbent", symptoms, observations, treatment, plan, signedAt,
            blankToNull(appointment), null);
        notes.set(index, updated);

        if (sign) {
            versions.add(originalVersion(updated, signedAt));
        }
        return updated;
    }

    // ---------------------------------------------------------------- versioner og rettelser

    /** Notatets versioner, nyeste først. Den første i listen er den, journalen viser. */
    public List<NoteVersion> versionsFor(long noteId) {
        return versions.stream()
            .filter(v -> v.noteId() == noteId)
            .sorted(Comparator.comparingInt(NoteVersion::number).reversed())
            .toList();
    }

    /**
     * Retter et signeret notat. Den gamle tekst bevares som en tidligere version,
     * og rettelsen bliver den nye version, som journalen viser.
     */
    public NoteView addCorrection(long noteId, String symptoms, String observations, String treatment,
                                  String plan, String reason) {
        return applyCorrection(noteId, symptoms, observations, treatment, plan, reason, now());
    }

    /** Sand, hvis teksten er den samme som notatets nuværende tekst. */
    public static boolean sameText(NoteView note, String symptoms, String observations, String treatment,
                                   String plan) {
        return same(note.symptoms(), symptoms) && same(note.observations(), observations)
            && same(note.treatment(), treatment) && same(note.plan(), plan);
    }

    private NoteView applyCorrection(long noteId, String symptoms, String observations, String treatment,
                                     String plan, String reason, String savedAt) {
        int index = indexOf(noteId);
        NoteView existing = notes.get(index);
        if (!existing.signed()) {
            throw new IllegalStateException("Kun signerede notater rettes; en kladde ændres direkte");
        }

        int number = versionsFor(noteId).size() + 1;
        versions.add(new NoteVersion(noteId, number, "Rettelse", symptoms, observations, treatment, plan,
            savedAt, blankToNull(reason)));

        NoteView corrected = new NoteView(existing.id(), existing.patientId(), existing.date(),
            existing.dateLabel(), existing.status(), symptoms, observations, treatment, plan,
            existing.signedAt(), existing.appointment(), savedAt);
        notes.set(index, corrected);
        return corrected;
    }

    // ---------------------------------------------------------------- hjælpemetoder

    private int indexOf(long noteId) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).id() == noteId) {
                return i;
            }
        }
        throw new IllegalArgumentException("Notatet findes ikke: " + noteId);
    }

    private static NoteVersion originalVersion(NoteView note, String signedAt) {
        return new NoteVersion(note.id(), 1, "Oprindelig", note.symptoms(), note.observations(),
            note.treatment(), note.plan(), signedAt, null);
    }

    private static NoteView note(long id, long patientId, LocalDate date, String status, String symptoms,
                                 String observations, String treatment, String plan, String signedAt) {
        return new NoteView(id, patientId, date, label(date), status,
            symptoms, observations, treatment, plan, signedAt, null, null);
    }

    // Tom tekst og null regnes for det samme, og mellemrum i enderne ignoreres
    private static boolean same(String a, String b) {
        return Objects.equals(blankToNull(a == null ? null : a.strip()), blankToNull(b == null ? null : b.strip()));
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    private static String now() {
        return LocalDateTime.now().format(SIGNED_AT);
    }

    // "tor. 29. okt. 2026" -> "Tor. 29. okt. 2026"
    private static String label(LocalDate date) {
        String text = date.format(NOTE_DATE);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
