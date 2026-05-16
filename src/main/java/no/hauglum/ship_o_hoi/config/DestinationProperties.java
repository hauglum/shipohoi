package no.hauglum.ship_o_hoi.config;

import no.hauglum.ship_o_hoi.model.DestinationProfile;
import no.hauglum.ship_o_hoi.model.Position;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "destinations")
public class DestinationProperties {

    private String active;
    private List<Profile> profiles = new ArrayList<>();

    public DestinationProfile resolveActive() {
        return profiles.stream()
                .filter(p -> p.name.equalsIgnoreCase(active))
                .findFirst()
                .map(Profile::toDestinationProfile)
                .orElseThrow(() -> new IllegalStateException(
                        "No destination profile configured for active: '" + active + "'"));
    }

    public String getActive() { return active; }
    public void setActive(String active) { this.active = active; }
    public List<Profile> getProfiles() { return profiles; }
    public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }

    public static class Profile {
        private String name;
        private List<String> aliases = new ArrayList<>();
        private Double latitude;
        private Double longitude;

        DestinationProfile toDestinationProfile() {
            Position pos = (latitude != null && longitude != null)
                    ? new Position(latitude, longitude) : null;
            return new DestinationProfile(name, new HashSet<>(aliases), pos);
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getAliases() { return aliases; }
        public void setAliases(List<String> aliases) { this.aliases = aliases; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }
}
