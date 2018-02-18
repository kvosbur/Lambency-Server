public class Groupies {
    //TODO
    public int orgID;
    public int userID;
    public int type;
    public boolean confirmed;

    Groupies(int orgID, int userId, int type, boolean confirmed){
        this.orgID = orgID;
        this.userID = userId;
        this.type = type;
        this.confirmed = confirmed;
    }

    public int getOrgID() {
        return orgID;
    }

    public void setOrgID(int orgID) {
        this.orgID = orgID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

}
