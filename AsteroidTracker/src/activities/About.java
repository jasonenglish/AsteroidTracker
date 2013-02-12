package activities;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.google.analytics.tracking.android.EasyTracker;
import com.vitruviussoftware.bunifish.asteroidtracker.R;
import domains.AboutAsteroidTracker;
import activities.fragment.AsteroidTabFragments;
import adapters.AboutAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class About extends SherlockListActivity {


    public static Drawable drawableAbout;
    static AboutAdapter AboutDapter;
    ArrayList<AboutAsteroidTracker> aboutEntityList = new ArrayList();
    ActionBar actionBar;
    ListView ListView_acout;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar=getSupportActionBar();
        setTitle(getResources().getString(R.string.about));
        AboutAsteroidTracker about = new AboutAsteroidTracker();
        aboutEntityList.add(about);
        final ProgressDialog ArtcleDialog = ProgressDialog.show(this, "","", true);
        final Handler Artclehandler = new Handler() {
            public void handleMessage(Message msg) {
                ArtcleDialog.dismiss();
            }
        };
        Thread checkUpdate = new Thread() {
            public void run() {
                Artclehandler.sendEmptyMessage(0);
                About.this.runOnUiThread(new Runnable() {
                       public void run() {
                           AboutDapter = new AboutAdapter(About.this, R.layout.about_main, aboutEntityList);
                           setListAdapter(About.this.AboutDapter);
                       }
                   });
            }
        };
        checkUpdate.start();
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }

    private OnClickListener GoToNASANeoSite = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://neo.jpl.nasa.gov"));
            startActivity(intent);
        }
    };

    private OnClickListener GoToBFsite = new OnClickListener() {
        public void onClick(View v) {
            String url = "";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    };

}
