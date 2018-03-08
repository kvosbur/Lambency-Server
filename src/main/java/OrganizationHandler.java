
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
    public static OrganizationModel createOrganization(OrganizationModel org){

        //check if organization with same name already exists
        try {
            OrganizationModel organization = LambencyServer.dbc.searchForOrg(org.name);
            if (organization != null) {
                organization.name = null;
                return organization;
            }
        }catch(Exception e){
            Printing.println(e.toString());
        }


        // Saves the orgs image to a file
        String path = null;
        int status;
        try {
            path = ImageWR.writeImageToFile(org.getImage());

        } catch (IOException e) {
            Printing.println("Error adding image.");
        }
        try {
            Printing.println(org.toString());
            status = LambencyServer.dbc.createOrganization(org.getName(), org.getDescription(), org.getEmail(), org.getContact()
                    .getUserId(), org.getLocation(), path, org.getOrganizers().get(0).getUserId());
            OrganizationModel organization = LambencyServer.dbc.searchForOrg(status);
            //image is currently storing path so change it to store
            organization.setImage(ImageWR.getEncodedImageFromFile(organization.getImage()));
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
    public static ArrayList<OrganizationModel> searchOrgName(String name){
        ArrayList<OrganizationModel> array;
        try {
            array = LambencyServer.dbc.searchForOrgArray(name);

            //for each organization in the array
            for(OrganizationModel org: array){
                if(org.getImage() != null) {
                    org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
                }
            }
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
    public static OrganizationModel searchOrgID(int orgID) {

        try {
            OrganizationModel organization = LambencyServer.dbc.searchForOrg(orgID);
            if(organization.getImage() != null) {
                organization.setImage(ImageWR.getEncodedImageFromFile(organization.getImage()));
            }
            return organization;
        } catch (SQLException e) {
            Printing.println("Error in finding organization");
            return null;
        } catch (Exception e) {
            System.out.println("General Error SearchOrgID");
            return null;
        }

    }

    /**
     * @param oAuthCode the authorization code of the user
     * @param orgID the id of the organization
     * @return a list of the events of that organization
     */
    public static ArrayList<EventModel> searchEventsByOrg(String oAuthCode, int orgID){
        try {
            if(oAuthCode == null){
                return null;
            }
            if(LambencyServer.dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return null;
            }
            ArrayList<Integer> ids = LambencyServer.dbc.getOrgEvents(orgID);
            if(ids == null || ids.size() == 0){
                Printing.println("Organization has no events");
                return null;
            }
            ArrayList<EventModel> list = new ArrayList<EventModel>();
            for(int i: ids){
                list.add(EventHandler.searchEventID(i));
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
    public static Integer endorseEvent(String oAuthCode, int orgID, int eventID){
        try {
            if(oAuthCode == null){
                return new Integer(-3);
            }
            if(LambencyServer.dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return new Integer(-3);
            }
            if(!isAdmin(oAuthCode,orgID)){
                Printing.println("User is not an admin");
                return new Integer(-3);
            }
            if(searchOrgID(orgID) == null){
                Printing.println("Error in finding organization");
                return new Integer(-3);
            }
            if(EventHandler.searchEventID(eventID) == null){
                Printing.println("Error in finding event");
                return new Integer(-3);
            }
            if(LambencyServer.dbc.isEndorsed(orgID, eventID)){
                Printing.println("Event is already endorsed");
                return new Integer(-2);
            }
            return LambencyServer.dbc.endorseEvent(orgID, eventID);
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
    public static Integer unendorseEvent(String oAuthCode, int orgID, int eventID){
        try {
            if(oAuthCode == null){
                return new Integer(-3);
            }
            if(LambencyServer.dbc.searchForUser(oAuthCode) == null){
                Printing.println("Unable to verify user");
                return new Integer(-3);
            }
            if(!isAdmin(oAuthCode,orgID)){
                Printing.println("User is not an admin");
                return new Integer(-3);
            }
            if(searchOrgID(orgID) == null){
                Printing.println("Error in finding organization");
                return new Integer(-3);
            }
            if(EventHandler.searchEventID(eventID) == null){
                Printing.println("Error in finding event");
                return new Integer(-3);
            }
            if(!LambencyServer.dbc.isEndorsed(orgID, eventID)){
                Printing.println("Event is not already endorsed");
                return new Integer(-2);
            }
            return LambencyServer.dbc.unendorseEvent(orgID, eventID);
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
    public static boolean isAdmin(String oAuthCode, int orgID){
        try {
            UserModel u = LambencyServer.dbc.searchForUser(oAuthCode);
            GroupiesModel g = LambencyServer.dbc.searchGroupies(u.getUserId(), orgID);
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


    public static ArrayList<UserModel>[] getMembersAndOrganizers(String oAuthcode, int orgID){
        try {
            ArrayList<UserModel>[] memsandorgs = new ArrayList[2];
            memsandorgs[0] = new ArrayList<>();
            memsandorgs[1] = new ArrayList<>();
            UserModel u = LambencyServer.dbc.searchForUser(oAuthcode);
            OrganizationModel org = LambencyServer.dbc.searchForOrg(orgID);
            if(u == null || org == null){
                return null;
            }
            else{
                GroupiesModel g = LambencyServer.dbc.searchGroupies(u.getUserId(),orgID);
                if(g == null || g.getType() < DatabaseConnection.MEMBER){
                    return null;
                }
                else{
                    ArrayList<Integer[]> user_ids = LambencyServer.dbc.getMembersAndOrganizers(orgID);
                    for(Integer[] i: user_ids){
                        UserModel user = LambencyServer.dbc.searchForUser(""+i[0], DatabaseConnection.LAMBNECYUSERID);
                        if(i[1]==DatabaseConnection.MEMBER){
                            memsandorgs[0].add(user);
                        }
                        else if(i[1]==DatabaseConnection.ORGANIZER){
                            memsandorgs[0].add(user);
                        }
                    }
                }
            }
            return memsandorgs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static ArrayList<UserModel> getRequestedToJoinMembers(String oAuthcode, int orgID){
        try {
            ArrayList<UserModel> users = new ArrayList<>();
            UserModel u = LambencyServer.dbc.searchForUser(oAuthcode);
            OrganizationModel org = LambencyServer.dbc.searchForOrg(orgID);
            if(u == null || org == null){
                return null;
            }
            else{
                GroupiesModel g = LambencyServer.dbc.searchGroupies(u.getUserId(),orgID);
                if(g == null || g.getType() <= DatabaseConnection.MEMBER){
                    return null;
                }
                else{
                    ArrayList<Integer> user_ids = LambencyServer.dbc.getRequestedToJoinUsers(orgID);
                    for(Integer i: user_ids){
                        UserModel user = LambencyServer.dbc.searchForUser(""+i, DatabaseConnection.LAMBNECYUSERID);
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

}
