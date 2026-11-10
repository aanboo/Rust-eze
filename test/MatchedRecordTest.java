import static org.junit.jupiter.api.Assertions.*;

class MatchedRecordTest {

    private final GPSLogEntry waEntry = new GPSLogEntry("2026-09-22 20:37:41.000", 47.654030, -117.381552, 568, 26.41, 26);
    private final GPSLogEntry idEntry = new GPSLogEntry("2026-09-22 20:17:15.000", 47.71314203, -116.9488803, 647, 13.05, 22);

    private final HighwaySegment waSegment = new HighwaySegment("c2kx0", "WA", 279, 279, 280);
    private final HighwaySegment idSegment = new HighwaySegment("c2kz6", "ID", 0, 7, 10);

    @org.junit.jupiter.api.Test
    void getGpsEntry() {
        MatchedRecord waRecord = new MatchedRecord(waEntry, waSegment);
        MatchedRecord idRecord = new MatchedRecord(idEntry, idSegment);

        assertEquals(waEntry, waRecord.getGpsEntry());
        assertEquals(idEntry, idRecord.getGpsEntry());
    }

    @org.junit.jupiter.api.Test
    void getSegment() {
        MatchedRecord waRecord = new MatchedRecord(waEntry, waSegment);
        MatchedRecord idRecord = new MatchedRecord(idEntry, idSegment);

        assertEquals(waSegment, waRecord.getSegment());
        assertEquals(idSegment, idRecord.getSegment());
    }

    @org.junit.jupiter.api.Test
    void hasMatch() {
        MatchedRecord waRecord = new MatchedRecord(waEntry, waSegment);
        MatchedRecord idRecord = new MatchedRecord(idEntry, idSegment);
        MatchedRecord unmatchedRecord = new MatchedRecord(waEntry, null);

        assertTrue(waRecord.hasMatch());
        assertTrue(idRecord.hasMatch());
        assertFalse(unmatchedRecord.hasMatch());
    }

    @org.junit.jupiter.api.Test
    void getChannel() {MatchedRecord waRecord = new MatchedRecord(waEntry, waSegment);
        MatchedRecord idRecord = new MatchedRecord(idEntry, idSegment);
        MatchedRecord unmatchedRecord = new MatchedRecord(waEntry, null);

        assertEquals(280, waRecord.getChannel());
        assertEquals(10, idRecord.getChannel());
        assertEquals(0, unmatchedRecord.getChannel());
    }

    @org.junit.jupiter.api.Test
    void getState() {MatchedRecord waRecord = new MatchedRecord(waEntry, waSegment);
        MatchedRecord idRecord = new MatchedRecord(idEntry, idSegment);
        MatchedRecord unmatchedRecord = new MatchedRecord(waEntry, null);

        assertEquals("WA", waRecord.getState());
        assertEquals("ID", idRecord.getState());
        assertEquals("", unmatchedRecord.getState());
    }

    @org.junit.jupiter.api.Test
    void testToString(){
        MatchedRecord waRecord = new MatchedRecord(waEntry, waSegment);
        MatchedRecord idRecord = new MatchedRecord(idEntry, idSegment);

        assertNotNull(waRecord.toString());
        assertTrue(waRecord.toString().contains("c2kx0"));

        assertNotNull(idRecord.toString());
        assertTrue(idRecord.toString().contains("c2kz6"));
    }
}