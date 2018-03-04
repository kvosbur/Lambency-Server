import java.util.List;

public class MyLambencyModel {

    UserModel userModel;
    List<OrganizationModel> myOrgs;
    List<OrganizationModel> joinedOrgs;
    List<EventModel> eventsAttending;
    List<EventModel> eventsOrganizing;

    public MyLambencyModel(UserModel userModel, List<OrganizationModel> myOrgs, List<OrganizationModel> joinedOrgs,
                           List<EventModel> eventsAttending, List<EventModel> eventsOrganizing) {
        this.userModel = userModel;
        this.myOrgs = myOrgs;
        this.joinedOrgs = joinedOrgs;
        this.eventsAttending = eventsAttending;
        this.eventsOrganizing = eventsOrganizing;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public List<OrganizationModel> getMyOrgs() {
        return myOrgs;
    }

    public void setMyOrgs(List<OrganizationModel> myOrgs) {
        this.myOrgs = myOrgs;
    }

    public List<OrganizationModel> getJoinedOrgs() {
        return joinedOrgs;
    }

    public void setJoinedOrgs(List<OrganizationModel> joinedOrgs) {
        this.joinedOrgs = joinedOrgs;
    }

    public List<EventModel> getEventsAttending() {
        return eventsAttending;
    }

    public void setEventsAttending(List<EventModel> eventsAttending) {
        this.eventsAttending = eventsAttending;
    }

    public List<EventModel> getEventsOrganizing() {
        return eventsOrganizing;
    }

    public void setEventsOrganizing(List<EventModel> eventsOrganizing) {
        this.eventsOrganizing = eventsOrganizing;
    }
}
