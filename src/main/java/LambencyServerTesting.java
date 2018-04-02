import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static spark.Spark.port;
import static spark.Spark.secure;

public class LambencyServerTesting extends LambencyServer {

    LambencyServerTesting(){
        super(1);
        port(20000);

        //adds https capability to server
        //secure("cert.jks", "l4b3ncY!r0ckz",null,null);

        addroutes();

        //Setup and start timer for midnight server task
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        Thread serverTaskThread = new ServerTaskThread();
        Timer timer = new Timer();

        timer.schedule(new ServerTaskTimer(serverTaskThread),date.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
    }

    public static void main(String[]args) {

        LambencyServer lb = new LambencyServerTesting();

    }
}
