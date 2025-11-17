package com.relieflink.controller;

import com.relieflink.model.*;
import com.relieflink.repository.DataStore;
import com.relieflink.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    //autowired the ApiController to the services
    @Autowired
    private DonationService donationService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private MatchingService matchingService;
    
    @Autowired
    private DataStore dataStore;

    @PostMapping("/donations")
    //mapping it to the post method of rest API on donation page, so that data is requested and a donation is created and added to the repository data
    public Donation createDonation(@RequestBody Donation donation, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        donation.setDonorId(userId);
        donation.setDonorName(username);
        System.out.println("Creating a donation");
        return donationService.createDonation(donation);
    }

    @GetMapping("/donations")
    //binds to the get method of rest api on donations page, returns the list of donations to the page using restAPI
    public List<Donation> getAllDonations() {
        return donationService.getAllDonations();
    }

    @PostMapping("/requests")
    //binds to the post method of rest API on the requests page, the method request the data from the page and create request and add them to the repository data
    public Request createRequest(@RequestBody Request request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        request.setRequesterId(userId);
        request.setRequesterName(username);
        return requestService.createRequest(request);
    }

    //similarly other pages are too mapped to the respective rest aPI methods
    @GetMapping("/requests")
    public List<Request> getAllRequests() {
        return requestService.getAllRequests();
    }

    @PostMapping("/matches/find")
    public List<Match> findMatches(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only admins can run matching.");
        }
        return matchingService.findMatches();
    }

    @GetMapping("/matches")
    public List<Match> getAllMatches() {
        return matchingService.getAllMatches();
    }

    @PostMapping("/admin/reset")
    public String resetSystem() {
        dataStore.resetAll();
        return "{\"success\": true, \"message\": \"System reset successfully\"}";
    }

    @GetMapping("/admin/backup")
    public Map<String, Object> backupData() {
        return dataStore.exportData();
    }

    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        return dataStore.findAllUsers();
    }

    @GetMapping("/user")
    public User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return dataStore.findUserById(userId).orElse(null);
    }

    @GetMapping("/activity")
    public List<Map<String, Object>> getActivityFeed() {
        List<Map<String, Object>> activities = new java.util.ArrayList<>();

        // Get recent donations
        List<Donation> recentDonations = donationService.getAllDonations().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .toList();

        // Get recent requests
        List<Request> recentRequests = requestService.getAllRequests().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .toList();

        // Get recent matches
        List<Match> recentMatches = matchingService.getAllMatches().stream()
            .sorted((a, b) -> b.getMatchedAt().compareTo(a.getMatchedAt()))
            .limit(5)
            .toList();

        // Add donation activities
        for (Donation donation : recentDonations) {
            Map<String, Object> activity = new java.util.HashMap<>();
            activity.put("type", "donation");
            activity.put("message", donation.getItemName() + " donated by " + donation.getDonorName() + " in " + donation.getLocation());
            activity.put("timestamp", donation.getCreatedAt());
            activity.put("status", donation.isMatched() ? "matched" : "pending");
            activities.add(activity);
        }

        // Add request activities
        for (Request request : recentRequests) {
            Map<String, Object> activity = new java.util.HashMap<>();
            activity.put("type", "request");
            activity.put("message", request.getItemName() + " requested by " + request.getRequesterName() + " in " + request.getLocation());
            activity.put("timestamp", request.getCreatedAt());
            activity.put("status", request.isMatched() ? "matched" : "pending");
            activities.add(activity);
        }

        // Add match activities
        for (Match match : recentMatches) {
            Map<String, Object> activity = new java.util.HashMap<>();
            activity.put("type", "match");
            activity.put("message", match.getItemName() + " matched for " + match.getRequesterName() + " in " + match.getLocation());
            activity.put("timestamp", match.getMatchedAt());
            activity.put("status", "completed");
            activities.add(activity);
        }

        // Sort by timestamp (most recent first) and limit to 20
        return activities.stream()
            .sorted((a, b) -> ((java.time.LocalDateTime) b.get("timestamp")).compareTo((java.time.LocalDateTime) a.get("timestamp")))
            .limit(20)
            .toList();
    }

    @GetMapping("/guidelines")
    public Map<String, String> getPublicGuidelinesContent() {
        Map<String, String> response = new java.util.HashMap<>();
        response.put("content", dataStore.getGuidelinesContent());
        return response;
    }

    @GetMapping("/admin/guidelines")
    public Map<String, String> getGuidelinesContent(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only admins can access guidelines content");
        }
        Map<String, String> response = new java.util.HashMap<>();
        response.put("content", dataStore.getGuidelinesContent());
        return response;
    }

    @PutMapping("/admin/guidelines")
    public Map<String, String> updateGuidelinesContent(@RequestBody Map<String, String> request, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only admins can update guidelines content");
        }
        String content = request.get("content");
        dataStore.setGuidelinesContent(content);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Guidelines updated successfully");
        return response;
    }

    @GetMapping("/emergency")
    public Map<String, String> getPublicEmergencyContent() {
        Map<String, String> response = new java.util.HashMap<>();
        response.put("content", dataStore.getEmergencyContent());
        return response;
    }

    @GetMapping("/admin/emergency")
    public Map<String, String> getEmergencyContent(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only admins can access emergency content");
        }
        Map<String, String> response = new java.util.HashMap<>();
        response.put("content", dataStore.getEmergencyContent());
        return response;
    }

    @PutMapping("/admin/emergency")
    public Map<String, String> updateEmergencyContent(@RequestBody Map<String, String> request, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only admins can update emergency content");
        }
        String content = request.get("content");
        dataStore.setEmergencyContent(content);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Emergency contacts updated successfully");
        return response;
    }
}
