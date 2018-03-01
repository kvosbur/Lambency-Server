
import java.sql.Timestamp;
import java.util.ArrayList;

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


    public String createStringQuery(){
        String fields;
        ArrayList<String> ands = new ArrayList<>();
        String where = "";

        fields = "event_id, sqrt(pow(latitude - " + latitude + ",2) + " +
                "pow(longitude - " + longitude + ",2)) as distance";
        if(startStamp != null){
            ands.add("start_time >= "+startStamp.toString());
        }
        if(endStamp != null){
            ands.add("end_time <= "+endStamp.toString());
        }


        if(ands.size() != 0){
            where = "WHERE ";
            for(int i = 0; i < ands.size(); i++) {
                if(i == 0){
                    where = ands.get(i)+" ";
                }
                else{
                    where += "AND " + ands.get(i) + " ";
                }
            }
        }

        String query = "SELECT " + fields + " FROM events "+where+" ORDER BY distance asc ;" ;

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
