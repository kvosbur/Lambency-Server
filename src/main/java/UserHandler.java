import com.google.firebase.database.FirebaseDatabase;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
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
                    ArrayList<Integer>[] user_ids = dbc.getMembersAndOrganizers(orgID);
                    FirebaseHelper.userSendOrgJoinRequest(u,org, user_ids[1], dbc);
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
                    myOrgs.add(org);
                    ArrayList<Integer> events = dbc.getOrgEvents(i);
                    for(Integer j: events){
                        EventModel event = dbc.searchEvents(j);
                        if(event != null){
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
                    joinedOrgs.add(org);
                }
            }

            //create arraylist of events that user is attending
            ArrayList<EventModel> eventsAttending = new ArrayList<>();
            for(Integer i: user.getEventsAttending()){
                EventModel event = dbc.searchEvents(i);
                if(event != null){
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
                        Printing.printlnError("null event");
                    }
                    else {
                        boolean permissionToView = true;
                        if(eventModel.getPrivateEvent()) {
                            GroupiesModel g = dbc.searchGroupies(u.getUserId(), eventModel.getOrg_id());
                            if( g==null || g.getType() < DatabaseConnection.MEMBER){
                                permissionToView = false;
                            }
                        }
                        if(permissionToView &&!u.getEventsAttending().contains(event)  && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
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
                        Printing.printlnError("null event");
                    }
                    else {
                        boolean permissionToView = true;
                        if(eventModel.getPrivateEvent()) {
                            GroupiesModel g = dbc.searchGroupies(u.getUserId(), eventModel.getOrg_id());
                            if( g==null || g.getType() < DatabaseConnection.MEMBER){
                                permissionToView = false;
                            }
                        }
                        if(permissionToView &&!u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
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
                Printing.println(org);
                ArrayList<Integer> events = dbc.getOrgEvents(org);
                for(int event: events){
                    Printing.println("EVent id: "+event);
                    EventModel eventModel = dbc.searchEvents(event);
                    if(eventModel == null){
                        Printing.printlnError("null event");
                    }
                    else {
                        boolean permissionToView = true;
                        if(eventModel.getPrivateEvent()) {
                            GroupiesModel g = dbc.searchGroupies(u.getUserId(), eventModel.getOrg_id());
                            if( g==null || g.getType() < DatabaseConnection.MEMBER){
                                permissionToView = false;
                                Printing.println("No permission to view");
                            }
                        }
                        if(permissionToView &&!u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
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
                        Printing.printlnError("null event");
                    }
                    else {
                        boolean permissionToView = true;
                        if(eventModel.getPrivateEvent()) {
                            GroupiesModel g = dbc.searchGroupies(u.getUserId(), eventModel.getOrg_id());
                            if( g==null || g.getType() < DatabaseConnection.MEMBER){
                                permissionToView = false;
                            }
                        }
                        if(permissionToView && !u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
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
                            boolean permissionToView = true;
                            if(nearby.get(i).getPrivateEvent()) {
                                GroupiesModel g = dbc.searchGroupies(u.getUserId(), nearby.get(i).getOrg_id());
                                if( g==null || g.getType() < DatabaseConnection.MEMBER){
                                    permissionToView = false;
                                }
                            }
                            if(permissionToView && !u.getEventsAttending().contains(nearby.get(i).getEvent_id()) && !eventsFeed.contains(nearby.get(i)) && !subList.contains(nearby.get(i))) {
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
                                Printing.printlnError("null event");
                            }
                            else {
                                boolean permissionToView = true;
                                if(eventModel.getPrivateEvent()) {
                                    GroupiesModel g = dbc.searchGroupies(u.getUserId(), eventModel.getOrg_id());
                                    if( g==null || g.getType() < DatabaseConnection.MEMBER){
                                        permissionToView = false;
                                    }
                                }
                                if(permissionToView && !u.getEventsAttending().contains(event) && !eventsFeed.contains(eventModel) && !subList.contains(eventModel)) {
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
            Printing.printlnError("SQLException");
            Printing.printlnError(e.toString());
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

            //get hash and salt for given passwd
            String hash = PasswordUtil.hash(passwd);//{"salt","hash"};  //implement hashing and salt creation method

            //save account information into database with a non verified email

            int id = dbc.createUser(firstName,lastName,email,hash, ""); //implement database method to insert information into table

            //send email verification to user
            String code = new String(PasswordUtil.generateSalt(30));
            dbc.userAddVerification(id,code);
            int ret = GMailHelper.sendVerificationEmail(email,id, code);

            return ret;
        } catch (Exception e) {
            Printing.println("Excpetion");
            Printing.println(e.toString());
            return 3;
        }
    }

    /**
     * Verifys the email of a user by the code it is given
     * @param userID id of user in question
     * @param verificationCode the code to use to verify the email
     * @param dbc database connection to be used in method
     * @return updated user object
     */
    public static int verifyEmail(int userID, String verificationCode, DatabaseConnection dbc){
        try{
            String storedCode = dbc.userGetVerification(userID);
            if(storedCode == null || storedCode.equals("")){
                return 2;
            }

            if(storedCode.equals(verificationCode)){
                //valid verification code
                dbc.userRemoveVerification(userID);
                UserAuthenticator ua = new UserAuthenticator(UserAuthenticator.Status.SUCCESS);
                dbc.setOauthCode(userID, ua.getoAuthCode());
                return 0;
            }
            return 3;
        } catch (Exception e) {
            Printing.println("Excpetion");
            Printing.println(e.toString());
            return 1;
        }

    }


    /**
     * Logins in a user using their email and password
     * @param email email of user to login
     * @param password unhashed password of user login attempt
     * @param dbc database connection to be used in method
     * @return UserAuthenticator object
     */
    public static UserAuthenticator lambencyLogin(String email, String password, DatabaseConnection dbc){
        try{
            //search for user by email
            int user_id = dbc.getUserByEmail(email);
            if(user_id < 0){
                return new UserAuthenticator(UserAuthenticator.Status.INVALID_LOGIN, null);
            }

            //get salt and hash for user
            String[] strings = dbc.userGetHash(user_id);
            //add salt to password and verify it against hash
            if(PasswordUtil.verify(password, strings[1])){
                //correct password
                UserModel um = dbc.searchForUser("" + user_id, DatabaseConnection.LAMBNECYUSERID);
                if(um.getOauthToken() == null || um.getOauthToken().equals("")){
                    //if email has yet to be verified
                    return new UserAuthenticator(UserAuthenticator.Status.NON_UNIQUE_EMAIL, null);
                }
                return new UserAuthenticator(UserAuthenticator.Status.SUCCESS, um.getOauthToken());
            }
            //invalid password
            return new UserAuthenticator(UserAuthenticator.Status.INVALID_LOGIN, null);
        } catch (Exception e) {
            Printing.println("Excpetion");
            Printing.println(e.toString());
            return null;
        }

    }

    /**
     * Set the firebase service code for a specific user
     *
     * @param oAuthCode         oAuthCode for user in question
     * @param firebaseCode  the code to insert for this specific user
     * @return the success code of setting the code. 0 on success, otherwise failure
     */
    public static int setFirebaseCode(String oAuthCode, String firebaseCode, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                return 1;
            }
            //get user for specific oauthcode
            UserModel user = dbc.searchForUser(oAuthCode);
            if (user == null) {
                Printing.println("UserModel not found");
                return 2;
            }

            //set the code in the database
            int ret = dbc.userSetFirebase(user.getUserId(),firebaseCode);
            if(ret == 1) {
                return 0;
            }
            return 4;

        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            Printing.println(e.toString());
            return 3;
        }
    }

    /**
     * Change the password of a given user using their oAuthToken
     *
     * @param oAuthCode oAuthCode for user in question
     * @param password  the new password of the user
     * @param confirmPassword  a duplicate of the new password to confirm right password
     * @return the success code of setting the new password
     */
    public static int changePassword(String oAuthCode, String password, String confirmPassword, String oldPassword, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                return 3;
            }
            //get user for specific oauthcode
            UserModel user = dbc.searchForUser(oAuthCode);
            if (user == null) {
                Printing.println("UserModel not found");
                return 4;
            }

            //check if old password is correct
            //get salt and hash for user
            String[] strings = dbc.userGetHash(user.getUserId());

            if(PasswordUtil.verify(oldPassword, strings[1])) {

                //check if both passwords are the same password
                if (password.equals(confirmPassword)) {
                    //the new passwords are the same
                    //change password in database
                    int ret = PasswordUtil.setPassword(password, user.getUserId(), dbc);
                    return ret;
                }
                //passwords are different
                return 5;
            }

            //incorrect old password
            return 7;


        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            Printing.println(e.toString());
            return 6;
        }
    }

    /**
     *
     * @param start the start of the range
     * @param end end of the range
     * @return returns a list of users with ranks on the leaderboard between the start and end, null on error
     */
    public static List<UserModel> leaderboardRange(int start, int end, DatabaseConnection dbc){
        if(start <= 0 || end < 0 || start > end){
            return null;

        }
        try{
            List<Integer> userIDs = dbc.leaderboardRange(start, end);
            if(userIDs == null){
                Printing.println(" dbc.leaderboardRange() returned null");
                return null;
            }
            int rank = start;
            List<UserModel> leaderboard = new ArrayList<UserModel>();
            for(int i : userIDs){
                UserModel userModel = dbc.searchForUser("" + i, DatabaseConnection.LAMBNECYUSERID);
                if(userModel.getHoursWorked() < 0.05){
                    break;
                }
                userModel.setOauthToken("" + rank);
                rank++;
                leaderboard.add(userModel);
            }

            return leaderboard;
        }
        catch (SQLException e){
            Printing.println("SQLException");
            Printing.printlnException(e);
        }
        return null;
    }

    /**
     *
     * @param oAuthCode the oAuthCode of the user
     * @param dbc
     * @return returns a list of users around the given users on the leaderboard, null on error or invalid
     */
    public static List<UserModel> leaderboardAroundUser(String oAuthCode, DatabaseConnection dbc){
        try{
            if(oAuthCode == null || dbc == null){
                Printing.println("null oAuthCode");
                return null;
            }
            //get user for specific oauthcode
            UserModel user = dbc.searchForUser(oAuthCode);
            if (user == null) {
                Printing.println("UserModel not found");
                return null;
            }
            int userRank = dbc.leaderboardRankOf(user.getUserId());
            int start = userRank - 2;
            int end = userRank + 2;
            if(userRank <= 2){
                Printing.println("User in top 2, use leaderboard range");
                return null;
            }
            List<UserModel> leaderboard = leaderboardRange(start, end, dbc);
            while(leaderboard.get(2).getUserId() != user.getUserId()){
                start++;
                end++;
                leaderboard = leaderboardRange(start, end, dbc);
            }
            return leaderboard;

        }
        catch (SQLException e){
            Printing.println("SQLException");
            Printing.printlnException(e);
        }
        return null;
    }
    /**
     * send email to user that would wish to recover their password
     *
     * @param email email of user that wants to reset their password
     * @return the success code of setting the new password
     */
    public static int beginRecoverPassword(String email, DatabaseConnection dbc){
        try{

            //get user for specific oauthcode
            int userID = dbc.getUserByEmail(email);
            if (userID < 0) {
                Printing.println("Trouble with finding email address");
                return userID;  // -1 doesn't have account , -2 not unique (shouldn't happen)
            }

            //create code for user
            String code = new String(PasswordUtil.generateSalt(30));
            if(dbc.userGetVerification(userID) != null){
                //remove previous verification if present
                dbc.userRemoveVerification(userID);
            }
            dbc.userAddVerification(userID,code);
            return GMailHelper.sendChangePasswordEmail(email, userID, code);

        } catch (Exception e) {
            Printing.println("SQLExcpetion");
            Printing.printlnException(e);
            return 2;
        }
    }

    /**
     * Change the password of a given their correct verification code
     *
     * @param verification verification code of user in question
     * @param password  the new password of the user
     * @param confirmPassword  a duplicate of the new password to confirm right password
     * @return the success code of setting the new password
     */
    public static int endRecoveryPassword(String verification, String password, String confirmPassword, int userID,  DatabaseConnection dbc){
        try{
            if(verification == null){
                return 3;
            }
            UserModel user = dbc.searchForUser("" + userID, DatabaseConnection.LAMBNECYUSERID);

            if (user == null) {
                Printing.println("Not a valid user for recovering password " + userID);
                return 4;
            }

            //get user for specific oauthcode
            String code = dbc.userGetVerification(userID);
            if (code == null) {
                Printing.println("Could not find verification code for given user");
                return 5;
            }

            if(code.equals(verification)) {

                //check if both passwords are the same password
                if (password.equals(confirmPassword)) {
                    //the new passwords are the same
                    //change password in database
                    int ret = PasswordUtil.setPassword(password, user.getUserId(), dbc);
                    dbc.userRemoveVerification(userID);
                    return ret;
                }
                //passwords are different but correct code
                return 6;
            }
            //not valid code
            return 7;
        } catch (SQLException e) {
            Printing.println("SQLExcpetion");
            Printing.println(e.toString());
            return 8;
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

    /**
     * Change the notification preference for the user
     *
     * @param oAuthCode
     * @param preference
     * @param dbc
     * @return  -1: No user Model
     *          -2: -2 Illegal preference value
     *          -3: SQL exception
     *          -4: bad SQL connection
     */
    public static Integer updateNotificationPreferences(String oAuthCode, int preference, DatabaseConnection dbc){
        if(dbc.connect == null){
            return -4;
        }
        //search for user
        if(preference < DatabaseConnection.NOTIFY_EMAIL_PUSH || preference > DatabaseConnection.NOTIFY_NOT){
            return -2;
        }
        try {
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("UserModel not found");
                return -1;
            }

            dbc.updateUserNotificationPreferences(oAuthCode,preference);
            return 0;

        } catch (SQLException e) {
            Printing.printlnException(e);
            return -3;
        }

    }

    /**
     * Set the active variable for the user
     *
     * @param oAuthCode     oAuthToken for the user that wants to have their active state set
     * @param isActive      boolean for user if they are active or not
     * @param dbc           databse connection
     * @return              0 on success
     *                      -1 on prepared statement issue
     *                      -2 on no user found
     *                      -3 on SQL exception
     *                      -4 on databse connect issue
     */
    public static Integer updateActiveStatus(String oAuthCode, boolean isActive, DatabaseConnection dbc){
        if(dbc.connect == null){
            Printing.printlnError("Database connect error");
            return -4;
        }
        try {
            UserModel u =dbc.searchForUser(oAuthCode);
            if(u == null){
                Printing.println("UserModel not found");
                return -2;
            }

            return dbc.setUserActiveStatus(u.getUserId(),isActive);

        } catch (SQLException e) {
            Printing.printlnException(e);
            return -3;
        }
    }

    /**
     *                      Retrieve the active status of the user with USer_id
     *
     * @param oAuthCode     oAuthCode of who is making the request
     * @param user_id       User_id for who wants to have the active status known
     * @param dbc           Database connection
     * @return              0 if inactivce
     *                      1 if active
     *                      -1 on query fail
     *                      -2 on no user found
     *                      -3 on SQL exceptipn
     *                      -4 on no database connect
     */
    public static Integer getActiveStatus(String oAuthCode, int user_id, DatabaseConnection dbc) {
        if (dbc.connect == null) {
            Printing.printlnError("Database connect error");
            return -4;
        }
        try {
            UserModel u = dbc.searchForUser(oAuthCode);
            if (u == null) {
                Printing.println("UserModel not found");
                return -2;
            }

            return dbc.getUserActiveStatus(user_id);
        } catch (SQLException e) {
            Printing.printlnException(e);
            return -3;
        }
    }

    /**
     * Method for creating new Chat
     *
     * @param oAuthCode
     * @param user2_id
     * @param groupchat
     * @param dbc
     * @return
     */
    public static ChatModel createNewChat(String oAuthCode, int user2_id, boolean groupchat, DatabaseConnection dbc){
        try {
            UserModel u1 = dbc.searchForUser(oAuthCode);
            UserModel u2 = dbc.searchForUser(""+user2_id,DatabaseConnection.LAMBNECYUSERID);
            if (u1 == null || u2 == null) {
                Printing.printlnError("UserModel not found");
                return null;
            }
            if(dbc.chatExists(u1.getUserId(), u2.getUserId())){
                Printing.printlnError("Chat already exists");
                return null;
            }

            String one = u1.getFirstName()+" "+u1.getLastName();
            String two = u2.getFirstName()+" "+u2.getLastName();

            ChatModel c = new ChatModel(dbc.createChat(u1.getUserId(),user2_id,one,two,groupchat),two,0,"",u2.getUserId());
            FirebaseDatabase.getInstance().getReference().child("chats").child("" + c.getChatID()).setValueAsync("EMPTY");

            return c;

        } catch (SQLException e) {
            Printing.printlnException(e);
            return null;
        }
    }

    /**
     * Retrives all the chat models for one user
     *
     * @param oAuthCode     oAuthCode of the user
     * @param dbc
     * @return              Array list of ChatModels
     */
    public static ArrayList<ChatModel> getAllChatModels(String oAuthCode, DatabaseConnection dbc){
        try {
            UserModel u1 = dbc.searchForUser(oAuthCode);
            if (u1 == null) {
                Printing.printlnError("UserModel not found");
                return null;
            }

            return dbc.getChat(u1.getUserId(),false);

        } catch (SQLException e) {
            Printing.printlnException(e);
            return null;
        }
    }




     /**
     * @param oAuthCode the oAuthCode of the user
     * @param dbc database connection
     * @return the list of orgs that have requested for the user to join
     */
    public static ArrayList<OrganizationModel> getRequestedToJoinOrgs(String oAuthCode, DatabaseConnection dbc){
        try {
            if (oAuthCode == null || dbc == null) {
                Printing.println("null oAuthCode");
                return null;
            }
            //get user for specific oauthcode
            UserModel user = dbc.searchForUser(oAuthCode);
            if (user == null) {
                Printing.println("UserModel not found");
                return null;
            }
            ArrayList<Integer> orgIDs = dbc.getRequestsToJoinOrgs(user.getUserId());
            if(orgIDs == null){
                Printing.println("dbc.getRequestsToJoinOrgs returned null");
                return null;
            }
            ArrayList<OrganizationModel> orgs = new ArrayList<OrganizationModel>();
            for(int i: orgIDs){
                orgs.add(OrganizationHandler.searchOrgID(i, dbc));
            }
            return orgs;
        }
        catch (SQLException e){
            Printing.printlnException(e);
        }
        return null;

    }

    /**
     *
     * @param oAuthCode the oAuthCode of the user responding to request
     * @param orgID id of the org that is being accepted or rejected
     * @param accecpt true = accept request, false = reject request
     * @param dbc database connection
     * @return 0 on success for accepting request, 1 on success for rejecting request, -1 on error
     */
    public static Integer respondToRequest(String oAuthCode, int orgID, boolean accecpt, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                Printing.println("bad oAuthCode");
                return -1;
            }
            UserModel user = dbc.searchForUser(oAuthCode);
            if(user == null){
                Printing.println("cannot find user");
                return -1;
            }
            OrganizationModel org = dbc.searchForOrg(orgID);
            if(org == null){
                Printing.println("Organization does not exist. Try again next time" + orgID);
                return -1;
            }
            ArrayList<OrganizationModel> orgs = getRequestedToJoinOrgs(oAuthCode, dbc);
            if(!orgs.contains(org)){
                Printing.println("No request for user id = " + user.getUserId());
                return -1;
            }
            // all params are correct

            if(accecpt){
                dbc.approveMemberGroupie(user.getUserId(),orgID);
                return 0;
            }
            else{
                dbc.deleteGroupies(user.getUserId(),orgID,DatabaseConnection.MEMBER);
                return 1;
            }


        }
        catch (SQLException e){
            Printing.printlnException(e);
            return new Integer(-1);
        }
    }

    /**
     * Given the oauthcode for a user return a list of all users that are connected to that user.
     * @param oAuthCode the oAuthCode of the user responding to request
     * @param dbc database connection
     * @return Arraylist of usermodels
     */
    public static ArrayList<UserModel> getRelatedUsers(String oAuthCode, DatabaseConnection dbc){
        ArrayList<UserModel> ret = new ArrayList<>();
        try{
            if(oAuthCode == null){
                Printing.println("bad oAuthCode");
                return ret;
            }
            UserModel user = dbc.searchForUser(oAuthCode);
            if(user == null){
                Printing.println("cannot find user");
                return ret;
            }

            //database method for finding orgs that this user is either a member of or an organizer for
            ArrayList<Integer> myOrgs = dbc.getUserList(user.getUserId(),DatabaseConnection.ORGANIZER, true);
            ArrayList<Integer> memberOrgs = dbc.getUserList(user.getUserId(),DatabaseConnection.MEMBER, true);

            myOrgs.addAll(memberOrgs);

            //get all users associated with those orgs and compile a list that does not have duplicates
            for(Integer orgID: myOrgs){
                ArrayList<Integer>[] members = dbc.getMembersAndOrganizers(orgID);
                //iterate through all organizers of organization
                for(Integer userID: members[0]){
                    UserModel u = dbc.searchForUser("" + userID, DatabaseConnection.LAMBNECYUSERID);
                    if(u != null){
                        if(u.getUserId() != user.getUserId()) {
                            ret.add(u);
                        }
                    }
                }

                //iterate through all members of organization
                for(Integer userID: members[1]){
                    UserModel u = dbc.searchForUser("" + userID, DatabaseConnection.LAMBNECYUSERID);
                    if(u != null){
                        if(u.getUserId() != user.getUserId()) {
                            ret.add(u);
                        }
                    }
                }

            }

            //alphabetically sort all of the users (lastname then firstname)
            ret.sort(new Comparator<UserModel>() {
                @Override
                public int compare(UserModel o1, UserModel o2) {
                    if(o1.getLastName().compareTo(o2.getLastName()) == 0){
                        return o1.getFirstName().compareTo(o2.getFirstName());
                    }
                    return o1.getLastName().compareTo(o2.getLastName());
                }
            });

            //remove any duplicates that may be present
            for(int i = 1; i < ret.size() - 1; i++){
                UserModel prev = ret.get(i-1);
                UserModel next = ret.get(i);
                if(prev.getUserId() == next.getUserId()){
                    ret.remove(i);
                }
            }


            return ret;
        }
        catch (Exception e){
            Printing.printlnException(e);
            return ret;
        }
    }

    /**
     * Given the oauthcode for a user return a list of all users that are connected to that user.
     * @param oAuthCode the oAuthCode of the user responding to request
     * @param dbc database connection
     * @return Arraylist of usermodels
     */
    public static int sendMessage(String oAuthCode, int chatID, MessageModel messageModel, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                Printing.println("bad oAuthCode");
                return -1;
            }
            UserModel user = dbc.searchForUser(oAuthCode);
            if(user == null){
                Printing.println("cannot find user");
                return -1;
            }

            //get current max message num
            ChatModel chatModel = dbc.getSpecificChat(chatID,user.getUserId());
            if(chatModel == null){
                return -1;
            }

            //update chat model
            int msg_id = chatModel.getRecent_msg_id() + 1;
            int ret = dbc.updateChat(chatModel.getChatID(), msg_id, messageModel.messageText);
            if(ret == 1){
                return -2;
            }

            //send message into the firebase database
            /*
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("chats/" + chatModel.getChatID());
            myRef.child("" + msg_id).setValueAsync(messageModel);
            */
            /* COULD'T GET IT TO WORK.....
            FirebaseFirestore db = FirestoreClient.getFirestore();
            db.collection("chats").document("" + chatModel.getChatID())
                    .collection("messages")
                    .document("" + msg_id)
                    .set(messageModel);
            */


            //send notification to other user about message
            String token = dbc.userGetFirebase(chatModel.getUserID());
            FirebaseHelper.sendCloudChatMessage(token,"" + msg_id, "" +chatModel.getChatID(), user.getFirstName() + " " + user.getLastName());

            return msg_id;
        }
        catch (Exception e){
            Printing.printlnException(e);
            return -1;
        }
    }

    /**
     * returns a list of objects of event models and the hours worked at that event
     * @param oAuthCode the oAuthCode of the user
     * @param dbc
     * @return
     */
    public static ArrayList<EventModel> pastEvents(String oAuthCode, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                Printing.println("bad oAuthCode");
                return null;
            }
            UserModel user = dbc.searchForUser(oAuthCode);
            if(user == null){
                Printing.println("cannot find user");
                return null;
            }
            ArrayList<EventModel> events = new ArrayList<>();
            List<Integer> eventIDs = dbc.searchUserEventAttendanceClockedOut(user.getUserId());
            if(eventIDs == null){
                Printing.println("dbc.searchUserEventAttendanceClockedOut returned null");
                return null;
            }
            for(int i: eventIDs){
                EventModel eventModel = EventHandler.searchEventID(i, dbc);
                EventAttendanceModel eventAttendanceModel = dbc.searchEventAttendance(user.getUserId(), eventModel.getEvent_id());
                eventModel.setDescription("" + eventAttendanceModel.getHoursWorked());
                events.add(eventModel);
            }
            return events;
        }
        catch (Exception e){
            Printing.printlnException(e);
        }
        return null;

    }

    public static ArrayList<EventModel> pastEventsInOrg(String oAuthCode, int userID, int orgID, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                Printing.println("bad oAuthCode");
                return null;
            }
            UserModel admin = dbc.searchForUser(oAuthCode);
            if(admin == null){
                Printing.println("cannot find admin");
                return null;
            }
            OrganizationModel org = OrganizationHandler.searchOrgID(orgID, dbc);
            if(org == null){
                Printing.println("cannot find org");
                return null;
            }
            if(!OrganizationHandler.isAdmin(oAuthCode, orgID, dbc)){
                if(admin.getUserId() != userID) {
                    Printing.println("user is not admin of org");
                    return null;
                }
            }
            UserModel user = dbc.searchForUserByID(userID);
            if(user == null){
                Printing.println("cannot find user");
                return null;
            }
            GroupiesModel groupiesModel = dbc.searchGroupies(userID, orgID);
            if(groupiesModel == null){
                Printing.println("user is not a member of org");
                return null;
            }
            ArrayList<EventModel> events = new ArrayList<>();
            List<Integer> eventIDs = dbc.searchUserEventAttendanceInOrg(userID, orgID);
            if(eventIDs == null){
                Printing.println("dbc.searchUserEventAttendanceInOrg returned null");
                return null;
            }
            for(int i: eventIDs){
                EventModel eventModel = EventHandler.searchEventID(i, dbc);
                EventAttendanceModel eventAttendanceModel = dbc.searchEventAttendance(user.getUserId(), eventModel.getEvent_id());
                eventModel.setDescription("" + eventAttendanceModel.getHoursWorked());
                events.add(eventModel);
            }
            return events;
        }
        catch (Exception e){
            Printing.printlnException(e);
        }
        return null;

    }
}
