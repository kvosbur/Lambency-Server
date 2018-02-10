import static spark.Spark.*;

public class LambencyServer{

    /*
    spark documentation
    http://sparkjava.com/documentation
    localhost:4567/hello
    I made change
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
            Test t = new Test();
            return t.array;
        }, new JsonTransformer());
        get("/User/login/facebook", "application/json", (request, response) -> {
            UserAuthenticator ua = FacebookLogin.facebookLogin(request.queryParams("id"), request.queryParams("first"), request.queryParams("last"), request.queryParams("email"));
            return ua.getoAuthCode();
        }, new JsonTransformer());
    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}