package util;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by kim on 2018-04-17.
 */

public interface RetrofitPushService {
    @FormUrlEncoded
    @POST("fcm.setting.php")
    Call<RetrofitItem> getPush(
            @FieldMap Map<String, String> option
    );
}
