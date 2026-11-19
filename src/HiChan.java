import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryPoolMXBean;

public class  HiChan {
    public static void main(String[] args)throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);



        if (args.length < 2 || args.length > 3) {
            System.out.println("Error: Please provide the GPS log file path and the preprocessed CSV as an argument.");
            return;
        }

        String gpsPath = args[0];
        String preprocPath = args[1];
        String outputPath = (args.length == 3)
                ? args[2]
                : System.getProperty("hichan.output", "src/HiChan_Output.csv");

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
                //System.out.println(match);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            return;
        }

        try{
            boolean authorize = true;
            File out = new File(outputPath);
            String response = "";
            if (out.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(System.in);
                do {
                    System.out.print("Output file already exists. Overwrite this file? (y/n): ");
                    if (!scanner.hasNextLine()) {
                        authorize = false;
                        break;
                    }
                    response = scanner.nextLine().trim();
                    if(!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("n"))
                    {
                        System.out.println("Error - invalid input enter (y/n): ");
                    } else if (!response.equalsIgnoreCase("y")) {
                        authorize = false;
                    }
                }while(!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("n"));
            }

            String result = OutputGeneration.writeOutput(matchedRecords, outputPath, authorize);
            if (!"SUCCESS".equals(result)) {
                System.out.println(result);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        long usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
        double usedMemoryMB = usedMemoryBytes / (1024.0 * 1024.0);

        // 4. Get CPU Utilization (returns 0.0 to 1.0, or negative if not yet sampled)
        double processCpuLoad = osBean.getProcessCpuLoad() * 100.0;

        System.out.printf("Memory Consumed: %.2f MB%n", usedMemoryMB);
        System.out.printf("Process CPU Utilization: %.2f%%%n", processCpuLoad);


        long peakHeapBytes = 0;
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP) {
                peakHeapBytes += pool.getPeakUsage().getUsed();
            }
        }

        double peakHeapMB = peakHeapBytes / (1024.0 * 1024.0);
        System.out.printf("Peak Heap Memory: %.2f MB%n", peakHeapMB);

    }
}