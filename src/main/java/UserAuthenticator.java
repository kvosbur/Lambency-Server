import java.util.Random;

public class UserAuthenticator {

    public enum Status{
        SUCCESS,NON_UNIQUE_EMAIL,NON_DETERMINANT_ERROR, INVALID_LOGIN
    }

    private final char[] charArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();




    private Status status;
    private String oAuthCode;

    UserAuthenticator(Status s){

        this.status = s;
        this.oAuthCode = generateOAuthCode();

    }

    UserAuthenticator(Status s, String oCode){
        this.status = s;
        this.oAuthCode = oCode;
    }

    public Status getStatus() {
        return status;
    }

    public String getoAuthCode() {
        return oAuthCode;
    }

    private String generateOAuthCode(){
        StringBuilder code = new StringBuilder();
        Random r = new Random();
        for(int i = 0; i < 20; i++){
            code.append(charArray[r.nextInt(charArray.length)]);
        }
        return code.toString();
    }


}
