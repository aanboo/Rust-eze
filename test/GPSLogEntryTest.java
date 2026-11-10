import static org.junit.jupiter.api.Assertions.*;

class GPSLogEntryTest {

    private final GPSLogEntry waEntry = new GPSLogEntry("2026-09-22 20:37:41.000", 47.654030, -117.381552, 568, 26.41, 26);
    private final GPSLogEntry idEntry = new GPSLogEntry("2026-09-22 20:17:15.000", 47.71314203, -116.9488803, 647, 13.05, 22);

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
        assertEquals(-117.381552, waEntry.getLon());
        assertEquals(-116.9488803, idEntry.getLon());
    }

    @org.junit.jupiter.api.Test
    void getAltitude() {
        assertEquals(568.0, waEntry.getAltitude(), 0.001);
        assertEquals(647.0, idEntry.getAltitude(), 0.001);
    }

    @org.junit.jupiter.api.Test
    void getSpeed() {
        assertEquals(26.41, waEntry.getSpeed());
        assertEquals(13.05, idEntry.getSpeed());
    }

    @org.junit.jupiter.api.Test
    void getSatUsed() {
        assertEquals(26, waEntry.getSatUsed());
        assertEquals(22, idEntry.getSatUsed());
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