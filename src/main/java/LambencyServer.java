
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
            String oAuthCode = request.queryParams("oAuthCode");
            String id = request.queryParams("id");
            return UserHandler.searchForUser(oAuthCode,id);
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

        get("Event/search","application/json", (request,response)->{
            String latStr = request.queryParams("lat");
            String longStr = request.queryParams("long");
            String name = request.queryParams("name");
            String org_idStr = request.queryParams("org_id");
            return EventHandler.getEventsByLocation(Double.parseDouble(latStr), Double.parseDouble(longStr));
        }, new JsonTransformer());

        get("/User/login/facebook", "application/json", (request, response) -> {
            UserAuthenticator ua = FacebookLogin.facebookLogin(request.queryParams("id"), request.queryParams("first"), request.queryParams("last"), request.queryParams("email"));
            return ua;
        }, new JsonTransformer());
        get("/User/unfollowOrg","application/json",(request, response) -> UserHandler.unfollowOrg(request.queryParams("oAuthCode"),Integer.parseInt(request.queryParams("org_id"))), new JsonTransformer());


    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}