import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class GMailHelper {

    /** Application name. */
    private static final String APPLICATION_NAME =
            "Lambency";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.dir"), ".credentials/gmail-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/gmail-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private Gmail service;
    public int status;
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;

    GMailHelper(){
        try {
            service = getGmailService();
            this.status = SUCCESS;
        }catch(Exception e){
            Printing.println(e.toString());
            this.status = FAILURE;
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                GMailHelper.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();

        LocalServerReceiver lsr = new LocalServerReceiver.Builder()
                .setPort(63734)
                .setHost("localhost")
                .setCallbackPath("/Callback")
                .build();


        Credential credential= new AuthorizationCodeInstalledApp(
                flow, lsr).authorize("user");

        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    private static Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to email address of the receiver
     * @param from email address of the sender, the mailbox account
     * @param subject subject of the email
     * @param bodyText body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     */
    private static MimeMessage createEmail(String to,
                                          String from,
                                          String subject,
                                          String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(bodyText, "text/html");
        return email;
    }

    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException
     * @throws MessagingException
     */
    private static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param emailContent Email to be sent.
     * @return The sent message
     * @throws MessagingException
     * @throws IOException
     */
    private Message sendMessage(Gmail service,
                                      String userId,
                                      MimeMessage emailContent)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().send(userId, message).execute();

        //System.out.println("Message id: " + message.getId());
        //System.out.println(message.toPrettyString());
        return message;
    }

    public int sendEmail(String to,String subject, String bodyText){
        try {
            MimeMessage example = GMailHelper.createEmail(to, "me", subject
                    , bodyText);
            sendMessage(service,"me",example);
            return SUCCESS;
        }catch(Exception e){
            e.printStackTrace();
            return FAILURE;
        }
    }

    /**
     * Send a verification email to the new user's email
     *
     * @param email The email of the new user
     * @return status of the email
     */
    public static int sendVerificationEmail(String email,int userID, String verificationCode){

        String subject = "Email Vefification for Lambency Account";

        StringBuilder sb = new StringBuilder();

        sb.append("Please click the link below to open our app in order to verify your email.<br>");
        sb.append("<a href=\"www.mylambencyclient.com/login?code=" + verificationCode + "&uid=" + userID);
        sb.append("\"> Click Here To Vefify</a>");

        String body = sb.toString();
        GMailHelper gmh = new GMailHelper();
        return gmh.sendEmail(email, subject, body);
    }

    /**
     * Send an email to a user to allow them to change their password
     *
     * @param email The email of the  user
     * @return status of the email
     */
    public static int sendChangePasswordEmail(String email,int userID, String verificationCode){

        String subject = "Password Reset for Lambency Account";

        StringBuilder sb = new StringBuilder();

        sb.append("Please click the link below to open our app in order to set your new password.<br>");
        sb.append("<a href=\"www.mylambencyclient.com/login?code=" + verificationCode + "&uid=" + userID);
        sb.append("\"> Click Here To Change Password</a>");

        String body = sb.toString();
        GMailHelper gmh = new GMailHelper();
        return gmh.sendEmail(email, subject, body);
    }

    public static void main(String[] args) throws IOException {
        //send email using gmail

        GMailHelper gmh = new GMailHelper();
        gmh.sendEmail("kvosbur@purdue.edu", "subject", "this is the body in the text");
    }
}
