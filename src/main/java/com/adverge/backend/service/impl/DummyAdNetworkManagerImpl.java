package com.adverge.backend.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.adverge.backend.dto.AdRequest;
import com.adverge.backend.dto.AdResponse;
import com.adverge.backend.dto.BidResponse;
import com.adverge.backend.service.AdNetworkManager;
import com.adverge.backend.service.AdNetworkService;

@Service
public class DummyAdNetworkManagerImpl implements AdNetworkManager {

    @Override
    public List<AdNetworkService> getAdNetworkServices() {
        return Collections.emptyList();
    }

    @Override
    public AdNetworkService getAdNetworkService(String networkName) {
        return null;
    }

    @Override
    public List<BidResponse> requestBids(AdRequest request) {
        return Collections.emptyList();
    }

    @Override
    public AdResponse processEvent(String eventType, String networkName, String requestId, String adUnitId) {
        return new AdResponse();
    }
} 