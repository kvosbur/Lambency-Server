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

    @POST("Organization/create")
    Call<Integer> postCreateOrganization(@Body OrganizationModel org);
    @GET("Organization/search")
    Call<ArrayList<OrganizationModel>> getOrganizationSearch(@Query("name") String name);

    @GET("Organization/searchByID")
    Call<OrganizationModel> getOrgSearchByID(@Query("id") String org_id);

    @GET("Organization/events")
    Call<List<EventModel>> getEventsByOrg(@Query("oAuthCode") String oAuthCode, @Query("id") String org_id);

    @GET("Organization/endorse")
    Call<Integer> getEndorse(@Query("oAuthCode") String oAuthCode, @Query("orgID") String org_id, @Query("eventID") String event_id);

    @GET("Organization/unendorse")
    Call<Integer> getUnendorse(@Query("oAuthCode") String oAuthCode, @Query("orgID") String org_id, @Query("eventID") String event_id);

    @GET("Event/search")
    Call<List<EventModel>> getEventsWithParams(@Query("lat") double lat, @Query("long") double longitude,
                                               @Query("name") String name, @Query("org_idStr") String org_idStr);
    @GET("Event/searchByID")
    Call<EventModel> getEventSearchByID(@Query("id") String event_id);

    @GET("Event/users")
    Call<ArrayList<UserModel>> getListOfUsers(@Query("oauthcode") String oAuthCode, @Query("event_id") int eventId);

    @POST("Event/update")
    Call<Integer> postUpdateEvent(@Body EventModel event);

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

    @POST("Event/create")
    Call<Integer> createEvent(@Body EventModel eventModel);
}
