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
    private static final String VALID_EASTBOUND_INPUT = "PATH/TO/VALID_WA_ID_EASTBOUND_LOG.csv";
    private static final String VALID_WESTBOUND_INPUT = "PATH/TO/VALID_WA_ID_WESTBOUND_LOG.csv";

    // Output files for different tests
    private static final String OUTPUT_VALID_EASTBOUND = "acceptance_test/OUTPUT_VALID_EASTBOUND.csv";
    private static final String OUTPUT_VALID_WESTBOUND = "acceptance_test/OUTPUT_VALID_WESTBOUND.csv";
    private static final String OUTPUT_FOR_DUP_TSTAMPS = "acceptance_test/OUTPUT_DUPLICATE_TIMESTAMPS.csv";
    private static final String OUTPUT_FOR_INVALID_DATA = "acceptance_test/OUTPUT_INVALID_DATA.csv";
    private static final String OUTPUT_FOR_MISSING_HWY = "acceptance_test/OUTPUT_MISSING_HWY.csv";
    private static final String OUTPUT_FOR_MISSING_INPUT = "acceptance_test/OUTPUT_MISSING_INPUT.csv";
    private static final String OUTPUT_FOR_INPUT_PRESERVATION = "acceptance_test/OUTPUT_INPUT_PRESERVATION.csv";

    // Expected output for accuracy comparison
    private static final String EXPECTED_OUTPUT_ACCURACY = "PATH/TO/EXPECTED_OUTPUT_ACCURACY.csv";

    // Inputs used for input-preservation test
    private static final String PRESERVATION_INPUT_GPS_LOG = "PATH/TO/PRESERVATION_GPS_LOG.csv";
    private static final String PRESERVATION_INPUT_HIGHWAY_REF = "PATH/TO/PRESERVATION_HIGHWAY_REF.csv";

    public static void main(String[] args) throws Exception {
        System.out.println("=== HiChan Acceptance Test Harness ===");

        runTest("Valid trip: Eastbound", AcceptanceTest::testValidTripEastbound);
        runTest("Valid trip: Westbound", AcceptanceTest::testValidTripWestbound);
        runTest("Output accuracy against expected CSV", AcceptanceTest::testOutputAccuracy);
        runTest("Duplicate timestamps warning and filtering", AcceptanceTest::testDuplicateTimestamps);
        runTest("Invalid GPS data: invalid coordinate", AcceptanceTest::testInvalidGpsCoordinate);
        runTest("Invalid GPS data: speed above 90 m/s", AcceptanceTest::testInvalidSpeed);
        runTest("Invalid GPS data: malformed row", AcceptanceTest::testMalformedRow);
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
    // Individual test cases
    // ======================
    // Contents required:
    // - VALID_EASTBOUND_INPUT: GPS log CSV consisting of a valid trip across Washington and Idaho,
    //   traveling EASTBOUND along the target highway. All records must be valid, with timestamps
    //   in chronological order, and all locations should match highway-reference geohashes.
    // - : Highway channel reference CSV containing geohash/channel mappings that
    //   cover all locations in VALID_EASTBOUND_INPUT.
    // Expected behavior:
    // - HiChan completes successfully.
    // - OUTPUT_VALID_EASTBOUND is created and contains a retained row for each valid GPS record
    //   with the correct channel assigned (this is not deeply inspected here, only existence).
    private static void testValidTripEastbound() throws Exception {
        deleteIfExists(OUTPUT_VALID_EASTBOUND);

        RunResult result = runHiChan(
                VALID_EASTBOUND_INPUT,
                OUTPUT_VALID_EASTBOUND
        );

        if (result.exception != null) {
            throw new AssertionError("HiChan threw exception for valid eastbound trip: " + result.exception);
        }

        assertFileExists(OUTPUT_VALID_EASTBOUND, true, "Valid eastbound trip should produce output");
    }

    // Contents required:
    // - VALID_WESTBOUND_INPUT: GPS log CSV consisting of a valid trip across Washington and Idaho,
    //   traveling WESTBOUND along the target highway. All records must be valid, with timestamps
    //   in chronological order, and all locations should match highway-reference geohashes.
    // - : Highway channel reference CSV containing geohash/channel mappings that
    //   cover all locations in VALID_WESTBOUND_INPUT.
    // Expected behavior:
    // - HiChan completes successfully.
    // - OUTPUT_VALID_WESTBOUND is created and contains retained rows for each valid GPS record
    //   with correct channels (only existence is asserted here).
    private static void testValidTripWestbound() throws Exception {
        deleteIfExists(OUTPUT_VALID_WESTBOUND);

        RunResult result = runHiChan(
                VALID_WESTBOUND_INPUT,
                OUTPUT_VALID_WESTBOUND
        );

        if (result.exception != null) {
            throw new AssertionError("HiChan threw exception for valid westbound trip: " + result.exception);
        }

        assertFileExists(OUTPUT_VALID_WESTBOUND, true, "Valid westbound trip should produce output");
    }

    // Contents required:
    // - VALID_EASTBOUND_INPUT (or another valid GPS log):
    //   GPS log CSV for a valid trip whose expected output has been manually prepared.
    // - : Highway channel reference CSV that matches all GPS points.
    // - EXPECTED_OUTPUT_ACCURACY: A manually crafted CSV whose rows correspond exactly to the
    //   expected retained GPS records in order, and where the final field in each row is the
    //   expected channel for that record.
    // Expected behavior:
    // - HiChan completes successfully.
    // - Generated output matches EXPECTED_OUTPUT_ACCURACY exactly (same rows, same order, same channels).
    private static void testOutputAccuracy() throws Exception {
        deleteIfExists(OUTPUT_VALID_EASTBOUND);

        RunResult result = runHiChan(
                VALID_EASTBOUND_INPUT,
                OUTPUT_VALID_EASTBOUND
        );

        if (result.exception != null) {
            throw new AssertionError("HiChan threw exception during output accuracy test: " + result.exception);
        }

        assertFileExists(OUTPUT_VALID_EASTBOUND, true, "Accuracy test should produce output");

        assertFileContentEquals(
                EXPECTED_OUTPUT_ACCURACY,
                OUTPUT_VALID_EASTBOUND,
                "Output CSV must match expected CSV exactly"
        );
    }

    // Expected behavior:
    // - A warning about duplicate timestamps appears (either in stdout or stderr).
    // - Only the first occurrence of each duplicated timestamp is retained in the output.
    // - OUTPUT_FOR_DUP_TSTAMPS is created and contains no duplicate timestamps.
    private static void testDuplicateTimestamps() throws Exception {
        deleteIfExists(OUTPUT_FOR_DUP_TSTAMPS);

        RunResult result = runHiChan(
                "test_csv/DuplicateGPS.csv",
                OUTPUT_FOR_DUP_TSTAMPS
        );


        assertFileExists(OUTPUT_FOR_DUP_TSTAMPS, true, "Duplicate timestamp test should produce output");

        // Adjust this substring to whatever warning text HiChan actually uses.
        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "Error - Duplicate timestamp entry detected for:",
                "Duplicate timestamp warning should mention 'duplicate' (adjust substring if needed)"
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

    // Contents required:
    // - A GPS log CSV for invalid coordinate (e.g., latitude > 90 or longitude > 180)
    //   referenced by some placeholder such as INVALID_COORD_INPUT (replace below).
    //   The rest of the records should be valid and match .
    // -  matching all valid locations.
    // Expected behavior:
    // - Processing stops when the invalid coordinate is encountered.
    // - An error message about invalid coordinates appears in stderr or stdout.
    // - OUTPUT_FOR_INVALID_DATA is NOT created or is left unchanged.
    private static void testInvalidGpsCoordinate() throws Exception {
        String INVALID_COORD_INPUT = "PATH/TO/INVALID_COORDINATE_LOG.csv";

        deleteIfExists(OUTPUT_FOR_INVALID_DATA);

        RunResult result = runHiChan(
                INVALID_COORD_INPUT,
                OUTPUT_FOR_INVALID_DATA
        );

        // Adjust error text as needed to match HiChan's messages.
        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "invalid",
                "Invalid coordinate test should emit an error mentioning 'invalid' (adjust substring if needed)"
        );

        assertFileExists(
                OUTPUT_FOR_INVALID_DATA,
                false,
                "Invalid coordinate test should not create or modify the output file"
        );
    }

    // Contents required:
    // - A GPS log CSV that is valid except for at least one record with speed > 90 m/s.
    //   Reference this by some placeholder such as INVALID_SPEED_INPUT (replace below).
    // -  matching all valid locations.
    // Expected behavior:
    // - Processing stops when the high-speed record is encountered.
    // - An error message about speed above allowed maximum appears.
    // - OUTPUT_FOR_INVALID_DATA is NOT created or is left unchanged.
    private static void testInvalidSpeed() throws Exception {
        String INVALID_SPEED_INPUT = "PATH/TO/INVALID_SPEED_LOG.csv";

        deleteIfExists(OUTPUT_FOR_INVALID_DATA);

        RunResult result = runHiChan(
                INVALID_SPEED_INPUT,
                OUTPUT_FOR_INVALID_DATA
        );

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "speed",
                "Invalid speed test should emit an error mentioning 'speed' (adjust substring if needed)"
        );

        assertFileExists(
                OUTPUT_FOR_INVALID_DATA,
                false,
                "Invalid speed test should not create or modify the output file"
        );
    }

    // Contents required:
    // - A GPS log CSV with at least one malformed row (e.g., missing fields, wrong delimiter,
    //   non-numeric where a number is expected), referenced by a placeholder such as
    //   MALFORMED_ROW_INPUT (replace below). All other rows should be valid.
    // -  matching all valid locations.
    // Expected behavior:
    // - Processing stops at the malformed row.
    // - An error message about malformed input / parse failure appears.
    // - OUTPUT_FOR_INVALID_DATA is NOT created or is left unchanged.
    private static void testMalformedRow() throws Exception {
        String MALFORMED_ROW_INPUT = "PATH/TO/MALFORMED_ROW_LOG.csv";

        deleteIfExists(OUTPUT_FOR_INVALID_DATA);

        RunResult result = runHiChan(
                MALFORMED_ROW_INPUT,
                OUTPUT_FOR_INVALID_DATA
        );

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "malformed",
                "Malformed row test should emit an error mentioning 'malformed' (adjust substring if needed)"
        );

        assertFileExists(
                OUTPUT_FOR_INVALID_DATA,
                false,
                "Malformed row test should not create or modify the output file"
        );
    }


    // Expected behavior:
    // - An error about missing highway match or unmatched location appears.
    // - Processing fails; OUTPUT_FOR_MISSING_HWY is NOT produced.
    private static void testMissingHighwayMatch() throws Exception {
        String MISSING_HWY_INPUT = "C:\\Users\\aaliy\\OneDrive\\Documents\\School\\Software dev class\\Rust-eze\\test_csv\\AcceptanceTest_I80EBSacramento.csv";

        deleteIfExists(OUTPUT_FOR_MISSING_HWY);

        RunResult result = runHiChan(
                MISSING_HWY_INPUT,
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

    // Contents required:
    // - The GPS log input path passed into HiChan must NOT exist at all
    //   (e.g., a filename that does not exist in the filesystem).
    // -  may be valid or also missing, but the main point is that at least
    //   one required input file is absent.
    // Expected behavior:
    // - A file-access / file-not-found error appears.
    // - OUTPUT_FOR_MISSING_INPUT is NOT produced.
    private static void testMissingInputFile() throws Exception {
        String NONEXISTENT_INPUT = "PATH/TO/THIS_FILE_DOES_NOT_EXIST.csv";

        deleteIfExists(OUTPUT_FOR_MISSING_INPUT);

        RunResult result = runHiChan(
                NONEXISTENT_INPUT,
                OUTPUT_FOR_MISSING_INPUT
        );

        String combinedOutput = result.stdout + "\n" + result.stderr;
        assertContains(
                combinedOutput,
                "file",
                "Missing input file test should emit a file-access error (adjust substring if needed)"
        );

        assertFileExists(
                OUTPUT_FOR_MISSING_INPUT,
                false,
                "Missing input file test should not create or modify the output file"
        );
    }

    // Contents required:
    // - PRESERVATION_INPUT_GPS_LOG: A valid GPS log CSV for a successful run.
    // - PRESERVATION_INPUT_HIGHWAY_REF: A valid highway reference CSV matching all locations.
    // - Both files should be in a known state before the test (no one else modifies them during the run).
    // Expected behavior:
    // - HiChan completes successfully and produces OUTPUT_FOR_INPUT_PRESERVATION.
    // - The byte contents of PRESERVATION_INPUT_GPS_LOG and PRESERVATION_INPUT_HIGHWAY_REF
    //   are *unchanged* before vs. after the run.
    private static void testInputPreservation() throws Exception {
        deleteIfExists(OUTPUT_FOR_INPUT_PRESERVATION);

        byte[] beforeGps = snapshotFileBytes(PRESERVATION_INPUT_GPS_LOG);
        byte[] beforeRef = snapshotFileBytes(PRESERVATION_INPUT_HIGHWAY_REF);

        if (beforeGps == null || beforeRef == null) {
            throw new AssertionError("Input preservation test requires both input files to exist before running.");
        }

        RunResult result = runHiChan(
                PRESERVATION_INPUT_GPS_LOG,
                PRESERVATION_INPUT_HIGHWAY_REF,
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

        byte[] afterGps = snapshotFileBytes(PRESERVATION_INPUT_GPS_LOG);
        byte[] afterRef = snapshotFileBytes(PRESERVATION_INPUT_HIGHWAY_REF);

        if (!java.util.Arrays.equals(beforeGps, afterGps)) {
            throw new AssertionError("GPS input file was modified during processing.");
        }
        if (!java.util.Arrays.equals(beforeRef, afterRef)) {
            throw new AssertionError("Highway reference input file was modified during processing.");
        }
    }
}