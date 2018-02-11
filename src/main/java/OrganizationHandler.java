import java.awt.image.RenderedImage;
import java.io.IOException;

public class OrganizationHandler {


    /**
     * This method will take an organziation and save it in the databse
     *
     * @param org the Organization to be saved in the databse
     * @return an integer representing the org_id on success and an error code on failure.
     *           -1 Database Error | -2 Non Determinant Error
     */
    public static int createOrganization(Organization org){

        // Saves the orgs image to a file
        RenderedImage toSave = org.image;
        String path = null;
        int status;
        try {
            String encoding = ImageWR.makeEncodingFromImage(toSave);
            path = ImageWR.writeImageToFile(encoding);

        } catch (IOException e) {
            System.out.println("Error adding image.");
        }
        try {
            status = LambencyServer.dbc.createOrganization(org.getName(), org.getDescription(), org.getEmail(), org.getContact()
                    .getUserId(), org.getLocation(), path, org.getOrganizers().get(0).getUserId());
        }
        catch (Exception e){
            status = -2;
        }
        return status;

    }

}
