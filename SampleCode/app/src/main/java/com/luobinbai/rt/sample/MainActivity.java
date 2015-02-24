package com.luobinbai.rt.sample;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.luobinbai.rt.sample.Adapters.MyAdapter;
import com.luobinbai.rt.sample.Parsers.XMLParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<HashMap<String, String>> dataItems;
    private RelativeLayout mMainLoading;
    private LinearLayout mNetWorkRetry;

    // URL
    static final String URL = "http://api.flickr.com/services/feeds/photos_public.gne?tags=boston";
    // XML Keys
    static final String KEY_ENTRY = "entry"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_LINK = "link";
    static final String KEY_TITLE = "title";
    static final String KEY_PUB = "published";
    private XMLParser parser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mMainLoading = (RelativeLayout)findViewById(R.id.main_loading);
        mNetWorkRetry = (LinearLayout)findViewById(R.id.main_network_retry);
        mNetWorkRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //===== Use this setting to improve performance if you know that changes
        //===== in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //===== Initiate the parser
        parser = new XMLParser();
        //===== Create an AsyncTask: getAndInitData to make http request and load data
        new getAndInitData().execute();

        mNetWorkRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                new getAndInitData().execute();
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class getAndInitData extends AsyncTask<String, String, Document> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataItems = new ArrayList<HashMap<String, String>>();
        }

        @Override
        protected Document doInBackground(String... uri) {
            Document doc = null;
            try {
                //===== Getting XML
                String XML = parser.getXmlFromUrl(URL);
                //===== Getting DOM element
                doc = parser.getDomElement(XML);
            }catch (Exception e){
                Log.v("Error:", e.getMessage());
            }

            return doc;

        }

        @Override
        protected void onPostExecute(Document doc) {
            super.onPostExecute(doc);

            if(doc != null){
                NodeList nl = doc.getElementsByTagName(KEY_ENTRY);
                //===== Looping through all item nodes <entry>
                for (int i = 0; i < nl.getLength(); i++) {
                    //===== Creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();
                    Element e = (Element) nl.item(i);
                    //===== Adding each child node to HashMap key => value
                    map.put(KEY_ID, parser.getValue(e, KEY_ID));
                    map.put(KEY_LINK, parser.getAttrValue(e, KEY_LINK));
                    map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
                    map.put(KEY_PUB, parser.getValue(e, KEY_PUB));

                    //===== Adding HashList to ArrayList
                    dataItems.add(map);
                }

                //===== Set up the adapter of the RecyclerView
                mAdapter = new MyAdapter(dataItems, MainActivity.this);
                mRecyclerView.setAdapter(mAdapter);
                mMainLoading.setVisibility(View.GONE);
            }else{
                mNetWorkRetry.setVisibility(View.VISIBLE);
            }

        }


    }
}
