package com.relieflink.repository;

import com.relieflink.model.*;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class DataStore {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, Donation> donations = new ConcurrentHashMap<>();
    private final Map<Long, Request> requests = new ConcurrentHashMap<>();
    private final Map<Long, Match> matches = new ConcurrentHashMap<>();

    private final AtomicLong userIdCounter = new AtomicLong(1);
    private final AtomicLong donationIdCounter = new AtomicLong(1);
    private final AtomicLong requestIdCounter = new AtomicLong(1);
    private final AtomicLong matchIdCounter = new AtomicLong(1);

    private String guidelinesContent;
    private String emergencyContent;

    private final ObjectMapper objectMapper;
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String DONATIONS_FILE = DATA_DIR + "/donations.json";
    private static final String REQUESTS_FILE = DATA_DIR + "/requests.json";
    private static final String MATCHES_FILE = DATA_DIR + "/matches.json";
    private static final String COUNTERS_FILE = DATA_DIR + "/counters.json";

    public DataStore() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        // Create data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }

        // Load data from files
        loadDataFromFiles();

        // Create admin user if no users exist
        if (users.isEmpty()) {
            User admin = new User();
            admin.setId(userIdCounter.getAndIncrement());
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setEmail("admin@relieflink.com");
            admin.setFullName("System Administrator");
            admin.setLocation("Central Command");
            admin.setRole(UserRole.ADMIN);
            users.put(admin.getId(), admin);
        }

        // Initialize default content if not set
        if (guidelinesContent == null) {
            guidelinesContent = """
                <div class="guidelines-section">
                    <h2>For Donors</h2>
                    <ul>
                        <li>Verify the items you're donating are in good condition and safe to use</li>
                        <li>Provide accurate quantity and location information</li>
                        <li>Set urgency levels realistically to help prioritize critical needs</li>
                        <li>Include detailed descriptions to help requesters identify suitable items</li>
                        <li>Be prepared to deliver or arrange pickup when matched</li>
                    </ul>
                </div>

                <div class="guidelines-section">
                    <h2>For Requesters</h2>
                    <ul>
                        <li>Be specific about your needs - item type, quantity, and urgency</li>
                        <li>Provide accurate location information for efficient matching</li>
                        <li>Mark items as critical only when truly urgent to ensure fair distribution</li>
                        <li>Update your requests if needs change or are fulfilled</li>
                        <li>Coordinate with matched donors promptly</li>
                    </ul>
                </div>

                <div class="guidelines-section">
                    <h2>For Volunteers</h2>
                    <ul>
                        <li>Help coordinate between donors and requesters in your area</li>
                        <li>Assist with transportation and distribution when possible</li>
                        <li>Verify information accuracy to maintain system integrity</li>
                        <li>Report any issues or discrepancies to administrators</li>
                        <li>Prioritize safety in all relief operations</li>
                    </ul>
                </div>

                <div class="guidelines-section">
                    <h2>General Best Practices</h2>
                    <ul>
                        <li>Always prioritize life-saving resources (medicine, water, food)</li>
                        <li>Coordinate with local authorities and established relief organizations</li>
                        <li>Maintain clear communication throughout the matching process</li>
                        <li>Document all transactions for accountability</li>
                        <li>Report any suspicious activity to administrators immediately</li>
                    </ul>
                </div>
                """;
        }

        if (emergencyContent == null) {
            emergencyContent = """
                <div class="contact-section">
                    <h2>Emergency Services</h2>
                    <div class="contact-card">
                        <h3>Emergency Hotline</h3>
                        <p class="contact-number">112</p>
                        <p>For immediate life-threatening emergencies</p>
                    </div>
                    <div class="contact-card">
                        <h3>Disaster Management Authority</h3>
                        <p class="contact-number">1-800-RELIEF</p>
                        <p>24/7 disaster response coordination</p>
                    </div>
                </div>

                <div class="contact-section">
                    <h2>Relief Organizations</h2>
                    <div class="contact-card">
                        <h3>Red Cross</h3>
                        <p class="contact-number">1-800-RED-CROSS</p>
                        <p>Disaster relief and emergency assistance</p>
                    </div>
                    <div class="contact-card">
                        <h3>FEMA</h3>
                        <p class="contact-number">1-800-621-3362</p>
                        <p>Federal emergency management assistance</p>
                    </div>
                </div>

                <div class="contact-section">
                    <h2>Local Authorities</h2>
                    <div class="contact-card">
                        <h3>Local Emergency Management</h3>
                        <p class="contact-number">Contact your local authorities</p>
                        <p>Regional disaster coordination</p>
                    </div>
                    <div class="contact-card">
                        <h3>Community Crisis Center</h3>
                        <p class="contact-number">211</p>
                        <p>Community resources and support services</p>
                    </div>
                </div>

                <div class="contact-section">
                    <h2>Medical Services</h2>
                    <div class="contact-card">
                        <h3>Poison Control</h3>
                        <p class="contact-number">1-800-222-1222</p>
                        <p>24/7 poison emergency assistance</p>
                    </div>
                    <div class="contact-card">
                        <h3>Mental Health Crisis Line</h3>
                        <p class="contact-number">988</p>
                        <p>Crisis counseling and mental health support</p>
                    </div>
                </div>
                """;
        }
    }

    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setId(userIdCounter.getAndIncrement());
        }
        users.put(user.getId(), user);
        saveDataToFiles();
        return user;
    }

    public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findUserByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void deleteUser(Long id) {
        users.remove(id);
    }

    public Donation saveDonation(Donation donation) {
        if (donation.getId() == null) {
            donation.setId(donationIdCounter.getAndIncrement());
        }
        donations.put(donation.getId(), donation);
        saveDataToFiles();
        return donation;
    }

    public Optional<Donation> findDonationById(Long id) {
        return Optional.ofNullable(donations.get(id));
    }

    public List<Donation> findAllDonations() {
        return new ArrayList<>(donations.values());
    }

    public List<Donation> findUnmatchedDonations() {
        return donations.values().stream()
                .filter(d -> !d.isMatched())
                .collect(Collectors.toList());
    }

    public Request saveRequest(Request request) {
        if (request.getId() == null) {
            request.setId(requestIdCounter.getAndIncrement());
        }
        requests.put(request.getId(), request);
        saveDataToFiles();
        return request;
    }

    public Optional<Request> findRequestById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }

    public List<Request> findAllRequests() {
        return new ArrayList<>(requests.values());
    }

    public List<Request> findUnmatchedRequests() {
        return requests.values().stream()
                .filter(r -> !r.isMatched())
                .collect(Collectors.toList());
    }

    public Match saveMatch(Match match) {
        if (match.getId() == null) {
            match.setId(matchIdCounter.getAndIncrement());
        }
        matches.put(match.getId(), match);
        saveDataToFiles();
        return match;
    }

    public List<Match> findAllMatches() {
        return new ArrayList<>(matches.values());
    }

    public void resetAll() {
        donations.clear();
        requests.clear();
        matches.clear();
        users.clear();
        User admin = new User();
        admin.setId(userIdCounter.getAndIncrement());
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setEmail("admin@relieflink.com");
        admin.setFullName("System Administrator");
        admin.setLocation("Central Command");
        admin.setRole(UserRole.ADMIN);
        users.put(admin.getId(), admin);
    }

    public Map<String, Object> exportData() {
        Map<String, Object> data = new HashMap<>();
        data.put("users", new ArrayList<>(users.values()));
        data.put("donations", new ArrayList<>(donations.values()));
        data.put("requests", new ArrayList<>(requests.values()));
        data.put("matches", new ArrayList<>(matches.values()));
        return data;
    }

    public String getGuidelinesContent() {
        return guidelinesContent;
    }

    public void setGuidelinesContent(String guidelinesContent) {
        this.guidelinesContent = guidelinesContent;
        saveDataToFiles();
    }

    public String getEmergencyContent() {
        return emergencyContent;
    }

    public void setEmergencyContent(String emergencyContent) {
        this.emergencyContent = emergencyContent;
        saveDataToFiles();
    }

    private void loadDataFromFiles() {
        try {
            // Load users
            File usersFile = new File(USERS_FILE);
            if (usersFile.exists()) {
                List<User> userList = objectMapper.readValue(usersFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
                userList.forEach(user -> users.put(user.getId(), user));
            }

            // Load donations
            File donationsFile = new File(DONATIONS_FILE);
            if (donationsFile.exists()) {
                List<Donation> donationList = objectMapper.readValue(donationsFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Donation.class));
                donationList.forEach(donation -> donations.put(donation.getId(), donation));
            }

            // Load requests
            File requestsFile = new File(REQUESTS_FILE);
            if (requestsFile.exists()) {
                List<Request> requestList = objectMapper.readValue(requestsFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Request.class));
                requestList.forEach(request -> requests.put(request.getId(), request));
            }

            // Load matches
            File matchesFile = new File(MATCHES_FILE);
            if (matchesFile.exists()) {
                List<Match> matchList = objectMapper.readValue(matchesFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Match.class));
                matchList.forEach(match -> matches.put(match.getId(), match));
            }

            // Load counters
            File countersFile = new File(COUNTERS_FILE);
            if (countersFile.exists()) {
                Map<String, Long> counters = objectMapper.readValue(countersFile,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Long.class));
                userIdCounter.set(counters.getOrDefault("userIdCounter", 1L));
                donationIdCounter.set(counters.getOrDefault("donationIdCounter", 1L));
                requestIdCounter.set(counters.getOrDefault("requestIdCounter", 1L));
                matchIdCounter.set(counters.getOrDefault("matchIdCounter", 1L));
            }

        } catch (IOException e) {
            System.err.println("Error loading data from files: " + e.getMessage());
        }
    }

    private void saveDataToFiles() {
        try {
            // Save users
            objectMapper.writeValue(new File(USERS_FILE), new ArrayList<>(users.values()));

            // Save donations
            objectMapper.writeValue(new File(DONATIONS_FILE), new ArrayList<>(donations.values()));

            // Save requests
            objectMapper.writeValue(new File(REQUESTS_FILE), new ArrayList<>(requests.values()));

            // Save matches
            objectMapper.writeValue(new File(MATCHES_FILE), new ArrayList<>(matches.values()));

            // Save counters
            Map<String, Long> counters = new HashMap<>();
            counters.put("userIdCounter", userIdCounter.get());
            counters.put("donationIdCounter", donationIdCounter.get());
            counters.put("requestIdCounter", requestIdCounter.get());
            counters.put("matchIdCounter", matchIdCounter.get());
            objectMapper.writeValue(new File(COUNTERS_FILE), counters);

        } catch (IOException e) {
            System.err.println("Error saving data to files: " + e.getMessage());
        }
    }
}
