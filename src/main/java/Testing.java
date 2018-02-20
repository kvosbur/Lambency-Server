import java.sql.Timestamp;

public class Testing {
    private static DatabaseConnection dbc;
    private static void clearDatabase(){
        //method that clears database
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

    public static boolean testCreateEvent(){
        try{
            User u = dbc.searchForUser("facebookUser", 2);
            Organization org = dbc.searchForOrg("My Organization");
            int eventID = dbc.createEvent(org.getOrgID(),"Event 1", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 500),
                    "This is a test event", "Location", "imgg", 5 , 5);
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

    public static void main(String[] args){
        try {
            dbc = new DatabaseConnection();
            boolean passedAll = true;
            clearDatabase();
            int passed = 0;
            int count = 0;

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
