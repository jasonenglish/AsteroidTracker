package fragments;

import java.util.List;

import activities.fragment.AsteroidTabFragments;
import adapters.BooksAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.MenuItem;
import com.vitruviussoftware.bunifish.asteroidtracker.R;

import domains.AmazonItemListing;
import domains.baseEntity;

public class BookFragment extends AsteroidFragmentBase {

    public BooksAdapter adapterBooks;
    public List<AmazonItemListing> items;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingMessage = resources.getString(R.string.text_content_loading_books);
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(LOADER_BOOKS, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemClickListener(listingClickListener);
    }


    @Override
    public Loader<List> onCreateLoader(int id, Bundle args) {
        super.onCreateLoader(id, args);
            AsyncTaskLoader<List> Loader = new AsyncTaskLoader<List>(getActivity()) {
            @Override
            public List<AmazonItemListing> loadInBackground() {
                items = downloadManager.retrieveScienceBooks(isNetworkAvailable);
                return items;
                }
            };
            Loader.forceLoad();
            return Loader;
        }

    @Override
    public void onLoadFinished( Loader<List> loader, List data ) {
        super.onLoadFinished(loader, data);
        if (adapterBooks != null) {
            if (adapterBooks.getItem(0).title.equals(baseEntity.FAILURELOADING)) {
                loadContent(data);
            } else {
                if(data.size() > 1){
                    loadContent(data);
                }
            }
        } else {
            loadContent(data);
        }
    }

    public void loadContent(List data) {
        adapterBooks = new BooksAdapter(AsteroidTabFragments.cText, R.layout.view_books_fragment, data);
        setListAdapter(adapterBooks);
    }

    protected void restartLoading(MenuItem item) {
        reloadItem = item;
        setRefreshIcon(true, "Books");
        getLoaderManager().restartLoader(LOADER_BOOKS, null, this);
    }

    public boolean onOptionsItemSelected(final MenuItem item) 
    {
      switch (item.getItemId()) {
      case R.id.reload:
          restartLoading(item);
          return super.onOptionsItemSelected(item);
    default:
        return false;
        }
    }
    
    public OnItemClickListener listingClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View view, int position, long id) { 
            if (!adapterBooks.getItem(0).title.equals(baseEntity.FAILURELOADING)) {
                Object object = getListAdapter().getItem(position);    
                AmazonItemListing asteroidEntity = (AmazonItemListing) object;
                defaultTracker.sendEvent("Books", "book_click", "Title: "+ asteroidEntity.getTitle(), null);
                Intent i = new Intent(Intent.ACTION_VIEW);
                try {
                    i.setData(Uri.parse(asteroidEntity.getDetailPageUri()));
                    startActivity(i);
                } catch (ActivityNotFoundException e){
                    Log.d("Books", "clicklistner", e);
                }
            }
        };
    };

}