package com.relieflink.service;

import com.relieflink.model.Donation;
import com.relieflink.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationService {
    
    @Autowired
    private DataStore dataStore;

    public Donation createDonation(Donation donation) {
        return dataStore.saveDonation(donation);
    }

    public List<Donation> getAllDonations() {
        return dataStore.findAllDonations();
    }

    public List<Donation> getUnmatchedDonations() {
        return dataStore.findUnmatchedDonations();
    }
}
