package com.uber_clone.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.uber.h3core.H3Core;

@Service
public class H3Service {

    private final H3Core h3;

    public H3Service() {
        try {
            this.h3 = H3Core.newInstance();
        } catch (IOException e) {
            throw new IllegalStateException("");
        }
    }

    public String geoToH3Adress(double lat, double lng, int resolution) {

        return h3.geoToH3Address(lat, lng, resolution);

    }

    public List<String> getKRingNeighbors(String h3Adress, int k) {
        if (k < 0) {
            throw new IllegalArgumentException("K must be greater than 0");
        }

        return h3.kRing(h3Adress, k);

    }

}
