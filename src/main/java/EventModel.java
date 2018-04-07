import java.io.IOException;
import java.sql.Timestamp;

public class EventModel {

    private String name;
    private int org_id;
    private Timestamp start;
    private Timestamp end;
    private String description;
    private String location;
    private String image_path; // file path for server only
    private String imageFile; // base 64 encoded
    private int event_id;
    private double latitude;
    private double longitude;
    private String clockInCode;
    private String clockOutCode;
    private String orgName;
    private boolean privateEvent;


    public EventModel(String name, int org_id, Timestamp start, Timestamp end, String description, String location, String orgName, boolean privateEvent) {
        this.name = name;
        this.org_id = org_id;
        this.start = start;
        this.end = end;
        this.description = description;
        this.location = location;
        this.orgName = orgName;
        this.privateEvent = privateEvent;
    }

    public EventModel(String name, int org_id, Timestamp start, Timestamp end, String description, String location, double latitude,
                      double longitude, String orgName, boolean privateEvent) {
        this(name, org_id, start, end, description, location, orgName, privateEvent);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public EventModel(String name, int org_id, Timestamp start, Timestamp end, String description, String location,
                      String imageFile, double latitude, double longitude, String orgName, boolean privateEvent) {

        this(name, org_id, start, end, description, location, latitude, longitude, orgName, privateEvent);
    }

    public EventModel(String name, int org_id, Timestamp start, Timestamp end, String description, String location, String orgName,
                      String imageFile, boolean privateEvent) {
        this(name, org_id, start, end, description, location, orgName, privateEvent);
        try {
            updateImage(imageFile);
        } catch (IOException e) {
            Printing.println("Failed to save image to event: "+name);
        }
    }




    public EventModel(String name, int org_id, Timestamp start, Timestamp end, String description, String location,
                      String image_path, int event_id, double latitude, double longitude, String clockInCode,
                      String clockOutCode, String orgName, boolean privateEvent) {
        this(name, org_id, start, end, description, location, latitude, longitude, orgName, privateEvent);
        this.image_path = image_path;
        this.event_id = event_id;
        this.clockInCode = clockInCode;
        this.clockOutCode = clockOutCode;
    }

    /**
     *
     * @param encodedImage    base 64 encoded image string
     * @throws IOException    Throws an exception if there is an issue with FileIO in ImageWR
     */
    private void updateImage(String encodedImage) throws IOException{

        this.imageFile = encodedImage;
        this.image_path = ImageWR.writeImageToFile(encodedImage);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrg_id() {
        return org_id;
    }

    public void setOrg_id(int org_id) {
        this.org_id = org_id;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage_path() {
        return image_path;
    }

    /** BE CAUTIOUS IF YOU USE THIS. MAKE SURE THIS PATH COMES FROM IMAGEWR
     *
     * @param image_path   image path to the image
     */

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getImageFile() {
        return imageFile;
    }

    /**
     * DONT USE THIS METHOD UNLESS YOU HAVE TO. USE setImageFile.
     * This will not update the file path for this image
     *
     * @param imageFile base 64 encoded image string
     */
    public void setImageFile(String imageFile)  {
        this.imageFile = imageFile;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int id){
        this.event_id = id;
    }

    public double getLattitude() {
        return latitude;
    }

    public void setLattitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getClockInCode() {
        return clockInCode;
    }

    public void setClockInCode(String clockInCode) {
        this.clockInCode = clockInCode;
    }

    public String getClockOutCode() {
        return clockOutCode;
    }

    public void setClockOutCode(String clockOutCode) {
        this.clockOutCode = clockOutCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public boolean getPrivateEvent() {
        return privateEvent;
    }

    public void setPrivateEvent(boolean privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if(!( obj instanceof EventModel)){
            Printing.println("BAD class");
            return false;
        }
        EventModel em = (EventModel) obj;
        return em.getEvent_id() == event_id;
    }

    @Override
    public String toString(){
        return  "NAME: "+getName() + "\t"+
                "Addr: "+getLocation() +"\t" + "ID: "+getEvent_id();
    }

}
