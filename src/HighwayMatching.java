import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Component 3.2: Highway Matching
public class HighwayMatching {
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int GEOHASH_PRECISION = 5;

    public HighwayMatching(){}

    public static List<MatchedRecord> matchRecords(List<GPSLogEntry> gpsLogs, Map<String, List<HighwaySegment>> highwaySegments) throws IllegalArgumentException{
        if (gpsLogs == null) {
            throw new NullPointerException("Error - GPS log list cannot be null");
        }
        if (highwaySegments == null || highwaySegments.isEmpty()){
            throw new IllegalArgumentException("Error - Highway lookup map cannot be empty");
        }

        List<MatchedRecord> results = new ArrayList<>();

        String currentDirection = "EB";
        if (gpsLogs.size() > 1) {
            if (gpsLogs.get(1).getLon() < gpsLogs.get(0).getLon()) {
                currentDirection = "WB";
            }
        }

        Double prevLon = null;

        for (GPSLogEntry gps : gpsLogs) {
            if (prevLon != null) {
                if (gps.getLon() > prevLon) {
                    currentDirection = "EB";
                } else if (gps.getLon() < prevLon) {
                    currentDirection = "WB";
                }
                // When stationary, currentDirection remains unchanged
            }
            prevLon = gps.getLon();

            String hash = encodeGeohash(gps.getLat(), gps.getLon(), GEOHASH_PRECISION);
            List<HighwaySegment> candidates = highwaySegments.get(hash);

            HighwaySegment bestMatch = null;
            if (candidates != null && !candidates.isEmpty()) {
                for (HighwaySegment segment : candidates) {
                    if (segment.getGeohash().equals(hash)) {
                        bestMatch = segment;
                        break;
                    }
                }
            }

            String channelID = "UNKNOWN";
            if (bestMatch == null) {

            System.err.println(
                    "Warning - Channel ID UNKNOWN for GPS record: " +
                            "Timestamp: " + gps.getDateTime() +
                            ", Latitude: " + gps.getLat() +
                            ", Longitude: " + gps.getLon()
            );

        }else{
                channelID = "#I90" + bestMatch.getSateCode() + currentDirection + "_" + bestMatch.getChannel();

            }

            results.add(new MatchedRecord(gps, bestMatch, channelID));
        }

        return results;
    }

    // encodeGeohash takes in the lat and lon from the gps file and converts to a geohash
    public static String encodeGeohash(double lat, double lon, int precision) {
        double latMin = -90.0, latMax = 90.0;
        double lonMin = -180.0, lonMax = 180.0;
        StringBuilder geohash = new StringBuilder();

        int bitCount = 0;
        int ch = 0;
        int step = 0;

        while (geohash.length() < precision) {
            if (step % 2 == 0) {
                double mid = (lonMin + lonMax) / 2.0;
                if (lon >= mid) {
                    ch = (ch * 2) + 1;
                    lonMin = mid;
                } else {
                    ch = ch * 2;
                    lonMax = mid;
                }
            } else {
                double mid = (latMin + latMax) / 2.0;
                if (lat >= mid) {
                    ch = (ch * 2) + 1;
                    latMin = mid;
                } else {
                    ch = ch * 2;
                    latMax = mid;
                }
            }

            step++;
            bitCount++;

            if (bitCount == 5) {
                geohash.append(BASE32.charAt(ch));
                bitCount = 0;
                ch = 0;
            }
        }

        return geohash.toString();
    }
}