import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class OrganizationModel {
    public UserModel owner;
    public String name;
    public ArrayList<UserModel> members;
    public String location;
    public int orgID;
    public ArrayList<Object> events;
    public String description;
    public String email;
    public UserModel contact;
    public String image;
    public ArrayList<UserModel> organizers;
    public double lattitude;
    public double longitude;
    public BufferedImage imageToSave;

    public OrganizationModel(UserModel owner, String name, String location, int orgID, String description, String email, UserModel contact, String image) {
        this.owner = owner;
        this.name = name;
        this.location = location;
        this.orgID = orgID;
        this.description = description;
        this.email = email;
        this.contact = contact;
        this.image = image;
        members = new ArrayList<UserModel>();
        members.add(owner);
        events = new ArrayList<>();
        this.organizers = new ArrayList<UserModel>();
        organizers.add(owner);

    }

    public OrganizationModel(UserModel owner, String name, String location, int orgID, String description,
                             String email, UserModel contact, String image, double lattitude, double longitude) {
        this.owner = owner;
        this.name = name;
        this.location = location;
        this.orgID = orgID;
        this.description = description;
        this.email = email;
        this.contact = contact;
        this.image = image;
        this.lattitude = lattitude;
        this.longitude = longitude;
        members = new ArrayList<UserModel>();
        members.add(owner);
        events = new ArrayList<>();
        this.organizers = new ArrayList<UserModel>();
        organizers.add(owner);
    }

    public int numFollowing;

    public BufferedImage getImageToSave() {
        return imageToSave;
    }

    public void setImageToSave(BufferedImage imageToSave) {
        this.imageToSave = imageToSave;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<UserModel> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<UserModel> members) {
        this.members = members;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getOrgID() {
        return orgID;
    }

    public void setOrgID(int orgID) {
        this.orgID = orgID;
    }

    public ArrayList<Object> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Object> events) {
        this.events = events;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserModel getContact() {
        return contact;
    }

    public void setContact(UserModel contact) {
        this.contact = contact;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<UserModel> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(ArrayList<UserModel> organizers) {
        this.organizers = organizers;
    }

    public double getLattitude()
    {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getNumFollowing(){
        return numFollowing;
    }
    public void incNumFollowing() {
        this.numFollowing += 1;
    }

    public void decNumFollowing(int numFollowing) {
        this.numFollowing -= 1;
    }

    public int checkPermissions(UserModel u){
        return 0;
    }

    public String toString(){

        String result = "";
        result += "Org ID: " + orgID + "\n";
        result += "Name: " + name + "\n";
        result += "Contact:\n" + contact.toString();
        result += "Owner:\n" + owner.toString();
        result += "Location:" + location + "\n";
        result += "Description: " + description + "\n";
        result += "Email: " + email + "\n";
        if(members == null){
            result += "Members: NULL\n";
        } else if(members.size() == 0){
            result += "Members: NONE\n";
        }else{
            result += "Members: Some\n";
        }

        if(organizers == null){
            result += "Organizers: NULL\n";
        } else if(organizers.size() == 0){
            result += "Organizers: NONE\n";
        }else{
            result += "Organizers: Some\n";
        }

        if(events == null){
            result += "Events: NULL\n";
        } else if(events.size() == 0){
            result += "Events: NONE\n";
        }else{
            result += "Events: Some\n";
        }
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if(!( obj instanceof OrganizationModel)){
            Printing.println("BAD class");
            return false;
        }
        OrganizationModel om = (OrganizationModel) obj;
        return om.getOrgID() == orgID;
    }
}
