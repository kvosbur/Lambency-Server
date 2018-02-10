import java.sql.*;

public class DatabaseConnection {

    private Connection connect = null;
    public final static int GOOGLE = 1;
    public final static int FACEBOOK = 2;

    DatabaseConnection() throws Exception{
        // This will load the MySQL driver, each DB has its own driver

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        // Setup the connection with the DB
        connect = DriverManager
                .getConnection("jdbc:mysql://localhost:3306/lambencydb", "lambencyuser", "jCAbyTP<?VQ3)dv");

    }

    public void searchForUser(String identifier, int type){
        /*
        type:
           1 - google
           2 - facebook
         */


    }

    public String searchForUser(String oathCode){
        /*
        type:
           1 - google
           2 - facebook
         */

        return null;
    }

    /**
     Description: Given user information create a user profile that is either associated with a google or facebook profile

     @param identifier either represents google user id or facebook user id depending on situation
     @param firstName users first name
     @param lastName users last name
     @param email users email
     @param type whether it is a google or facebook login (static numbers defined at top)

     @return returns int of lambency user id  on success, NULL on failure
     */

    public int createUser(String identifier, String firstName, String lastName, String email, int type) throws SQLException{

        /*
        type:
           1 - google
           2 - facebook
         */

        //insert user into table
        PreparedStatement ps = null;
        if(type == FACEBOOK) {
            ps = connect.prepareStatement("INSERT INTO user (first_name, last_name, user_email, facebook_id) VALUES ('TEMP',?,?,?)");
        }else if(type == GOOGLE){
            ps = connect.prepareStatement("INSERT INTO user (first_name, last_name, user_email, google_id) VALUES ('TEMP',?,?,?)");
        }

        if(ps != null) {
            //insert values into prepare statement
            ps.setString(1, lastName);
            ps.setString(2, email);
            ps.setString(3, identifier);

            ps.execute();

        }else{
            throw new SQLException("Improper use. Please specify either a google or facebook login");
        }

        //get user id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT user_id FROM user WHERE first_name = 'TEMP'");
        rs.next();
        int lambencyID = rs.getInt(1);

        //update user with actual firstname
        ps = connect.prepareStatement("UPDATE user SET first_name = ? WHERE user_id = " + lambencyID);
        ps.setString(1, firstName);

        ps.executeUpdate();

        return lambencyID;

    }

    public static void main(String[] args) {
        try {
            DatabaseConnection db = new DatabaseConnection();
            System.out.println("connected successfully");

            int result = db.createUser("myggoogleidentity", "mock", "user", "dummy@dummy.com", GOOGLE);
            System.out.println(result);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
