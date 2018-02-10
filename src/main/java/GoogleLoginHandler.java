import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;


import java.util.Collections;

public class GoogleLoginHandler {

    private final String GOOGLE_CLIENT_ID;

    GoogleLoginHandler() {

        GOOGLE_CLIENT_ID = "406595282653-cc9eb7143bvpgfe5da941r3jq174b4dq.apps.googleusercontent.com";
    }

    public UserAuthenticator getAuthenticator(String idTokenString) {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

// (Receive idTokenString by HTTPS POST)
        GoogleIdToken idToken ;

        DatabaseConnection dbc;
        UserAuthenticator.Status status;
        UserAuthenticator ua = null;
        try {
            idToken = verifier.verify(idTokenString);
            dbc = new DatabaseConnection();
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                // Print user identifier
                String userId = payload.getSubject();
                System.out.println("User ID: " + userId);

                // Get profile information from payload
                String email = payload.getEmail();
                boolean emailVerified = payload.getEmailVerified();
//              String name = (String) payload.get("name");
//              String pictureUrl = (String) payload.get("picture");
//              String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");
                String sub = (String) payload.get("sub");
                String aud = (String) payload.get("aud");
                String iss = (String) payload.get("iss");

                if (!emailVerified) {
                    status = UserAuthenticator.Status.UNVERIFIED_EMAIL;
                    System.out.println("Failed to have verified email.");
                }
                else if(!(aud.equals(GOOGLE_CLIENT_ID))){
                    System.out.println("Error with comparing aud: "+aud);
                    status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
                }
                else if(!(iss.equals("https://accounts.google.com"))) { //&& !(iss.equals()))
                    System.out.println("Error with comparing iss: "+iss);
                    status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
                }
                /*else if (dbc.searchForUser(sub,1)!= null) {
                    // take OAuthCode from this user and set it in the UserAutneticator
                    new UserAuthenticator(status, <get_id_from_uzer>);
                }
                */else{
                    dbc.createUser(sub, givenName, familyName, email, 1);
                    status = UserAuthenticator.Status.SUCCESS;
                    ua = new UserAuthenticator(status);
                }


            }
            else {
                System.out.println("Invalid ID token.");
                status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
            }
        } catch (Exception e) {

            System.out.println("Exception from database: " + e);
            status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
        }
        if(ua == null){
            ua = new UserAuthenticator(status);
        }

        return ua;
    }


}

