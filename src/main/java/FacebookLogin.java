import java.sql.SQLException;

public class FacebookLogin {
    /**
     *
     * @param facebookId id from facebook API for a specific user
     * @param firstName users first name
     * @param lastName users last name
     * @param email usrs email
     * @return returns the UserAuthenicator object from the login attempt
     */
    public static UserAuthenticator facebookLogin(String facebookId, String firstName, String lastName, String email){
        UserAuthenticator.Status status;
        UserAuthenticator ua = null;
        User user;
        try{
            //check if user exist
            user = LambencyServer.dbc.searchForUser(facebookId, 2);
            if(user != null){
                //user exists
                //success
                status = UserAuthenticator.Status.SUCCESS;
                ua = new UserAuthenticator(status);
                LambencyServer.dbc.setOauthCode(user.getUserId(), ua.getoAuthCode());
                return ua;
            }
            else{
                //user doesnt exist
                //create user
                int userId = LambencyServer.dbc.createUser(facebookId, firstName, lastName, email, 2);
                //make and set oAuthCode
                status = UserAuthenticator.Status.SUCCESS;
                ua = new UserAuthenticator(status);
                LambencyServer.dbc.setOauthCode(userId, ua.getoAuthCode());
                return ua;
            }
        }
        catch (SQLException e){
            //error occurred
            status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
            ua = new UserAuthenticator(status);
            return ua;
        }

    }
}
