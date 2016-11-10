package com.appboy.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ListView;

import com.appboy.Constants;
import com.appboy.enums.CardCategory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class FeedCategoriesFragment extends DialogFragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, FeedCategoriesFragment.class.getName());
  public static final String CATEGORIES_STRING = "categories";

  /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
  public interface NoticeDialogListener {
    void onDialogPositiveClick(FeedCategoriesFragment dialog);
  }

  public FeedCategoriesFragment() {}

  public EnumSet<CardCategory> selectedCategories;

  // Use this instance of the interface to deliver action events
  NoticeDialogListener mListener;
  boolean[] mCategoryIsChecked;

  static final String[] CATEGORIES = {"all", CardCategory.ADVERTISING.toString(), CardCategory.ANNOUNCEMENTS.toString(), CardCategory.NEWS.toString(), CardCategory.SOCIAL.toString()};

  static FeedCategoriesFragment newInstance(EnumSet<CardCategory> categories) {
    FeedCategoriesFragment categoriesFragment = new FeedCategoriesFragment();

    Bundle args = new Bundle();
    args.putSerializable(CATEGORIES_STRING, categories);
    categoriesFragment.setArguments(args);

    return categoriesFragment;
  }

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      mListener = (NoticeDialogListener) activity;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(activity.toString()
          + " must implement NoticeDialogListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    selectedCategories = (EnumSet<CardCategory>)getArguments().getSerializable(CATEGORIES_STRING);
    mCategoryIsChecked = getBooleansFromEnumSet(selectedCategories);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    // Set the dialog title
    builder.setTitle("News Feed Categories")

        // Specify the list array, the items to be selected by default (the EnumSet from DroidBoyActivity),
        // and the listener through which to receive callbacks when items are selected
        .setMultiChoiceItems(CATEGORIES, mCategoryIsChecked,
            new DialogInterface.OnMultiChoiceClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                ListView lv = ((AlertDialog)getDialog()).getListView();
                if (which == 0) {
                  // The "All" option is clicked, we should update all other options to be checked/unchecked.
                  for (int i = 0; i < Arrays.asList(CATEGORIES).size(); i++) {
                    lv.setItemChecked(i, isChecked);
                    mCategoryIsChecked[i] = isChecked;
                  }
                } else if (which < Arrays.asList(CATEGORIES).size()) {
                  mCategoryIsChecked[which] = isChecked;
                  if (!isChecked) {
                    // When there is an option is unchecked, we should also unchecked the "All" option
                    lv.setItemChecked(0, false);
                    mCategoryIsChecked[0] = false;
                  }
                }
              }
            }
        )
        // Set the action buttons
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            selectedCategories = getEnumSetFromBooleans(mCategoryIsChecked);
            mListener.onDialogPositiveClick(FeedCategoriesFragment.this);
          }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
          }
        });
    return builder.create();
  }

  @SuppressWarnings("checkstyle:localvariablename")
  private boolean[] getBooleansFromEnumSet(EnumSet<CardCategory> categories) {
    boolean[] array = new boolean[CATEGORIES.length];
    if (categories.equals(CardCategory.getAllCategories())) {
      Arrays.fill(array, true);
      return array;
    } else {
      Arrays.fill(array, false);
      List<String> categoriesStringList = Arrays.asList(CATEGORIES);
      for (int i = 0; i < categoriesStringList.size(); i ++) {
        String category = categoriesStringList.get(i);
        categoriesStringList.set(i, category.toUpperCase(Locale.US));
      }
      for (CardCategory category: categories) {
        int i = categoriesStringList.indexOf(category.toString());
        if (i >= 0) {
          array[i] = true;
        } else {
          Log.i(TAG, "Cannot find Category for" + category.toString() + "in the categories list.");
        }
      }
      return array;
    }
  }

  private EnumSet<CardCategory> getEnumSetFromBooleans(boolean[] isChecked) {
    EnumSet<CardCategory> set = EnumSet.noneOf(CardCategory.class);
    if (isChecked[0]) {
      set = CardCategory.getAllCategories();
    } else {
      for (int i = 1; i < Arrays.asList(CATEGORIES).size(); i ++) {
        if (isChecked[i]) {
          set.add(CardCategory.get(CATEGORIES[i]));
        }
      }
      if (set.isEmpty()) {
        set.add(CardCategory.NO_CATEGORY);
      }
    }
    return set;
  }
}
