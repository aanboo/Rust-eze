/*HiChan Utility*/


import java.util.HashMap;
public class HiChan {
    public static void main(String[] args){
        System.out.println("Hello welcome to the HiChan utility!");
        HashMap<String,String[]> preproCSV = new HashMap<>();
        preproCSV = Read_GPS_Log(args[0]);
        System.out.println(preproCSV);
    }

    public static HashMap<String, String[]> Read_GPS_Log(String arg1){
        HashMap<String,String[]> milemarkers = new HashMap<>();
        System.out.println("Running Read_Gps_Log input is: " + arg1);
        //correctly open file, then read first/sec line, then for loop it

        String line = "c2kz3 47.709889 -116.99399 ID 0 6.825 10";
        String[] fields = line.split(",");

        milemarkers.put(fields[0], fields);
        return milemarkers;
    }
}

