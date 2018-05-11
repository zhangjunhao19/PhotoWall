package com.example.photowall;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

        Http http=new Http("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/0/0");
        Log.d("getPhoto", "finish: 启动了getPhoto");
        http.sendRequestWithHttpURLConnection(new Http.Callback() {
            @Override
            public void finish(String respone) {
                if(isNetworkConnected(context)){
                    Log.d("finish", "网络连接成功 ");
                    parseJSON(respone);
                }
                else {
                    Photos=getArrayList();
                    Log.d("finish", "网络连接不成功 Photo");
                    if(Photos.size()!=0) Log.d("nullHttp", "此时的Photos不为空 ");
                }
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
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                initview();
            }
        });
    }
    public List getArrayList()
    {
        Log.d("getArrayList", "得到数组 ");
        ObjectInputStream objectInputStream;
        FileInputStream fileInputStream;
         List<String>list =new ArrayList<>();
        try {
            fileInputStream=new FileInputStream(totalFile.toString());
            objectInputStream=new ObjectInputStream(fileInputStream);
            list= (List<String>) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }
    public void saveArrayList(List<String> arrayList)
    {
        Log.d("saveArrayList", "保存数组 "+arrayList.size());
        FileOutputStream fileOutputStream=null;
        ObjectOutputStream objectOutputStream=null;
        try {
            fileOutputStream=new FileOutputStream(totalFile.toString());
            objectOutputStream=new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(arrayList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static File getFile(Context context)
    {
        File file=new File(new File(Environment.getExternalStorageDirectory(),context.getPackageName()),"ChunchuList");
        if(!file.exists()) try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(file.exists()) Log.d("getFile", "文件创建成功 ");
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
