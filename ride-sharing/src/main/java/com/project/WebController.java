package com.project;
import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class WebController {
 private final ProjectService service;
 public WebController(ProjectService service){this.service=service;}
 @ModelAttribute("signedIn") boolean signedIn(Principal p){return p!=null;}
 @GetMapping("/") String home(){return "home";}
 @GetMapping("/login") String login(){return "login";}
 @GetMapping("/register") String register(){return "register";}
 @PostMapping("/register") String register(@RequestParam String username,@RequestParam String password,RedirectAttributes flash){service.register(username,password);flash.addFlashAttribute("message","Account created. Please sign in.");return "redirect:/login";}
 @GetMapping("/rides") String rides(@RequestParam(defaultValue="") String from,@RequestParam(defaultValue="") String to,@RequestParam(defaultValue="") String date,Model m){m.addAttribute("rides",service.rides(from,to,date));m.addAttribute("from",from);m.addAttribute("to",to);m.addAttribute("date",date);return "rides";}
 @GetMapping("/rides/new") String publish(){return "publish";}
 @PostMapping("/rides/new") String publish(Principal p,@RequestParam String from,@RequestParam String to,@RequestParam String departure,@RequestParam BigDecimal price,@RequestParam int seats,@RequestParam String car,RedirectAttributes flash){service.publish(p.getName(),from,to,departure,price,seats,car);flash.addFlashAttribute("message","Your ride is published.");return "redirect:/account";}
 @PostMapping("/rides/{id}/book") String book(Principal p,@PathVariable long id,RedirectAttributes flash){service.book(p.getName(),id);flash.addFlashAttribute("message","Seat booked successfully.");return "redirect:/account";}
 @PostMapping("/bookings/{id}/cancel") String cancel(Principal p,@PathVariable long id,RedirectAttributes flash){service.cancel(p.getName(),id);flash.addFlashAttribute("message","Booking cancelled and seat restored.");return "redirect:/account";}
 @GetMapping("/account") String account(Principal p,Model m){m.addAttribute("bookings",service.bookings(p.getName()));m.addAttribute("published",service.published(p.getName()));return "account";}
 @ExceptionHandler({IllegalArgumentException.class,java.time.DateTimeException.class,org.springframework.dao.DataIntegrityViolationException.class}) String invalid(Exception e,Model m,Principal p,jakarta.servlet.http.HttpServletResponse response){m.addAttribute("signedIn",p!=null);response.setStatus(400);m.addAttribute("problem",e instanceof UserInputException?e.getMessage():"Please check the information you entered and try again.");return "problem";}
}
