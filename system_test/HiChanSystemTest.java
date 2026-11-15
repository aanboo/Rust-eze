
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HiChanSystemTest {

    private static final String HIGHWAY =
            "src/i90_channels.csv";


    private String runHiChan(String gpsFile, String answer)
            throws Exception {

        assertTrue(Files.exists(Path.of(HIGHWAY)),
                "Missing highway reference CSV");

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        InputStream oldIn = System.in;

        try (PrintStream capture = new PrintStream(
                output, true, StandardCharsets.UTF_8)) {

            System.setOut(capture);
            System.setErr(capture);

            System.setIn(new ByteArrayInputStream(
                    (answer + "\n").getBytes(
                            StandardCharsets.UTF_8)));

            HiChan.main(new String[]{gpsFile, HIGHWAY});

        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
            System.setIn(oldIn);
        }

        return output.toString(StandardCharsets.UTF_8);
    }


    private String runHiChan(String gpsFile)
            throws Exception {

        return runHiChan(gpsFile, "n");
    }


    private String runHiChanWithOutput(
            String gpsFile,
            Path outputFile,
            String answer
    ) throws Exception {

        String oldOutput =
                System.getProperty("hichan.output");

        try {
            System.setProperty(
                    "hichan.output",
                    outputFile.toString()
            );

            return runHiChan(gpsFile, answer);

        } finally {
            if (oldOutput == null) {
                System.clearProperty("hichan.output");
            } else {
                System.setProperty(
                        "hichan.output",
                        oldOutput
                );
            }
        }
    }

    private String runHiChanWithOutput(
            String gpsFile,
            Path outputFile
    ) throws Exception {

        return runHiChanWithOutput(
                gpsFile, outputFile, "n");
    }


    private void assertError(
            String file,
            String expected
    ) throws Exception {

        String output = runHiChan(file);

        assertTrue(
                output.contains(expected),
                "Expected: " + expected
                        + "\nActual output:\n" + output
        );
    }

    //Westbound GPS processing
    @Test
    void testWestboundGPS(@TempDir Path tempDir)
            throws Exception {

        Path outputFile =
                tempDir.resolve("westbound.csv");

        String console = runHiChanWithOutput(
                "src/GPSLog_1.csv",
                outputFile
        );

        assertTrue(Files.exists(outputFile),
                "Westbound output missing:\n" + console);

        List<String> lines =
                Files.readAllLines(outputFile);

        assertTrue(lines.size() > 1,
                "No GPS records were generated");

        for (int i = 1; i < lines.size(); i++) {

            String[] fields =
                    lines.get(i).split(",", -1);

            assertEquals(7, fields.length,
                    "Incorrect columns at line " + (i + 1));

            assertTrue(
                    fields[6].matches(
                            "#I90(WA|ID)WB_\\d+"),
                    "Invalid westbound Channel ID at line "
                            + (i + 1) + ": " + fields[6]
            );
        }
    }

    //Eastbound GPS processing
    @Test
    void testEastboundGPS(@TempDir Path tempDir)
            throws Exception {

        Path outputFile =
                tempDir.resolve("eastbound.csv");

        String console = runHiChanWithOutput(
                "src/GPSLog_2.csv",
                outputFile
        );

        assertTrue(Files.exists(outputFile),
                "Eastbound output missing:\n" + console);

        List<String> lines =
                Files.readAllLines(outputFile);

        assertTrue(lines.size() > 1,
                "No GPS records were generated");

        for (int i = 1; i < lines.size(); i++) {

            String[] fields =
                    lines.get(i).split(",", -1);

            assertEquals(7, fields.length,
                    "Incorrect columns at line " + (i + 1));

            assertTrue(
                    fields[6].matches(
                            "#I90(WA|ID)EB_\\d+"),
                    "Invalid eastbound Channel ID at line "
                            + (i + 1) + ": " + fields[6]
            );
        }
    }

    //Missing GPS file
    @Test
    void testMissingGPSFile() throws Exception {

        assertError(
                "test_csv/DoesNotExist.csv",
                "Error - File cannot be opened"
        );
    }

    //Invalid CSV format
    @Test
    void testInvalidFormat() throws Exception {

        assertError(
                "test_csv/InvalidGPS.csv",
                "Error - Invalid Format"
        );
    }

    //Duplicate GPS timestamps

    @Test
    void testDuplicateTimestamps(
            @org.junit.jupiter.api.io.TempDir Path tempDir
    ) throws Exception {

        Path inputFile = Path.of("test_csv/DuplicateGPS.csv");
        Path outputFile = tempDir.resolve("duplicates.csv");

        String console = runHiChanWithOutput(
                inputFile.toString(), outputFile
        );

        assertTrue(
                console.contains("Duplicate timestamp"),
                "Expected duplicate timestamp warning:\n" + console
        );

        assertTrue(Files.exists(outputFile),
                "Output CSV was not generated:\n" + console);

        java.util.List<String> inputLines =
                Files.readAllLines(inputFile);

        java.util.List<String> outputLines =
                Files.readAllLines(outputFile);

        // InputProcessing intentionally ignores blank lines
        java.util.List<String> nonBlankInputLines =
                inputLines.stream()
                        .filter(line -> !line.trim().isEmpty())
                        .toList();

        assertEquals(
                nonBlankInputLines.size() - 1,
                outputLines.size(),
                "Duplicate GPS record should be skipped"
        );

        java.util.Set<String> seenTimestamps = new java.util.HashSet<>();
        java.util.List<String> expectedUniqueInputLines = nonBlankInputLines.stream()
                .filter(line -> seenTimestamps.add(line.split(",", -1)[0].trim()))
                .toList();

        // Verify every retained GPS record is preserved in order
        for (int i = 1; i < expectedUniqueInputLines.size(); i++) {
            assertTrue(
                    outputLines.get(i).startsWith(
                            expectedUniqueInputLines.get(i) + ","
                    ), "GPS record changed or skipped at line " + (i + 1)
            );
        }

        // Verify both records with the duplicate timestamp are present
        String duplicateRecord = nonBlankInputLines.get(1);

        long duplicateCount = outputLines.stream()
                .skip(1)
                .filter(line -> line.startsWith(duplicateRecord + ","))
                .count();

        assertEquals(
                1,
                duplicateCount,
                "Only the first duplicate GPS records must be preserved"
        );
    }

    //Overwrite protection
    @Test
    void testOverwriteProtection(
            @TempDir Path tempDir
    ) throws Exception {

        Path outputFile =
                tempDir.resolve("existing.csv");

        String originalContent =
                "Existing file - do not overwrite";

        Files.writeString(
                outputFile,
                originalContent
        );

        String console = runHiChanWithOutput(
                "test_csv/UnmatchedGPS.csv",
                outputFile,
                "n"
        );

        assertTrue(
                console.contains(
                        "Error Overwrite Authorization is not provided"),
                "Expected overwrite authorization error:\n"
                        + console
        );

        assertEquals(
                originalContent,
                Files.readString(outputFile),
                "Existing output file was modified"
        );
    }

    //Unmatched GPS coordinates
    @Test
    void testUnmatchedGPS(
            @TempDir Path tempDir
    ) throws Exception {

        Path outputFile =
                tempDir.resolve("unmatched.csv");

        String console = runHiChanWithOutput(
                "test_csv/UnmatchedGPS.csv",
                outputFile
        );

        assertTrue(
                console.contains("UNKNOWN"),
                "Expected UNKNOWN warning:\n" + console
        );

        assertTrue(Files.exists(outputFile),
                "Unmatched GPS output missing");

        List<String> lines =
                Files.readAllLines(outputFile);

        assertTrue(lines.size() > 1);

        for (int i = 1; i < lines.size(); i++) {

            String[] fields =
                    lines.get(i).split(",", -1);

            assertEquals(7, fields.length);

            assertEquals(
                    "UNKNOWN",
                    fields[6],
                    "Expected UNKNOWN Channel ID at line "
                            + (i + 1)
            );
        }
    }

    //Output CSV integrity
    @Test
    void testOutputIntegrity(
            @TempDir Path tempDir
    ) throws Exception {

        Path inputFile =
                Path.of("src/GPSLog_2.csv");

        Path outputFile =
                tempDir.resolve("integrity.csv");

        String console = runHiChanWithOutput(
                inputFile.toString(),
                outputFile
        );

        assertTrue(Files.exists(outputFile),
                "Output missing:\n" + console);

        List<String> inputLines =
                Files.readAllLines(inputFile);

        List<String> outputLines =
                Files.readAllLines(outputFile);

        assertEquals(
                inputLines.size(),
                outputLines.size(),
                "Input and output record counts differ"
        );

        assertEquals(
                "date time,latitude,longitude,"
                        + "altitude(m),speed(m/s),sat_used,ChannelID",
                outputLines.get(0)
        );

        for (int i = 1; i < inputLines.size(); i++) {

            String[] fields =
                    outputLines.get(i).split(",", -1);

            assertEquals(
                    7,
                    fields.length,
                    "Incorrect column count at line " + (i + 1)
            );

            assertTrue(
                    outputLines.get(i).startsWith(
                            inputLines.get(i) + ","),
                    "Original GPS record changed at line "
                            + (i + 1)
            );

            assertFalse(
                    fields[6].isBlank(),
                    "Missing Channel ID at line " + (i + 1)
            );
        }
    }

    //Invalid GPS coordinates
    @Test
    void testInvalidCoordinates() throws Exception {

        assertError(
                "test_csv/InvalidCoordinates.csv",
                "Error - Invalid GPS Coordinates"
        );
    }

    //Invalid elevation
    @Test
    void testInvalidElevation() throws Exception {

        assertError(
                "test_csv/InvalidElevation.csv",
                "Error - Invalid GPS Elevation"
        );
    }

    //Invalid speed
    @Test
    void testInvalidSpeed() throws Exception {

        assertError(
                "test_csv/InvalidSpeed.csv",
                "Error - Invalid Speed"
        );
    }

    //Invalid satellite count
    @Test
    void testInvalidSatellites() throws Exception {

        assertError(
                "test_csv/InvalidSatellites.csv",
                "Error - Invalid Satellite Count"
        );
    }

    //Empty GPS file
    @Test
    void testEmptyGPS() throws Exception {

        assertError(
                "test_csv/EmptyGPS.csv",
                "Error - GPS Log is Empty"
        );
    }
}
