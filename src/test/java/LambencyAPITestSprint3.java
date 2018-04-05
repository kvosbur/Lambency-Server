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