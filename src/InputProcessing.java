import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Component 3.1: Input Processing
public class InputProcessing {
    public InputProcessing(){}
    public void fileExists(String filePath) throws IOException {
        if(filePath == null || filePath.trim().isEmpty()){
            throw new IOException("Error - File cannot be opened");
        }

        File file = new File(filePath.trim());
        if(!file.exists() || !file.isFile() || !file.canRead()){
            throw new IOException("Error - File cannot be opened");
        }
    }

    public Map<String, List<HighwaySegment>> readPreprocessedCsv(String filePath) throws IOException{
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
                //6 should probably be turned into a global int because it will change once we get rid of
                // the unecessary columns in the CSV.
                if(tokens.length < 6){
                    throw new IllegalArgumentException("Error - Records cannot be parsed");
                }

                try{
                    String geohash = tokens[0].trim();
                    double lat = Double.parseDouble(tokens[1].trim());
                    double lon = Double.parseDouble(tokens[2].trim());
                    String sign1 = "I-90";
                    String stFIPS = tokens[3].trim();
                    double begMP = Double.parseDouble(tokens[5].trim());
                    double endMP = Double.parseDouble(tokens[6].trim());
                    int channel;
                    //7 should probably be turned into a global int because it will change once we get rid of
                    // the unecessary columns in the CSV. Intellij throws an error that this will always evaluate to true
                    if(tokens.length >= 7) {
                        channel = Integer.parseInt(tokens[6].trim());
                    }else{
                        channel = (int) Math.ceil(endMP / 10.0) * 10;
                    }

                    HighwaySegment segment = new HighwaySegment(geohash, lat, lon, sign1, stFIPS, begMP, endMP, channel);
                    highwayIndex.computeIfAbsent(geohash, k -> new ArrayList<>()).add(segment);
                } catch (NumberFormatException e){
                    throw new IllegalArgumentException("Error - Records cannot be parsed");
                }
            }
        } catch (IOException e){
            throw new IOException("Error - Records cannot be read");
        }

        return highwayIndex;
    }


}
