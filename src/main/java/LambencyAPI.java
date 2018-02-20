import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import javax.annotation.PostConstruct;
import java.util.ArrayList;

import java.util.List;

public interface LambencyAPI {

    @GET("User/login/google/")
    Call<UserAuthenticator> getGoogleLogin(@Query("id") String id);

    @GET("User/login/facebook")
    Call<UserAuthenticator> getFacebookLogin(@Query("id") String id, @Query("first") String first, @Query("last") String last, @Query("email") String email);

    @POST("Organization/Create")
    Call<Integer> postCreateOrganization(@Body Organization org);
    @GET("Organization/search")
    Call<ArrayList<Organization>> getOrganizationSearch(@Query("name") String name);

    @GET("Event/search")
    Call<List<EventModel>> getEventsWithParams(@Query("lat") double lat, @Query("long") double longitude,
                                               @Query("name") String name, @Query("org_idStr") String org_idStr);

    @GET("Event/users")
    Call<ArrayList<User>> getListOfUsers(@Query("oauthcode") String oAuthCode, @Query("event_id") int eventId);
}
