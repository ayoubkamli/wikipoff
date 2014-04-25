package fr.renzo.wikipoff;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


public class TabInstalledFragment extends Fragment implements OnItemClickListener {
	private SharedPreferences config;
	private File rootDbDir;
	private ArrayList<Wiki> installedwikis=new ArrayList<Wiki>();
	private ListView installedwikislistview;
	private Context context;
	private View wholeview;

	private static final String TAG = "TabInstalledFragment";

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context= getActivity();
		config = PreferenceManager.getDefaultSharedPreferences(context);
		rootDbDir= new File(Environment.getExternalStorageDirectory(),context.getString(R.string.DBDir));

		wholeview=inflater.inflate(R.layout.fragment_tab_installed,null);
		this.installedwikis=loadInstalledDb();

		InstalledWikisListViewAdapter adapter = new InstalledWikisListViewAdapter(getActivity(),  this.installedwikis); 

		installedwikislistview= (ListView) wholeview.findViewById(R.id.installedwikislistview);
		installedwikislistview.setAdapter(adapter);
		installedwikislistview.setOnItemClickListener(this);

		return wholeview;

	}

	private ArrayList<Wiki> loadInstalledDb() {
		
		ArrayList<Wiki> res = new ArrayList<Wiki>();
		for (File f : rootDbDir.listFiles()) {
			String name = f.getName();
			if (name.endsWith(".sqlite")) {
				Wiki w;
				try {
					w = new Wiki(context,f);
					res.add(w);
				} catch (WikiException e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
		Collections.sort(res, new Comparator<Wiki>() {
			public int compare(Wiki w1, Wiki w2) {
				if (w1.getLangcode().equals(w2.getLangcode())) {
					return w1.getGendate().compareTo(w2.getGendate());
				} else {
					return w1.getLangcode().compareToIgnoreCase(w2.getLangcode());
				}
			}
		}
				);

		return res;
	}


	public class InstalledWikisListViewAdapter extends BaseAdapter implements OnClickListener {
		private LayoutInflater inflater;
		private ArrayList<Wiki> data;
		private int selectedPosition = 0;
		public InstalledWikisListViewAdapter(Context context, ArrayList<Wiki> data){
			// Caches the LayoutInflater for quicker use
			this.inflater = LayoutInflater.from(context);
			// Sets the events data
			this.data= data;
		}
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			if(position < getCount() && position >= 0 ){
				return position;
			} else {
				return -1;
			}
		}

		@Override
		public void onClick(View view) {
			selectedPosition = (Integer)view.getTag();
			notifyDataSetInvalidated();
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Wiki w = data.get(position);
			if(convertView == null){ // If the View is not cached
				// Inflates the Common View from XML file
				convertView = this.inflater.inflate(R.layout.installed_wiki, null);
			}
			TextView header = (TextView ) convertView.findViewById(R.id.installedwikiheader);
			header.setText(w.getType()+" "+w.getLanglocal());
			TextView bot = (TextView ) convertView.findViewById(R.id.installedwikifooter);
			bot.setText(w.getFilename()+" "+w.getLocalizedGendate());
			RadioButton rb = (RadioButton) convertView.findViewById(R.id.radio);
			rb.setChecked(w.isSelected());
			rb.setTag(position);
			rb.setOnClickListener(this);
			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Wiki wiki = installedwikis.get(position);
		Log.d(TAG,"Clicked on "+wiki.toString());
		config.edit().putString(context.getString(R.string.config_key_selecteddbfile),wiki.getFilename() ).commit();
		RadioButton rb =(RadioButton) view.findViewById(R.id.radio);
		rb.setSelected(true);
		for (int i = 0; i < installedwikis.size(); i++) {
			View ll = (View) installedwikislistview.getChildAt(i);
			RadioButton rbb =(RadioButton) ll.findViewById(R.id.radio);
			if (i!=position) {
				rbb.setChecked(false);
			} else {
				rbb.setChecked(true);

			}
		}

	}	
}