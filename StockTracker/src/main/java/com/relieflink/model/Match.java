package com.relieflink.model;

import java.time.LocalDateTime;

public class Match {
    private Long id;
    private Long donationId;
    private Long requestId;
    private String donorName;
    private String requesterName;
    private ItemCategory category;
    private String itemName;
    private int quantity;
    private String location;
    private UrgencyLevel urgency;
    private LocalDateTime matchedAt;

    public Match() {
        this.matchedAt = LocalDateTime.now();
    }

    public Match(Donation donation, Request request) {
        this.donationId = donation.getId();
        this.requestId = request.getId();
        this.donorName = donation.getDonorName();
        this.requesterName = request.getRequesterName();
        this.category = donation.getCategory();
        this.itemName = donation.getItemName();
        this.quantity = Math.min(donation.getQuantity(), request.getQuantity());
        this.location = request.getLocation();
        this.urgency = request.getUrgency();
        this.matchedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDonationId() {
        return donationId;
    }

    public void setDonationId(Long donationId) {
        this.donationId = donationId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UrgencyLevel getUrgency() {
        return urgency;
    }

    public void setUrgency(UrgencyLevel urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }
}
