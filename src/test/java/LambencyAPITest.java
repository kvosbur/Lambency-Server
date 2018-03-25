import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class LambencyAPITest {

    public LambencyAPI getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:20000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LambencyAPI lambencyAPI = retrofit.create(LambencyAPI.class);


        return lambencyAPI;
        /*
        Call<UserAuthenticator> call = lambencyAPI.getGoogleLogin("my test id");
        call.enqueue(this);
        Call<UserAuthenticator> call2 = lambencyAPI.getFacebookLogin("id", "first", "last", "email.com");
        call2.enqueue(this);
        */
    }

    public DatabaseConnection getDatabaseInstance() {
        DatabaseConnection dbc = new DatabaseConnection();
        if(dbc.connect == null) {
            return null;
        }
        else {
            return dbc;
        }
    }


    @Test
    public void getFollowOrg() {
    }

    @Test
    public void getUnfollowOrg() {
    }

    @Test
    public void getMyOrganizedOrgs() {
    }

    @Test
    public void getEventsByOrg() {
    }

    @Test
    public void getRequestsToJoin() {
    }

    @Test
    public void getEndorse() {
    }

    @Test
    public void getChangeUserPermissions() {
    }

    @Test
    public void getMembersAndOrganizersWith0UsersAnd1Organizer() {

        //Create organizer, create 3 users, create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);

        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call
        Response<ArrayList<UserModel>[]> response = null;
        try {
            response = this.getInstance().getMembersAndOrganizers(organizer.getOauthToken(),organization.getOrgID()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<UserModel>[] users = response.body();


        ArrayList<UserModel> members = users[0];
        ArrayList<UserModel> organizers = users[1];
        assertTrue(members.isEmpty() && organizers.contains(organizer));


    }

    @Test
    public void getMembersAndOrganizersWith3UsersAnd1Organizer() {

        //Create organizer, create 3 users, create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        UserModel user1 = new UserModel("user1", "lastnameUSER1", "user1@nonemail.com");
        UserModel user2 = new UserModel("user2", "lastnameUSER2", "user2@nonemail.com");
        UserModel user3 = new UserModel("user3", "lastnameUSER3", "user3@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);

        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));
            user2.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user2.getFirstName(),
                    user2.getLastName(), user2.getEmail(), DatabaseConnection.GOOGLE));
            user3.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user3.getFirstName(),
                    user3.getLastName(), user3.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            // force users to join organization in database

            this.getDatabaseInstance().addGroupies(user1.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 1);
            this.getDatabaseInstance().addGroupies(user2.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 1);
            this.getDatabaseInstance().addGroupies(user3.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 1);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call
        Response<ArrayList<UserModel>[]> response = null;
        try {
            response = this.getInstance().getMembersAndOrganizers(organizer.getOauthToken(),organization.getOrgID()).execute();
            Printing.println("Done executing");
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<UserModel>[] users = response.body();


        ArrayList<UserModel> members = users[0];
        ArrayList<UserModel> organizers = users[1];
        assertTrue(members.contains(user1) && members.contains(user2) && members.contains(user3)&& organizers.contains(organizer));


    }

    @Test
    public void respondToJoinRequest() {
    }

    @Test
    public void getEventNumAttendingWithInvalidEventID() {


        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        UserModel user1 = new UserModel("user1", "lastnameUSER1", "user1@nonemail.com");
        UserModel user2 = new UserModel("user2", "lastnameUSER2", "user2@nonemail.com");
        UserModel user3 = new UserModel("user3", "lastnameUSER3", "user3@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event = new EventModel("Event 1", organization.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 50000),"Test event", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));
            user2.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user2.getFirstName(),
                    user2.getLastName(), user2.getEmail(), DatabaseConnection.GOOGLE));
            user3.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user3.getFirstName(),
                    user3.getLastName(), user3.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());
            event.setOrg_id(organization.getOrgID());
            event = EventHandler.createEvent(event,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            // force users to attend evnet in database

            this.getDatabaseInstance().registerForEvent(user1.getUserId(),event.getEvent_id());
            this.getDatabaseInstance().registerForEvent(user2.getUserId(),event.getEvent_id());
            this.getDatabaseInstance().registerForEvent(user3.getUserId(),event.getEvent_id());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call
        Response<Integer> response = null;
        try {
            response = this.getInstance().getEventNumAttending(organizer.getOauthToken(),"-1").execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        Integer ret = response.body();
        assertTrue(ret == -1);
    }

    @Test
    public void getEventNumAttendingWithInvalidOAUTHCODE() {


        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        UserModel user1 = new UserModel("user1", "lastnameUSER1", "user1@nonemail.com");
        UserModel user2 = new UserModel("user2", "lastnameUSER2", "user2@nonemail.com");
        UserModel user3 = new UserModel("user3", "lastnameUSER3", "user3@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event = new EventModel("Event 1", organization.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 50000),"Test event", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));
            user2.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user2.getFirstName(),
                    user2.getLastName(), user2.getEmail(), DatabaseConnection.GOOGLE));
            user3.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user3.getFirstName(),
                    user3.getLastName(), user3.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());
            event.setOrg_id(organization.getOrgID());
            event = EventHandler.createEvent(event,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            // force users to attend evnet in database

            this.getDatabaseInstance().registerForEvent(user1.getUserId(),event.getEvent_id());
            this.getDatabaseInstance().registerForEvent(user2.getUserId(),event.getEvent_id());
            this.getDatabaseInstance().registerForEvent(user3.getUserId(),event.getEvent_id());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call
        Response<Integer> response = null;
        try {
            response = this.getInstance().getEventNumAttending("NOT A REAL oAUTHCODE",""+event.getEvent_id()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        Integer ret = response.body();
        assertTrue(ret == -1);
    }

    @Test
    public void getEventNumAttendingWith0Attending() {


        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event = new EventModel("Event 1", organization.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 50000),"Test event", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());
            event.setOrg_id(organization.getOrgID());
            event = EventHandler.createEvent(event,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            // force users to attend evnet in database
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call
        Response<Integer> response = null;
        try {
            response = this.getInstance().getEventNumAttending(organizer.getOauthToken(),""+event.getEvent_id()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        Integer ret = response.body();
        assertTrue(ret == 0);
    }

    @Test
    public void getEventNumAttendingWith3Attending() {


        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        UserModel user1 = new UserModel("user1", "lastnameUSER1", "user1@nonemail.com");
        UserModel user2 = new UserModel("user2", "lastnameUSER2", "user2@nonemail.com");
        UserModel user3 = new UserModel("user3", "lastnameUSER3", "user3@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event = new EventModel("Event 1", organization.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 50000),"Test event", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));
            user2.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user2.getFirstName(),
                    user2.getLastName(), user2.getEmail(), DatabaseConnection.GOOGLE));
            user3.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user3.getFirstName(),
                    user3.getLastName(), user3.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());
            event.setOrg_id(organization.getOrgID());
            event = EventHandler.createEvent(event,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            // force users to attend evnet in database

            this.getDatabaseInstance().registerForEvent(user1.getUserId(),event.getEvent_id());
            this.getDatabaseInstance().registerForEvent(user2.getUserId(),event.getEvent_id());
            this.getDatabaseInstance().registerForEvent(user3.getUserId(),event.getEvent_id());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call
        Response<Integer> response = null;
        try {
            response = this.getInstance().getEventNumAttending(organizer.getOauthToken(),""+event.getEvent_id()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        Integer ret = response.body();
        assertTrue(ret == 3);
    }

    @Test
    public void postLeaveOrganization() {
    }

    @Test
    public void getEventsFeed() {
    }

    @Test
    public void getEndorsedOrgs() {
    }

    @Test
    public void testFilterWithNoDate(){

        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 10000000;
        long endTime = currentTime + 1000000;

        //Create organizer, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event0 = new EventModel("Event before timeFrame", organization.getOrgID(), new Timestamp(startTime - 2000000), new Timestamp(startTime - 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event1 = new EventModel("Event starts before and ends during timeFrame", organization.getOrgID(), new Timestamp(startTime - 500000), new Timestamp(currentTime),"Test event 1", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event2 = new EventModel("Event runs during time Frame", organization.getOrgID(), new Timestamp(startTime), new Timestamp(endTime),"Test event 2", "1102 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event3 = new EventModel("Event starts during and ends after timeFrame", organization.getOrgID(), new Timestamp(currentTime), new Timestamp(endTime + 500000),"Test event 3", "1103 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event4 = new EventModel("Event after timeFrame", organization.getOrgID(), new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 4", "1103 3rd Street West Lafayette, IN 47906", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //set up Events
            event0.setOrg_id(organization.getOrgID());
            event1.setOrg_id(organization.getOrgID());
            event2.setOrg_id(organization.getOrgID());
            event3.setOrg_id(organization.getOrgID());
            event4.setOrg_id(organization.getOrgID());

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());
            event1 = EventHandler.createEvent(event1,this.getDatabaseInstance());
            event2 = EventHandler.createEvent(event2,this.getDatabaseInstance());
            event3 = EventHandler.createEvent(event3,this.getDatabaseInstance());
            event4 = EventHandler.createEvent(event4,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //Create Event Filter Model
        EventFilterModel efm = new EventFilterModel(0,0);


        //test the API retrofit call
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsWithFilter(efm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<EventModel> events = response.body();
        assertTrue(events.contains(event0) && events.contains(event1) && events.contains(event2) && events.contains(event3) && events.contains(event4));


    }

    @Test
    public void testFilterWithOnlyStartTime(){

        long currentTime = System.currentTimeMillis();
        long startTimeZero = currentTime - 10000000;

        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event0 = new EventModel("Event before startDate", organization.getOrgID(), new Timestamp(startTimeZero), new Timestamp(currentTime - 5000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event1 = new EventModel("Event ends on startDate", organization.getOrgID(), new Timestamp(startTimeZero), new Timestamp(currentTime),"Test event 1", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event2 = new EventModel("Event starts before and ends after Start Date", organization.getOrgID(), new Timestamp((currentTime + startTimeZero)/2), new Timestamp(currentTime + 10000000),"Test event 2", "1102 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event3 = new EventModel("Event starts after start date", organization.getOrgID(), new Timestamp(currentTime + 10000000), new Timestamp(currentTime + 20000000),"Test event 3", "1103 3rd Street West Lafayette, IN 47906", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //set up Events
            event0.setOrg_id(organization.getOrgID());
            event1.setOrg_id(organization.getOrgID());
            event2.setOrg_id(organization.getOrgID());
            event3.setOrg_id(organization.getOrgID());

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());
            event1 = EventHandler.createEvent(event1,this.getDatabaseInstance());
            event2 = EventHandler.createEvent(event2,this.getDatabaseInstance());
            event3 = EventHandler.createEvent(event3,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //Create Event Filter Model
        EventFilterModel efm = new EventFilterModel(new Timestamp(System.currentTimeMillis() - 50000),0,0 );


        //test the API retrofit call
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsWithFilter(efm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<EventModel> events = response.body();
        assertTrue(!events.contains(event0));
        assertTrue(events.contains(event1));
        assertTrue(events.contains(event2));
        assertTrue(events.contains(event3));

    }

    @Test
    public void testFilterWithOnlyEndTime(){

        long currentTime = System.currentTimeMillis();
        long startTimeZero = currentTime - 10000000;

        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event0 = new EventModel("Event before endDate", organization.getOrgID(), new Timestamp(startTimeZero), new Timestamp(currentTime - 5000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event1 = new EventModel("Event ends on endDate", organization.getOrgID(), new Timestamp(startTimeZero), new Timestamp(currentTime),"Test event 1", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event2 = new EventModel("Event starts before and ends after endDate", organization.getOrgID(), new Timestamp((currentTime + startTimeZero)/2), new Timestamp(currentTime + 10000000),"Test event 2", "1102 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event3 = new EventModel("Event starts after endDate", organization.getOrgID(), new Timestamp(currentTime + 10000000), new Timestamp(currentTime + 20000000),"Test event 3", "1103 3rd Street West Lafayette, IN 47906", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //set up Events
            event0.setOrg_id(organization.getOrgID());
            event1.setOrg_id(organization.getOrgID());
            event2.setOrg_id(organization.getOrgID());
            event3.setOrg_id(organization.getOrgID());

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());
            event1 = EventHandler.createEvent(event1,this.getDatabaseInstance());
            event2 = EventHandler.createEvent(event2,this.getDatabaseInstance());
            event3 = EventHandler.createEvent(event3,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //Create Event Filter Model
        EventFilterModel efm = new EventFilterModel(0,0, new Timestamp(System.currentTimeMillis() - 50000));


        //test the API retrofit call
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsWithFilter(efm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<EventModel> events = response.body();
        assertTrue(events.contains(event0));
        assertTrue(events.contains(event1));
        assertTrue(events.contains(event2));
        assertTrue(!events.contains(event3));

    }

    @Test
    public void testFilterWithStartAndEndTime(){
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 10000000;
        long endTime = currentTime + 1000000;

        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event0 = new EventModel("Event before timeFrame", organization.getOrgID(), new Timestamp(startTime - 2000000), new Timestamp(startTime - 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event1 = new EventModel("Event starts before and ends during timeFrame", organization.getOrgID(), new Timestamp(startTime - 500000), new Timestamp(currentTime),"Test event 1", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event2 = new EventModel("Event runs during time Frame", organization.getOrgID(), new Timestamp(startTime), new Timestamp(endTime),"Test event 2", "1102 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event3 = new EventModel("Event starts during and ends after timeFrame", organization.getOrgID(), new Timestamp(currentTime), new Timestamp(endTime + 500000),"Test event 3", "1103 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event4 = new EventModel("Event after timeFrame", organization.getOrgID(), new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 4", "1103 3rd Street West Lafayette, IN 47906", "Organization");
        Printing.println(new Timestamp(endTime + 1000000) +" baaarrb   " + new Timestamp(endTime + 2000000));
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //set up Events
            event0.setOrg_id(organization.getOrgID());
            event1.setOrg_id(organization.getOrgID());
            event2.setOrg_id(organization.getOrgID());
            event3.setOrg_id(organization.getOrgID());
            event4.setOrg_id(organization.getOrgID());

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());
            event1 = EventHandler.createEvent(event1,this.getDatabaseInstance());
            event2 = EventHandler.createEvent(event2,this.getDatabaseInstance());
            event3 = EventHandler.createEvent(event3,this.getDatabaseInstance());
            event4 = EventHandler.createEvent(event4,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //Create Event Filter Model
        EventFilterModel efm = new EventFilterModel(0,0, new Timestamp(startTime), new Timestamp(endTime));


        //test the API retrofit call
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsWithFilter(efm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<EventModel> events = response.body();
        assertTrue(!events.contains(event0));
        assertTrue(events.contains(event1));
        assertTrue(events.contains(event2));
        assertTrue(events.contains(event3));
        assertTrue(!events.contains(event4));


    }

    @Test
    public void testFilterWithJustDistance(){
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 10000000;
        long endTime = currentTime + 1000000;

        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event0 = new EventModel("Event At location", organization.getOrgID(), new Timestamp(startTime - 2000000), new Timestamp(startTime - 1000000),"Test event 0", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event1 = new EventModel("Event near location", organization.getOrgID(), new Timestamp(startTime - 500000), new Timestamp(currentTime),"Test event 1", "1275 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event2 = new EventModel("Event far but within range", organization.getOrgID(), new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 4", "47403", "Organization");
        EventModel event3 = new EventModel("Event not in range", organization.getOrgID(), new Timestamp(currentTime), new Timestamp(endTime + 500000),"Test event 3", "63017", "Organization");
        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //set up Events
            event0.setOrg_id(organization.getOrgID());
            event1.setOrg_id(organization.getOrgID());
            event2.setOrg_id(organization.getOrgID());
            event3.setOrg_id(organization.getOrgID());

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());
            event1 = EventHandler.createEvent(event1,this.getDatabaseInstance());
            event2 = EventHandler.createEvent(event2,this.getDatabaseInstance());
            event3 = EventHandler.createEvent(event3,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //Create Event Filter Model
        EventFilterModel efm = new EventFilterModel(40.427126,-86.919638, 120);


        //test the API retrofit call
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsWithFilter(efm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<EventModel> events = response.body();
        Printing.println(events);
        assertTrue(events.size() == 3);
        Printing.println("event 0: "+event0);
        assertTrue(event0.equals(events.get(0)));
        assertTrue(events.get(1).equals(event1));
        assertTrue(events.get(2).equals(event2));


    }

    @Test
    public void testFilterWithDistanceAndTime(){
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 10000000;
        long endTime = currentTime + 1000000;

        //Create organizer, create 3 users, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event01 = new EventModel("Event At location out of Time Frame", organization.getOrgID(),  new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 0", "1101 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event02 = new EventModel("Event At location in time Frame", organization.getOrgID(),  new Timestamp(startTime), new Timestamp(endTime),"Test event 0", "1101 3rd Street West Lafayette, IN 47906", "Organization");

        EventModel event11 = new EventModel("Event near location not in timeFrame", organization.getOrgID(),  new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 1", "1275 3rd Street West Lafayette, IN 47906", "Organization");
        EventModel event12 = new EventModel("Event near location in timeFrame", organization.getOrgID(),  new Timestamp(startTime), new Timestamp(endTime),"Test event 1", "1275 3rd Street West Lafayette, IN 47906", "Organization");

        EventModel event21 = new EventModel("Event far but within range not in timeFrame", organization.getOrgID(), new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 4", "47403", "Organization");
        EventModel event22 = new EventModel("Event far but within range in timeFrame", organization.getOrgID(),  new Timestamp(startTime), new Timestamp(endTime),"Test event 4", "47403", "Organization");

        EventModel event31 = new EventModel("Event not in range not in timeFrame", organization.getOrgID(),  new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 3", "63017", "Organization");
        EventModel event32 = new EventModel("Event not in range in timeFrame", organization.getOrgID(),  new Timestamp(startTime), new Timestamp(endTime),"Test event 3", "63017", "Organization");

        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //set up Events
            event01.setOrg_id(organization.getOrgID());
            event11.setOrg_id(organization.getOrgID());
            event21.setOrg_id(organization.getOrgID());
            event31.setOrg_id(organization.getOrgID());
            event02.setOrg_id(organization.getOrgID());
            event12.setOrg_id(organization.getOrgID());
            event22.setOrg_id(organization.getOrgID());
            event32.setOrg_id(organization.getOrgID());

            event01 = EventHandler.createEvent(event01,this.getDatabaseInstance());
            event11 = EventHandler.createEvent(event11,this.getDatabaseInstance());
            event21 = EventHandler.createEvent(event21,this.getDatabaseInstance());
            event31 = EventHandler.createEvent(event31,this.getDatabaseInstance());
            event02 = EventHandler.createEvent(event02,this.getDatabaseInstance());
            event12 = EventHandler.createEvent(event12,this.getDatabaseInstance());
            event22 = EventHandler.createEvent(event22,this.getDatabaseInstance());
            event32 = EventHandler.createEvent(event32,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //Create Event Filter Model
        EventFilterModel efm = new EventFilterModel(40.427126,-86.919638,new Timestamp(startTime), new Timestamp(endTime), 120);


        //test the API retrofit call
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsWithFilter(efm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<EventModel> events = response.body();
        Printing.println(events);
        assertTrue(events.size() == 3);
        assertTrue(events.get(0).equals(event02));
        assertTrue(events.get(1).equals(event12));
        assertTrue(events.get(2).equals(event22));


    }

}