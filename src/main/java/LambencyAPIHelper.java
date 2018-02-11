import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LambencyAPIHelper implements Callback<UserAuthenticator> {


    public void start(){
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:4567")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LambencyAPI lambencyAPI = retrofit.create(LambencyAPI.class);

        Call<UserAuthenticator> call = lambencyAPI.getGoogleLogin("my test id");
        call.enqueue(this);
        Call<UserAuthenticator> call2 = lambencyAPI.getFacebookLogin("id", "first", "last", "email.com");
        call2.enqueue(this);
    }

    @Override
    public void onResponse(Call<UserAuthenticator> call, Response<UserAuthenticator> response) {
        if(response.isSuccessful()) {
            UserAuthenticator ua = response.body();
            System.out.println(ua.getoAuthCode() + "\n" + ua.getStatus());
        } else {
            System.out.println(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<UserAuthenticator> call, Throwable t) {
        t.printStackTrace();
    }

    public static void main(String[] args) {
        LambencyAPIHelper lh = new LambencyAPIHelper();
        lh.start();
    }
}
