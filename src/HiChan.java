
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiChan {
     public void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Error: Please provide the GPS log file path and the preprocessed CSV as an argument.");
            return;
        }

         String gpsPath = args[0];
         String outputPath = args[1];

         List<GPSLogEntry> gpsLogEntries = new ArrayList<>();
        Map<String, List<HighwaySegment>> highwaySegments = new HashMap<>();
        //Filling the GPS log and highway segments
        try {
            // Call readGPSLog from InputProcessing and store the result in gpsLogEntries
            gpsLogEntries = InputProcessing.readGPSLog(gpsPath);
        } catch (IOException e) {
            // Handle file-related errors
            System.out.println(e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            // Handle parsing-related errors
            System.out.println(e.getMessage());
            return;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.out.println(e.getMessage());
            return;
        }
        //Opening the preprocessed CSV
         String preprocPath = "src/i90_channels.csv";
         if (new File("i90_channels.csv").exists()) {
             preprocPath = "i90_channels.csv";
         }

        try {
            highwaySegments = InputProcessing.readPreprocessedCsv(preprocPath);
        } catch (IOException e) {
            // Handle file-related errors
            System.out.println(e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            // Handle parsing-related errors
            System.out.println(e.getMessage());
            return;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.out.println(e.getMessage());
            return;
        }

        List<MatchedRecord> matchedRecords = new ArrayList<>();
        try {
           matchedRecords = HighwayMatching.matchRecords(gpsLogEntries, highwaySegments);

            for (MatchedRecord match : matchedRecords) {
                System.out.println(match);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            return;
        }

        try{//Should we get overwrite authorization from the user or just overwrite it for now?
            boolean authorize = true; //yes, I believe that was the original intent we wanted
            File out = new File(outputPath);

            if (out.exists()) {
                System.out.print("Output file already exists. Overwrite this file? (y/n): ");
                java.util.Scanner scanner = new java.util.Scanner(System.in);
                String response = scanner.nextLine().trim();
                if (!response.equalsIgnoreCase("y")) {
                    authorize = false;
                }
            }

            String result = OutputGeneration.writeOutput(matchedRecords, outputPath, authorize);
            if (!"SUCCESS".equals(result)) {
                System.out.println(result);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}



