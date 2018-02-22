
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrganizationHandler {


    /**
     * This method will take an organziation and save it in the databse
     *
     * @param org the Organization to be saved in the databse
     * @return an integer representing the org_id on success and an error code on failure.
     *           -1 Database Error | -2 Non Determinant Error
     */
    public static Organization createOrganization(Organization org){

        //check if organization with same name already exists
        try {
            Organization organization = LambencyServer.dbc.searchForOrg(org.name);
            if (organization != null) {
                organization.name = null;
                return organization;
            }
        }catch(Exception e){
            e.printStackTrace();
        }


        // Saves the orgs image to a file
        String path = null;
        int status;
        try {
            path = ImageWR.writeImageToFile(org.getImage());

        } catch (IOException e) {
            System.out.println("Error adding image.");
        }
        try {
            System.out.println(org.toString());
            status = LambencyServer.dbc.createOrganization(org.getName(), org.getDescription(), org.getEmail(), org.getContact()
                    .getUserId(), org.getLocation(), path, org.getOrganizers().get(0).getUserId());
            Organization organization = LambencyServer.dbc.searchForOrg(status);
            //image is currently storing path so change it to store
            organization.setImage(ImageWR.getEncodedImageFromFile(organization.getImage()));
            return organization;
        }
        catch (Exception e){
            status = -2;
            e.printStackTrace();
        }
        return null;

    }


    /**
     * Searches for organizations beginning with the substring name
     * @param name name of the organization to be searched for
     * @return an ArrayList of organizations
     */
    public static ArrayList<Organization> searchOrgName(String name){
        ArrayList<Organization> array;
        try {
            array = LambencyServer.dbc.searchForOrgArray(name);

            //for each organization in the array
            for(Organization org: array){
                org.setImage(ImageWR.getEncodedImageFromFile(org.getImage()));
            }
        }
        catch (SQLException e){
            array = new ArrayList<Organization>();
        }catch(Exception e){
            array = new ArrayList<Organization>();
        }
        return array;
    }

}
