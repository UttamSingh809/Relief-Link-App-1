package com.relieflink.service;

import com.relieflink.model.*;
import com.relieflink.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {
    
    private class TreeNode<T> {
        T data;
        TreeNode<T> left;
        TreeNode<T> right;
        int height;
        List<T> items;
        ItemCategory category;

        TreeNode(T data, ItemCategory category) {
            this.data = data;
            this.left = null;
            this.right = null;
            this.height = 1;
            this.items = new ArrayList<>();
            this.items.add(data);
            this.category = category;
        }
    }

    private TreeNode<Donation> donationTree;
    private TreeNode<Request> requestTree;

    @Autowired
    public void init() {
        donationTree = null;
        requestTree = null;
    }

    private TreeNode<Request> insertRequest(TreeNode<Request> node, Request request) {
        if (node == null) {
            return new TreeNode<>(request, request.getCategory());
        }

        int compareResult = request.getCategory().compareTo(node.category);

        if (compareResult < 0)
            node.left = insertRequest(node.left, request);
        else if (compareResult > 0)
            node.right = insertRequest(node.right, request);
        else {
            node.items.add(request);
            return node;
        }

        node.height = Math.max(height(node.left), height(node.right)) + 1;

        int balance = getBalance(node);

        // Left Left Case
        if (balance > 1 && request.getCategory().compareTo(node.left.category) < 0)
            return rightRotate(node);

        // Right Right Case
        if (balance < -1 && request.getCategory().compareTo(node.right.category) > 0)
            return leftRotate(node);

        // Left Right Case
        if (balance > 1 && request.getCategory().compareTo(node.left.category) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Left Case
        if (balance < -1 && request.getCategory().compareTo(node.right.category) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    private List<Request> findRequestsByCategory(TreeNode<Request> node, ItemCategory category) {
        if (node == null) return new ArrayList<>();

        int compareResult = category.compareTo(node.category);

        if (compareResult < 0)
            return findRequestsByCategory(node.left, category);
        else if (compareResult > 0)
            return findRequestsByCategory(node.right, category);
        else
            return node.items.stream()
                    .filter(r -> !r.isMatched())
                    .collect(Collectors.toList());
    }

    private int height(TreeNode<?> node) {
        if (node == null) return 0;
        return node.height;
    }

    private int getBalance(TreeNode<?> node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    private <T> TreeNode<T> rightRotate(TreeNode<T> y) {
        TreeNode<T> x = y.left;
        TreeNode<T> T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private <T> TreeNode<T> leftRotate(TreeNode<T> x) {
        TreeNode<T> y = x.right;
        TreeNode<T> T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    private TreeNode<Donation> insertDonation(TreeNode<Donation> node, Donation donation) {
        if (node == null) {
            return new TreeNode<>(donation, donation.getCategory());
        }

        int compareResult = donation.getCategory().compareTo(node.category);

        if (compareResult < 0)
            node.left = insertDonation(node.left, donation);
        else if (compareResult > 0)
            node.right = insertDonation(node.right, donation);
        else {
            node.items.add(donation);
            return node;
        }

        node.height = Math.max(height(node.left), height(node.right)) + 1;

        int balance = getBalance(node);

        // Left Left Case
        if (balance > 1 && donation.getCategory().compareTo(node.left.category) < 0)
            return rightRotate(node);

        // Right Right Case
        if (balance < -1 && donation.getCategory().compareTo(node.right.category) > 0)
            return leftRotate(node);

        // Left Right Case
        if (balance > 1 && donation.getCategory().compareTo(node.left.category) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Left Case
        if (balance < -1 && donation.getCategory().compareTo(node.right.category) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    private List<Donation> findDonationsByCategory(TreeNode<Donation> node, ItemCategory category) {
        if (node == null) return new ArrayList<>();

        int compareResult = category.compareTo(node.category);

        if (compareResult < 0)
            return findDonationsByCategory(node.left, category);
        else if (compareResult > 0)
            return findDonationsByCategory(node.right, category);
        else
            return node.items.stream()
                    .filter(d -> !d.isMatched())
                    .collect(Collectors.toList());
    }
    
    @Autowired
    private DataStore dataStore;

    public List<Match> findMatches() {
        List<Match> newMatches = new ArrayList<>();
        List<Donation> unmatchedDonations = dataStore.findUnmatchedDonations();
        List<Request> unmatchedRequests = dataStore.findUnmatchedRequests();

        // Build donation tree
        donationTree = null;
        for (Donation donation : unmatchedDonations) {
            if (!donation.isMatched()) {
                donationTree = insertDonation(donationTree, donation);
            }
        }

        // Sort requests by urgency and waiting time
        List<Request> sortedRequests = unmatchedRequests.stream()
            .sorted(this::compareRequestPriority)
            .collect(Collectors.toList());

        for (Request request : sortedRequests) {
            // Find compatible donations for this request using tree structure
            List<Donation> categoryDonations = findDonationsByCategory(donationTree, request.getCategory());
            List<Donation> compatibleDonations = categoryDonations.stream()
                .filter(donation -> isPartiallyCompatible(donation, request))
                .sorted(this::compareDonationPriority)
                .collect(Collectors.toList());

            if (!compatibleDonations.isEmpty()) {
                // Try to find a single donation that can fulfill the request
                Optional<Donation> singleMatch = compatibleDonations.stream()
                    .filter(d -> d.getQuantity() >= request.getQuantity())
                    .findFirst();

                if (singleMatch.isPresent()) {
                    // Handle single donation match
                    Match match = createMatch(singleMatch.get(), request);
                    newMatches.add(match);
                } else {
                    // Try to combine multiple donations
                    List<Donation> combinedDonations = findCombinableDonations(compatibleDonations, request);
                    if (!combinedDonations.isEmpty()) {
                        List<Match> combinedMatches = createCombinedMatch(combinedDonations, request);
                        newMatches.addAll(combinedMatches);
                    }
                }
            }
        }
        return newMatches;
    }

    private int compareRequestPriority(Request r1, Request r2) {
        // First, compare by urgency level
        int urgencyCompare = r2.getUrgency().compareTo(r1.getUrgency());
        if (urgencyCompare != 0) {
            return urgencyCompare;
        }
        
        // Then, compare by waiting time (older requests get priority)
        return r1.getCreatedAt().compareTo(r2.getCreatedAt());
    }

    private int compareDonationPriority(Donation d1, Donation d2) {
        // Prioritize donations that are fresher (newer first)
        return d2.getCreatedAt().compareTo(d1.getCreatedAt());
    }

    private boolean isCompatible(Donation donation, Request request) {
        // Check category match
        if (donation.getCategory() != request.getCategory()) {
            return false;
        }

        // Check location match (case-insensitive)
        if (!donation.getLocation().trim().equalsIgnoreCase(request.getLocation().trim())) {
            return false;
        }

        // Check if donation quantity is sufficient
        if (donation.getQuantity() < request.getQuantity()) {
            return false;
        }

        // Check if item names are similar (ignoring case and trimming)
        String donationItem = donation.getItemName().trim().toLowerCase();
        String requestItem = request.getItemName().trim().toLowerCase();
        if (!donationItem.contains(requestItem) && !requestItem.contains(donationItem)) {
            return false;
        }

        // Check if the donation is not too old (e.g., more than 30 days)
        long daysOld = ChronoUnit.DAYS.between(donation.getCreatedAt(), LocalDateTime.now());
        if (daysOld > 30) {
            return false;
        }

        return true;
    }

    private Match createMatch(Donation donation, Request request) {
        Match match = new Match(donation, request);
        dataStore.saveMatch(match);

        // Update the matched status
        donation.setMatched(true);
        request.setMatched(true);

        // Handle remaining quantity if donation has more than needed
        if (donation.getQuantity() > request.getQuantity()) {
            handleRemainingQuantity(donation, request);
        }

        // Save the updated entities
        dataStore.saveDonation(donation);
        dataStore.saveRequest(request);

        return match;
    }

    private void handleRemainingQuantity(Donation donation, Request request) {
        int remainingQuantity = donation.getQuantity() - request.getQuantity();
        if (remainingQuantity > 0) {
            // Create a new donation with remaining quantity
            Donation newDonation = new Donation();
            newDonation.setDonorId(donation.getDonorId());
            newDonation.setDonorName(donation.getDonorName());
            newDonation.setCategory(donation.getCategory());
            newDonation.setItemName(donation.getItemName());
            newDonation.setQuantity(remainingQuantity);
            newDonation.setLocation(donation.getLocation());
            newDonation.setDescription(donation.getDescription());
            newDonation.setCreatedAt(donation.getCreatedAt());

            // Save the new donation and add it to the tree
            dataStore.saveDonation(newDonation);
            donationTree = insertDonation(donationTree, newDonation);
        }
        
        // Update original donation quantity to match the request
        donation.setQuantity(request.getQuantity());
        dataStore.saveDonation(donation);
    }

    private boolean isPartiallyCompatible(Donation donation, Request request) {
        // Check category match
        if (donation.getCategory() != request.getCategory()) {
            return false;
        }

        // Check location match (case-insensitive)
        if (!donation.getLocation().trim().equalsIgnoreCase(request.getLocation().trim())) {
            return false;
        }

        // Check if item names are similar (ignoring case and trimming)
        String donationItem = donation.getItemName().trim().toLowerCase();
        String requestItem = request.getItemName().trim().toLowerCase();
        if (!donationItem.contains(requestItem) && !requestItem.contains(donationItem)) {
            return false;
        }

        // Check if the donation is not too old (e.g., more than 30 days)
        long daysOld = ChronoUnit.DAYS.between(donation.getCreatedAt(), LocalDateTime.now());
        if (daysOld > 30) {
            return false;
        }

        return true;
    }

    private List<Donation> findCombinableDonations(List<Donation> compatibleDonations, Request request) {
        List<Donation> result = new ArrayList<>();
        int remainingQuantity = request.getQuantity();

        // Sort donations by freshness (newer first) to prefer fresher items
        List<Donation> sortedDonations = new ArrayList<>(compatibleDonations);
        sortedDonations.sort((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()));

        for (Donation donation : sortedDonations) {
            if (donation.isMatched()) continue;

            result.add(donation);
            remainingQuantity -= donation.getQuantity();

            if (remainingQuantity <= 0) {
                break;
            }
        }

        // Only return the result if we can fulfill the entire request
        return remainingQuantity <= 0 ? result : new ArrayList<>();
    }

    private List<Match> createCombinedMatch(List<Donation> donations, Request request) {
        List<Match> matches = new ArrayList<>();
        int remainingRequestQuantity = request.getQuantity();
        
        for (int i = 0; i < donations.size() && remainingRequestQuantity > 0; i++) {
            Donation donation = donations.get(i);
            int matchQuantity = Math.min(donation.getQuantity(), remainingRequestQuantity);
            
            // Create a match for this portion
            Match match = new Match();
            match.setDonationId(donation.getId());
            match.setRequestId(request.getId());
            match.setDonorName(donation.getDonorName());
            match.setRequesterName(request.getRequesterName());
            match.setCategory(donation.getCategory());
            match.setItemName(donation.getItemName());
            match.setQuantity(matchQuantity);
            match.setLocation(request.getLocation());
            match.setUrgency(request.getUrgency());
            match.setMatchedAt(LocalDateTime.now());
            
            // Save the match
            dataStore.saveMatch(match);
            matches.add(match);
            
            // Update donation quantity or mark as matched
            if (matchQuantity == donation.getQuantity()) {
                donation.setMatched(true);
            } else {
                handleRemainingQuantity(donation, matchQuantity);
            }
            
            remainingRequestQuantity -= matchQuantity;
        }
        
        // Mark the request as matched since we've fulfilled it completely
        if (remainingRequestQuantity == 0) {
            request.setMatched(true);
            dataStore.saveRequest(request);
        }
        
        return matches;
    }

    private void handleRemainingQuantity(Donation donation, int usedQuantity) {
        int remainingQuantity = donation.getQuantity() - usedQuantity;
        if (remainingQuantity > 0) {
            // Create a new donation with remaining quantity
            Donation newDonation = new Donation();
            newDonation.setDonorId(donation.getDonorId());
            newDonation.setDonorName(donation.getDonorName());
            newDonation.setCategory(donation.getCategory());
            newDonation.setItemName(donation.getItemName());
            newDonation.setQuantity(remainingQuantity);
            newDonation.setLocation(donation.getLocation());
            newDonation.setDescription(donation.getDescription());
            newDonation.setCreatedAt(donation.getCreatedAt());

            // Save the new donation and add it to the tree
            dataStore.saveDonation(newDonation);
            donationTree = insertDonation(donationTree, newDonation);
        }
        
        // Update original donation quantity
        donation.setQuantity(usedQuantity);
        donation.setMatched(true);
        dataStore.saveDonation(donation);
    }

    public List<Match> getAllMatches() {
        return dataStore.findAllMatches();
    }
}
