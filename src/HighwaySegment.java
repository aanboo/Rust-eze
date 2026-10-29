public class HighwaySegment {
    private final String geohash;
    private final double lat;
    private final double lon;
    private final String sign1;
    private final String stFIPS;
    private final double begMP;
    private final double endMP;
    private final int channel;

    public HighwaySegment(String geohash, double lat, double lon,
                          String sign1, String stFIPS, double begMP,
                          double endMP, int channel) {
        this.geohash = geohash;
        this.lat = lat;
        this.lon = lon;
        this.sign1 = sign1;
        this.stFIPS = stFIPS;
        this.begMP = begMP;
        this.endMP = endMP;
        this.channel = channel;
    }

    public String getGeohash() {
        return geohash;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getSign1() {
        return sign1;
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
}