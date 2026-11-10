import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;


//This should append whatever the channelID is for each GPSLogEntry in the GPSLog_1.csv.
public class OutputGeneration {

    public static void writeOutput(List<MatchedRecord> matchedRecords, String outputFile) throws IOException {
        String result = writeOutput(matchedRecords, outputFile, false);
        if ("Output File cannot be written to".equals(result)) {
            throw new IllegalArgumentException("Output File cannot be written to");
        } else if ("Error Overwrite Authorization is not provided".equals(result)) {
            throw new IllegalArgumentException("Error Overwrite Authorization is not provided");
        }
    }

    public static String writeOutput(
            List<MatchedRecord> matchedRecords,
            String outputFile,
            boolean overwriteAuthorized) {


        if (matchedRecords == null || outputFile == null || outputFile.trim().isEmpty()) {
            return "Output File cannot be written to";
        }

        try {
            Path outputPath = Paths.get(outputFile);
            if (Files.exists(outputPath) && !overwriteAuthorized) {
                return "Error Overwrite Authorization is not provided";
            }


            try (BufferedWriter writer = Files.newBufferedWriter(
                    outputPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {


                writer.write(
                        "date time,latitude,longitude," +
                                "altitude(m),speed(m/s),sat_used,ChannelID");
                writer.newLine();

                for (MatchedRecord record : matchedRecords) {
                    GPSLogEntry gps = record.getGpsEntry();
                    String channelID = "UNKNOWN";

                    if (record.hasMatch()) {
                        channelID = record.getChannelID();
                    }

                    if (channelID == null || channelID.trim().isEmpty()) {
                        channelID = "UNKNOWN";
                    }

                    writer.write(
                            gps.getOriginalCsvLine() + "," + channelID);
                    writer.newLine();
                }
            }

            System.out.println("Ouput generated successfully");
            System.out.println("Output file: " + outputFile);
            return "SUCCESS";
        } catch (IOException | RuntimeException e) {
            return "Output File cannot be written to";
        }
    }
}