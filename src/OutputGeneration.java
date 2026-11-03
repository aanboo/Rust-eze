    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.List;


//This should append whatever the channelID is for each GPSLogEntry in the GPSLog_1.csv.
    public class OutputGeneration {

        public static void writeOutput(List<MatchedRecord> matchedRecords, String outputFile) throws IOException{
        if(matchedRecords == null){
            throw new IllegalArgumentException("Matched records is null");
        }

        if(outputFile == null || outputFile.trim().isEmpty()){
            throw new IllegalArgumentException("Output file is null or empty");
        }

        Path outputPath = Paths.get(outputFile);
        //This should append the channel ID's to the GPSLOG_1.csv so it should exist.
        if(Files.exists(outputPath)){
            throw new IllegalArgumentException("Output file already exists:" + outputFile);
        }

        int matched = 0;
        int unmatched = 0;

        try(BufferedWriter writer = Files.newBufferedWriter(outputPath)){
            writer.write(
                    "date time,latitude,longitude," +
                            "altitude(m),speed(m/s),sat_used,ChannelID");
            writer.newLine();

            for(MatchedRecord record : matchedRecords){
                GPSLogEntry gps = record.getGpsEntry();
                String channelID;
                if(record.hasMatch()){
                    channelID = String.valueOf(record.getChannel());
                    matched++;
                } else {
                    channelID = "UNKNOWN";
                    unmatched++;
                    System.err.println("Warning: Channel ID UNKNOWN at "
                            + "latitude " + gps.getLat()
                            + ", longitude " + gps.getLon()
                            + " , time " + gps.getDateTime());


                }
                writer.write(
                        gps.getDateTime() + ","
                        + gps.getLat() + ","
                        + gps.getLon() + ","
                        + gps.getAltitude() + ","
                        + gps.getSpeed() + ","
                        + gps.getSatUsed() + ","
                        + channelID);
                writer.newLine();

            }

        }

        System.out.println("Output generated successfully");
        System.out.println("Matched records: " + matched);
        System.out.println("Unmatched records: " + unmatched);
        System.out.println("Output file: " + outputFile);
            }
        }

