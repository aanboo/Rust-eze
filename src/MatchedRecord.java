//The raw GPS and matched highway data are grouped
public class MatchedRecord {
    private final GPSLogEntry gpsEntry;
    private final HighwaySegment segment;

    public MatchedRecord(GPSLogEntry gpsEntry, HighwaySegment segment) {
        this.gpsEntry = gpsEntry;
        this.segment = segment;
    }

    public GPSLogEntry getGpsEntry() {
        return gpsEntry;
    }

    public HighwaySegment getSegment() {
        return segment;
    }

    public boolean hasMatch() {
        return segment != null;
    }

    public int getChannel() {
        if (segment != null) {
            return segment.getChannel();
        }
        return 0; // Standard default: Channel 0 indicates unassigned/no match
    }

    public String getState() {
        if (segment != null) {
            return segment.getSateCode();
        }
        return ""; // Outputs empty field in CSV rather than arbitrary placeholder text
    }
    public String toString(){
        return gpsEntry + " " + segment;
    }
}