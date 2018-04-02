import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

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

    public static String[] hash(char[] password, Charset charset){
        String[] ret = new String[2];
        Argon2 argon2hasher = null;
        char[] appended = new char[100 + password.length];

        try{
            argon2hasher = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2i);

            //generate salt
            char[] salt = generateSalt(100);
            ret[1] = new String(salt);

            int index = 0;
            for(int i = 0; i < salt.length; i++, index++){
                System.out.print(salt[i]);
                appended[index] = salt[i];
            }
            System.out.print("\n");

            for(int i = 0; i < password.length; i++, index++){
                appended[index] = password[i];
            }

            ret[0] = argon2hasher.hash(ITERATIONS, MEMORY, PARALLELISM,appended);
        }finally{
            if(argon2hasher != null){
                argon2hasher.wipeArray(password);
                argon2hasher.wipeArray(appended);
            }
        }
        int index = ret[0].lastIndexOf("p=4$");
        ret[0] = ret[0].substring(index+4);
        return ret;
    }

    public static void main(String[] args) {
        Charset charset = Charset.defaultCharset();
        char[] passwd = {'a', 'b', 'E', '2', '!', 'r', '?', '6'};

        long startTime = System.nanoTime();
        String[] hash = hash(passwd, charset);

        long endTime = System.nanoTime();
        System.out.println(((endTime - startTime) / 1000000));
        System.out.println(hash[0]);
        System.out.println(hash[1]);





    }
}
