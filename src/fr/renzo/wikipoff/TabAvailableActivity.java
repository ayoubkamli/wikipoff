package fr.renzo.wikipoff;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TabAvailableActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "TabAvailableActivity";
	private static final String available_db_xml_file="available_wikis.xml";
	private ArrayList<Wiki> wikis=new ArrayList<Wiki>();
	private ListView availablewikislistview;
	private SharedPreferences config;

	private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this.getApplicationContext();
        config = PreferenceManager.getDefaultSharedPreferences(this);

        
        setContentView(R.layout.activity_tab_available);
        try {
			loadDB();
		} catch (IOException e) {
			Toast.makeText(this, "Problem opening available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
			this.finish();
		}
        availablewikislistview= (ListView) findViewById(R.id.availablewikislistview);
        ArrayAdapter<Wiki> adapter = new ArrayAdapter<Wiki>(this, android.R.layout.simple_list_item_1, this.wikis); 
        availablewikislistview.setAdapter(adapter);
        availablewikislistview.setOnItemClickListener(this);

        
   }
    private void loadDB() throws IOException {
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(getAssets().open(available_db_xml_file)));
		    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(reader); 
            int eventType = parser.getEventType();
            
            String curtext = null;
            Wiki curwiki=null;
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
            	switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					 //   Log.d(TAG,"Start document");
					break;
				case XmlPullParser.START_TAG:
//					Log.d(TAG,"Start tag "+parser.getName());
	            	 String t=parser.getName();
	            	 if (t.equalsIgnoreCase("wiki")){
	            		 curwiki = new Wiki();
					}
	            	 break;
				case XmlPullParser.TEXT:
					curtext = parser.getText();
					break;
				case XmlPullParser.END_TAG:
//					Log.d(TAG,"End tag"+parser.getName());
	            	String endt=parser.getName();

					if (endt.equalsIgnoreCase("wiki")){
						this.wikis.add(curwiki);
						curwiki=null;
					} else if (endt.equalsIgnoreCase("type")) {
						curwiki.setType(curtext);
					} else if (endt.equalsIgnoreCase("lang")) {
						curwiki.setLang(curtext);
					} else if (endt.equalsIgnoreCase("url")) {
						curwiki.setUrl(curtext);
					} else if (endt.equalsIgnoreCase("gendate")) {
						curwiki.setGendate(curtext);
					} else if (endt.equalsIgnoreCase("version")) {
						curwiki.setVersion(curtext);
					}
					break;
				default:
					break;
				}

             eventType = parser.next();
            }
//            Log.d(TAG,"End document");
		    
		} catch (XmlPullParserException e) {
			Toast.makeText(this, "Problem parsing available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
			this.finish();
		} finally {
		    if (reader != null) {
		        reader.close();
		    }
		}
	}

	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Wiki wiki = this.wikis.get(position);
		Log.d(TAG,"Clicked on "+wiki.toString());
		this.download(wiki);
	}
	private void download(Wiki wiki) {
		// check already downloaded
		File already_there = wiki.isAlreadyInstalled(context);
		if (already_there == null) {
			Log.d(TAG, "we need to dl !");
		} else {
			Toast.makeText(this, "The wiki is already installed "+ already_there, Toast.LENGTH_LONG).show();
		}
	}

}