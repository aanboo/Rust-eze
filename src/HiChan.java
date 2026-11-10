
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//Should the program get the highway channel data filepath from the user as well?
public class HiChan {
     static void main(String[] args) {
        // Check if the file path is provided in args
        if (args.length != 2) {
            System.out.println("Error: Please provide the GPS log file path and the preprocessed CSV as an argument.");
            return;
        }

        List<GPSLogEntry> gpsLogEntries = new ArrayList<>();
        Map<String, List<HighwaySegment>> highwaySegments = new HashMap();
        //Filling the GPS log and highway segments
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
            System.out.println("Error reading the preprocessed CSV file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle parsing-related errors
            System.out.println("Error parsing the preprocessed CSV file: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
        List<MatchedRecord> matchedRecords = new ArrayList<MatchedRecord>();
        try {
           matchedRecords = HighwayMatching.matchRecords(gpsLogEntries, highwaySegments);

            for (MatchedRecord match : matchedRecords) {
                System.out.println(match);
            }
        }catch(Exception e){
            System.out.println("Error matching segment: " + e.getMessage());
        }

        try{//Should we get overwrite authorization from the user or just overwrite it for now?
            OutputGeneration.writeOutput(matchedRecords,args[1]);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}



