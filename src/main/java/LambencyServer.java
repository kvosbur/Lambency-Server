
import com.google.gson.Gson;

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

        addroutes();
    }

    public void addroutes(){
        // example of responding with a json object made from a java object
        get("/User/login/google", "application/json", (request, response) -> {
            String token = request.queryParams("idToken");
            GoogleLoginHandler glh = new GoogleLoginHandler();
            return glh.getAuthenticator("token");

        }, new JsonTransformer());
        get("/User/followOrg", "application/json", (request, response) -> {
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgId");
            Integer ret = User.followOrg(oAuthCode, orgID);
            return ret;
        }, new JsonTransformer());
        post("/Organization/create", "application/json",
                (request, response) ->
                        OrganizationHandler.createOrganization( new Gson().fromJson(request.body(), Organization.class))
                , new JsonTransformer());
        post("/Organization/upate", "application/json",
                (request, response) ->
                        EventHandler.updateEvent( new Gson().fromJson(request.body(), Event.class))
                , new JsonTransformer());
        post("/Event/create", "application/json", (request, response) ->
                        EventHandler.createEvent( new Gson().fromJson(request.body(), Event.class))
                , new JsonTransformer());

        get("/User/login/facebook", "application/json", (request, response) -> {
            UserAuthenticator ua = FacebookLogin.facebookLogin(request.queryParams("id"), request.queryParams("first"), request.queryParams("last"), request.queryParams("email"));
            return ua.getoAuthCode();
        }, new JsonTransformer());

    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}