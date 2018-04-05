
import com.google.maps.model.LatLng;

import java.util.ArrayList;

public class OrganizationFilterModel {

    private Double latitude;
    private Double longitude;
    private int distanceMiles = -1;
    private String title;
    private String location;

    public OrganizationFilterModel(String location, int distanceMiles) {
        this.distanceMiles = distanceMiles;
        this.location = location;
    }

    public OrganizationFilterModel(String location, int distanceMiles, String title){
        this(location,distanceMiles);
        this.title = title;
    }

    public OrganizationFilterModel(String title){
        this.title = title;
    }

    public OrganizationFilterModel(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public OrganizationFilterModel(double latitude, double longitude, String title) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }



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


        if(latitude == null || longitude == null){
            fields = "org_id, name, description, org_email, `org_ contact`, org_img, org_location";
        }
        else {
            fields = "org_id, name, description, org_email, `org_ contact`, org_img, org_location, sqrt(pow(latitude - " + latitude + ",2) + " +
                    "pow(longitude - " + longitude + ",2)) as distance";
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
        String query = "SELECT org_id, name, description, org_email, `org_ contact`, org_img, org_location FROM ( SELECT "+fields+ " FROM organization "+where+") AS T ORDER BY distance asc ;" ;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
