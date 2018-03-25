import com.google.maps.model.LatLng;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

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

            //notify users of event creation
            ArrayList<String> userEmails = dbc.getUserEmailsToNotify(event.getOrg_id());
            EventModel eventRet = dbc.searchEvents(event_id);
            sendEmailsOfEventCreation(userEmails,eventRet,dbc);

            return eventRet;
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
            EventModel prev = dbc.searchEvents(event.getEvent_id());
            dbc.modifyEventInfo(event.getEvent_id(),event.getName(),event.getStart(),event.getEnd(),
                    event.getDescription(),event.getLocation(),event.getImage_path(),event.getLattitude(),event.getLongitude());
            EventModel now = dbc.searchEvents(event.getEvent_id());

            //send emails to attending users of info change
            ArrayList<Object> users = dbc.searchEventAttendanceUsers(prev.getEvent_id(),true);
            if(users != null) {
                EventHandler.sendEmailsOfEventModification(users, prev, now, dbc);
            }

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
            Printing.println("null Filter Model");
            return null;
        }
        List<Integer> eventIDs;
        List<EventModel> events = new ArrayList<>();
        try{
            eventIDs = dbc.searchEventsWithFilterModel(efm);
            if(eventIDs == null){
                //there were no search results found
                Printing.println("No search results from Model");
                return null;
            }
            for(Integer i: eventIDs){
                EventModel eventModel = dbc.searchEvents(i);
                eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                events.add(eventModel);
            }
        } catch (SQLException e) {
            Printing.println(e.toString());
            Printing.println("Error in get events by filter with error: "+e);;
            return null;
        } catch (Exception e){
            Printing.println(e.toString());
            Printing.println("Error in get events by Filter");
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
                    return 0;
                }else{
                    return 2;
                }

            }
        } catch (SQLException e) {
            Printing.println(e.toString());
        }
        return 4;
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
        char[] charArray = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
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
                Printing.println("invalid oAuthCode");
                return new Integer(-1);
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return new Integer(-1);
            }
            if(dbc.searchEvents(eventID) == null){
                Printing.println("Could not find event.");
                return new Integer(-1);
            }
            return dbc.numUsersAttending(eventID);
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return new Integer(-1);

    }

    /**
     * Sorts a list of EventModel by their start time
     * @param events
     * @return the sorted list of EventModel
     */
    public static List<EventModel> sortEventListByDate(List<EventModel> events){
        Collections.sort(events, Comparator.comparing(EventModel::getStart));
        return events;
    }

    /**
     * Sends emails to users listed of the new event
     * @param userEmails list of user emails
     * @param event The event info to be displayed in email
     */
    public static void sendEmailsOfEventCreation(ArrayList<String> userEmails, EventModel event, DatabaseConnection dbc){

        try{
            //create email
            OrganizationModel org = dbc.searchForOrg(event.getOrg_id());
            if(org == null){
                Printing.println("Issue finding Organization to go with event created");
                return;
            }
            String subject = "New Event From " + org.getName();
            StringBuilder sb = new StringBuilder();
            sb.append("<strong>" + event.getName() + "</strong><br>");

            String starting = new SimpleDateFormat("MMMM dd, YYYY hh:mm a").format(event.getStart());
            String ending = new SimpleDateFormat("MMMM dd, YYYY hh:mm a").format(event.getEnd());

            sb.append("<u>Starts</u> : " + starting + "<br><u>Ends</u> : " + ending + "<br>");

            sb.append("<u>Location</u> : " + event.getLocation());
            sb.append("<br><u>Description</u><br>" + event.getDescription());
            sb.append("<br><br><div style=\"opacity:0.2;\">*Please do not reply to this email as this is an automated message.*</div>");

            //create GMailHelper object
            GMailHelper gMailHelper= new GMailHelper();

            //send emails to all users
            for(String userEmail: userEmails){
                int ret = gMailHelper.sendEmail(userEmail,subject,sb.toString());
                if(ret == GMailHelper.FAILURE){
                    Printing.println("Issue sending email to " + userEmail + " for event creation");
                }
            }

        }catch(Exception e){
            Printing.println("Issue trying to send emails to users for event creation.");
            Printing.println(e.toString());
        }
    }

    /**
     * Sends emails to users listed of the modified event
     * @param users list of users
     * @param prev The event info as it previous was
     * @param now The event info as it now is
     */
    public static void sendEmailsOfEventModification(ArrayList<Object> users, EventModel prev, EventModel now, DatabaseConnection dbc){

        try{

            boolean changed = false;
            //create email
            String subject = "Event Info changed for " + now.getName();

            //for each info item of event bold title of it if the info has changed
            StringBuilder sb = new StringBuilder();
            String name;
            if(prev.getName().equals(now.getName())){
                name = now.getName() + "<br>";
            }else{
                name = "<strong>" + now.getName() + "</strong><br>";
                changed = true;
            }
            sb.append(name);

            String startingPrev = new SimpleDateFormat("MMMM dd, YYYY hh:mm a").format(prev.getStart());
            String startingNow = new SimpleDateFormat("MMMM dd, YYYY hh:mm a").format(now.getStart());
            String endingPrev = new SimpleDateFormat("MMMM dd, YYYY hh:mm a").format(prev.getEnd());
            String endingNow = new SimpleDateFormat("MMMM dd, YYYY hh:mm a").format(now.getEnd());

            String starting;
            String ending;
            if(startingNow.equals(startingPrev)){
                starting = "<u>Starts</u> : " + startingNow + "<br>";
            }else{
                starting = "<strong><u>Starts</u> : " + startingNow + "</strong><br>";
                changed = true;
            }

            if(endingNow.equals(endingPrev)){
                ending = "<u>Starts</u> : " + endingNow + "<br>";
            }else{
                ending = "<strong><u>Ends</u> : " + endingNow + "</strong><br>";
                changed = true;
            }

            sb.append(starting + ending);

            String location;
            if(prev.getLocation().equals(now.getLocation())){
                location = "<u>Location</u> : " + prev.getLocation();
            }else{
                location = "<strong><u>Location</u> : " + prev.getLocation() + "</strong>";
                changed = true;
            }

            sb.append("<br><br>*Please do not reply to this email as this is an automated message.*");

            //don't send emails if nothing really changed
            if(changed) {
                //create GMailHelper object
                GMailHelper gMailHelper = new GMailHelper();

                //send emails to all users
                for (Object userObject : users) {
                    UserModel user =  (UserModel) userObject;
                    int ret = gMailHelper.sendEmail(user.getEmail(), subject, sb.toString());
                    if (ret == GMailHelper.FAILURE) {
                        Printing.println("Issue sending email to " + user.getEmail() + " for event creation");
                    }
                }
            }

        }catch(Exception e){
            Printing.println("Issue trying to send emails to users for event modification.");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param oAuthCode oAuthCode of the user
     * @param eventID id of the event
     * @param dbc database connection
     * @return list of the orgs that have endorsed the events
     */
    public static List<OrganizationModel> getEndorsedOrgs(String oAuthCode, int eventID, DatabaseConnection dbc){
        try {
            if(oAuthCode == null){
                Printing.println("invalid oAuthCode");
                return null;
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return null;
            }
            if(dbc.searchEvents(eventID) == null){
                Printing.println("Event not found");
                return null;
            }
            List<Integer> orgs = dbc.getEndorsedOrgs(eventID);
            if(orgs == null){
                Printing.println("list of orgs was null");
            }
            List<OrganizationModel> orgList = new ArrayList<OrganizationModel>();
            for(int i: orgs){
                orgList.add(OrganizationHandler.searchOrgID(i, dbc));
            }
            return orgList;
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return null;
    }
}
