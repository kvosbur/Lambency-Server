import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

public class LambencyServer{


    public class ServerTaskThread extends Thread{


        public void run(){
            //code to run task
            Printing.println("Starting Midnight server task");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return;
            }
            boolean status = EventHandler.cleanUpEvents(databaseConnection);
            databaseConnection.close();
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
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            UserAuthenticator authenticator = glh.getAuthenticator(token, databaseConnection);
            databaseConnection.close();
            return authenticator;
        }, new JsonTransformer());
        get("/User/search", "application/json", (request, response) -> {
            Printing.println("/User/search");
            String oAuthCode = request.queryParams("oAuthToken");
            String id = request.queryParams("id");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            UserModel u = UserHandler.searchForUser(oAuthCode,id, databaseConnection);
            Printing.println(u.toString());
            databaseConnection.close();
            return u;
        }, new JsonTransformer());
        post("/User/changeInfo", "application/json", (request, response) -> {
            Printing.println("/User/changeInfo");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            UserModel u = new Gson().fromJson(request.body(), UserModel.class);
            UserModel changed = UserHandler.changeInfo(u, databaseConnection);
            databaseConnection.close();
            return changed;
        }, new JsonTransformer());
        get("/User/followOrg", "application/json", (request, response) -> {
            Printing.println("/User/followOrg");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            Integer ret = UserHandler.followOrg(oAuthCode, Integer.parseInt(orgID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        post("/User/requestJoinOrg", "application/json", (request, response) -> {
            Printing.println("/User/requestJoinOrg");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgId");
            int ret = UserHandler.requestJoinOrg(oAuthCode, Integer.parseInt(orgID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/User/registerForEvent", "application/json", (request, response) -> {
            Printing.println("/User/registerForEvent");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String oAuthCode = request.queryParams("oAuthCode");
            String eventID = request.queryParams("eventID");
            Integer ret = UserHandler.registerEvent(oAuthCode, Integer.parseInt(eventID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/User/eventsFeed", "application/json", (request, response) -> {
            Printing.println("/User/eventsFeed");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String oAuthCode = request.queryParams("oAuthCode");
            String latitude  = request.queryParams("latitude");
            String longitude = request.queryParams("longitude");
            if(oAuthCode == null){
                Printing.println("null params");
                databaseConnection.close();
                return null;
            }
            List<EventModel> events = UserHandler.eventsFeed(oAuthCode, latitude, longitude, databaseConnection);
            databaseConnection.close();
            return events;
        }, new JsonTransformer());
        post("/Organization/create", "application/json",
                (request, response) -> {
                    Printing.println("/Organization/create");
                    DatabaseConnection databaseConnection = new DatabaseConnection();
                    if(databaseConnection.connect == null){
                        return null;
                    }
                    OrganizationModel ret = OrganizationHandler.createOrganization( new Gson().fromJson(request.body(), OrganizationModel.class),
                            databaseConnection);
                    databaseConnection.close();
                    return ret;
                }, new JsonTransformer());
        get("/Organization/search", "application/json", (request, response) -> {
            Printing.println("/Organization/search");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            ArrayList<OrganizationModel> orgList = OrganizationHandler.searchOrgName(request.queryParams("name"), databaseConnection);
            databaseConnection.close();
            return orgList;
        }, new JsonTransformer());
        get("/Organization/searchByID", "application/json", (request, response) -> {
            Printing.println("/Organization/searchByID");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            OrganizationModel organization = OrganizationHandler.searchOrgID(Integer.parseInt(request.queryParams("id")),
                    databaseConnection);
            databaseConnection.close();
            return organization;
        }, new JsonTransformer());
        get("/Organization/events", "application/json", (request, response) -> {
            Printing.println("/Organization/events");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("id");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            List<EventModel> list = OrganizationHandler.searchEventsByOrg(oAuthCode, Integer.parseInt(orgID), databaseConnection);
            databaseConnection.close();
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
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            int ret = OrganizationHandler.endorseEvent(oAuthCode, Integer.parseInt(orgID), Integer.parseInt(eventID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());

        get("/Organization/unendorse", "application/json", (request, response) -> {
            Printing.println("/Organization/unendorse");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            String eventID = request.queryParams("eventID");
            if(oAuthCode == null || orgID == null || eventID == null){
                return new Integer(-3);
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            int ret = OrganizationHandler.unendorseEvent(oAuthCode, Integer.parseInt(orgID), Integer.parseInt(eventID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/Organization/changeUserPermissions", "application/json", (request, response) -> {
            Printing.println("/Organization/changeUserPermissions");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            String userChanged = request.queryParams("userChanged");
            String type = request.queryParams("type");
            if(oAuthCode == null || orgID == null || userChanged == null || type == null){
                Printing.println("Null queries");
                return new Integer(-3);
            }
            if(databaseConnection.connect == null){
                return null;
            }
            int ret = OrganizationHandler.manageUserPermissions(oAuthCode, Integer.parseInt(orgID), Integer.parseInt(userChanged), Integer.parseInt(type),
                    databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/Organization/members", "application.json", (request, response) -> {
            Printing.println("Organization/members");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            ArrayList<UserModel>[] ret = OrganizationHandler.getMembersAndOrganizers(oAuthCode,Integer.parseInt(orgID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());

        get("/Organization/joinRequests","application.json", (request, response) -> {
            Printing.println("Organization/joinRequests");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            ArrayList<UserModel> ret = OrganizationHandler.getRequestedToJoinMembers(oAuthCode,Integer.parseInt(orgID),
                    databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());

        get("/Organization/respondToJoinRequest","application/json", (request, response) -> {
            Printing.println("Organization/respondToJoinRequest");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            String userID = request.queryParams("userID");
            String accepted = request.queryParams("approved");
            if(oAuthCode == null || orgID == null || userID == null || accepted == null){
                Printing.println("Null object recieved from retrofit");
                return null;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                Printing.println("Failed to create database connection");
                return null;
            }
            Integer ret = OrganizationHandler.respondToRequest(oAuthCode,Integer.parseInt(orgID), Integer.parseInt(userID), Boolean.parseBoolean(accepted), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());

        get("/Organization/getMembersAndOrganizers", "application.json", (request, response) -> {
            Printing.println("Organization/getMembersAndOrganizers");
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                return null;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            ArrayList<UserModel>[] ret = OrganizationHandler.getMembersAndOrganizers(oAuthCode,Integer.parseInt(orgID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());

        post("/Event/update", "application/json",
                (request, response) ->{
                    Printing.println("/Event/update");
                    DatabaseConnection databaseConnection = new DatabaseConnection();
                    if(databaseConnection.connect == null){
                        return null;
                    }
                    int ret =  EventHandler.updateEvent( new Gson().fromJson(request.body(), EventModel.class), databaseConnection);
                    databaseConnection.close();
                    return ret;
                }
                , new JsonTransformer());
        post("/Event/create", "application/json", (request, response) -> {
                    Printing.println("/Event/create");
                    DatabaseConnection databaseConnection = new DatabaseConnection();
                    if(databaseConnection.connect == null){
                        return null;
                    }
                    EventModel ret = EventHandler.createEvent(new Gson().fromJson(request.body(), EventModel.class), databaseConnection);
                    databaseConnection.close();
                    return ret;
                }
                , new JsonTransformer());
        get("/Event/search","application/json", (request,response)->{
            Printing.println("/Event/search");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String latStr = request.queryParams("lat");
            String longStr = request.queryParams("long");
            String name = request.queryParams("name");
            String org_idStr = request.queryParams("org_id");
            List<EventModel> ret =  EventHandler.getEventsByLocation(Double.parseDouble(latStr), Double.parseDouble(longStr), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/Event/searchWithFilter","application/json",(request, response) -> {
            EventFilterModel efm = new Gson().fromJson(request.body(), EventFilterModel.class);
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            List<EventModel> ret = EventHandler.getEventsWithFilter(efm, databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/Event/searchByID", "application/json", (request, response) -> {
            Printing.println("/Event/searchByID");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            EventModel eventModel = EventHandler.searchEventID(Integer.parseInt(request.queryParams("id")), databaseConnection);
            databaseConnection.close();
            return eventModel;
        }, new JsonTransformer());
        get("/Event/searchByIDs", "application/json", (request, response) -> {
            Printing.println("/Event/searchByIDs");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String userID = request.queryParams("userID");
            String oAuthCode = request.queryParams("oAuthCode");
            List<EventModel> eventModels = EventHandler.searchEventIDS(Integer.parseInt(userID), oAuthCode, databaseConnection);
            databaseConnection.close();
            return eventModels;
        }, new JsonTransformer());
        get("/Event/users","application/json",(request,response)->{
            Printing.println("/Event/users");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String oauthcode = request.queryParams("oauthcode");
            int event_id = Integer.parseInt(request.queryParams("event_id"));
            List<Object> ret = EventHandler.getUsersAttending(oauthcode,event_id, databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/Event/numAttending", "application/json", (request, response) -> {
            Printing.println("/Event/numAttending");
            String oAuthCode = request.queryParams("oAuthCode");
            String eventID = request.queryParams("id");
            if(oAuthCode == null || eventID == null){
                Printing.print("null params");
                return new Integer(-1);
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            Integer ret = EventHandler.numAttending(oAuthCode, Integer.parseInt(eventID), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());
        get("/User/login/facebook", "application/json", (request, response) -> {
            Printing.println("/User/login/facebook");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            UserAuthenticator ua = FacebookLogin.facebookLogin(request.queryParams("id"), request.queryParams("first"), request.queryParams("last"),
                    request.queryParams("email"), databaseConnection);
            databaseConnection.close();
            return ua;
        }, new JsonTransformer());
        get("/User/unfollowOrg","application/json",(request, response) -> {
            Printing.println("/User/unfollowOrg");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            int ret = UserHandler.unfollowOrg(request.queryParams("oAuthCode"), Integer.parseInt(request.queryParams("org_id")), databaseConnection);
            databaseConnection.close();
            return ret;
        }, new JsonTransformer());

        post("/User/leaveOrg","application/json",(request, response) -> {

            Printing.println("/User/leaveOrg");
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            String oAuthCode = request.queryParams("oAuthCode");
            String orgID = request.queryParams("orgID");
            if(oAuthCode == null || orgID == null){
                Printing.println("oAuthCode is null or orgID is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            int ret = UserHandler.leaveOrganization(oAuthCode,Integer.parseInt(orgID), databaseConnection);
            databaseConnection.close();
            return ret;
        },new JsonTransformer());

        post("/User/ClockInOut","application/json",(request, response) -> {

            Printing.println("/Event/ClockIn");
            String oAuthCode = request.queryParams("oAuthCode");
            EventAttendanceModel eventAttendanceModel = new Gson().fromJson(request.body(), EventAttendanceModel.class);
            if(oAuthCode == null || eventAttendanceModel == null){
                Printing.println("oAuthCode is null or eventAttendanceModel is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            int ret = EventHandler.clockInEvent(oAuthCode, eventAttendanceModel, databaseConnection);
            databaseConnection.close();
            return ret;
        },new JsonTransformer());

        get("/User/MyLambency","application/json",(request, response) -> {

            Printing.println("/User/MyLambency");
            String oAuthCode = request.queryParams("oAuthCode");
            if(oAuthCode == null){
                Printing.println("oAuthCode is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return null;
            }
            MyLambencyModel ret = UserHandler.getMyLambency(oAuthCode, databaseConnection);
            databaseConnection.close();
            return ret;
        },new JsonTransformer());

        post("/Organization/InviteUser","application/json",(request, response) -> {

            Printing.println("/Organization/InviteUser");
            String oAuthCode = request.queryParams("oAuthCode");
            int orgID = Integer.parseInt(request.queryParams("orgID"));
            String userEmail = request.queryParams("emailString");
            if(oAuthCode == null || userEmail == null){
                Printing.println("oAuthCode is null or emailString is null. (Note: those are the correct spellings for params)");
                return -1;
            }
            DatabaseConnection databaseConnection = new DatabaseConnection();
            if(databaseConnection.connect == null){
                return -2;
            }
            int ret = OrganizationHandler.sendOrganizationInvite(oAuthCode,orgID,userEmail,databaseConnection);
            databaseConnection.close();
            return ret;
        },new JsonTransformer());


    }

    public static void main(String[]args){

        LambencyServer lb = new LambencyServer();
//
//        DatabaseConnection dbc = new DatabaseConnection();
//        if(dbc == null){
//            System.out.println("FAILURE NO DBC");
//            return;
//        }
//        UserAuthenticator ua = FacebookLogin.facebookLogin("id","first", "last", "email@mail.com",dbc );
//        //EventModel e = new EventModel("Event", 8 , new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3), "description","location", "path", 10,   100, 1200);
//        //int eventID = EventHandler.createEvent(e);
//        //UserHandler.registerEvent(ua.getoAuthCode(), eventID);
//        try {
//            UserModel organizer = dbc.searchForUser("1", DatabaseConnection.LAMBNECYUSERID);
//
//            OrganizationModel org = dbc.searchForOrg(1);
//
//            System.out.println(dbc.addGroupies(organizer.getUserId(),org.getOrgID(),DatabaseConnection.ORGANIZER,true));
//            UserModel usr = dbc.searchForUser(""+2,DatabaseConnection.LAMBNECYUSERID);
//
//            System.out.println(dbc.addGroupies(usr.getUserId(),org.getOrgID(),DatabaseConnection.MEMBER,false));
//
//            System.out.println(OrganizationHandler.getRequestedToJoinMembers(organizer.getOauthToken(),org.getOrgID(),dbc));
//            System.out.println(OrganizationHandler.respondToRequest(organizer.getOauthToken(),org.getOrgID(),usr.getUserId(),true,dbc));
//
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        dbc.close();

    }
}