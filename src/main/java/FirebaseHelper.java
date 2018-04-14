import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FirebaseHelper {

    public static void initializeFirebase(){
        //Initialize firebase stuff
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/lambency-f7029-firebase-adminsdk-ptyok-dfb184df75.json");

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

    public static void sendCloudChatMessage(String registrationToken, String msgId, String chatId) {
        Message message = Message.builder()
                .putData("type", "chatMessage")
                .putData("chatId", chatId)
                .putData("msgId", msgId)
                .setToken(registrationToken)
                .build();

        String response = null;

        try {
            response = FirebaseMessaging.getInstance().sendAsync(message).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Response is a message ID string.
        System.out.println("Successfully sent join request message: " + response);
    }

    public static void userSendOrgJoinRequest(UserModel u, OrganizationModel o, ArrayList<Integer> ids, DatabaseConnection dbc){

        try {
            for (Integer i : ids) {
                String firebase = dbc.userGetFirebase(i);
                if(firebase != null){
                    FirebaseHelper.sendCloudJoinRequest(firebase, u.getFirstName() + " " + u.getLastName(), "" + u.getUserId(),
                            o.name, "" + o.getOrgID());
                }
            }
        }catch(Exception e){
            Printing.println(e.toString());
        }

    }

    public static void main (String [] args){
        System.out.println(System.getProperty("user.dir"));
        initializeFirebase();

        // This registration token comes from the client FCM SDKs.
        //String registrationToken = "fSKEStmhAzs:APA91bES98kK8nPMSaXDu25RM4C3PKxALl3yjK95b0L78zu4A4CrTRgm8ETWULqgWVFor2kzJzdb1xeud_cHJbR_sDPrtN8QguQqC_NGT3pvm-rg7wQTUcTNMbSAtjihQnWgRvnHEtY8";

        MessageModel messageModel = new MessageModel("here is my text", "Kevin", "04/14/2018");
        Firestore db = FirestoreClient.getFirestore();
        db.collection("chats").document("0")
                .collection("messages")
                .document("0")
                .set(messageModel);

        //sendCloudJoinRequest(registrationToken, "lshank", "uid", "the best org", "5");
    }
}
