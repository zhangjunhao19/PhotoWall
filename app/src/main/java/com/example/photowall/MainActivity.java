package com.example.photowall;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private File totalFile;
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
        totalFile=getFile(this);
         getPhoto(this);

    }
    private void initview()
    {
        final Context context=this;
        Log.d("initview", "进行了初始化布局 ");
        final ImageLoader imageLoader=ImageLoader.build(this);
        RecyclerView recyclerView=findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerviewAdapter=new RecyclerviewAdapter(this,Photos);
        recyclerView.setAdapter(recyclerviewAdapter);
        FloatingActionButton floatingActionButton=findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageLoader.clear();
                Toast.makeText(context,"清理缓存成功",Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean isNetworkConnected(Context context) {
            if (context != null) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                         }
                 }
             return false;
         }
    private void getPhoto(final Context context) {
       // Log.d("网络情况", "此时的网络连接情况是 "+isNetworkConnected(context));
        if(isNetworkConnected(context)){
        Http http=new Http("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/0/0");
        Log.d("getPhoto", "finish: 启动了getPhoto");
        http.sendRequestWithHttpURLConnection(new Http.Callback() {
            @Override
            public void finish(String respone) {
                 //   Log.d("finish", "网络连接成功 ");
                    parseJSON(respone);
              runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        initview();
                    }
                });
            }

            @Override
            public void backnull() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        initview();
                    }
                });

            }
            });
        }
        else {
            Toast.makeText(context,"未连接网络",Toast.LENGTH_SHORT).show();
            Photos=getArrayList();
            //Log.d("finish", "网络连接不成功 Photo");
            if(Photos.size()!=0) Log.d("nullHttp", "此时的Photos不为空 ");
        }
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                initview();
            }
        });
    }
    public List getArrayList()
    {
        Log.d("难受呀马飞", "得到数组方法启动 ");
        List<String> list=new ArrayList<>();
        SharedPreferences sharedPreferences=getSharedPreferences("urldata",MODE_PRIVATE);
        for(int i=0;;i++)
        {
           // Log.d("得到数据", "进行循环 ");
            if(sharedPreferences.getString(String.valueOf(i),"0")=="0")break;
            list.add(sharedPreferences.getString(String.valueOf(i),"0"));
        }
        Log.d("得到数组", "此时数组的大小 "+list.size());
        return list;
    }
    public void saveArrayList(List<String> arrayList)
    {
        Log.d("难受呀马飞", "saveArrayList:启动了 ");
        SharedPreferences.Editor editor=getSharedPreferences("urldata",MODE_PRIVATE).edit();
        for(int i=0;i<arrayList.size();i++)
        {
            //Log.d("存入sharedpreference", "此时的arraylist数据为 "+arrayList.get(i));
            editor.putString(String.valueOf(i),arrayList.get(i));
            editor.apply();
        }
        SharedPreferences sharedPreferences=getSharedPreferences("urldata",MODE_PRIVATE);
       // Log.d("sharepreference中的", " "+sharedPreferences.getString(String.valueOf(0),"0"));
    }
    public static File getFile(Context context)
    {
        File file=new File(new File(Environment.getExternalStorageDirectory(),context.getPackageName()),"ChunchuList");
        if(!file.exists()) try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
      //  if(file.exists()) Log.d("getFile", "文件创建成功 ");
        return file;
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
           Log.d("Json解析", "parseJSON: "+jsonObject.toString());
           String result=jsonObject.getString("results");
           JSONArray jsonArray=new JSONArray(result);

           //Log.d("parseJson", "parseJSON:启动了parseJson ,json数组长" + jsonArray.length());
           for (int i = 0; i < jsonArray.length(); i++) {
               JSONObject jsonObject1 = jsonArray.getJSONObject(i);
               //Log.d("难受呀嘤嘤毛", "parseJSON: " + jsonObject1.getString("url"));
               Photos.add(jsonObject1.getString("url"));

           }
          // Log.d("parseJSON", "此时的Photo大小为 "+Photos.size());
           saveArrayList(Photos);
       } catch (JSONException e) {
           e.printStackTrace();
       }
   }

}
