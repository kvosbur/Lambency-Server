import java.sql.Timestamp;

import static org.junit.Assert.*;

/**
 * Created by Evan on 2/23/2018.
 */
public class DatabaseConnectionTest {

    private static DatabaseConnection dbc;
    private static int event_id = 0;

    @org.junit.Test
    public void getUserList() throws Exception {

    }

    @org.junit.Test
    public void searchForUser() throws Exception {

    }

    @org.junit.Test
    public void searchForUser1() throws Exception {

    }

    @org.junit.Test
    public void createUser() throws Exception {
        int userID = dbc.createUser("facebookUser", "First", "Last", "email@gmail.com", 2);
        if (dbc.verifyUserEmail("email@gmail.com") == 1) {
            //System.out.println("verifyUserEmail failed: email@gmail.com should exist");
            throw new Exception("verifyUserEmail failed: email@gmail.com should exist");
        }
        if (dbc.verifyUserEmail("fake@mail.com") == -1) {
            throw new Exception("verifyUserEmail failed: fake@mail.com should not exist");

        }
        UserModel u = dbc.searchForUser("facebookUser", 2);
        UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
        dbc.setOauthCode(userID, ua.getoAuthCode());
        if (u == null) {
            throw new Exception("search for user failed: returned null");
        }
        if (u.getEmail().equals("email@gmail.com") && u.getFirstName().equals("First") && u.getLastName().equals("Last")) {
            //UserModel was created and found successfully
        } else {
            throw new Exception("UserModel information failed: information was incorrect");
        }
    }

    @org.junit.Test
    public void createEvent() throws Exception {
        UserModel u = dbc.searchForUser("facebookUser", 2);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int eventID = dbc.createEvent(org.getOrgID(),"Event 1", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 500),
                "This is a test event", "Location", "imgg", 5 , 5);
        event_id = eventID;
        if (eventID <= 0){
            throw new Exception("Event creation failed");
        }
        EventModel e = dbc.searchEvents(eventID);
        if(!(e.getName().equals("Event 1") && e.getOrg_id() == org.getOrgID() && e.getDescription().equals("This is a test event") && e.getLocation().equals("Location")
                && e.getImage_path().equals("imgg") && Math.abs(e.getLattitude()-5) < 0.01 && Math.abs(e.getLongitude() -5) < 0.01)){
            throw new Exception("search for event by name failed: incorrect fields");
        }
    }

    @org.junit.Test
    public void modifyEventInfo() throws Exception {
        EventModel e = dbc.searchEvents(event_id);
        if(e == null){
            throw new Exception("search for event failed");
        }
        dbc.modifyEventInfo(event_id, "Updated Name", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 10), "Updated description",
                "Location 2", "img2", 20, 20);
        e = dbc.searchEvents(event_id);
        if(e == null){
            throw new Exception("search for event failed");
        }
        if(!(e.getName().equals("Updated Name") && e.getDescription().equals("Updated description") && e.getLocation().equals("Location 2")
                && e.getImage_path().equals("img2") && Math.abs(e.getLattitude()-20) < 0.01 && Math.abs(e.getLongitude() -20) < 0.01)){
            throw new Exception("search for event by name failed: incorrect fields");
        }
    }

    @org.junit.Test
    public void setOauthCode() throws Exception {

    }

    @org.junit.Test
    public void modifyUserInfo() throws Exception {

    }

    @org.junit.Test
    public void verifyUserEmail() throws Exception {

    }

    @org.junit.Test
    public void createEvent1() throws Exception {

    }

    @org.junit.Test
    public void searchEventsByLocation() throws Exception {

    }

    @org.junit.Test
    public void searchEvents() throws Exception {

    }

    @org.junit.Test
    public void searchEventAttendance() throws Exception {

    }

    @org.junit.Test
    public void registerForEvent() throws Exception {

    }

    @org.junit.Test
    public void searchEventAttendanceUsers() throws Exception {

    }

}