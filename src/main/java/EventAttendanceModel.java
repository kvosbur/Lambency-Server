public class EventAttendanceModel {
    private int userID;
    private int eventID;
    public EventAttendanceModel(int userID, int eventID) {
        this.userID = userID;
        this.eventID = eventID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

}