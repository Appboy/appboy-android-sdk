package com.appboy.ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import com.appboy.Appboy;
import com.appboy.IAppboy;
import com.appboy.enums.SlideupPriority;
import com.appboy.events.FeedUpdatedEvent;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.SlideupEvent;
import com.appboy.models.cards.ICard;
import com.appboy.models.results.Slideup;
import com.appboy.ui.adapters.AppboyListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Appboy news stream that can be dropped into any app
 */
public class AppboyFeedActivity extends ListActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedActivity.class.getName());

  private IAppboy mAppboy;
  private ArrayAdapter<ICard> mAdapter;
  private List<ICard> mCardList;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.appboy_feed);
    mAppboy = Appboy.getInstance(this);

    mCardList = new ArrayList<ICard>();
    mAdapter = new AppboyListAdapter(this, R.id.tag, mCardList);

    Button refreshButton = (Button) findViewById(R.id.refreshButton);
    refreshButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mAppboy.refreshAll();
      }
    });

    setListAdapter(mAdapter);
  }

  // We place openSession in onStart as opposed to onResume because onResume only gets called when the user can
  // interact with the activity. If there is an overlapping dialog, onStart will have been called, but on onResume
  // will only be called once the dialog has been dismissed
  @Override
  public void onStart() {
    super.onStart();
    IEventSubscriber<FeedUpdatedEvent> feedSubscriber = new IEventSubscriber<FeedUpdatedEvent>() {
      @Override
      public void trigger(FeedUpdatedEvent event) {
        mAdapter.clear();
        mCardList = event.getFeedCards();
        for (ICard card : mCardList) {
          mAdapter.add(card);
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mAdapter.notifyDataSetChanged();
          }
        });
      }
    };
    IEventSubscriber<SlideupEvent> slideupSubscriber = new IEventSubscriber<SlideupEvent>() {
      @Override
      public void trigger(SlideupEvent event) {
        Slideup slideup = event.getSlideup();
        if (slideup.getPriority() == SlideupPriority.IMMEDIATE) {
          Toast.makeText(AppboyFeedActivity.this, slideup.getMessage(), Toast.LENGTH_LONG).show();
        }
      }
    };
    mAppboy.subscribeToFeedUpdates(feedSubscriber, this);
    mAppboy.subscribeToNewSlideups(slideupSubscriber, this);
    mAppboy.openSession();
  }

  @Override
  public void onStop() {
    mAppboy.removeActivitySubscriptions(this);
    mAppboy.closeSession();
    super.onStop();
  }
}