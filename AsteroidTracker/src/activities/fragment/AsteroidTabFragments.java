package activities.fragment;

import service.SharingService;
import utils.SkyLogUtil;
import activities.BaseActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.viewpagerindicator.PageIndicator;
import com.vitruviussoftware.bunifish.asteroidtracker.R;

import fragments.BookFragment;
import fragments.ImpactFragment;
import fragments.NewsFragment;
import fragments.RecentFragment;
import fragments.UpcomingFragment;

public class AsteroidTabFragments extends BaseActivity {
    PageIndicator mIndicator;
    public static FragPageAdapter mPagerAdapter;
    public static SkyLogUtil skyLogUtil = new SkyLogUtil();
    public static ViewPager mViewPager;
    public static Context cText;
    public static Drawable drawable;
    public static SharingService shareSvc = new SharingService();
    public static String TAG = "AsteroidTabFragments";
    static com.actionbarsherlock.view.MenuItem reloadItem;
    ActionBar actionBar;
    private Tracker defaultTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        Configuration conf = getResources().getConfiguration();
        fixActionBar(conf);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tabs_viewpager_layout);
        actionBar = getSupportActionBar();
        setSupportProgressBarIndeterminateVisibility(false);
        cText = this;
        initActionBarFragmentsAndPading(actionBar);
        drawable = getResources().getDrawable(R.drawable.asteroid);

        if (!skyLogUtil.checkRunDateIsValid()) {
            View bannerskylog = (View) findViewById(R.id.bannerskylog);
            bannerskylog.setVisibility(View.GONE);
        }
        // Buttons for SkyLog Banner
        final Button bannerVote = (Button) findViewById(R.id.bannerVote);
        bannerVote.setOnClickListener(clickListnerBannerVote);

        final Button bannerMore = (Button) findViewById(R.id.bannerMore);
        bannerMore.setOnClickListener(clickListnerBannerMore);

        EasyTracker.getInstance().setContext(this);
        defaultTracker = EasyTracker.getTracker();
    }

    public OnClickListener clickListnerBannerVote = new OnClickListener() {
        public void onClick(View view) {
            sentTrackingEvent("SkyLog", "vote", "banner", null);
            String twitterMessage = getString(R.string.skylog_twitter_message);
            String twitterShareLink = getString(R.string.skylog_twitter_share_link);
            Intent shareMe = AsteroidTabFragments.shareSvc.createTwitterShareIntent(twitterMessage, twitterShareLink, cText);
          ((SherlockFragmentActivity)cText).startActivity(shareMe);
        }
    };

    public OnClickListener clickListnerBannerMore = new OnClickListener() {
        public void onClick(View view) {
            sentTrackingEvent("SkyLog", "moreInfo", "banner", null);
            openAboutSkylogApp(cText);
        }
    };
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

    public FragPageAdapter getAdap(){
        return AsteroidTabFragments.mPagerAdapter;
    }

    public void initActionBarFragmentsAndPading(ActionBar actionBar)
    {
        mViewPager = (ViewPager)super.findViewById(R.id.viewpager);
        mPagerAdapter = new FragPageAdapter(this, actionBar, mViewPager);
        mPagerAdapter.addTab(actionBar.newTab().setText("Recent") ,RecentFragment.class, null);
        mPagerAdapter.addTab(actionBar.newTab().setText("Upcoming") ,UpcomingFragment.class, null);
        mPagerAdapter.addTab(actionBar.newTab().setText("ImpactRisk") ,ImpactFragment.class, null);
        mPagerAdapter.addTab(actionBar.newTab().setText("SpaceTracks") ,NewsFragment.class, null);
        mPagerAdapter.addTab(actionBar.newTab().setText("Books") ,BookFragment.class, null);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.about:
                openAbout(this);
                return true;
            case R.id.aboutSkylog:
                openAboutSkylogApp(this);
                return true;
        default:
        return false;
        }
    }

    public void fixActionBar(Configuration configuration) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        getSherlock().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixActionBar(newConfig);
    }

    public void sentTrackingEvent(String Category, String Action, String Label, Long value) {
        try {
            defaultTracker.sendEvent(Category, Action, Label, value);
        } catch(NullPointerException e) {
            EasyTracker.getInstance().setContext(this);
            defaultTracker = EasyTracker.getTracker();
            defaultTracker.sendEvent(Category, Action, Label, value);
        }
    }

}