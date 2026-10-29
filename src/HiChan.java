import java.io.File;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.util.List;
import java.util.Map;

/*HiChan Utility*/

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Arrays;
public static Integer NUMBER_OF_CSV_FIELDS = 7;
void main(String[] args){
        HashMap <String, String[]> preproCSV = Read_Preproc_CSV(args[0]);
        System.out.println(Arrays.toString(preproCSV.get("c27uw")));
    }
/*This function takes in the name of the csv file and parses it into a HashMap using the geohash as the key.
* Ir returns the created hashmap with all of the values.*/
public static HashMap<String, String[]> Read_Preproc_CSV(String arg1) throws Exception{
    HashMap<String,String[]> preproc_csv = new HashMap<>();
    Path pathToCSV = Path.of("src/"+arg1);
    try(BufferedReader reader = Files.newBufferedReader(pathToCSV)){
        String line;
        while((line = reader.readLine()) != null){
            String[] fields = line.split(",");
            if(fields.length != NUMBER_OF_CSV_FIELDS){
                throw new Exception("CSV file has invalid row length");

            }
            preproc_csv.put(fields[0], fields);
        }
    } catch(IOException e){
        System.out.println("Error – File cannot be opened: " + e.getMessage());
    }
    return preproc_csv;
}

