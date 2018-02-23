
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
            Printing.println("TEst");
        }

        port(20000);

        addroutes();
    }

    public void addroutes(){
        // example of responding with a json object made from a java object
        get("/User/login/google", "application/json", (request, response) -> {
            Printing.println("/User/login/google");
            String token = request.queryParams("idToken");
            GoogleLoginHandler glh = new GoogleLoginHandler();
            return glh.getAuthenticator(token);
        }, new JsonTransformer());
        get("/User/search", "application/json", (request, response) -> {
            Printing.println("/User/search");
            String oAuthCode = request.queryParams("oAuthToken");
            String id = request.queryParams("id");
            return UserHandler.searchForUser(oAuthCode,id);
        }, new JsonTransformer());
        post("/User/changeInfo", "application/json", (request, response) -> {
            Printing.println("/User/changeInfo");
            UserModel u = new Gson().fromJson(request.body(), UserModel.class);
            return UserHandler.changeInfo(u);
        }, new JsonTransformer());
        get("/User/followOrg", "application/json", (request, response) -> {
            Printing.println("/User/followOrg");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            Integer ret = UserHandler.followOrg(oAuthCode, Integer.parseInt(orgID));
            return ret;
        }, new JsonTransformer());
        post("/User/requestJoinOrg", "application/json", (request, response) -> {
            Printing.println("/User/requestJoinOrg");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgId");
            int ret = UserHandler.requestJoinOrg(oAuthCode, Integer.parseInt(orgID));
            return ret;
        }, new JsonTransformer());
        get("/User/registerForEvent", "application/json", (request, response) -> {
            Printing.println("/User/registerForEvent");
            String oAuthCode = request.queryParams("oAuthCode");
            String eventID = request.queryParams("eventID");
            Integer ret = UserHandler.registerEvent(oAuthCode, Integer.parseInt(eventID));
            return ret;
        }, new JsonTransformer());
        post("/Organization/create", "application/json",
                (request, response) -> {
                    Printing.println("/Organization/create");
                       return OrganizationHandler.createOrganization( new Gson().fromJson(request.body(), OrganizationModel.class));
                }, new JsonTransformer());
        get("/Organization/search", "application/json", (request, response) -> {
            Printing.println("/Organization/search");
            ArrayList<OrganizationModel> orgList = OrganizationHandler.searchOrgName(request.queryParams("name"));
            return orgList;
        }, new JsonTransformer());
        get("/Organization/searchByID", "application/json", (request, response) -> {
            Printing.println("/Organization/searchByID");
            OrganizationModel organization = OrganizationHandler.searchOrgID(Integer.parseInt(request.queryParams("id")));
            return organization;
        }, new JsonTransformer());
        post("/Event/update", "application/json",
                (request, response) ->{
                    Printing.println("/Event/update");
                    return EventHandler.updateEvent( new Gson().fromJson(request.body(), EventModel.class));
                }
                , new JsonTransformer());
        post("/Event/create", "application/json", (request, response) -> {
                    Printing.println("/Event/create");
                    return EventHandler.createEvent(new Gson().fromJson(request.body(), EventModel.class));
                }
                , new JsonTransformer());
        get("/Event/search","application/json", (request,response)->{
            Printing.println("/Event/search");
            String latStr = request.queryParams("lat");
            String longStr = request.queryParams("long");
            String name = request.queryParams("name");
            String org_idStr = request.queryParams("org_id");
            return EventHandler.getEventsByLocation(Double.parseDouble(latStr), Double.parseDouble(longStr));
        }, new JsonTransformer());
        get("/Event/searchByID", "application/json", (request, response) -> {
            Printing.println("/Event/searchByID");
            EventModel eventModel = EventHandler.searchEventID(Integer.parseInt(request.queryParams("id")));
            return eventModel;
        }, new JsonTransformer());
        get("/Event/searchByIDs", "application/json", (request, response) -> {
            Printing.println("/Event/searchByIDs");
            String userID = request.queryParams("userID");
            String oAuthCode = request.queryParams("oAuthCode");
            List<EventModel> eventModels = EventHandler.searchEventIDS(Integer.parseInt(userID), oAuthCode);
            return eventModels;
        }, new JsonTransformer());
        get("/Event/users","application/json",(request,response)->{
            Printing.println("/Event/users");
            String oauthcode = request.queryParams("oauthcode");
            int event_id = Integer.parseInt(request.queryParams("event_id"));
            return EventHandler.getUsersAttending(oauthcode,event_id);
        }, new JsonTransformer());

        get("/User/login/facebook", "application/json", (request, response) -> {
            Printing.println("/User/login/facebook");
            UserAuthenticator ua = FacebookLogin.facebookLogin(request.queryParams("id"), request.queryParams("first"), request.queryParams("last"), request.queryParams("email"));
            return ua;
        }, new JsonTransformer());
        get("/User/unfollowOrg","application/json",(request, response) -> {
            Printing.println("/User/unfollowOrg");
            return UserHandler.unfollowOrg(request.queryParams("oAuthCode"), Integer.parseInt(request.queryParams("org_id")));
        }, new JsonTransformer());


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