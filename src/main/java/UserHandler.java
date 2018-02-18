import javax.xml.crypto.Data;
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
                System.out.println("User not found");
                return 1;
            }
            //search for organization by ID
            Organization org = LambencyServer.dbc.searchForOrg(orgID);
            if (org != null) {
                User u = LambencyServer.dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                Groupies g = LambencyServer.dbc.searchGroupies(u.getUserId(), orgID);
                if (g != null) {
                    //if org is found and no groupies already exist, set to follow in database
                    LambencyServer.dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW, true);
                    return 0;
                } else {
                    //Groupies already exist for this user
                    System.out.println("Groupies already exists with equal or higher permissions");
                    return 1;
                }
            }
            else {
                //org is not found, return error
                System.out.println("Organization not found");
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
                System.out.println("User not found");
                return 1;
            }
            //search for organization by ID
            Organization org = LambencyServer.dbc.searchForOrg(orgID);
            if (org != null) {
                User u = LambencyServer.dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                Groupies g = LambencyServer.dbc.searchGroupies(u.getUserId(), orgID);
                if (g != null) {
                    //if org is found and no groupies already exist, set to follow in database
                    LambencyServer.dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, false);
                    return 0;
                } else {
                    //Groupies already exist for this user
                    if(g.getType() == DatabaseConnection.FOLLOW){
                        //upgrade to a member
                        LambencyServer.dbc.deleteGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW);
                        LambencyServer.dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, false);
                        return 0;
                    }
                    //user already has higher or equal permissions
                    System.out.println("Groupies already exists with equal or higher permissions");
                    return 1;
                }
            } else {
                //org is not found, return error
                System.out.println("Organization not found");
                return 1;
            }
        } catch (SQLException e) {
            System.out.println("SQLExcpetion");
            return 2;
        }
    }
}
