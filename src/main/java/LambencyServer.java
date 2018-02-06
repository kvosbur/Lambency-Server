import static spark.Spark.*;

public class LambencyServer{

    /*
    spark documentation
    http://sparkjava.com/documentation
    localhost:4567/hello
    I made change
     */

    LambencyServer(){

        addroutes();
    }

    private void addroutes(){

        // example of responding with a json object made from a java object
        get("/hello", "application/json", (request, response) -> {
            return new Test(request.queryParams("var1"), request.queryParams("var2"));
        }, new JsonTransformer());
    }


    public static void main(String[]args){
        LambencyServer ls = new LambencyServer();

    }
}