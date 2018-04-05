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

public class LambencyAPITestSprint3 {

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
    public void filterOrgByDistance() {
        //Create organizer,  create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer,"Organization1","1101 3rd Street West Lafayette, IN 47906",-1,"Test org","organization@noemail.com",organizer,null);
        OrganizationModel organization2 = new OrganizationModel(organizer,"Organization2","1275 3rd Street West Lafayette, IN 47906",-1,"Test org2","organization2@noemail.com",organizer,null);
        OrganizationModel organization3 = new OrganizationModel(organizer,"Organization3","47403",-1,"Test org3","organization3@noemail.com",organizer,null);
        OrganizationModel organization4 = new OrganizationModel(organizer,"Organization4","63017",-1,"Test org4","organization4@noemail.com",organizer,null);




        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));


            organization4 = OrganizationHandler.createOrganization(organization4, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());
            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization3 = OrganizationHandler.createOrganization(organization3, this.getDatabaseInstance());


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
        Response<ArrayList<OrganizationModel>> response = null;
        try {
            OrganizationFilterModel ofm = new OrganizationFilterModel(40.427126,-86.919638);
            response = this.getInstance().getOrganizationsWithFilter(ofm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println(response.code());
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<OrganizationModel> ret = response.body();
        System.out.println(ret);

        assertTrue(ret.get(0).equals(organization1) && ret.get(1).equals(organization2) && ret.get(2).equals(organization3) && ret.get(3).equals(organization4) );

    }

    @Test
    public void filterOrgByNotUserLocationDistance() {
        //Create organizer,  create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer,"Brian's org","1101 3rd Street West Lafayette, IN 47906",-1,"Test org","organization@noemail.com",organizer,null);
        OrganizationModel organization2 = new OrganizationModel(organizer,"Bill's org","1275 3rd Street West Lafayette, IN 47906",-1,"Test org2","organization2@noemail.com",organizer,null);
        OrganizationModel organization3 = new OrganizationModel(organizer,"Nobody's org","47403",-1,"Test org3","organization3@noemail.com",organizer,null);
        OrganizationModel organization4 = new OrganizationModel(organizer,"Brnady's org","63017",-1,"Test org4","organization4@noemail.com",organizer,null);




        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));


            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());
            organization3 = OrganizationHandler.createOrganization(organization3, this.getDatabaseInstance());
            organization4 = OrganizationHandler.createOrganization(organization4, this.getDatabaseInstance());


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
        Response<ArrayList<OrganizationModel>> response = null;
        try {
            OrganizationFilterModel ofm = new OrganizationFilterModel("B");
            response = this.getInstance().getOrganizationsWithFilter(ofm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println(response.code());
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<OrganizationModel> ret = response.body();
        System.out.println(ret);

        assertTrue(ret.get(0).equals(organization1) && ret.get(1).equals(organization2) && ret.get(2).equals(organization4) && ret.size() == 3 );

    }

    @Test
    public void filterOrgByName() {
        //Create organizer,  create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer,"Organization1","1101 3rd Street West Lafayette, IN 47906",-1,"Test org","organization@noemail.com",organizer,null);
        OrganizationModel organization2 = new OrganizationModel(organizer,"Organization2","1275 3rd Street West Lafayette, IN 47906",-1,"Test org2","organization2@noemail.com",organizer,null);
        OrganizationModel organization3 = new OrganizationModel(organizer,"Organization3","47403",-1,"Test org3","organization3@noemail.com",organizer,null);
        OrganizationModel organization4 = new OrganizationModel(organizer,"Organization4","63017",-1,"Test org4","organization4@noemail.com",organizer,null);




        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));


            organization4 = OrganizationHandler.createOrganization(organization4, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());
            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization3 = OrganizationHandler.createOrganization(organization3, this.getDatabaseInstance());


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
        Response<ArrayList<OrganizationModel>> response = null;
        try {
            OrganizationFilterModel ofm = new OrganizationFilterModel(40.427126,-86.919638);
            response = this.getInstance().getOrganizationsWithFilter(ofm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println(response.code());
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<OrganizationModel> ret = response.body();
        System.out.println(ret);

        assertTrue(ret.get(0).equals(organization1) && ret.get(1).equals(organization2) && ret.get(2).equals(organization3) && ret.get(3).equals(organization4) );

    }

    @Test
    public void filterOrgByDistanceAndName() {
        //Create organizer,  create an organization locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer,"Bob's stuff","1101 3rd Street West Lafayette, IN 47906",-1,"Test org","organization@noemail.com",organizer,null);
        OrganizationModel organization2 = new OrganizationModel(organizer,"Bond","1275 3rd Street West Lafayette, IN 47906",-1,"Test org2","organization2@noemail.com",organizer,null);
        OrganizationModel organization3 = new OrganizationModel(organizer,"Carl","47403",-1,"Test org3","organization3@noemail.com",organizer,null);
        OrganizationModel organization4 = new OrganizationModel(organizer,"Dumons","63017",-1,"Test org4","organization4@noemail.com",organizer,null);




        try {
            this.getDatabaseInstance().truncateTables();



            //create in database
            organizer.setUserId(this.getDatabaseInstance().createUser("myggoogleidentity", organizer.getFirstName(),
                    organizer.getLastName(), organizer.getEmail(), DatabaseConnection.GOOGLE));


            organization4 = OrganizationHandler.createOrganization(organization4, this.getDatabaseInstance());
            organization2 = OrganizationHandler.createOrganization(organization2, this.getDatabaseInstance());
            organization1 = OrganizationHandler.createOrganization(organization1, this.getDatabaseInstance());
            organization3 = OrganizationHandler.createOrganization(organization3, this.getDatabaseInstance());


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
        Response<ArrayList<OrganizationModel>> response = null;
        try {
            OrganizationFilterModel ofm = new OrganizationFilterModel(40.427126,-86.919638,"Bo");
            response = this.getInstance().getOrganizationsWithFilter(ofm).execute();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
        if (response.body() == null || response.code() != 200) {
            System.out.println(response.code());
            System.out.println("ERROR!!!!!");
            assertTrue(false);
        }
        //when response is back
        ArrayList<OrganizationModel> ret = response.body();
        System.out.println(ret);

        assertTrue(ret.get(0).equals(organization1) && ret.get(1).equals(organization2)  );


    }

    @Test
    public void testEventsFeed(){
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 10000000;
        long endTime = currentTime + 1000000;

        //Create organizer, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event0 = new EventModel("Event before timeFrame", organization.getOrgID(), new Timestamp(startTime - 2000000), new Timestamp(startTime - 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event1 = new EventModel("Event starts before and ends during timeFrame", organization.getOrgID(), new Timestamp(startTime - 500000), new Timestamp(currentTime),"Test event 1", "1101 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event2 = new EventModel("Event runs during time Frame", organization.getOrgID(), new Timestamp(startTime), new Timestamp(endTime),"Test event 2", "1102 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event3 = new EventModel("Event starts during and ends after timeFrame", organization.getOrgID(), new Timestamp(currentTime), new Timestamp(endTime + 500000),"Test event 3", "1103 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event4 = new EventModel("Event after timeFrame", organization.getOrgID(), new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 4", "1103 3rd Street West Lafayette, IN 47906", "Organization",false);


        UserModel organizer2 = new UserModel("Organizer2", "Lastname2", "organizer2@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer2,"Organization2","1101 3rd street",-1,"Test org2","organizatio2@noemail.com",organizer2,null);
        OrganizationModel organization2 = new OrganizationModel(organizer2,"Organization3","1101 3rd street",-1,"Test org3","organization3@noemail.com",organizer2,null);

        EventModel event20 = new EventModel("Event 20", organization1.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization1",false);

        //organization 3 will be followed by organizer
        OrganizationModel organization3 = new OrganizationModel(organizer2,"Other","1101 3rd street",-1,"Test org3","organization3@noemail.com",organizer2,null);

        // Event 99 is an open event far away
        EventModel event99 = new EventModel("Event 99", organization3.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "Lafayette, IN", "Organization1",false);

        //Event 87 is a private event
        EventModel event87 = new EventModel("Event 87", organization3.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization1",true);



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



            //organization 3 will be followed by organizer
            organization3 = OrganizationHandler.createOrganization(organization3, this.getDatabaseInstance());

            // Event 99 is an open event far away
            event99.setOrg_id(organization3.getOrgID());
            event87.setOrg_id(organization3.getOrgID());

            event99 = EventHandler.createEvent(event99,this.getDatabaseInstance());
            event87 = EventHandler.createEvent(event87,this.getDatabaseInstance());

            UserHandler.followOrg(organizer.getOauthToken(),organization3.getOrgID(),this.getDatabaseInstance());
            ArrayList<Integer> follow = new ArrayList<>();
            follow.add(organization3.orgID);
            organizer.setFollowingOrgs(follow);

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
        System.out.println(eventsFeed);
        assertTrue(!eventsFeed.contains(event1));
        assertTrue(eventsFeed.size()==5);
        assertTrue(eventsFeed.get(0).equals(event0));
        assertTrue(eventsFeed.get(1).equals(event2));
        assertTrue(eventsFeed.get(2).equals(event20));
        assertTrue(eventsFeed.get(3).equals(event99));
        assertTrue(eventsFeed.get(4).equals(event4));
    }

    @Test
    public void testEventsFeedInvalid(){
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 10000000;
        long endTime = currentTime + 1000000;

        //Create organizer, create organization, and create event locally
        UserModel organizer = new UserModel("Organizer", "Lastname", "organizer@nonemail.com");
        OrganizationModel organization = new OrganizationModel(organizer,"Organization","1101 3rd street",-1,"Test org","organization@noemail.com",organizer,null);
        EventModel event0 = new EventModel("Event before timeFrame", organization.getOrgID(), new Timestamp(startTime - 2000000), new Timestamp(startTime - 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event1 = new EventModel("Event starts before and ends during timeFrame", organization.getOrgID(), new Timestamp(startTime - 500000), new Timestamp(currentTime),"Test event 1", "1101 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event2 = new EventModel("Event runs during time Frame", organization.getOrgID(), new Timestamp(startTime), new Timestamp(endTime),"Test event 2", "1102 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event3 = new EventModel("Event starts during and ends after timeFrame", organization.getOrgID(), new Timestamp(currentTime), new Timestamp(endTime + 500000),"Test event 3", "1103 3rd Street West Lafayette, IN 47906", "Organization",false);
        EventModel event4 = new EventModel("Event after timeFrame", organization.getOrgID(), new Timestamp(endTime + 1000000), new Timestamp(endTime + 2000000),"Test event 4", "1103 3rd Street West Lafayette, IN 47906", "Organization",false);


        UserModel organizer2 = new UserModel("Organizer2", "Lastname2", "organizer2@nonemail.com");
        OrganizationModel organization1 = new OrganizationModel(organizer2,"Organization2","1101 3rd street",-1,"Test org2","organizatio2@noemail.com",organizer2,null);
        OrganizationModel organization2 = new OrganizationModel(organizer2,"Organization3","1101 3rd street",-1,"Test org3","organization3@noemail.com",organizer2,null);

        EventModel event20 = new EventModel("Event 20", organization1.getOrgID(), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 1000000),"Test event 0", "1100 3rd Street West Lafayette, IN 47906", "Organization1",false);




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


}