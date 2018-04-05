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
import java.util.List;

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

    //user story 3
    @Test
    public void get5EventsFromOrg() {

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


        //test the API retrofit call
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsByOrg(organizer.getOauthToken(),""+organization.getOrgID()).execute();
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
    public void getNoEventsFromEventlessOrg() {

        //Create organizer, create organization, and create event locally
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
        Response<ArrayList<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsByOrg(organizer.getOauthToken(),""+organization.getOrgID()).execute();
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
        assertTrue(events.isEmpty());


    }

    //user story 4
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

    //user story 5
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

    //user story 6
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
            System.out.println("reponse code: " + response.code());
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

    //user story 7
    @Test
    public void testClockInNotAttending(){

        UserModel user1 = new UserModel("Organizer", "Lastname", "organizer@gmail.com");

        OrganizationModel organization = new OrganizationModel(user1,"Organization","1101 3rd street",-1,
                "Test org","organization@noemail.com",user1,null);

        EventModel event = new EventModel("Event 1", organization.getOrgID(), new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() + 50000),"Test event", "1101 3rd Street West Lafayette, IN 47906", "Organization");

        try {
            this.getDatabaseInstance().truncateTables();

            //create in database
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            event.setOrg_id(organization.getOrgID());
            event = EventHandler.createEvent(event,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user1.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user1.getUserId(),ua.getoAuthCode());


        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //model of user attendance
        EventAttendanceModel attendance = new EventAttendanceModel(event.getEvent_id(),user1.getUserId(),
                new Timestamp(System.currentTimeMillis()),event.getClockInCode());


        //test the API retrofit call for user
        Response<Integer> response = null;
        try {
            response = this.getInstance().sendClockInCode(user1.getOauthToken(),attendance).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response == null || response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        int responseValue = response.body();

        assertTrue(responseValue != 0);

    }

    @Test
    public void testClockOutBeforeClockIn(){
        //testing if able to clock out before clocking in

        UserModel user1 = new UserModel("Organizer", "Lastname", "organizer@gmail.com");

        OrganizationModel organization = new OrganizationModel(user1,"Organization","1101 3rd street",-1,
                "Test org","organization@noemail.com",user1,null);

        EventModel event = new EventModel("Event 1", organization.getOrgID(), new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() + 50000),"Test event", "1101 3rd Street West Lafayette, IN 47906", "Organization");

        try {
            this.getDatabaseInstance().truncateTables();

            //create in database
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            event.setOrg_id(organization.getOrgID());
            event = EventHandler.createEvent(event,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user1.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user1.getUserId(),ua.getoAuthCode());


            this.getDatabaseInstance().registerForEvent(user1.getUserId(),event.getEvent_id());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //model of user attendance
        EventAttendanceModel attendance = new EventAttendanceModel(event.getEvent_id(),user1.getUserId(),
                new Timestamp(System.currentTimeMillis()),event.getClockOutCode());


        //test the API retrofit call for user
        Response<Integer> response = null;
        try {
            response = this.getInstance().sendClockInCode(user1.getOauthToken(),attendance).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response == null || response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        int responseValue = response.body();

        assertTrue(responseValue != 0);
    }


    //user story 9
    @Test
    public void endorseEvent() {
        //Create organizer,  create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer,"Organization1","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        OrganizationModel organization2 = new OrganizationModel(organizer,"Organization2","1101 3rd street",-1,"Test org2","organization2@noemail.com",organizer,null);

        EventModel event0 = new EventModel("Event before timeFrame", organization1.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization1");




        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());


            event0.setOrg_id(organization1.getOrgID());

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());

        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }





        //test the API retrofit call
        Response<Integer> response = null;
        try {
            response = this.getInstance().getEndorse(organizer.getOauthToken(),""+organization2.getOrgID(),""+event0.getEvent_id()).execute();
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

        try {
            assertTrue(ret == 0 && this.getDatabaseInstance().isEndorsed(organization2.getOrgID(),event0.getEvent_id()));
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }

    @Test
    public void endorseEventAlreadyEndorsedByThisOrg() {
        //Create organizer,  create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer,"Organization1","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        OrganizationModel organization2 = new OrganizationModel(organizer,"Organization2","1101 3rd street",-1,"Test org2","organization2@noemail.com",organizer,null);

        EventModel event0 = new EventModel("Event before timeFrame", organization1.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization1");




        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));

            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());


            event0.setOrg_id(organization1.getOrgID());

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());

            this.getDatabaseInstance().endorseEvent(organization2.getOrgID(),event0.getEvent_id());

        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }





        //test the API retrofit call
        Response<Integer> response = null;
        try {
            response = this.getInstance().getEndorse(organizer.getOauthToken(),""+organization2.getOrgID(),""+event0.getEvent_id()).execute();
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

        try {
            assertTrue(ret == -2 && this.getDatabaseInstance().isEndorsed(organization2.getOrgID(),event0.getEvent_id()));
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }

    //user story 10
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

    //user story 12
    @Test
    public void testOrgInviteInvalidPermissions(){
        //person trying to invite is not in an org / not in org for request
        //user1 = organizer , user2 = normal user, user3, normal user actual email

        UserModel user1 = new UserModel("Organizer", "Lastname", "organizer@gmail.com");
        UserModel user2 = new UserModel("user2", "Lastname", "user@nonemail.com");
        UserModel user3 = new UserModel("user3", "Lastname", "kevinvosburgh4@gmail.com");
        UserModel user4 = new UserModel("Member", "Lastname", "member@gmail.com");
        OrganizationModel organization = new OrganizationModel(user1,"Organization","1101 3rd street",-1,
                "Test org","organization@noemail.com",user1,null);

        try {
            this.getDatabaseInstance().truncateTables();

            //create in database
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));
            user2.setUserId(this.getDatabaseInstance().createUser("myfacebookidentity", user2.getFirstName(),
                    user2.getLastName(), user2.getEmail(), DatabaseConnection.FACEBOOK));
            user3.setUserId(this.getDatabaseInstance().createUser("myfacebookidentity2", user3.getFirstName(),
                    user3.getLastName(), user3.getEmail(), DatabaseConnection.FACEBOOK));
            user4.setUserId(this.getDatabaseInstance().createUser("myfacebookidentity3", user4.getFirstName(),
                    user4.getLastName(), user4.getEmail(), DatabaseConnection.FACEBOOK));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user1.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user1.getUserId(),ua.getoAuthCode());

            ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user2.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user2.getUserId(),ua.getoAuthCode());

            ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user3.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user3.getUserId(),ua.getoAuthCode());

            ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user4.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user4.getUserId(),ua.getoAuthCode());

            //make user4 a member of org
            this.getDatabaseInstance().addGroupies(user4.getUserId(),organization.getOrgID(),DatabaseConnection.MEMBER,1);


        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call for user
        Response<Integer> response = null;
        try {
            response = this.getInstance().inviteUser(user2.getOauthToken(),"" + organization.getOrgID(), user3.getEmail()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response == null || response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        int responseValue = response.body();

        assertTrue(responseValue != 0);

        //test the API retrofit call for member of org
        response = null;
        try {
            response = this.getInstance().inviteUser(user4.getOauthToken(),"" + organization.getOrgID(), user3.getEmail()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response == null || response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        responseValue = response.body();

        System.out.println("response code is: " + responseValue);

        assertTrue(responseValue != 0);

    }

    @Test
    public void testOrgInviteNotValidEmail(){
        //valid person requesting invalid email

        //person trying to invite is not in an org / member of org
        //user1 = organizer , user2 = normal user

        UserModel user1 = new UserModel("Organizer", "Lastname", "organizer@gmail.com");
        UserModel user2 = new UserModel("user2", "Lastname", "user@nonemail.com");
        OrganizationModel organization = new OrganizationModel(user1,"Organization","1101 3rd street",-1,
                "Test org","organization@noemail.com",user1,null);

        try {
            this.getDatabaseInstance().truncateTables();

            //create in database
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));
            user2.setUserId(this.getDatabaseInstance().createUser("myfacebookidentity", user2.getFirstName(),
                    user2.getLastName(), user2.getEmail(), DatabaseConnection.FACEBOOK));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user1.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user1.getUserId(),ua.getoAuthCode());

            ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user2.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user2.getUserId(),ua.getoAuthCode());


        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call for user
        Response<Integer> response = null;
        try {
            response = this.getInstance().inviteUser(user1.getOauthToken(),"" + organization.getOrgID(), "Invalid@UserEmail").execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response == null || response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        int responseValue = response.body();

        assertTrue(responseValue != 0);
    }

    @Test
    public void testOrgInviteInvalidOrg(){
        //valid person requesting invalid email

        //user1 = organizer , user2 = normal user

        UserModel user1 = new UserModel("Organizer", "Lastname", "organizer@gmail.com");
        UserModel user2 = new UserModel("user2", "Lastname", "user@nonemail.com");
        OrganizationModel organization = new OrganizationModel(user1,"Organization","1101 3rd street",-1,
                "Test org","organization@noemail.com",user1,null);

        try {
            this.getDatabaseInstance().truncateTables();

            //create in database
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));
            user2.setUserId(this.getDatabaseInstance().createUser("myfacebookidentity", user2.getFirstName(),
                    user2.getLastName(), user2.getEmail(), DatabaseConnection.FACEBOOK));

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user1.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user1.getUserId(),ua.getoAuthCode());

            ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            user2.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(user2.getUserId(),ua.getoAuthCode());


        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call for user
        Response<Integer> response = null;
        try {
            response = this.getInstance().inviteUser(user1.getOauthToken(),"" + (organization.getOrgID() + 2), user2.getEmail()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response == null || response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        int responseValue = response.body();

        assertTrue(responseValue != 0);
    }

    //user story 13
    @Test
    public void testRemoveMemberFromOrg() {
        //Create organizer, create 3 users, create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        UserModel user1 = new UserModel("user1", "lastnameUSER1", "user1@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);

        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));
            user1.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", user1.getFirstName(),
                    user1.getLastName(), user1.getEmail(), DatabaseConnection.GOOGLE));;

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            // force users to join organization in database

            this.getDatabaseInstance().addGroupies(user1.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 1);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call

        Response<Integer> response = null;
        try{
            response = this.getInstance().getChangeUserPermissions(organizer.getOauthToken(),""+organization.getOrgID(),""+user1.getUserId(),""+0).execute();
        }
        catch (IOException e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        Integer ret = response.body();
        if(ret != 0 ){
            System.out.println("response issue");
            assertTrue(false);
        }




        ArrayList<Integer>[] response2 = null;
        try {
            response2 = this.getDatabaseInstance().getMembersAndOrganizers(organization.getOrgID());
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        assertTrue( !response2[0].contains(user1));


    }

    @Test
    public void testRemoveOrganizerFromOrg() {
        //Create organizer, create 3 users, create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        UserModel organizer2 = new UserModel("user1", "lastnameUSER1", "user1@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);

        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));
            organizer2.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer2.getFirstName(),
                    organizer2.getLastName(),organizer2.getEmail(), DatabaseConnection.GOOGLE));;

            organization = OrganizationHandler.createOrganization(organization, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            // force users to join organization in database

            this.getDatabaseInstance().addGroupies(organizer2.getUserId(), organization.getOrgID(),
                    DatabaseConnection.ORGANIZER, 1);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call

        Response<Integer> response = null;
        try{
            response = this.getInstance().getChangeUserPermissions(organizer.getOauthToken(),""+organization.getOrgID(),""+organizer2.getUserId(),""+0).execute();
        }
        catch (IOException e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        Integer ret = response.body();
        if(ret != 0 ){
            System.out.println("response issue");
            assertTrue(false);
        }




        ArrayList<Integer>[] response2 = null;
        try {
            response2 = this.getDatabaseInstance().getMembersAndOrganizers(organization.getOrgID());
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        assertTrue( !response2[1].contains(organizer2) && response2[1].size() == 1 && response2[1].contains(organizer.getUserId()));


    }

    @Test
    public void testRemoveLastOrganizerFromOrg() {
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

            // force users to join organization in database

        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call

        Response<Integer> response = null;
        try{
            response = this.getInstance().getChangeUserPermissions(organizer.getOauthToken(),""+organization.getOrgID(),""+organizer.getUserId(),""+0).execute();
        }
        catch (IOException e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        Integer ret = response.body();
        Printing.println(ret);
        assertTrue(ret == -4);



    }

    //user story 15

    @Test
    public void getRequestsToJoinFrom3UnconfirmedUsers() {

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
                    DatabaseConnection.MEMBER, 0);
            this.getDatabaseInstance().addGroupies(user2.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 0);
            this.getDatabaseInstance().addGroupies(user3.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 0);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call
        Response<ArrayList<UserModel>> response = null;
        try {
            response = this.getInstance().getRequestsToJoin(organizer.getOauthToken(),organization.getOrgID()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<UserModel> users = response.body();

        assertTrue(users.contains(user1) && users.contains(user2) && users.contains(user3));


    }

    @Test
    public void respondToJoinRequest() {

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
                    DatabaseConnection.MEMBER, 0);
            this.getDatabaseInstance().addGroupies(user2.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 0);
            this.getDatabaseInstance().addGroupies(user3.getUserId(), organization.getOrgID(),
                    DatabaseConnection.MEMBER, 0);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call

        Response<Integer> response = null;
        try{
            response = this.getInstance().respondToJoinRequest(organizer.getOauthToken(),organization.getOrgID(),user1.getUserId(),true).execute();
        }
        catch (IOException e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        Integer ret = response.body();
        if(ret != 0 ){
            System.out.println("response issue");
            assertTrue(false);
        }




        Response<ArrayList<UserModel>> response2 = null;
        try {
            response2 = this.getInstance().getRequestsToJoin(organizer.getOauthToken(),organization.getOrgID()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response2.body() == null || response2.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<UserModel> users = response2.body();

        assertTrue( users.contains(user2) && users.contains(user3));


    }


    //user story 17
    @Test
    public void testEventsFeed(){
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


        UserModel organizer2 = new UserModel("Organizer2", "Lastname2", "organizer2@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer2,"Organization2","1101 3rd street",-1,"Test org2","organizatio2@noemail.com",organizer2,null);
        OrganizationModel organization2 = new OrganizationModel(organizer2,"Organization3","1101 3rd street",-1,"Test org3","organization3@noemail.com",organizer2,null);

        EventModel event20 = new EventModel("Event 20", organization1.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization1");

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

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());
            event1 = EventHandler.createEvent(event1,this.getDatabaseInstance());
            event2 = EventHandler.createEvent(event2,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            organizer2.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity2", organizer2.getFirstName(),
                    organizer2.getLastName(), organizer2.getEmail(), DatabaseConnection.GOOGLE));

            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua2 = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer2.setOauthToken(ua2.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer2.getUserId(), ua2.getoAuthCode());

            event3.setOrg_id(organization1.getOrgID());
            event4.setOrg_id(organization1.getOrgID());
            event3 = EventHandler.createEvent(event3,this.getDatabaseInstance());
            event4 = EventHandler.createEvent(event4,this.getDatabaseInstance());

            event20.setOrg_id(organization2.getOrgID());

            event20 = EventHandler.createEvent(event20, this.getDatabaseInstance());

            //event 0 = in organization
            //event1 = registered
            UserHandler.registerEvent(organizer.getOauthToken(), event1.getEvent_id(), this.getDatabaseInstance());
            //event20 = endorsed
            OrganizationHandler.endorseEvent(organizer.getOauthToken(), organization.getOrgID(), event20.getEvent_id(), this.getDatabaseInstance());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call for user
        Response<List<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsFeed(organizer.getOauthToken(), null, null).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        if (response == null || response.body() == null || response.code() != 200) {
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        List<EventModel> eventsFeed = response.body();

        assertTrue(!eventsFeed.contains(event1));
        assertTrue(eventsFeed.get(0).equals(event0));
        assertTrue(eventsFeed.get(1).equals(event2));
        assertTrue(eventsFeed.get(2).equals(event20));
        assertTrue(eventsFeed.get(3).equals(event4));
    }

    @Test
    public void testEventsFeedInvalid(){
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


        UserModel organizer2 = new UserModel("Organizer2", "Lastname2", "organizer2@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer2,"Organization2","1101 3rd street",-1,"Test org2","organizatio2@noemail.com",organizer2,null);
        OrganizationModel organization2 = new OrganizationModel(organizer2,"Organization3","1101 3rd street",-1,"Test org3","organization3@noemail.com",organizer2,null);

        EventModel event20 = new EventModel("Event 20", organization1.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization1");

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

            event0 = EventHandler.createEvent(event0,this.getDatabaseInstance());
            event1 = EventHandler.createEvent(event1,this.getDatabaseInstance());
            event2 = EventHandler.createEvent(event2,this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer.setOauthToken(ua.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer.getUserId(),ua.getoAuthCode());

            organizer2.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity2", organizer2.getFirstName(),
                    organizer2.getLastName(), organizer2.getEmail(), DatabaseConnection.GOOGLE));

            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());

            //create oAuthCode for organizer
            UserAuthenticator ua2 = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            organizer2.setOauthToken(ua2.getoAuthCode());
            this.getDatabaseInstance().setOauthCode(organizer2.getUserId(), ua2.getoAuthCode());

            event3.setOrg_id(organization1.getOrgID());
            event4.setOrg_id(organization1.getOrgID());
            event3 = EventHandler.createEvent(event3,this.getDatabaseInstance());
            event4 = EventHandler.createEvent(event4,this.getDatabaseInstance());

            event20.setOrg_id(organization2.getOrgID());

            event20 = EventHandler.createEvent(event20, this.getDatabaseInstance());

            //event 0 = in organization
            //event1 = registered
            UserHandler.registerEvent(organizer.getOauthToken(), event1.getEvent_id(), this.getDatabaseInstance());
            //event20 = endorsed
            OrganizationHandler.endorseEvent(organizer.getOauthToken(), organization.getOrgID(), event20.getEvent_id(), this.getDatabaseInstance());
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
            assertTrue(false);
        }

        //test the API retrofit call for user
        Response<List<EventModel>> response = null;
        try {
            response = this.getInstance().getEventsFeed(null, null, null).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        //when response is back
        List<EventModel> eventsFeed = response.body();

        assertTrue(eventsFeed == null);
    }

    //User story 12
    @Test
    public void testDeleteEventNoAttendance(){
        insertData();
        //event 4
        DatabaseConnection dbc = this.getDatabaseInstance();
        UserModel u3;
        EventModel event4;
        Response<Integer> response = null;
        try {
            u3 = dbc.searchForUser("facebook3", 2);
            u3 = UserHandler.searchForUser(u3.getOauthToken(), null, dbc);
            event4 = EventHandler.searchEventID(4, dbc);
            try {
                response = this.getInstance().getDeleteEvent(u3.getOauthToken(), "" + event4.getEvent_id(), "My message").execute();
            }
            catch (IOException e) {
                e.printStackTrace();
                assertTrue(false);
            }
            //when response is back
            Integer ret = response.body();

            assertTrue(ret == 0);
            EventModel e = EventHandler.searchEventID(event4.getEvent_id(), dbc);
            assertTrue(e == null);
        }
        catch (SQLException e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testDeleteEventWithAttendance(){
        insertData();
        //event 1
        DatabaseConnection dbc = this.getDatabaseInstance();
        UserModel u1;
        UserModel u2;
        UserModel u3;
        EventModel event1;
        Response<Integer> response = null;
        try {
            u1 = dbc.searchForUser("facebook1", 2);
            u1 = UserHandler.searchForUser(u1.getOauthToken(), null, dbc);
            u2 = dbc.searchForUser("facebook2", 2);
            u2 = UserHandler.searchForUser(u2.getOauthToken(), null, dbc);
            u3 = dbc.searchForUser("facebook3", 2);
            u3 = UserHandler.searchForUser(u3.getOauthToken(), null, dbc);
            event1 = EventHandler.searchEventID(1, dbc);

            try {
                response = this.getInstance().getDeleteEvent(u1.getOauthToken(), "" + event1.getEvent_id(), "My message").execute();
            }
            catch (IOException e) {
                e.printStackTrace();
                assertTrue(false);
            }
            //when response is back
            Integer ret = response.body();

            assertTrue(ret == 0);
            EventModel e = EventHandler.searchEventID(event1.getEvent_id(), dbc);
            assertTrue(e == null);
            assertTrue(dbc.searchEventAttendance(u1.getUserId(), event1.getEvent_id()) == null);
            assertTrue(dbc.searchEventAttendance(u2.getUserId(), event1.getEvent_id()) == null);
            assertTrue(dbc.searchEventAttendance(u3.getUserId(), event1.getEvent_id()) == null);

        }
        catch (SQLException e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testDeleteEventInvalidUser(){
        insertData();
        //event 4
        DatabaseConnection dbc = this.getDatabaseInstance();
        UserModel u3;
        EventModel event4;
        Response<Integer> response = null;
        try {
            u3 = dbc.searchForUser("facebook3", 2);
            u3 = UserHandler.searchForUser(u3.getOauthToken(), null, dbc);
            event4 = EventHandler.searchEventID(4, dbc);
            try {
                response = this.getInstance().getDeleteEvent(null, "" + event4.getEvent_id(), "My message").execute();
            }
            catch (IOException e) {
                e.printStackTrace();
                assertTrue(false);
            }
            //when response is back
            Integer ret = response.body();

            assertTrue(ret == -2);
            EventModel e = EventHandler.searchEventID(event4.getEvent_id(), dbc);
            assertTrue(e != null);
        }
        catch (SQLException e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
    @Test
    public void testDeleteEventInvalidEvent(){
        insertData();
        //event 4
        DatabaseConnection dbc = this.getDatabaseInstance();
        UserModel u3;
        EventModel event4;
        Response<Integer> response = null;
        try {
            u3 = dbc.searchForUser("facebook3", 2);
            u3 = UserHandler.searchForUser(u3.getOauthToken(), null, dbc);
            event4 = EventHandler.searchEventID(4, dbc);
            try {
                response = this.getInstance().getDeleteEvent(u3.getOauthToken(), "" + -1, "My message").execute();
            }
            catch (IOException e) {
                e.printStackTrace();
                assertTrue(false);
            }
            //when response is back
            Integer ret = response.body();

            assertTrue(ret == -2);
            EventModel e = EventHandler.searchEventID(event4.getEvent_id(), dbc);
            assertTrue(e != null);
        }
        catch (SQLException e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testDeleteEventInsufficientPermissions(){
        insertData();
        //event 1
        DatabaseConnection dbc = this.getDatabaseInstance();
        UserModel u1;
        UserModel u2;
        UserModel u3;
        EventModel event1;
        Response<Integer> response = null;
        try {
            u1 = dbc.searchForUser("facebook1", 2);
            u1 = UserHandler.searchForUser(u1.getOauthToken(), null, dbc);
            u2 = dbc.searchForUser("facebook2", 2);
            u2 = UserHandler.searchForUser(u2.getOauthToken(), null, dbc);
            u3 = dbc.searchForUser("facebook3", 2);
            u3 = UserHandler.searchForUser(u3.getOauthToken(), null, dbc);
            event1 = EventHandler.searchEventID(1, dbc);

            try {
                response = this.getInstance().getDeleteEvent(u2.getOauthToken(), "" + event1.getEvent_id(), "My message").execute();
            }
            catch (IOException e) {
                e.printStackTrace();
                assertTrue(false);
            }
            //when response is back
            Integer ret = response.body();

            assertTrue(ret == -3);
            EventModel e = EventHandler.searchEventID(event1.getEvent_id(), dbc);
            assertTrue(e != null);
            assertTrue(dbc.searchEventAttendance(u1.getUserId(), event1.getEvent_id()) != null);
            assertTrue(dbc.searchEventAttendance(u2.getUserId(), event1.getEvent_id()) != null);
            assertTrue(dbc.searchEventAttendance(u3.getUserId(), event1.getEvent_id()) != null);

        }
        catch (SQLException e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void searches(){
        DatabaseConnection dbc = this.getDatabaseInstance();

        UserModel u1;
        UserModel u2;
        UserModel u3;
        UserModel noOrgUser;

        OrganizationModel org1;
        OrganizationModel org2;
        OrganizationModel org3;
        OrganizationModel eventlessOrg;

        EventModel event1;
        EventModel event2;
        EventModel event3;
        EventModel event4;

        try {
            //users
            u1 = dbc.searchForUser("facebook1", 2);
            u1 = UserHandler.searchForUser(u1.getOauthToken(), null, dbc);
            u2 = dbc.searchForUser("facebook2", 2);
            u2 = UserHandler.searchForUser(u2.getOauthToken(), null, dbc);
            u3 = dbc.searchForUser("facebook3", 2);
            u3 = UserHandler.searchForUser(u3.getOauthToken(), null, dbc);
            noOrgUser = dbc.searchForUser("facebook4", 2);
            noOrgUser = UserHandler.searchForUser(noOrgUser.getOauthToken(), null, dbc);

            //orgs
            org1 = OrganizationHandler.searchOrgID(1, dbc);
            org2 = OrganizationHandler.searchOrgID(2, dbc);
            org3 = OrganizationHandler.searchOrgID(3, dbc);
            eventlessOrg = OrganizationHandler.searchOrgID(4, dbc);

            //events
            event1 = EventHandler.searchEventID(1, dbc);
            event2 = EventHandler.searchEventID(2, dbc);
            event3 = EventHandler.searchEventID(3, dbc);
            event4 = EventHandler.searchEventID(4, dbc);

            System.out.println("This is a test");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void insertData(){
        try {
            DatabaseConnection dbc = this.getDatabaseInstance();
            dbc.truncateTables();
            //make users
            UserAuthenticator ua1 = FacebookLogin.facebookLogin("facebook1", "first1", "last1", "email1@nomail.com", dbc);
            UserModel u1 = UserHandler.searchForUser(ua1.getoAuthCode(), null, dbc);
            UserAuthenticator ua2 = FacebookLogin.facebookLogin("facebook2", "first2", "last2", "email2@nomail.com", dbc);
            UserModel u2 = UserHandler.searchForUser(ua2.getoAuthCode(), null, dbc);
            UserAuthenticator ua3 = FacebookLogin.facebookLogin("facebook3", "first3", "last3", "email3@nomail.com", dbc);
            UserModel u3 = UserHandler.searchForUser(ua3.getoAuthCode(), null, dbc);
            UserAuthenticator ua4 = FacebookLogin.facebookLogin("facebook4", "no", "org", "email4@nomail.com", dbc);
            UserModel noOrgUser = UserHandler.searchForUser(ua4.getoAuthCode(), null, dbc);

            //make orgs
            OrganizationModel org1 = new OrganizationModel(u1, "Org1", "1101 3rd Street", -1, "Test org 1", "org1@nomail.com", u1, null);
            org1 = OrganizationHandler.createOrganization(org1, dbc);
            OrganizationModel org2 = new OrganizationModel(u2, "Org2", "305 N University St", -1, "Test org 2", "org2@nomail.com", u2, null);
            org2 = OrganizationHandler.createOrganization(org2, dbc);
            OrganizationModel org3 = new OrganizationModel(u3, "Org3", "1090 3rd Street", -1, "Test org 3", "org3@nomail.com", u3, null);
            org3 = OrganizationHandler.createOrganization(org3, dbc);
            OrganizationModel eventlessOrg = new OrganizationModel(u3, "Org4", "250 N University St", -1, "Test org 4", "org4@nomail.com", u3, null);
            eventlessOrg = OrganizationHandler.createOrganization(eventlessOrg, dbc);

            //make events
            EventModel event1 = new EventModel("Event1", org1.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 2000000),"Event 1", "1103 3rd Street West Lafayette, IN 47906", "Org1");
            event1 = EventHandler.createEvent(event1, dbc);
            EventModel event2 = new EventModel("Event2", org2.getOrgID(), new Timestamp(System.currentTimeMillis() + 3000000), new Timestamp(System.currentTimeMillis() + 4000000),"Event 2", "305 N University St West Lafayette, IN 47906", "Org2");
            event2 = EventHandler.createEvent(event2, dbc);
            EventModel event3 = new EventModel("Event3", org3.getOrgID(), new Timestamp(System.currentTimeMillis() + 4000000), new Timestamp(System.currentTimeMillis() + 5000000),"Event 3", "1090 3rd Street West Lafayette, IN 47906", "Org3");
            event3 = EventHandler.createEvent(event3, dbc);
            EventModel event4 = new EventModel("Event4", org3.getOrgID(), new Timestamp(System.currentTimeMillis() + 5000000), new Timestamp(System.currentTimeMillis() + 6000000),"Event 4", "250 N University St West Lafayette, IN 47906", "Org3");
            event4 = EventHandler.createEvent(event4, dbc);

            //add users to other groups
            UserHandler.requestJoinOrg(u2.getOauthToken(), org1.getOrgID(), dbc);
            OrganizationHandler.respondToRequest(u1.getOauthToken(), org1.getOrgID(), u2.getUserId(), true, dbc);
            UserHandler.requestJoinOrg(u3.getOauthToken(), org1.getOrgID(), dbc);
            OrganizationHandler.respondToRequest(u1.getOauthToken(), org1.getOrgID(), u3.getUserId(), true, dbc);
            UserHandler.requestJoinOrg(u3.getOauthToken(), org2.getOrgID(), dbc);
            OrganizationHandler.respondToRequest(u2.getOauthToken(), org2.getOrgID(), u3.getUserId(), true, dbc);
            UserHandler.requestJoinOrg(u1.getOauthToken(), org3.getOrgID(), dbc);
            OrganizationHandler.respondToRequest(u3.getOauthToken(), org3.getOrgID(), u1.getUserId(), true, dbc);

            //set following
            UserHandler.followOrg(u1.getOauthToken(), org2.getOrgID(), dbc);
            UserHandler.followOrg(u2.getOauthToken(), org3.getOrgID(), dbc);
            UserHandler.followOrg(u2.getOauthToken(), eventlessOrg.getOrgID(), dbc);

            //endorse events
            OrganizationHandler.endorseEvent(u1.getOauthToken(), org1.getOrgID(), event2.getEvent_id(), dbc);
            OrganizationHandler.endorseEvent(u1.getOauthToken(), org1.getOrgID(), event4.getEvent_id(), dbc);
            OrganizationHandler.endorseEvent(u2.getOauthToken(), org2.getOrgID(), event1.getEvent_id(), dbc);
            OrganizationHandler.endorseEvent(u3.getOauthToken(), org3.getOrgID(), event2.getEvent_id(), dbc);

            //register for events
            UserHandler.registerEvent(u1.getOauthToken(), event1.getEvent_id(), dbc);
            UserHandler.registerEvent(u2.getOauthToken(), event1.getEvent_id(), dbc);
            UserHandler.registerEvent(u3.getOauthToken(), event1.getEvent_id(), dbc);
            UserHandler.registerEvent(u2.getOauthToken(), event2.getEvent_id(), dbc);
            UserHandler.registerEvent(u3.getOauthToken(), event2.getEvent_id(), dbc);
            UserHandler.registerEvent(u3.getOauthToken(), event3.getEvent_id(), dbc);


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}