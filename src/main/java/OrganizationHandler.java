
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

        // Saves the orgs image to a file
        String path = null;
        int status;
        try {
            path = ImageWR.writeImageToFile(org.getImage());

        } catch (IOException e) {
            System.out.println("Error adding image.");
        }
        try {
            status = LambencyServer.dbc.createOrganization(org.getName(), org.getDescription(), org.getEmail(), org.getContact()
                    .getUserId(), org.getLocation(), path, org.getOrganizers().get(0).getUserId());
            return LambencyServer.dbc.searchForOrg(status);
        }
        catch (Exception e){
            status = -2;
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
        }
        catch (SQLException e){
            array = new ArrayList<Organization>();
        }
        return array;
    }

}
