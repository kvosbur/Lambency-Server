import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static spark.Spark.*;

public class LambencyServer{

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
        post("/Organization/create", "application/json", (request, response) -> {

                new Gson().fromJson(request.body(), OrganizationHandler.class);
                return new Test();
        }, new JsonTransformer());
    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}