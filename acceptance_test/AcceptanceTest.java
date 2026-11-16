// AcceptanceTest.java

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AcceptanceTest {
    // Valid trips (Washington & Idaho, Eastbound and Westbound)
    private static final String VALID_EASTBOUND_INPUT = "src/GPSLog_2.csv";
    private static final String VALID_EASTBOUND_INPUT1 = "test_csv/GPSLog_2.csv";
    private static final String VALID_WESTBOUND_INPUT = "src/GPSLog_1.csv";

    // Output files for different tests
    private static final String OUTPUT_VALID_EASTBOUND = "acceptance_test/OUTPUT_VALID_EASTBOUND.csv";
    private static final String OUTPUT_VALID_WESTBOUND = "acceptance_test/OUTPUT_VALID_WESTBOUND.csv";
    private static final String OUTPUT_FOR_DUP_TSTAMPS = "acceptance_test/OUTPUT_DUPLICATE_TIMESTAMPS.csv";
    private static final String OUTPUT_FOR_INVALID_DATA = "acceptance_test/OUTPUT_INVALID_DATA.csv";
    private static final String OUTPUT_FOR_MISSING_HWY = "acceptance_test/OUTPUT_MISSING_HWY.csv";
    private static final String OUTPUT_FOR_MISSING_INPUT = "acceptance_test/OUTPUT_MISSING_INPUT.csv";
    private static final String OUTPUT_FOR_INPUT_PRESERVATION = "acceptance_test/OUTPUT_INPUT_PRESERVATION.csv";


    public static void main(String[] args) throws Exception {
        System.out.println("=== HiChan Acceptance Test Harness ===");

        runTest("Valid trip: Eastbound", AcceptanceTest::testValidTripEastbound);
        runTest("Valid trip: Westbound", AcceptanceTest::testValidTripWestbound);
        runTest("Output accuracy against expected CSV", AcceptanceTest::testOutputAccuracy);
        runTest("Duplicate timestamps warning and filtering", AcceptanceTest::testDuplicateTimestamps);
        runTest("Invalid GPS data: invalid coordinate", AcceptanceTest::testInvalidGpsCoordinate);
        runTest("Invalid GPS data: speed above 90 m/s", AcceptanceTest::testInvalidSpeed);
        runTest("Missing highway match", AcceptanceTest::testMissingHighwayMatch);
        runTest("Missing input file", AcceptanceTest::testMissingInputFile);
        runTest("Input preservation", AcceptanceTest::testInputPreservation);

        System.out.println("=== Acceptance tests finished ===");
    }

    private interface TestCase {
        void run() throws Exception;
    }

    private static void runTest(String name, TestCase testCase) {
        System.out.println();
        System.out.println("---- " + name + " ----");
        try {
            testCase.run();
            System.out.println("[PASS] " + name);
        } catch (AssertionError ae) {
            System.out.println("[FAIL] " + name + ": " + ae.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] " + name + ": " + e);
            e.printStackTrace(System.out);
        }
    }

    // Utility function to run HiChan.main(...) while capturing stdout/stderr.
    private static RunResult runHiChan(String... hiChanArgs) throws Exception {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        PrintStream tempOut = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
        PrintStream tempErr = new PrintStream(errBuffer, true, StandardCharsets.UTF_8);

        Exception thrown = null;
        try {
            System.setOut(tempOut);
            System.setErr(tempErr);

            HiChan.main(hiChanArgs);
        } catch (Exception e) {
            thrown = e;
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            tempOut.flush();
            tempErr.flush();
        }

        return new RunResult(
                outBuffer.toString(StandardCharsets.UTF_8),
                errBuffer.toString(StandardCharsets.UTF_8),
                thrown
        );
    }

    private static class RunResult {
        final String stdout;
        final String stderr;
        final Exception exception;

        RunResult(String stdout, String stderr, Exception exception) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exception = exception;
        }
    }

    // Helper: delete a file if it exists
    private static void deleteIfExists(String path) throws Exception {
        Files.deleteIfExists(Paths.get(path));
    }

    // Helper: asserts the existence / non-existence of a file
    private static void assertFileExists(String path, boolean shouldExist, String message) {
        File f = new File(path);
        if (shouldExist && !f.exists()) {
            throw new AssertionError(message + ": expected file to exist: " + path);
        }
        if (!shouldExist && f.exists()) {
            throw new AssertionError(message + ": expected file NOT to exist: " + path);
        }
    }

    // Helper: asserts substring in stderr or stdout
    private static void assertContains(String text, String expectedSubstring, String message) {
        if (text == null) text = "";
        if (!text.contains(expectedSubstring)) {
            throw new AssertionError(message + " (expected to contain: \"" + expectedSubstring + "\")");
        }
    }

    // Helper: compare two text files as strings (exact match)
    private static void assertFileContentEquals(String expectedPath, String actualPath, String message) throws Exception {
        Path expected = Paths.get(expectedPath);
        Path actual = Paths.get(actualPath);

        String expectedText = Files.readString(expected, StandardCharsets.UTF_8);
        String actualText = Files.readString(actual, StandardCharsets.UTF_8);

        if (!expectedText.equals(actualText)) {
            throw new AssertionError(message + ": file contents differ.\nExpected: " + expectedPath + "\nActual:   " + actualPath);
        }
    }

    // Helper: snapshot file bytes (or null if missing)
    private static byte[] snapshotFileBytes(String path) throws Exception {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            return null;
        }
        return Files.readAllBytes(p);
    }

    // ======================
    // Expected behavior:
    // - HiChan completes successfully.
    // - OUTPUT_VALID_EASTBOUND is created and contains a retained row for each valid GPS record
    //   with the correct channel assigned (this is not deeply inspected here, only existence).
    private static void testValidTripEastbound() throws Exception {
        deleteIfExists(OUTPUT_VALID_EASTBOUND);

        RunResult result = runHiChan(
                VALID_EASTBOUND_INPUT,"src/i90_channels.csv",
                OUTPUT_VALID_EASTBOUND
        );

        if (result.exception != null) {
            throw new AssertionError("HiChan threw exception for valid eastbound trip: " + result.exception);
        }

        assertFileExists(OUTPUT_VALID_EASTBOUND, true, "Valid eastbound trip should produce output");
    }

    // Expected behavior:
    // - HiChan completes successfully.
    // - OUTPUT_VALID_WESTBOUND is created and contains retained rows for each valid GPS record
    //   with correct channels (only existence is asserted here).
    private static void testValidTripWestbound() throws Exception {
        deleteIfExists(OUTPUT_VALID_WESTBOUND);

        RunResult result = runHiChan(
                VALID_WESTBOUND_INPUT,"src/i90_channels.csv",
                OUTPUT_VALID_WESTBOUND
        );

        if (result.exception != null) {
            throw new AssertionError("HiChan threw exception for valid westbound trip: " + result.exception);
        }

        assertFileExists(OUTPUT_VALID_WESTBOUND, true, "Valid westbound trip should produce output");
    }


    // Expected behavior:
    // - HiChan completes successfully.
    // - Generated output matches EXPECTED_OUTPUT_ACCURACY exactly (same rows, same order, same channels).
    private static void testOutputAccuracy() throws Exception {
        deleteIfExists(OUTPUT_VALID_EASTBOUND);

        RunResult result = runHiChan(
                VALID_EASTBOUND_INPUT1,"src/i90_channels.csv",
                OUTPUT_VALID_EASTBOUND
        );

        if (result.exception != null) {
            throw new AssertionError("HiChan threw exception during output accuracy test: " + result.exception);
        }

        assertFileExists(OUTPUT_VALID_EASTBOUND, true, "Accuracy test should produce output");

        assertFileContentEquals(
                "test_csv/ExpectedOuputAccE.csv",
                OUTPUT_VALID_EASTBOUND,
                "Output CSV must match expected CSV exactly"
        );
    }

    // Expected behavior:
    // - A warning about duplicate timestamps appears (either in stdout or stderr).
    // - Only the first occurrence of each duplicated timestamp is retained in the output.
    private static void testDuplicateTimestamps() throws Exception {
        deleteIfExists(OUTPUT_FOR_DUP_TSTAMPS);

        RunResult result = runHiChan(
                "test_csv/DuplicateGPS.csv","src/i90_channels.csv",
                OUTPUT_FOR_DUP_TSTAMPS
        );


        assertFileExists(OUTPUT_FOR_DUP_TSTAMPS, true, "Duplicate timestamp test should produce output");

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "Error - Duplicate timestamp entry detected for:",
                "Duplicate timestamp warning should mention 'duplicate'"
        );

        // NOTE: To strictly verify that "only the first record is included", you can:
        // - Read OUTPUT_FOR_DUP_TSTAMPS CSV.
        // - Collect timestamps and ensure each appears only once.
        // Skeleton code is shown below; customize parsing to your actual CSV format.

        // Example (optional, safe if you fill in a real CSV format):
        /*
        List<String> lines = Files.readAllLines(Paths.get(OUTPUT_FOR_DUP_TSTAMPS), StandardCharsets.UTF_8);
        Set<String> seen = new HashSet<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split(",");  // adjust if different delimiter
            String timestamp = fields[0];       // adjust index to actual timestamp column
            if (!seen.add(timestamp)) {
                throw new AssertionError("Found duplicate timestamp in output: " + timestamp);
            }
        }
        */
    }

    // Expected behavior:
    // - Skips invalid coordinates
    // - An error message about invalid coordinates appears in stderr or stdout.
    // - OUTPUT_FOR_INVALID_DATA is NOT created or is left unchanged for that entry.
    private static void testInvalidGpsCoordinate() throws Exception {
        deleteIfExists(OUTPUT_FOR_INVALID_DATA);

        RunResult result = runHiChan(
                "test_csv/InvalidCoordinates.csv","src/i90_channels.csv",
                OUTPUT_FOR_INVALID_DATA
        );

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "Error - Invalid GPS Coordinates",
                "Invalid coordinate test should emit an Error - Invalid GPS Coordinates"
        );

        assertFileExists(
                OUTPUT_FOR_INVALID_DATA,
                false,
                "Invalid coordinate test should not create or modify the output file"
        );
    }

    // Expected behavior:
    // - Processing skips entry when high-speed record is encountered.
    // - An error message about speed appears.
    // - OUTPUT_FOR_INVALID_DATA is NOT created or is left unchanged for that record.
    private static void testInvalidSpeed() throws Exception {
        deleteIfExists(OUTPUT_FOR_INVALID_DATA);

        RunResult result = runHiChan(
                "test_csv/InvalidSpeed.csv","src/i90_channels.csv",
                OUTPUT_FOR_INVALID_DATA
        );

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "Error - Invalid Speed",
                "Invalid speed test should emit Error - Invalid Speed.')"
        );

        assertFileExists(
                OUTPUT_FOR_INVALID_DATA,
                false,
                "Invalid speed test should not create or modify the output file"
        );
    }


    // Expected behavior:
    // - An error about missing highway match or unmatched location appears.
    // - Processing fails; OUTPUT_FOR_MISSING_HWY is NOT produced.
    private static void testMissingHighwayMatch() throws Exception {
        String MISSING_HWY_INPUT = "C:\\Users\\aaliy\\OneDrive\\Documents\\School\\Software dev class\\Rust-eze\\test_csv\\AcceptanceTest_I80EBSacramento.csv";

        deleteIfExists(OUTPUT_FOR_MISSING_HWY);

        RunResult result = runHiChan(
                MISSING_HWY_INPUT,"src/i90_channels.csv",
                OUTPUT_FOR_MISSING_HWY
        );

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "Warning - Channel ID UNKNOWN for GPS record:",
                "Missing highway match test should emit an error mentioning Warning - Channel ID UNKNOWN for GPS record:"
        );

        assertFileExists(
                OUTPUT_FOR_MISSING_HWY,
                true,
                "Missing highway match test should create or modify the output file"
        );
    }

    // Expected behavior:
    // - A file-access / file-not-found error appears.
    // - OUTPUT_FOR_MISSING_INPUT is NOT produced.
    private static void testMissingInputFile() throws Exception {
        String NONEXISTENT_INPUT = "DoesNotExists.csv";

        deleteIfExists(OUTPUT_FOR_MISSING_INPUT);

        RunResult result = runHiChan(
                NONEXISTENT_INPUT,"src/i90_channels.csv",
                OUTPUT_FOR_MISSING_INPUT
        );

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "Error - File cannot be opened",
                "Missing input file test should emit Error - File cannot be opened"
        );

        assertFileExists(
                OUTPUT_FOR_MISSING_INPUT,
                false,
                "Missing input file test should not create or modify the output file"
        );
    }

    // Expected behavior:
    // - HiChan completes successfully and produces OUTPUT_FOR_INPUT_PRESERVATION.
    // - The byte contents of PRESERVATION_INPUT_GPS_LOG and PRESERVATION_INPUT_HIGHWAY_REF
    //   are *unchanged* before vs. after the run.
    private static void testInputPreservation() throws Exception {
        deleteIfExists(OUTPUT_FOR_INPUT_PRESERVATION);

        byte[] beforeGps = snapshotFileBytes(VALID_EASTBOUND_INPUT);
        byte[] beforeRef = snapshotFileBytes("src/I90_channels.csv");

        if (beforeGps == null || beforeRef == null) {
            throw new AssertionError("Input preservation test requires both input files to exist before running.");
        }

        RunResult result = runHiChan(
                VALID_EASTBOUND_INPUT,
                "src/I90_channels.csv",
                OUTPUT_FOR_INPUT_PRESERVATION
        );

        if (result.exception != null) {
            throw new AssertionError("HiChan threw exception during input preservation test: " + result.exception);
        }

        assertFileExists(
                OUTPUT_FOR_INPUT_PRESERVATION,
                true,
                "Input preservation test should produce output"
        );

        byte[] afterGps = snapshotFileBytes(VALID_EASTBOUND_INPUT);
        byte[] afterRef = snapshotFileBytes("src/I90_channels.csv");

        if (!java.util.Arrays.equals(beforeGps, afterGps)) {
            throw new AssertionError("GPS input file was modified during processing.");
        }
        if (!java.util.Arrays.equals(beforeRef, afterRef)) {
            throw new AssertionError("Highway reference input file was modified during processing.");
        }
    }
}