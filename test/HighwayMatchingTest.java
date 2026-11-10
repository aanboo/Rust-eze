import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class HighwayMatchingTest {

    private final GPSLogEntry waEntry = new GPSLogEntry("2026-09-22 20:37:41.000", 47.654030, -117.381552, 568, 26.41, 26);
    private final GPSLogEntry idEntry = new GPSLogEntry("2026-09-22 20:17:15.000", 47.71314203, -116.9488803, 647, 13.05, 22);

    private final HighwaySegment waSegment = new HighwaySegment("c2kx0", "WA", 279, 279, 280);
    private final HighwaySegment idSegment = new HighwaySegment("c2kz6", "ID", 0, 7, 10);

    @org.junit.jupiter.api.Test
    void matchRecords() {
        Map<String, List<HighwaySegment>> highwayIndex = new HashMap<>();
        highwayIndex.put("c2kx0", new ArrayList<>(List.of(waSegment)));
        highwayIndex.put("c2kz6", new ArrayList<>(List.of(idSegment)));

        List<MatchedRecord> results = HighwayMatching.matchRecords(Arrays.asList(waEntry, idEntry), highwayIndex);

        assertEquals(2, results.size());

        assertEquals("c2kx0", results.getFirst().getSegment().getGeohash());
        assertEquals("WA", results.get(0).getState());
        assertEquals(280, results.get(0).getChannel());

        assertEquals("c2kz6", results.get(1).getSegment().getGeohash());
        assertEquals("ID", results.get(1).getState());
        assertEquals(10, results.get(1).getChannel());

        boolean unmatchedCaught = false;
        try {
            HighwayMatching.matchRecords(List.of(waEntry), new HashMap<>());
        } catch (IllegalArgumentException e) {
            unmatchedCaught = true;
        }
        assertTrue(unmatchedCaught);

        boolean nullCaught = false;
        try {
            HighwayMatching.matchRecords(null, highwayIndex);
        } catch (NullPointerException e) {
            nullCaught = true;
        }
        assertTrue(nullCaught);
    }

    @org.junit.jupiter.api.Test
    void encodeGeohash() {
        String hashWa = HighwayMatching.encodeGeohash(waEntry.getLat(), waEntry.getLon(), 5);
        String hashId = HighwayMatching.encodeGeohash(idEntry.getLat(), idEntry.getLon(), 5);

        assertEquals("c2kx0", hashWa);
        assertEquals("c2kz6", hashId);
    }
}