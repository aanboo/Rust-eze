
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//Should the program get the highway channel data filepath from the user as well?
public class HiChan {
    public static void main(String[] args) {
        System.out.println("Hello welcome to the HiChan utility!");
        // Check if the file path is provided in args
        if (args.length != 1) {
            System.out.println("Error: Please provide the GPS log file path and the preprocessed CSV as an argument.");
            return;
        }
        // Create a list to store GPSLogEntry objects
        List<GPSLogEntry> gpsLogEntries = new ArrayList<>();
        Map<String, List<HighwaySegment>> highwaySegments = new HashMap();
    }
}

//Testing for opening and printing the contents of the given preprocessed csv and gps log.
/*try {
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
    //Opening the preprocessed CSV
        try {
            // Call readPreprocessedCsv from InputProcessing and store the result in highwaySegments
            highwaySegments = InputProcessing.readPreprocessedCsv(args[1]);

            // Print the list to verify the entries
            System.out.println("Preproccessed CSV Highway Segment Entries:");
            for (Map.Entry<String, List<HighwaySegment>> entry : highwaySegments.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }
        } catch (IOException e) {
            // Handle file-related errors
            System.out.println("Error reading the preproccessed CSV file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle parsing-related errors
            System.out.println("Error parsing the preproccessed CSV file: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }
    */

