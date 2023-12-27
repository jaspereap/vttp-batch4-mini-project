package nus.iss.travlr.service;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import nus.iss.travlr.model.Activity;
import nus.iss.travlr.model.Itinerary;
import nus.iss.travlr.repository.TravlrRepository;

@Service
public class TravlrService {
    @Autowired
    private TravlrRepository travRepo;

    @Value("${google.api.key}")
    private String API_KEY;
    @Value("${geocode.url}")
    private String GEOCODE_URL;

    public void addItinerary(String userName, Itinerary itinerary) {
        travRepo.addItinerary(userName, itinerary);
    }

    public Optional<List<Itinerary>> getItinerary(String userName) {
        Optional<List<Itinerary>> optItineraryList = travRepo.getItinerary(userName);
        return optItineraryList;
    }

    public void deleteItinerary() {

    }

    public void addActivity(String userName, Integer iid, Activity activity) {
        travRepo.addActivity(userName, iid, activity);
    }

    public JsonObject getAddressDetails(String address) {
        String url = GEOCODE_URL;
        String endpoint = UriComponentsBuilder
            .fromUriString(url)
            .queryParam("key", API_KEY)
            .queryParam("address", address)
            .build()
            .toString();
        System.out.println(endpoint);

        RestTemplate template = new RestTemplate();
        String response = template.getForObject(endpoint, String.class);

        JsonReader reader = Json.createReader(new StringReader(response));
        JsonObject responseObj = reader.readObject();
        String status = responseObj.getString("status");
        // Status Codes -> OK, ZERO_RESULTS, OVER_DAILY_LIMIT, OVER_QUERY_LIMIT, REQUEST_DENIED, INVALID_REQUEST, UNKNOWN_ERROR
        System.out.println("Status: " + status);
        if (!status.equals("OK")) {
            return null;
        }

        JsonObject results = responseObj.getJsonArray("results").getJsonObject(0);
        JsonObject location = results.getJsonObject("geometry").getJsonObject("location");

        String formatted_address = results.getString("formatted_address");
        String place_id = results.getString("place_id");
        String lat = location.getJsonNumber("lat").toString();
        String lng = location.getJsonNumber("lng").toString();
        System.out.printf("Formatted address: %s, \nplace id: %s, \nlat: %s, \nlng: %s\n", formatted_address, place_id, lat, lng);

        JsonObjectBuilder returnBuilder = Json.createObjectBuilder();
        JsonObject compiled = returnBuilder
        .add("lat", lat)
        .add("lng", lng)
        .add("formatted_address", formatted_address)
        .add("place_id", place_id)
        .build();

        return compiled;
    }
}
