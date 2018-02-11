import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static spark.Spark.*;

public class LambencyServer{

    DatabaseConnection dbc = null;

    LambencyServer(){

        try {
            this.dbc = new DatabaseConnection();
        }catch(Exception e){
            //error happened in connecting to database
        }

        addroutes();
    }

    public void addroutes(){
        // example of responding with a json object made from a java object
        get("/User/login/google", "application/json", (request, response) -> {
            Test t = new Test();
            return t.array;
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