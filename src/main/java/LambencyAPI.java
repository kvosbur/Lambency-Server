import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import javax.annotation.Generated;
import java.util.ArrayList;

import java.util.List;

public interface LambencyAPI {

    @GET("User/login/google")
    Call<UserAuthenticator> getGoogleLogin(@Query("id") String id);

    @GET("User/login/facebook")
    Call<UserAuthenticator> getFacebookLogin(@Query("id") String id, @Query("first") String first, @Query("last") String last, @Query("email") String email);

    @GET("User/search")
    Call<User> getUserSearch(@Query("oAuthCode") String oAuthCode, @Query("id") String id);

    @GET("User/followOrg")
    Call<Integer> getFollowOrg(@Query("oAuthCode") String oAuthCode, @Query("orgID") String orgID);

    @GET("User/unfollowOrg")
    Call<Integer> getUnfollowOrg(@Query("oAuthCode") String oAuthCode, @Query("org_id") String orgID);

    @POST("Organization/create")
    Call<Integer> postCreateOrganization(@Body Organization org);
    @GET("Organization/search")
    Call<ArrayList<Organization>> getOrganizationSearch(@Query("name") String name);

    @GET("Event/search")
    Call<List<EventModel>> getEventsWithParams(@Query("lat") double lat, @Query("long") double longitude,
                                               @Query("name") String name, @Query("org_idStr") String org_idStr);
    @GET("Event/users")
    Call<ArrayList<User>> getListOfUsers(@Query("oauthcode") String oAuthCode, @Query("event_id") int eventId);

    @POST("Event/update")
    Call<Integer> postUpdateEvent(@Body EventModel event);

    @POST("User/requestJoinOrg")
    Call<Integer> postJoinOrganization(@Query("oAuthCode") String oAuthCode, @Query("orgId") int orgID);

    @GET("User/changeInfo/")
    Call<User> getChangeAccountInfo(@Query("user") User u);

    @GET("User/registerForEvent")
    Call<Integer> getRegisterEvent(@Query("oAuthCode") String oAuthCode, @Query("eventID") int eventID);

    @POST("Event/create")
    Call<Integer> createEvent(@Body EventModel eventModel);
}
