import static spark.Spark.get;
import static spark.Spark.port;

public class LambencyServer{

    static DatabaseConnection dbc = null;

    /*
    go to url localhost:20000/User/login/google   for request
     */

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
            System.out.println("id: " + request.queryParams("id"));
            return new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
        }, new JsonTransformer());

    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}