import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

class OutputGenerationTest {

    private final GPSLogEntry waEntry = new GPSLogEntry("2026-09-22 20:37:41.000", 47.654030, -117.381552, 568, 26.41, 26);
    private final GPSLogEntry idEntry = new GPSLogEntry("2026-09-22 20:17:15.000", 47.71314203, -116.9488803, 647, 13.05, 22);

    private final HighwaySegment waSegment = new HighwaySegment("c2kx0", "WA", 279, 279, 280);
    private final HighwaySegment idSegment = new HighwaySegment("c2kz6", "ID", 0, 7, 10);

    @org.junit.jupiter.api.Test
    void writeOutput() throws IOException {
        MatchedRecord waRecord = new MatchedRecord(waEntry, waSegment);
        MatchedRecord idRecord = new MatchedRecord(idEntry, idSegment);
        MatchedRecord unmatchedRecord = new MatchedRecord(waEntry, null);

        File tempFile = new File("temp_test.csv");
        if (tempFile.exists()) {
            tempFile.delete();
        }

        try {
            OutputGeneration.writeOutput(Arrays.asList(waRecord, idRecord, unmatchedRecord), tempFile.getPath());
            assertTrue(tempFile.exists());

            List<String> lines = Files.readAllLines(tempFile.toPath());
            assertEquals(4, lines.size());
            assertEquals("date time,latitude,longitude,altitude(m),speed(m/s),sat_used,ChannelID", lines.get(0));

            assertTrue(lines.get(1).contains("2026-09-22 20:37:41.000"));
            assertTrue(lines.get(1).endsWith(",280"));
            assertTrue(lines.get(2).contains("2026-09-22 20:17:15.000"));
            assertTrue(lines.get(2).endsWith(",10"));
            assertTrue(lines.get(3).contains("2026-09-22 20:37:41.000"));
            assertTrue(lines.get(3).endsWith(",UNKNOWN"));

            tempFile.delete();
        } catch (IOException e) {
            System.out.println("writeOutput threw an unexpected IOException: " + e.getMessage());
        }

        File collisionFile = new File("temp_collision.csv");
        try {
            collisionFile.createNewFile();

            boolean collisionCaught = false;
            try {
                OutputGeneration.writeOutput(List.of(waRecord), collisionFile.getPath());
            } catch (IllegalArgumentException e) {
                collisionCaught = true;
            }
            assertTrue(collisionCaught);
            collisionFile.delete();

        } catch (IOException e) {
            System.out.println("Setup failed for collision test: " + e.getMessage());
        }

        boolean nullCaught = false;
        try {
            OutputGeneration.writeOutput(null, "output.csv");
        } catch (IllegalArgumentException e) {
            nullCaught = true;
        }
        assertTrue(nullCaught);

        boolean emptyCaught = false;
        try {
            OutputGeneration.writeOutput(List.of(waRecord), "");
        } catch (IllegalArgumentException e) {
            emptyCaught = true;
        }
        assertTrue(emptyCaught);
    }
}