import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GoogleLoginHandler {

    private final String GOOGLE_CLIENT_ID;
    private final String GOOGLE_ANDROID_ID;

    GoogleLoginHandler() {

        //GOOGLE_CLIENT_ID = "406595282653-cc9eb7143bvpgfe5da941r3jq174b4dq.apps.googleusercontent.com";
        //GOOGLE_ANDROID_ID = "406595282653-87c0rdih5bqi4nrei8catgh3pq1usith.apps.googleusercontent.com";
        //kevins
        GOOGLE_CLIENT_ID = "801710608826-ai9n5mnitg4ea1e92c1o7c9j77f02fbq.apps.googleusercontent.com";
        GOOGLE_ANDROID_ID = "801710608826-06vpf384rl9nfcbumav56niql251419n.apps.googleusercontent.com";

        //used this as reference for fix https://stackoverflow.com/questions/43043526/java-web-googlesignin-googleidtokenverifier-verify-token-string-returns-null/43203748

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

        DatabaseConnection dbc = LambencyServer.dbc;
        UserAuthenticator.Status status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
        UserAuthenticator ua = null;
        try {
            idToken = verifier.verify(idTokenString);
            Map<String, String> payload = getMapFromGoogleTokenString(idTokenString);
            if (doTokenVerification(payload)) {
                //Payload payload = idToken.getPayload();

                // Print user identifier
                String userId = payload.get("sub");
                Printing.println("UserModel ID: " + userId);

                // Get profile information from payload
                String email = payload.get("email");
                String emailVerified = payload.get("email_verified");
//              String name = (String) payload.get("name");
//              String pictureUrl = (String) payload.get("picture");
//              String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");
                String sub = (String) payload.get("sub");
                String aud = (String) payload.get("aud");
                String iss = (String) payload.get("iss");
                UserModel us;

                if (emailVerified.equals("false")) {
                    status = UserAuthenticator.Status.NON_UNIQUE_EMAIL;
                    Printing.println("Failed to have verified email.");
                }
                else if(!(aud.equals(GOOGLE_ANDROID_ID))){
                    Printing.println("Error with comparing aud: "+aud);
                    status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
                }
                else if(!(iss.equals("https://accounts.google.com"))) { //&& !(iss.equals()))
                    Printing.println("Error with comparing iss: "+iss);
                    status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
                }
                else if ((us = dbc.searchForUser(sub,1))!= null) {
                    // take OAuthCode from this user and set it in the UserAutneticator
                    ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS, us.getOauthToken());
                }
                else{
                    int labID = dbc.createUser(sub, givenName, familyName, email, 1);
                    status = UserAuthenticator.Status.SUCCESS;
                    ua = new UserAuthenticator(status);
                    dbc.setOauthCode(labID,ua.getoAuthCode());
                }


            }
            else {
                Printing.println("Invalid ID token.");
                status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
            }
        } catch (Exception e) {

            Printing.println("Exception from database: " + e);
            status = UserAuthenticator.Status.NON_DETERMINANT_ERROR;
            Printing.println(idTokenString);
            Printing.println(e.toString());
        }
        if(ua == null){
            ua = new UserAuthenticator(status);
        }

        return ua;
    }

    private Map<String,String> getMapFromGoogleTokenString(final String idTokenString){
        BufferedReader in = null;
        try {
            // get information from token by contacting the google_token_verify_tool url :
            in = new BufferedReader(new InputStreamReader(
                    ((HttpURLConnection) (new URL("https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + idTokenString.trim()))
                            .openConnection()).getInputStream(), Charset.forName("UTF-8")));

            // read information into a string buffer :
            StringBuffer b = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                b.append(inputLine + "\n");
            }

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> myMap = gson.fromJson(b.toString(), type);

            // transforming json string into Map<String,String> :
            //ObjectMapper objectMapper = new ObjectMapper();
            //return objectMapper.readValue(b.toString(), objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
            return myMap;

            // exception handling :
        } catch (MalformedURLException e) {
            Printing.println(e.toString());
        } catch (IOException e) {
            Printing.println(e.toString());
        } catch(Exception e){
            Printing.println("\n\n\tFailed to transform json to string\n");
            Printing.println(e.toString());
        } finally{
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    Printing.println(e.toString());
                }
            }
        }
        return null;
    }

    // chack the "email_verified" and "email" values in token payload
    private boolean verifyEmail(final Map<String,String> tokenPayload){
        if(tokenPayload.get("email_verified")!=null && tokenPayload.get("email")!=null){
            try{
                return Boolean.valueOf(tokenPayload.get("email_verified")) && tokenPayload.get("email").contains("@gmail.");
            }catch(Exception e){
                Printing.println("\n\n\tCheck emailVerified failed - cannot parse "+tokenPayload.get("email_verified")+" to boolean\n");
            }
        }else{
            Printing.println("\n\n\tCheck emailVerified failed - required information missing in the token");
        }
        return false;
    }

    // check token expiration is after now :
    private boolean checkExpirationTime(final Map<String,String> tokenPayload){
        try{
            if(tokenPayload.get("exp")!=null){
                // the "exp" value is in seconds and Date().getTime is in mili seconds
                return Long.parseLong(tokenPayload.get("exp")+"000") > new java.util.Date().getTime();
            }else{
                Printing.println("\n\n\tCheck expiration failed - required information missing in the token\n");
            }
        }catch(Exception e){
            Printing.println("\n\n\tCheck expiration failed - cannot parse "+tokenPayload.get("exp")+" into long\n");
        }
        return false;
    }

    // check that at least one CLIENT_ID matches with token values
    private boolean checkAudience(final Map<String,String> tokenPayload){
        if(tokenPayload.get("aud")!=null && tokenPayload.get("azp")!=null){
            List<String> pom = Arrays.asList(GOOGLE_CLIENT_ID);

            if(pom.contains(tokenPayload.get("aud")) || pom.contains(tokenPayload.get("azp"))){
                return true;
            }else{
                Printing.println("\n\n\tCheck audience failed - audiences differ\n");
                return false;
            }
        }
        Printing.println("\n\n\tCheck audience failed - required information missing in the token\n");
        return false;
    }

    // verify google token payload :
    private boolean doTokenVerification(final Map<String,String> tokenPayload){
        if(tokenPayload!=null){
            return verifyEmail(tokenPayload) // check that email address is verifies
                    && checkExpirationTime(tokenPayload) // check that token is not expired
                    && checkAudience(tokenPayload) // check audience
                    ;
        }
        return false;
    }
}

