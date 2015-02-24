package com.luobinbai.rt.sample.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * Created by luobinbai on 1/31/15.
 */
public class ImageManager {
    private HashMap imageMap = new HashMap();
    private File cacheDir;
    private Bitmap bmpGotFromCache = null;

    public ImageManager(Context context) {

        // Find the dir to save cached images
        String sdState = android.os.Environment.getExternalStorageState();
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File sdDir = android.os.Environment.getExternalStorageDirectory();
            cacheDir = new File(sdDir,"data/cache");
        }
        else
            cacheDir = context.getCacheDir();

        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }


    public void saveImageToCache(Bitmap bmp, String url){

        new saveImageToCacheTask(bmp, url).execute();
    }

    private class saveImageToCacheTask extends AsyncTask<String, Void, String> {
        private Bitmap bmp;
        private String url;

        private saveImageToCacheTask(Bitmap bmp, String url) {
            this.bmp = bmp;
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... urls) {

            String filename = String.valueOf(url.hashCode());
            File f = new File(cacheDir, filename);
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.PNG, 80, out);

            }catch (Exception e) {

                e.printStackTrace();

            }finally {
                try {
                    if (out != null ) out.close();
                } catch(Exception ex) {}
            }

            return "image saved";
        }


        protected void onPostExecute(String result) {

        }
    }

    public void getImageFromCache(String url){

        new getImageFromCacheTask(url).execute();

    }

    private class getImageFromCacheTask extends AsyncTask<String, Void, Bitmap> {
        private String url;

        private getImageFromCacheTask(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Bitmap doInBackground(String... urls) {

            String filename = String.valueOf(url.hashCode());
            File f = new File(cacheDir, filename);
            //===== Is the bitmap in our cache?
            Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
            if(bitmap != null)
                return bitmap;
            else return null;
        }


        protected void onPostExecute(Bitmap result) {


        }
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    //
//    private class ImageRef {
//        public String url;
//        public ImageView imageView;
//        public ImageRef(String u, ImageView i) {
//            url=u;
//            imageView=i;
//        }
//    }
//
//    private class ImageQueue {
//        private Stack imageRefs = new Stack();
//
//        public void Clean(ImageView view) {
//            for(int i = 0 ;i < imageRefs.size();) {
//                if(imageRefs.get(i).imageView == view)
//                    imageRefs.remove(i);
//                else ++i;
//            }
//        }
//    }
//
//    private void queueImage(String url, ImageView imageView) {
//        // This ImageView might have been used for other images, so we clear
//        // the queue of old tasks before starting.
//        imageQueue.Clean(imageView);
//        ImageRef p=new ImageRef(url, imageView);
//        synchronized(imageQueue.imageRefs) {
//            imageQueue.imageRefs.push(p);
//            imageQueue.imageRefs.notifyAll();
//        }
//
//        // Start thread if it's not started yet
//        if(imageLoaderThread.getState() == Thread.State.NEW)
//            imageLoaderThread.start();
//    }




}
