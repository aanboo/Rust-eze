public class GPSLogEntry {
    private final String date_time;
    private final double lat;
    private final double lon;
    private final int altitude;
    private final int speed;
    private final int sat_used;

    public GPSLogEntry(String date_time, double lat, double lon,
                          int altitude, int speed, int sat_used) {
        this.date_time = date_time;
        this.lat = lat;
        this.lon = lon;
        this.altitude = altitude;
        this.speed = speed;
        this.sat_used = sat_used;
    }

    public String getDateTime() {
        return date_time;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getAltitude() {
        return altitude;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSatUsed() {return sat_used;}
}
