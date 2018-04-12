import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseConnection {
    public Connection connect = null;
    public final static int GOOGLE = 1;
    public final static int FACEBOOK = 2;
    public final static int LAMBNECYUSERID = 3;
    public final static int FOLLOW = 0;
    public final static int MEMBER = 1;
    public final static int ORGANIZER = 2;
    public final static int NOTIFY_EMAIL_PUSH = 0;
    public final static int NOTIFY_EMAIL = 1;
    public final static int NOTIFY_PUSH = 2;
    public final static int NOTIFY_NOT = 3;



    DatabaseConnection(){
        // This will load the MySQL driver, each DB has its own driver

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/lambencydb", "lambencyuser", "jCAbyTP<?VQ3)dv");
        }catch(Exception e){
            connect = null;
            Printing.println("Error opening database connection");
            Printing.println(e.toString());
        }

    }

    public void close(){
        try {
            connect.close();
        }catch(SQLException e){
            Printing.println("Error closing database connection");
            Printing.println(e.toString());
        }
    }


    /**
     * BEGIN USER METHODS
     */

    /**
     * Description: given unique string identifier return matching user object
     @param userID userid of user in question
     @param groupieType what type of relationship to organization looking for

     @return returns array on org_ids that match userid and relationship type
     */

    public ArrayList<Integer> getUserList(int userID, int groupieType, boolean confirmed) throws SQLException{

        //create string for query
        String fields = "org_id";
        String query = "SELECT " + fields + " FROM groupies WHERE user_id = ? and groupies_type = ? and  confirmed = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, userID);
        ps.setInt(2,groupieType);
        ps.setBoolean(3,confirmed);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> array = new ArrayList<>();

        //check for results and return object
        while(rs.next()){
            int org_id = rs.getInt(1);
            array.add(org_id);
        }
        return array;
    }

    /**
     * Description: given unique string identifier return matching user object
     @param identifier either represents google user id or facebook user id depending on situation
     @param type whether it is a google or facebook login (static numbers defined at top)

     @return returns user that matches given identifier
     */

    public UserModel searchForUser(String identifier, int type) throws SQLException{

        //figure query to use dependent on identifier type
        String query;

        String fields = "user_id, first_name, last_name, user_email, oauth_token, hours, notify_pref,active";
        if(type == GOOGLE){
            query = "SELECT " + fields + " FROM user WHERE google_id = ?";
        }else if(type == FACEBOOK) {
            query = "SELECT " + fields + " FROM user WHERE facebook_id = ?";
        }else if(type == LAMBNECYUSERID){
            query = "SELECT " + fields + " FROM user WHERE user_id = ?";
        }else{
            throw new SQLException("Usage error please specifier valid type of identifier");
        }

        // run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, identifier);
        ResultSet rs = ps.executeQuery();

        //check if entry in results and if so create new user object with information
        if(rs.next()){
            return new UserModel(rs.getString(2), rs.getString(3), rs.getString(4), null, null,

                    null, null,null,rs.getInt(1), rs.getInt(6), rs.getString(5), rs.getInt(7),rs.getBoolean(8));

        }

        return null;
    }


    /**
     Description: given oauthCode

     @param oauthCode oauthcode for the given user to search

     @return returns user that matches given oauthCode
     */

    public UserModel searchForUser(String oauthCode) throws SQLException{

        //create string for query

        String fields = "user_id, first_name, last_name, user_email, oauth_token, hours, notify_pref,active";

        String query = "SELECT " + fields + " FROM user WHERE oauth_token = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, oauthCode);
        ResultSet rs = ps.executeQuery();


        //check for results and if any then return user
        if(rs.next()){
            rs.getInt(1);
            rs.getString(2);
            rs.getString(3);
            rs.getString(4);
            rs.getString(5);
            return new UserModel(rs.getString(2), rs.getString(3), rs.getString(4), null, null,

                    null, null,null,rs.getInt(1), rs.getInt(6), rs.getString(5),rs.getInt(7),rs.getBoolean(8));

        }
        return null;
    }

    /**
     *
     * @param oauthCode
     * @param prefernces
     * @return  -2: illegal preference number
     *          0 on success
     *
     */
    public void updateUserNotificationPreferences(String oauthCode, int prefernces) throws SQLException{

        PreparedStatement ps = connect.prepareStatement("UPDATE user SET notify_pref = ? WHERE oauth_token = ?");
        ps.setInt(1,prefernces);
        ps.setString(2,oauthCode);
        ps.executeUpdate();
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
            ps = connect.prepareStatement("INSERT INTO user (first_name, last_name, user_email, facebook_id, notify_pref) VALUES ('TEMP',?,?,?,?)");
        }else if(type == GOOGLE){
            ps = connect.prepareStatement("INSERT INTO user (first_name, last_name, user_email, google_id, notify_pref) VALUES ('TEMP',?,?,?,?)");
        }

        if(ps != null) {
            //insert values into prepare statement
            ps.setString(1, lastName);
            ps.setString(2, email);
            ps.setString(3, identifier);
            ps.setInt(4,NOTIFY_EMAIL_PUSH);
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


    /**
     Description: Given user information create a user profile that is either associated with a google or facebook profile

     @param firstName users first name
     @param lastName users last name
     @param email users email
     @param hash hash of password and salt
     @param salt salt used for user

     @return returns int of lambency user id  on success, NULL on failure
     */

    public int createUser(String firstName, String lastName, String email, String hash, String salt) throws SQLException{

        String sqlColumns = "(user_email, first_name, last_name, hash_password, salt)";

        //insert user into table
        PreparedStatement ps = connect.prepareStatement("INSERT INTO user " + sqlColumns +" VALUES ('TEMP',?,?,?,?)");

        //insert values into prepare statement
        ps.setString(1, firstName);
        ps.setString(2, lastName);
        ps.setString(3, hash);
        ps.setString(4, salt);

        ps.execute();

        //get user id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT user_id FROM user WHERE user_email = 'TEMP'");
        rs.next();
        int lambencyID = rs.getInt(1);

        //update user with actual firstname
        ps = connect.prepareStatement("UPDATE user SET user_email = ? WHERE user_id = " + lambencyID);
        ps.setString(1, email);

        ps.executeUpdate();

        return lambencyID;

    }

    /**
     Description: add user to table to be verified
     @param userid users id in the main users table
     @param code the code to verify that user's email

     @return
     */
    public int userAddVerification(int userid, String code) throws SQLException{

        String sql = "INSERT INTO verify (user_id, verification_code) VALUES (?,?)";
        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement(sql);

        //set parameters for prepared statement
        ps.setInt(1, userid);
        ps.setString(2,code);


        //execute query
        int ret = ps.executeUpdate();
        if(ret == 1){
            return 0;
        }
        return 1;
    }

    /**
     Description: get code that is used for email verification for user
     @param userid users id in the main users table

     @return String of verification code for user
     */
    public String userGetVerification(int userid) throws SQLException{

        String sql = "SELECT verification_code FROM  verify WHERE user_id = ?";
        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement(sql);

        //set parameters for prepared statement
        ps.setInt(1, userid);


        //execute query
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            return rs.getString(1);
        }
        return null;
    }


    /**
     Description: Remove the verification for user from table (means user's email has been verified successfully)
     @param userid users id in the main users table

     @return 0 on success , 1 on failure
     */
    public int userRemoveVerification(int userid) throws SQLException{

        String sql = "DELETE FROM  verify WHERE user_id = ?";
        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement(sql);

        //set parameters for prepared statement
        ps.setInt(1, userid);

        //execute query
        int ret = ps.executeUpdate();

        if(ret == 1){
            return 0;
        }
        return 1;
    }

    /**
     *  Update the active status of the user in the database
     *
     *
     * @param userID            id of the user who is being updated
     * @param isActive          boolean to if they are active or not
     * @return              0 on success or 1 on failure
     * @throws SQLException     Thrown if database issue
     */
    public int setUserActiveStatus(int userID, boolean isActive) throws SQLException{
        String sql = "UPDATE SET active = ? FROM user WHERE user_id = ?";


        PreparedStatement ps = connect.prepareStatement(sql);
        ps.setBoolean(1,isActive);
        ps.setInt(2,userID);

        int ret = ps.executeUpdate();
        if(ret == 1){
            return 0;
        }
        return -1;
    }

    /**
     * Get the status for the active value of the user. Note this returns an integer
     *
     * @param userID            Id of the user who is being queried
     * @return                  0 if false, 1 if true, -1 on failure
     * @throws SQLException
     */
    public int getUserActiveStatus(int userID) throws SQLException{
        String sql = "SELECT active FROM user WHERE user_id = ?";
        PreparedStatement ps = connect.prepareStatement(sql);
        ps.setInt(1,userID);
        ResultSet rs = ps.executeQuery();


        //check for results and if any then return user
        if(rs.next()){
            return rs.getInt(1);
        }
        return -1;

    }

    /**
     *  Description: Creates an event in the database from these elements
     *
     * @param org_id    Integer that represents an organization in this database
     * @param name      String that represents the name of the event
     * @param start     Timestamp that represents the start time of the event
     * @param end       Timestamp that represents the end time of the event
     * @param description   String that represents a description of the events activities
     * @param location  String representing the physical address of the event.
     * @param imgPath   String representing the file_path to the image
     *
     * @return          Returns the int for the event_id
     *
     * @throws SQLException Throws the exception if there is an issue in the database.
     */


    public int createEvent(int org_id, String name , Timestamp start, Timestamp end, String description, String location, String imgPath) throws SQLException{


        //insert event into table
        PreparedStatement ps;
        ps = connect.prepareStatement("INSERT INTO events (name, org_id, start_time, end_time, description, location, event_image) VALUES ('TEMP',?,?,?,?,?,?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, org_id);
            ps.setObject(2,start);      // According to google, java should know how to turn a Timestamp object into a dateTime object for the db
            ps.setObject(3,end);
            ps.setString(4,description);
            ps.setString(5, location);
            ps.setString(6, imgPath);
            ps.execute();

        }else{
            throw new SQLException("Error in SQL database.");
        }

        //get event id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT event_id FROM events WHERE name = 'TEMP'");
        rs.next();
        int event_id = rs.getInt(1);

        //update event with actual name
        ps = connect.prepareStatement("UPDATE events SET name = ? WHERE event_id = " + event_id);
        ps.setString(1, name);

        ps.executeUpdate();

        return event_id;

    }

    /**
     * Updates the event with the id event_id.
     *
     *
     * @param event_id      int id of event that needs to be updated
     * @param name          string name of event
     * @param start         Timestamp of start time of event
     * @param end           Timestamp of end time of event
     * @param description   String description of event
     * @param location      String location of event
     * @param imgPath       String file path to image
     * @throws SQLException Throws if there is an issue with the database
     */


    public void modifyEventInfo(int event_id, String name , Timestamp start, Timestamp end, String description, String location, String imgPath, double lat, double longit, boolean privateEvent) throws SQLException{

        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement("UPDATE events SET name = ? , start_time = ?, " +
                "end_time = ? , description = ? , location = ? , event_img = ?, latitude = ?, longitude = ?, private = ? WHERE event_id = ?");

        //set parameters for prepared statement
        ps.setString(1, name);
        ps.setObject(2,start);      // According to google, java should know how to turn a Timestamp object into a dateTime object for the db
        ps.setObject(3,end);
        ps.setString(4,description);
        ps.setString(5, location);
        ps.setString(6, imgPath);
        ps.setDouble(7,lat);
        ps.setDouble(8,longit);
        ps.setBoolean(9,privateEvent);
        ps.setInt(10,event_id);

        //execute query
        ps.executeUpdate();

    }


    /**
     Description: given unique string identifier return matching user object

     @param lambencyId userId of user
     @param oauthCode string to be set as oauthCode for specified user

     @return true = success, false = failure ( most failures will result in thrown exception )
     */

    public boolean setOauthCode(int lambencyId, String oauthCode) throws SQLException{


        //check to make sure user exists
        UserModel user = this.searchForUser("" + lambencyId, LAMBNECYUSERID);
        if (user == null){
            return false;
        }

        //update user with new oauthCode
        PreparedStatement ps = connect.prepareStatement("UPDATE user SET oauth_token = ? WHERE user_id = " + lambencyId);
        ps.setString(1, oauthCode);
        ps.executeUpdate();

        return true;
    }


    /**
     Description: Given user information create a user profile that is either associated with a google or facebook profile

     @param lambencyID userid that is stored in the table
     @param firstName users first name to be changed to
     @param lastName users last name to be changed to
     @param email users email to be changed to

     @return returns UserModel object of updated values
     */

    public UserModel modifyUserInfo(int lambencyID, String firstName, String lastName, String email) throws SQLException{

        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement("UPDATE user SET first_name = ? , last_name = ?, " +
                "user_email = ? WHERE user_id = ?");

        //set parameters for prepared statement
        ps.setString(1,firstName);
        ps.setString(2,lastName);
        ps.setString(3,email);
        ps.setInt(4,lambencyID);

        //execute query
        ps.executeUpdate();

        return searchForUser("" + lambencyID, LAMBNECYUSERID);

    }

    /**
     *
     * @param email email to be verified to be unique
     * @return -1 on non-unique, 1 on unique
     */

    public int verifyUserEmail(String email) throws SQLException{
        //create string for query
        String fields = "user_id";
        String query = "SELECT " + fields + " FROM user WHERE user_email = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();


        //check for results and if any then return user
        if(rs.next()){
            return -1;
        }
        return 1;
    }


    /**
     * Given an email of a user search for that given user
     * @param email email of user to find
     * @return -2 multiple matches, -1 no matches, otherwise id of match
     */
    public int getUserByEmail(String email) throws SQLException{

        String query = "SELECT user_id from user WHERE user_email = ?";
        PreparedStatement ps = connect.prepareStatement(query);

        ps.setString(1,email);

        ResultSet rs = ps.executeQuery();

        if(rs.next()){
            //found a user with email
            int ret = rs.getInt(1);
            if(rs.next()){
                //there are multiple matches for that email
                return -2;
            }
            return ret;
        }
        //no match found
        return -1;
    }


    /**
     *  Given a user id assign a firebase token to that user
     * @param userID  userid of user to add token to
     * @param firebaseToken  firebase token for specified user
     * @return 1 on success and != 1 on failure
     */
    public int userSetFirebase(int userID, String firebaseToken) throws SQLException{

        String query = "UPDATE user set firebase_token = ? where user_id = ?";
        PreparedStatement ps = connect.prepareStatement(query);

        ps.setString(1,firebaseToken);
        ps.setInt(2, userID);

        int ret = ps.executeUpdate();

        return ret;
    }

    /**
     *  Given a user id get the firebase token for that user
     * @param userID  userid of user to get token from
     * @return firebase token String
     */
    public String userGetFirebase(int userID) throws SQLException{

        String query = "SELECT  firebase_token FROM user WHERE user_id = ?";

        PreparedStatement ps = connect.prepareStatement(query);

        ps.setInt(1, userID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()){
            return rs.getString(1);
        }
        return null;
    }

    /**
     *  Gets the hash and salt for a given user
     * @param userID  userid of user to get info for
     * @return array containing hash and salt where it goes salt and then hash
     */
    public String[] userGetHash(int userID) throws SQLException{

        String query = "SELECT  salt, hash_password FROM user WHERE user_id = ?";

        PreparedStatement ps = connect.prepareStatement(query);

        ps.setInt(1, userID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()){
            String[] strings = new String[2];
            strings[0] = rs.getString(1);
            strings[1] = rs.getString(2);
            return strings;
        }
        return null;
    }


    /**
     *  Sets the hash and salt for a given user
     * @param userID  userid of user to set password for
     * @param hash hash of password to store for user
     * @param salt salt for password hash for user
     * @return integer; 0 on success , 1 on failure
     */
    public int userSetHash(int userID, String hash, String salt) throws SQLException{

        String query = "UPDATE user set salt = ? , hash_password = ?  WHERE user_id = ?";

        PreparedStatement ps = connect.prepareStatement(query);

        ps.setString(1,salt);
        ps.setString(2,hash);
        ps.setInt(3, userID);

        int ret = ps.executeUpdate();

        if(ret == 1){
            return 0;
        }
        return 1;
    }

    /**
     *
     * @param start the start of the range
     * @param end the end of the range
     * @return the list of the user IDs of the corresponding range
     * @throws SQLException
     */
    public List<Integer> leaderboardRange(int start, int end) throws SQLException{
        String fields = "user_id";
        String query = "SELECT " + fields + " FROM user ORDER BY hours DESC LIMIT ?,?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, start - 1);
        ps.setInt(2, end - start + 1);
        ResultSet rs = ps.executeQuery();
        List<Integer> leaderboard = new ArrayList<Integer>();
        while(rs.next()){
            leaderboard.add(rs.getInt(1));
        }
        return leaderboard;
    }

    /**
     *
     * @param userID the id of the user to check for their rank
     * @return the rank of the user, -1 on error
     * @throws SQLException
     */
    public int leaderboardRankOf(int userID) throws SQLException{
        String query = "SELECT hours FROM user WHERE user_id = ?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1,userID);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int ret = rs.getInt(1);// ret = number of hours for user
        query = "SELECT COUNT(*) FROM user WHERE hours > ?";
        ps = connect.prepareStatement(query);
        ps.setInt(1,ret);
        rs = ps.executeQuery();
        rs.next();
        ret = rs.getInt(1);// ret = number of users with more hours than userID
        ret++; // rank of user
        return ret;
    }

    /**
     * adds to the current hours of the user
     * @param userID the id of the user
     * @param hours number of hours to be added
     * @return 0 on success
     * @throws SQLException
     */
    public int addHours(int userID, int hours) throws SQLException{
        if(userID <= 0 || hours < 0){
            return -1;
        }
        String query = "UPDATE user SET hours = hours + ? WHERE user_id = ?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1,hours);
        ps.setInt(2,userID);
        ps.executeUpdate();
        return 0;
    }

    /**
     *
     * @param userID the id of the user
     * @return the list of org ids that have requested the user to join
     * @throws SQLException
     */
    public ArrayList<Integer> getRequestsToJoinOrgs(int userID) throws SQLException{

        String fields = "org_id";
        String query = "SELECT "+ fields +" FROM groupies WHERE user_id = ? AND groupies_type = ? AND confirmed = ?";

        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1,userID);
        ps.setInt(2,DatabaseConnection.MEMBER);
        ps.setInt(3,2);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> orgIDs = new ArrayList<>();

        while(rs.next()){
            orgIDs.add(rs.getInt(1));
        }
        return orgIDs;
    }


    /**
     * END USER METHODS
     */

    /**
     * BEGIN EVENT METHODS
     */

    /**
     *  Description: Creates an event in the database from these elements
     *
     * @param org_id    Integer that represents an organization in this database
     * @param name      String that represents the name of the event
     * @param start     Timestamp that represents the start time of the event
     * @param end       Timestamp that represents the end time of the event
     * @param description   String that represents a description of the events activities
     * @param location  String representing the physical address of the event.
     * @param imgPath   String representing the file_path to the image
     * @param latitude   double representing latitude of location of event
     * @param longitude   double representing longitude of location of event
     *
     * @return          Returns the int for the event_id
     *
     * @throws SQLException Throws the exception if there is an issue in the database.
     */

    public int createEvent(int org_id, String name , Timestamp start, Timestamp end, String description, String location,
                           String imgPath, double latitude, double longitude, String clockInCode, String clockOutCode) throws SQLException{
        Printing.println("using outdated createEvent!!!!!!!!!!!!!");

        //insert event into table
        PreparedStatement ps;
        ps = connect.prepareStatement("INSERT INTO events (name, org_id, start_time, end_time, description, location, " +
                "event_img, latitude, longitude, clock_in_code, clock_out_code) VALUES ('TEMP',?,?,?,?,?,?,?,?,?,?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, org_id);
            ps.setObject(2,start);      // According to google, java should know how to turn a Timestamp object into a dateTime object for the db
            ps.setObject(3,end);
            ps.setString(4,description);
            ps.setString(5, location);
            ps.setString(6, (imgPath == null)?"":imgPath);
            ps.setDouble(7,latitude);
            ps.setDouble(8, longitude);
            ps.setString(9, clockInCode);
            ps.setString(10, clockOutCode);
            ps.execute();

        }else{
            throw new SQLException("Error in SQL database.");
        }

        //get event id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT event_id FROM events WHERE name = 'TEMP'");
        rs.next();
        int event_id = rs.getInt(1);

        //update event with actual name
        ps = connect.prepareStatement("UPDATE events SET name = ? WHERE event_id = " + event_id);
        ps.setString(1, name);

        ps.executeUpdate();

        return event_id;

    }

    public int createEvent(int org_id, String name , Timestamp start, Timestamp end, String description, String location,
                           String imgPath, double latitude, double longitude, String clockInCode, String clockOutCode, boolean privateEvent) throws SQLException{

        //insert event into table
        PreparedStatement ps;
        ps = connect.prepareStatement("INSERT INTO events (name, org_id, start_time, end_time, description, location, " +
                "event_img, latitude, longitude, clock_in_code, clock_out_code, private) VALUES ('TEMP',?,?,?,?,?,?,?,?,?,?,?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, org_id);
            ps.setObject(2,start);      // According to google, java should know how to turn a Timestamp object into a dateTime object for the db
            ps.setObject(3,end);
            ps.setString(4,description);
            ps.setString(5, location);
            ps.setString(6, (imgPath == null)?"":imgPath);
            ps.setDouble(7,latitude);
            ps.setDouble(8, longitude);
            ps.setString(9, clockInCode);
            ps.setString(10, clockOutCode);
            ps.setBoolean(11,privateEvent);
            ps.execute();

        }else{
            throw new SQLException("Error in SQL database.");
        }

        //get event id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT event_id FROM events WHERE name = 'TEMP'");
        rs.next();
        int event_id = rs.getInt(1);

        //update event with actual name
        ps = connect.prepareStatement("UPDATE events SET name = ? WHERE event_id = " + event_id);
        ps.setString(1, name);

        ps.executeUpdate();

        return event_id;

    }


    /**
     * Description : Search events by latitude and longitude locations
     *
     * @param latitude double which is latitude to search by
     * @param longitude double which is the longitude to search by
     *
     * @return List of all event id's in the order of distance to coordinates
     */

    public List<Integer> searchEventsByLocation(double latitude, double longitude) throws SQLException{

        //create string for query
        String fields = "event_id, sqrt(pow(latitude - " + latitude + ",2) + " +
                "pow(longitude - " + longitude + ",2)) as distance";
        String query = "SELECT " + fields + " FROM events order by distance asc";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        //create resulting list
        List<Integer> results = new ArrayList<>();

        //check for results and if any then return user
        while(rs.next()){
            results.add(rs.getInt(1));
        }

        if(results.size() != 0){
            return results;
        }

        return null;
    }

    /**
     *
     * @param efm   EventFilterModel Object that contains all of the constraints for searching for events
     *
     * @return  List<Integers> of event ids on success and null on failure.
     *
     * @throws SQLException
     */

    public List<Integer> searchEventsWithFilterModel(EventFilterModel efm) throws SQLException{
        PreparedStatement ps = connect.prepareStatement(efm.createStringQuery());
        System.out.println(efm.createStringQuery());
        ResultSet rs = ps.executeQuery();

        //create resulting list
        List<Integer> results = new ArrayList<>();

        //check for results and if any then return user
        while(rs.next()){
            results.add(rs.getInt(1));
        }

        if(results.size() != 0){
            return results;
        }

        return null;
    }

    public List<Integer> searchEventsByDateTime(Timestamp start) throws SQLException{
        PreparedStatement ps = connect.prepareStatement("SELECT * FROM events WHERE start_time >= ? ORDER BY start_time");
        ps.setObject(1,start);

        ResultSet rs = ps.executeQuery();

        //create resulting list
        List<Integer> results = new ArrayList<>();

        //check for results and if any then return user
        while(rs.next()){
            results.add(rs.getInt(1));
        }

        if(results.size() != 0){
            return results;
        }
        return null;
    }

    /**
     * Description : Search events by latitude and longitude locations
     *
     * @param eventId Id of event to search for
     *
     * @return Event object of corresponding event id , null otherwise
     */

    public EventModel searchEvents(int eventId) throws SQLException{

        //create string for query
        String fields = "event_id, org_id, name, start_time, end_time, description," +
                "location, event_img, latitude, longitude, clock_in_code, clock_out_code, private";
        String query = "SELECT " + fields + " FROM events WHERE event_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        //check for results and if any then return user
        if(rs.next()){
            OrganizationModel org = searchForOrg(rs.getInt(2));
            EventModel em =  new EventModel(rs.getString(3),rs.getInt(2), rs.getTimestamp(4), rs.getTimestamp(5),
                    rs.getString(6), rs.getString(7), rs.getString(8), rs.getInt(1),
                    rs.getDouble(9), rs.getDouble(10), rs.getString(11), rs.getString(12),
                    org.getName(),rs.getBoolean(13));
            //em.setOrgName(getNameOfOrgForEvent(em.getOrg_id()));
            return em;
        }

        return null;
    }

    /**
     * Returns the EventAttendanceModel object associated with the given userID and eventID
     * @param userID the id of the user to search for
     * @param eventID the id of the event to search for
     * @return EventAttendanceModel object for the corresponding userID and eventId, null if non-existent
     */
    public EventAttendanceModel searchEventAttendance(int userID, int eventID) throws SQLException{

        //create string for query
        String fields = "event_id, user_id, check_in_time, check_out_time";
        String query = "SELECT " + fields + " FROM event_attendence WHERE event_id = ? and user_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ps.setInt(2, userID);
        ResultSet rs = ps.executeQuery();

        //check for results and return object
        if(rs.next()){
            return new EventAttendanceModel(rs.getInt(2), rs.getInt(1),rs.getTimestamp(3), rs.getTimestamp(4));
        }

        return null;
    }

    /**
     * Returns the Events associated with the given userID
     * @param userID the id of the user to search for
     * @return ArrayList<Integer> object for the corresponding userID and eventId, null if non-existent
     */
    public ArrayList<Integer> searchUserEventAttendance(int userID) throws SQLException{

        //create string for query
        String fields = "event_id";
        String query = "SELECT " + fields + " FROM event_attendence WHERE user_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, userID);
        ResultSet rs = ps.executeQuery();


        ArrayList<Integer> array = new ArrayList<>();
        //check for results and return object
        while(rs.next()){
            array.add(rs.getInt(1));
        }

        return array;
    }

    /**
     * Registers a user to an event
     * @param userID the id of the user registering for an event
     * @param eventID the id of the event
     * @return 0 on success, -1 on failure
     */
    public int registerForEvent(int userID, int eventID) throws SQLException{

        //create string for query
        String fields = "(event_id, user_id)";
        String query = "INSERT INTO event_attendence  " + fields + " VALUES (?,?)";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ps.setInt(2, userID);
        int result = ps.executeUpdate();

        if(result > 0){
            return 0;
        }else{
            return -1;
        }
    }

    /**
     * Unregisters a user to an event
     * @param userID the id of the user registering for an event
     * @param eventID the id of the event
     * @return 0 on success, -1 on failure
     */
    public int unRegisterForEvent(int userID, int eventID) throws SQLException{

        //create string for query
        String query = "Delete from event_attendence WHERE event_id = ? AND user_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ps.setInt(2, userID);
        int result = ps.executeUpdate();

        if(result > 0){
            return 0;
        }else{
            return -1;
        }
    }




    /**
     * Searches for all users who are attending the specified event
     * @param eventID the id of the event to search for
     * @param object whether to return UserModels or Integers
     * @return Arraylist of UserModel objects, null if no users found
     */
    public ArrayList<Object> searchEventAttendanceUsers(int eventID, boolean object) throws SQLException{

        //create string for query
        String fields = "user_id";
        String query = "SELECT " + fields + " FROM event_attendence WHERE event_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ResultSet rs = ps.executeQuery();

        ArrayList<Object> users = new ArrayList<>();

        //check for results and return object
        while(rs.next()){
            int userID = rs.getInt(1);
            if(object){
                UserModel user = searchForUser("" + userID, LAMBNECYUSERID);
                if(user != null){
                    users.add(user);
                }
            }else{
                users.add(userID);
            }

        }

        if(users.size() == 0){
            return null;
        }

        return users;
    }

    /**
     *
     * @param eventID the id of the event
     * @return true if a user has clocked in to the event, otherwise false
     * @throws SQLException
     */
    public boolean isClockInForEvent(int eventID) throws SQLException{
        String query = "SELECT check_in_time FROM event_attendence WHERE event_id = ?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            Date date = rs.getDate(1);
            if(date != null){
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the number of users registered for an event
     * @param eventID the id of the event to search for
     * @return Integer representing the number of users attending the event
     */
    public Integer numUsersAttending(int eventID) throws SQLException{

        //create string for query
        String fields = "user_id";
        String query = "SELECT " + fields + " FROM event_attendence WHERE event_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ResultSet rs = ps.executeQuery();

        int counter = 0;


        //check for results and return object
        while(rs.next()){
            int userID = rs.getInt(1);
            UserModel user = searchForUser("" + userID, LAMBNECYUSERID);
            if(user != null){
                counter++;
            }
        }

        return new Integer(counter);
    }



    /**
     * Given a code figure out what event it is for and whether it is a clock in or clock out code
     * @param code string of code to look for matching event
     * @return boolean of whether given code is correct or not
     */
    public List<int[]> findClockInOutCode(String code) throws SQLException{

        String query = "SELECT event_id, clock_in_code, clock_out_code FROM events WHERE clock_in_code = ? or clock_out_code = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, code);
        ps.setString(2, code);
        ResultSet rs = ps.executeQuery();


        List<int[]> response = new ArrayList<>();
        while(rs.next()){
            int[] result = new int[2];
            if(rs.getString(2).equals(code)){
                result[1] = EventAttendanceModel.CLOCKINCODE;
            }else if(rs.getString(3).equals(code)){
                result[1] = EventAttendanceModel.CLOCKOUTCODE;
            }else{
                result[1] = -1;
            }
            result[0] = rs.getInt(1);
            response.add(result);
        }
        if(response.size() != 0){
            return response;
        }
        return null;
    }

    /**
     * Clocks a user to an event for either the start or end of the event
     * @param eventID the id of the event
     * @param userID id of the user to change event attendance for
     * @param startEndTime time to enter
     * @param type whether to enter clock in or clock out
     * @return code of successfulness, 0 = success, 1 = failure
     */
    public int eventClockInOutUser(int eventID, int userID, Timestamp startEndTime, int type) throws SQLException{

        //create string for query
        String field;
        if(type == EventAttendanceModel.CLOCKINCODE) {
            field = "check_in_time";
        }else if(type == EventAttendanceModel.CLOCKOUTCODE){
            field = "check_out_time";
        }else{
            throw new SQLException("please give a valid code type from EventAttendanceModel.");
        }

        String query = "UPDATE event_attendence SET " + field + " = ? WHERE event_id = ? and user_id = ?";

        //run update
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setTimestamp(1,startEndTime);
        ps.setInt(2, eventID);
        ps.setInt(3, userID);
        int result = ps.executeUpdate();
        if(result == 1){
            return 0;
        }
        return 1;
    }

    /**
     * Searches database for events that have ended
     * @param endTime optional argument of end time used (defaults to now)
     * @return list of event_ids of events that have ended
     */
    public ArrayList<Integer> getEventsThatEnded(Timestamp endTime) throws SQLException{

        String query;
        if(endTime != null){
            query = "SELECT event_id FROM events WHERE end_time < ?";
        }else {
            query = "SELECT event_id FROM events WHERE end_time < now()";
        }

        PreparedStatement ps = connect.prepareStatement(query);
        if(endTime != null){
            ps.setTimestamp(1,endTime);
        }
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> events = new ArrayList<>();

        while(rs.next()){
            events.add(rs.getInt(1));
        }
        return events;
    }

    /**
     * Searches database for users that don't have end times for specific events
     * @return list of userIDs that fit criteria
     */
    public ArrayList<Integer> getUsersNoEndTime(int eventID) throws SQLException{

        String query = "SELECT user_id FROM event_attendence WHERE check_in_time IS NOT NULL and check_out_time IS NULL and event_id = ?";

        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> users = new ArrayList<>();

        while(rs.next()){
            users.add(rs.getInt(1));
        }
        return users;
    }

    /**
     * Given an event id move all information for it from current to historical tables
     * @param eventID event id of the event to move to historical tables
     * @return
     */
    public int moveEventToHistorical(int eventID) throws SQLException{

        int result = 0;
        result += eventEntryToHistorical(eventID);
        ArrayList<Object> users = searchEventAttendanceUsers(eventID,false);
        if(users == null){
            return 0;
        }

        for(Object user_id: users){
            Integer id = (Integer)user_id;
            result += eventAttendanceEntryToHistorical(eventID,id);
        }
        return result;
    }

    /**
     * Move the given event from events table to events_historical table
     * @param eventID event id of the event to move to historical tables
     * @return 0 on success, 1 on failure
     */
    public int eventEntryToHistorical(int eventID) throws SQLException{

        //get info from current table
        EventModel event = searchEvents(eventID);
        if(event == null){
            return 1;
        }

        //insert into historical table
        PreparedStatement ps;
        ps = connect.prepareStatement("INSERT INTO events_historical (name, org_id, start_time, end_time, description, location, " +
                "event_img, latitude, longitude, attended, event_id, clock_in_code, clock_out_code) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");

        ps.setString(1,event.getName());
        ps.setInt(2, event.getOrg_id());
        ps.setObject(3,event.getStart());
        ps.setObject(4,event.getEnd());
        ps.setString(5,event.getDescription());
        ps.setString(6, event.getLocation());
        ps.setString(7, event.getImage_path());
        ps.setDouble(8, event.getLattitude());
        ps.setDouble(9, event.getLongitude());
        ps.setInt(10,numUsersAttending(eventID));
        ps.setInt(11,eventID);
        ps.setString(12, event.getClockInCode());
        ps.setString(13, event.getClockOutCode());
        int result = ps.executeUpdate();

        if(result != 1){
            //an error occurred in inserting so return now so that the record doesn't get deleted if not transferred
            return 1;
        }

        //delete from main table
        ps = connect.prepareStatement("DELETE FROM events WHERE event_id = ?");
        ps.setInt(1, eventID);

        result = ps.executeUpdate();
        if(result != 1){
            //wasn't deleted correctly
            return 1;
        }
        return 0;
    }

    /**
     * Move given event attendence row from main table to historical table
     * @param eventID event id of the event to move to historical tables
     * @return 0 on success, 1 on failure
     */
    public int eventAttendanceEntryToHistorical(int eventID, int userID) throws SQLException{

        //get current attendence object
        EventAttendanceModel attendanceModel = searchEventAttendance(userID, eventID);
        if(attendanceModel == null){
            return 1;
        }

        //insert into historical table
        PreparedStatement ps;
        ps = connect.prepareStatement("INSERT INTO event_attendence_historical (event_id, user_id, check_in_time, check_out_time)" +
                " VALUES (?,?,?,?)");
        ps.setInt(1,eventID);
        ps.setInt(2,userID);
        ps.setTimestamp(3,attendanceModel.getStartTime());
        ps.setTimestamp(4, attendanceModel.getEndTime());
        int result = ps.executeUpdate();

        if(result != 1){
            return 1;
        }

        //delete from current table
        ps = connect.prepareStatement("DELETE FROM event_attendence WHERE event_id = ? and user_id = ?");
        ps.setInt(1,eventID);
        ps.setInt(2,userID);
        result = ps.executeUpdate();
        if(result != 1){
            return 1;
        }

        return 0;
    }

    /**
     * Removes the event from the database and the event_attendence for that event
     * @param eventID the id of the event to be deleted
     * @return 0 on success, -1 on failure
     * @throws SQLException
     */
    public int deleteEvent(int eventID) throws SQLException{
        if(searchEvents(eventID) == null){
            return -1;
        }
        PreparedStatement ps;
        int result;
        ps = connect.prepareStatement("DELETE FROM events WHERE event_id = ?");
        ps.setInt(1,eventID);
        ps.executeUpdate();
        ps = connect.prepareStatement("DELETE FROM event_attendence WHERE event_id = ?");
        ps.setInt(1,eventID);
        ps.executeUpdate();
        ps = connect.prepareStatement("DELETE FROM endorse WHERE endorsed_id = ?");
        ps.setInt(1,eventID);
        ps.executeUpdate();
        ps = connect.prepareStatement("DELETE FROM event_attendence WHERE event_id = ?");
        ps.setInt(1,eventID);
        ps.executeUpdate();

        return 0;
    }



    /**
     * END EVENT METHODS
     */

    /**
     * BEGIN ORGANIZATION METHODS
     */
    /**
     *
     * @param name the name of the organization
     * @param description description for the organization
     * @param email email for the organization
     * @param userContact_id id of the contact for the organization
     * @param location the location of the organization
     * @param img_path profile image for the organization
     * @param organizer_id id of the creator of the organization to become an organizer
     *
     * @return  -1 if failure otherwise return org_id
     */

    public int createOrganization(String name, String description, String email, int userContact_id, String location, String img_path, int organizer_id ) throws SQLException{
        Printing.printlnError("Using Organization Creation without LATLONG");
        if(searchForOrg(name) != null){
            return -1;
        }
        PreparedStatement ps = null;
        ps = connect.prepareStatement("INSERT INTO organization (name, description, org_email, `org_ contact`, org_img, org_location, date_created) VALUES ('TEMP',?,?,?,?,?,NOW())");


        if(ps != null) {
            //insert values into prepare statement
            ps.setString(1, description);
            ps.setString(2, email);
            ps.setInt(3, userContact_id);
            ps.setString(4, img_path);
            ps.setString(5, location);
            ps.execute();

        }else{
            throw new SQLException("Improper use. There was an error in creating the SQL statement");
        }

        //get user id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT org_id FROM organization WHERE name = 'TEMP'");
        rs.next();
        int orgID = rs.getInt(1);

        //update user with actual firstname
        ps = connect.prepareStatement("UPDATE organization SET name = ? WHERE org_id = " + orgID);
        ps.setString(1, name);
        ps.executeUpdate();

        addGroupies(organizer_id, orgID, ORGANIZER, 1);
        return orgID;
    }

    public int createOrganization(String name, String description, String email, int userContact_id, String location, String img_path, int organizer_id, double lat, double lng ) throws SQLException{
        if(searchForOrg(name) != null){
            return -1;
        }
        PreparedStatement ps = null;
        ps = connect.prepareStatement("INSERT INTO organization (name, description, org_email, `org_ contact`, org_img, org_location, date_created, latitude, longitude) VALUES ('TEMP',?,?,?,?,?,NOW(),?,?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setString(1, description);
            ps.setString(2, email);
            ps.setInt(3, userContact_id);
            ps.setString(4, img_path);
            ps.setString(5, location);
            ps.setDouble(6,lat);
            ps.setDouble(7,lng);
            ps.execute();

        }else{
            throw new SQLException("Improper use. There was an error in creating the SQL statement");
        }

        //get user id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT org_id FROM organization WHERE name = 'TEMP'");
        rs.next();
        int orgID = rs.getInt(1);

        //update user with actual firstname
        ps = connect.prepareStatement("UPDATE organization SET name = ? WHERE org_id = " + orgID);
        ps.setString(1, name);
        ps.executeUpdate();

        addGroupies(organizer_id, orgID, ORGANIZER, 1);
        return orgID;
    }

    /**
     * modifies the org in the database to organizationModel
     * @param organizationModel
     * @return 0 on success, -1 on error
     * @throws SQLException
     */
    public int modifyOrganization(OrganizationModel organizationModel) throws SQLException{
        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement("UPDATE organization SET name = ?, description = ?, org_email = ?, " +
                        "org_img = ?, org_location = ?, longitude = ?, latitude = ? WHERE org_id = ?");

        //set parameters for prepared statement
        if(ps != null) {
            ps.setString(1, organizationModel.getName());
            ps.setString(2, organizationModel.getDescription());
            ps.setString(3, organizationModel.getEmail());
            ps.setString(4, organizationModel.getImagePath());
            ps.setString(5, organizationModel.getLocation());
            ps.setDouble(6,organizationModel.getLongitude());
            ps.setDouble(7,organizationModel.getLattitude());
            ps.setInt(8,organizationModel.getOrgID());
        }
        else{
            return -1;
        }

        //execute query
        ps.executeUpdate();

        return 0;
    }

    /**
     * sets the organization to deleted in db. makes it unsearchable
     * @param orgID the org id to be set to deleted
     * @return
     * @throws SQLException
     */
    public int deleteOrganization(int orgID) throws SQLException{
        if(searchForOrg(orgID) == null){
            return -1;
        }
        PreparedStatement ps;
        int result;
        ps = connect.prepareStatement("UPDATE organization SET deleted = 1, name = Concat('(Inactive) ', name) WHERE org_id = ?;");
        ps.setInt(1,orgID);
        ps.executeUpdate();

        ps = connect.prepareStatement("DELETE FROM groupies WHERE org_id = ?");
        ps.setInt(1,orgID);
        ps.executeUpdate();

        ps = connect.prepareStatement("SELECT event_id FROM events WHERE org_id = ? AND start_time > now();");
        ps.setInt(1,orgID);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            deleteEvent(rs.getInt(1));
        }

        return 0;
    }

    /**
     * Searches for an organization by name
     * @param name name of the organization
     * @return null if no results, otherwise the organiztion
     */

    public OrganizationModel searchForOrg(String name) throws SQLException{

        //create string for query
        String fields = "org_id, name, description, org_email," +
                "`org_ contact`, org_img, org_location";
        String query = "SELECT " + fields + " FROM organization WHERE name = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();

        //check for results and if any then return user
        if(rs.next()){
            UserModel owner = searchForUser("" + rs.getInt(5), LAMBNECYUSERID);
            if(owner == null){
                return null;
            }
            return new OrganizationModel(owner,rs.getString(2),rs.getString(7), rs.getInt(1),
                    rs.getString(3),rs.getString(4),owner,rs.getString(6));
        }

        return null;
    }

    /**
     * Returns all organizations beginning with the substring 'name'
     * @param name name of the organization
     * @return an arraylist of organizations with a size of 0 or greater.
     */

    public ArrayList<OrganizationModel> searchForOrgArray(String name) throws SQLException{
        ArrayList<OrganizationModel> array = new ArrayList<OrganizationModel>();

        //create string for query
        String fields = "org_id, name, description, org_email," +
                "`org_ contact`, org_img, org_location";
        String query = "SELECT " + fields + " FROM organization WHERE name LIKE ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, name + "%");
        ResultSet rs = ps.executeQuery();

        while(rs.next()){
            UserModel owner = searchForUser("" + rs.getInt(5), LAMBNECYUSERID);
            OrganizationModel organization = new OrganizationModel(owner,rs.getString(2),rs.getString(7), rs.getInt(1),
                    rs.getString(3),rs.getString(4),owner,rs.getString(6));
            array.add(organization);
        }

        return array;
    }


      /**
     *  Search for organization by org_id
     * @param orgID id of the organization
     * @return the organization corresponding to the org id
     */

    public OrganizationModel searchForOrg(int orgID) throws SQLException{

        //create string for query
        String fields = "org_id, name, description, org_email," +
                "`org_ contact`, org_img, org_location";
        String query = "SELECT " + fields + " FROM organization WHERE org_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, orgID);
        ResultSet rs = ps.executeQuery();

        //check for results and if any then return user
        if(rs.next()){
            UserModel owner = searchForUser("" + rs.getInt(5), LAMBNECYUSERID);
            if(owner == null){
                return null;
            }
            return new OrganizationModel(owner,rs.getString(2),rs.getString(7), rs.getInt(1),
                    rs.getString(3),rs.getString(4),owner,rs.getString(6));
        }

        return null;
    }

    /**
     * Add a groupies relationship between a given user and organization
     * @param user_id the id of the user to be added
     * @param org_id the id of the organization
     * @param type type to be changed to: FOLLOW, MEMBER, or ORGANIZER
     * @param confirmed whether the user status is confirmed
     * @return -1 on failure, else 0
     */

    public int addGroupies(int user_id, int org_id, int type, int confirmed) throws SQLException{

        PreparedStatement ps = null;
        ps = connect.prepareStatement("INSERT INTO groupies (org_id, user_id, groupies_type, confirmed)" +
                " VALUES (?,?,?,?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, org_id);
            ps.setInt(2, user_id);
            ps.setInt(3, type);
            ps.setInt(4, confirmed);
            ps.execute();

        }else{
            return -1;
        }
        return 0;
    }


    /**
     * Remove groupie relationship between user and organization
     * @param user_id the id of the user to be deleted
     * @param org_id the id of the organization
     * @param type type to be changed to: FOLLOW, MEMBER, or ORGANIZER
     * @return -1 on failure, else 0
     */

    public int deleteGroupies(int user_id, int org_id, int type) throws SQLException{

        PreparedStatement ps = null;
        ps = connect.prepareStatement("Delete FROM groupies WHERE org_id = ? and user_id = ? and" +
                " groupies_type = ?");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, org_id);
            ps.setInt(2, user_id);
            ps.setInt(3, type);
            ps.executeUpdate();

        }else{
            return -1;
        }
        return 0;
    }

    /**
     * Change groupies type to a new type
     * @param user_id the id of the user to be changed
     * @param org_id the id of the organization
     * @param type type to be changed to: FOLLOW, MEMBER, or ORGANIZER
     * @return -1 on failure, else 0
     */
    public int modifyGroupies(int user_id, int org_id, int type) throws SQLException{
        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement("UPDATE groupies SET groupies_type = ? WHERE user_id = ? and org_id = ?");

        //set parameters for prepared statement
        if(ps != null) {
            ps.setInt(1, type);
            ps.setInt(2, user_id);
            ps.setInt(3, org_id);
        }
        else{
            return -1;
        }

        //execute query
        ps.executeUpdate();

        return 0;
    }


    /**
     * Update Groupie for a member request to an officially joined Member Groupie
     *
     * @param user_id       User id for groupie that needs to be official
     * @param org_id        Org id for Groupie that needs to be official
     * @return              -1 on failure, 0 on success
     * @throws SQLException
     */
    public int approveMemberGroupie(int user_id, int org_id) throws SQLException{
        PreparedStatement ps = connect.prepareStatement("UPDATE groupies SET confirmed = ? WHERE user_id = ? AND org_id = ? AND groupies_type = ?");

        if(ps != null){
            ps.setBoolean(1,true);
            ps.setInt(2,user_id);
            ps.setInt(3,org_id);
            ps.setInt(4, MEMBER);
        }
        else {
            return -1;
        }
        ps.executeUpdate();

        return 0;
    }



    /**
     * DESCRIPTION - search for groupie status for a user and a given organization
     * @param user_id the id of the user to be search for
     * @param org_id the id of the organization
     * @return GroupiesModel a groupies object if exist, else return null
     */

    public GroupiesModel searchGroupies(int user_id, int org_id) throws SQLException{
        //create string for query
        String fields = "org_id, user_id, groupies_type, confirmed";
        String query = "SELECT " + fields + " FROM groupies WHERE user_id = ? and org_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, user_id);
        ps.setInt(2, org_id);
        ResultSet rs = ps.executeQuery();

        //check for results and if any then return user
        if(rs.next()){
            GroupiesModel groupies = new GroupiesModel(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getBoolean(4));
            if(rs.next()){
                throw new SQLException("there are mulitple group entries for user_id = " + user_id);
            }
            return groupies;
        }

        return null;
    }

    /**
     * Description: given unique string identifier return matching user object
     @param orgID id of the organization in question

     @return returns array of event ids that match orgID
     */

    public ArrayList<Integer> getOrgEvents(int orgID) throws SQLException{

        //create string for query
        String fields = "event_id";
        String query = "SELECT " + fields + " FROM events WHERE org_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, orgID);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> array = new ArrayList<>();

        //check for results and return object
        while(rs.next()){
            int event_id = rs.getInt(1);
            array.add(event_id);
        }
        return array;
    }

    public String getNameOfOrgForEvent(int orgID) throws SQLException{
        String query = "SELECT name FROM Organization WHERE org_id = ?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, orgID);
        ResultSet rs = ps.executeQuery();

        return rs.getString(1);
    }


    /**
     * Endorses the given event id for the organization
     * @param orgID the id of the organization
     * @param eventID the event that is being endorsed
     *
     * @return  0 on success, -1 on failure
     */

    public int endorseEvent(int orgID, int eventID) throws SQLException{
        if(orgID < 1 || eventID < 1){
            return -1;
        }
        PreparedStatement ps = null;
        ps = connect.prepareStatement("INSERT INTO endorse (org_id, endorsed_id, endorse_type) VALUES (?, ?, ?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, orgID);
            ps.setInt(2, eventID);
            ps.setBoolean(3, true);
            ps.execute();

        }else{
            throw new SQLException("Improper use. There was an error in creating the SQL statement");
        }

        return 0;
    }

    /**
     *  Unendorses the event for the given org
     * @param orgID the id of the organization
     * @param eventID the event that is being endorsed
     *
     * @return  0 on success, -1 on failure
     */

    public int unendorseEvent(int orgID, int eventID) throws SQLException{
        if(orgID < 1 || eventID < 1){
            return -1;
        }
        PreparedStatement ps = null;
        ps = connect.prepareStatement("Delete FROM endorse WHERE org_id = ? and endorsed_id = ? and" +
                " endorse_type = ?");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, orgID);
            ps.setInt(2, eventID);
            ps.setBoolean(3, true);
            ps.execute();

        }else{
            throw new SQLException("Improper use. There was an error in creating the SQL statement");
        }

        return 0;
    }

    /**
     *  Checks if the org has endorsed the event
     * @param orgID the id of the organization
     * @param eventID the event that is being endorsed
     *
     * @return  true on endorsed, false on not endorsed
     */
    public boolean isEndorsed(int orgID, int eventID) throws SQLException{
        //create string for query
        String fields = "org_id";
        String query = "SELECT " + fields + " FROM endorse WHERE org_id = ? and endorsed_id = ? and endorse_type = ? ";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, orgID);
        ps.setInt(2, eventID);
        ps.setBoolean(3, true);
        ResultSet rs = ps.executeQuery();

        //check for results and return object
        if(rs.next()){
            return true;
        }
        return false;
    }
    /**
     *  Finds all of the events endorsed by a given org
     * @param orgID the id of the organization
     *
     * @return  List of all endorsed events for that org
     */
    public ArrayList<Integer> getEndorsedEvents(int orgID) throws SQLException{

        //create string for query
        String fields = "endorsed_id";
        String query = "SELECT " + fields + " FROM endorse WHERE org_id = ? and endorse_type = ? ";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, orgID);
        ps.setBoolean(2, true);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> array = new ArrayList<>();

        //check for results and return object
        while(rs.next()){
            int event_id = rs.getInt(1);
            array.add(event_id);
        }
        return array;
    }

    /**
     *  Finds all of the orgs that endorsed a given event
     * @param eventID the id of the event
     *
     * @return  List of all orgs that endorsed the event
     */
    public ArrayList<Integer> getEndorsedOrgs(int eventID) throws SQLException{

        //create string for query
        String fields = "org_id";
        String query = "SELECT " + fields + " FROM endorse WHERE endorsed_id = ? and endorse_type = ? ";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ps.setBoolean(2, true);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> array = new ArrayList<>();

        //check for results and return object
        while(rs.next()){
            int org_id = rs.getInt(1);
            array.add(org_id);
        }
        return array;
    }

    public ArrayList<Integer>[] getMembersAndOrganizers( int orgID) throws SQLException{

        String fields = "user_id, groupies_type";
        String query = "SELECT "+ fields +" FROM groupies WHERE org_id = ? AND ( groupies_type = ? OR groupies_type = ?) AND confirmed = ? ORDER BY confirmed asc";

        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1,orgID);
        ps.setInt(2,DatabaseConnection.MEMBER);
        ps.setInt(3,DatabaseConnection.ORGANIZER);
        ps.setBoolean(4,true);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer>[] userIDs = new ArrayList[2];

        ArrayList<Integer> members = new ArrayList<>();
        ArrayList<Integer> organizers = new ArrayList<>();

        while(rs.next()){
            Integer[] i = new Integer[2];
            int id  = rs.getInt(1);
            int type = rs.getInt(2);

            if(type == MEMBER){
                members.add(id);
            }
            else if(type == ORGANIZER){
                organizers.add(id);
            }
        }
        userIDs[0] = members;
        userIDs[1] = organizers;

        // In userIDs the Integer[] at location 0 should be an array with all of the ids for members
        //                          at location 1 should be an array with all ids for organizers
        return userIDs;

    }

    public ArrayList<Integer> getRequestedToJoinUsers(int orgID) throws SQLException{

        String fields = "user_id";
        String query = "SELECT "+ fields +" FROM groupies WHERE org_id = ? AND groupies_type = ? AND confirmed = ?";

        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1,orgID);
        ps.setInt(2,DatabaseConnection.MEMBER);
        ps.setBoolean(3,false);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> userIDs = new ArrayList<>();

        while(rs.next()){
            userIDs.add(rs.getInt(1));
        }
        return userIDs;

    }

    /**
     * Returns the emails of all the users that are following or are members/organizers for the organization given
     * @param orgID the id of the organization to search for
     * @return String array of user Emails
     */
    public ArrayList<String> getUserEmailsToNotify(int orgID) throws SQLException{

        //create string for query
        String query = "SELECT user.user_email FROM groupies INNER JOIN (user) ON (groupies.org_id = ? AND groupies.user_id = user.user_id" +
                " AND groupies.confirmed = TRUE)";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, orgID);
        ResultSet rs = ps.executeQuery();

        ArrayList<String> userEmails = new ArrayList<>();

        //check for results and return object
        while(rs.next()){
            String userEmail = rs.getString(1);
            userEmails.add(userEmail);
        }

        return userEmails;
    }

    public ArrayList<OrganizationModel> searchOrganizationsWithFilterModel(OrganizationFilterModel ofm) throws SQLException{
        PreparedStatement ps = connect.prepareStatement(ofm.createStringQuery());
        ResultSet rs = ps.executeQuery();

        //create resulting list
        ArrayList<OrganizationModel> results = new ArrayList<>();

        //check for results and if any then return user
        while(rs.next()){
            UserModel owner = searchForUser("" + rs.getInt(5), LAMBNECYUSERID);
            if(owner == null){
                return null;
            }
            results.add( new OrganizationModel(owner,rs.getString(2),rs.getString(7), rs.getInt(1),
                    rs.getString(3),rs.getString(4),owner,rs.getString(6)));
        }

        return results;
    }

    /**
     * END ORGANIZATION METHODS
     */

    /**
     * BEGIN UTIL METHODS
     */

    /**
     * This method deletes data from all tables and resets the auto increment ids in the process (For testing purposes)
     * @return -1 if there was an error in deleting one of the tables, 0 on success
     */

    public int truncateTables() throws SQLException{

        //array of tables that exist in database
        String[] tables = {"chat", "endorse", "event_attendence", "events",
            "groupies", "message", "organization", "user"};

        String sql = "TRUNCATE TABLE ";

        int error = 0;
        for(String table:tables){
            Statement statement = connect.createStatement();
            int result = statement.executeUpdate(sql + table);
            if(result == -1){
                error = -1;
            }
        }

        return error;
    }


    /**
     * END UTIL METHODS
     */

    public static void main(String[] args) {
        try {
            DatabaseConnection db = new DatabaseConnection();
            Printing.println("connected successfully");

            int ret = db.getUserByEmail("kvosbur@purdue.edu");
            Printing.println(ret);

            db.close();

            /*
            Test for searching for orgnizations by name
            ArrayList<OrganizationModel> organizations = db.searchForOrgArray("my");
            for(OrganizationModel o: organizations){
                Printing.println(o.name);
            }

            /*
            test for registering for events and searching for attendence
            db.registerForEvent(23,10);
            EventAttendanceModel eventAttendance = db.searchEventAttendance(23,10);
            Printing.println(eventAttendance.getEventID());
            Printing.println(eventAttendance.getUserID());
            */
            /*
            test for create event
            int eventID = db.createEvent(1,"Event", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3), "description", "location", "path",  100, 120);
            int ua = db.createUser("id2", "first", "last", "email@mail.com", 2);
            UserModel u = db.searchForUser("id2", 2);
            UserHandler.registerEvent(u.getOauthToken(), eventID);
            */
            /*
            test for adding / searching groupies
            int a = db.deleteGroupies(21,20, MEMBER);
            int a = db.addGroupies(21,20, MEMBER, false);
            Printing.println(a);
            */
            /*
            test for unique email
            db.createUser("123", "first", "last", "email.com", FACEBOOK);
            Printing.println(db.verifyUserEmail("email.com"));
            Printing.println(db.verifyUserEmail("unique"));
            */

            /*
            test for searching for events
            Event event = db.searchEvents(2);
            List<Integer> events = db.searchEventsByLocation(110,110);
            for(Integer i: events){
                Printing.println(i);
            }
            db.createEvent(1,"Another Event", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3), "description", "location", "path", 100, 120);
            */
            /*
            //test of creation of org
            int result = db.createOrganization("org","this is an org","org@gmail.com", 123, "Purdue", "img", 123);
            db.createOrganization("org2","this is an org","org@gmail.com", 123, "Purdue", "img", 123);
            Printing.println(result);
            */

            /*
            //test insertion of user
            int rs = db.createUser("myggoogleidentity", "mock", "user", "dummy@dummy.com", GOOGLE);
            Printing.println(rs);
            db.addGroupies(4,5,2,true);

            /*
            test oauth methods and searching for user
            UserModel user = db.searchForUser("myggoogleidentity", GOOGLE);
            UserModel user = db.searchForUser("" + user.getUserId(), LAMBNECYUSERID);
            Printing.println(user.toString());
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            db.setOauthCode(4, ua.getoAuthCode());
            UserModel user = db.searchForUser(ua.gettoAuthCode());
            Printing.println(user.toString());
            */
            /*
            test modifing user data
            UserModel user = db.modifyUserInfo(4, "changedFirst", "changedLast", "changedemail@changed.com");
            Printing.println(user.toString());
            */

            /*
            DatabaseConnection dbc = new DatabaseConnection();
            if(dbc == null){
                System.out.println("FAILURE NO DBC");
                return;
            }
            UserAuthenticator ua = FacebookLogin.facebookLogin("id","first", "last", "email@mail.com",dbc );
            //EventModel e = new EventModel("Event", 8 , new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3), "description","location", "path", 10,   100, 1200);
            //int eventID = EventHandler.createEvent(e);
            //UserHandler.registerEvent(ua.getoAuthCode(), eventID);
            try {
                UserModel organizer = dbc.searchForUser("1", DatabaseConnection.LAMBNECYUSERID);

                OrganizationModel org = dbc.searchForOrg(1);

                System.out.println(dbc.addGroupies(organizer.getUserId(),org.getOrgID(),DatabaseConnection.ORGANIZER,true));
                UserModel usr = dbc.searchForUser(""+2,DatabaseConnection.LAMBNECYUSERID);

                System.out.println(dbc.addGroupies(usr.getUserId(),org.getOrgID(),DatabaseConnection.MEMBER,false));

                System.out.println(OrganizationHandler.getRequestedToJoinMembers(organizer.getOauthToken(),org.getOrgID(),dbc));
                System.out.println(dbc.approveMemberGroupie(usr.getUserId(),org.getOrgID()));


            } catch (SQLException e) {
                e.printStackTrace();
            }
            dbc.close();
            */

        }catch(Exception e){
            Printing.println(e.toString());
        }
    }


}
