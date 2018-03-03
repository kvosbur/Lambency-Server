import java.sql.Timestamp;

public class EventAttendanceModel {

    private int eventID;
    private int userID;
    private UserModel userModel;
    private Timestamp startTime;
    private Timestamp endTime;
    //data member for temporary storage of code user is trying to clock in/out with
    private String clockInOutCode;

    public final static int CLOCKINCODE = 0;
    public final static int CLOCKOUTCODE = 1;

    EventAttendanceModel(int eventID, int userID){
        this.eventID = eventID;
        this.userID = userID;
    }

    EventAttendanceModel(int eventID, int userID, Timestamp startTime){
        this(eventID,userID);
        this.startTime = startTime;
    }

    EventAttendanceModel(int eventID, int userID, Timestamp startTime, String clockInOutCode){
        this(eventID,userID, startTime);
        this.clockInOutCode = clockInOutCode;
    }

    EventAttendanceModel(int eventID, int userID, Timestamp startTime, Timestamp endTime){
        this(eventID, userID, startTime);
        this.endTime = endTime;
    }

    EventAttendanceModel(int eventID, int userID, Timestamp startTime, Timestamp endTime, String clockInOutCode){
        this(eventID, userID, startTime, endTime);
        this.clockInOutCode = clockInOutCode;
    }

    //helpful for
    EventAttendanceModel(int eventID, UserModel user, Timestamp startTime, Timestamp endTime){
        this(eventID, user.getUserId(), startTime, endTime);
        this.userModel = userModel;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getClockInOutCode() {
        return clockInOutCode;
    }

    public void setClockInOutCode(String clockInOutCode) {
        this.clockInOutCode = clockInOutCode;
    }
}
