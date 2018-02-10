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
        get("/Organization/create", "application/json", (request, response) -> {
            Test t = new Test();
            return t.array;
        }, new JsonTransformer());
    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

    }
}