import java.awt.*;
import java.awt.image.RenderedImage;
import java.util.ArrayList;

public class Organization {
    public User owner;
    public String name;
    public ArrayList<User> members;
    public String location;
    public int orgID;
    public ArrayList<Object> events;
    public String description;
    public String email;
    public User contact;
    public RenderedImage image;
    public ArrayList<User> organizers;

    public Organization(User owner, String name, String location, int orgID, String description, String email, User contact, RenderedImage image) {
        this.owner = owner;
        this.name = name;
        this.location = location;
        this.orgID = orgID;
        this.description = description;
        this.email = email;
        this.contact = contact;
        this.image = image;
        members = new ArrayList<User>();
        members.add(owner);
        events = new ArrayList<>();
        this.organizers = new ArrayList<User>();
        organizers.add(owner);

    }

    public int numFollowing;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<User> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<User> members) {
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

    public User getContact() {
        return contact;
    }

    public void setContact(User contact) {
        this.contact = contact;
    }

    public RenderedImage getImage() {
        return image;
    }

    public void setImage(RenderedImage image) {
        this.image = image;
    }

    public ArrayList<User> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(ArrayList<User> organizers) {
        this.organizers = organizers;
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

    public int checkPermissions(User u){
        return 0;
    }
}
