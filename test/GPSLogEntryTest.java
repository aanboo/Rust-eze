import static org.junit.jupiter.api.Assertions.*;

class GPSLogEntryTest {

    private final GPSLogEntry entry = new GPSLogEntry(
            "2026-09-22 20:17:15.000",47.71314203,-116.9488803,646.743,13.05,22
    );

    @org.junit.jupiter.api.Test
    void getDateTime() {
        assertEquals("2026-09-22 20:17:15.000", entry.getDateTime());
    }

    @org.junit.jupiter.api.Test
    void getLat() {
        assertEquals(47.71314203, entry.getLat(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getLon() {
        assertEquals(-116.9488803, entry.getLon(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getAltitude() {
        assertEquals(646.743, entry.getAltitude(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getSpeed() {
        assertEquals(13.05, entry.getSpeed(), 0.0001);
    }

    @org.junit.jupiter.api.Test
    void getSatUsed() {
        assertEquals(22.0, entry.getSatUsed());
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        assertNotNull(entry.toString());
        assertFalse(entry.toString().isEmpty());
        assertEquals(
                "2026-09-22 20:17:15.000 47.71314203 -116.9488803 646.743 13.05 22.0",
                entry.toString()
        );
    }
}