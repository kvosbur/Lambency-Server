
import java.sql.Timestamp;

public class EventFilterModel {

    private double latitude;
    private double longitude;
    private Timestamp startStamp;
    private Timestamp endStamp;

    public EventFilterModel(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public EventFilterModel(double latitude, double longitude, Timestamp startStamp, Timestamp endStamp){
        this(latitude,longitude);
        this.startStamp = startStamp;
        this.endStamp = endStamp;
    }


    public String createQuery(){
        String fields = "event_id, sqrt(pow(latitude - " + latitude + ",2) + " +
                "pow(longitude - " + longitude + ",2)) as distance";






        String query = "SELECT " + fields + " FROM events order by distance asc";

        return query;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Timestamp getStartStamp() {
        return startStamp;
    }

    public void setStartStamp(Timestamp startStamp) {
        this.startStamp = startStamp;
    }

    public Timestamp getEndStamp() {
        return endStamp;
    }

    public void setEndStamp(Timestamp endStamp) {
        this.endStamp = endStamp;
    }
}
