import static spark.Spark.*;

public class LambencyServer{

    public static void main(String[]args){


        // example of responding with a json object made from a java object
        get("/hello", "application/json", (request, response) -> {
            return new Test();
        }, new JsonTransformer());
        get("/hola/:name", "application/json", (request, response) -> {
            return new Test(request.params(":name"));
        }, new JsonTransformer());
        get("/hallo/:name/", "application/json", (request, response) -> {
            return new Test(request.params(":name"), request.queryParams("foo"));
        }, new JsonTransformer());
    }
}