import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
        UserAuthenticator ua = FacebookLogin.facebookLogin("User2", "Jeff", "Turkstra", "jeff@purdue.edu", dbc);
        UserModel u = dbc.searchForUser("User2", 2);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int ret = UserHandler.followOrg(u.getOauthToken(), org.getOrgID(), dbc);
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
        int ret = UserHandler.unfollowOrg(u.getOauthToken(), org.getOrgID(),dbc);
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
        u = UserHandler.changeInfo(u, dbc);
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
        int ret = UserHandler.registerEvent(u.getOauthToken(), event_id, dbc);
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
        List<Object> userList = EventHandler.getUsersAttending(u.getOauthToken(), event_id, dbc);
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
        int ret = UserHandler.requestJoinOrg(u.getOauthToken(), org.getOrgID(), dbc);
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
        int eventID = dbc.createEvent(org.getOrgID(),"Event 2", new Timestamp(2020, 1, 1, 1, 1, 1, 1), new Timestamp(System.currentTimeMillis() + 500),
                "This is my second event", "Location 2", "C:\\Users\\zm\\Pictures\\Camera Roll\\Schedule.PNG", 5 , 5, "ClockIn", "ClockOut");
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


    //Spint 2 Testing
    @org.junit.Test
    public void testEventsByOrg() throws Exception{
        insertData();
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
        if(list.size() != 3 || list.get(0) != 1 || list.get(1) != 2 || list.get(2) != 3){
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

    @org.junit.Test
    public void testEventsByOrgInvalid() throws Exception{
        insertData();
        List<Integer> list = dbc.getOrgEvents(27);
        if(list == null){
            throw new Exception("failed to search events by org: returned null");
        }
        if(list.size() != 0){
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
    @org.junit.Test
    public void testNumAttending() throws Exception{
        insertData();
        int num = dbc.numUsersAttending(1);
        if(num != 1){
            throw new Exception("failed to get num attending event: incorrect number");
        }
        UserModel u = dbc.searchForUser("User2", 2);
        UserHandler.registerEvent(u.getOauthToken(), event_id, dbc);
        num = dbc.numUsersAttending(1);
        if(num != 2){
            throw new Exception("failed to get num attending event: incorrect number");
        }

        Integer number = EventHandler.numAttending(u.getOauthToken(), 1, dbc);
        if(number != 2){
            throw new Exception("failed to get num attending event: incorrect number");
        }

        u = dbc.searchForUser("googleUser", 1);
        UserHandler.registerEvent(u.getOauthToken(), event_id, dbc);
        number = EventHandler.numAttending(u.getOauthToken(), 1, dbc);
        if(number != 3){
            throw new Exception("failed to get num attending event: incorrect number");
        }
    }

    @org.junit.Test
    public void testNumAttendingInvalid() throws Exception{
        insertData();
        int num = dbc.numUsersAttending(-5);
        if(num != 0){
            throw new Exception("failed to get num attending event: incorrect number");
        }

        num = dbc.numUsersAttending(32000);
        if(num != 0){
            throw new Exception("failed to get num attending event: incorrect number");
        }
    }

    @org.junit.Test
    public void testEventsFeed() throws Exception{
        insertData();
        UserModel u = dbc.searchForUser("User2", 2);
        List<EventModel> eventsFeed = UserHandler.eventsFeed(u.getOauthToken(), null, null, dbc);
        if(eventsFeed == null){
            throw new Exception("failed to get events feed: returned null");
        }
        if(eventsFeed.get(0).getEvent_id() != 3 && eventsFeed.get(1).getEvent_id() != 2){
            throw new Exception("failed to get events feed: incorrect event");
        }
    }

    @org.junit.Test
    public void testEventsFeedInvalid() throws Exception{
        insertData();
        List<EventModel> eventsFeed = UserHandler.eventsFeed("invalid authorization token", null, null, dbc);
        if(eventsFeed != null){
            throw new Exception("failed to get events feed: returned a non-null list");
        }
        eventsFeed = UserHandler.eventsFeed(null, null, null, dbc);
        if(eventsFeed != null){
            throw new Exception("failed to get events feed: returned a non-null list");
        }
    }

    @org.junit.Test
    public void testEndorseUnendorse() throws Exception{
        insertData();
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int ret = dbc.endorseEvent(org.getOrgID(), event_id);
        if(ret == -1 || ret != 0){
            throw new Exception("failed to endorse event: returned error code");
        }
        if(!dbc.isEndorsed(org.getOrgID(), event_id)){
            throw new Exception("failed to endorse event: event is not endorsed in database");
        }
        ret = dbc.unendorseEvent(org.getOrgID(), event_id);
        if(ret == -1 || ret != 0){
            throw new Exception("failed to unendorse event: returned error code");
        }
        if(dbc.isEndorsed(org.getOrgID(), event_id)){
            throw new Exception("failed to unendorse event: event is still endorsed");
        }
    }

    @org.junit.Test
    public void testEndorseUnendorseInvalid() throws Exception{
        insertData();
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        int ret = dbc.endorseEvent(org.getOrgID(), -1);
        if(ret != -1){
            throw new Exception("failed to endorse event: returned success code when expecting error code");
        }
        ret = dbc.endorseEvent(-1, event_id);
        if(ret != -1){
            throw new Exception("failed to endorse event: returned success code when expecting error code");
        }
        ret = dbc.endorseEvent(-1, -1);
        if(ret != -1){
            throw new Exception("failed to endorse event: returned success code when expecting error code");
        }
        if(dbc.isEndorsed(org.getOrgID(), event_id)){
            throw new Exception("failed to endorse event: event is endorsed in database when it should not be");
        }

        ret = dbc.unendorseEvent(org.getOrgID(), -1);
        if(ret != -1){
            throw new Exception("failed to endorse event: returned success code when expecting error code");
        }
        ret = dbc.unendorseEvent(-1, event_id);
        if(ret != -1){
            throw new Exception("failed to endorse event: returned success code when expecting error code");
        }
        ret = dbc.unendorseEvent(-1, -1);
        if(ret != -1){
            throw new Exception("failed to endorse event: returned success code when expecting error code");
        }
    }

    @org.junit.Test
    public void testManagePermissions() throws Exception{
        insertData();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        UserModel u2 = dbc.searchForUser("googleUser", 1);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
        dbc.addGroupies(u2.getUserId(), org.getOrgID(), DatabaseConnection.MEMBER, 1);

        int ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), org.getOrgID(), u2.getUserId(), DatabaseConnection.ORGANIZER, dbc);
        if(ret != 0){
            throw new Exception("failed to change user permission: returned error code");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()).getType() != DatabaseConnection.ORGANIZER){
            throw new Exception("failed to change user permission: failed to change database to organizer");
        }

        ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), org.getOrgID(), u2.getUserId(), DatabaseConnection.MEMBER, dbc);
        if(ret != 0){
            throw new Exception("failed to change user permission: returned error code");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()).getType() != DatabaseConnection.MEMBER){
            throw new Exception("failed to change user permission: failed to change database to member");
        }

        //test for changing to what the value already is
        ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), org.getOrgID(), u2.getUserId(), DatabaseConnection.MEMBER, dbc);
        if(ret != 0){
            throw new Exception("failed to change user permission: returned error code");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()).getType() != DatabaseConnection.MEMBER){
            throw new Exception("failed to change user permission: failed to change database to same type it already was");
        }

        ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), org.getOrgID(), u2.getUserId(), 0, dbc);
        if(ret != 0){
            throw new Exception("failed to change user permission: returned error code");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()) != null){
            throw new Exception("failed to change user permission: failed to delete groupies");
        }
    }

    @org.junit.Test
    public void testManagePermissionsInvalid() throws Exception{
        insertData();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        UserModel u2 = dbc.searchForUser("googleUser", 1);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");

        int ret = OrganizationHandler.manageUserPermissions("", org.getOrgID(), u2.getUserId(), DatabaseConnection.ORGANIZER, dbc);
        if(ret != -3){
            throw new Exception("failed to change user permission: returned incorrect return code when oAuthCode is invalid");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()) != null){
            throw new Exception("failed to change user permission: created groupies object when shouldn't have");
        }

        ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), -1, u2.getUserId(), DatabaseConnection.ORGANIZER, dbc);
        if(ret != -3){
            throw new Exception("failed to change user permission: returned incorrect return code when orgID is invalid");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()) != null){
            throw new Exception("failed to change user permission: created groupies object when shouldn't have");
        }

        ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), org.getOrgID(), -1, DatabaseConnection.ORGANIZER, dbc);
        if(ret != -3){
            throw new Exception("failed to change user permission: returned incorrect return code when changedID is invalid");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()) != null){
            throw new Exception("failed to change user permission: created groupies object when shouldn't have");
        }

        ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), org.getOrgID(), u2.getUserId(), -1, dbc);
        if(ret != -3){
            throw new Exception("failed to change user permission: returned incorrect return code when type is invalid");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()) != null){
            throw new Exception("failed to change user permission: created groupies object when shouldn't have");
        }

        ret = OrganizationHandler.manageUserPermissions(u.getOauthToken(), org.getOrgID(), u2.getUserId(), DatabaseConnection.MEMBER, dbc);
        if(ret != -3){
            throw new Exception("failed to change user permission: returned incorrect return code when changedID is not a member of the org");
        }
        if(dbc.searchGroupies(u2.getUserId(), org.getOrgID()) != null){
            throw new Exception("failed to change user permission: created groupies object when shouldn't have");
        }

    }

    @org.junit.Test
    public void testManagePermissionsInsufficient() throws Exception{
        insertData();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        UserModel u2 = dbc.searchForUser("googleUser", 1);
        OrganizationModel org = dbc.searchForOrg("My OrganizationModel");

        int ret = OrganizationHandler.manageUserPermissions(u2.getOauthToken(), org.getOrgID(), u.getUserId(), DatabaseConnection.MEMBER, dbc);
        if(ret != -2){
            throw new Exception("failed to change user permission: returned incorrect return code when insufficient permissions");
        }
        if(dbc.searchGroupies(u.getUserId(), org.getOrgID()).getType() != DatabaseConnection.ORGANIZER){
            throw new Exception("failed to change user permission: created groupies object when shouldn't have");
        }

        dbc.addGroupies(u2.getUserId(), org.getOrgID(), DatabaseConnection.MEMBER, 1);
        ret = OrganizationHandler.manageUserPermissions(u2.getOauthToken(), org.getOrgID(), u.getUserId(), DatabaseConnection.MEMBER, dbc);
        if(ret != -2){
            throw new Exception("failed to change user permission: returned incorrect return code when insufficient permissions");
        }
        if(dbc.searchGroupies(u.getUserId(), org.getOrgID()).getType() != DatabaseConnection.ORGANIZER){
            throw new Exception("failed to change user permission: created groupies object when shouldn't have");
        }
    }

    @org.junit.Test
    public void testEventByDate() throws Exception{
        insertData();
        Date date = new Date(118, 1, 1, 1, 1, 1);
        Timestamp start = new Timestamp(date.getTime());
        date = new Date(119, 1, 1, 1, 1, 1);
        Timestamp end = new Timestamp(date.getTime());
        EventFilterModel efm = new EventFilterModel(0, 0, start, end);
        List<EventModel> list = EventHandler.getEventsWithFilter(efm, dbc);
        if(list == null){
            throw new Exception("failed to search events by date: returned null");
        }
        if(list.size() == 0 || list.size() > 1){
            throw new Exception("failed to search events by date: returned list of wrong size");
        }
        if(list.get(0).getEvent_id() != 1){
            throw new Exception("failed to search events by date: returned incorrect event");
        }

        start = end;
        date = new Date(122, 1, 1, 1, 1, 1);
        end = new Timestamp(date.getTime());
        efm = new EventFilterModel(0, 0, start, end);
        list = EventHandler.getEventsWithFilter(efm, dbc);
        if(list == null){
            throw new Exception("failed to search events by date: returned null");
        }
        if(list.size() == 0 || list.size() > 2){
            throw new Exception("failed to search events by date: returned list of wrong size");
        }
        if(list.get(0).getEvent_id() != 2 || list.get(1).getEvent_id() != 3){
            throw new Exception("failed to search events by date: returned incorrect event");
        }
    }

    @org.junit.Test
    public void testEventByDateInvalid() throws Exception{
        insertData();
        Date date = new Date(118, 1, 1, 1, 1, 1);
        Timestamp start = new Timestamp(date.getTime());
        date = new Date(119, 1, 1, 1, 1, 1);
        Timestamp end = new Timestamp(date.getTime());
        EventFilterModel efm = new EventFilterModel(0, 0, null, end);
        List<EventModel> list = EventHandler.getEventsWithFilter(efm, dbc);
        if(list == null){
            throw new Exception("failed to search events by date: returned null");
        }
        if(list.size() == 0 || list.size() > 1){
            throw new Exception("failed to search events by date: returned list of wrong size");
        }
        if(list.get(0).getEvent_id() != 1){
            throw new Exception("failed to search events by date: returned incorrect event");
        }

        start = end;
        efm = new EventFilterModel(0, 0, start, null);
        list = EventHandler.getEventsWithFilter(efm, dbc);
        if(list == null){
            throw new Exception("failed to search events by date: returned null");
        }
        if(list.size() == 0 || list.size() > 2){
            throw new Exception("failed to search events by date: returned list of wrong size");
        }
        if(list.get(0).getEvent_id() != 2 || list.get(1).getEvent_id() != 3){
            throw new Exception("failed to search events by date: returned incorrect event");
        }

        efm = new EventFilterModel(0, 0, null, null);
        list = EventHandler.getEventsWithFilter(efm, dbc);
        if(list == null){
            throw new Exception("failed to search events by date: returned null");
        }
        if(list.size() == 0 || list.size() > 3){
            throw new Exception("failed to search events by date: returned list of wrong size");
        }
        if(list.get(0).getEvent_id() != 2 || list.get(1).getEvent_id() != 3 || list.get(2).getEvent_id() != 1){
            throw new Exception("failed to search events by date: returned incorrect event");
        }

        list = EventHandler.getEventsWithFilter(null, dbc);
        if(list != null){
            throw new Exception("failed to search events by date: returned null");
        }
    }

    @org.junit.Test
    public void testEventByEdge() throws Exception{
        insertData();
        Date date = new Date(119, 1, 2, 1, 1, 1);
        Timestamp start = new Timestamp(date.getTime());
        date = new Date(119, 1, 10, 1, 1, 1);
        Timestamp end = new Timestamp(date.getTime());
        EventFilterModel efm = new EventFilterModel(0, 0, start, end);
        List<EventModel> list = EventHandler.getEventsWithFilter(efm, dbc);
        if(list == null){
            throw new Exception("failed to search events by date: returned null");
        }
        if(list.size() == 0 || list.size() > 1){
            throw new Exception("failed to search events by date: returned list of wrong size");
        }
        if(list.get(0).getEvent_id() != 3){
            throw new Exception("failed to search events by date: returned incorrect event");
        }

        date = new Date(118, 10, 2, 1, 1, 1);
        start = new Timestamp(date.getTime());
        date = new Date(119, 1, 3, 1, 1, 1);
        end = new Timestamp(date.getTime());
        efm = new EventFilterModel(0, 0, start, end);
        list = EventHandler.getEventsWithFilter(efm, dbc);
        if(list == null){
            throw new Exception("failed to search events by date: returned null");
        }
        if(list.size() == 0 || list.size() > 1){
            throw new Exception("failed to search events by date: returned list of wrong size");
        }
        if(list.get(0).getEvent_id() != 3){
            throw new Exception("failed to search events by date: returned incorrect event");
        }
    }
    @org.junit.Test
    public void testMyLambency() throws  Exception{
        insertData();
        UserModel u = dbc.searchForUser("facebookUser", 2);
        List<OrganizationModel> organizationModelList;
        List<EventModel> eventModelList;
        MyLambencyModel myLambencyModel = UserHandler.getMyLambency(u.getOauthToken(), dbc);
        if(myLambencyModel == null){
            throw new Exception("failed to create my lambency: returned null");
        }
        organizationModelList = myLambencyModel.myOrgs;
        if(organizationModelList == null || organizationModelList.size() != 1 || organizationModelList.get(0).getOrgID() != 1){
            throw new Exception("failed to create my lambency: incorrect myOrgs list");
        }
        organizationModelList = myLambencyModel.joinedOrgs;
        if(organizationModelList == null || organizationModelList.size() != 0){
            throw new Exception("failed to create my lambency: incorrect joinedOrgs list");
        }
        eventModelList = myLambencyModel.eventsAttending;
        if(eventModelList == null || eventModelList.size() != 1 || eventModelList.get(0).getEvent_id() != 1){
            throw new Exception("failed to create my lambency: incorrect eventsAttending list");
        }
        eventModelList = myLambencyModel.eventsOrganizing;
        if(eventModelList == null || eventModelList.size() != 3){
            throw new Exception("failed to create my lambency: incorrect eventsOrganizing list");
        }
    }

    @org.junit.Test
    public void testMyLambencyInvalid() throws  Exception{
        insertData();
        MyLambencyModel myLambencyModel = UserHandler.getMyLambency("", dbc);
        if(myLambencyModel != null){
            throw new Exception("failed to create my lambency: returned a non-null object when expecting null");
        }
        myLambencyModel = UserHandler.getMyLambency(null, dbc);
        if(myLambencyModel != null){
            throw new Exception("failed to create my lambency: returned a non-null object when expecting null");
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
    }

    public void insertData(){
        setUpTests();
        try{
            int userID = dbc.createUser("facebookUser", "First", "Last", "email@gmail.com", 2);
            UserModel u = dbc.searchForUser("facebookUser", 2);
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            dbc.setOauthCode(userID, ua.getoAuthCode());

            userID = dbc.createUser("googleUser", "GoogleFirst", "GoogleLast", "Googleemail@gmail.com", 1);
            u = dbc.searchForUser("googleUser", 1);
            ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            dbc.setOauthCode(userID, ua.getoAuthCode());

            u = dbc.searchForUser("facebookUser", 2);
            dbc.createOrganization("My OrganizationModel", "This is a description", "Org@gmail.com", u.getUserId(), "West Lafayette",
                    "C:\\Users\\zm\\Pictures\\Camera Roll\\Schedule.PNG", u.getUserId());

            OrganizationModel org = dbc.searchForOrg("My OrganizationModel");
            int eventID = dbc.createEvent(org.getOrgID(),"Event 1", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 500),
                    "This is a test event", "Location", "C:\\Users\\zm\\Pictures\\Camera Roll\\Schedule.PNG", 5 , 5, "ClockIn", "ClockOut");
            event_id = eventID;

            EventModel e = dbc.searchEvents(event_id);
            dbc.modifyEventInfo(event_id, "Updated Name", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 10), "Updated description",
                    "Location 2", "C:\\Users\\zm\\Pictures\\Camera Roll\\Schedule.PNG", 20, 20);

            ua = FacebookLogin.facebookLogin("User2", "Jeff", "Turkstra", "jeff@purdue.edu", dbc);
            u = dbc.searchForUser("User2", 2);
            org = dbc.searchForOrg("My OrganizationModel");
            UserHandler.followOrg(u.getOauthToken(), org.getOrgID(), dbc);

            u = dbc.searchForUser("facebookUser", 2);
            u.setEmail("newemail@gmail.com");
            u.setFirstName("George");
            u.setLastName("Adams");
            UserHandler.changeInfo(u, dbc);

            u = dbc.searchForUser("facebookUser", 2);
            int ret = UserHandler.registerEvent(u.getOauthToken(), event_id, dbc);

            u = dbc.searchForUser("User2", 2);
            org = dbc.searchForOrg("My OrganizationModel");
            UserHandler.requestJoinOrg(u.getOauthToken(), org.getOrgID(), dbc);

            dbc.createEvent(org.getOrgID(),"Event 2", new Timestamp(120, 1, 1, 1, 1, 1, 1), new Timestamp(120, 1, 1, 2, 1, 1, 1),
                    "This is my second event", "Location 2", "C:\\Users\\zm\\Pictures\\Camera Roll\\Schedule.PNG", 5 , 5, "ClockIn", "ClockOut");

            u = dbc.searchForUser("User2", 2);
            int orgID = dbc.createOrganization("My Second OrganizationModel", "Second", "Org2@gmail.com", u.getUserId(), "Purdue",
                    "C:\\Users\\zm\\Pictures\\Camera Roll\\Schedule.PNG", u.getUserId());

            dbc.createEvent(org.getOrgID(),"Event 3", new Timestamp(119, 1, 1, 1, 1, 1, 1), new Timestamp(119, 1, 5, 1, 1, 1, 1),
                    "This is my third event", "348 Cottonwood Lane", "C:\\Users\\zm\\Pictures\\Camera Roll\\Schedule.PNG", 5 , 5, "ClockIn", "ClockOut");

        }
        catch (Exception e){
            Printing.println(e.toString());
        }
    }

}