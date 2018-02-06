import static spark.Spark.*;

public class LambencyServer{

    public static void main(String[]args){


        // example of responding with a json object made from a java object
        get("/hello", "application/json", (request, response) -> {
            return new Test();
        }, new JsonTransformer());
    }
}