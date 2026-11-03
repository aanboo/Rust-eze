
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HiChan {
    public static void main(String[] args) {
        System.out.println("Hello welcome to the HiChan utility!");
        // Check if the file path is provided in args
        if (args.length == 0) {
            System.out.println("Error: Please provide the GPS log file path as an argument.");
            return;
        }
        // Create a list to store GPSLogEntry objects
        List<GPSLogEntry> gpsLogEntries = new ArrayList<>();

        try {
            // Call readGPSLog from InputProcessing and store the result in gpsLogEntries
            gpsLogEntries = InputProcessing.readGPSLog(args[0]);

            // Print the list to verify the entries
            System.out.println("GPS Log Entries:");
            for (GPSLogEntry entry : gpsLogEntries) {
                System.out.println(entry);
            }
        } catch (IOException e) {
            // Handle file-related errors
            System.out.println("Error reading the GPS log file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle parsing-related errors
            System.out.println("Error parsing the GPS log file: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}




