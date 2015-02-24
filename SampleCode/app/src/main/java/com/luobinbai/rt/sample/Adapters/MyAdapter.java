package com.luobinbai.rt.sample.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luobinbai.rt.sample.R;
import com.luobinbai.rt.sample.Utils.ImageManager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by luobinbai on 1/31/15.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    static final String KEY_ID = "id";
    static final String KEY_LINK = "link";
    static final String KEY_TITLE = "title";
    static final String KEY_PUB = "published";

    private ArrayList<HashMap<String, String>> mDataset;
    private HashMap<Integer, Bitmap> mBMPCache;//===== the internal storage cache of images

    private ImageManager imageManager;

    private Context context;

    //===== ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mTextView;
        public RelativeLayout mCover;
        public DownloadImageTask dt;//===== For cancelling the task
        public getImageFromCacheTask gt;

        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView)v.findViewById(R.id.my_img);
            mTextView = (TextView)v.findViewById(R.id.my_title);
            mCover = (RelativeLayout)v.findViewById(R.id.my_cover);
        }

        public DownloadImageTask getDt() {
            return dt;
        }

        public void setDt(DownloadImageTask dt) {
            this.dt = dt;
        }

        public getImageFromCacheTask getGt() {
            return gt;
        }

        public void setGt(getImageFromCacheTask gt) {
            this.gt = gt;
        }
    }

    //===== Constructor
    public MyAdapter(ArrayList<HashMap<String, String>> myDataset, Context context) {
        this.mDataset = myDataset;
        this.mBMPCache = new HashMap<Integer, Bitmap>();
        this.context = context;
        this.imageManager = new ImageManager(context);
    }

    //===== Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        //===== create a new view
        RelativeLayout v = (RelativeLayout)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_item_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    //===== Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //===== The cover may be hidden by former item
        holder.mCover.setVisibility(View.VISIBLE);

        //===== If this position already has a image in internal cache
        //===== loaf the image from cache instead of downloading it
        if(mBMPCache.containsKey(position)){
            holder.mImageView.setImageBitmap(mBMPCache.get(position));
            holder.mCover.setVisibility(View.GONE);
        }else{
            DownloadImageTask dt = new DownloadImageTask(holder, position);

            //===== Stop the former gt task and starts a new one
            //===== in case that the user could see one Card loading for multiple imgs
            if(holder.getGt() != null){
                holder.getGt().cancel(true);
            }
            getImageFromCacheTask gt = new getImageFromCacheTask(holder, mDataset.get(position).get(KEY_LINK), dt, position);
            holder.setGt(gt);
            holder.getGt().execute();

//            //===== Stop the former task and starts a new one
//            //===== in case that the user could see one Card loading for multiple imgs
//            if(holder.getDt() != null){
//                holder.getDt().cancel(true);
//            }
//            DownloadImageTask dt = new DownloadImageTask(holder, position);
//            holder.setDt(dt);
//            holder.getDt().execute(mDataset.get(position).get(KEY_LINK));
        }

        holder.mTextView.setText(mDataset.get(position).get(KEY_TITLE));




    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }



    protected void imageFadeIn(ImageView iv){

        int mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        iv.setAlpha(0f);

        iv.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);
    }



    private class getImageFromCacheTask extends AsyncTask<String, Void, Bitmap> {
        private ViewHolder holder;
        private String url;
        private DownloadImageTask dt;
        private int position;

        private getImageFromCacheTask(ViewHolder holder, String url, DownloadImageTask dt, int position) {
            this.holder = holder;
            this.url = url;
            this.dt = dt;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Bitmap doInBackground(String... urls) {

            String filename = String.valueOf(url.hashCode());
            File f = new File(imageManager.getCacheDir(), filename);
            //===== Is the bitmap in our cache?
            Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
            if(bitmap != null)
                return bitmap;
            else return null;
        }


        protected void onPostExecute(Bitmap result) {

            if(result == null){
                //===== A new image needs to be downloaded
                if(holder.getDt() != null){
                    holder.getDt().cancel(true);
                }
                holder.setDt(dt);
                holder.getDt().execute(url);
            }else{
                mBMPCache.put(position, result);
                holder.mImageView.setImageBitmap(result);
                holder.mCover.setVisibility(View.GONE);
            }

        }
    }



    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView iv;
        private RelativeLayout cover;
        private int position;
        private String urldisplay;

        public DownloadImageTask(ViewHolder holder, int position) {
            this.iv = holder.mImageView;
            this.cover = holder.mCover;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            iv.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            urldisplay = urls[0];
            Log.v("URL", urldisplay);
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {

            if(result != null){
                mBMPCache.put(position, result);
                imageManager.saveImageToCache(result, urldisplay);
                iv.setImageBitmap(result);
                cover.setVisibility(View.GONE);
                imageFadeIn(iv);
            }

        }
    }
}
