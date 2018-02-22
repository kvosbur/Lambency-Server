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


    public static int createEvent(EventModel event) {

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


    public static int updateEvent(EventModel event) {
        try{
            LambencyServer.dbc.modifyEventInfo(event.getEvent_id(),event.getName(),event.getStart(),event.getEnd(),
                    event.getDescription(),event.getLocation(),event.getImage_path(),event.getLattitude(),event.getLongitude());
            return 0;
        }
        catch (SQLException e){
            System.out.println("Error in updating Event: "+event.getName());
            return 1;
        }
    }

    /** Call from the API to gather the events that are searched by location
     *
     * @param lattitude    double for lat
     * @param longitude    double for long
     * @return     List of events if successful, null otherwise
     */

    public static List<EventModel> getEventsByLocation(double lattitude, double longitude){
        // I am assuming that the odds of them being at exactly 0,0 (lat,long) is so microscopic that the only case that it would be 0 is if Double.parse(null) == 0
        if(lattitude == 0 || longitude == 0){
            return null;
        }
        List<Integer> eventIDs;
        List<EventModel> events = new ArrayList<>();
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

    /** This will return a list of all users who are attending an event
     *  This will only return if the user has organizer role in the organization
     *
     *
     * @param oauthcode   code for user who requests it
     * @param event_id      id of event that they want user list for
     * @return List of users if it succeeds. Null if database error, no user, no event, or wrong permission
     */

    public static List<User> getUsersAttending(String oauthcode, int event_id){
        // verify oauthcode is a user
        try {
            User us = LambencyServer.dbc.searchForUser(oauthcode);
            if(us == null){
                return null;
            }
            else{
                //verify that event exists
                EventModel eventModel = LambencyServer.dbc.searchEvents(event_id);
                if(eventModel == null){
                    return null;
                }
                else{
                    // check if they have organizer permission
                    Groupies gp = LambencyServer.dbc.searchGroupies(us.getUserId(),eventModel.getOrg_id());
                    if(gp == null || gp.getType() != DatabaseConnection.ORGANIZER){
                        return null;
                    }

                    else if(gp.getType() == DatabaseConnection.ORGANIZER){ // if you want members too, add || gp.getType() == DatabaseConnection.MEMBER
                        // yayy they have permission!!!!! Get the users
                        return LambencyServer.dbc.searchEventAttendanceUsers(event_id);
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EventModel searchEventID(int eventID) {

        try {
            //get latitude and longitude for database
            EventModel eventModel = LambencyServer.dbc.searchEvents(eventID);
            return eventModel;
        } catch (SQLException e) {
            System.out.println("Error in finding event");
            return null;
        }

    }

}
