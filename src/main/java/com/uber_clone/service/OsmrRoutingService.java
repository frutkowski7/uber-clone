package com.uber_clone.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.uber_clone.dto.OsmrResponse;

import tools.jackson.databind.ObjectMapper;

@Service
public class OsmrRoutingService {

    // mapper działa tak, że Jackson czyta tekst JSON z response.body(),
    // znajduje w nim klucz "routes",
    // widzi, że w Twojej klasie istnieje pole routes, i przechodzi w głąb tej
    // listy.
    private final ObjectMapper mapper = new ObjectMapper();

    public OsmrResponse getRoute(double pLng, double pLat, double dLng, double dLat) {

        HttpClient httpClient = HttpClient.newHttpClient();
        String url = String.format(
                Locale.US,
                "http://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                pLng, pLat, dLng, dLat);

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return mapper.readValue(response.body(), OsmrResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Exception" + e);
        }

    }

}
