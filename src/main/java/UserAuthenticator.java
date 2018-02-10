import java.util.Random;

public class UserAuthenticator {

    public enum Status{
        SUCCESS,NON_UNIQUE_EMAIL,NON_DETERMINANT_ERROR, FAILURE

    }

    private final char[] charArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();




    private Status status;
    private String oAuthCode;

    public UserAuthenticator(Status s){
        this.status = s;
        this.oAuthCode = generateOAuthCode();

    }


    public Status getStatus() {
        return status;
    }

    public String getoAuthCode() {
        return oAuthCode;
    }

    private String generateOAuthCode(){
        String code = "";
        Random r = new Random();
        for(int i = 0; i < 20; i++){
            code = code + charArray[r.nextInt(charArray.length)];
        }
        return code;
    }


}
