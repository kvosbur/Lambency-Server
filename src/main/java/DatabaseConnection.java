import java.sql.Connection;

public class DatabaseConnection {

    private Connection connect = null;

    DatabaseConnection() throws Exception{
        // This will load the MySQL driver, each DB has its own driver
        /*
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        // Setup the connection with the DB
        connect = DriverManager
                .getConnection("jdbc:mysql://localhost:3306/lambencydb", "lambencyuser", "jCAbyTP<?VQ3)dv");
                */

        //"user=lambencyuser&password=jCAbyTP<?VQ3)dv"
    }

    public void searchForUser(String identifier, int type){
        /*
        type:
           1 - google
           2 - facebook
         */


    }

    public void searchForUser(String oathCode){
        /*
        type:
           1 - google
           2 - facebook
         */


    }

    public void createUser(String identifier, String firstName, String lastName, String email, int type){

        /*
        type:
           1 - google
           2 - facebook
         */

        //check if user already exist
    }

    public static void main(String[] args) {
        try {
            DatabaseConnection db = new DatabaseConnection();
            System.out.println("connected successfully");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
