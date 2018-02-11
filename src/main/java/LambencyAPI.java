import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LambencyAPI {

    @GET("User/login/google/")
    Call<UserAuthenticator> getGoogleLogin(@Query("id") String id);
    @GET("User/login/facebook")
    Call<UserAuthenticator> getFacebookLogin(@Query("id") String id, @Query("first") String first, @Query("last") String last, @Query("email") String email);

}
