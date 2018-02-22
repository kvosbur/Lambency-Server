import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Testing {
    private static DatabaseConnection dbc;
    private static int event_id = 0;
    private static void clearDatabase(){
        //method that clears database
        try {
            dbc.truncateTables();
        }
        catch (Exception e){
            System.out.println("Reset database failed");
        }
    }
    private static boolean testCreateUser(){
        try {
            int userID = dbc.createUser("facebookUser", "First", "Last", "email@gmail.com", 2);
            if(dbc.verifyUserEmail("email@gmail.com") == 1){
                System.out.println("verifyUserEmail failed: email@gmail.com should exist");
                return false;
            }
            if(dbc.verifyUserEmail("fake@mail.com") == -1){
                System.out.println("verifyUserEmail failed: fake@mail.com should not exist");
                return false;
            }
            User u = dbc.searchForUser("facebookUser", 2);
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            dbc.setOauthCode(userID, ua.getoAuthCode());
            if(u == null){
                System.out.println("search for user failed: returned null");
                return false;
            }
            if(u.getEmail().equals("email@gmail.com") && u.getFirstName().equals("First") && u.getLastName().equals("Last")){
                //User was created and found successfully
                return true;
            }
            else{
                System.out.println("User information failed: information was incorrect");
                return false;
            }


        }
        catch (Exception e){
            System.out.println("databse exception");
        }
        return false;
    }
    private static boolean testCreateOrg(){
        try {
            User u = dbc.searchForUser("facebookUser", 2);
            int orgID = dbc.createOrganization("My Organization", "This is a description", "Org@gmail.com", u.getUserId(), "West Lafayette",
                    "img", u.getUserId());
            if(orgID == -1){
                System.out.println("createOrganization failed: returned -1");
                return false;
            }
            Organization org = dbc.searchForOrg(orgID);
            Organization org2 = dbc.searchForOrg("My Organization");
            if(org == null){
                System.out.println("search for org by id failed: returned null");
                return false;
            }
            if(!(org.getName().equals("My Organization") && org.getDescription().equals("This is a description") && org.getEmail().equals("Org@gmail.com")
                    && org.getContact().getUserId() == u.getUserId() && org.getLocation().equals("West Lafayette") && org.getImage().equals("img"))){
                System.out.println("search for org by id failed: incorrect fields");
                return false;
            }
            org = org2;
            if(org == null){
                System.out.println("search for org by name failed: returned null");
                return false;
            }
            if(!(org.getName().equals("My Organization") && org.getDescription().equals("This is a description") && org.getEmail().equals("Org@gmail.com")
                    && org.getContact().getUserId() == u.getUserId() && org.getLocation().equals("West Lafayette") && org.getImage().equals("img"))){
                System.out.println("search for org by name failed: incorrect fields");
                return false;
            }
            return true;
        }
        catch (Exception e){
            System.out.println("database exception");
        }
        return false;
    }

    private static boolean testCreateEvent(){
        try{
            User u = dbc.searchForUser("facebookUser", 2);
            Organization org = dbc.searchForOrg("My Organization");
            int eventID = dbc.createEvent(org.getOrgID(),"Event 1", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 500),
                    "This is a test event", "Location", "imgg", 5 , 5);
            event_id = eventID;
            if (eventID <= 0){
                System.out.println("Event creation failed");
                return false;
            }
            EventModel e = dbc.searchEvents(eventID);
            if(!(e.getName().equals("Event 1") && e.getOrg_id() == org.getOrgID() && e.getDescription().equals("This is a test event") && e.getLocation().equals("Location")
                    && e.getImage_path().equals("imgg") && Math.abs(e.getLattitude()-5) < 0.01 && Math.abs(e.getLongitude() -5) < 0.01)){
                System.out.println("search for event by name failed: incorrect fields");
                return false;
            }
            return true;
        }
        catch (Exception e){
            System.out.println("database exception");
        }
        return false;
    }
    private static boolean testModifyEvent(){
        try {
            EventModel e = dbc.searchEvents(event_id);
            if(e == null){
                System.out.println("search for event failed");
                return false;
            }
            dbc.modifyEventInfo(event_id, "Updated Name", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 10), "Updated description",
                    "Location 2", "img2", 20, 20);
            e = dbc.searchEvents(event_id);
            if(e == null){
                System.out.println("search for event failed");
                return false;
            }
            if(!(e.getName().equals("Updated Name") && e.getDescription().equals("Updated description") && e.getLocation().equals("Location 2")
                    && e.getImage_path().equals("img2") && Math.abs(e.getLattitude()-20) < 0.01 && Math.abs(e.getLongitude() -20) < 0.01)){
                System.out.println("search for event by name failed: incorrect fields");
                return false;
            }
            return true;

        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    private static boolean testFollowOrg(){
        try{
            UserAuthenticator ua = FacebookLogin.facebookLogin("User2", "Jeff", "Turkstra", "jeff@purdue.edu");
            User u = dbc.searchForUser("User2", 2);
            Organization org = dbc.searchForOrg("My Organization");
            int ret = UserHandler.followOrg(u.getOauthToken(), org.getOrgID());
            if(ret == 1){
                System.out.println("Unable to find user or organization");
                return false;
            }
            if(ret == 2){
                System.out.println("SQL exception");
                return false;
            }
            Groupies g = dbc.searchGroupies(u.getUserId(), org.getOrgID());
            if(g == null){
                System.out.println("Error in groupies: returned null");
                return false;
            }
            if(g.getType() != DatabaseConnection.FOLLOW){
                System.out.println("Error in groupies: not set to follow");
                return false;
            }
            return true;
        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    private static boolean testUnfollowOrg(){
        try{
            User u = dbc.searchForUser("User2", 2);
            Organization org = dbc.searchForOrg("My Organization");
            int ret = UserHandler.unfollowOrg(u.getOauthToken(), org.getOrgID());
            if(ret == 1){
                System.out.println("Unable to find user or organization");
                return false;
            }
            if(ret == 2){
                System.out.println("SQL exception");
                return false;
            }
            Groupies g = dbc.searchGroupies(u.getUserId(), org.getOrgID());
            if(g != null){
                System.out.println("Error in groupies: not deleted");
                return false;
            }
            return true;
        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    private static boolean testChangeUserInfo(){
        try{
            User u = dbc.searchForUser("facebookUser", 2);
            u.setEmail("newemail@gmail.com");
            u.setFirstName("George");
            u.setLastName("Adams");
            u = UserHandler.changeInfo(u);
            if(u == null){
                System.out.println("change user info failed: returned null");
                return false;
            }
            if(!(u.getEmail().equals("newemail@gmail.com") && u.getFirstName().equals("George") && u.getLastName().equals("Adams"))){
                System.out.println("change user info failed: returned incorrect user object");
                return false;
            }
            u = dbc.searchForUser("facebookUser", 2);
            if(!(u.getEmail().equals("newemail@gmail.com") && u.getFirstName().equals("George") && u.getLastName().equals("Adams"))){
                System.out.println("change user info failed: failed to update database");
                return false;
            }
            return true;
        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    private static boolean testRegisterEvent(){
        try{
            User u = dbc.searchForUser("facebookUser", 2);
            int ret = UserHandler.registerEvent(u.getOauthToken(), event_id);
            if(ret == 1){
                System.out.println("event registration failed: failed to find user or org");
                return false;
            }
            if(ret == 2){
                System.out.println("event registration failed: SQL exception");
                return false;
            }
            if(ret == 3){
                System.out.println("event registration failed: user already registered");
                return false;
            }
            EventAttendance ea = dbc.searchEventAttendance(u.getUserId(), event_id);
            if(ea == null){
                System.out.println("event registration failed: failed to update database");
                return false;
            }
            if(!(ea.getUserID() == u.getUserId() || ea.getEventID() == event_id)){
                System.out.println("event registration failed: incorrect information in database");
                return false;
            }
            return true;

        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    private static boolean testUsersAttending(){
        try {
            User u = dbc.searchForUser("facebookUser", 2);
            List<User> userList = EventHandler.getUsersAttending(u.getOauthToken(), event_id);
            if(userList == null){
                System.out.println("making user list failed: returned null");
                return false;
            }
            if(userList.size() > 1){
                System.out.println("making user list failed: returned list of incorrect length");
                return false;
            }
            u = userList.get(0);
            if(!(u.getEmail().equals("newemail@gmail.com") && u.getFirstName().equals("George") && u.getLastName().equals("Adams"))){
                System.out.println("making user list failed: returned incorrect user object");
                return false;
            }
            return true;
        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    private static boolean testRequestJoinOrg(){
        try{
            User u = dbc.searchForUser("User2", 2);
            Organization org = dbc.searchForOrg("My Organization");
            int ret = UserHandler.requestJoinOrg(u.getOauthToken(), org.getOrgID());
            if(ret == 1){
                System.out.println("request to join org failed: unable to find user or org or is already registered");
                return false;
            }
            if(ret == 2){
                System.out.println("request to join org failed: SQL error");
                return false;
            }
            Groupies g = dbc.searchGroupies(u.getUserId(), org.getOrgID());
            if(g == null){
                System.out.println("Error in groupies: returned null");
                return false;
            }
            if(g.getType() != DatabaseConnection.MEMBER){
                System.out.println("Error in groupies: not set to member");
                return false;
            }

            if(g.isConfirmed()){
                System.out.println("Error in groupies: set to confirmed when not");
                return false;
            }
            return true;


        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    private static boolean testSearchOrg(){
        try{
            Organization org = dbc.searchForOrg("My Organization");
            Organization org2 = OrganizationHandler.searchOrgID(org.getOrgID());
            if(!(org.getOrgID() == org2.getOrgID() && org.getName().equals(org2.getName()) && org.getDescription().equals(org2.getDescription()))){
                System.out.println("failed to find org by org id: incorrect fields");
                return false;
            }
            ArrayList<Organization> array = dbc.searchForOrgArray("M");
            if(array == null){
                System.out.println("failed to search org by name: returned null");
                return false;
            }
            if(!(array.size() == 1 && array.get(0).getOrgID() == org.getOrgID())){
                System.out.println("failed to search org by name: returned incorrect org");
                return false;
            }
            User u = dbc.searchForUser("User2", 2);
            int orgID = dbc.createOrganization("My Second Organization", "Second", "Org2@gmail.com", u.getUserId(), "Purdue",
                    "img2", u.getUserId());

            array = dbc.searchForOrgArray("M");
            if(array == null){
                System.out.println("failed to search org by name: returned null");
                return false;
            }
            if(!(array.size() == 2 && array.get(0).getOrgID() == org.getOrgID() && array.get(1).getOrgID() == orgID)){
                System.out.println("failed to search org by name: returned incorrect org");
                return false;
            }
            array = dbc.searchForOrgArray("My Sec");
            if(array == null){
                System.out.println("failed to search org by name: returned null");
                return false;
            }
            if(!(array.size() == 1 && array.get(0).getOrgID() == orgID)){
                System.out.println("failed to search org by name: returned incorrect org");
                return false;
            }

            return true;
        }
        catch (Exception e){
            System.out.println("database error");
        }
        return false;
    }

    public static void main(String[] args){
        try {
            dbc = new DatabaseConnection();
            boolean passedAll = true;
            clearDatabase();
            int passed = 0;
            int count = 0;
            LambencyServer.dbc = dbc;

            System.out.print("Test Create User: ");
            count++;
            if (testCreateUser()) {
                System.out.println("PASSED");
                passed++;
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Create Organization: ");
            count++;
            if (testCreateOrg()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Create Event: ");
            count++;
            if (testCreateEvent()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Modify Event: ");
            count++;
            if (testModifyEvent()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Follow Org: ");
            count++;
            if (testFollowOrg()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Unfollow Org: ");
            count++;
            if (testUnfollowOrg()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Change User Info: ");
            count++;
            if (testChangeUserInfo()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Register Event: ");
            count++;
            if (testRegisterEvent()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test List Users Attending: ");
            count++;
            if (testUsersAttending()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Request Join Org: ");
            count++;
            if (testRequestJoinOrg()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            System.out.print("Test Search Org: ");
            count++;
            if (testSearchOrg()) {
                passed++;
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
                passedAll = false;
            }

            if (passedAll) {
                System.out.println("\nAll Tests Passed");
            } else {
                System.out.println("\nTESTS FAILED");
            }
            System.out.println("Score: " + passed + "/" + count);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
