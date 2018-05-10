package com.example.photowall;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /*private String[] Photo={"https://ws1.sinaimg.cn/large/610dc034ly1fp9qm6nv50j20u00miacg.jpg",
            "https://ws1.sinaimg.cn/large/610dc034ly1foowtrkpvkj20sg0izdkx.jpg",
            "http://7xi8d6.com1.z0.glb.clouddn.com/20171227115959_lmlLZ3_Screenshot.jpeg",
            "http://7xi8d6.com1.z0.glb.clouddn.com/20171206084331_wylXWG_misafighting_6_12_2017_8_43_16_390.jpeg",
            "http://7xi8d6.com1.z0.glb.clouddn.com/20171113084220_LuJgqv_sakura.gun_13_11_2017_8_42_12_311.jpeg",
            "http://7xi8d6.com1.z0.glb.clouddn.com/2017-11-17-22794158_128707347832045_9158114204975104000_n.jpg",
            "https://ws1.sinaimg.cn/large/610dc034ly1fgi3vd6irmj20u011i439.jpg",
            "http://7xi8d6.com1.z0.glb.clouddn.com/20171102092251_AY0l4b_alrisaa_2_11_2017_9_22_44_335.jpeg"
    };*/
    private List<String> Photos=new ArrayList<>();
    private RecyclerviewAdapter recyclerviewAdapter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0&&permissions.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED)Toast.makeText(this,"获得权限可以进行本地缓存", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this,"没有权限就不能本地缓存",Toast.LENGTH_SHORT).show();
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        if(isGrantExternalRW(this))Toast.makeText(this,"权限申请成功",Toast.LENGTH_SHORT).show();
        else Toast.makeText(this,"权限申请不成功，无法进行本地缓存",Toast.LENGTH_SHORT).show();
        getPhoto();

    }
    private void initview()
    {
        RecyclerView recyclerView=findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerviewAdapter=new RecyclerviewAdapter(this,Photos);
        recyclerView.setAdapter(recyclerviewAdapter);
    }
    private void getPhoto()
    {
        Http http=new Http("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/0/0");
        Log.d("getPhoto", "finish: 启动了getPhoto");
        http.sendRequestWithHttpURLConnection(new Http.Callback() {
            @Override
            public void finish(String respone) {
                parseJSON(respone);

              runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        initview();
                    }
                });
            }

        });
    }
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }
   private void parseJSON(String respone) {
       try {
           JSONObject jsonObject=new JSONObject(respone);
           String result=jsonObject.getString("results");
           JSONArray jsonArray=new JSONArray(result);

           Log.d("parseJson", "parseJSON:启动了parseJson ,json数组长" + jsonArray.length());
           for (int i = 0; i < jsonArray.length(); i++) {
               JSONObject jsonObject1 = jsonArray.getJSONObject(i);
               Log.d("难受呀嘤嘤毛", "parseJSON: " + jsonObject1.getString("url"));
               Photos.add(jsonObject1.getString("url"));

           }
       } catch (JSONException e) {
           e.printStackTrace();
       }
   }

}
