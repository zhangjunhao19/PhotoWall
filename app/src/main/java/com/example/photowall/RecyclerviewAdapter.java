package com.example.photowall;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photowall.R;

import java.util.List;

/**
 * Created by 67698 on 2018/5/7.
 */

public class RecyclerviewAdapter extends RecyclerView.Adapter <RecyclerviewAdapter.ViewHolder>{
   private Context mContext;
   private List<String> Photos;
   private ImageLoader imageLoader;
   private Dialog dialog;
   private ImageView imageView;
   public RecyclerviewAdapter(Context context,List<String> Photos ){
       this.Photos=Photos;
        imageLoader=ImageLoader.build(context);
   }
    @Override
    public RecyclerviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext==null)mContext=parent.getContext();
        View view= LayoutInflater.from(mContext).inflate(R.layout.cardview,parent,false);;
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerviewAdapter.ViewHolder holder, final int position) {
        Log.d("onBindView", "onBindViewHolder:onbindview启动了 "+Photos.get(position));
        imageLoader.bindBitmap(Photos.get(position),holder.imageView,holder.imageView.getWidth(),holder.itemView.getHeight());
        dialog=new Dialog(mContext);
        dialog.setContentView(R.layout.dialog);
        dialog.setCanceledOnTouchOutside(true);
        Window window=dialog.getWindow();
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        layoutParams.x=0;
        layoutParams.y=40;
        dialog.onWindowAttributesChanged(layoutParams);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView =(ImageView) dialog.findViewById(R.id.large_image);
                imageLoader.bindBitmap(Photos.get(position),imageView,imageView.getWidth(),imageView.getHeight());
                Toast.makeText(mContext,"这是position"+position,Toast.LENGTH_SHORT).show();
                dialog.show();
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
   }

    @Override
    public int getItemCount() {
        return Photos.size();
    }

    public Bitmap getViewImage(View view)//获取image里面的图片,如果强行使用.getDrawingCache方法会出现超出内存的情况
    {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            cardView=(CardView)itemView;
            imageView=(ImageView)itemView.findViewById(R.id.photoview);
        }
    }
}
