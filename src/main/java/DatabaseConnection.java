import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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
            return new UserModel(rs.getString(2), rs.getString(3), rs.getString(4), null, null,
                    null, null,null,rs.getInt(1), 0, rs.getString(5));
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
            return new UserModel(rs.getString(2), rs.getString(3), rs.getString(4), null, null,
                    null, null,null,rs.getInt(1), 0, rs.getString(5));
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

    public void modifyEventInfo(int event_id, String name , Timestamp start, Timestamp end, String description, String location, String imgPath, double lat, double longit) throws SQLException{

        //create prepare statement for sql query
        PreparedStatement ps = connect.prepareStatement("UPDATE events SET name = ? , start_time = ?, " +
                "end_time = ? , description = ? , location = ? , event_img = ?, latitude = ?, longitude = ? WHERE event_id = ?");

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
            ps.setString(6, imgPath);
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
     * Description : Search events by latitude and longitude locations
     *
     * @param eventId Id of event to search for
     *
     * @return Event object of corresponding event id , null otherwise
     */

    public EventModel searchEvents(int eventId) throws SQLException{

        //create string for query
        String fields = "event_id, org_id, name, start_time, end_time, description," +
                "location, event_img, latitude, longitude, clock_in_code, clock_out_code";
        String query = "SELECT " + fields + " FROM events WHERE event_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        //check for results and if any then return user
        if(rs.next()){
            return new EventModel(rs.getString(3),rs.getInt(2), rs.getTimestamp(4), rs.getTimestamp(5),
                    rs.getString(6), rs.getString(7), rs.getString(8), rs.getInt(1),
                    rs.getDouble(9), rs.getDouble(10), rs.getString(11), rs.getString(12));
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
        String fields = "event_id, user_id";
        String query = "SELECT " + fields + " FROM event_attendence WHERE event_id = ? and user_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ps.setInt(2, userID);
        ResultSet rs = ps.executeQuery();

        //check for results and return object
        if(rs.next()){
            return new EventAttendanceModel(rs.getInt(2), rs.getInt(1));
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
     * Searches for all users who are attending the specified event
     * @param eventID the id of the event to search for
     * @return Arraylist of UserModel objects, null if no users found
     */
    public ArrayList<UserModel> searchEventAttendanceUsers(int eventID) throws SQLException{

        //create string for query
        String fields = "user_id";
        String query = "SELECT " + fields + " FROM event_attendence WHERE event_id = ?";

        //run query
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, eventID);
        ResultSet rs = ps.executeQuery();

        ArrayList<UserModel> users = new ArrayList<>();

        //check for results and return object
        while(rs.next()){
            int userID = rs.getInt(1);
            UserModel user = searchForUser("" + userID, LAMBNECYUSERID);
            if(user != null){
                users.add(user);
            }
        }

        if(users.size() == 0){
            return null;
        }

        return users;
    }

    /**
     * @TODO
     * @param eventID the id of the event to search for
     * @return boolean of whether given code is correct or not
     */
    public boolean verifyEventClockInOutCode(int eventID, String clockInOutCode, int type) throws SQLException{

        return true;
    }

    /**
     * @TODO
     * @param eventID the id of the event
     * @param userID id of the user to change event attendance for
     * @param startEndTime time to enter
     * @param type whether to enter clock in or clock out
     * @return code of successfulness, 0 = success, 1 = failure
     */
    public int eventClockInOutUser(int eventID, int userID, Timestamp startEndTime, int type) throws SQLException{

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

        addGroupies(organizer_id, orgID, ORGANIZER, true);
        return orgID;
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

    public int addGroupies(int user_id, int org_id, int type, boolean confirmed) throws SQLException{

        PreparedStatement ps = null;
        ps = connect.prepareStatement("INSERT INTO groupies (org_id, user_id, groupies_type, confirmed)" +
                " VALUES (?,?,?,?)");


        if(ps != null) {
            //insert values into prepare statement
            ps.setInt(1, org_id);
            ps.setInt(2, user_id);
            ps.setInt(3, type);
            ps.setBoolean(4, confirmed);
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
            test of creation of org
            int result = db.createOrganization("org","this is an org","org@gmail.com", 123, "Purdue", "img", 123);
            db.createOrganization("org2","this is an org","org@gmail.com", 123, "Purdue", "img", 123);
            Printing.println(result);
            /*

            /*
            test insertion of user
            int result = db.createUser("myggoogleidentity", "mock", "user", "dummy@dummy.com", GOOGLE);
            Printing.println(result);
            */

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

        }catch(Exception e){
            Printing.println(e.toString());
        }
    }


}
