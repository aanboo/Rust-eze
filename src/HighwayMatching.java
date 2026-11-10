import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// Component 3.2: Highway Matching
public class HighwayMatching {
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int GEOHASH_PRECISION = 5;
    private static final double MOVEMENT_THRESHOLD = 0.00007;


    public HighwayMatching(){}


    public static List<MatchedRecord> matchRecords(List<GPSLogEntry> gpsLogs, Map<String, List<HighwaySegment>> highwaySegments) throws IllegalArgumentException{
        List<MatchedRecord> results = new ArrayList<>();

        String previousChannelID = "UNKNOWN";
        String previousDirection = "UNKNOWN";
        GPSLogEntry previousGPS = null;
        HighwaySegment previousSegment = null;

        for (GPSLogEntry gps : gpsLogs) {
            String hash = encodeGeohash(gps.getLat(), gps.getLon(), GEOHASH_PRECISION);
            //takes the hash calculated from the gps log and matches it to the hashmap to find the right channel?
            List<HighwaySegment> candidates = highwaySegments.get(hash); //returns all the hash

            HighwaySegment bestMatch = null;

            if (candidates != null && !candidates.isEmpty()) {
                for (HighwaySegment segment : candidates) {
                    // Pick the segment whose geohash exactly matches the generated hash
                    if (segment.getGeohash().equals(hash)) {
                        // Since geohashes are unique in the highway file, we can take the first exact match
                        bestMatch = segment;
                        break;
                    }
                }
            }
            // If we didn't find an exact match, throw an error
            if (bestMatch == null) {
                //System.out.println("Highway segment geohash is: "+ hash);
                throw new IllegalArgumentException("Error - Highway segment cannot be matched");
            }

            MatchedRecord record = new MatchedRecord(gps, bestMatch);

            boolean sameLocation = previousGPS != null
                    && Double.compare(
                    gps.getLat(), previousGPS.getLat()) == 0
                    &&
                    Double.compare(
                            gps.getLon(), previousGPS.getLon()) == 0;
            String direction;
            if (sameLocation) {
                direction = previousDirection;
                if (!previousChannelID.equals("UNKNOWN")) {
                    record.setChannelID(previousChannelID);
                    results.add(record);
                    previousGPS = gps;
                    previousSegment = bestMatch;
                    continue;
                }
            } else {
                direction = detectDirection(
                        previousGPS,
                        gps,
                        previousSegment,
                        bestMatch
                );

                if (direction.equals("UNKNOWN")) {
                    direction = previousDirection;
                }
            }
            String state = bestMatch.getSateCode();
            int mileBlock = bestMatch.getChannel();

            if (("WA".equals(state) || "ID".equals(state))
                    && ("EB".equals(direction) || "WB".equals(direction))
                    && mileBlock >= 0) {
                String channelID = "#190"
                        + state
                        + direction
                        + "_"
                        + mileBlock;
                record.setChannelID(channelID);
                previousChannelID = channelID;
                previousDirection = direction;
            } else {
                record.setChannelID("UNKNOWN");
            }

            results.add(record);
            previousGPS = gps;
            previousSegment = bestMatch;
        }
            return results;

            //results.add(new MatchedRecord(gps, bestMatch));
        }

        public static String detectDirection(
                GPSLogEntry previous,
                GPSLogEntry current,
                HighwaySegment previousSegment,
                HighwaySegment currentSegment){
        if(previous == null){
            return "UNKNOWN";
        }
        double longitudeDifference = current.getLon() - previous.getLon();

        if(longitudeDifference > MOVEMENT_THRESHOLD){
            return "EB";
        }
        if(longitudeDifference < -MOVEMENT_THRESHOLD){
            return "WB";
        }
        return "UNKNOWN";
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