import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.ArrayList;
import java.util.List;

public interface LambencyAPI {

    @GET("User/login/google")
    Call<UserAuthenticator> getGoogleLogin(@Query("id") String id);

    @GET("User/login/facebook")
    Call<UserAuthenticator> getFacebookLogin(@Query("id") String id, @Query("first") String first, @Query("last") String last, @Query("email") String email);

    @GET("User/search")
    Call<UserModel> getUserSearch(@Query("oAuthCode") String oAuthCode, @Query("id") String id);

    @GET("User/followOrg")
    Call<Integer> getFollowOrg(@Query("oAuthCode") String oAuthCode, @Query("orgID") String orgID);

    @GET("User/unfollowOrg")
    Call<Integer> getUnfollowOrg(@Query("oAuthCode") String oAuthCode, @Query("org_id") String orgID);

    @GET("User/getOrgs")
    Call<ArrayList<OrganizationModel>> getMyOrganizedOrgs(@Query("oAuthCode") String oAuthCode);
    
    @GET("User/leaderboardRange")
    Call<List<UserModel>> getLeaderboardRange(@Query("start") String start, @Query("end") String end);

    @GET("User/leaderboardAroundUser")
    Call<List<UserModel>> getLeaderboardAroundUser(@Query("oAuthCode") String oAuthCode);

    @GET("User/joinRequests")
    Call<List<OrganizationModel>> getUserJoinRequests(@Query("oAuthCode") String oAuthCode);

    @GET("User/respondToJoinRequest")
    Call<Integer> getUserRespondToJoinRequest(@Query("oAuthCode") String oAuthCode, @Query("orgID") String orgID, @Query("accept") String accept);

    @GET("User/pastEvents")
    Call<ArrayList<ArrayList<Object>>> getPastEvents(@Query("oAuthCode") String oAuthCode);

    @POST("Organization/create")
    Call<Integer> postCreateOrganization(@Body OrganizationModel org);
    @GET("Organization/search")
    Call<ArrayList<OrganizationModel>> getOrganizationSearch(@Query("name") String name);

    @GET("Organization/searchByID")
    Call<OrganizationModel> getOrgSearchByID(@Query("id") String org_id);

    @GET("Organization/events")
    Call<ArrayList<EventModel>> getEventsByOrg(@Query("oAuthCode") String oAuthCode, @Query("id") String org_id);

    @GET("Organization/joinRequests")
    Call<ArrayList<UserModel>> getRequestsToJoin(@Query("oAuthCode") String oAuthCode, @Query("orgID") int org_id);

    @GET("Organization/endorse")
    Call<Integer> getEndorse(@Query("oAuthCode") String oAuthCode, @Query("orgID") String org_id, @Query("eventID") String event_id);

    @GET("/Organization/changeUserPermissions")
    Call<Integer> getChangeUserPermissions(@Query("oAuthCode") String oAuthCode, @Query("orgID") String org_id, @Query("userChanged") String changedID, @Query("type") String type);

    @GET("Organization/unendorse")
    Call<Integer> getUnendorse(@Query("oAuthCode") String oAuthCode, @Query("orgID") String org_id, @Query("eventID") String event_id);

    @GET("/Organization/members")
    Call<ArrayList<UserModel>[]> getMembersAndOrganizers(@Query("oAuthCode") String oAuthCode, @Query("orgID") int orgID);

    @GET("Organization/respondToJoinRequest")
    Call<Integer> respondToJoinRequest(@Query("oAuthCode") String oAuthCode, @Query("orgID") int orgID, @Query("userID") int userID, @Query("approved") boolean
                                        approved);
    @POST("Organization/edit")
    Call<OrganizationModel> getEditOrganization(@Query("oAuthCode") String oAuthCode, @Body OrganizationModel organizationModel);

    @GET("Organization/delete")
    Call<Integer> getDeleteOrganization(@Query("oAuthCode") String oAuthCode, @Query("orgID") String orgID);

    @GET("Event/search")
    Call<List<EventModel>> getEventsWithParams(@Query("lat") double lat, @Query("long") double longitude,
                                               @Query("name") String name, @Query("org_idStr") String org_idStr);
    @GET("Event/searchByID")
    Call<EventModel> getEventSearchByID(@Query("id") String event_id);

    @GET("Event/users")
    Call<ArrayList<UserModel>> getListOfUsers(@Query("oauthcode") String oAuthCode, @Query("event_id") int eventId);

    @POST("Event/update")
    Call<Integer> getUpdateEvent(@Body EventModel event, @Query("message") String message);

    @GET("Event/numAttending")
    Call<Integer> getEventNumAttending(@Query("oAuthCode") String oAuthCode, @Query("id") String event_id);

    @POST("User/requestJoinOrg")
    Call<Integer> postJoinOrganization(@Query("oAuthCode") String oAuthCode, @Query("orgId") int orgID);

    @POST("User/leaveOrg")
    Call<Integer> postLeaveOrganization(@Query("oAuthCode") String oAuthCode, @Query("orgID") int orgID);

    @GET("User/changeInfo")
    Call<UserModel> getChangeAccountInfo(@Query("user") UserModel u);

    @GET("User/registerForEvent")
    Call<Integer> getRegisterEvent(@Query("oAuthCode") String oAuthCode, @Query("eventID") int eventID);

    @GET("User/eventsFeed")
    Call<List<EventModel>> getEventsFeed(@Query("oAuthCode") String oAuthCode, @Query("latitude") String latitude, @Query("longitude") String longitude);

    @POST("Event/create")
    Call<Integer> createEvent(@Body EventModel eventModel);

    @GET("Event/endorsedOrgs")
    Call<List<OrganizationModel>> getEndorsedOrgs(@Query("oAuthCode") String oAuthCode, @Query("eventId") String eventID);

    @GET("Event/deleteEvent")
    Call<Integer> getDeleteEvent(@Query("oAuthCode") String oAuthCode, @Query("eventID") String eventID, @Query("message") String message);

    @POST("/Event/searchWithFilter")
    Call<ArrayList<EventModel>> getEventsWithFilter(@Body EventFilterModel efm);

    @POST("Organization/InviteUser")
    Call<Integer> inviteUser(@Query("oAuthCode") String oAuthCode, @Query("orgID") String orgID, @Query("emailString") String userEmail);

    @POST("/User/ClockInOut")
    Call<Integer> sendClockInCode(@Query("oAuthCode") String oAuthCode, @Body EventAttendanceModel eventAttendanceModel);

    @POST("Organization/searchWithFilter")
    Call<ArrayList<OrganizationModel>> getOrganizationsWithFilter(@Body OrganizationFilterModel organizationFilterModel);

    @GET("User/setNotificationPreference")
    Call<Integer> updateNotificationPreference(@Query("oAuthCode") String oAuthCode, @Query("preference") int preference);


}
