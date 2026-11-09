import static org.junit.jupiter.api.Assertions.*;

class GPSLogEntryTest {

    private final GPSLogEntry waEntry = new GPSLogEntry("2026-09-22 20:37:41.000", 47.654030, -117.381552, 568.148, 26.41, 26.0);
    private final GPSLogEntry idEntry = new GPSLogEntry("2026-09-22 20:17:15.000", 47.71314203, -116.9488803, 646.743, 13.05, 22.0);

    @org.junit.jupiter.api.Test
    void getDateTime() {
        assertEquals("2026-09-22 20:37:41.000", waEntry.getDateTime());
        assertEquals("2026-09-22 20:17:15.000", idEntry.getDateTime());
    }

    @org.junit.jupiter.api.Test
    void getLat() {
        assertEquals(47.654030, waEntry.getLat(), 0.0001);
        assertEquals(47.71314203, idEntry.getLat(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getLon() {
        assertEquals(-117.381552, waEntry.getLon(), 0.0001);
        assertEquals(-116.9488803, idEntry.getLon(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getAltitude() {
        assertEquals(568.148, waEntry.getAltitude(), 0.001);
        assertEquals(646.743, idEntry.getAltitude(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getSpeed() {
        assertEquals(26.41, waEntry.getSpeed(), 0.001);
        assertEquals(13.05, idEntry.getSpeed(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getSatUsed() {
        assertEquals(26.0, waEntry.getSatUsed(), 0.001);
        assertEquals(22.0, idEntry.getSatUsed(), 0.001);
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        assertNotNull(waEntry.toString());
        assertFalse(waEntry.toString().isEmpty());
        assertTrue(waEntry.toString().contains("2026-09-22 20:37:41.000"));

        assertNotNull(idEntry.toString());
        assertFalse(idEntry.toString().isEmpty());
        assertTrue(idEntry.toString().contains("2026-09-22 20:17:15.000"));
    }
}