package com.dreamforone.jipgo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.BackPressCloseHandler;
import util.Common;
import util.LocationPosition;
import util.NetworkCheck;
import util.RetrofitItem;
import util.RetrofitPushService;


public class MainActivity extends AppCompatActivity {

    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    private final int REQUEST_VIEWER=1000;

    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;

    LinearLayout webLayout;
    RelativeLayout networkLayout;
    WebView webView;
    NetworkCheck netCheck;
    Button replayBtn;
    ProgressBar loadingProgress;
    public static boolean execBoolean = true;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    double lat,lng;

    String firstUrl = "";
    ExpandableListView menuListView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic("jipgo");
        FirebaseInstanceId.getInstance().getToken();
        Intent intent = getIntent();
        firstUrl = getString(R.string.url);
        CookieSyncManager.createInstance(this);
        try{
            if(!intent.getExtras().getString("goUrl").equals("")){
                firstUrl =intent.getExtras().getString("goUrl");
            }
        }catch(Exception e){

        }
        try {
            if (Common.TOKEN.equals("") || Common.TOKEN.equals(null)) {
                refreshToken();
            } else {
                postPush();
            }
        }catch (Exception e){
            refreshToken();
        }
        setLayout();


    }

    private void refreshToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("yujinauto");
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();
        postPush();
    }
    private void postPush(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(getString(R.string.domain))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //서버에 보낼 파라미터
        Map map=new HashMap();
        map.put("token",Common.TOKEN);
        map.put("mb_id",Common.getPref(this,"mb_id",""));
        map.put("DeviceID",Common.getMyDeviceId(this));


        RetrofitPushService retrofitService=retrofit.create(RetrofitPushService.class);
        Call<RetrofitItem> call=retrofitService.getPush(map);

        call.enqueue(new Callback<RetrofitItem>() {
            @Override
            public void onResponse(Call<RetrofitItem> call, Response<RetrofitItem> response) {
                //서버에 데이터 받기가 성공할시
                if(response.isSuccessful()){

                }else{

                }
            }
            //데이터 받기가 실패할 시
            @Override
            public void onFailure(Call<RetrofitItem> call, Throwable t) {

            }
        });
    }
    //레이아웃 설정
    public void setLayout() {
        networkLayout = (RelativeLayout) findViewById(R.id.networkLayout);//네트워크 연결이 끊겼을 때 레이아웃 가져오기
        webLayout = (LinearLayout) findViewById(R.id.webLayout);//웹뷰 레이아웃 가져오기
        /*webLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("url", webView.getUrl());
                webView.reload();

            }
        });*/
        loadingProgress = (ProgressBar)findViewById(R.id.loadingProgress);
        webView = (WebView) findViewById(R.id.webView);//웹뷰 가져오기
        webView.loadUrl(firstUrl);

        webViewSetting();
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    public void webViewSetting() {
        /*webView.addJavascriptInterface(new AppShare(), "appshare");
        webView.addJavascriptInterface(new AppShares(), "appshares");
        webView.addJavascriptInterface(new PostEmail(), "postemail");*/
        WebSettings setting = webView.getSettings();//웹뷰 세팅용

        setting.setAllowFileAccess(true);//웹에서 파일 접근 여부
        setting.setAppCacheEnabled(true);//캐쉬 사용여부
        setting.setGeolocationEnabled(true);//위치 정보 사용여부
        setting.setDatabaseEnabled(true);//HTML5에서 db 사용여부
        setting.setDomStorageEnabled(true);//HTML5에서 DOM 사용여부
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);//캐시 사용모드 LOAD_NO_CACHE는 캐시를 사용않는다는 뜻
        setting.setJavaScriptEnabled(true);//자바스크립트 사용여부
        setting.setSupportMultipleWindows(false);//윈도우 창 여러개를 사용할 것인지의 여부 무조건 false로 하는 게 좋음
        setting.setUseWideViewPort(true);//웹에서 view port 사용여부
        webView.setWebChromeClient(chrome);//웹에서 경고창이나 또는 컴펌창을 띄우기 위한 메서드
        webView.setWebViewClient(client);//웹페이지 관련된 메서드 페이지 이동할 때 또는 페이지가 로딩이 끝날 때 주로 쓰임
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent+" JipGo");
        webView.addJavascriptInterface(new WebJavascriptEvent(), "Android");
        webView.getSettings().setTextZoom(100);

        //현재 안드로이드 버전이 허니콤(3.0) 보다 높으면 줌 컨트롤 사용여부 체킹
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setting.setBuiltInZoomControls(true);
            setting.setDisplayZoomControls(false);
        }
        //네트워크 체킹을 할 때 쓰임
        netCheck = new NetworkCheck(this, this);
        netCheck.setNetworkLayout(networkLayout);
        netCheck.setWebLayout(webLayout);
        netCheck.networkCheck();
        //뒤로가기 버튼을 눌렀을 때 클래스로 제어함
        backPressCloseHandler = new BackPressCloseHandler(this);

        replayBtn=(Button)findViewById(R.id.replayBtn);
        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                netCheck.networkCheck();
            }
        });
        LocationPosition.act=MainActivity.this;
        LocationPosition.setPosition(MainActivity.this);
    }

    WebChromeClient chrome;
    {
        chrome = new WebChromeClient() {
            //새창 띄우기 여부
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return false;
            }

            //경고창 띄우기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                }).create().show();
                return true;
            }

            //컴펌 띄우기
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.cancel();
                                    }
                                }).create().show();
                return true;
            }

            //현재 위치 정보 사용여부 묻기
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Should implement this function.
                final String myOrigin = origin;
                final GeolocationPermissions.Callback myCallback = callback;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Request message");
                builder.setMessage("Allow current location?");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, true, false);
                    }

                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, false, false);
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }


            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (filePathCallbackLollipop != null) {
//                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;


                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }

                // Create camera captured image file path and name
                File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);

                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
                return true;

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
            if (filePathCallbackNormal == null) return;
            Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
            filePathCallbackNormal.onReceiveValue(result);
            filePathCallbackNormal = null;

        } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
            Uri[] result = new Uri[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if(resultCode == RESULT_OK){
                    result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                }

                filePathCallbackLollipop.onReceiveValue(result);

            }
        }
    }

    WebViewClient client;
    {
        client = new WebViewClient() {
            //페이지 로딩중일 때 (마시멜로) 6.0 이후에는 쓰지 않음
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                loadingProgress.setVisibility(View.VISIBLE);
                if (url.startsWith("tel")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.


                        }
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                }
                return false;
            }
            //페이지 로딩이 다 끝났을 때
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                //webLayout.setRefreshing(false);
                loadingProgress.setVisibility(View.GONE);
                Log.d("url",url);
                Log.d("ss_mb_id", Common.getPref(getApplicationContext(),"ss_mb_id",""));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
                //로그인할 때
                if(url.startsWith(getString(R.string.domain)+"/bbs/login.php")){
                    view.loadUrl("javascript:getToken('"+ Common.TOKEN+"','"+Common.getMyDeviceId(MainActivity.this)+"')");
                }
                if (url.equals(getString(R.string.url)+"/") || url.equals(getString(R.string.domain)+"/")) {
                    isIndex=true;
                } else {
                    isIndex=false;
                }

            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.reload();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //쿠키 값 삭제
    public void deleteCookie(){
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(webView.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();
        cookieSyncManager.sync();
    }
    //다시 들어왔을 때
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
        execBoolean=true;
        Log.d("newtork","onResume");
        //netCheck.networkCheck();
    }
    //홈버튼 눌러서 바탕화면 나갔을 때
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
        firstUrl=webView.getUrl();

        execBoolean=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        netCheck.stopReciver();
    }

    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();

        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함
        if (!isIndex) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else if (webView.canGoBack() == false) {
                backPressCloseHandler.onBackPressed();
            }
        } else {
            backPressCloseHandler.onBackPressed();
        }

    }
    //드로우 메뉴 실행하기
    class WebJavascriptEvent{

        @JavascriptInterface
        public void login(String mb_id,String mb_name){
            Common.savePref(MainActivity.this,"mb_id",mb_id);
            Common.savePref(MainActivity.this,"mb_name",mb_name);
        }
        @JavascriptInterface
        public void logout(){
            Common.savePref(MainActivity.this,"mb_id","");
            Common.savePref(MainActivity.this,"mb_name","");
        }
        @JavascriptInterface
        public void pushSetting(String value){
            boolean isBool=Boolean.parseBoolean(value);
            Log.d("value",value+"");
            Common.savePref(MainActivity.this,"setting","ok");
            Common.savePref(MainActivity.this,"push",isBool);
        }
        @JavascriptInterface
        public void getPushSetting(){
            Log.d("push-setting",Common.getPref(MainActivity.this,"push",false)+"");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:getPushSetting("+Common.getPref(MainActivity.this,"push",false)+");");
                }
            });

        }
        @JavascriptInterface
        public void getLocation(){
            lng = LocationPosition.lng;
            lat = LocationPosition.lat;
            String address;
            address=LocationPosition.getAddress(lat,lng);
            Log.d("address",address);
            if(address.equals("")){
                LocationPosition.act=MainActivity.this;
                LocationPosition.setPosition(MainActivity.this);
                Toast.makeText(getApplicationContext(),"GPS 신호가 잡히지 않고 있습니다.\n GPS를 활성화하시거나 잠시 후에 이용하시길 바랍니다.",Toast.LENGTH_SHORT).show();
                return;
            }
            final String addr=address;


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:getAppAddress('"+addr+"');");
                }
            });

        }
        @JavascriptInterface
        public void getLocation2(){
            lng = LocationPosition.lng;
            lat = LocationPosition.lat;
            String address;
            address=LocationPosition.getAddress(lat,lng);
            Log.d("address",address);
            if(address.equals("")){
                LocationPosition.act=MainActivity.this;
                LocationPosition.setPosition(MainActivity.this);
                Toast.makeText(getApplicationContext(),"GPS 신호가 잡히지 않고 있습니다.\n GPS를 활성화하시거나 잠시 후에 이용하시길 바랍니다.",Toast.LENGTH_SHORT).show();
                return;
            }
            final String addr=address;


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:getAppAddress2('"+addr+"');");
                }
            });

        }


    }

}
