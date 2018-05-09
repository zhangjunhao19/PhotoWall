package com.example.photowall;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.photowall.R;

import java.util.List;

/**
 * Created by 67698 on 2018/5/7.
 */

public class RecyclerviewAdapter extends RecyclerView.Adapter <RecyclerviewAdapter.ViewHolder>{
   private Context mContext;
   private List<String> Photos;
   private ImageLoader imageLoader;
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
    public void onBindViewHolder(RecyclerviewAdapter.ViewHolder holder, int position) {
        Log.d("onBindView", "onBindViewHolder:onbindview启动了 "+Photos.get(position));
        imageLoader.bindBitmap(Photos.get(position),holder.imageView,holder.imageView.getWidth(),holder.itemView.getHeight());
    }

    @Override
    public int getItemCount() {
        return Photos.size();
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
