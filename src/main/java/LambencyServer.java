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
            GoogleLoginHandler glh = new GoogleLoginHandler();
            return glh.getAuthenticator("token");
        }, new JsonTransformer());
        get("/hola/:name", "application/json", (request, response) -> {
            return new Test(request.params(":name"));
        }, new JsonTransformer());
        get("/hallo/:name/", "application/json", (request, response) -> {
            return new Test(request.params(":name"), request.queryParams("foo"));
        }, new JsonTransformer());
    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}