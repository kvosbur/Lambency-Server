import java.sql.SQLException;
import java.util.ArrayList;

public class UserHandler {
    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param orgID id of organization to be followed
     * @return returns 0 on success, 1 if not able to find user or org or groupies already exists, 2 on SQLException
     */
    public static Integer followOrg(String oAuthCode, int orgID, DatabaseConnection dbc){
        try {
            //search for user
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = dbc.searchGroupies(u.getUserId(), orgID);
                if (g == null) {
                    //if org is found and no groupies already exist, set to follow in database
                    dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW, true);
                    return 0;
                } else {
                    //GroupiesModel already exist for this user
                    Printing.println("GroupiesModel already exists with equal or higher permissions");
                    return 1;
                }
            }
            else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        }
        catch (SQLException e){
            Printing.println("SQLExcpetion");
            return 2;
        }
    }


    /**
     * Unfollow an organization
     *
     * @param oAuthCode     Code of user who made the request
     * @param orgID         Id of organization to unfollow
     * @return              0 if success, 1 if could not locate user or organization or fails to delete, and 2 if SQL failure
     */
    public static Integer unfollowOrg(String oAuthCode, int orgID, DatabaseConnection dbc){
        try {
            //search for user
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = dbc.searchGroupies(u.getUserId(), orgID);
                if (g != null) {
                    //if org is found and a groupie exists delete it if it is a follow
                    // deleteGroupies returns a -1 if failure
                    return -1 * dbc.deleteGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW);
                } else {
                    //GroupiesModel already exist for this user
                    Printing.println("Following of OrganizationModel doesn't exist.");
                    return 1;
                }
            }
            else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        }
        catch (SQLException e){
            Printing.println("SQLExcpetion");
            return 2;
        }
    }


    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param orgID id of organization to request to joi
     * @return returns 0 on success, 1 if not able to find user or org or groupies already exists, 2 on SQLException
     */
    public static Integer requestJoinOrg(String oAuthCode, int orgID, DatabaseConnection dbc) {
        try {
            //search for user
            if (dbc.searchForUser(oAuthCode) == null) {
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = dbc.searchGroupies(u.getUserId(), orgID);
                if (g == null) {
                    //if org is found and no groupies already exist, set to follow in database
                    dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, false);
                    return 0;
                } else {
                    //GroupiesModel already exist for this user
                    if(g.getType() == DatabaseConnection.FOLLOW){
                        //upgrade to a member
                        dbc.deleteGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW);
                        dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, false);
                        return 0;
                    }
                    //user already has higher or equal permissions
                    Printing.println("GroupiesModel already exists with equal or higher permissions");
                    return 1;
                }
            } else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            return 2;
        }
    }


    /**
     *
     * @param oAuthCode String oAuthCode from user
     * @param orgID     Int orgID of the organization you want to leave.
     * @return      -1 if exception is caught, 1 if user not found, 2 if org does not exist, 3 if not a member of organization,
     *              0 if confirmed and deleted, 100 if not confirmed and not deleted;
     */
    public static Integer leaveOrganization(String oAuthCode, int orgID, DatabaseConnection dbc){
        try {
            UserModel usr = dbc.searchForUser(oAuthCode);
            if(usr != null){
                OrganizationModel org = dbc.searchForOrg(orgID);
                if(org != null){
                    GroupiesModel gp = dbc.searchGroupies(usr.getUserId(),orgID);
                    if(gp != null){
                        int toReturn = dbc.deleteGroupies(usr.getUserId(),orgID, gp.getType());
                        if(toReturn == -1){
                            Printing.println("dbc.deleteGroupies returned -1;");
                            return -1;
                        }
                        else if(gp.confirmed){
                            return 0;
                        }
                        else{
                            return 100;
                        }
                    }
                    else{
                        Printing.println("There is no membership with org, so you can not leave the organizaiton.");
                        return 3;
                    }
                }
                else{
                    Printing.println("No organization found.");
                    return 2;
                }
            }
            else{
                Printing.println("No user found.");
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Printing.println("SQL EXCEPTION in Leave Organization");
            return -1;
        }
    }


    /**
     *
     * @param oAuthCode the oAuthCode of the given user
     * @param eventID the id of the event
     * @return 0 on success, 1 on failing to find user or organization, 2 on SQL exception, 3 on already registered
     */
    public static Integer registerEvent(String oAuthCode, int eventID, DatabaseConnection dbc){
        try {
            //search for user
            if (dbc.searchForUser(oAuthCode) == null) {
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            EventModel event = dbc.searchEvents(eventID);
            if (event != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if user is already registered for an event
                EventAttendanceModel ea = dbc.searchEventAttendance(u.getUserId(), eventID);
                if (ea == null) {
                    //if event is found and no event attended already exist, register user for event
                    dbc.registerForEvent(u.getUserId(), eventID);
                    return 0;
                } else {
                    //UserModel is already registered
                    Printing.println("UserModel is already registered for the event");
                    return 3;
                }
            } else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            return 2;
        }
    }

    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param userID id of user to search for
     * @return returns UserModel object on success or null if failure
     */
    public static UserModel searchForUser(String oAuthCode, String userID, DatabaseConnection dbc) {
        try {
            //search for user
            UserModel user = dbc.searchForUser(oAuthCode);
            if (userID != null && user == null) {
                Printing.println("UserModel not found");
                return null;
            }
            //search for organization by ID
            if(userID == null){
                user =  dbc.searchForUser(oAuthCode);
                user = UserHandler.updateOrgLists(user, dbc);
                return user;
            }else{
                user = dbc.searchForUser(userID, DatabaseConnection.LAMBNECYUSERID);
                user = UserHandler.updateOrgLists(user, dbc);
                return user;
            }

        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            Printing.println(e.toString());
            return null;
        }
    }

    /**
     * Changes the account information to the information in the user object
     * @param u the user object with the info to be changed in the database
     * @return updated user object from the database
     */
    public static UserModel changeInfo(UserModel u, DatabaseConnection dbc){
        try {
            if(u == null){
                Printing.println("Null user");
                return null;
            }
            UserModel user = dbc.searchForUser(u.getOauthToken());
            if(user == null){
                Printing.println("UserModel not found");
                return null;
            }
            if(u.getEmail() == null || u.getLastName() == null| u.getFirstName() == null){
                Printing.println("UserModel info invalid");
                return null;
            }
            u = dbc.modifyUserInfo(user.getUserId(), u.getFirstName(), u.getLastName(), u.getEmail());
            u = UserHandler.updateOrgLists(u, dbc);
            return u;
        }
        catch (SQLException e){
            Printing.println("SQLException");
            Printing.println(e.toString());
            return null;
        }
    }

    /**
     * Changes the account information to the information in the user object
     * @param oAuthCode oauthcode of user requesting mylambency info
     * @return updated user object from the database
     */
    public static MyLambencyModel getMyLambency(String oAuthCode, DatabaseConnection dbc){
        try {
            UserModel user = dbc.searchForUser(oAuthCode);
            if(user == null){
                Printing.println("User Model not found");
                return null;
            }
            user = updateOrgLists(user, dbc);
            //create arraylist of organization models from myOrgs also create arraylist of events that user is organizer for
            ArrayList<OrganizationModel> myOrgs = new ArrayList<>();
            ArrayList<EventModel> eventsOrganizing = new ArrayList<>();
            for(Integer i: user.getMyOrgs()){
                OrganizationModel org = dbc.searchForOrg(i);
                if(org != null){
                    if(org.getImage() != null) {
                        org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
                    }
                    myOrgs.add(org);
                    ArrayList<Integer> events = dbc.getOrgEvents(i);
                    for(Integer j: events){
                        EventModel event = dbc.searchEvents(j);
                        if(event != null){
                            event.setImageFile(ImageWR.getEncodedImageFromFile(event.getImage_path()));
                            eventsOrganizing.add(event);
                        }
                    }
                }
            }

            //create arraylist of organization models from joinedOrgs
            ArrayList<OrganizationModel> joinedOrgs = new ArrayList<>();
            for(Integer i: user.getJoinedOrgs()){
                OrganizationModel org = dbc.searchForOrg(i);
                if(org != null){
                    if(org.getImage() != null) {
                        org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
                    }
                    joinedOrgs.add(org);
                }
            }

            //create arraylist of events that user is attending
            ArrayList<EventModel> eventsAttending = new ArrayList<>();
            for(Integer i: user.getEventsAttending()){
                EventModel event = dbc.searchEvents(i);
                if(event != null){
                    event.setImageFile(ImageWR.getEncodedImageFromFile(event.getImage_path()));
                    eventsAttending.add(event);
                }
            }
            return new MyLambencyModel(user,myOrgs,joinedOrgs,eventsAttending,eventsOrganizing);
        }
        catch (SQLException e){
            Printing.println("SQLException");
            Printing.println(e.toString());
            return null;
        }catch(Exception e){
            Printing.println("General Exception");
            Printing.println(e.toString());
            return null;
        }
    }


    /**
     * Changes the account information to the information in the user object
     * @param u the user object to update the arraylist of orgs for
     * @return updated user object
     */

    private static UserModel updateOrgLists(UserModel u, DatabaseConnection dbc) throws SQLException{

        u.setMyOrgs(dbc.getUserList(u.getUserId(),DatabaseConnection.ORGANIZER, true));
        u.setJoinedOrgs(dbc.getUserList(u.getUserId(),DatabaseConnection.MEMBER, true));
        u.setFollowingOrgs(dbc.getUserList(u.getUserId(),DatabaseConnection.FOLLOW, true));
        u.setEventsAttending(dbc.searchUserEventAttendance(u.getUserId()));
        u.setRequestedJoinOrgIds(dbc.getUserList(u.getUserId(),DatabaseConnection.MEMBER, false));
        return u;
    }
}
