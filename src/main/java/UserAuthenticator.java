import javax.xml.crypto.Data;
import java.util.Random;

public class UserAuthenticator {

    public enum Status{
        SUCCESS,NON_UNIQUE_EMAIL,NON_DETERMINANT_ERROR, FAILURE, UNVERIFIED_EMAIL

    }

    private final char[] charArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private Status status;
    private String oAuthCode;

    /*
        Object to create and verify user oauth codes.
           @param Status s: Status from the Enum that represents whether the user has been verified or not
     */
    public UserAuthenticator(Status s){
        try {
            this.status = s;
            if (s.equals(Status.SUCCESS)) {
                generateOAuthCode();
            }
        }
        catch (Exception e){
            status = Status.NON_DETERMINANT_ERROR;
        }
    }

    /*
        return the status of the user
        @return Status status

     */

    public Status getStatus() {
        return status;
    }

    /*
        Returns the oAuthCode
        @return String oAuthCode

     */

    public String getOAuthCode() {
        return oAuthCode;
    }

    /*
        private method for generating oAuthCode
           @return String oAuth code
           @throw  Exception from the database
     */
    private String generateOAuthCode() throws Exception{
        String code = "";
        Random r = new Random();
        DatabaseConnection dbc = null;
        do{
            for(int i = 0; i < 20; i++){
                code = code + charArray[r.nextInt(charArray.length)];
            }
            dbc = new DatabaseConnection();

        }
        while(dbc != null && dbc.searchForUser(code)!=null);
        return code;
    }


    public static void main(String[] args){
        try {
            UserAuthenticator u = new UserAuthenticator(Status.FAILURE);
            for (int i = 0; i < 10; i++) {
                System.out.println(u.generateOAuthCode());

            }
        }
        catch (Exception e){
            System.out.println("ERROR: "+e);

        }

    }

}
