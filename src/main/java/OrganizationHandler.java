
import java.awt.*;
import java.io.IOException;
import java.lang.invoke.LambdaConversionException;
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
                org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
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
            organization.setImage(ImageWR.getEncodedImageFromFile(organization.getImage()));
            return organization;
        } catch (SQLException e) {
            Printing.println("Error in finding organization");
            return null;
        } catch (Exception e) {
            System.out.println("General Error SearchOrgID");
            return null;
        }

    }

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
                list.add(LambencyServer.dbc.searchEvents(i));
            }
            return list;
        }
        catch (SQLException e){
            Printing.println(e.toString());
        }
        return null;
    }

}
