import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

public class LambencyServer{


    static DatabaseConnection dbc = null;

    public class ServerTaskThread extends Thread{


        public void run(){
            //code to run task
            Printing.println("Starting Midnight server task");
            boolean status = EventHandler.cleanUpEvents();
            if(status){
                Printing.println("The server successfully cleaned up events and moved all to historical tables.");
            }else{
                Printing.println("The server had issues cleaning up events.Some might have still been moved but not all of them.");
            }
        }
    }

    public class ServerTaskTimer extends TimerTask {

        Thread serverTaskThread;

        ServerTaskTimer(Thread serverTaskThread){
            this.serverTaskThread = serverTaskThread;
        }

        public void run(){
            serverTaskThread.start();
        }
    }

    /*
    spark documentation
    http://sparkjava.com/documentation
    localhost:4567/hello
     */

    LambencyServer(){

        try {
            LambencyServer.dbc = new DatabaseConnection();
        }catch(Exception e){
            //error happened in connecting to database
            Printing.println("TEst");
        }

        port(20000);

        addroutes();

        //Setup and start timer for midnight server task
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        Thread serverTaskThread = new ServerTaskThread();
        Timer timer = new Timer();

        timer.schedule(new ServerTaskTimer(serverTaskThread),date.getTime(),TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
    }

    public void addroutes(){
        // example of responding with a json object made from a java object
        get("/User/login/google", "application/json", (request, response) -> {
            Printing.println("/User/login/google");
            String token = request.queryParams("idToken");
            GoogleLoginHandler glh = new GoogleLoginHandler();
            return glh.getAuthenticator(token);
        }, new JsonTransformer());
        get("/User/search", "application/json", (request, response) -> {
            Printing.println("/User/search");
            String oAuthCode = request.queryParams("oAuthToken");
            String id = request.queryParams("id");
            UserModel u = UserHandler.searchForUser(oAuthCode,id);
            Printing.println(u.toString());
            return u;
        }, new JsonTransformer());
        post("/User/changeInfo", "application/json", (request, response) -> {
            Printing.println("/User/changeInfo");
            UserModel u = new Gson().fromJson(request.body(), UserModel.class);
            return UserHandler.changeInfo(u);
        }, new JsonTransformer());
        get("/User/followOrg", "application/json", (request, response) -> {
            Printing.println("/User/followOrg");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            Integer ret = UserHandler.followOrg(oAuthCode, Integer.parseInt(orgID));
            return ret;
        }, new JsonTransformer());
        post("/User/requestJoinOrg", "application/json", (request, response) -> {
            Printing.println("/User/requestJoinOrg");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgId");
            int ret = UserHandler.requestJoinOrg(oAuthCode, Integer.parseInt(orgID));
            return ret;
        }, new JsonTransformer());
        get("/User/registerForEvent", "application/json", (request, response) -> {
            Printing.println("/User/registerForEvent");
            String oAuthCode = request.queryParams("oAuthCode");
            String eventID = request.queryParams("eventID");
            Integer ret = UserHandler.registerEvent(oAuthCode, Integer.parseInt(eventID));
            return ret;
        }, new JsonTransformer());
        post("/Organization/create", "application/json",
                (request, response) -> {
                    Printing.println("/Organization/create");
                       return OrganizationHandler.createOrganization( new Gson().fromJson(request.body(), OrganizationModel.class));
                }, new JsonTransformer());
        get("/Organization/search", "application/json", (request, response) -> {
            Printing.println("/Organization/search");
            ArrayList<OrganizationModel> orgList = OrganizationHandler.searchOrgName(request.queryParams("name"));
            return orgList;
        }, new JsonTransformer());
        get("/Organization/searchByID", "application/json", (request, response) -> {
            Printing.println("/Organization/searchByID");
            OrganizationModel organization = OrganizationHandler.searchOrgID(Integer.parseInt(request.queryParams("id")));
            return organization;
        }, new JsonTransformer());
        get("/Organization/events", "application/json", (request, response) -> {
            Printing.println("/Organization/events");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("id");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            List<EventModel> list = OrganizationHandler.searchEventsByOrg(oAuthCode, Integer.parseInt(orgID));
            return list;
        }, new JsonTransformer());
        get("/Organization/endorse", "application/json", (request, response) -> {
            Printing.println("/Organization/endorse");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            String eventID = request.queryParams("eventID");
            if(oAuthCode == null || orgID == null || eventID == null){
                return new Integer(-3);
            }
            return OrganizationHandler.endorseEvent(oAuthCode, Integer.parseInt(orgID), Integer.parseInt(eventID));
        }, new JsonTransformer());

        get("/Organization/unendorse", "application/json", (request, response) -> {
            Printing.println("/Organization/unendorse");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            String eventID = request.queryParams("eventID");
            if(oAuthCode == null || orgID == null || eventID == null){
                return new Integer(-3);
            }
            return OrganizationHandler.unendorseEvent(oAuthCode, Integer.parseInt(orgID), Integer.parseInt(eventID));
        }, new JsonTransformer());
        get("/Organization/members", "application.json", (request, response) -> {
            Printing.println("Organization/members");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            return OrganizationHandler.getMembersAndOrganizers(oAuthCode,Integer.parseInt(orgID));
        }, new JsonTransformer());

        get("/Organization/joinRequests","application.json", (request, response) -> {
            Printing.println("Organization/joinRequests");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            return OrganizationHandler.getRequestedToJoinMembers(oAuthCode,Integer.parseInt(orgID));
        }, new JsonTransformer());

        get("/Organization/members", "application.json", (request, response) -> {
            Printing.println("Organization/members");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            return OrganizationHandler.getMembersAndOrganizers(oAuthCode,Integer.parseInt(orgID));
        }, new JsonTransformer());



        post("/Event/update", "application/json",
                (request, response) ->{
                    Printing.println("/Event/update");
                    return EventHandler.updateEvent( new Gson().fromJson(request.body(), EventModel.class));
                }
                , new JsonTransformer());
        post("/Event/create", "application/json", (request, response) -> {
                    Printing.println("/Event/create");
                    return EventHandler.createEvent(new Gson().fromJson(request.body(), EventModel.class));
                }
                , new JsonTransformer());
        get("/Event/search","application/json", (request,response)->{
            Printing.println("/Event/search");
            String latStr = request.queryParams("lat");
            String longStr = request.queryParams("long");
            String name = request.queryParams("name");
            String org_idStr = request.queryParams("org_id");
            return EventHandler.getEventsByLocation(Double.parseDouble(latStr), Double.parseDouble(longStr));
        }, new JsonTransformer());
        get("/Event/searchWithFilter","application/json",(request, response) -> {
            EventFilterModel efm = new Gson().fromJson(request.body(), EventFilterModel.class);
            return EventHandler.getEventsWithFilter(efm);
        }, new JsonTransformer());
        get("/Event/searchByID", "application/json", (request, response) -> {
            Printing.println("/Event/searchByID");
            EventModel eventModel = EventHandler.searchEventID(Integer.parseInt(request.queryParams("id")));
            return eventModel;
        }, new JsonTransformer());
        get("/Event/searchByIDs", "application/json", (request, response) -> {
            Printing.println("/Event/searchByIDs");
            String userID = request.queryParams("userID");
            String oAuthCode = request.queryParams("oAuthCode");
            List<EventModel> eventModels = EventHandler.searchEventIDS(Integer.parseInt(userID), oAuthCode);
            return eventModels;
        }, new JsonTransformer());
        get("/Event/users","application/json",(request,response)->{
            Printing.println("/Event/users");
            String oauthcode = request.queryParams("oauthcode");
            int event_id = Integer.parseInt(request.queryParams("event_id"));
            return EventHandler.getUsersAttending(oauthcode,event_id);
        }, new JsonTransformer());
        get("/Event/numAttending", "application/json", (request, response) -> {
            Printing.println("/Event/numAttending");
            String oAuthCode = request.queryParams("oAuthCode");
            String eventID = request.queryParams("id");
            if(oAuthCode == null || eventID == null){
                return new Integer(-1);
            }
            Integer ret = EventHandler.numAttending(oAuthCode, Integer.parseInt(eventID));
            return ret;
        }, new JsonTransformer());
        get("/User/login/facebook", "application/json", (request, response) -> {
            Printing.println("/User/login/facebook");
            UserAuthenticator ua = FacebookLogin.facebookLogin(request.queryParams("id"), request.queryParams("first"), request.queryParams("last"), request.queryParams("email"));
            return ua;
        }, new JsonTransformer());
        get("/User/unfollowOrg","application/json",(request, response) -> {
            Printing.println("/User/unfollowOrg");
            return UserHandler.unfollowOrg(request.queryParams("oAuthCode"), Integer.parseInt(request.queryParams("org_id")));
        }, new JsonTransformer());

        post("/User/leaveOrg","application/json",(request, response) -> {

            Printing.println("/User/leaveOrg");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                Printing.println("oAuthCode is null or orgID is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            return UserHandler.leaveOrganization(oAuthCode,Integer.parseInt(orgID));
        },new JsonTransformer());

        post("/User/ClockIn","application/json",(request, response) -> {

            Printing.println("/Event/ClockIn");
            String oAuthCode = request.queryParams("oAuthCode");
            EventAttendanceModel eventAttendanceModel = new Gson().fromJson(request.body(), EventAttendanceModel.class);
            if(oAuthCode == null || eventAttendanceModel == null){
                Printing.println("oAuthCode is null or eventAttendanceModel is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            return EventHandler.clockInEvent(eventAttendanceModel,oAuthCode,EventAttendanceModel.CLOCKINCODE);
        },new JsonTransformer());
        post("/User/ClockOut","application/json",(request, response) -> {

            Printing.println("/User/ClockOut");
            String oAuthCode = request.queryParams("oAuthCode");
            EventAttendanceModel eventAttendanceModel = new Gson().fromJson(request.body(), EventAttendanceModel.class);
            if(oAuthCode == null || eventAttendanceModel == null){
                Printing.println("oAuthCode is null or eventAttendanceModel is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            return EventHandler.clockInEvent(eventAttendanceModel,oAuthCode,EventAttendanceModel.CLOCKOUTCODE);
        },new JsonTransformer());
        get("/User/MyLambency","application/json",(request, response) -> {

            Printing.println("/User/MyLambency");
            String oAuthCode = request.queryParams("oAuthCode");
            if(oAuthCode == null){
                Printing.println("oAuthCode is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            return UserHandler.getMyLambency(oAuthCode);
        },new JsonTransformer());


    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();

        /*
        UserAuthenticator ua = FacebookLogin.facebookLogin("id","first", "last", "email@mail.com" );
        EventModel e = new EventModel("Event", 8 , new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3), "description","location", "path", 10,   100, 1200);
        int eventID = EventHandler.createEvent(e);
        UserHandler.registerEvent(ua.getoAuthCode(), eventID);
        */
    }
}