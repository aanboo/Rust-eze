import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Component 3.2: Highway Matching
public class HighwayMatching {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int GEOHASH_PRECISION = 5;

    public HighwayMatching(){}

    public List<MatchedRecord> matchRecords(List<GPSLogEntry> gpsLogs, Map<String, List<HighwaySegment>> highwaySegments) {
        List<MatchedRecord> results = new ArrayList<>();

        for (GPSLogEntry gps : gpsLogs) {
            String hash = encodeGeohash(gps.getLat(), gps.getLon(), GEOHASH_PRECISION);
            List<HighwaySegment> candidates = highwaySegments.get(hash);

            HighwaySegment bestMatch = null;
            double minDistance = Double.MAX_VALUE;

            if (candidates != null && !candidates.isEmpty()) {
                for (HighwaySegment segment : candidates) {
                    double dist = calculateDistance(gps.getLat(), gps.getLon(),
                            segment.getLat(), segment.getLon());
                    if (dist < minDistance) {
                        minDistance = dist;
                        bestMatch = segment;
                    }
                }
            }

            results.add(new MatchedRecord(gps, bestMatch));
        }

        return results;
    }

    // Base-32 geohash encoding matching preprocessor precision level
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

    // Euclidean distance approximation for nearby points
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = lat1 - lat2;
        double dLon = lon1 - lon2;
        return Math.sqrt((dLat * dLat) + (dLon * dLon));
    }
}