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
                    throw new IllegalArgumentException("Error - Invalid Column length");
                }

                try{
                    String geohash = tokens[0].trim();
                    String stFIPS = tokens[1].trim();
                    double begMP = Double.parseDouble(tokens[2].trim());
                    double endMP = Double.parseDouble(tokens[3].trim());
                    int channel = (int) Math.ceil(endMP / 10.0) * 10;

                    HighwaySegment segment = new HighwaySegment(geohash, stFIPS, begMP, endMP, channel);
                    if (!highwayIndex.containsKey(geohash)) {
                        highwayIndex.put(geohash, new ArrayList<>());
                    }
                    highwayIndex.get(geohash).add(segment);
                } catch (NumberFormatException e){
                    throw new IllegalArgumentException("Error - Records cannot be parsed");
                }
            }
        } catch (IOException e){
            throw new IOException("Error - Records cannot be read");
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
                String originalLine = line;
                line = line.trim();
                if(line.isEmpty()){
                    continue;
                }

                String[] tokens = line.split(",");

                if(tokens.length != GPS_EXPECTED_COLUMNS){
                    throw new IllegalArgumentException("Error - Records cannot be parsed");
                }

                try{
                    String date_time = tokens[0].trim();
                    double lat = Double.parseDouble(tokens[1].trim());
                    double lon = Double.parseDouble(tokens[2].trim());
                    double altitude = Double.parseDouble(tokens[3].trim());
                    double speed = Double.parseDouble(tokens[4].trim());
                    double sat_used = Double.parseDouble(tokens[5].trim());

                    if(date_time.isEmpty() ){
                        throw new IllegalArgumentException("Error - Records cannot be parsed");
                    }
                    if(lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0){
                        throw new IllegalArgumentException("Invalid GPS Coordinates");
                    }
                    if(speed < 0 || speed > 120){
                        throw new IllegalArgumentException("Invalid Speed");
                    }

                    GPSLogEntry entry = new GPSLogEntry(date_time, lat, lon, altitude,speed,sat_used);
                    entry.setOriginalCsvLine(originalLine);
                    logEntries.add(entry);
                    //logEntries.add(new GPSLogEntry(date_time, lat, lon, altitude,speed,sat_used));

                } catch (NumberFormatException e){
                    throw new IllegalArgumentException("Error - Records cannot be parsed");
                }
            }
        } catch (IOException e){
            throw new IOException("Error - Records cannot be read");
        }

        return logEntries;
    }
}
