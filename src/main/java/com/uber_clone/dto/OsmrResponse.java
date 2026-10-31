package com.uber_clone.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsmrResponse(
        List<Route> routes

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(
            double distance,
            double duration,
            Geometry geometry) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
            // Geometry to Lista, [lng, lat]
            List<List<Double>> coordinates) {
    }

}
