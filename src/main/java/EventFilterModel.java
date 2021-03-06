
import com.google.maps.model.LatLng;

import java.sql.Timestamp;
import java.util.ArrayList;

public class EventFilterModel {

    private double latitude;
    private double longitude;
    private Timestamp startStamp;
    private Timestamp endStamp;
    private int distanceMiles = -1;
    private String title;
    private String location;

    public EventFilterModel(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public EventFilterModel(double latitude, double longitude, Timestamp endStamp){
        this(latitude,longitude);
        this.endStamp = endStamp;
    }
    public EventFilterModel( Timestamp startStamp, double latitude, double longitude){
        this(latitude,longitude);
        this.startStamp = startStamp;
    }

    public EventFilterModel(double latitude, double longitude, Timestamp startStamp, Timestamp endStamp){
        this(latitude,longitude);
        this.startStamp = startStamp;
        this.endStamp = endStamp;
    }

    public EventFilterModel(double latitude, double longitude, Timestamp startStamp, Timestamp endStamp, int distanceMiles){
        this(latitude,longitude, startStamp, endStamp);
        this.distanceMiles = distanceMiles;
    }

    public EventFilterModel(double latitude, double longitude, int distanceMiles){
        this(latitude,longitude);
        this.distanceMiles = distanceMiles;
    }


    public String createStringQuery(){
        if(location != null){
            Printing.println("Filter events from other location");
            LatLng loc = GoogleGeoCodeUtil.getGeoData(location);
            latitude = loc.lat;
            longitude = loc.lng;
        }
        String fields;
        ArrayList<String> ands = new ArrayList<>();
        String where = "";



        fields = "event_id, sqrt(pow(latitude - " + latitude + ",2) + " +
                "pow(longitude - " + longitude + ",2)) as distance";
        if(startStamp != null){
            Printing.println(startStamp);
            ands.add("end_time > '"+startStamp.toString()+"'");
        }
        if(endStamp != null){
            Printing.println(endStamp);
            ands.add("start_time < '"+endStamp.toString()+"'");
        }
        if(distanceMiles != -1){
            ands.add("(sqrt(pow(latitude - " + latitude + ",2) + " +
                    "pow(longitude - " + longitude + ",2)) * 69)  <= "+ distanceMiles +"");
        }
        if(title != null && ! title.equals("")){
            ands.add("name LIKE \'"+title+"%\'");
        }


        if(ands.size() != 0){
            where = "WHERE ";
            for(int i = 0; i < ands.size(); i++) {
                if(i == 0){
                    where += ands.get(i)+" ";
                }
                else{
                    where += "AND " + ands.get(i) + " ";
                }
            }
        }
        //SELECT +"+fields+" FROM Events WHERE start_time > ?
        //SELECT "+fields+ " FROM events "+where+"
        String query = "SELECT event_id "+ " FROM ( SELECT "+fields+ " FROM events "+where+") AS T ORDER BY distance asc ;" ;
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

    public int getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(int distanceMiles) {
        this.distanceMiles = distanceMiles;
    }
}
