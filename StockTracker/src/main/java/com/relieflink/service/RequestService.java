package com.relieflink.service;

import com.relieflink.model.Request;
import com.relieflink.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {
    
    @Autowired
    private DataStore dataStore;

    public Request createRequest(Request request) {
        return dataStore.saveRequest(request);
    }

    public List<Request> getAllRequests() {
        return dataStore.findAllRequests();
    }

    public List<Request> getUnmatchedRequests() {
        return dataStore.findUnmatchedRequests();
    }
}
