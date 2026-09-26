package com.example.seminar_booking_website;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.ui.Model;

import com.example.seminar_booking_website.model.User; // 🚨 this tells Spring what "User" is
import com.example.seminar_booking_website.repository.UserRepository; // 🚨 same for repo
import com.example.seminar_booking_website.model.Booking;
import com.example.seminar_booking_website.repository.BookingRepository;

import java.time.LocalTime;
import java.time.LocalDate;

@Controller
public class SeminarController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "home"; // this will load home.html from templates
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // loads login.html from templates
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // destroy the session
        return "redirect:/"; // go back to home
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               Model model,
                               HttpSession session) {
    User user = userRepository.findByEmail(email);

    if (user == null) {
        model.addAttribute("error", "Email not found.");
        return "login";
    }

    if (user.getPassword() == null || user.getPassword().isEmpty()) {

        // First-time password set 🚀
        user.setPassword(password);
        userRepository.save(user);
        session.setAttribute("user", user); // store user in session
        model.addAttribute("message", "Password set successfully! You're now logged in.");
        return "redirect:/";
    } 
    else {
        // Password already exists, validate it
        if (user.getPassword().equals(password)) {
            session.setAttribute("user",user);
            model.addAttribute("message", "Login successful!");
            return "redirect:/";
        } else {
            model.addAttribute("error", "Incorrect password");
            return "login";
        }
    }
}


    @GetMapping("/event")
    public String showEventsPage(Model model) {
    List<Booking> allBookings = bookingRepository.findAll();

    LocalDate today = LocalDate.now();
    LocalTime now = LocalTime.now();

    List<Booking> ongoingEvents = new ArrayList<>();
    List<Booking> upcomingEvents = new ArrayList<>();

    for (Booking booking : allBookings) {
        if (booking.getDate().equals(today)) {
            // Compare times too!
            if (now.isAfter(booking.getTime()) && now.isBefore(booking.getEndTime())) {
                ongoingEvents.add(booking);
            } else if (now.isBefore(booking.getTime())) {
                upcomingEvents.add(booking);
            }
        } else if (booking.getDate().isAfter(today)) {
            upcomingEvents.add(booking);
        }
    }

    model.addAttribute("ongoingEvents", ongoingEvents);
    model.addAttribute("upcomingEvents", upcomingEvents);

    return "event";
    }


    @GetMapping("/apj")
    public String apj(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "apj"; // loads apj.html
    }

    @GetMapping("/apjlab")
    public String apjlab(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "apjlab"; // loads apj.html
    }

    @GetMapping("/db")
    public String db(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "db"; // loads db.html
    }

    @GetMapping("/bfsi1")
    public String bfsi1(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "bfsi1"; // loads db.html
    }

    @GetMapping("/bfsi2")
    public String bfsi2(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "bfsi2"; // loads db.html
    }

    @GetMapping("/sapphire")
    public String sapphire(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "sapphire"; // loads sapphire.html
    }

    @GetMapping("/ssh")
    public String ssh(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "ssh"; // loads ssh.html
    }

    @GetMapping("/smb")
    public String smb(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "smb"; // loads smb.html
    }

    @GetMapping("/smbtraining")
    public String smbtraining(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("user")!=null);
        return "smbtraining"; // loads smb.html
    }



    @PostMapping("/bookHall")
    @ResponseBody
    public String bookHall(@RequestParam String eventName,
                        @RequestParam String eventDate,
                        @RequestParam String eventTime,
                        @RequestParam String endTime,
                        @RequestParam String hallName,
                        @RequestParam String venue,
                        HttpSession session) {

        LocalDate date = LocalDate.parse(eventDate);
        LocalTime start = LocalTime.parse(eventTime);
        LocalTime end = LocalTime.parse(endTime);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today)) {
            return "❌ Booking not possible for past dates!";
        }


        if (date.equals(today) && start.isBefore(now)) {
            return "❌ You can't book an event in the past! Try a future time.";
        }

        User user = (User) session.getAttribute("user");
        if (user == null) return "login";

        //catch bookings from previous nightdate.plusDays(1) //catch spillover events into the next day
        List<Booking> existingBookings = bookingRepository.findByHallNameAndDateBetween(hallName,date.minusDays(1),date.plusDays(1));

        for (Booking booking : existingBookings) {
            boolean exactDuplicate = booking.getDate().equals(date) &&
                                    booking.getTime().equals(start) &&
                                    booking.getEndTime().equals(end) &&
                                    booking.getHallName().equalsIgnoreCase(hallName) &&
                                    booking.getVenue().equalsIgnoreCase(venue);
                                    
            if (exactDuplicate) {
                return "❌ This slot is already booked for this hall! Please choose a different time.";
            }
        }

        for (Booking booking : existingBookings) {
            LocalDate bookingDate = booking.getDate();
            LocalTime startTime = booking.getTime();
            LocalTime bookingendTime = booking.getEndTime();

            LocalDateTime existingStartDT = LocalDateTime.of(bookingDate, startTime);

            // ✨ Here's the trick:
            LocalDate endDate = bookingendTime.isBefore(startTime) ? bookingDate.plusDays(1) : bookingDate;
            LocalDateTime existingEndDT = LocalDateTime.of(endDate, bookingendTime);

            LocalDateTime bufferStartDT = existingStartDT.minusHours(1);
            LocalDateTime bufferEndDT = existingEndDT.plusHours(1);

            LocalDateTime requestStartDT = LocalDateTime.of(date, start);
            LocalDateTime requestEndDT = LocalDateTime.of(date, end);

            // if the event crosses midnight
            if (end.isBefore(start)) {
                requestEndDT = requestEndDT.plusDays(1);
            }

            boolean overlaps = requestStartDT.isBefore(bufferEndDT) && requestEndDT.isAfter(bufferStartDT);
            if (overlaps) {
                return "❌ This hall is blocked due to another event from " + booking.getTime() + " to " + booking.getEndTime()
                    + ". You can't book within 1 hour before or after it.";
            }
        }

        Booking b = new Booking();
        b.setEventName(eventName);
        b.setHallName(hallName);
        b.setVenue(venue);
        b.setDate(date);
        b.setTime(start);
        b.setEndTime(end);
        b.setUser(user);
        bookingRepository.save(b);
        return "✅ Booking Confirmed!";
    }

    @GetMapping("/mybooking")
    public String viewBookings(HttpSession session, Model model) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        model.addAttribute("isLoggedIn", false);
        return "redirect:/login";
    }
    model.addAttribute("isLoggedIn", true);
    List<Booking> bookings = bookingRepository.findByUser(user);
    model.addAttribute("bookings", bookings);
    return "mybooking";
    }

    @PostMapping("/deleteBooking")
    public ResponseEntity<?> deleteBooking(@RequestParam Long id, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
    Booking booking = bookingRepository.findById(id).orElse(null);
    if (booking != null && booking.getUser().getId().equals(user.getId())) {
        bookingRepository.delete(booking);
    }
    return ResponseEntity.ok("Booking deleted"); 
    }

   @GetMapping("/bookings")
    public String viewAllBookings(Model model, HttpSession session) {
        model.addAttribute("isLoggedIn", session.getAttribute("user") != null);
        List<Booking> bookings = bookingRepository.findAll();
        model.addAttribute("bookings", bookings);

        List<String> allPossibleHalls = List.of("APJ Seminar Hall", "APJ Block  Lab", "Diamond Seminar Hall", "BFSI Hall 1", "BFSI Hall 2", "Sapphire Seminar Hall", "Sapphire Block Lab", "SMB Seminar Hall", "SMB Training Hall");
        List<LocalDate> dates = bookings.stream()
        .map(Booking::getDate)
        .distinct()
        .sorted()
        .collect(Collectors.toList());

        model.addAttribute("hallNames", allPossibleHalls);
        model.addAttribute("dates", dates);


        model.addAttribute("hallNames", allPossibleHalls);
        model.addAttribute("dates", dates);

        return "bookings";
    }


    // add more if you have more HTML files
}
