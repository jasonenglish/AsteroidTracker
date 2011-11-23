package com.vitruviussoftware.bunifish.asteroidtracker;

import android.net.Uri;
import android.os.Bundle;
import java.util.List;
import com.vitruviussoftware.bunifish.asteroidtracker.R;
import com.vitruviussoftware.bunifish.asteroidtracker.database.NewsDB_Adtapter;

import nasa.neoAstroid.nasa_neo;
import nasa.neoAstroid.nasa_neoArrayAdapter;
import nasa.neoAstroid.neoAstroidFeed;
import nasa.neoAstroid.impackRisk.nasa_neoImpactAdapter;
import nasa.neoAstroid.impackRisk.nasa_neoImpactEntity;
import nasa.neoAstroid.news.AsteroidNewsProxy;
import nasa.neoAstroid.news.asteroidNewsAdapter;
import nasa.neoAstroid.news.newsEntity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;

public class AsteroidTrackerActivity extends ListActivity {

	static nasa_neoArrayAdapter adapter_RECENT; 
	static nasa_neoArrayAdapter adapter_UPCOMING; 
	static nasa_neoImpactAdapter adapter_IMPACT;
	static asteroidNewsAdapter adapter_NEWS;
	public static List<nasa_neo> List_NASA_RECENT;
	public static List<nasa_neo> List_NASA_UPCOMING;
	public static List<nasa_neoImpactEntity> List_NASA_IMPACT;
	public static List<newsEntity> List_NASA_News;
	ListView ls1_ListView_Recent;
	ListView ls2_ListView_Upcoming;
	ListView ls3_ListView_Impact;
	ListView ls4_ListView_News;
	View ImgView;
	public static Drawable drawable;
	static TabHost tabHost;
	TabSpec TabSpec1_Recent;
	TabSpec TabSpec2_Upcoming;
	TabSpec TabSpec3_Impact;
	TabSpec TabSpec4_News;
	static neoAstroidFeed neo_AstroidFeed = new neoAstroidFeed();
	static AsteroidNewsProxy NewsProxy = new AsteroidNewsProxy();
	public static boolean refresh = false;
	ProgressDialog dialog;
	Handler handler;
	int closeDialog = 0;
	
	
	private NewsDB_Adtapter dbHelper; 
	private Cursor cursor;
	
//	long startTime;
//	long endTime;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.asteroid_main_tablayout);
		setTitle("Asteroid Tracker");
		Resources res = getResources();
		drawable = res.getDrawable(R.drawable.asteroid);
		TabAndListViewSetup();		
		dbHelper = new NewsDB_Adtapter(this);
		dbHelper.open();
		fillData();
		processFeeds();
    }

    
    private void fillData() {
		cursor = dbHelper.fetchAllArticles();
		startManagingCursor(cursor);
		String[] from = new String[] { NewsDB_Adtapter.KEY_TITLE };
		Log.i("DB", "FETCH_SIZE: "+from.length);
		Log.i("DB", "FETCH_STRING: "+from[0].toLowerCase());
		Log.i("DB", "first cursor count: "+ cursor.getCount());
		
		long created = dbHelper.createNewsArticle("TitleTest", "CoolArtcile", "test.blah.com","10302011", "10292011");
		Log.i("DB", "created: "+created);
		cursor = dbHelper.fetchAllArticles();
		Log.i("DB", "next cursor count: "+ cursor.getCount());
		
		if(cursor.getCount() > 1){
			Cursor article = dbHelper.fetchArticle(1);
			startManagingCursor(article);
			String title = article.getString(article.getColumnIndexOrThrow(dbHelper.KEY_TITLE));
			Log.i("DB", "NEWS_title: "+title);
			
			String LastModified = article.getString(article.getColumnIndexOrThrow(dbHelper.KEY_LASTMODIFIED));
			Log.i("DB", "NEWS_LastModified: "+LastModified);

			if(LastModified.equals("10302011")){
				Log.i("DB", "Up to date");
			}else{
				Log.i("DB", "Not UpToDate");
			}
		}	
		dbHelper.deleteAllArticles();
		cursor = dbHelper.fetchAllArticles();
		Log.i("DB", "next cursor count: "+ cursor.getCount());
		
    }
    
    public void TabAndListViewSetup(){
    	tabHost=(TabHost)findViewById(R.id.tabHost);
		tabHost.setup();
		tabHost.getTabWidget().setDividerDrawable(R.drawable.silverbar);
		
		ls1_ListView_Recent = new ListView(AsteroidTrackerActivity.this);   
		TabSpec1_Recent=tabHost.newTabSpec("Tab 1");
		TabSpec1_Recent.setIndicator("RECENT");
		TabSpec1_Recent.setContent(R.id.tab1);
		
		ls2_ListView_Upcoming = new ListView(AsteroidTrackerActivity.this);  
		TabSpec2_Upcoming=tabHost.newTabSpec("Tab 2");
		TabSpec2_Upcoming.setIndicator("UPCOMING");
		TabSpec2_Upcoming.setContent(R.id.tab2);
		
		ls3_ListView_Impact = new ListView(AsteroidTrackerActivity.this);  
		TabSpec3_Impact=tabHost.newTabSpec("Tab 3");
		TabSpec3_Impact.setIndicator("IMPACT RISK");
		TabSpec3_Impact.setContent(R.id.tab3);
		
		ls4_ListView_News = new ListView(AsteroidTrackerActivity.this); 
		TabSpec4_News=tabHost.newTabSpec("Tab 4");
		TabSpec4_News.setIndicator("NEWS");
		TabSpec4_News.setContent(R.id.tab4);
		
		tabHost.addTab(TabSpec1_Recent);
		tabHost.addTab(TabSpec2_Upcoming);
		tabHost.addTab(TabSpec3_Impact);
		tabHost.addTab(TabSpec4_News);
    }
    
    public void processFeeds(){
    	progressDialog();
//      startTime = System.currentTimeMillis();
    	tabHost.setCurrentTab(0);
    	processImpactFeed();
		processAsteroidNewsFeed();
		processNEOFeed();
    	
    }
    
    public void closeDialog(){
    	closeDialog++;
    	if(closeDialog == 3){
    		handler.sendEmptyMessage(0);
    		}
    	}
    
    public void progressDialog(){
    	  dialog = ProgressDialog.show(this, "", "Loading NASA Asteroid Feed...", true);
    	  handler = new Handler() {
			public void handleMessage(Message msg) {
				dialog.dismiss();}
			};
    }
    
    public void processNEOFeed(){
			Thread checkUpdate = new Thread() {
			public void run() {	 
					if(!refresh){
						String HTTPDATA =  AsteroidTrackerActivity.neo_AstroidFeed.getAstroidFeedDATA(AsteroidTrackerActivity.neo_AstroidFeed.URL_NASA_NEO);
						loadEntityLists_NEO(HTTPDATA);
					}
					LoadAdapters_NEO();
					refresh = true;
				AsteroidTrackerActivity.this.runOnUiThread(new Runnable() {
		               @Override
		               public void run() {
			           	Log.i("HTTPFEED", "Setting data: NEO");
		            	   SetAdapters_NEO();
		               }
		           });
				Log.i("HTTPFEED", "closeing-Dialog:"+closeDialog);
				closeDialog();
			}};
			checkUpdate.start();
			}
    
    public void processImpactFeed(){
		Thread ImpactUpdate = new Thread() {
			public void run() {			 
					if(!refresh){
						String HTTP_IMPACT_DATA =  AsteroidTrackerActivity.neo_AstroidFeed.getAstroidFeedDATA(AsteroidTrackerActivity.neo_AstroidFeed.URL_NASA_NEO_IMPACT_FEED);
						loadEntityLists_IMPACT(HTTP_IMPACT_DATA);
					}
					LoadAdapters_IMPACT();
					refresh = true;
				AsteroidTrackerActivity.this.runOnUiThread(new Runnable() {
		               @Override
		               public void run() {
		            	   dialog.setMessage("Loading NASA Impact Risk Feed...");
		            	   Log.i("HTTPFEED", "Setting data: IMPACT");
		            	   SetAdapters_IMPACT();
		               }
		           });
				Log.i("HTTPFEED", "closeing-Dialog:"+closeDialog);
				closeDialog();
			}};
			ImpactUpdate.start();
    }
    
    public void processAsteroidNewsFeed(){
		Thread NewsUpdate = new Thread() {
			public void run() {			 
					if(!refresh){
						String HTTP_NEWS_DATA = AsteroidTrackerActivity.neo_AstroidFeed.getAstroidFeedDATA(AsteroidTrackerActivity.neo_AstroidFeed.URL_JPL_AsteroidNewsFeed);
						loadEntityLists_NEWS(HTTP_NEWS_DATA);
					}
					LoadAdapters_NEWS();
					refresh = true;
			    AsteroidTrackerActivity.this.runOnUiThread(new Runnable() {
		               @Override
		               public void run() {
		            	   dialog.setMessage("Loading NASA News Feed...");
		            	   Log.i("HTTPFEED", "Setting data: NEWS");
		            	   SetAdapters_NEWS();
		               }
		           });
				Log.i("HTTPFEED", "closeing-Dialog:"+closeDialog);
				closeDialog();
			}};
			NewsUpdate.start();
    }



    public void loadEntityLists_NEO(String HTTPDATA){
	  	AsteroidTrackerActivity.List_NASA_RECENT = AsteroidTrackerActivity.neo_AstroidFeed.getRecentList(HTTPDATA);
    	AsteroidTrackerActivity.List_NASA_UPCOMING = AsteroidTrackerActivity.neo_AstroidFeed.getUpcomingList(HTTPDATA);
    }
    public void loadEntityLists_IMPACT(String HTTPDATA){
    	AsteroidTrackerActivity.List_NASA_IMPACT = AsteroidTrackerActivity.neo_AstroidFeed.getImpactList(HTTPDATA);
    }
    public void loadEntityLists_NEWS(String HTTPDATA){
    	AsteroidTrackerActivity.List_NASA_News = AsteroidTrackerActivity.NewsProxy.parseNewsFeed(HTTPDATA);
    	//Check if data was already loaded today.
    	//
    }
    
    
    public void LoadAdapters_NEO(){
    	AsteroidTrackerActivity.this.adapter_RECENT = new nasa_neoArrayAdapter(AsteroidTrackerActivity.this, R.layout.nasa_neolistview, AsteroidTrackerActivity.List_NASA_RECENT);
    	AsteroidTrackerActivity.this.adapter_UPCOMING = new nasa_neoArrayAdapter(AsteroidTrackerActivity.this, R.layout.nasa_neolistview, AsteroidTrackerActivity.List_NASA_UPCOMING);
    }
    public void LoadAdapters_IMPACT(){
    	AsteroidTrackerActivity.this.adapter_IMPACT = new nasa_neoImpactAdapter(AsteroidTrackerActivity.this, R.layout.nasa_neo_impact_listview, AsteroidTrackerActivity.List_NASA_IMPACT);
    }
    public void LoadAdapters_NEWS(){
		AsteroidTrackerActivity.this.adapter_NEWS = new asteroidNewsAdapter(AsteroidTrackerActivity.this, R.layout.jpl_asteroid_news, AsteroidTrackerActivity.List_NASA_News);
    }
    
   
    public void SetAdapters_NEO(){
    	setListAdapter(adapter_RECENT);
//    		spec1.setContent(new TabHost.TabContentFactory(){
//   		        public View createTabContent(String tag)
//   		        {
//   		        	ls1.setAdapter(AsteroidTrackerActivity.adapter);
//   		            return ls1;
//   		        }       
//   		    });
    		TabSpec2_Upcoming.setContent(new TabHost.TabContentFactory(){
		        public View createTabContent(String tag)
		        {
		        	ls2_ListView_Upcoming.setAdapter(AsteroidTrackerActivity.adapter_UPCOMING);
		            return ls2_ListView_Upcoming;
		        }       
		    });
    }
    public void SetAdapters_IMPACT(){
    	TabSpec3_Impact.setContent(new TabHost.TabContentFactory(){
	        public View createTabContent(String tag)
	        {
	        	ls3_ListView_Impact.setAdapter(AsteroidTrackerActivity.adapter_IMPACT);
	        	ls3_ListView_Impact.setOnItemClickListener(ImpactRiskClickListener);
	        	return ls3_ListView_Impact;
	        }       
	    });	
    }
    public void SetAdapters_NEWS(){
    	TabSpec4_News.setContent(new TabHost.TabContentFactory(){
   	        public View createTabContent(String tag)
   	        {
   	        	ls4_ListView_News.setAdapter(AsteroidTrackerActivity.adapter_NEWS);
   	        	ls4_ListView_News.setOnItemClickListener(Asteroid_NewsArticle_ClickListener);
   	        	return ls4_ListView_News;
   	        }       
   	    });	
    }
 
    
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.quit:
			finish();
			return true;
		case R.id.about:
			openAbout();
			return true;
		case R.id.refresh:
			refresh = false;
			closeDialog = 0;
			processFeeds();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
}

	public void openAbout() {
		Intent i = new Intent(AsteroidTrackerActivity.this, about.class);
        startActivity(i);	
       }
	
	
	
	public OnItemClickListener ImpactRiskClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView parent, View view, int position, long id) {
		    Object object = AsteroidTrackerActivity.this.ls3_ListView_Impact.getAdapter().getItem(position);	
		    nasa_neoImpactEntity asteroidEntity = (nasa_neoImpactEntity) object;
		    Intent openArticleView = new Intent(AsteroidTrackerActivity.this, nasa.neoAstroid.impackRisk.ImpactRiskDetailView.class);
			Log.i("track", Integer.toString(position));
		    openArticleView.putExtra("position", position);
		    startActivity(openArticleView);
        };
	};

	public OnItemClickListener Asteroid_NewsArticle_ClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView parent, View view, int position, long id) {
		    Object object = AsteroidTrackerActivity.this.ls4_ListView_News.getAdapter().getItem(position);	
		    newsEntity asteroidEntity = (newsEntity) object;
		    Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(asteroidEntity.artcileUrl));
			startActivity(i);
			
        };
	};
}