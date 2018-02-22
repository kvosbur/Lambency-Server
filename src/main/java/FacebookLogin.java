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
        UserModel user;
        try{
            //check if user exist
            user = LambencyServer.dbc.searchForUser(facebookId, 2);
            if(user != null){
                //user exists
                //make and set oAuthCode
                status = UserAuthenticator.Status.SUCCESS;
                ua = new UserAuthenticator(status);
                LambencyServer.dbc.setOauthCode(user.getUserId(), ua.getoAuthCode());
            }
            else{
                //user doesnt exist
                //check if email is valid/not in table already
                if(LambencyServer.dbc.verifyUserEmail(email) == -1){
                    Printing.println("Could not verify email");
                    status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
                    ua = new UserAuthenticator(status);
                    return ua;
                }
                //create user
                int userId = LambencyServer.dbc.createUser(facebookId, firstName, lastName, email, 2);
                //make and set oAuthCode
                status = UserAuthenticator.Status.SUCCESS;
                ua = new UserAuthenticator(status);
                LambencyServer.dbc.setOauthCode(userId, ua.getoAuthCode());
            }
        }
        catch (SQLException e){
            //error occurred
            Printing.println("Exception from database");
            status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
            e.printStackTrace();
            ua = new UserAuthenticator(status);
        }
        return ua;

    }
}
