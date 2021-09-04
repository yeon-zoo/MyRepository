package com.example.trash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
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
    TextView textView;
    MapPoint mapPoint;
    ViewGroup mapViewContainer;
    Thread thread;

    int backValue = 0;

    private static String GEOCODE_URL="http://dapi.kakao.com/v2/local/search/address.json?query=";
    private static String GEOCODE_USER_INFO="KakaoAK 9b8a3e3977e9b91b4f84fda1d9a72499";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text_view);

        addresses();
    }

    class BackThread extends Thread{  // Thread 를 상속받은 작업스레드 생성
        @Override
        public void run() {
            URL obj;
            try{
                String address = URLEncoder.encode("대구광역시 중구 동성로2가 동성로2길 81", "UTF-8");
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
                System.out.println(response.toString());
            } catch (Exception e) { e.printStackTrace(); }
        }
    } // end class BackThread

    public void addresses() {
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
                JSONObject jo = jsonArray.getJSONObject(i);

                String add1 = jo.getString("FIELD2");
                String add2 = jo.getString("FIELD3");
                String add3 = jo.getString("FIELD4");

                s += "구 : " + add1 + "\n도로명 : " + add2 + "\n기타 : " + add3 + "\n\n";
            }
            textView.setText(s);
        } catch(IOException e) { e.printStackTrace(); } catch(JSONException e) { e.printStackTrace(); }

        BackThread thread = new BackThread();  // 작업스레드 생성
        thread.setDaemon(true);  // 메인스레드와 종료 동기화
        thread.start();     // 작업스레드 시작 -> run() 이 작업스레드로 실행됨
    }

    public void marker() {
        mapPoint = MapPoint.mapPointWithGeoCoord(35.898054, 128.544296);
        mapView.setMapCenterPoint(mapPoint, true);

        MapPOIItem mapPOIItem = new MapPOIItem();
        mapPOIItem.setItemName("클릭시 나타나는 이름");
        mapPOIItem.setTag(0);
        mapPOIItem.setMapPoint(mapPoint);
        mapPOIItem.setMarkerType(MapPOIItem.MarkerType.BluePin);
        mapPOIItem.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(mapPOIItem);
    }
}