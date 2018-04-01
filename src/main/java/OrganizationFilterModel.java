
import com.google.maps.model.LatLng;
import java.util.ArrayList;

public class OrganizationFilterModel {

    private double latitude;
    private double longitude;
    private int distanceMiles = -1;
    private String title;
    private String location;


    public String createStringQuery(){
        if(location != null){
            Printing.println("Filter organization from other location");
            LatLng loc = GoogleGeoCodeUtil.getGeoData(location);
            latitude = loc.lat;
            longitude = loc.lng;
        }
        String fields;
        ArrayList<String> ands = new ArrayList<>();
        String where = "";



        fields = "org_id, sqrt(pow(latitude - " + latitude + ",2) + " +
                "pow(longitude - " + longitude + ",2)) as distance";
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
        String query = "SELECT org_id "+ " FROM ( SELECT "+fields+ " FROM organization "+where+") AS T ORDER BY distance asc ;" ;
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

    public int getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(int distanceMiles) {
        this.distanceMiles = distanceMiles;
    }
}
