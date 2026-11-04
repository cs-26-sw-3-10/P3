package cs_26_sw_3_10.P3Project.ui;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalendarController {

    // Midlertidige typer til dummy-data. Erstattes af rigtige klasser fra model/ senere.
    record AppointmentView(String time, String patient, String status) {}
    record DayView(String label, List<AppointmentView> appointments) {}

    // Forsiden sender videre til kalenderen
    @GetMapping("/")
    public String home() {
        return "redirect:/kalender";
    }

    @GetMapping("/kalender")
    public String calendar(Model model) {
        List<DayView> week = List.of(
            new DayView("Man. 2", List.of(
                new AppointmentView("09:00–10:00", "Patient A", "Planlagt"),
                new AppointmentView("13:00–14:00", "Patient B", "Planlagt"))),
            new DayView("Tir. 3", List.of(
                new AppointmentView("10:00–11:00", "Patient C", "Påmindet"))),
            new DayView("Ons. 4", List.of(
                new AppointmentView("08:00–09:00", "Patient D", "Afholdt"),
                new AppointmentView("12:00–13:00", "Patient E", "Aflyst"))),
            new DayView("Tor. 5", List.of()),
            new DayView("Fre. 6", List.of(
                new AppointmentView("11:00–12:00", "Patient F", "Planlagt"))));

        model.addAttribute("weekLabel", "Uge 45");
        model.addAttribute("days", week);
        return "calendar"; // = templates/calendar.html
    }
}