
//CSV format should be: geohash. ST(16,53), channel(10-300). Only record unique geohashes. Can use jq utility Channel Id should average
//Convert each individual coordinate to a geohash
//read into a memory into an array. should have 1257 ish
//Read the JSON
//Each highway record contains a LineString geometry and attributes such as SIGN1, STFIPS, BEGMP, and ENDMP
//Use a JSON libary to help preprocess.
//read the coordinate pairs from each LineString. A pair is in the order [longitude, latitude], whereas geohash functions commonly take arguments in the order (latitude, longitude).
//
//Generate precision-5 geohashes for points along the line, not just the first coordinate. You must account for the areas between coordinate vertices too, so long segments do not leave gaps in the index.
//For every geohash a segment intersects, write a row containing the geohash and the segment's attributes. Remove duplicate geohash/segment combinations. Use a CSV writer or correctly escape commas and quotation marks if writing CSV manually.
//test one known segment first, then a few more. Verify that the CSV contains both states, that each row has a six-character geohash, and that GPS points along the highway can retrieve the expected candidate segments. Compare results against a small manually checked sample before processing the full dataset

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Hi_Chan preprocessor (debugging version): converts the filtered NHPN JSON file
 * (a features collection of I-90 segments) into a flat CSV with one row per coordinate.
 *
 * CSV columns:
 *   geohash,lat,lon,stfips,begmp,endmp,channel
 *
 * Each geohash appears only once: the first point found for a geohash is kept and every
 * later point with the same geohash is dropped.
 *
 * The channel is based only on the segment's ENDMP:
 *   ENDMP 0 to 10 (inclusive) -> channel 10
 *   ENDMP above 10 up to 20   -> channel 20
 *   ... and so on up to 290.
 * No milepost is calculated for individual points.
 *
 * Setup in IntelliJ: add Gson to the project (Maven or Gradle), for example
 *   Maven: com.google.code.gson:gson:2.10.1
 *
 * Usage:  java HiChanPreprocessor <input.json> <output.csv>
 */
public class HiChanPreprocessor {

    /** Geohash uses its own base-32 alphabet (digits plus letters, minus a, i, l, o). */
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    /** Number of geohash characters. Precision 5 is a cell of roughly 4.9 km x 4.9 km. */
    private static final int GEOHASH_PRECISION = 5;

    /** Each channel covers 10 miles. */
    private static final int CHANNEL_SIZE = 10;

    /** Highest channel number allowed (an ENDMP above 290 is clamped to it). */
    private static final int MAX_CHANNEL = 300;

    /**
     * Every geohash already written to the CSV. A HashSet answers "have I seen this
     * before?" in constant time, so removing duplicates stays fast even for large files.
     */
    private static final Set<String> seenGeohashes = new HashSet<>();

    /** How many rows were dropped because their geohash had already been written. */
    private static int duplicatesRemoved = 0;

    public static void main(String[] args) throws IOException {
        Path input = Paths.get(args.length > 0 ? args[0] : "src/I90_WA_ID.json");
        Path output = Paths.get(args.length > 1 ? args[1] : "i90_channels.csv");

        int segmentsRead = 0;
        int segmentsSkipped = 0;
        int rowsWritten = 0;

        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {

            // The file is small enough for Gson to load the whole thing into a tree in memory.
            JsonElement root = JsonParser.parseReader(reader);

            // Accept either {"features":[...]} or a plain top-level array of segments.
            JsonArray features = root.isJsonArray()
                    ? root.getAsJsonArray()
                    : root.getAsJsonObject().getAsJsonArray("features");

            writer.write("geohash,lat,lon,stfips,begmp,endmp,channel");
            writer.newLine();

            for (JsonElement element : features) {
                segmentsRead++;
                int written = writeSegment(element.getAsJsonObject(), writer);
                if (written == 0) {
                    segmentsSkipped++;
                }
                rowsWritten += written;
            }
        }

        System.out.printf("Read %d segments (%d added no rows), wrote %d unique geohash rows, "
                        + "removed %d duplicates, output: %s%n",
                segmentsRead, segmentsSkipped, rowsWritten, duplicatesRemoved, output);
    }

    /**
     * Writes one CSV row for every coordinate in a single highway segment.
     * Every row from the same segment shares the same STFIPS, BEGMP, ENDMP and channel.
     *
     * @return the number of rows written (0 if the segment was skipped)
     */
    private static int writeSegment(JsonObject feature, BufferedWriter writer) throws IOException {
        // Standard GeoJSON keeps attributes under "properties"; the filtered file keeps them
        // directly on the feature. Support both layouts.
        JsonObject attrs = feature;
        if (feature.has("properties") && feature.get("properties").isJsonObject()) {
            attrs = feature.getAsJsonObject("properties");
        }

        // Skip anything that is not I-90 (a safety net) or that is missing required fields.
        if (attrs.has("SIGN1") && !"I90".equals(attrs.get("SIGN1").getAsString().trim())) {
            return 0;
        }
        if (!attrs.has("STFIPS") || !attrs.has("BEGMP") || !attrs.has("ENDMP")
                || !feature.has("geometry") || feature.get("geometry").isJsonNull()) {
            return 0;
        }

        // getAsString works whether the JSON stores STFIPS as "16" or as the number 16.
        String stfips = attrs.get("STFIPS").getAsString().trim();
        if (!stfips.equals("16") && !stfips.equals("53")) {
            return 0; // only Idaho (16) and Washington (53)
        }
        if(stfips.equals("16")){
            stfips = "ID";
        }
        if(stfips.equals("53")){
            stfips = "WA";
        }
        // Keep BEGMP and ENDMP as the exact text from the JSON for the CSV, and parse
        // ENDMP as a number for the channel calculation.
        String begMpText = attrs.get("BEGMP").getAsString().trim();
        String endMpText = attrs.get("ENDMP").getAsString().trim();
        int channel = calcChannel(attrs.get("ENDMP").getAsDouble());

        JsonObject geometry = feature.getAsJsonObject("geometry");
        String type = geometry.get("type").getAsString();
        JsonArray coords = geometry.getAsJsonArray("coordinates");

        int rows = 0;
        if (type.equals("LineString")) {
            rows += writeLine(coords, stfips, begMpText, endMpText, channel, writer);
        } else if (type.equals("MultiLineString")) {
            // A MultiLineString is a list of lines; write each one in order.
            for (JsonElement part : coords) {
                rows += writeLine(part.getAsJsonArray(), stfips, begMpText, endMpText, channel, writer);
            }
        }
        return rows;
    }

    /**
     * Writes one CSV row per [lon, lat] pair in a single line of coordinates, skipping any
     * point whose geohash has already been written (so every geohash in the CSV is unique).
     *
     * @return the number of rows actually written
     */
    private static int writeLine(JsonArray line, String stfips, String begMp, String endMp,
                                 int channel, BufferedWriter writer) throws IOException {
        int rows = 0;
        for (JsonElement pair : line) {
            JsonArray p = pair.getAsJsonArray();
            double lon = p.get(0).getAsDouble(); // GeoJSON order is [longitude, latitude]
            double lat = p.get(1).getAsDouble();

            String hash = geohashEncode(lat, lon, GEOHASH_PRECISION);

            // Keep only the FIRST point seen for each geohash. Set.add returns false when
            // the value is already in the set, which means this geohash was written before.
            if (!seenGeohashes.add(hash)) {
                duplicatesRemoved++;
                continue;
            }

            // Plain concatenation prints lat and lon unrounded, exactly as Java parsed them.
            writer.write(hash + "," + lat + "," + lon + "," + stfips + ","
                    + begMp + "," + endMp + "," + channel);
            writer.newLine();
            rows++;
        }
        return rows;
    }

    /**
     * Converts a segment's ENDMP into its channel number.
     *
     * Channel 10 covers 0 to 10 inclusive, channel 20 covers anything above 10 up to 20
     * inclusive, and so on. That is ceil(endMp / 10) * 10: dividing by 10 and rounding up
     * gives the number of the 10-mile block the milepost falls in, and multiplying by 10
     * turns that block number back into a channel. For example:
     *   6.825 -> ceil(0.6825) = 1 -> channel 10
     *   10.0  -> ceil(1.0)    = 1 -> channel 10
     *   10.5  -> ceil(1.05)   = 2 -> channel 20
     * Values at or below 0 are forced into channel 10, and values above 290 are clamped.
     */
    private static int calcChannel(double endMp) {
        if (endMp <= 0) {
            return CHANNEL_SIZE;
        }
        int channel = (int) Math.ceil(endMp / CHANNEL_SIZE) * CHANNEL_SIZE;
        return Math.min(channel, MAX_CHANNEL);
    }

    /**
     * Encodes a latitude/longitude pair as a geohash string.
     *
     * Geohash works by repeatedly cutting the world in half:
     *   - Start with the full ranges lon -180..180 and lat -90..90.
     *   - Alternate between longitude and latitude, beginning with longitude.
     *   - Each step compares the coordinate with the midpoint of its current range.
     *     At or above the midpoint writes a 1 bit and keeps the upper half; below it
     *     writes a 0 bit and keeps the lower half.
     *   - Every 5 bits form a number from 0 to 31, which is looked up in BASE32
     *     to give one character.
     * Points that share a geohash prefix are close together, so the geohash works as a
     * fast index for finding nearby highway points.
     *
     * Check value: (57.64911, 10.40744) at precision 5 should give "u4pru".
     */
    static String geohashEncode(double lat, double lon, int precision) {
        double latLo = -90.0, latHi = 90.0;
        double lonLo = -180.0, lonHi = 180.0;

        StringBuilder hash = new StringBuilder();
        int bits = 0;           // bits collected for the character being built
        int bitCount = 0;       // how many of the 5 bits we have so far
        boolean evenBit = true; // true = longitude step, false = latitude step

        while (hash.length() < precision) {
            if (evenBit) {
                double mid = (lonLo + lonHi) / 2;
                if (lon >= mid) {
                    bits = (bits << 1) | 1;
                    lonLo = mid;
                } else {
                    bits = bits << 1;
                    lonHi = mid;
                }
            } else {
                double mid = (latLo + latHi) / 2;
                if (lat >= mid) {
                    bits = (bits << 1) | 1;
                    latLo = mid;
                } else {
                    bits = bits << 1;
                    latHi = mid;
                }
            }
            evenBit = !evenBit;
            bitCount++;

            if (bitCount == 5) {
                hash.append(BASE32.charAt(bits));
                bits = 0;
                bitCount = 0;
            }
        }
        return hash.toString();
    }
}