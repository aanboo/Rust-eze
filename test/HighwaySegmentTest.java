import static org.junit.jupiter.api.Assertions.*;

class HighwaySegmentTest {

    private final HighwaySegment waSegment = new HighwaySegment("c2kx0", "WA", 279, 279, 280);
    private final HighwaySegment idSegment = new HighwaySegment("c2kz6", "ID", 0, 7, 10);

    @org.junit.jupiter.api.Test
    void getGeohash() {
        assertEquals("c2kx0", waSegment.getGeohash());
        assertEquals("c2kz6", idSegment.getGeohash());
    }

    @org.junit.jupiter.api.Test
    void getStFIPS() {
        assertEquals("WA", waSegment.getStFIPS());
        assertEquals("ID", idSegment.getStFIPS());
    }

    @org.junit.jupiter.api.Test
    void getBegMP() {
        assertEquals(279, waSegment.getBegMP());
        assertEquals(0, idSegment.getBegMP());
    }

    @org.junit.jupiter.api.Test
    void getEndMP() {
        assertEquals(279, waSegment.getEndMP());
        assertEquals(7, idSegment.getEndMP());
    }

    @org.junit.jupiter.api.Test
    void getChannel() {
        assertEquals(280, waSegment.getChannel());
        assertEquals(10, idSegment.getChannel());
    }

    @org.junit.jupiter.api.Test
    void getSateCode() {
        assertEquals("WA", waSegment.getSateCode());
        assertEquals("ID", idSegment.getSateCode());

        HighwaySegment waFips = new HighwaySegment("c2kx0", "53", 279, 279, 280);
        HighwaySegment idFips = new HighwaySegment("c2kz6", "16", 0, 7, 10);

        assertEquals("WA", waFips.getSateCode());
        assertEquals("ID", idFips.getSateCode());
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        assertNotNull(waSegment.toString());
        assertTrue(waSegment.toString().contains("c2kx0"));

        assertNotNull(idSegment.toString());
        assertTrue(idSegment.toString().contains("c2kz6"));
    }
}