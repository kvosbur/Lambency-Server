import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserHandler {
    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param orgID id of organization to be followed
     * @return returns 0 on success, 1 if not able to find user or org or groupies already exists, 2 on SQLException
     */
    public static Integer followOrg(String oAuthCode, int orgID, DatabaseConnection dbc){
        try {
            //search for user
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = dbc.searchGroupies(u.getUserId(), orgID);
                if (g == null) {
                    //if org is found and no groupies already exist, set to follow in database
                    dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW, 1);
                    return 0;
                } else {
                    //GroupiesModel already exist for this user
                    Printing.println("GroupiesModel already exists with equal or higher permissions");
                    return 1;
                }
            }
            else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        }
        catch (SQLException e){
            Printing.println("SQLExcpetion");
            return 2;
        }
    }


    /**
     * Unfollow an organization
     *
     * @param oAuthCode     Code of user who made the request
     * @param orgID         Id of organization to unfollow
     * @return              0 if success, 1 if could not locate user or organization or fails to delete, and 2 if SQL failure
     */
    public static Integer unfollowOrg(String oAuthCode, int orgID, DatabaseConnection dbc){
        try {
            //search for user
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = dbc.searchGroupies(u.getUserId(), orgID);
                if (g != null) {
                    //if org is found and a groupie exists delete it if it is a follow
                    // deleteGroupies returns a -1 if failure
                    return -1 * dbc.deleteGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW);
                } else {
                    //GroupiesModel already exist for this user
                    Printing.println("Following of OrganizationModel doesn't exist.");
                    return 1;
                }
            }
            else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        }
        catch (SQLException e){
            Printing.println("SQLExcpetion");
            return 2;
        }
    }


    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param orgID id of organization to request to joi
     * @return returns 0 on success, 1 if not able to find user or org or groupies already exists, 2 on SQLException
     */
    public static Integer requestJoinOrg(String oAuthCode, int orgID, DatabaseConnection dbc) {
        try {
            //search for user
            if (dbc.searchForUser(oAuthCode) == null) {
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            OrganizationModel org = dbc.searchForOrg(orgID);
            if (org != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if already has a groupie for this org
                GroupiesModel g = dbc.searchGroupies(u.getUserId(), orgID);
                if (g == null) {
                    //if org is found and no groupies already exist, set to follow in database
                    dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, 0);
                    String firebase_token = dbc.userGetFirebase(u.getUserId());
                    FirebaseHelper.sendCloudJoinRequest(firebase_token,u.getFirstName() + " " + u.getLastName(),"" + u.getUserId(),
                            org.name, "" + org.getOrgID());
                    return 0;
                } else {
                    //GroupiesModel already exist for this user
                    if(g.getType() == DatabaseConnection.FOLLOW){
                        //upgrade to a member
                        dbc.deleteGroupies(u.getUserId(), orgID, DatabaseConnection.FOLLOW);
                        dbc.addGroupies(u.getUserId(), orgID, DatabaseConnection.MEMBER, 0);
                        String firebase_token = dbc.userGetFirebase(u.getUserId());
                        FirebaseHelper.sendCloudJoinRequest(firebase_token,u.getFirstName() + " " + u.getLastName(),"" + u.getUserId(),
                                org.name, "" + org.getOrgID());
                        return 0;
                    }
                    //user already has higher or equal permissions
                    Printing.println("GroupiesModel already exists with equal or higher permissions");
                    return 1;
                }
            } else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            return 2;
        }
    }


    /**
     *
     * @param oAuthCode String oAuthCode from user
     * @param orgID     Int orgID of the organization you want to leave.
     * @return      -1 if exception is caught, 1 if user not found, 2 if org does not exist, 3 if not a member of organization,
     *              0 if confirmed and deleted, 100 if not confirmed but deleted;
     */
    public static Integer leaveOrganization(String oAuthCode, int orgID, DatabaseConnection dbc){
        try {
            UserModel usr = dbc.searchForUser(oAuthCode);
            if(usr != null){
                OrganizationModel org = dbc.searchForOrg(orgID);
                if(org != null){
                    GroupiesModel gp = dbc.searchGroupies(usr.getUserId(),orgID);
                    if(gp != null){
                        int toReturn = dbc.deleteGroupies(usr.getUserId(),orgID, gp.getType());
                        if(toReturn == -1){
                            Printing.println("dbc.deleteGroupies returned -1;");
                            return -1;
                        }
                        else if(gp.confirmed){
                            return 0;
                        }
                        else{
                            return 100;
                        }
                    }
                    else{
                        Printing.println("There is no membership with org, so you can not leave the organizaiton.");
                        return 3;
                    }
                }
                else{
                    Printing.println("No organization found.");
                    return 2;
                }
            }
            else{
                Printing.println("No user found.");
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Printing.println("SQL EXCEPTION in Leave Organization");
            return -1;
        }
    }


    /**
     *
     * @param oAuthCode the oAuthCode of the given user
     * @param eventID the id of the event
     * @return 0 on success, 1 on failing to find user or organization, 2 on SQL exception, 3 on already registered
     */
    public static Integer registerEvent(String oAuthCode, int eventID, DatabaseConnection dbc){
        try {
            //search for user
            if (dbc.searchForUser(oAuthCode) == null) {
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            EventModel event = dbc.searchEvents(eventID);
            if (event != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if user is already registered for an event
                EventAttendanceModel ea = dbc.searchEventAttendance(u.getUserId(), eventID);
                if (ea == null) {
                    //if event is found and no event attended already exist, register user for event
                    dbc.registerForEvent(u.getUserId(), eventID);
                    return 0;
                } else {
                    //UserModel is already registered
                    Printing.println("UserModel is already registered for the event");
                    return 3;
                }
            } else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            return 2;
        }
    }

    /**
     *
     * @param oAuthCode the oAuthCode of the given user
     * @param eventID the id of the event
     * @return 0 on success, 1 on failing to find user or organization, 2 on SQL exception, 3 on not registered
     */
    public static Integer unRegisterEvent(String oAuthCode, int eventID, DatabaseConnection dbc){
        try {
            //search for user
            if (dbc.searchForUser(oAuthCode) == null) {
                Printing.println("UserModel not found");
                return 1;
            }
            //search for organization by ID
            EventModel event = dbc.searchEvents(eventID);
            if (event != null) {
                UserModel u = dbc.searchForUser(oAuthCode);
                //check if user is already registered for an event
                EventAttendanceModel ea = dbc.searchEventAttendance(u.getUserId(), eventID);
                if (ea == null) {
                    //if event is found and no event attended already exist, register user for event
                    //dbc.registerForEvent(u.getUserId(), eventID);
                    Printing.println("UserModel is not registered for the event, so it can not unregister.");
                    return 3;
                } else {
                    //UserModel is already registered
                    dbc.unRegisterForEvent(u.getUserId(),eventID);
                    return 0;
                }
            } else {
                //org is not found, return error
                Printing.println("OrganizationModel not found");
                return 1;
            }
        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            return 2;
        }
    }



    /**
     *
     * @param oAuthCode oAuthCode for the user
     * @param userID id of user to search for
     * @return returns UserModel object on success or null if failure
     */
    public static UserModel searchForUser(String oAuthCode, String userID, DatabaseConnection dbc) {
        try {
            //search for user
            UserModel user = dbc.searchForUser(oAuthCode);
            if (userID != null && user == null) {
                Printing.println("UserModel not found");
                return null;
            }
            //search for organization by ID
            if(userID == null){
                user =  dbc.searchForUser(oAuthCode);
                if(user != null) {
                    user = UserHandler.updateOrgLists(user, dbc);
                }else{
                    Printing.println("Can't found user model!" + oAuthCode);
                }
                return user;
            }else{
                user = dbc.searchForUser(userID, DatabaseConnection.LAMBNECYUSERID);
                user = UserHandler.updateOrgLists(user, dbc);
                return user;
            }

        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            Printing.println(e.toString());
            return null;
        }
    }

    /**
     * Changes the account information to the information in the user object
     * @param u the user object with the info to be changed in the database
     * @return updated user object from the database
     */
    public static UserModel changeInfo(UserModel u, DatabaseConnection dbc){
        try {
            if(u == null){
                Printing.println("Null user");
                return null;
            }
            UserModel user = dbc.searchForUser(u.getOauthToken());
            if(user == null){
                Printing.println("UserModel not found");
                return null;
            }
            if(u.getEmail() == null || u.getLastName() == null| u.getFirstName() == null){
                Printing.println("UserModel info invalid");
                return null;
            }
            u = dbc.modifyUserInfo(user.getUserId(), u.getFirstName(), u.getLastName(), u.getEmail());
            u = UserHandler.updateOrgLists(u, dbc);
            return u;
        }
        catch (SQLException e){
            Printing.println("SQLException");
            Printing.println(e.toString());
            return null;
        }
    }

    /**
     * Changes the account information to the information in the user object
     * @param oAuthCode oauthcode of user requesting mylambency info
     * @return updated user object from the database
     */
    public static MyLambencyModel getMyLambency(String oAuthCode, DatabaseConnection dbc){
        try {
            UserModel user = dbc.searchForUser(oAuthCode);
            if(user == null){
                Printing.println("User Model not found");
                return null;
            }
            user = updateOrgLists(user, dbc);
            //create arraylist of organization models from myOrgs also create arraylist of events that user is organizer for
            ArrayList<OrganizationModel> myOrgs = new ArrayList<>();
            ArrayList<EventModel> eventsOrganizing = new ArrayList<>();
            for(Integer i: user.getMyOrgs()){
                OrganizationModel org = dbc.searchForOrg(i);
                if(org != null){
                    if(org.getImage() != null) {
                        org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
                    }
                    myOrgs.add(org);
                    ArrayList<Integer> events = dbc.getOrgEvents(i);
                    for(Integer j: events){
                        EventModel event = dbc.searchEvents(j);
                        if(event != null){
                            event.setImageFile(ImageWR.getEncodedImageFromFile(event.getImage_path()));
                            eventsOrganizing.add(event);
                        }
                    }
                }
            }

            //create arraylist of organization models from joinedOrgs
            ArrayList<OrganizationModel> joinedOrgs = new ArrayList<>();
            for(Integer i: user.getJoinedOrgs()){
                OrganizationModel org = dbc.searchForOrg(i);
                if(org != null){
                    if(org.getImage() != null) {
                        org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
                    }
                    joinedOrgs.add(org);
                }
            }

            //create arraylist of events that user is attending
            ArrayList<EventModel> eventsAttending = new ArrayList<>();
            for(Integer i: user.getEventsAttending()){
                EventModel event = dbc.searchEvents(i);
                if(event != null){
                    event.setImageFile(ImageWR.getEncodedImageFromFile(event.getImage_path()));
                    eventsAttending.add(event);
                }
            }
            return new MyLambencyModel(user,myOrgs,joinedOrgs,eventsAttending,eventsOrganizing);
        }
        catch (SQLException e){
            Printing.println("SQLException");
            Printing.println(e.toString());
            return null;
        }catch(Exception e){
            Printing.println("General Exception");
            Printing.println(e.toString());
            return null;
        }
    }

    /**
     *
     * @param oAuthCode the code of the user
     * @param dbc database connection to the server
     * @param latitude latitude of the user
     * @param longitude longitude of the user
     * @return list of events to appear in events feed, on error null
     */
    public static List<EventModel> eventsFeed(String oAuthCode, String latitude, String longitude, DatabaseConnection dbc){
        try {
            UserModel u = dbc.searchForUser(oAuthCode);
            if(u == null){
                return null;
            }
            List<EventModel> eventsFeed = new ArrayList<EventModel>();
            List<EventModel> subList = new ArrayList<EventModel>();
            u = updateOrgLists(u, dbc);
            List<Integer> list = u.getMyOrgs();
            for(int org: list){
                ArrayList<Integer> events = dbc.getOrgEvents(org);
                for(int event: events){
                    EventModel eventModel = dbc.searchEvents(event);
                    if(eventModel == null){
                        Printing.println("null event");
                    }
                    else {
                        eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                        if (!u.getEventsAttending().contains(event)  && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
                            subList.add(eventModel);
                        }
                    }
                }
            }
            //sort by date
            subList = EventHandler.sortEventListByDate(subList);
            //add to eventsFeed
            eventsFeed.addAll(subList);
            subList = new ArrayList<EventModel>();

            for(int org: list){
                ArrayList<Integer> events = dbc.getEndorsedEvents(org);
                for(int event: events){
                    EventModel eventModel = dbc.searchEvents(event);
                    if(eventModel == null){
                        Printing.println("null event");
                    }
                    else {
                        eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                        if (!u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
                            subList.add(eventModel);
                        }
                    }
                }
            }
            //sort by date
            subList = EventHandler.sortEventListByDate(subList);
            //add to eventsFeed
            eventsFeed.addAll(subList);
            subList = new ArrayList<EventModel>();

            list = u.getFollowingOrgs();
            for(int org: list){
                ArrayList<Integer> events = dbc.getOrgEvents(org);
                for(int event: events){
                    EventModel eventModel = dbc.searchEvents(event);
                    if(eventModel == null){
                        Printing.println("null event");
                    }
                    else {
                        eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                        if (!u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
                            subList.add(eventModel);
                        }
                    }
                }
            }
            //sort by date
            subList = EventHandler.sortEventListByDate(subList);
            //add to eventsFeed
            eventsFeed.addAll(subList);
            subList = new ArrayList<EventModel>();

            for(int org: list){
                ArrayList<Integer> events = dbc.getEndorsedEvents(org);
                for(int event: events){
                    EventModel eventModel = dbc.searchEvents(event);
                    if(eventModel == null){
                        Printing.println("null event");
                    }
                    else {
                        eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                        if (!u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
                            subList.add(eventModel);
                        }
                    }
                }
            }
            //sort by date
            subList = EventHandler.sortEventListByDate(subList);
            //add to eventsFeed
            eventsFeed.addAll(subList);
            boolean doDate = false;
            if (eventsFeed.size() < 20){
                if(latitude!= null && longitude != null) {
                    double lat = Double.parseDouble(latitude);
                    double longit = Double.parseDouble(longitude);
                    List<EventModel> nearby = EventHandler.getEventsByLocation(lat, longit, dbc);
                    if(nearby != null) {
                        nearby = EventHandler.sortEventListByDate(nearby);
                        int i = 0;
                        while (eventsFeed.size() < 20 && i < nearby.size()) {
                            if (!u.getEventsAttending().contains(nearby.get(i).getEvent_id()) && !eventsFeed.contains(nearby.get(i)) && !subList.contains(nearby.get(i))) {
                                if(nearby.get(i) != null) {
                                    eventsFeed.add(nearby.get(i));
                                }
                            }
                            i++;
                        }
                    }
                    else{
                        doDate = true;
                    }
                }
                else{
                    doDate = true;
                }
                if(doDate || eventsFeed.size() < 20){
                    List<Integer> events = dbc.searchEventsByDateTime(new Timestamp(System.currentTimeMillis()));
                    if(events != null) {
                        for (int event : events) {
                            EventModel eventModel = dbc.searchEvents(event);
                            if(eventModel == null){
                                Printing.println("null event");
                            }
                            else {
                                eventModel.setImageFile(ImageWR.getEncodedImageFromFile(eventModel.getImage_path()));
                                if (!u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
                                    subList.add(eventModel);
                                }
                            }
                        }
                    }
                    eventsFeed.addAll(subList);
                    if(eventsFeed.size()> 20){
                        eventsFeed = eventsFeed.subList(0, 20);
                    }
                }
            }
            else{
                eventsFeed = eventsFeed.subList(0 , 20);
            }
            return eventsFeed;
        }
        catch (SQLException e){
            Printing.println("SQLException");
            Printing.println(e.toString());
        }
//        catch (Exception e){
//            Printing.println("General Exception");
//            e.printStackTrace();
//            Printing.println(e.toString());
//        }
        return null;
    }


    /**
     * Changes the account information to the information in the user object
     * @param u the user object to update the arraylist of orgs for
     * @return updated user object
     */

    /**
     * Returns all the organizationModels for organizations where the user is an ORGANIZER
     *
     * @param oAuthCode         oAuthCode for user who is trying to get ORGS
     * @return      ArrayList of Organziation models that they are organizers for
     * @throws SQLException         Problem
     */
    public static ArrayList<OrganizationModel> getMyOrganizations(String oAuthCode, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                return null;
            }
            UserModel user = dbc.searchForUser(oAuthCode);
            if (user == null) {
                Printing.println("UserModel not found");
                return null;
            }
            //search for organization by ID
            user.setMyOrgs(dbc.getUserList(user.getUserId(),DatabaseConnection.ORGANIZER, true));
            ArrayList<OrganizationModel> orgs = new ArrayList<>();
            for(Integer org_id:user.getMyOrgs()){
                OrganizationModel organization = dbc.searchForOrg(org_id);
                if(organization.getImage() != null) {
                    organization.setImage(ImageWR.getEncodedImageFromFile(organization.getImage()));

                }
                orgs.add(organization);
            }
            return orgs;

        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            Printing.println(e.toString());
            return null;
        }
    }


    /**
     * Registers new Lambency account with given information
     * @param firstName firstname of user
     * @param lastName lastname of user
     * @param email  email account of user
     * @param passwd password of user
     * @param dbc database connection to be used in method
     * @return updated user object
     */
    public static int register(String firstName, String lastName, String email,
            String passwd, DatabaseConnection dbc){
        try{
            //verify unique email address
            int uniqueEmail = dbc.verifyUserEmail(email);

            //if not unique return now with value for non unique email
            if(uniqueEmail == -1){
                return 2;
            }

            //send email verification to user

            //get hash and salt for given passwd
            String[] pair = {"salt","hash"};  //implement hashing and salt creation method

            //save account information into database with a non verified email

            int success = dbc.createUser(firstName,lastName,email,pair[0], pair[1]); //implement database method to insert information into table

            return success;
        } catch (Exception e) {
            Printing.println("Excpetion");
            Printing.println(e.toString());
            return 1;
        }
    }



    private static UserModel updateOrgLists(UserModel u, DatabaseConnection dbc) throws SQLException{

        u.setMyOrgs(dbc.getUserList(u.getUserId(),DatabaseConnection.ORGANIZER, true));
        u.setJoinedOrgs(dbc.getUserList(u.getUserId(),DatabaseConnection.MEMBER, true));
        u.setFollowingOrgs(dbc.getUserList(u.getUserId(),DatabaseConnection.FOLLOW, true));
        u.setEventsAttending(dbc.searchUserEventAttendance(u.getUserId()));
        u.setRequestedJoinOrgIds(dbc.getUserList(u.getUserId(),DatabaseConnection.MEMBER, false));
        return u;
    }



}
