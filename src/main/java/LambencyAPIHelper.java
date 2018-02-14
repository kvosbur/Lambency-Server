import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LambencyAPIHelper implements Callback<UserAuthenticator> {


    public LambencyAPI getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:4567")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LambencyAPI lambencyAPI = retrofit.create(LambencyAPI.class);


        return lambencyAPI;
        /*
        Call<UserAuthenticator> call = lambencyAPI.getGoogleLogin("my test id");
        call.enqueue(this);
        Call<UserAuthenticator> call2 = lambencyAPI.getFacebookLogin("id", "first", "last", "email.com");
        call2.enqueue(this);
        */
    }

    public void facebookLoginRetrofit(String id, String firstName, String lastName, String email){

        this.getInstance().getFacebookLogin(id, firstName, lastName, email).enqueue(new Callback<UserAuthenticator>() {
            @Override
            public void onResponse(Call<UserAuthenticator> call, Response<UserAuthenticator> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                System.out.println("SUCCESS");
                UserAuthenticator ua = response.body();
                System.out.println(ua.getoAuthCode());
                System.out.println(ua.getStatus());
            }

            @Override
            public void onFailure(Call<UserAuthenticator> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED");
            }
        });
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
        lh.facebookLoginRetrofit("id", "fist", "last", "email.com");
    }
}
