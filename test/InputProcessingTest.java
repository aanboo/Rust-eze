import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class InputProcessingTest {

    @org.junit.jupiter.api.Test
    void fileExists() throws IOException {
            Path tempFile = Files.createTempFile("test", ".csv");

            try {
                assertDoesNotThrow(() ->
                        InputProcessing.fileExists(tempFile.toString())
                );
            } finally {
                Files.deleteIfExists(tempFile);
            }

        //Tests passing in NULL
        assertThrows(IOException.class, () ->
                InputProcessing.fileExists(null)
        );
        //Test passing in empty string
        assertThrows(IOException.class, () ->
                InputProcessing.fileExists("")
        );
        //Tests if the given file does not exist
        assertThrows(IOException.class, () ->
                InputProcessing.fileExists("this_file_does_not_exist.csv")
        );
        //Tests passing in a directory
        Path tempDirectory = Files.createTempDirectory("test");
        try {
            assertThrows(IOException.class, () ->
                    InputProcessing.fileExists(tempDirectory.toString())
            );
        } finally {
            Files.deleteIfExists(tempDirectory);
        }
        }

    @org.junit.jupiter.api.Test
    void readPreprocessedCsv() {

    }

    @org.junit.jupiter.api.Test
    void readGPSLog() throws IOException {
        // Creates a valid CSV file
        Path tempFile = Files.createTempFile("gps_test", ".csv");

        try {
            Files.writeString(tempFile,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:17:15.000,47.71314203,-116.9488803,646.743,13.05,22\n"
            );

            // Tests reading a valid CSV file
            assertDoesNotThrow(() ->
                    InputProcessing.readGPSLog(tempFile.toString())
            );

            // Tests that a valid CSV produces the expected number of entries
            assertEquals(1,
                    InputProcessing.readGPSLog(tempFile.toString()).size()
            );

            // Tests passing in NULL
            assertThrows(IOException.class, () ->
                    InputProcessing.readGPSLog(null)
            );

            // Tests passing in an empty string
            assertThrows(IOException.class, () ->
                    InputProcessing.readGPSLog("")
            );

            // Tests if the given file does not exist
            assertThrows(IOException.class, () ->
                    InputProcessing.readGPSLog("this_file_does_not_exist.csv")
            );

        } finally {
            Files.deleteIfExists(tempFile);
        }

        // Tests an empty CSV file (no header)
        Path emptyFile = Files.createTempFile("gps_empty", ".csv");

        try {
            assertThrows(IOException.class, () ->
                    InputProcessing.readGPSLog(emptyFile.toString())
            );
        } finally {
            Files.deleteIfExists(emptyFile);
        }

        // Tests too few columns
        Path tooFewColumns = Files.createTempFile("gps_few", ".csv");

        try {
            Files.writeString(tooFewColumns,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:17:15.000,47.71314203,-116.9488803,646.743\n"
            );

            assertThrows(IllegalArgumentException.class, () ->
                    InputProcessing.readGPSLog(tooFewColumns.toString())
            );
        } finally {
            Files.deleteIfExists(tooFewColumns);
        }

        // Tests too many columns
        Path tooManyColumns = Files.createTempFile("gps_many", ".csv");

        try {
            Files.writeString(tooManyColumns,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:17:15.000,47.71314203,-116.9488803,646.743,13.05,22,extra\n"
            );

            assertThrows(IllegalArgumentException.class, () ->
                    InputProcessing.readGPSLog(tooManyColumns.toString())
            );
        } finally {
            Files.deleteIfExists(tooManyColumns);
        }

        // Tests invalid numerical values
        Path invalidNumber = Files.createTempFile("gps_number", ".csv");

        try {
            Files.writeString(invalidNumber,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:17:15.000,not_a_number,-116.9488803,646.743,13.05,22\n"
            );

            assertThrows(IllegalArgumentException.class, () ->
                    InputProcessing.readGPSLog(invalidNumber.toString())
            );
        } finally {
            Files.deleteIfExists(invalidNumber);
        }

        // Tests invalid GPS coordinates (latitude too high)
        Path invalidCoordinates = Files.createTempFile("gps_coordinates", ".csv");

        try {
            Files.writeString(invalidCoordinates,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:17:15.000,95.0,-116.9488803,646.743,13.05,22\n"
            );

            assertThrows(IllegalArgumentException.class, () ->
                    InputProcessing.readGPSLog(invalidCoordinates.toString())
            );
        } finally {
            Files.deleteIfExists(invalidCoordinates);
        }

        // Tests invalid speed (negative)
        Path invalidSpeed = Files.createTempFile("gps_speed", ".csv");

        try {
            Files.writeString(invalidSpeed,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:17:15.000,47.71314203,-116.9488803,646.743,-5.0,22\n"
            );

            assertThrows(IllegalArgumentException.class, () ->
                    InputProcessing.readGPSLog(invalidSpeed.toString())
            );
        } finally {
            Files.deleteIfExists(invalidSpeed);
        }

        // Tests duplicate timestamps
        Path duplicateFile = Files.createTempFile("gps_duplicate", ".csv");

        try {
            Files.writeString(duplicateFile,
                    "DateTime,Lat,Lon,Altitude,Speed,SatUsed\n" +
                            "2026-09-22 20:17:15.000,47.71314203,-116.9488803,646.743,13.05,22\n" +
                            "2026-09-22 20:17:15.000,47.71414203,-116.9498803,647.743,14.05,23\n"
            );

            assertEquals(2,
                    InputProcessing.readGPSLog(duplicateFile.toString()).size()
            );
        } finally {
            Files.deleteIfExists(duplicateFile);
        }
    }

}