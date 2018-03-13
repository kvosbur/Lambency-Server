import com.google.maps.model.LatLng;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventHandler {

    /**
     *
     * @param event   Event that is to be added to the database
     * @return  (int)   Will return event_id on success, -1 on failure
     */


    public static EventModel createEvent(EventModel event, DatabaseConnection dbc) {
        
        try {
            //get latitude and longitude for database
            LatLng latlng = GoogleGeoCodeUtil.getGeoData(event.getLocation());
            if(latlng == null){
                latlng = new LatLng(180,180);
            }
            //write event image
            Printing.println("file_path is null: "+event.getImage_path() == null);
            if(event.getImage_path() == null && event.getImageFile() != null){
                event.setImage_path(ImageWR.writeImageToFile(event.getImageFile()));
            }
            //create clock in and clock out code
            event.setClockInCode(EventHandler.generateClockInOutCodes());
            event.setClockOutCode(EventHandler.generateClockInOutCodes());
            //create event
            int event_id = dbc.createEvent(event.getOrg_id(),event.getName(),event.getStart(),
                    event.getEnd(),event.getDescription(),event.getLocation(),event.getImage_path(), latlng.lat, latlng.lng,
                    event.getClockInCode(), event.getClockOutCode());
            Printing.println((latlng == null));
            return dbc.searchEvents(event_id);
        } catch (SQLException e) {
            Printing.println("SQL exception: ");
            Printing.println(e.toString());
            Printing.println("Error in creating event: "+event.getName());
            return null;
        }
        catch (IOException e){
            Printing.println("Error in writing image.");
            Printing.println("Error in creating event: "+event.getName());
            Printing.println(e.toString());
            return null;
        }

    }


    public static int updateEvent(EventModel event, DatabaseConnection dbc) {

        try{
            dbc.modifyEventInfo(event.getEvent_id(),event.getName(),event.getStart(),event.getEnd(),
                    event.getDescription(),event.getLocation(),event.getImage_path(),event.getLattitude(),event.getLongitude());
            return 0;
        }
        catch (SQLException e){
            Printing.println("Error in updating Event: "+event.getName());
            return 1;
        }
    }


    /** Call from the API to gather the events that are searched by location
     *
     * @param efm EventFilterModel that contains all constraints for the search
     * @return     List of events if successful, null otherwise
     */

    public static List<EventModel> getEventsWithFilter(EventFilterModel efm, DatabaseConnection dbc){
        if(efm == null){
            return null;
        }
        List<Integer> eventIDs;
        List<EventModel> events = new ArrayList<>();
        try{
            eventIDs = dbc.searchEventsWithFilterModel(efm);
            for(Integer i: eventIDs){
                EventModel eventModel = dbc.searchEvents(i);
                eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                events.add(eventModel);
            }
        } catch (SQLException e) {
            Printing.println(e.toString());
            Printing.println("Error in get events by location: "+e);
            return null;
        } catch (Exception e){
            System.out.println("Error in get events by location");
            return null;
        }

        return events;
    }

    /** Call from the API to gather the events that are searched by location
     *
     * @param lattitude    double for lat
     * @param longitude    double for long
     * @return     List of events if successful, null otherwise
     */

    public static List<EventModel> getEventsByLocation(double lattitude, double longitude, DatabaseConnection dbc){
        // I am assuming that the odds of them being at exactly 0,0 (lat,long) is so microscopic that the only case that it would be 0 is if Double.parse(null) == 0
        if(lattitude == 0 || longitude == 0) {
            return null;
        }
        List<Integer> eventIDs;
        List<EventModel> events = new ArrayList<>();
        try{
            eventIDs = dbc.searchEventsByLocation(lattitude,longitude);
            for(Integer i: eventIDs){
                EventModel eventModel = dbc.searchEvents(i);
                eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                events.add(eventModel);
            }
        } catch (SQLException e) {
            Printing.println(e.toString());
            Printing.println("Error in get events by location: "+e);
            return null;
        } catch (Exception e){
            Printing.println(e.toString());
            Printing.println("Error in get events by location" + e);
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

    public static List<Object> getUsersAttending(String oauthcode, int event_id, DatabaseConnection dbc){


        // verify oauthcode is a user
        try {
            UserModel us = dbc.searchForUser(oauthcode);
            if(us == null){
                return null;
            }
            else{
                //verify that event exists
                EventModel eventModel = dbc.searchEvents(event_id);
                if(eventModel == null){
                    return null;
                }
                else{
                    // check if they have organizer permission
                    GroupiesModel gp = dbc.searchGroupies(us.getUserId(),eventModel.getOrg_id());
                    if(gp == null || gp.getType() != DatabaseConnection.ORGANIZER){
                        return null;
                    }

                    else if(gp.getType() == DatabaseConnection.ORGANIZER){ // if you want members too, add || gp.getType() == DatabaseConnection.MEMBER
                        // yayy they have permission!!!!! Get the users
                        return dbc.searchEventAttendanceUsers(event_id, true);
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            Printing.println(e.toString());
        }
        return null;
    }


    /**
     *
     * @param eventID the id of the event
     * @return the event object for the id, otherwise null
     */
    public static EventModel searchEventID(int eventID, DatabaseConnection dbc) {

        try {
            EventModel eventModel = dbc.searchEvents(eventID);
            eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
            return eventModel;
        } catch (SQLException e) {
            Printing.println("Error in finding event");
            return null;
        }catch(Exception e){
            Printing.println(e.toString());
            System.out.println("error in getting event image");
            return null;
        }

    }

    /**
     *
     * @param userID the id of the user
     * @param oAuthCode the oAuthCode of the user
     * @return a list of all the events the user is attending, if size =0, the user is attending no events
     */
    public static List<EventModel> searchEventIDS(int userID, String oAuthCode, DatabaseConnection dbc){

        try {
            List<Integer> list = dbc.searchUserEventAttendance(userID);
            List<EventModel> ret = new ArrayList<EventModel>();
            for (int id : list) {
                EventModel eventModel = searchEventID(id, dbc);
                if(eventModel == null){
                    //dont add to list
                }
                else {
                    ret.add(eventModel);
                }
            }
            return ret;
        }
        catch (SQLException e){
            Printing.println("Failed to find user event attendance");
            return new ArrayList<EventModel>();
        }
    }

    /**
     *
     * @param eventAttendanceModel the model of the info to use to clock in with
     * @param oAuthCode the oAuthCode of the user
     * @return an integer telling success of request, 0 = success / 1 = failure
     */

    public static int clockInEvent(String oAuthCode, EventAttendanceModel eventAttendanceModel, DatabaseConnection dbc){

        // verify oauthcode is a user
        try {
            UserModel us = dbc.searchForUser(oAuthCode);
            if(us == null){
                Printing.println("oauth token is not valid");
                return 1;
            }
            else{
                //verify that event exists
                String clockInOutCode = eventAttendanceModel.getClockInOutCode();
                List<int[]> response = dbc.findClockInOutCode(clockInOutCode);
                if(response != null) {
                    //they have already signed up for event(required in order to clock in)
                    int eventid = 0, clockType = 0;
                    List<Integer> attending = dbc.searchUserEventAttendance(us.getUserId());
                    for(int[] a: response){
                        if(attending.contains(a[0])){
                            eventid = a[0];
                            clockType = a[1];
                        }
                    }

                    //no event found for user that match code inputted
                    if(eventid == 0){
                        return 3;
                    }

                    //clock in user
                    if (clockType == EventAttendanceModel.CLOCKOUTCODE){
                        dbc.eventClockInOutUser(eventid, us.getUserId(), eventAttendanceModel.getEndTime(), EventAttendanceModel.CLOCKOUTCODE);
                    }else if(clockType == EventAttendanceModel.CLOCKINCODE){
                        dbc.eventClockInOutUser(eventid,us.getUserId(),eventAttendanceModel.getStartTime(), EventAttendanceModel.CLOCKINCODE);
                    }
                }else{
                    return 2;
                }

            }
        } catch (SQLException e) {
            Printing.println(e.toString());
        }
        return 1;
    }

    /**
     *
     * @return boolean of whether clean up was successful
     */

    public static boolean cleanUpEvents(DatabaseConnection dbc){


        try {
            //find all events that have ended
            ArrayList<Integer> events = dbc.getEventsThatEnded(null);

            int result = 0;
            for(Integer eventID: events){
                //check for users for this event that don't have check out times
                ArrayList<Integer> users = dbc.getUsersNoEndTime(eventID);
                EventModel event = null;
                if(users.size() != 0){
                    event = dbc.searchEvents(eventID);
                }
                for(Integer userID: users){
                    //give them the end time that is stored in the event model
                    dbc.eventClockInOutUser(eventID, userID, event.getEnd(), EventAttendanceModel.CLOCKOUTCODE);
                }
                //move this event to the historical tables(event entry and all attendence records
                result += dbc.moveEventToHistorical(eventID);
            }

            if(result == 0){
                return true;
            }
            return false;
        }catch(SQLException e){
            Printing.println(e.toString());
        }

        return false;
    }

    public static String generateClockInOutCodes(){
        char[] charArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder code = new StringBuilder();
        Random r = new Random();
        for(int i = 0; i < 8; i++){
            code.append(charArray[r.nextInt(charArray.length)]);
        }
        return code.toString();
    }

    /**
     * @param oAuthCode the authentication code of the user
     * @param eventID the id of the event
     * @return the number of users attending an event, on fail -1
     */
    public static Integer numAttending(String oAuthCode, int eventID, DatabaseConnection dbc) {

        try {
            if(oAuthCode == null){
                return new Integer(-1);
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return new Integer(-1);
            }
            return dbc.numUsersAttending(eventID);
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return new Integer(-1);

    }




}
