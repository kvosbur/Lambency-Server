import java.sql.SQLException;

public class UserHandler {
    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param orgID id of organization to be followed
     * @return returns 0 on success, 1 if not able to find user or org or groupies already exists, 2 on SQLException
     */
    public static Integer followOrg(String oAuthCode, int orgID){
        try {
            //search for user
            if(LambencyServer.dbc.searchForUser(oAuthCode) == null){
                System.out.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = LambencyServer.dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = LambencyServer.dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = LambencyServer.dbc.searchGroupies(u.getUserId(), orgID);
                if (g == null) {
                    //if org is found and no groupies already exist, set to follow in database
                    LambencyServer.dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW, true);
                    return 0;
                } else {
                    //GroupiesModel already exist for this user
                    System.out.println("GroupiesModel already exists with equal or higher permissions");
                    return 1;
                }
            }
            else {
                //org is not found, return error
                System.out.println("OrganizationModel not found");
                return 1;
            }
        }
        catch (SQLException e){
            System.out.println("SQLExcpetion");
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
    public static Integer unfollowOrg(String oAuthCode, int orgID){
        try {
            //search for user
            if(LambencyServer.dbc.searchForUser(oAuthCode) == null){
                System.out.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = LambencyServer.dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = LambencyServer.dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = LambencyServer.dbc.searchGroupies(u.getUserId(), orgID);
                if (g != null) {
                    //if org is found and a groupie exists delete it if it is a follow
                    // deleteGroupies returns a -1 if failure
                    return -1 * LambencyServer.dbc.deleteGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW);
                } else {
                    //GroupiesModel already exist for this user
                    System.out.println("Following of OrganizationModel doesn't exist.");
                    return 1;
                }
            }
            else {
                //org is not found, return error
                System.out.println("OrganizationModel not found");
                return 1;
            }
        }
        catch (SQLException e){
            System.out.println("SQLExcpetion");
            return 2;
        }
    }


    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param orgID id of organization to request to joi
     * @return returns 0 on success, 1 if not able to find user or org or groupies already exists, 2 on SQLException
     */
    public static Integer requestJoinOrg(String oAuthCode, int orgID) {
        try {
            //search for user
            if (LambencyServer.dbc.searchForUser(oAuthCode) == null) {
                System.out.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = LambencyServer.dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = LambencyServer.dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = LambencyServer.dbc.searchGroupies(u.getUserId(), orgID);
                if (g == null) {
                    //if org is found and no groupies already exist, set to follow in database
                    LambencyServer.dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, false);
                    return 0;
                } else {
                    //GroupiesModel already exist for this user
                    if(g.getType() == DatabaseConnection.FOLLOW){
                        //upgrade to a member
                        LambencyServer.dbc.deleteGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW);
                        LambencyServer.dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, false);
                        return 0;
                    }
                    //user already has higher or equal permissions
                    System.out.println("GroupiesModel already exists with equal or higher permissions");
                    return 1;
                }
            } else {
                //org is not found, return error
                System.out.println("OrganizationModel not found");
                return 1;
            }
        } catch (SQLException e) {
            System.out.println("SQLExcpetion");
            return 2;
        }
    }

    /**
     *
     * @param oAuthCode the oAuthCode of the given user
     * @param eventID the id of the event
     * @return 0 on success, 1 on failing to find user or organization, 2 on SQL exception, 3 on already registered
     */
    public static Integer registerEvent(String oAuthCode, int eventID){
        try {
            //search for user
            if (LambencyServer.dbc.searchForUser(oAuthCode) == null) {
                System.out.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            EventModel event = LambencyServer.dbc.searchEvents(eventID);
            if (event != null) {
                UserModel u = LambencyServer.dbc.searchForUser(oAuthCode);
                //check if user is already registered for an event
                EventAttendanceModel ea = LambencyServer.dbc.searchEventAttendance(u.getUserId(), eventID);
                if (ea == null) {
                    //if event is found and no event attended already exist, register user for event
                    LambencyServer.dbc.registerForEvent(u.getUserId(), eventID);
                    return 0;
                } else {
                    //UserModel is already registered
                    System.out.println("UserModel is already registered for the event");
                    return 3;
                }
            } else {
                //org is not found, return error
                System.out.println("OrganizationModel not found");
                return 1;
            }
        } catch (SQLException e) {
            System.out.println("SQLExcpetion");
            return 2;
        }
    }

    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param userID id of user to search for
     * @return returns UserModel object on success or null if failure
     */
    public static UserModel searchForUser(String oAuthCode, String userID) {
        try {
            //search for user
            UserModel user = LambencyServer.dbc.searchForUser(oAuthCode);
            if (userID != null && user == null) {
                System.out.println("UserModel not found");
                return null;
            }
            //search for organization by ID
            if(userID == null){
                user =  LambencyServer.dbc.searchForUser(oAuthCode);
                return user;
            }else{
                user = LambencyServer.dbc.searchForUser(userID, DatabaseConnection.LAMBNECYUSERID);
                user = UserHandler.updateOrgLists(user);
                return user;
            }

        } catch (SQLException e) {
            System.out.println("SQLExcpetion");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Changes the account information to the information in the user object
     * @param u the user object with the info to be changed in the database
     * @return updated user object from the database
     */
    public static UserModel changeInfo(UserModel u){
        try {
            if(u == null){
                System.out.println("Null user");
                return null;
            }
            UserModel user = LambencyServer.dbc.searchForUser(u.getOauthToken());
            if(user == null){
                System.out.println("UserModel not found");
                return null;
            }
            if(u.getEmail() == null || u.getLastName() == null| u.getFirstName() == null){
                System.out.println("UserModel info invalid");
                return null;
            }
            u = LambencyServer.dbc.modifyUserInfo(user.getUserId(), u.getFirstName(), u.getLastName(), u.getEmail());
            u = UserHandler.updateOrgLists(u);
            return u;
        }
        catch (SQLException e){
            System.out.println("SQLException");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Changes the account information to the information in the user object
     * @param u the user object to update the arraylist of orgs for
     * @return updated user object
     */

    public static UserModel updateOrgLists(UserModel u) throws SQLException{

        u.setMyOrgs(LambencyServer.dbc.getUserList(u.getUserId(),DatabaseConnection.ORGANIZER));
        u.setJoinedOrgs(LambencyServer.dbc.getUserList(u.getUserId(),DatabaseConnection.MEMBER));
        u.setFollowingOrgs(LambencyServer.dbc.getUserList(u.getUserId(),DatabaseConnection.FOLLOW));
        return u;
    }
}
