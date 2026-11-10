import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//
//Component 3.1: Input Processing
public class InputProcessing {
    public static final int HIGHWAY_EXPECTED_COLUMNS = 5;
    public static final int GPS_EXPECTED_COLUMNS = 6;
    public static final int GEOHASH_PRECISION = 5;

    public InputProcessing(){}
    public static void fileExists(String filePath) throws IOException {

        if(filePath == null || filePath.trim().isEmpty()){
            throw new IOException("Error - File cannot be opened");
        }

        File file = new File(filePath.trim());
        if(!file.exists() || !file.isFile() || !file.canRead()){
            throw new IOException("Error - File cannot be opened");
        }
    }

    public static Map<String, List<HighwaySegment>> readPreprocessedCsv(String filePath) throws IOException{
        fileExists(filePath);
        Map<String, List<HighwaySegment>> highwayIndex = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath.trim()))){
            String header = reader.readLine();
            if(header == null){
                throw new IOException("Error - Records cannot be read");
            }

            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.isEmpty()){
                    continue;
                }

                String[] tokens = line.split(",");
                if(tokens.length != HIGHWAY_EXPECTED_COLUMNS){
                    throw new IllegalArgumentException("Error - Invalid Format");
                }

                try{
                    String geohash = tokens[0].trim();
                    String stFIPS = tokens[1].trim();
                    Double begMP = Double.parseDouble(tokens[2].trim());
                    Double endMP = Double.parseDouble(tokens[3].trim());
                    int channel = Integer.parseInt(tokens[4].trim());
                    if (geohash.length() != GEOHASH_PRECISION) {
                        throw new IllegalArgumentException("Error - Invalid Format");
                    }
                    HighwaySegment segment = new HighwaySegment(geohash, stFIPS, begMP, endMP, channel);
                    if (!highwayIndex.containsKey(geohash)) {
                        highwayIndex.put(geohash, new ArrayList<>());
                    }
                    highwayIndex.get(geohash).add(segment);
                } catch (NumberFormatException e){
                    throw new IllegalArgumentException("Error - Invalid Format",e );
                }
            }
        } catch (IOException e){
            throw new IOException("Error - Records cannot be read", e);
        }

        return highwayIndex;
    }

    //date time,latitude,longitude,altitude(m),speed(m/s),sat_used
    public static List<GPSLogEntry> readGPSLog(String filePath) throws IOException {
        fileExists(filePath);
        List<GPSLogEntry> logEntries = new ArrayList<>();
        Set<String> seenDateTimes = new HashSet<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath.trim()))){
            String header = reader.readLine();
            if(header == null){
                throw new IOException("Error - Records cannot be read");
            }

            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.isEmpty()){
                    continue;
                }

                String[] tokens = line.split(",", -1);

                if(tokens.length != GPS_EXPECTED_COLUMNS){
                    throw new IllegalArgumentException("Error - Invalid Format");
                }

                try{
                    String date_time = tokens[0].trim();
                    double lat = Double.parseDouble(tokens[1].trim());
                    double lon = Double.parseDouble(tokens[2].trim());
                    double altitude = Double.parseDouble(tokens[3].trim());
                    double speed = Double.parseDouble(tokens[4].trim());
                    int sat_used = Integer.parseInt(tokens[5].trim());

                    // Check for duplicate entries (by key,timestamp) and remove them
                    if (!seenDateTimes.add(date_time)) {
                        // Print error to screen but do not throw an exception
                        System.err.println("Error - Duplicate timestamp entry detected for: " + date_time);
                        // Skip adding this duplicate entry so it never reaches any HashMap conversion later
                        continue;
                    }

                    if(date_time.isEmpty() ){
                        throw new IllegalArgumentException("Error - Records cannot be parsed");
                    }
                    if(lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0){
                        throw new IllegalArgumentException("Error - Invalid GPS Coordinates");
                    }
                    if(speed < 0 || speed > 90){
                        throw new IllegalArgumentException("Error - Invalid Speed");
                    }

                    logEntries.add(new GPSLogEntry(date_time, lat, lon, altitude,speed,sat_used));

                } catch (NumberFormatException e){
                    throw new IllegalArgumentException("Error - Records cannot be parsed", e);
                }
            }
        } catch (IOException e){
            throw new IOException("Error - Records cannot be read",e);
        }

        return logEntries;
    }
}
