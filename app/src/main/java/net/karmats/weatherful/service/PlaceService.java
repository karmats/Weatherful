package net.karmats.weatherful.service;

import java.util.List;

public interface PlaceService {

    /**
     * Search for autocomplete suggestions in googles places api
     * 
     * @param query
     *            The query to search for
     * @return A list of {@link PlaceAutocomplete}
     */
    List<PlaceAutocomplete> autocompletePlace(String query);

    /**
     * Fetches details from google places api
     * 
     * @param reference
     *            The google reference
     * @return A {@link PlaceDetails}
     */
    PlaceDetails fetchDetails(String reference);

    /**
     * Represents a place result from autocomplete
     * 
     * @author mats
     * 
     */
    public static class PlaceAutocomplete {
        private final String reference;
        private final String city;
        private final String stateAndCountry;

        public PlaceAutocomplete(String reference, String city, String stateAndCountry) {
            super();
            this.reference = reference;
            this.city = city;
            this.stateAndCountry = stateAndCountry;
        }

        public String getReference() {
            return reference;
        }

        public String getCity() {
            return city;
        }

        public String getStateAndCountry() {
            return stateAndCountry;
        }

    }

    /**
     * Represents a place details result
     * 
     * @author mats
     * 
     */
    public static class PlaceDetails {
        private final Double latitude;
        private final Double longitude;
        private final String city;
        private final String stateAndCountry;

        public PlaceDetails(Double latitude, Double longitude, String city, String stateAndCountry) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.city = city;
            this.stateAndCountry = stateAndCountry;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public String getCity() {
            return city;
        }

        public String getStateAndCountry() {
            return stateAndCountry;
        }
    }
}
