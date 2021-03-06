
import com.google.maps.model.LatLng;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrganizationHandler {


    /**
     * This method will take an organziation and save it in the databse
     *
     * @param org the OrganizationModel to be saved in the databse
     * @return an integer representing the org_id on success and an error code on failure.
     *           -1 Database Error | -2 Non Determinant Error
     */
    public static OrganizationModel createOrganization(OrganizationModel org, DatabaseConnection dbc){

        //check if organization with same name already exists
        try {
            OrganizationModel organization = dbc.searchForOrg(org.name);
            if (organization != null) {
                organization.name = null;
                return organization;
            }
        }catch(Exception e){
            Printing.println(e.toString());
        }

        LatLng latlng = GoogleGeoCodeUtil.getGeoData(org.getLocation());
        if(latlng == null){
            latlng = new LatLng(180,180);
        }

        org.setLattitude(latlng.lat);
        org.setLongitude(latlng.lng);

        // Saves the orgs image to a file
        String path = null;
        int status;
        if(org.getImageFile() != null) {
            try {
                Printing.println("imageFile not null");
                path = ImageWR.writeImageToFile(org.getImageFile());
                //path = ImageWR.saveImage(org.getImageToSave());

            } catch (IOException e) {
                Printing.println("Error adding image for org " + org.getName() + ".");
            }
        }else{
            Printing.println("imageFile null");
        }
        try {
            status = dbc.createOrganization(org.getName(), org.getDescription(), org.getEmail(), org.getContact()
                    .getUserId(), org.getLocation(), path, org.getOrganizers().get(0).getUserId(),latlng.lat,latlng.lng);
            OrganizationModel organization = dbc.searchForOrg(status);
            //image is currently storing path so change it to store
            //organization.setImage(ImageWR.getEncodedImageFromFile(organization.getImage()));
            return organization;
        }
        catch (Exception e){
            status = -2;
            Printing.println(e.toString());
        }
        return null;

    }


    /**
     * Searches for organizations beginning with the substring name
     * @param name name of the organization to be searched for
     * @return an ArrayList of organizations
     */
    public static ArrayList<OrganizationModel> searchOrgName(String name, DatabaseConnection dbc){
        ArrayList<OrganizationModel> array;
        try {
            array = dbc.searchForOrgArray(name);

            /*
            //for each organization in the array
            for(OrganizationModel org: array){
                if(org.getImage() != null) {
                    //org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
                }
            }
            */
        }
        catch (SQLException e){
            array = new ArrayList<OrganizationModel>();
        }catch(Exception e){
            array = new ArrayList<OrganizationModel>();
        }
        return array;
    }

    /**
     *
     * @param orgID the id of the organization
     * @return the organization object for the id, otherwise null
     */
    public static OrganizationModel searchOrgID(int orgID, DatabaseConnection dbc) {

        try {
            OrganizationModel organization = dbc.searchForOrg(orgID);

            ArrayList<UserModel>[] memsandorgs = new ArrayList[2];
            memsandorgs[0] = new ArrayList<>();
            memsandorgs[1] = new ArrayList<>();
            ArrayList<Integer>[] user_ids = dbc.getMembersAndOrganizers(orgID);
            for(Integer i: user_ids[0]){
                UserModel user = dbc.searchForUser(""+i, DatabaseConnection.LAMBNECYUSERID);
                memsandorgs[0].add(user);
            }
            for(Integer i: user_ids[1]){
                UserModel user = dbc.searchForUser(""+i, DatabaseConnection.LAMBNECYUSERID);
                memsandorgs[1].add(user);
            }

            organization.setMembers(memsandorgs[0]);
            organization.setOrganizers(memsandorgs[1]);

            /*
            if(organization.getImage() != null) {
                organization.setImage(ImageWR.getEncodedImageFromFile(organization.getImage()));
            }
            */
            return organization;
        } catch (SQLException e) {
            Printing.println("Error in finding organization");
            return null;
        } catch (Exception e) {
            System.out.println("General Error SearchOrgID");
            e.printStackTrace();
            return null;
        }

    }

    /**
     * @param oAuthCode the authorization code of the user
     * @param orgID the id of the organization
     * @return a list of the events of that organization
     */
    public static ArrayList<EventModel> searchEventsByOrg(String oAuthCode, int orgID, DatabaseConnection dbc){
        ArrayList<EventModel> list = new ArrayList<EventModel>();
        try {
            if(oAuthCode == null){
                return null;
            }
            UserModel u = dbc.searchForUser(oAuthCode);
            if(u  == null){
                Printing.println("Unable to verify user");
                return null;
            }
            ArrayList<Integer> ids = dbc.getOrgEvents(orgID);
            if(ids == null || ids.size() == 0){
                Printing.println("Organization has no events");
                return list;
            }

            for(int i: ids){
                GroupiesModel g = dbc.searchGroupies(u.getUserId(),orgID);
                EventModel e = EventHandler.searchEventID(i, dbc);
                if(!e.getPrivateEvent() || (g != null && g.getType() >= DatabaseConnection.MEMBER)) {
                    list.add(e);
                }
            }
            return list;
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return null;
    }
    /**
     * @param oAuthCode the authorization code of the user
     * @param orgID the id of the organization
     * @param eventID the id of the event to be endorsed
     * @return 0 on success, -3 on failure to verify parameters, -1 on database error, -2 on already endorsed
     */
    public static Integer endorseEvent(String oAuthCode, int orgID, int eventID, DatabaseConnection dbc){
        try {
            if(oAuthCode == null){
                return new Integer(-3);
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return new Integer(-3);
            }
            if(searchOrgID(orgID, dbc) == null){
                Printing.println("Error in finding organization");
                return new Integer(-3);
            }
            if(!isAdmin(oAuthCode,orgID, dbc)){
                Printing.println("User is not an admin");
                return new Integer(-3);
            }
            if(EventHandler.searchEventID(eventID, dbc) == null){
                Printing.println("Error in finding event");
                return new Integer(-3);
            }
            if(dbc.isEndorsed(orgID, eventID)){
                Printing.println("Event is already endorsed");
                return new Integer(-2);
            }
            return dbc.endorseEvent(orgID, eventID);
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return new Integer(-1);
    }

    /**
     * @param oAuthCode the authorization code of the user
     * @param orgID the id of the organization
     * @param eventID the id of the event to be unendorsed
     * @return 0 on success, -3 on failure to verify parameters, -1 on database error, -2 on not endorsed
     */
    public static Integer unendorseEvent(String oAuthCode, int orgID, int eventID, DatabaseConnection dbc){
        try {
            if(oAuthCode == null){
                return new Integer(-3);
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return new Integer(-3);
            }
            if(searchOrgID(orgID, dbc) == null){
                Printing.println("Error in finding organization");
                return new Integer(-3);
            }
            if(!isAdmin(oAuthCode,orgID, dbc)){
                Printing.println("User is not an admin");
                return new Integer(-3);
            }
            if(EventHandler.searchEventID(eventID, dbc) == null){
                Printing.println("Error in finding event");
                return new Integer(-3);
            }
            if(!dbc.isEndorsed(orgID, eventID)){
                Printing.println("Event is not already endorsed");
                return new Integer(-2);
            }
            return dbc.unendorseEvent(orgID, eventID);
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return new Integer(-1);
    }
    /**
     * @param oAuthCode the authorization code of the user
     * @param orgID the id of the organization
     * @return true if the user is an admin of the given org, otherwise false
     */
    public static boolean isAdmin(String oAuthCode, int orgID, DatabaseConnection dbc){
        try {
            UserModel u = dbc.searchForUser(oAuthCode);
            GroupiesModel g = dbc.searchGroupies(u.getUserId(), orgID);
            if(g == null){
                Printing.println("user is not a member of the given org");
                return false;
            }
            if(g.type != DatabaseConnection.ORGANIZER){
                Printing.println("user is not an organizer of the given org");
                return false;
            }
            return true;
        }
        catch (SQLException e){
            Printing.println(e.toString());
            return false;
        }
    }

    /**
     *
     * @param oAuthcode         OAuth code for user
     * @param orgID             Organization id to get members for
     * @param dbc               database to check
     * @return        ArrayList<UserModel>[] where at position 0 is all the members and position 1 is all the organizers
     */
    public static ArrayList<UserModel>[] getMembersAndOrganizers(String oAuthcode, int orgID, DatabaseConnection dbc){
        try {
            ArrayList<UserModel>[] memsandorgs = new ArrayList[2];
            memsandorgs[0] = new ArrayList<>();
            memsandorgs[1] = new ArrayList<>();
            UserModel u = dbc.searchForUser(oAuthcode);
            OrganizationModel org = dbc.searchForOrg(orgID);
            if(u == null || org == null){
                Printing.println("no user:"+u+" or org: "+org);
                return null;
            }
            else{
                GroupiesModel g = dbc.searchGroupies(u.getUserId(),orgID);
                if(g == null || g.getType() < DatabaseConnection.MEMBER){
                    Printing.println("No gorupie for organizer");
                    return null;
                }
                else{
                    ArrayList<Integer>[] user_ids = dbc.getMembersAndOrganizers(orgID);
                    for(Integer i: user_ids[0]){
                        UserModel user = dbc.searchForUser(""+i, DatabaseConnection.LAMBNECYUSERID);
                            memsandorgs[0].add(user);
                    }
                    for(Integer i: user_ids[1]){
                        UserModel user = dbc.searchForUser(""+i, DatabaseConnection.LAMBNECYUSERID);
                        memsandorgs[1].add(user);
                    }
                }
            }
            return memsandorgs;
        } catch (SQLException e) {
            e.printStackTrace();
            Printing.println("sql error");
            return null;
        }

    }

    public static ArrayList<UserModel> getRequestedToJoinMembers(String oAuthcode, int orgID, DatabaseConnection dbc){
        try {
            ArrayList<UserModel> users = new ArrayList<>();
            UserModel u = dbc.searchForUser(oAuthcode);
            OrganizationModel org = dbc.searchForOrg(orgID);
            if(u == null || org == null){
                return null;
            }
            else{
                GroupiesModel g = dbc.searchGroupies(u.getUserId(),orgID);
                if(g == null || g.getType() <= DatabaseConnection.MEMBER){
                    return null;
                }
                else{
                    ArrayList<Integer> user_ids = dbc.getRequestedToJoinUsers(orgID);
                    for(Integer i: user_ids){
                        UserModel user = dbc.searchForUser(""+i, DatabaseConnection.LAMBNECYUSERID);
                        users.add(user);
                    }
                }
                return users;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param oAuthCode authorization code for the user making the changes
     * @param orgID id of the org
     * @param userChangedID id of the user being changed
     * @param type 0 is kick from org, 1 set to member, 2 set to organizer
     * @return 0 on success, -1 db error, -2 on insufficient permission, -3 on failure to verify parameters, -4 if illegal removal (last organizer)
     */
    public static Integer manageUserPermissions(String oAuthCode, int orgID, int userChangedID, int type, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                return new Integer(-3);
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to find user");
                return new Integer(-3);
            }
            if(dbc.searchForOrg(orgID) == null){
                Printing.println("Error in finding organization");
                return new Integer(-3);
            }
            if(!isAdmin(oAuthCode,orgID, dbc)){
                Printing.println("User is not an admin");
                return new Integer(-2);
            }
            if(dbc.searchForUser("" + userChangedID, DatabaseConnection.LAMBNECYUSERID) == null){
                Printing.println("Unable to find User to be changed");
                return new Integer(-3);
            }
            if(type > 2 || type < 0) {
                Printing.println("invalid type");
                return new Integer(-3);
            }
            GroupiesModel g = dbc.searchGroupies(userChangedID, orgID);
            if(g == null){
                Printing.println("User to be changed is not a member of org");
                return new Integer(-3);
            }
            if(type == 0){
                if(g.getType() == DatabaseConnection.ORGANIZER && dbc.getMembersAndOrganizers(orgID)[1].size() == 1){
                    Printing.println("Dude, you cant leave the organization if you are the last organizer. Please pass the torch first.");
                    return -4;
                }
                return dbc.deleteGroupies(userChangedID, orgID, g.getType());
            }
            else if(type == 1){
                return dbc.modifyGroupies(userChangedID, orgID, DatabaseConnection.MEMBER);
            }
            else if(type == 2){
                return dbc.modifyGroupies(userChangedID, orgID, DatabaseConnection.ORGANIZER);
            }
            return new Integer(-1);
        }
        catch (SQLException e){
            e.printStackTrace();
            return new Integer(-1);
        }
    }


    /**
     *
     * @param oAuthCode     String oAuthCode for Organizer who is responding to the join request
     * @param orgID         Int orgID for org that the request is for
     * @param userID        int USER id for the user whose request is being responded to
     * @param approved      boolean for whether they are approved or not
     * @param dbc           Database connection to use
     * @return              return -1 if failure, 0 if success join and 1 if success reject
     */
    public static Integer respondToRequest(String oAuthCode, int orgID, int userID, boolean approved, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                return -1;
            }
            UserModel organizer = dbc.searchForUser(oAuthCode);
            if(organizer == null){
                Printing.println("organizer does not exist");
                return -1;
            }
            OrganizationModel org = dbc.searchForOrg(orgID);
            if(org == null){
                Printing.println("Organization does not exist. Try again next time" + orgID);
                return -1;
            }
            GroupiesModel g = dbc.searchGroupies(organizer.getUserId(), orgID);
            if(g == null || g.type != DatabaseConnection.ORGANIZER){
                Printing.println("user is not an organizer of the given org");
                return -1;
            }

            UserModel requester = dbc.searchForUser(Integer.toString(userID),DatabaseConnection.LAMBNECYUSERID);
            if(requester == null){
                Printing.println("Requester does not actually exist.... sad");
                return -1;
            }

            GroupiesModel request = dbc.searchGroupies(userID,orgID);
            if(request == null){
                Printing.println("Request does not actually exist... You failed your Highness. I am a Jedi, like my father before me.");
                return -1;
            }

            // at this point everything exists

            if(approved){
                dbc.approveMemberGroupie(userID,orgID);
                return 0;
            }
            else{
                dbc.deleteGroupies(userID,orgID,DatabaseConnection.MEMBER);
                return 1;
            }


        }
        catch (SQLException e){
            e.printStackTrace();
            return new Integer(-1);
        }
    }


    /**
     *  This method will give a request for a user to join an organization by getting the user email and orgid
     * @param oAuthCode     String oAuthCode for Organizer who is responding to the join request
     * @param orgID         Int orgID for org that the request is for
     * @param emailString   String email of user to send invite to
     * @param dbc           Database connection to use
     * @return              return -1 if failure, 0 if success join and 1 if success reject
     */
    public static Integer sendOrganizationInvite(String oAuthCode, int orgID, String emailString, DatabaseConnection dbc){
        try{
            //first check if person with oAuthCode is in designated org
            //get user model of user sending invite
            UserModel user = dbc.searchForUser(oAuthCode);
            if(user == null){
                return 3;
            }
            //get organizations this user is a member/organizer of
            ArrayList<Integer> organizersOf = dbc.getUserList(user.getUserId(),DatabaseConnection.ORGANIZER,true);

            //check if any of those organizations match given orgid given
            if(organizersOf != null && organizersOf.contains(orgID)){
                //find user by email
                int invitedID= dbc.getUserByEmail(emailString);
                if(invitedID == -1){
                    //no match found
                    return 5;
                }else if(invitedID == -2){
                    //multiple matches found
                    return 6;
                }
                UserModel invited = dbc.searchForUser("" + invitedID,DatabaseConnection.LAMBNECYUSERID);

                GroupiesModel gm = dbc.searchGroupies(invited.getUserId(), orgID);
                if(gm == null || gm.confirmed || gm.type == DatabaseConnection.ORGANIZER) {

                    if (gm == null) {
                        //send invite to user in database
                        Printing.println("added user to groupies model");
                        dbc.addGroupies(invited.getUserId(), orgID, DatabaseConnection.MEMBER, 2);
                    }

                    //send email to user
                    OrganizationModel org = dbc.searchForOrg(orgID);
                    String firebase_token = dbc.userGetFirebase(invited.getUserId());
                    FirebaseHelper.sendCloudOrgInvite(firebase_token, org.name, "" + org.getOrgID());
                    int ret = sendInviteEmail(invited, org,dbc);
                    if (ret == 1) {
                        //issue sending email to user
                        return 7;
                    }
                }else{
                    return 8;
                }
                return 0;
            }else{
                //they do not have the permissions to invite to this organization
                return 4;
            }
        }
        catch (SQLException e){
            Printing.printlnException(e);
            return 1;
        }catch(Exception e){
            Printing.printlnException(e);
            return 2;
        }
    }

    /**
     *  This method will send an invite email to the given user from given organization
     * @param org the oranization that is inviting the user
     * @param invited the user that is being invited
     * @return return 1 if failure, 0 if successfully sent email
     */
    public static int sendInviteEmail(UserModel invited, OrganizationModel org,DatabaseConnection dbc){

        //create email content
        String subject;
        StringBuilder sb = new StringBuilder();
        //set subject
        subject = "You Have Been Invited To Join " + org.getName();

        //set message body
        sb.append(subject + "!<br>");
        sb.append("Please go into your requests in order to accept invitation.<br><br>*This is an automated message. Please do " +
                "not respond to this email.*");

        //create GMailHelper object
        GMailHelper gMailHelper = new GMailHelper();

        //send emails to all users
        Printing.println("email is : " + invited.getEmail());
        int ret = gMailHelper.sendEmail(invited.getEmail(), subject, sb.toString(),dbc);
        if (ret == GMailHelper.FAILURE) {
            return 1;
        }
        return 0;

    }

    /**
     * Using an OrganizationFitlerModel, return Organization models that match the request
     *
     * @param ofm  OrganizationFilterModel through which to search
     * @param dbc   Databaseconnection to use
     * @return  Arraylist of Organziation models.
     */
    public static ArrayList<OrganizationModel> getOrganizationWithFilter(OrganizationFilterModel ofm, DatabaseConnection dbc){
        Printing.println("Search ORG with filter");
        if(ofm == null){
            Printing.println("null Filter Model");
            return null;
        }
        ArrayList<OrganizationModel> orgs;
        try{
            orgs = dbc.searchOrganizationsWithFilterModel(ofm);
            if(orgs == null){
                Printing.printlnError("ORGS IS NULL");
            }

        } catch (SQLException e) {
            Printing.println(e.toString());
            Printing.println("Error in get Organization by filter with error: "+e);;
            return null;
        } catch (Exception e){
            Printing.println(e.toString());
            Printing.println("Error in get Organization by Filter");
            return null;
        }

        return orgs;
    }

    /**
     *
     * @param oAuthCode the oAuthCode of the user
     * @param newOrg model to be changed to
     * @param dbc database connection
     * @return updated model, null on error or insufficient permissions
     */
    public static OrganizationModel editOrg(String oAuthCode, OrganizationModel newOrg, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                Printing.println("invalid oAuthCode");
                return null;
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return null;
            }
            if(newOrg == null){
                Printing.println("Invalid new org");
                return null;
            }
            OrganizationModel organizationModel = OrganizationHandler.searchOrgID(newOrg.getOrgID(), dbc);
            if(organizationModel == null){
                Printing.println("Org not found");
                return null;
            }
            if(!OrganizationHandler.isAdmin(oAuthCode, newOrg.getOrgID(), dbc)){
                Printing.println("User is not an organizer of this org");
                return null;
            }
            LatLng latlng = GoogleGeoCodeUtil.getGeoData(newOrg.getLocation());
            if(latlng == null){
                latlng = new LatLng(180,180);
            }

            newOrg.setLattitude(latlng.lat);
            newOrg.setLongitude(latlng.lng);

            // Saves the orgs image to a file
            String path = newOrg.getImagePath();
            if(newOrg.getImageFile() != null) {
                try {
                    Printing.println("imageFile not null");
                    path = ImageWR.writeImageToFile(newOrg.getImageFile());
                    Printing.println("new path: " + path);
                    //path = ImageWR.saveImage(newOrg.getImageToSave());

                } catch (IOException e) {
                    Printing.println("Error adding image.");
                    Printing.printlnException(e);
                }
            }else{
                Printing.println("imageFile null");
            }
            newOrg.setImage(path);
            int ret = dbc.modifyOrganization(newOrg);
            if(ret == 0){
                return OrganizationHandler.searchOrgID(newOrg.getOrgID(), dbc);
            }
            return null;
        }
        catch (SQLException e){
            Printing.printlnException(e);
        }
        return null;
    }

    /**
     *
     * @param oAuthCode
     * @param orgID
     * @param dbc
     * @return 0 on success, -1 on error or bad params, -2 on invalid user permissions
     */
    public static int deleteOrg(String oAuthCode, int orgID, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                Printing.println("invalid oAuthCode");
                return -1;
            }
            if(dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return -1;
            }
            OrganizationModel organizationModel = OrganizationHandler.searchOrgID(orgID, dbc);
            if(organizationModel == null){
                Printing.println("Org not found");
                return -1;
            }
            if(!OrganizationHandler.isAdmin(oAuthCode, organizationModel.getOrgID(), dbc)){
                Printing.println("User is not an organizer of this org");
                return -2;
            }
            return dbc.deleteOrganization(orgID);
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return -1;
    }

    /**
     *
     * @param oAuthCode
     * @param orgID
     * @param dbc
     * @return 0 on success, -1 on error or bad params, -2 on invalid user permissions
     */
    public static ArrayList<EventModel> pastEventsForOrg(String oAuthCode, int orgID, DatabaseConnection dbc){
        try{
            if(oAuthCode == null){
                Printing.println("invalid oAuthCode");
                return null;
            }
            UserModel u = dbc.searchForUser(oAuthCode);
            if(u  == null){
                Printing.println("Unable to verify user");
                return null;
            }
            OrganizationModel organizationModel = OrganizationHandler.searchOrgID(orgID, dbc);
            if(organizationModel == null){
                Printing.println("Org not found");
                return null;
            }


            //have permissions to look at past events
            ArrayList<Integer> ids = dbc.getPastEventsForOrg(organizationModel.getOrgID());

            ArrayList<EventModel> pastEvents = new ArrayList<>();
            for(Integer i: ids){
                EventModel model = dbc.searchHistoricalEvents(i);
                if(model != null){
                    pastEvents.add(model);
                }
            }
            return pastEvents;

        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return null;
    }



}
