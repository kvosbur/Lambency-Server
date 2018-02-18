import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//TODO 235 searchForOrg(name)
//TODO 283 modifyGroupies(userid, orgid, type)
//TODO setFollowing, searchForOrg(orgID)
    /*
    TODO:search organization by id
    search for organization by name
    search for event by location
    search for following status
    set following status
     */
public class DatabaseConnection {
    private Connection connect = null;
    public final static int GOOGLE = 1;
    public final static int FACEBOOK = 2;
    public final static int LAMBNECYUSERID = 3;
    public final static int FOLLOW = 0;
    public final static int MEMBER = 1;
    public final static int ORGANIZER = 2;
    DatabaseConnection() throws Exception{
        // This will load the MySQL driver, each DB has its own driver

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        // Setup the connection with the DB
        connect = DriverManager
                .getConnection("jdbc:mysql://localhost:3306/lambencydb", "lambencyuser", "jCAbyTP<?VQ3)dv");

    }


    /**
     * BEGIN USER METHODS
     */


    /**
     * Description: given unique string identifier return matching user object
     @param identifier either represents google user id or facebook user id depending on situation
     @param type whether it is a google or facebook login (static numbers defined at top)

     @return returns user that matches given identifier
     */

    public User searchForUser(String identifier, int type) throws SQLException{

        //figure query to use dependent on identifier type
        String query;
        String fields = "user_id, first_name, last_name, user_email, oauth_token";
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
            return new User(rs.getString(2), rs.getString(3), rs.getString(4), null, null,
                    null, null,rs.getInt(1), 0, rs.getString(5));
        }

        return null;
    }



    /**
     Description: given oauthCode

     @param oauthCode oauthcode for the given user to search

    /**
     Description: given oauthCode

     @param oauthCode oauthcode for the given user to search

     @return returns user that matches given oauthCode
     */

    public User searchForUser(String oauthCode) throws SQLException{

        //create string for query
        String fields = "user_id, first_name, last_name, user_email, oauth_token";
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
            return new User(rs.getString(2), rs.getString(3), rs.getString(4), null, null,
                    null, null,rs.getInt(1), 0, rs.getString(5));
        }
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
        ps = connect.prepareStatement("INSERT INTO Events (name, org_id, start_time, end_time, description, location, event_image) VALUES ('TEMP',?,?,?,?,?,?)");


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
        ResultSet rs = st.executeQuery("SELECT event_id FROM Events WHERE name = 'TEMP'");
        rs.next();
        int event_id = rs.getInt(1);

        //update event with actual name
        ps = connect.prepareStatement("UPDATE Events SET name = ? WHERE event_id = " + event_id);
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

    public void modifyEventInfo(int event_id, String name , Timestamp start, Timestamp end, String description, String location, String imgPath, double lat, double longit) throws SQLException{

        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement("UPDATE Events SET name = ? , start_time = ?, " +
                "end_time = ? , description = ? , location = ? , event_image = ?, latitude = ?, longitude = ? WHERE event_id = ?");

        //set parameters for prepared statement
        ps.setString(1, name);
        ps.setObject(2,start);      // According to google, java should know how to turn a Timestamp object into a dateTime object for the db
        ps.setObject(3,end);
        ps.setString(4,description);
        ps.setString(5, location);
        ps.setString(6, imgPath);
        ps.setDouble(7,lat);
        ps.setDouble(8,longit);
        ps.setInt(9,event_id);

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
        User user = this.searchForUser("" + lambencyId, LAMBNECYUSERID);
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
     * TODO
     * @param userID id of the usere
     * @param orgID id of the organization
     * @return returns 0 on success, -1 on error
     */
    public int setFollowing(int userID, int orgID) throws SQLException{
        return -1;
    }


    /**
     Description: Given user information create a user profile that is either associated with a google or facebook profile

     @param lambencyID userid that is stored in the table
     @param firstName users first name to be changed to
     @param lastName users last name to be changed to
     @param email users email to be changed to

     @return returns User object of updated values
     */

    public User modifyUserInfo(int lambencyID, String firstName, String lastName, String email) throws SQLException{

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
     * @return -1 on non-unique, 0 on unique
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

    public int createEvent(int org_id, String name , Timestamp start, Timestamp end, String description, String location, String imgPath, double latitude, double longitude) throws SQLException{


        //insert event into table
        PreparedStatement ps;
        ps = connect.prepareStatement("INSERT INTO Events (name, org_id, start_time, end_time, description, location, event_img, latitude, longitude) VALUES ('TEMP',?,?,?,?,?,?,?,?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, org_id);
            ps.setObject(2,start);      // According to google, java should know how to turn a Timestamp object into a dateTime object for the db
            ps.setObject(3,end);
            ps.setString(4,description);
            ps.setString(5, location);
            ps.setString(6, imgPath);
            ps.setDouble(7,latitude);
            ps.setDouble(8, longitude);
            ps.execute();

        }else{
            throw new SQLException("Error in SQL database.");
        }

        //get event id from sql table
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT event_id FROM Events WHERE name = 'TEMP'");
        rs.next();
        int event_id = rs.getInt(1);

        //update event with actual name
        ps = connect.prepareStatement("UPDATE Events SET name = ? WHERE event_id = " + event_id);
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
        String query = "SELECT " + fields + " FROM events order by distance";

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
     * Description : Search events by latitude and longitude locations
     *
     * @param eventId Id of event to search for
     *
     * @return Event object of corresponding event id , null otherwise
     */

    public EventModel searchEvents(int eventId) throws SQLException{

        //create string for query
        String fields = "event_id, org_id, name, start_time, end_time, description," +
                "location, event_img, latitude, longitude";
        String query = "SELECT " + fields + " FROM events WHERE event_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        //check for results and if any then return user
        if(rs.next()){
            return new EventModel(rs.getString(3),rs.getInt(2), rs.getTimestamp(4), rs.getTimestamp(5),
                    rs.getString(6), rs.getString(7), rs.getString(8), rs.getInt(1),
                    rs.getDouble(9), rs.getDouble(10));
        }

        return null;
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

        modifyGroupies(organizer_id, orgID, ORGANIZER);
        return orgID;
    }


    /**
     * TODO
     * @param name name of the organization
     * @return null if no results, otherwise the organiztion
     */

    public Organization searchForOrg(String name){

        return null;
    }
      /**
     *  TODO
     * @param orgID id of the organization
     * @return the organization corresponding to the org id
     */
    public Organization searchForOrg(int orgID) throws SQLException{
        return null;
    }

    /**
     * TODO
     * @param user_id the id of the user to be changed
     * @param org_id the id of the organization
     * @param type type to be changed to: FOLLOW, MEMBER, or ORGANIZER
     * @return -1 on failure, else 0
     */

    public int modifyGroupies(int user_id, int org_id, int type){
        return -1;
    }

  
    /**
     * END ORGANIZATION METHODS
     */

    public static void main(String[] args) {
        try {
            DatabaseConnection db = new DatabaseConnection();
            System.out.println("connected successfully");
            /*
            test for unique email
            db.createUser("123", "first", "last", "email.com", FACEBOOK);
            System.out.println(db.verifyUserEmail("email.com"));
            System.out.println(db.verifyUserEmail("unique"));
            */

            /*
            test for searching for events
            Event event = db.searchEvents(2);
            List<Integer> events = db.searchEventsByLocation(110,110);
            for(Integer i: events){
                System.out.println(i);
            }
            db.createEvent(1,"Another Event", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3), "description", "location", "path", 100, 120);
            */
            /*
            test of creation of org
            int result = db.createOrganization("org","this is an org","org@gmail.com", 123, "Purdue", "img", 123);
            db.createOrganization("org2","this is an org","org@gmail.com", 123, "Purdue", "img", 123);
            System.out.println(result);
            /*

            /*
            test insertion of user
            int result = db.createUser("myggoogleidentity", "mock", "user", "dummy@dummy.com", GOOGLE);
            System.out.println(result);
            */

            /*
            test oauth methods and searching for user
            User user = db.searchForUser("myggoogleidentity", GOOGLE);
            User user = db.searchForUser("" + user.getUserId(), LAMBNECYUSERID);
            System.out.println(user.toString());
            UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
            db.setOauthCode(4, ua.getoAuthCode());
            User user = db.searchForUser(ua.gettoAuthCode());
            System.out.println(user.toString());
            */
            /*
            test modifing user data
            User user = db.modifyUserInfo(4, "changedFirst", "changedLast", "changedemail@changed.com");
            System.out.println(user.toString());
            */

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
