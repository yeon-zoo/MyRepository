package com.example.trash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import net.daum.android.map.MapViewEventListener;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    MapView mapView;
    MapPoint mapPoint;
    ViewGroup mapViewContainer;

    String result1 = null;   //카카오에서 응답으로 받아온 결과
    String result2 = null;  //StringBuffer에 있는 값을 JSON으로 변환한 결과
    String coordinate = null;

    private static String GEOCODE_URL="http://dapi.kakao.com/v2/local/search/address.json?query=";
    private static String GEOCODE_USER_INFO="KakaoAK 9b8a3e3977e9b91b4f84fda1d9a72499";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        AssetManager assetManager = getAssets();

        try {
            InputStream is = assetManager.open("trashcan.json");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();

            while(line != null) {
                buffer.append(line + "\n");
                line = reader.readLine();
            }

            String jsonData = buffer.toString();

            JSONArray jsonArray = new JSONArray(jsonData);
            String s = "";

            for(int i=0; i<jsonArray.length(); i++) {
                s = "";
                JSONObject jo = jsonArray.getJSONObject(i);

                String add1 = jo.getString("FIELD2");
                String add2 = jo.getString("FIELD3");
                String add3 = jo.getString("FIELD4");

                s += add1 + " " + add2 + " " + add3;
                addToCoordinate(s);
            }

        } catch(IOException e) { e.printStackTrace(); } catch(JSONException e) { e.printStackTrace(); }
    }

    public void addToCoordinate(String s) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                URL obj;
                try{
                    String address = URLEncoder.encode(s, "UTF-8");
                    obj = new URL(GEOCODE_URL+address);
                    HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Authorization",GEOCODE_USER_INFO);
                    con.setRequestProperty("content-type", "application/json");
                    con.setDoOutput(true);
                    con.setUseCaches(false);
                    con.setDefaultUseCaches(false);
                    Charset charset = Charset.forName("UTF-8");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
                    result1 = response.toString();
                    JSONObject json1 = new JSONObject(result1);
                    result2 = json1.getString("documents");

                    JSONArray jsonArray = new JSONArray(result2);
                    for(int i = 0; i<jsonArray.length(); i++) {
                        coordinate = "";
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        coordinate = jsonObject.getString("x") + " " + jsonObject.getString("y");
                        marker(coordinate);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        thread.setDaemon(true);  // 메인스레드와 종료 동기화
        thread.start();     // 작업스레드 시작 -> run() 이 작업스레드로 실행됨
    }

    public void marker(String s) {
        String array[] = s.split(" ");
        mapPoint = MapPoint.mapPointWithGeoCoord(Double.valueOf(array[1]), Double.valueOf(array[0]));
        mapView.setMapCenterPoint(mapPoint, true);

        MapPOIItem mapPOIItem = new MapPOIItem();
        mapPOIItem.setItemName(s);
        mapPOIItem.setTag(0);
        mapPOIItem.setMapPoint(mapPoint);
        mapPOIItem.setMarkerType(MapPOIItem.MarkerType.BluePin);
        mapPOIItem.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(mapPOIItem);
    }
}