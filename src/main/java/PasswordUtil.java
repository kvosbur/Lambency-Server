import org.springframework.security.crypto.bcrypt.BCrypt;

import java.nio.charset.Charset;
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

    public static void main(String[] args) {
        Charset charset = Charset.defaultCharset();
        String passwd = "abE2!r?6";

        long startTime = System.nanoTime();
        String hash = hash(passwd);


    }
}
