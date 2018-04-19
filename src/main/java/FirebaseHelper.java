import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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
                    new FileInputStream("lambency-f7029-firebase-adminsdk-ptyok-dfb184df75.json");

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

    public static void sendCloudChatMessage(String registrationToken, String msgId, String chatId, String sentName) {
        Message message = Message.builder()
                .putData("type", "chatMessage")
                .putData("chatId", chatId)
                .putData("msgId", msgId)
                .putData("name", sentName)
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

    public static void sendCloudOrgInvite(String registrationToken, String orgName, String org_id){
        Message message = Message.builder()
                .putData("type", "orgInvite")
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
        System.out.println("Successfully sent org invite message: " + response);
    }

    public static void sendCloudEventUpdate(String registrationToken, String eventName, String event_id){
        Message message = Message.builder()
                .putData("type", "eventUpdate")
                .putData("name", eventName)
                .putData("event_id", event_id)
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
        System.out.println("Successfully sent event update message: " + response);
    }

    public static void sendGroupEventUpdate(ArrayList<Object> users, EventModel eventModel, DatabaseConnection dbc){

        try {
            for (Object o : users) {
                UserModel userModel = (UserModel) o;
                String firebase_token = dbc.userGetFirebase(userModel.getUserId());
                if(firebase_token != null) {
                    sendCloudEventUpdate(firebase_token, eventModel.getName(), "" + eventModel.getEvent_id());
                }
            }
        }catch(Exception e){
            Printing.printlnException(e);
        }
    }

    public static void main (String [] args){
        System.out.println(System.getProperty("user.dir"));
        initializeFirebase();

        // This registration token comes from the client FCM SDKs.
        String registrationToken = "czw2HKdaOxw:APA91bE62Mvqhv2-X-xndhYKpr9lI999a4cHgj9acZ9M9U-BkZjBft-9ohK3Xpi-We7K8tMhqr3TnwJCUVsp3C30d0FiF0Xv8n2wmDFShNhzhatmeH_JFyGzCuEI5cqWLdxJ8WxH8cYV";

        sendCloudOrgInvite(registrationToken,"My House", "57");
        //sendCloudJoinRequest(registrationToken, "lshank", "uid", "the best org", "5");
    }
}
