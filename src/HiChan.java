
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//
//Should the program get the highway channel data filepath from the user as well?
public class HiChan {
    public static void main(String[] args) {
        // Check if the file path is provided in args
        if (args.length != 1) {
            System.out.println("Error: Please provide the GPS log file path and the preprocessed CSV as an argument.");
            return;
        }

        List<GPSLogEntry> gpsLogEntries = new ArrayList<>();
        Map<String, List<HighwaySegment>> highwaySegments = new HashMap();
        //Filling the GPS log and highway segements
        try {
            // Call readGPSLog from InputProcessing and store the result in gpsLogEntries
            gpsLogEntries = InputProcessing.readGPSLog(args[0]);
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
            highwaySegments = InputProcessing.readPreprocessedCsv("src/i90_channels.csv");
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


        List<MatchedRecord> matchedRecords = HighwayMatching.matchRecords(gpsLogEntries, highwaySegments);
        for(MatchedRecord match:matchedRecords){
            System.out.println(match);
        }

    }
}



