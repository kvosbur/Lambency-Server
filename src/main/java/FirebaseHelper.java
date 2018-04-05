import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class FirebaseHelper {

    public static void initializeFirebase(){
        //Initialize firebase stuff
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("C:\\Users\\lshan\\IdeaProjects\\Lambency-Server\\src\\lambency-f7029-firebase-adminsdk-ptyok-549eba84a5.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://lambency-f7029.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void sendCloudJoinRequest(String registrationToken, String username, String uid, String orgName, String org_id){
        Message message = Message.builder()
                .putData("type", "joinRequest")
                .putData("user", username)
                .putData("uid", uid)
                .putData("org", orgName)
                .putData("org_id", org_id)
                .setToken(registrationToken)
                .build();

        // Send a message to the device corresponding to the provided
        // registration token.
        String response = null;

        try {
            response = FirebaseMessaging.getInstance().sendAsync(message).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Response is a message ID string.
        System.out.println("Successfully sent join request message: " + response);
    }

    public static void main (String [] args){
        initializeFirebase();

        // This registration token comes from the client FCM SDKs.
        String registrationToken = "fSKEStmhAzs:APA91bES98kK8nPMSaXDu25RM4C3PKxALl3yjK95b0L78zu4A4CrTRgm8ETWULqgWVFor2kzJzdb1xeud_cHJbR_sDPrtN8QguQqC_NGT3pvm-rg7wQTUcTNMbSAtjihQnWgRvnHEtY8";

        sendCloudJoinRequest(registrationToken, "lshank", "uid", "the best org", "5");
    }
}
