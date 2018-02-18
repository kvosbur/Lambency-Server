import com.google.maps.model.LatLng;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventHandler {

    /**
     *
     * @param event   Event that is to be added to the database
     * @return  (int)   Will return event_id on success, -1 on failure
     */


    public static int createEvent(Event event) {

        try {
            //get latitude and longitude for database
            LatLng latlng = GoogleGeoCodeUtil.getGeoData(event.getLocation());
            int event_id = LambencyServer.dbc.createEvent(event.getOrg_id(),event.getName(),event.getStart(),
                    event.getEnd(),event.getDescription(),event.getLocation(),event.getImage_path(), latlng.lat, latlng.lng);
            return event_id;
        } catch (SQLException e) {
            System.out.println("Error in creating event: "+event.getName());
            return -1;
        }

    }


    public static int updateEvent(Event event) {
        try{
            LambencyServer.dbc.modifyEventInfo(event.getEvent_id(),event.getName(),event.getStart(),event.getEnd(),
                    event.getDescription(),event.getLocation(),event.getImage_path(),event.getLattitude(),event.getLongitude());
            return 1;
        }
        catch (SQLException e){
            System.out.println("Error in updating Event: "+event.getName());
            return 0;
        }
    }

    /** Call from the API to gather the events that are searched by location
     *
     * @param lattitude    double for lat
     * @param longitude    double for long
     * @return     List of events if successful, null otherwise
     */

    public static List<Event> getEventsByLocation(double lattitude, double longitude){
        // I am assuming that the odds of them being at exactly 0,0 (lat,long) is so microscopic that the only case that it would be 0 is if Double.parse(null) == 0
        if(lattitude == 0 || longitude == 0){
            return null;
        }
        List<Integer> eventIDs;
        List<Event> events = new ArrayList<>();
        try{
            eventIDs = LambencyServer.dbc.searchEventsByLocation(lattitude,longitude);
            for(Integer i: eventIDs){
                events.add(LambencyServer.dbc.searchEvents(i));
            }
        } catch (SQLException e) {
            System.out.println("Error in get events by location: "+e);
            return null;
        }

        return events;


    }



}
