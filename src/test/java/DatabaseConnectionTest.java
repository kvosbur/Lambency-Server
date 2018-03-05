import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 2/23/2018.
 */
public class DatabaseConnectionTest {

    private static DatabaseConnection dbc;
    private static int event_id = 0;

    @org.junit.Test
    public void createUser() throws Exception {
        setUpTests();
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

        userID = dbc.createUser("googleUser", "GoogleFirst", "GoogleLast", "Googleemail@gmail.com", 1);
        if (dbc.verifyUserEmail("Googleemail@gmail.com") == 1) {
            //System.out.println("verifyUserEmail failed: email@gmail.com should exist");
            throw new Exception("verifyUserEmail failed: Googleemail@gmail.com should exist");
        }
        if (dbc.verifyUserEmail("fake@mail.com") == -1) {
            throw new Exception("verifyUserEmail failed: fake@mail.com should not exist");

        }
        u = dbc.searchForUser("googleUser", 1);
        ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
        dbc.setOauthCode(userID, ua.getoAuthCode());
        if (u == null) {
            throw new Exception("search for user failed: returned null");
        }
        if (u.getEmail().equals("Googleemail@gmail.com") && u.getFirstName().equals("GoogleFirst") && u.getLastName().equals("GoogleLast")) {
            //UserModel was created and found successfully
        } else {
            throw new Exception("UserModel information failed: information was incorrect");
        }
    }

    @org.junit.Test
    public void userLoginFacebook() throws Exception{
        createUser();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
        dbc.setOauthCode(u.getUserId(), ua.getoAuthCode());
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
    public void userLoginGoogle() throws Exception{
        createUser();
        UserModel u = dbc.searchForUser("googleUser", 1);
        UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
        dbc.setOauthCode(u.getUserId(), ua.getoAuthCode());
        if (u == null) {
            throw new Exception("search for user failed: returned null");
        }
        if (u.getEmail().equals("Googleemail@gmail.com") && u.getFirstName().equals("GoogleFirst") && u.getLastName().equals("GoogleLast")) {
            //UserModel was created and found successfully
        } else {
            throw new Exception("UserModel information failed: information was incorrect");
        }
    }

    @org.junit.Test
    public void createOrganization() throws Exception{
        createUser();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        int orgID = dbc.createOrganization("My OrganizationModel", "This is a description", "Org@gmail.com", u.getUserId(), "West Lafayette",
                "img", u.getUserId());
        if(orgID == -1){
            throw new Exception("createOrganization failed: returned -1");
        }
        OrganizationModel org = dbc.searchForOrg(orgID);
        OrganizationModel org2 = dbc.searchForOrg("My OrganizationModel");
        if(org == null){
            throw new Exception("search for org by id failed: returned null");
        }
        if(!(org.getName().equals("My OrganizationModel") && org.getDescription().equals("This is a description") && org.getEmail().equals("Org@gmail.com")
                && org.getContact().getUserId() == u.getUserId() && org.getLocation().equals("West Lafayette") && org.getImage().equals("img"))){
            throw new Exception("search for org by id failed: incorrect fields");
        }
        org = org2;
        if(org == null){
            throw new Exception("search for org by name failed: returned null");
        }
        if(!(org.getName().equals("My OrganizationModel") && org.getDescription().equals("This is a description") && org.getEmail().equals("Org@gmail.com")
                && org.getContact().getUserId() == u.getUserId() && org.getLocation().equals("West Lafayette") && org.getImage().equals("img"))){
            throw new Exception("search for org by name failed: incorrect fields");
        }
    }

    @org.junit.Test
    public void createEvent() throws Exception {
        createOrganization();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int eventID = dbc.createEvent(org.getOrgID(),"Event 1", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 500),
                "This is a test event", "Location", "imgg", 5 , 5, "ClockIn", "ClockOut");
        event_id = eventID;
        if (eventID <= 0){
            throw new Exception("Event creation failed");
        }
        EventModel e = dbc.searchEvents(eventID);
        if(!(e.getName().equals("Event 1") && e.getOrg_id() == org.getOrgID() && e.getDescription().equals("This is a test event") && e.getLocation().equals("Location")
                && e.getImage_path().equals("imgg") && Math.abs(e.getLattitude()-5) < 0.01 && Math.abs(e.getLongitude() -5) < 0.01 &&
                e.getClockInCode().equals("ClockIn") && e.getClockOutCode().equals("ClockOut"))){
            throw new Exception("search for event by name failed: incorrect fields");
        }
    }

    @org.junit.Test
    public void modifyEventInfo() throws Exception {
        createEvent();
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
    public void followOrg() throws Exception{
        modifyEventInfo();
        UserAuthenticator ua = FacebookLogin.facebookLogin("User2", "Jeff", "Turkstra", "jeff@purdue.edu");
        UserModel u = dbc.searchForUser("User2", 2);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int ret = UserHandler.followOrg(u.getOauthToken(), org.getOrgID());
        if(ret == 1){
            throw new Exception("Unable to find user or organization");
        }
        if(ret == 2){
            throw new Exception("SQL exception");
        }
        GroupiesModel g = dbc.searchGroupies(u.getUserId(), org.getOrgID());
        if(g == null){
            throw new Exception("Error in groupies: returned null");
        }
        if(g.getType() != DatabaseConnection.FOLLOW){
            throw new Exception("Error in groupies: not set to follow");
        }
    }

    @org.junit.Test
    public void unfollowOrg() throws Exception{
        followOrg();
        UserModel u = dbc.searchForUser("User2", 2);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int ret = UserHandler.unfollowOrg(u.getOauthToken(), org.getOrgID());
        if(ret == 1){
            throw new Exception("Unable to find user or organization");
        }
        if(ret == 2){
            throw new Exception("SQL exception");
        }
        GroupiesModel g = dbc.searchGroupies(u.getUserId(), org.getOrgID());
        if(g != null){
            throw new Exception("Error in groupies: not deleted");
        }
    }

    @org.junit.Test
    public void modifyUserInfo() throws Exception {
        unfollowOrg();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        u.setEmail("newemail@gmail.com");
        u.setFirstName("George");
        u.setLastName("Adams");
        u = UserHandler.changeInfo(u);
        if(u == null){
            throw new Exception("change user info failed: returned null");
        }
        if(!(u.getEmail().equals("newemail@gmail.com") && u.getFirstName().equals("George") && u.getLastName().equals("Adams"))){
            throw new Exception("change user info failed: returned incorrect user object");
        }
        u = dbc.searchForUser("facebookUser", 2);
        if(!(u.getEmail().equals("newemail@gmail.com") && u.getFirstName().equals("George") && u.getLastName().equals("Adams"))){
            throw new Exception("change user info failed: failed to update database");
        }
    }

    @org.junit.Test
    public void registerForEvent() throws Exception {
        modifyUserInfo();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        int ret = UserHandler.registerEvent(u.getOauthToken(), event_id);
        if(ret == 1){
            throw new Exception("event registration failed: failed to find user or org");
        }
        if(ret == 2){
            throw new Exception("event registration failed: SQL exception");
        }
        if(ret == 3){
            throw new Exception("event registration failed: user already registered");
        }
        EventAttendanceModel ea = dbc.searchEventAttendance(u.getUserId(), event_id);
        if(ea == null){
            throw new Exception("event registration failed: failed to update database");
        }
        if(!(ea.getUserID() == u.getUserId() || ea.getEventID() == event_id)){
            throw new Exception("event registration failed: incorrect information in database");
        }
    }

    @org.junit.Test
    public void listUsersRegistered() throws Exception{
        registerForEvent();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        List<Object> userList = EventHandler.getUsersAttending(u.getOauthToken(), event_id);
        if(userList == null){
            throw new Exception("making user list failed: returned null");
        }
        if(userList.size() > 1){
            throw new Exception("making user list failed: returned list of incorrect length");
        }
        u = (UserModel)userList.get(0);
        if(!(u.getEmail().equals("newemail@gmail.com") && u.getFirstName().equals("George") && u.getLastName().equals("Adams"))){
            throw new Exception("making user list failed: returned incorrect user object");
        }
    }

    @org.junit.Test
    public void requestJoinOrg() throws Exception{
        listUsersRegistered();
        UserModel u = dbc.searchForUser("User2", 2);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int ret = UserHandler.requestJoinOrg(u.getOauthToken(), org.getOrgID());
        if(ret == 1){
            throw new Exception("request to join org failed: unable to find user or org or is already registered");
        }
        if(ret == 2){
            throw new Exception("request to join org failed: SQL error");
        }
        GroupiesModel g = dbc.searchGroupies(u.getUserId(), org.getOrgID());
        if(g == null){
            throw new Exception("Error in groupies: returned null");
        }
        if(g.getType() != DatabaseConnection.MEMBER){
            throw new Exception("Error in groupies: not set to member");
        }

        if(g.isConfirmed()){
            throw new Exception("Error in groupies: set to confirmed when not");
        }
    }

    @org.junit.Test
    public void searchForOrg() throws Exception{
        requestJoinOrg();
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        ArrayList<OrganizationModel> array = dbc.searchForOrgArray("M");
        if(array == null){
            throw new Exception("failed to search org by name: returned null");
        }
        if(!(array.size() == 1 && array.get(0).getOrgID() == org.getOrgID())){
            throw new Exception("failed to search org by name: returned incorrect org");
        }
        UserModel u = dbc.searchForUser("User2", 2);
        int orgID = dbc.createOrganization("My Second OrganizationModel", "Second", "Org2@gmail.com", u.getUserId(), "Purdue",
                "img2", u.getUserId());

        array = dbc.searchForOrgArray("M");
        if(array == null){
            throw new Exception("failed to search org by name: returned null");
        }
        if(!(array.size() == 2 && array.get(0).getOrgID() == org.getOrgID() && array.get(1).getOrgID() == orgID)){
            throw new Exception("failed to search org by name: returned incorrect org");
        }
        array = dbc.searchForOrgArray("My Sec");
        if(array == null){
            throw new Exception("failed to search org by name: returned null");
        }
        if(!(array.size() == 1 && array.get(0).getOrgID() == orgID)){
            throw new Exception("failed to search org by name: returned incorrect org");
        }
    }

    @org.junit.Test
    public void searchEventsByLocation() throws Exception{
        searchForOrg();
        List<Integer> list = dbc.searchEventsByLocation(0,0);
        if(list == null){
            throw new Exception("failed to search events by location: returned null");
        }
        if(list.get(0) != 1){
            throw new Exception("failed to search events by location: returned incorrect list");
        }
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int eventID = dbc.createEvent(org.getOrgID(),"Event 2", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 500),
                "This is my second event", "Location 2", "event2img", 5 , 5, "ClockIn", "ClockOut");
        list = dbc.searchEventsByLocation(0,0);
        if(list == null){
            throw new Exception("failed to search events by location: returned null");
        }
        if(list.get(0) != 2){
            throw new Exception("failed to search events by location: returned incorrect list");
        }
        if(list.get(1) != 1){
            throw new Exception("failed to search events by location: returned incorrect list");
        }

    }

    @org.junit.Test
    public void getOrgEvents() throws Exception{
        searchEventsByLocation();
        OrganizationModel org = dbc.searchForOrg("My Second OrganizationModel");
        List<Integer> list = dbc.getOrgEvents(org.getOrgID());
        if(list == null){
            throw new Exception("failed to search events by org: returned null");
        }
        if(list.size() != 0){
            throw new Exception("failed to search events by org: list contained incorrect entries");
        }
        org = dbc.searchForOrg("My OrganizationModel");
        list = dbc.getOrgEvents(org.getOrgID());
        if(list == null){
            throw new Exception("failed to search events by org: returned null");
        }
        if(list.size() != 2 || list.get(0) != 1 || list.get(1) != 2){
            throw new Exception("failed to search events by org: list contained incorrect entries");
        }
        list = dbc.getOrgEvents(-1);
        if(list == null){
            throw new Exception("failed to search events by org: returned null");
        }
        if(list.size() != 0){
            throw new Exception("failed to search events by org: list contained incorrect entries");
        }
    }
    public void setUpTests(){
        try {
            dbc = new DatabaseConnection();
            dbc.truncateTables();
        }
        catch (Exception e){
            return;
        }
        LambencyServer.dbc = dbc;
    }

}