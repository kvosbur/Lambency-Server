import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;

public class GoogleGeoCodeUtil {


    public static LatLng getGeoData(String address){
        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBTmYB1b5wyp_jMEtCFuicmlfSA3kLKbMg")
                    .build();
            GeocodingResult[] results = GeocodingApi.geocode(context,
                    "1210 West East Branch Rd. Bloomington,IN").await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Geometry s = results[0].geometry;

            return s.location;
            //Printing.println(gson.toJson(results[0].addressComponents));
        }catch(Exception e){
            Printing.println(e.toString());
            return null;
        }
    }

    public static void main(String[] args) {
        GoogleGeoCodeUtil.getGeoData("address");
    }
}
