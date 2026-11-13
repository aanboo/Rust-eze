import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HiChanIntegrationTest {
    private final GPSLogEntry waEntry = new GPSLogEntry("2026-09-22 20:37:41.000", 47.654030, -117.381552, 568, 26.41, 26);
    private final GPSLogEntry idEntry = new GPSLogEntry("2026-09-22 20:17:15.000", 47.71314203, -116.9488803, 647, 13.05, 22);

    private final HighwaySegment waSegment = new HighwaySegment("c2kx0", "WA", 279, 279, 280);
    private final HighwaySegment idSegment = new HighwaySegment("c2kz6", "ID", 0, 7, 10);

    @org.junit.jupiter.api.Test
    void inputProcessingToHighwayMatching() throws IOException {
        Path tempGps = Files.createTempFile("gps_integration", ".csv");

        String preprocPath = "src/i90_channels.csv";
        if (new File("i90_channels.csv").exists()) {
            preprocPath = "i90_channels.csv";
        }

        try {
            Files.writeString(tempGps,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:37:41.000,47.654030,-117.381552,568,26.41,26\n" +
                            "2026-09-22 20:17:15.000,47.71314203,-116.9488803,647,13.05,22\n"
            );

            Map<String, List<HighwaySegment>> highwayIndex = InputProcessing.readPreprocessedCsv(preprocPath);
            List<GPSLogEntry> gpsEntries = InputProcessing.readGPSLog(tempGps.toString());
            List<MatchedRecord> results = HighwayMatching.matchRecords(gpsEntries, highwayIndex);

            assertEquals(2, results.size());
            assertEquals("WA", results.get(0).getState());
            assertEquals("#I90WAEB_280", results.get(0).getChannelID());
            assertEquals("ID", results.get(1).getState());
            assertEquals("#I90IDEB_10", results.get(1).getChannelID());
        } finally {
            Files.deleteIfExists(tempGps);
        }
    }

    @org.junit.jupiter.api.Test
    void duplicateHandlingAcrossComponents() throws IOException {
        Path tempGps = Files.createTempFile("gps_dup_integration", ".csv");
        File tempOut = new File("temp_dup_integration.csv");
        if (tempOut.exists()) {
            tempOut.delete();
        }

        String preprocPath = "src/i90_channels.csv";
        if (new File("i90_channels.csv").exists()) {
            preprocPath = "i90_channels.csv";
        }

        try {
            Files.writeString(tempGps,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:37:41.000,47.654030,-117.381552,568,26.41,26\n" +
                            "2026-09-22 20:37:41.000,47.71314203,-116.9488803,647,13.05,22\n" +
                            "2026-09-22 20:37:46.000,47.654030,-117.381552,568,0.0,26\n"
            );

            Map<String, List<HighwaySegment>> highwayIndex = InputProcessing.readPreprocessedCsv(preprocPath);
            List<GPSLogEntry> gpsEntries = InputProcessing.readGPSLog(tempGps.toString());
            List<MatchedRecord> results = HighwayMatching.matchRecords(gpsEntries, highwayIndex);
            OutputGeneration.writeOutput(results, tempOut.getPath());

            assertTrue(tempOut.exists());
            List<String> lines = Files.readAllLines(tempOut.toPath());
            assertEquals(3, lines.size());
            assertTrue(lines.get(1).contains("2026-09-22 20:37:41.000"));
            assertTrue(lines.get(1).endsWith(",#I90WAEB_280"));
            assertTrue(lines.get(2).contains("2026-09-22 20:37:46.000"));
            assertTrue(lines.get(1).endsWith(",#I90WAEB_280"));
        } finally {
            Files.deleteIfExists(tempGps);
            tempOut.delete();
        }
    }

    @org.junit.jupiter.api.Test
    void highwayMatchingToOutputGeneration() throws IOException {
        Map<String, List<HighwaySegment>> highwayIndex = new HashMap<>();
        highwayIndex.put("c2kx0", new ArrayList<>(List.of(waSegment)));
        highwayIndex.put("c2kz6", new ArrayList<>(List.of(idSegment)));

        File tempOut = new File("temp_match_output.csv");
        if (tempOut.exists()) {
            tempOut.delete();
        }

        try {
            List<MatchedRecord> results = HighwayMatching.matchRecords(Arrays.asList(waEntry, idEntry), highwayIndex);
            OutputGeneration.writeOutput(results, tempOut.getPath());

            assertTrue(tempOut.exists());
            List<String> lines = Files.readAllLines(tempOut.toPath());
            assertEquals(3, lines.size());
            assertEquals("date time,latitude,longitude,altitude(m),speed(m/s),sat_used,ChannelID", lines.get(0));
            assertTrue(lines.get(1).contains("2026-09-22 20:37:41.000"));
            assertTrue(lines.get(1).endsWith(",#I90WAEB_280"));
            assertTrue(lines.get(2).contains("2026-09-22 20:17:15.000"));
            assertTrue(lines.get(2).endsWith(",#I90IDEB_10"));
        } finally {
            tempOut.delete();
        }
    }

    @org.junit.jupiter.api.Test
    void missingHighwayMatch() throws IOException {
        File tempOut = new File("temp_unknown_output.csv");
        if (tempOut.exists()) {
            tempOut.delete();
        }

        String preprocPath = "src/i90_channels.csv";
        if (new File("i90_channels.csv").exists()) {
            preprocPath = "i90_channels.csv";
        }

        try {
            Map<String, List<HighwaySegment>> highwayIndex = InputProcessing.readPreprocessedCsv(preprocPath);
            GPSLogEntry unknownEntry = new GPSLogEntry("2026-09-22 20:45:00.000", 38.5816, -121.4944, 10, 20.0, 18);

            List<MatchedRecord> results = HighwayMatching.matchRecords(List.of(unknownEntry), highwayIndex);
            assertEquals(1, results.size());
            assertFalse(results.get(0).hasMatch());
            assertEquals("UNKNOWN", results.get(0).getChannelID());

            OutputGeneration.writeOutput(results, tempOut.getPath());
            List<String> lines = Files.readAllLines(tempOut.toPath());
            assertEquals(2, lines.size());
            assertTrue(lines.get(1).endsWith(",UNKNOWN"));
        } finally {
            tempOut.delete();
        }
    }

    @org.junit.jupiter.api.Test
    void inputFailurePropagation() throws IOException {
        File tempOut = new File("temp_input_fail.csv");
        if (tempOut.exists()) {
            tempOut.delete();
        }

        Path malformedGps = Files.createTempFile("gps_malformed", ".csv");

        try {
            // Missing input file
            HiChan.main(new String[]{"this_file_does_not_exist.csv", tempOut.getPath()});
            assertFalse(tempOut.exists());

            // Malformed CSV file
            Files.writeString(malformedGps,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n2026-09-22 20:17:15.000,not_a_number,-116.9488803,647,13.05,22\n"
            );
            HiChan.main(new String[]{malformedGps.toString(), tempOut.getPath()});
            assertFalse(tempOut.exists());
        } finally {
            Files.deleteIfExists(malformedGps);
            tempOut.delete();
        }
    }

    @org.junit.jupiter.api.Test
    void outputFailurePropagation() throws IOException {
        Path tempGps = Files.createTempFile("gps_output_fail", ".csv");
        File badOutput = new File("nonexistent_dir/temp_output_fail.csv");

        try {
            Files.writeString(tempGps,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n2026-09-22 20:37:41.000,47.654030,-117.381552,568,26.41,26\n"
            );

            HiChan.main(new String[]{tempGps.toString(), badOutput.getPath()});
            assertFalse(badOutput.exists());
        } finally {
            Files.deleteIfExists(tempGps);
        }
    }

    @org.junit.jupiter.api.Test
    void endToEndExecution() throws IOException {
        Path tempGps = Files.createTempFile("gps_e2e", ".csv");
        File tempOut = new File("temp_e2e_output.csv");
        if (tempOut.exists()) {
            tempOut.delete();
        }

        String preprocPath = "src/i90_channels.csv";
        if (new File("i90_channels.csv").exists()) {
            preprocPath = "i90_channels.csv";
        }

        String originalGps =
                "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                        "2026-09-22 20:37:41.000,47.654030,-117.381552,568,26.41,26\n" +
                        "2026-09-22 20:17:15.000,47.71314203,-116.9488803,647,13.05,22\n";

        try {
            Files.writeString(tempGps, originalGps);
            String originalChannels = Files.readString(Path.of(preprocPath));

            HiChan.main(new String[]{tempGps.toString(), tempOut.getPath()});

            assertTrue(tempOut.exists());
            assertEquals(originalGps, Files.readString(tempGps));
            assertEquals(originalChannels, Files.readString(Path.of(preprocPath)));

            List<String> lines = Files.readAllLines(tempOut.toPath());
            assertEquals(3, lines.size());
            assertEquals("date time,latitude,longitude,altitude(m),speed(m/s),sat_used,ChannelID", lines.get(0));
            assertTrue(lines.get(1).contains("2026-09-22 20:37:41.000"));
            assertTrue(lines.get(1).endsWith(",#I90WAEB_280"));
            assertTrue(lines.get(2).contains("2026-09-22 20:17:15.000"));
            assertTrue(lines.get(2).endsWith(",#I90IDEB_10"));
        } finally {
            Files.deleteIfExists(tempGps);
            tempOut.delete();
        }
    }
}