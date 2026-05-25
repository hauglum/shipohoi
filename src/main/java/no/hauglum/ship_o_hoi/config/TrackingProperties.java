package no.hauglum.ship_o_hoi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "tracking")
public class TrackingProperties {

    private List<String> mmsiWatchlist = new ArrayList<>();

    public List<String> getMmsiWatchlist() { return mmsiWatchlist; }
    public void setMmsiWatchlist(List<String> mmsiWatchlist) { this.mmsiWatchlist = mmsiWatchlist; }
}
