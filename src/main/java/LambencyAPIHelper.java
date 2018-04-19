import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LambencyAPIHelper {


    public LambencyAPI getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:4567")
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

    public void facebookLoginRetrofit(String id, String firstName, String lastName, String email){

        this.getInstance().getFacebookLogin(id, firstName, lastName, email).enqueue(new Callback<UserAuthenticator>() {
            @Override
            public void onResponse(Call<UserAuthenticator> call, Response<UserAuthenticator> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                UserAuthenticator ua = response.body();
                String authCode = ua.getoAuthCode();//System.out.println(ua.getoAuthCode());
                //System.out.println(ua.getStatus());
                if(ua.getStatus() == UserAuthenticator.Status.SUCCESS){
                    //System.out.println("SUCCESS");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_DETERMINANT_ERROR){
                    //System.out.println("NON_DETERMINANT_ERROR");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_UNIQUE_EMAIL){
                    //System.out.println("NON_UNIQUE_EMAIL");
                }
            }

            @Override
            public void onFailure(Call<UserAuthenticator> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

//    public List<EventModel> findEventsWithParam(double lattitude, double longitude, String name, int org_id){
//        List<EventModel> events = null;
//
//        try {
//            Response<List<EventModel>> response = this.getInstance().getEventsWithParams(lattitude,longitude,name,Double.toString(org_id)).execute();
//            if(response.isSuccessful()) {
//                events = response.body();
//            }
//            else{
//                System.out.println("failed to gather events");
//            }
//
//        } catch (IOException e) {
//            Printing.println(e.toString());
//        }
//        return events;
//    }


    public void googleLoginRetrofit(String id){

        this.getInstance().getGoogleLogin(id).enqueue(new Callback<UserAuthenticator>() {
            @Override
            public void onResponse(Call<UserAuthenticator> call, Response<UserAuthenticator> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                UserAuthenticator ua = response.body();
                String authCode = ua.getoAuthCode();
                //System.out.println(ua.getoAuthCode());
                //System.out.println(ua.getStatus());
                if(ua.getStatus() == UserAuthenticator.Status.SUCCESS){
                    //System.out.println("SUCCESS");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_DETERMINANT_ERROR){
                    //System.out.println("NON_DETERMINANT_ERROR");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_UNIQUE_EMAIL){
                    //System.out.println("NON_UNIQUE_EMAIL");
                }
            }

            @Override
            public void onFailure(Call<UserAuthenticator> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

public void getOrganizationSearch(String name){

        this.getInstance().getOrganizationSearch(name).enqueue(new Callback<ArrayList<OrganizationModel>>() {
            @Override
            public void onResponse(Call<ArrayList<OrganizationModel>> call, Response<ArrayList<OrganizationModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                ArrayList<OrganizationModel> orgList = response.body();
                if(orgList.size() == 0){
                    //no results found
                }
                else{
                    //results found
                }
            }

            @Override
            public void onFailure(Call<ArrayList<OrganizationModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
}

    public void updateEventRetrofit(EventModel event, String message){

        this.getInstance().getUpdateEvent(event, message).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("successfully updated event");
                }
                else{
                    System.out.println("failed to update event");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

public void getListOfUsersRetrofit(String oAuthCode, int event_id)
{
    this.getInstance().getListOfUsers(oAuthCode, event_id).enqueue(new Callback<ArrayList<UserModel>>() {
        @Override
        public void onResponse(Call<ArrayList<UserModel>> call, Response<ArrayList<UserModel>> response) {
            if (response.body() == null || response.code() != 200) {
                System.out.println("ERROR!!!!!");
            }

            ArrayList<UserModel> userList = response.body();

            if(userList == null)
            {
                //error or no users registered
            }

            else
            {
                //users attending found
            }
        }

        @Override
        public void onFailure(Call<ArrayList<UserModel>> call, Throwable throwable) {
            //when failure
            System.out.println("FAILED CALL");
        }
    });
}

public void joinOrganizationRetrofit(String oAuthCode, int orgId)
{
    this.getInstance().postJoinOrganization(oAuthCode, orgId).enqueue(new Callback<Integer>() {
        @Override
        public void onResponse(Call<Integer> call, Response<Integer> response) {
            if (response.body() == null || response.code() != 200) {
                System.out.println("ERROR!!!!!");
                return;
            }
            //when response is back
            Integer status = response.body();
            System.out.println(status);
            if(status == 0){
                //System.out.println("SUCCESS");
            }
            else if(status == 1){
                //System.out.println("NON DETERMINANT ERROR");
            }
        }

        @Override
        public void onFailure(Call<Integer> call, Throwable throwable) {
            //when failure
            System.out.println("FAILED CALL");
        }
    });
}

    public void eventsByOrgRetrofit(String oAuthCode, String orgId)
    {
        this.getInstance().getEventsByOrg(oAuthCode, orgId).enqueue(new Callback<ArrayList<EventModel>>() {
            @Override
            public void onResponse(Call<ArrayList<EventModel>> call, Response<ArrayList<EventModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                List<EventModel> list = response.body();
                if(list == null){
                    //System.out.println("Org has no events or error has occurred");
                }
                else{
                    //System.out.println("list is a list of events for that org");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<EventModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

public void createOrganizationRetrofit(OrganizationModel org){

        this.getInstance().postCreateOrganization(org).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer status = response.body();
                System.out.println(status);
                if(status == 0){
                    //System.out.println("SUCCESS");
                }
                else if(status == 1){
                    //System.out.println("BAD USER ID");
                }
                else if(status == 2){
                    //System.out.println("NON DETERMINANT ERROR");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void createEventRetofit(EventModel event){
        this.getInstance().createEvent(event).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer status = response.body();
                System.out.println(status);

                if(status == -1){
                    System.out.println("Error in creating event");
                }

                // Status now contains event_id
                int event_id = status;
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void changeAccountInfoRetrofit(UserModel user){

        this.getInstance().getChangeAccountInfo(user).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                UserModel u  = response.body();
                if(u == null){
                    System.out.println("failed: returned null");
                }
                //u.getEmail();
                //updated user object
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void registerEventRetrofit(String oAuthCode, int eventID){

        this.getInstance().getRegisterEvent(oAuthCode, eventID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("Successfully registered for event");
                }
                else if(ret == 1){
                    System.out.println("failed to find user or event");
                }
                else if(ret == 2){
                    System.out.println("failed to register");
                }
                else if(ret == 3){
                    System.out.println("user already registered");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void searchUserRetrofit(String oAuthCode, String id){

        this.getInstance().getUserSearch(oAuthCode, id).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                UserModel u = response.body();
                if(u == null){
                    System.out.println("failed to find user");
                }
                else{
                    System.out.println("first name = " + u.getFirstName());
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void searchEventByIDRetrofit(String event_id){

        this.getInstance().getEventSearchByID(event_id).enqueue(new Callback<EventModel>() {
            @Override
            public void onResponse(Call<EventModel> call, Response<EventModel> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                EventModel eventModel= response.body();
                if(eventModel == null){
                    System.out.println("failed to event");
                }
                else{
                    System.out.println("event description: " + eventModel.getDescription());
                }
            }

            @Override
            public void onFailure(Call<EventModel> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void searchOrgByIDRetrofit(String org_id){

        this.getInstance().getOrgSearchByID(org_id).enqueue(new Callback<OrganizationModel>() {
            @Override
            public void onResponse(Call<OrganizationModel> call, Response<OrganizationModel> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                OrganizationModel organization= response.body();
                if(organization == null){
                    System.out.println("failed to find organization");
                }
                else{
                    System.out.println("organization description: " + organization.getDescription());
                }
            }

            @Override
            public void onFailure(Call<OrganizationModel> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void followOrgRetrofit(String oAuthCode, String orgID){

        this.getInstance().getFollowOrg(oAuthCode, orgID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("successfully followed organization");
                }
                else if (ret == 1){
                    System.out.println("failed to find user or organization");
                }
                else if (ret == 2){
                    System.out.println("undetermined error");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void unfollowOrgRetrofit(String oAuthCode, String orgID){

        this.getInstance().getUnfollowOrg(oAuthCode, orgID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("successfully unfollowed organization");
                }
                else if (ret == 1){
                    System.out.println("failed to find user or organization");
                }
                else if (ret == 2){
                    System.out.println("undetermined error");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void editOrg(String oAuthCode, OrganizationModel organizationModel){

        this.getInstance().getEditOrganization(oAuthCode, organizationModel).enqueue(new Callback<OrganizationModel>() {
            @Override
            public void onResponse(Call<OrganizationModel> call, Response<OrganizationModel> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("failed to update org or invalid permissions");
                }
                //when response is back
                OrganizationModel organization= response.body();
                if(organization == null){
                    System.out.println("failed to update organization");
                }
                else{
                    System.out.println("updated org: " + organization.getDescription());
                }
            }

            @Override
            public void onFailure(Call<OrganizationModel> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void deleteOrg(String oAuthCode, String orgID){

        this.getInstance().getDeleteOrganization(oAuthCode, orgID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("failed to update org or invalid permissions");
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("successfully deleted org");
                }
                else if(ret == -1){
                    System.out.println("failed to delete org: bad params or error occurred");
                }
                else if(ret == -2){
                    System.out.println("user is not an organizer");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }


    public void leaveOrganization(String oAuthCode, int orgID){
        this.getInstance().postLeaveOrganization(oAuthCode,orgID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("successfully left organization");
                }
                else if(ret == 100){
                    System.out.println("Join request canceled");
                }
                else if (ret == -1){
                    System.out.println("Database error caught.");
                }
                else if (ret == 1){
                    System.out.println("User not found");
                }
                else if (ret == 2){
                    System.out.println("Org does not exist");
                }
                else if(ret == 3){
                    System.out.println("Not a member of the organization, so can not leave.");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                System.out.println("FAILED CALL");
            }
        });
    }

    public void numAttendingEventRetrofit(String oAuthCode, String eventId)
    {
        this.getInstance().getEventNumAttending(oAuthCode, eventId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    /**
                     * set number to 0 !!!!!!!!!!
                     */
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == -1){
                    /**
                     * set number to 0 !!!!!!!!!!
                     */
                    System.out.println("Error has occurred");
                }
                else{
                    System.out.println("the number of users attending this event is" + ret);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void getMembersAndOrganizersFromOrg(String oAuthCode, int orgID){
        this.getInstance().getMembersAndOrganizers(oAuthCode,orgID).enqueue(new Callback<ArrayList<UserModel>[]>() {
            @Override
            public void onResponse(Call<ArrayList<UserModel>[]> call, Response<ArrayList<UserModel>[]> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                ArrayList<UserModel>[] users = response.body();
                if(users == null ){
                    System.out.println("Server returned null to getting members and organizers");
                }
                else{
                    ArrayList<UserModel> members = users[0];
                    ArrayList<UserModel> organizers = users[1];
                }
            }

            @Override
            public void onFailure(Call<ArrayList<UserModel>[]> call, Throwable throwable) {
                System.out.println("FAILED CALL in MEMBERS AND ORGANIZERS GETTING");
            }
        });



    }


    public void getRequestsToJoin(String oAuthCode, int orgID){
        this.getInstance().getRequestsToJoin(oAuthCode,orgID).enqueue(new Callback<ArrayList<UserModel>>() {
            @Override
            public void onResponse(Call<ArrayList<UserModel>> call, Response<ArrayList<UserModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                ArrayList<UserModel> users = response.body();
                if(users == null ){
                    System.out.println("Error has occurred");
                }
                else{
                    System.out.println("the number of member join requests is" + users.size());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<UserModel>> call, Throwable throwable) {
                System.out.println("FAILED CALL");
            }
        });
    }






    public void endorseRetrofit(String oAuthCode, String orgId, String eventId)
    {
        this.getInstance().getEndorse(oAuthCode, orgId, eventId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("Success");
                }
                else if(ret == -1){
                    System.out.println("an error has occurred");
                }
                else if(ret == -2){
                    System.out.println("already endorsed");
                }
                else if(ret == -3){
                    System.out.println("invalid arguments");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void unendorseRetrofit(String oAuthCode, String orgId, String eventId)
    {
        this.getInstance().getUnendorse(oAuthCode, orgId, eventId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("Success");
                }
                else if(ret == -1){
                    System.out.println("an error has occurred");
                }
                else if(ret == -2){
                    System.out.println("not endorsed");
                }
                else if(ret == -3){
                    System.out.println("invalid arguments");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void changeUserPermissionsRetrofit(String oAuthCode, String orgId, String userChangedID, String type)
    {
        //type  0 is remove from group
        //      1 is set to member
        //      2 is set to organizer
        this.getInstance().getChangeUserPermissions(oAuthCode, orgId, userChangedID, type).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == 0){
                    System.out.println("Success");
                }
                else if(ret == -1){
                    System.out.println("an error has occurred");
                }
                else if(ret == -2){
                    System.out.println("insufficient permissions");
                }
                else if(ret == -3){
                    System.out.println("invalid arguments");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void eventsFeedRetrofit(String oAuthCode, String latitude, String longitude)
    {
        this.getInstance().getEventsFeed(oAuthCode, latitude, longitude).enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                List<EventModel> eventList = response.body();
                if(eventList == null){
                    System.out.println("An error has occurred");
                }
                else if(eventList.size() == 0){
                    System.out.println("No events founud");
                }
                else{
                    System.out.println("success");
                }
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void respondToJoinRequest(String oAuthCode, int user_id, int org_id, boolean approved){
        this.getInstance().respondToJoinRequest(oAuthCode,org_id,user_id,approved).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("Shit. It messed up.");
                    return;
                }

                Integer returned = response.body();
                if(returned == 0){
                    System.out.println("Yay it worked!!! We have a new member!");
                }
                else if(returned == 1){
                    System.out.println("Yay!! We protected our organization from some crazy person!");
                }
                else{
                    System.out.println("Hmmm... something went wrong... oops");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                System.out.println("We are sorry, we can't handle your response to a join request. Please try turning it off then back on again.");
            }
        });
    }


    public void getMyOrganizedOrgs(String oAuthCode)
    {
        this.getInstance().getMyOrganizedOrgs(oAuthCode).enqueue(new Callback<ArrayList<OrganizationModel>>() {
            @Override
            public void onResponse(Call<ArrayList<OrganizationModel>> call,
                                   Response<ArrayList<OrganizationModel>> response) {

                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                ArrayList<OrganizationModel> ret = response.body();
                if(ret == null){
                    System.out.println("Error");
                }
                else{
                    //use ret here
                }

            }

            @Override
            public void onFailure(Call<ArrayList<OrganizationModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }
    public void endorsedOrgsRetrofit(String oAuthCode, String eventID)
    {
        this.getInstance().getEndorsedOrgs(oAuthCode, eventID).enqueue(new Callback<List<OrganizationModel>>() {
            @Override
            public void onResponse(Call<List<OrganizationModel>> call, Response<List<OrganizationModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred or no organizations were found");
                    return;
                }
                //when response is back
                List<OrganizationModel> orgList = response.body();
                if(orgList == null){
                    System.out.println("An error has occurred");
                }
                else if(orgList.size() == 0){
                    System.out.println("No events founud");
                }
                else{
                    System.out.println("success");
                }
            }

            @Override
            public void onFailure(Call<List<OrganizationModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }
    public void deleteEventRetrofit(String oAuthCode, String eventID, String message)
    {
        this.getInstance().getDeleteEvent(oAuthCode, eventID, message).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == null || ret == -1){
                    System.out.println("An error has occurred");
                }
                else if(ret == -2){
                    System.out.println("User or event not found");
                }
                else if(ret == -3){
                    System.out.println("User does not have permissions to delete the event");
                }
                else if(ret == 0){
                    System.out.println("success");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void updateNotifyPreference(String oAuthCode, int preference){
        this.getInstance().updateNotificationPreference(oAuthCode,preference).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                    return;
                }
                //when response is back
                Integer status = response.body();
                System.out.println(status);
                if(status == 0){
                    //System.out.println("SUCCESS");
                }
                else if(status == -1){
                    //System.out.println("BAD USER ID");
                }
                else if(status == -2){
                    //System.out.println("Illegal preference");
                }
                else if(status == -3){
                    //System.out.println("SQL EXCEPTION");
                }
                else if(status == -4){
                    //System.out.println("BAD SQL Connection");
                }
                else if(status == -5){
                    //System.out.println("Bad arguments passed in retrofit");
                }
            }
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");

            }
        });
    }
    public void leaderboardRangeRetrofit(String start, String end)
    {
        this.getInstance().getLeaderboardRange(start, end).enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred");
                    return;
                }
                //when response is back
                List<UserModel> ret = response.body();
                if(ret == null ) {
                    System.out.println("An error has occurred");
                }
                else{
                    UserModel userModel = ret.get(0);
                    int rank = Integer.parseInt(userModel.getOauthToken());
                    // I will set the oAuthToken to the users rank
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }
    public void leaderboardAroundUserRetrofit(String oAuthCode)
    {
        this.getInstance().getLeaderboardAroundUser(oAuthCode).enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred");
                    return;
                }
                //when response is back
                List<UserModel> ret = response.body();
                if(ret == null ) {
                    System.out.println("An error has occurred");
                }
                else{
                    UserModel userModel = ret.get(0);
                    int rank = Integer.parseInt(userModel.getOauthToken());
                    // I will set the oAuthToken to the users rank
                }
            }

            @Override

            public void onFailure(Call<List<UserModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void userJoinRequests(String oAuthCode)
    {
        this.getInstance().getUserJoinRequests(oAuthCode).enqueue(new Callback<List<OrganizationModel>>() {
            @Override
            public void onResponse(Call<List<OrganizationModel>> call, Response<List<OrganizationModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred");
                    return;
                }
                //when response is back
                List<OrganizationModel> ret = response.body();
                if(ret == null ) {
                    System.out.println("An error has occurred");
                }
                else{
                    for(OrganizationModel org: ret) {
                        System.out.println(org.getName() + " has sent a request to the user");
                    }
                }
            }
            @Override
            public void onFailure(Call<List<OrganizationModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void userRespondToJoinRequests(String oAuthCode, String orgID, boolean accept)
    {
        this.getInstance().getUserRespondToJoinRequest(oAuthCode, orgID, "" + accept).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred");
                    return;
                }
                //when response is back
                Integer ret = response.body();
                if(ret == -1 ) {
                    System.out.println("An error has occurred");
                }
                else if (ret == 0) {
                    System.out.println("Successfully joined org");
                }
                else if (ret == 1) {
                    System.out.println("Successfully rejected request");
                }

            }
            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void pastEvents(String oAuthCode)
    {
        this.getInstance().getPastEvents(oAuthCode).enqueue(new Callback<ArrayList<EventModel>>() {
            @Override
            public void onResponse(Call<ArrayList<EventModel>> call, Response<ArrayList<EventModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred or the user has no past events");
                    // if null is given show that the user has no past events
                    return;
                }
                //when response is back
                ArrayList<EventModel> events = response.body();
                for(EventModel eventModel : events){
                    // i set hours in description
                    double hours = Double.parseDouble(eventModel.getDescription());
                    System.out.println("User worked " + hours + " hours at " + eventModel.getName());
                }

            }
            @Override
            public void onFailure(Call<ArrayList<EventModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public void pastEventsInOrg(String oAuthCode, String userID, String orgID)
    {
        this.getInstance().getPastEventsInOrg(oAuthCode, userID, orgID).enqueue(new Callback<ArrayList<EventModel>>() {
            @Override
            public void onResponse(Call<ArrayList<EventModel>> call, Response<ArrayList<EventModel>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("An error has occurred or the user has no past events or user is not admin of org");
                    // if null is given show that the user has no past events
                    return;
                }
                //when response is back
                ArrayList<EventModel> events = response.body();
                for(EventModel eventModel : events){
                    // i set hours in description
                    double hours = Double.parseDouble(eventModel.getDescription());
                    System.out.println("User worked " + hours + " hours at " + eventModel.getName());
                }

            }
            @Override
            public void onFailure(Call<ArrayList<EventModel>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

//    public static void main(String[] args) {
//        LambencyAPIHelper lh = new LambencyAPIHelper();
//        //OrganizationModel org = new OrganizationModel(null, "Org1", "Purdue", 0, "This is an org", "email@a.com", null, "Img.com");
//        //lh.createOrganizationRetrofit(org);
//        //lh.facebookLoginRetrofit("id", "fist", "last", "email.com");
//        lh.facebookLoginRetrofit("id", "fist", "last", "email.com");
//        System.out.println(lh.findEventsWithParam(0.0,0.0,null,0));
//    }
}
