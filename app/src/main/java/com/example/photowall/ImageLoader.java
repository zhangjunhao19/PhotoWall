package com.example.photowall;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.IncompleteAnnotationException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by 67698 on 2018/4/28.
 */

public class ImageLoader {
    private static final String TAG="ImageLoader";
    public static final int MESSAGE_RESULT=1;
    private static final int CPU_COUNT=Runtime.getRuntime().availableProcessors();//获取cup数量
    private static final int CPRE_POOL_SIZE=CPU_COUNT+1;//核心线程要比cpu数多一个
    private static final int MAXIMUM_POOL_SIZE=CPU_COUNT*2+1;//线程池最大线程数量等于核心线程的两倍多一个
    private static final long KEEP_ALIVE=10L;//线程的最长等待时间，long赋值后面需要加L
    private static final int TAG_KEY_URI=R.id.photoview;//获取Imageview的key(不过我还是第一次知道布局id是int型)
    private static final long DISK_CACHE_SIZE=1024*1024*50;//最大缓存为50MB(因为内存的基本单位是b)
    private static final int IO_BUFFER_SIZE =8*1024;//流内最大缓存量
    private static final int DISK_CACHE_INDEX=0;//缓存指针
    private boolean mIsDiskLruCacheCreated=false;//默认不打开磁盘存储
    private static final ThreadFactory sThreadFactory=new ThreadFactory() {//为后面线程池做准备
        private final AtomicInteger mcount=new AtomicInteger(1);//原子操作Integer,线程安全的Integer类
        @Override
        public Thread newThread( Runnable r) {
            return new Thread(r,"ImageLoader#"+mcount.getAndIncrement());
        }
    };
    public static final Executor THREAD_POOL_EXECUTOR=new ThreadPoolExecutor(CPRE_POOL_SIZE,MAXIMUM_POOL_SIZE,KEEP_ALIVE, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(),sThreadFactory);//建立线程池
    private android.os.Handler mMainHandler=new android.os.Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result=(LoaderResult)msg.obj;
            ImageView imageView=result.imageView;
            imageView.setImageBitmap(result.bitmap);
            String url=(String)imageView.getTag(TAG_KEY_URI);
            if(url.equals(result.url)){
                imageView.setImageBitmap(result.bitmap);
            }
            else{
                Log.w(TAG,"无匹配url,更新");
            }
        }
    };
    private Context mContext;
    private ImageCompress mImageCompress=new ImageCompress();
    private LruCache<String,Bitmap>mMemoryCache;//内存缓存
    private File DiskFile=null;//磁盘储存文件
    private ImageLoader(Context context)
    {
        mContext=context.getApplicationContext();//获取上下文
        int MaxMemory=(int)(Runtime.getRuntime().maxMemory()/1024);//最大内存
        int CacheSize=MaxMemory/8;//缓存最大为内存的1/8;
        mMemoryCache=new LruCache<String, Bitmap>(CacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight()/1024;
            }
        };
        DiskFile=BuildDiskFile(context);
    }
    private static File BuildDiskFile(Context context)//获取文件
    {
        File diskfile=null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            diskfile=new File(new File(Environment.getExternalStorageDirectory(),context.getPackageName()),"Cache");
            if (!diskfile.exists()) {
                diskfile.mkdir();
            }
        }
        if(diskfile==null)
        {
            diskfile=context.getCacheDir();
        }
        return diskfile;
    }
    private static String getMD5(String url){//MD5加密
        String rec=null;
        try {
            MessageDigest messageDigest=MessageDigest.getInstance("MD5");
            byte [] data=messageDigest.digest(url.getBytes());
            StringBuilder stringBuilder=new StringBuilder();
            for (byte b:data){
                int ib=b& 0x0FF;
                String s=Integer.toHexString(ib);
                stringBuilder.append(s);
            }
            rec=stringBuilder.toString();
            Log.d("MD5", "MD5********** "+rec);
            return rec;
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }
    public static ImageLoader build(Context context)//设置生成对象方法
    {
        return new ImageLoader(context);
    }

    //同步加载
    public Bitmap loadBitmap(String url,int reqWidth,int reqHeight)
    {
        Bitmap bitmap=loadBitmapFromMemCache(url);
        if(bitmap!=null){
            Log.d(TAG,"从内存中加载");
            return bitmap;
        }
        bitmap=loadBitmapFormDiskCache(url);
        if(bitmap!=null)
        {
            Log.d(TAG, "从本地中加载");
            return bitmap;
        }
        bitmap=loadBitmapFormHttp(url,reqWidth,reqHeight);
        if(bitmap!=null) {
            Log.d(TAG, "从网络中加载 ");
            return bitmap;
        }
        return bitmap;
    }
    //异步加载

    //当不知道这个图片的准确大小时使用这种方法
    public void bindBitmap(final String url,final ImageView imageView)
    {
        bindBitmap(url,imageView,0,0);
    }

    public void  bindBitmap(final String url,final ImageView imageView,final int reqWidth,final int reqHeight)
    {
        imageView.setTag(TAG_KEY_URI,url);
        Bitmap bitmap=loadBitmapFromMemCache(url);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask=new Runnable() {
            @Override
            public void run() {
            Bitmap bitmap=loadBitmap(url,reqWidth,reqHeight);
            if(bitmap!=null)
            {
                LoaderResult result=new LoaderResult(imageView,url,bitmap);
                mMainHandler.obtainMessage(MESSAGE_RESULT,result).sendToTarget();
            }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }

    private Bitmap loadBitmapFromMemCache(String url)
    {   Log.d(TAG,"MemCache");
        Bitmap bitmap=getBitmapFromMemoryCache(getMD5(url));
        if(bitmap!=null)return bitmap;
        else return null;
    }
    private Bitmap loadBitmapFormDiskCache(String url)//看文件中有没有名字相同的
    {
        Log.d(TAG,"DISKCache");
        Bitmap bitmap=null;
        String imageFileName=DiskFile.toString()+File.separator+getMD5(url)+".png";
        File file=new File(imageFileName);
        if(file.exists()) bitmap= BitmapFactory.decodeFile(imageFileName);
        if(bitmap!=null) saveToMemoryCache(url,bitmap);
        return bitmap;
    }
    private Bitmap loadBitmapFormHttp(String urll,int reqWidth,int reqHeight)
    {
        ImageCompress imageCompress=new ImageCompress();
       if(Looper.myLooper()==Looper.getMainLooper()){
         throw new RuntimeException("网络操作不能在UI线程上操作");
       }
        Bitmap bitmap=null;
        HttpURLConnection urlConnection=null;
        InputStream inputStream=null;
        try{
            final URL url=new URL(urll);
            urlConnection=(HttpURLConnection)url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.connect();
            inputStream=urlConnection.getInputStream();
            bitmap=imageCompress.decodeSampledBitmapFromStream(inputStream,reqWidth,reqHeight);
           if(bitmap!=null) saveToDisk(urll,inputStream,reqWidth,reqHeight);
        }catch (final IOException e)
        {
            Log.e(TAG,"下图片的时候出现错误");
        }
        finally {
            if(urlConnection!=null)
            {
                urlConnection.disconnect();
            }
            if(inputStream!=null) try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private void saveToMemoryCache(String key,Bitmap bitmap)
    {
        if(getBitmapFromMemoryCache(key)==null)
        {
            Log.d(TAG, "saveToMemoryCache: key="+key);
            mMemoryCache.put(key,bitmap);
        }
    }
    private Bitmap getBitmapFromMemoryCache(String url)
    {
        return mMemoryCache.get(url);
    }


    public void saveToDisk(String imageurl, InputStream inputStream,int reqWidth,int reqHeight)//缓存到本地
    {
        String imageFileName=DiskFile.toString()+File.separator+getMD5(imageurl)+".jpg";//.separator就是/或者\增加鲁棒性
        File file=new File(imageFileName);
        File[] files=DiskFile.listFiles();
        long x=files.length;
        if(x>DISK_CACHE_SIZE)clear();
        try {
            FileOutputStream fo=new FileOutputStream(file);
            Bitmap bitmap=mImageCompress.decodeSampledBitmapFromStream(inputStream,reqWidth,reqHeight);
           if(bitmap!=null) bitmap.compress(Bitmap.CompressFormat.PNG,100,fo);
            try {
                fo.flush();
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void clear()//清理本地缓存
    {
        File[] files=DiskFile.listFiles();
        if(files!=null)
        {
            for (File f:files)
            {
                f.delete();
            }
        }
    }
    //下载数据类
    private static class LoaderResult{
        public ImageView imageView;
        public String url;
        public Bitmap bitmap;
        public LoaderResult(ImageView imageView,String url,Bitmap bitmap){
            this.imageView=imageView;
            this.url=url;
            this.bitmap=bitmap;
        }
    }


}
