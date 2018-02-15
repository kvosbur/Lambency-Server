import java.sql.SQLException;

public class UserHandler {
    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param orgID id of organization to be followed
     * @return returns 0 on success, 1 if not able to find user or org, 2 on SQLException
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
                //if org is found, set to follow in database
                User u = LambencyServer.dbc.searchForUser(oAuthCode);
                LambencyServer.dbc.setFollowing(u.getUserId(), orgID);
                return 0;
            } else {
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
}
