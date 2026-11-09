public class HighwaySegment {
    private final String geohash;
    private final String stFIPS;
    private final double begMP;
    private final double endMP;
    private final int channel;

    public HighwaySegment(String geohash, String stFIPS, double begMP,
                          double endMP, int channel) {
        this.geohash = geohash;
        this.stFIPS = stFIPS;
        this.begMP = begMP;
        this.endMP = endMP;
        this.channel = channel;
    }

    public String getGeohash() {
        return geohash;
    }

    public String getStFIPS() {
        return stFIPS;
    }

    public double getBegMP() {
        return begMP;
    }

    public double getEndMP() {
        return endMP;
    }

    public int getChannel() {
        return channel;
    }

    public String getSateCode() {
        if ("53".equals(stFIPS) || "WA".equalsIgnoreCase(stFIPS)) {
            return "WA";
        } else if ("16".equals(stFIPS) || "ID".equalsIgnoreCase(stFIPS)) {
            return "ID";
        }
        return stFIPS;
    }

    public String toString(){
        return geohash + " " + stFIPS + " " + begMP + " " + endMP + " " + channel;
    }
}