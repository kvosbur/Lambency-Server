import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.List;

import java.util.ArrayList;

public class LambencyAPIHelper {


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
                UserAuthenticator ua = response.body();
                String authCode = ua.getoAuthCode();//System.out.println(ua.getoAuthCode());
                //System.out.println(ua.getStatus());
                if(ua.getStatus() == UserAuthenticator.Status.SUCCESS){
                    //System.out.println("SUCCESS");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_DETERMINANT_ERROR){
                    //System.out.println("NON_DETERMINANT_ERROR");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_UNIQUE_EMAIL){
                    //System.out.println("NON_UNIQUE_EMAIL");
                }
            }

            @Override
            public void onFailure(Call<UserAuthenticator> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

    public List<EventModel> findEventsWithParam(double lattitude, double longitude, String name, int org_id){
        List<EventModel> events = null;

        try {
            Response<List<EventModel>> response = this.getInstance().getEventsWithParams(lattitude,longitude,name,Double.toString(org_id)).execute();
            if(response.isSuccessful()) {
                events = response.body();
            }
            else{
                System.out.println("failed to gather events");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }


    public void googleLoginRetrofit(String id){

        this.getInstance().getGoogleLogin(id).enqueue(new Callback<UserAuthenticator>() {
            @Override
            public void onResponse(Call<UserAuthenticator> call, Response<UserAuthenticator> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                UserAuthenticator ua = response.body();
                String authCode = ua.getoAuthCode();
                //System.out.println(ua.getoAuthCode());
                //System.out.println(ua.getStatus());
                if(ua.getStatus() == UserAuthenticator.Status.SUCCESS){
                    //System.out.println("SUCCESS");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_DETERMINANT_ERROR){
                    //System.out.println("NON_DETERMINANT_ERROR");
                }
                else if(ua.getStatus() == UserAuthenticator.Status.NON_UNIQUE_EMAIL){
                    //System.out.println("NON_UNIQUE_EMAIL");
                }
            }

            @Override
            public void onFailure(Call<UserAuthenticator> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }

public void getOrganizationSearch(String name){

        this.getInstance().getOrganizationSearch(name).enqueue(new Callback<ArrayList<Organization>>() {
            @Override
            public void onResponse(Call<ArrayList<Organization>> call, Response<ArrayList<Organization>> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                ArrayList<Organization> orgList = response.body();
                if(orgList.size() == 0){
                    //no results found
                }
                else{
                    //results found
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Organization>> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
}

public void createOrganizationRetrofit(Organization org){

        this.getInstance().postCreateOrganization(org).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null || response.code() != 200) {
                    System.out.println("ERROR!!!!!");
                }
                //when response is back
                Integer status = response.body();
                System.out.println(status);
                if(status == 0){
                    //System.out.println("SUCCESS");
                }
                else if(status == 1){
                    //System.out.println("BAD USER ID");
                }
                else if(status == 2){
                    //System.out.println("NON DETERMINANT ERROR");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                //when failure
                System.out.println("FAILED CALL");
            }
        });
    }
    public static void main(String[] args) {
        LambencyAPIHelper lh = new LambencyAPIHelper();
        //Organization org = new Organization(null, "Org1", "Purdue", 0, "This is an org", "email@a.com", null, "Img.com");
        //lh.createOrganizationRetrofit(org);
        //lh.facebookLoginRetrofit("id", "fist", "last", "email.com");
        lh.facebookLoginRetrofit("id", "fist", "last", "email.com");
        System.out.println(lh.findEventsWithParam(0.0,0.0,null,0));
    }
}
