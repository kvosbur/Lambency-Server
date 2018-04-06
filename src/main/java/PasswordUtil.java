import org.springframework.security.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Locale;

public final class PasswordUtil {

    private final static  int ITERATIONS = 10;
    private final static int MEMORY = 128000;
    private final static int PARALLELISM = 4;

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";

    public static final String alphanum = upper + lower + digits;

    public static char[] generateSalt(int length){

        SecureRandom secure = new SecureRandom();
        char[] salt = new char[length];
        char[] symbols = alphanum.toCharArray();
        for(int i = 0; i < length; i++){
            salt[i] = symbols[secure.nextInt(symbols.length)];

        }
        return salt;
    }

    public static String hash(String password){

        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verify(String password, String hash){

        return BCrypt.checkpw(password,hash);
    }

    public static int setPassword(String password, int userID, DatabaseConnection dbc){

        String hash = hash(password);
        try {
            return dbc.userSetHash(userID, hash, "");
        }catch(Exception e){
            Printing.printlnException(e);
            return 2;
        }
    }

    public static void main(String[] args) {
        DatabaseConnection dbc = new DatabaseConnection();

        int ret = PasswordUtil.setPassword("mysecurepassword", 1, dbc);
        System.out.println(ret);
        try {
            String[] strings = dbc.userGetHash(1);
            System.out.println(strings[0]);
            System.out.println(strings[1]);
            System.out.println(PasswordUtil.verify("mysecurepassword", strings[1]));
        }catch(Exception e){
            Printing.printlnException(e);
        }
        dbc.close();


    }
}
