import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;


import java.util.Collections;

public class GoogleLoginHandler {


    public GoogleLoginHandler() {

    }

    public UserAuthenticator getAuthenticator(String idTokenString) {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList("CLIENT_ID"))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

// (Receive idTokenString by HTTPS POST)
        GoogleIdToken idToken = null;

        DatabaseConnection dbc = null;
        UserAuthenticator.Status status;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {

            System.out.println("Error: " + e);
        }
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            String sub = (String) payload.get("sub");

            if (!emailVerified) {
                status = UserAuthenticator.Status.UNVERIFIED_EMAIL;
            } else {
                try {
                    dbc = new DatabaseConnection();
                    dbc.createUser(sub, givenName, familyName, email, 1);
                    status = UserAuthenticator.Status.SUCCESS;
                } catch (Exception e) {
                    status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
                }
            }


        } else {
            System.out.println("Invalid ID token.");
            status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
        }

        return new UserAuthenticator(status);
    }


}

