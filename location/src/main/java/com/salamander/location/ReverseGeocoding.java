package com.salamander.location;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;

import com.salamander.core.Utils;
import com.salamander.network.JSON;
import com.salamander.network.retro.RetroData;
import com.salamander.network.retro.RetroResp;
import com.salamander.network.retro.RetroStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.nlopez.smartlocation.SmartLocation;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ReverseGeocoding {

    public static final String LANGUAGE = "id";

    public static final String RESULT_OK = "OK";
    public static final String ZERO_RESULTS = "ZERO_RESULTS";
    public static final String OVER_DAILY_LIMIT = "OVER_DAILY_LIMIT";
    public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public static final String REQUEST_DENIED = "REQUEST_DENIED";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

    public static void getLocationFromAddress(final Context context, final String address, final String apiKey, final OnCB onCB) {
        if (Geocoder.isPresent())
            SmartLocation.with(context).geocoding().direct(address, (s, list) -> {
                if (list.size() > 0) {
                    Location location = list.get(0).getLocation();
                    RetroStatus retroStatus = new RetroStatus();
                    retroStatus.setSuccess(true);
                    onCB.onCB(retroStatus, location);
                } else getReverseGeocodingFromGoogleAPI(context, address, apiKey, onCB);
            });
        else getReverseGeocodingFromGoogleAPI(context, address, apiKey, onCB);
    }

    private static void getReverseGeocodingFromGoogleAPI(final Context context, final String address, final String apiKey, final OnCB onCB) {
        IC ic = createRetrofit().create(IC.class);
        ic.getLocation(address, LANGUAGE, apiKey).enqueue(new RetroResp.SuccessCallback<ResponseBody>(context) {
            @Override
            public void onCall(RetroData retroData) {
                super.onCall(retroData);
                JSONObject jsonObject = JSON.toJSONObject(retroData.getResult());
                String status = JSON.getString(jsonObject, "status");
                retroData.getRetroStatus().setSuccess(!Utils.isEmpty(status) && status.equals(RESULT_OK));
                Location location = new Location("");
                if (retroData.isSuccess()) {
                    try {
                        JSONArray jsonArray = JSON.getJSONArray(jsonObject, "results");
                        JSONObject jsonObjectResult = jsonArray.getJSONObject(0);
                        JSONObject jsonObjectGeometry = jsonObjectResult.getJSONObject("geometry");
                        JSONObject jsonObjectLocation = jsonObjectResult.getJSONObject("location");
                        location.setLatitude(jsonObjectLocation.getDouble("lat"));
                        location.setLatitude(jsonObjectLocation.getDouble("lng"));
                    } catch (Exception e) {
                        retroData.getRetroStatus().setSuccess(false);
                        retroData.getRetroStatus().setMessage(e.toString());
                    }
                } else {
                    String errorMessage = JSON.getStringOrNull(jsonObject, "error_message");
                    if (Utils.isEmpty(errorMessage))
                        retroData.getRetroStatus().setMessage("Address not found");
                    else retroData.getRetroStatus().setMessage(errorMessage);
                }
                onCB.onCB(retroData.getRetroStatus(), location);
            }
        });
    }

    public interface OnCB {
        void onCB(RetroStatus retroStatus, Location location);
    }

    interface IC {
        @GET("maps/api/geocode/json")
        Call<ResponseBody> getLocation(@Query("address") String address, @Query("language") String language, @Query("key") String key);
    }

    public static Retrofit createRetrofit() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES);
        return new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .client(client.build())
                .build();
    }
}