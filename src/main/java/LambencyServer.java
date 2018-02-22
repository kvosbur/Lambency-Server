
import com.google.gson.Gson;

import java.util.ArrayList;

import static spark.Spark.*;

public class LambencyServer{


    /*
    spark documentation
    http://sparkjava.com/documentation
    localhost:4567/hello
     */

    static DatabaseConnection dbc = null;

    LambencyServer(){

        try {
            LambencyServer.dbc = new DatabaseConnection();
        }catch(Exception e){
            //error happened in connecting to database
            System.out.println("TEst");
        }

        port(20000);

        addroutes();
    }

    public void addroutes(){
        // example of responding with a json object made from a java object
        get("/User/login/google", "application/json", (request, response) -> {
            String token = request.queryParams("idToken");
            GoogleLoginHandler glh = new GoogleLoginHandler();
            return glh.getAuthenticator(token);
        }, new JsonTransformer());
        get("/User/search", "application/json", (request, response) -> {
            String oAuthCode = request.queryParams("oAuthToken");
            String id = request.queryParams("id");
            return UserHandler.searchForUser(oAuthCode,id);
        }, new JsonTransformer());
        post("/User/changeInfo", "application/json", (request, response) -> {
            User u = new Gson().fromJson(request.body(), User.class);
            return UserHandler.changeInfo(u);
        }, new JsonTransformer());
        get("/User/followOrg", "application/json", (request, response) -> {
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgId");
            Integer ret = UserHandler.followOrg(oAuthCode, Integer.parseInt(orgID));
            return ret;
        }, new JsonTransformer());
        post("/User/requestJoinOrg", "application/json", (request, response) -> {
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgId");
            Integer ret = UserHandler.requestJoinOrg(oAuthCode, Integer.parseInt(orgID));
            return ret;
        }, new JsonTransformer());
        get("/User/registerForEvent", "application/json", (request, response) -> {
            String oAuthCode = request.queryParams("oAuthCode");
            String eventID = request.queryParams("eventId");
            Integer ret = UserHandler.registerEvent(oAuthCode, Integer.parseInt(eventID));
            return ret;
        }, new JsonTransformer());
        post("/Organization/create", "application/json",
                (request, response) -> {
                       return OrganizationHandler.createOrganization( new Gson().fromJson(request.body(), Organization.class));
                }, new JsonTransformer());
        get("/Organization/search", "application/json", (request, response) -> {
            ArrayList<Organization> orgList = OrganizationHandler.searchOrgName(request.queryParams("name"));
            return orgList;
        }, new JsonTransformer());
        post("/Event/update", "application/json",
                (request, response) ->
                        EventHandler.updateEvent( new Gson().fromJson(request.body(), EventModel.class))
                , new JsonTransformer());
        post("/Event/create", "application/json", (request, response) ->
                        EventHandler.createEvent( new Gson().fromJson(request.body(), EventModel.class))
                , new JsonTransformer());

        get("/Event/search","application/json", (request,response)->{
            String latStr = request.queryParams("lat");
            String longStr = request.queryParams("long");
            String name = request.queryParams("name");
            String org_idStr = request.queryParams("org_id");
            return EventHandler.getEventsByLocation(Double.parseDouble(latStr), Double.parseDouble(longStr));
        }, new JsonTransformer());
        get("/Event/searchByID", "application/json", (request, response) -> {
            EventModel eventModel = EventHandler.searchEventID(Integer.parseInt(request.queryParams("id")));
            return eventModel;
        }, new JsonTransformer());
        get("/Event/users","application/json",(request,response)->{
            String oauthcode = request.queryParams("oauthcode");
            int event_id = Integer.parseInt(request.queryParams("event_id"));
            return EventHandler.getUsersAttending(oauthcode,event_id);
        }, new JsonTransformer());

        get("/User/login/facebook", "application/json", (request, response) -> {
            UserAuthenticator ua = FacebookLogin.facebookLogin(request.queryParams("id"), request.queryParams("first"), request.queryParams("last"), request.queryParams("email"));
            return ua;
        }, new JsonTransformer());
        get("/User/unfollowOrg","application/json",(request, response) -> UserHandler.unfollowOrg(request.queryParams("oAuthCode"),Integer.parseInt(request.queryParams("org_id"))), new JsonTransformer());


    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();
        /*
        UserAuthenticator ua = FacebookLogin.facebookLogin("id","first", "last", "email@mail.com" );
        EventModel e = new EventModel("Event", 8 , new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3), "description","location", "path", 10,   100, 1200);
        int eventID = EventHandler.createEvent(e);
        UserHandler.registerEvent(ua.getoAuthCode(), eventID);
        */
    }
}