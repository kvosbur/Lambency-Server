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

}