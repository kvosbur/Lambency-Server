public class FacebookLogin {
    public FacebookLogin(String facebookId, String firstName, String lastName, String email){
        //TODO Do i need to verify with facebook or is that done in the front end?
        //check if user exist

        LambencyServer.dbc.searchForUser(facebookId, 2);
        boolean cond = true;
        if(cond){
            //user exists
            //do something
            //success
        }
        else{
            //user doesnt exist
            //create user
            LambencyServer.dbc.createUser(facebookId,firstName,lastName,email,2);
            //success
        }

    }
}
