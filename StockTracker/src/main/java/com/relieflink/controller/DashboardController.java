package com.relieflink.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        System.out.println("Inside the Dashbaoard");
        return "dashboard";
    }

    @GetMapping("/donate")
    public String donatePage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        return "donate";
    }

    @GetMapping("/request")
    public String requestPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        return "request";
    }

    @GetMapping("/matches")
    public String matchesPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        return "matches";
    }

    @GetMapping("/admin")
    public String adminPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }
        return "admin";
    }

    @GetMapping("/guidelines")
    public String guidelinesPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        return "guidelines";
    }

    @GetMapping("/emergency-contacts")
    public String emergencyContactsPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        return "emergency-contacts";
    }
}
