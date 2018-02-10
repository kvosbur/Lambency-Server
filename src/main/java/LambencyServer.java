import static spark.Spark.get;

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
            GoogleLoginHandler glh = new GoogleLoginHandler();
            return glh.getAuthenticator("token");
        }, new JsonTransformer());

    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}