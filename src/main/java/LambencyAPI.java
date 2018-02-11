import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LambencyAPI {

    @GET("User/login/google")
    Call<UserAuthenticator> getAllLocations(@Query("id") String id);

}
