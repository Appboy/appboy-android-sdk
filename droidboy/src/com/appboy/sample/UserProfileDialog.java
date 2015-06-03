package com.appboy.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.Constants;
import com.appboy.enums.Gender;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.sample.util.SharedPrefsUtil;
import com.appboy.ui.support.StringUtils;
import com.crittercism.app.Crittercism;

public class UserProfileDialog extends DialogPreference {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, UserProfileDialog.class.getName());
  private static final int GENDER_UNSPECIFIED_INDEX = 0;
  private static final int GENDER_MALE_INDEX = 1;
  private static final int GENDER_FEMALE_INDEX = 2;

  private static final String FIRST_NAME_PREFERENCE_KEY = "user.firstname";
  private static final String LAST_NAME_PREFERENCE_KEY = "user.lastname";
  private static final String EMAIL_PREFERENCE_KEY = "user.email";
  private static final String BIO_PREFERENCE_KEY = "user.bio";
  private static final String GENDER_PREFERENCE_KEY = "user.gender_resource_id";
  private static final String AVATAR_PREFERENCE_KEY = "user.avatar_image_url";

  private static final String SAMPLE_FIRST_NAME = "Jane";
  private static final String SAMPLE_LAST_NAME = "Doe";
  private static final String SAMPLE_EMAIL = "jane@appboy.com";
  private static final String SAMPLE_BIO = "I'm a Developer";
  private static final int SAMPLE_GENDER = R.id.female;
  private static final String SAMPLE_AVATAR_URL = "https://s3.amazonaws.com/appboy-dashboard-uploads/news/default-news-image.png";


  private EditText mFirstName;
  private EditText mLastName;
  private EditText mEmail;
  private EditText mBio;
  private RadioGroup mGender;
  private EditText mAvatarImageUrl;
  private CheckBox mRequestFlush;

  public UserProfileDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDialogLayoutResource(R.layout.user_preferences);
    setPersistent(false);
  }

  public UserProfileDialog(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setDialogLayoutResource(R.layout.user_preferences);
    setPersistent(false);
  }

  @Override
  public View onCreateDialogView() {
    View view = super.onCreateDialogView();
    mFirstName = (EditText) view.findViewById(R.id.first_name);
    mLastName = (EditText) view.findViewById(R.id.last_name);
    mEmail = (EditText) view.findViewById(R.id.email);
    mBio = (EditText) view.findViewById(R.id.bio);
    mGender = (RadioGroup) view.findViewById(R.id.gender);
    mAvatarImageUrl = (EditText) view.findViewById(R.id.avatar_image_url);
    mRequestFlush = (CheckBox) view.findViewById(R.id.user_dialog_flush_checkbox);
    return view;
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);

    SharedPreferences sharedPreferences = getSharedPreferences();
    mFirstName.setText(sharedPreferences.getString(FIRST_NAME_PREFERENCE_KEY, null));
    mLastName.setText(sharedPreferences.getString(LAST_NAME_PREFERENCE_KEY, null));
    mEmail.setText(sharedPreferences.getString(EMAIL_PREFERENCE_KEY, null));
    mBio.setText(sharedPreferences.getString(BIO_PREFERENCE_KEY, null));
    mGender.check(parseGenderFromSharedPreferences());
    mAvatarImageUrl.setText(sharedPreferences.getString(AVATAR_PREFERENCE_KEY, null));
    mRequestFlush.setChecked(false);
    ButtonUtils.setUpPopulateButton(view, R.id.first_name_button, mFirstName, getSharedPreferences().getString(FIRST_NAME_PREFERENCE_KEY, SAMPLE_FIRST_NAME));
    ButtonUtils.setUpPopulateButton(view, R.id.last_name_button, mLastName, getSharedPreferences().getString(LAST_NAME_PREFERENCE_KEY, SAMPLE_LAST_NAME));
    ButtonUtils.setUpPopulateButton(view, R.id.email_button, mEmail, getSharedPreferences().getString(EMAIL_PREFERENCE_KEY, SAMPLE_EMAIL));
    ButtonUtils.setUpPopulateButton(view, R.id.bio_button, mBio, getSharedPreferences().getString(BIO_PREFERENCE_KEY, SAMPLE_BIO));
    ButtonUtils.setUpPopulateButton(view, R.id.avatar_image_url_button, mAvatarImageUrl, getSharedPreferences().getString(AVATAR_PREFERENCE_KEY, SAMPLE_AVATAR_URL));

    final Button populateButton = (Button) view.findViewById(R.id.user_dialog_button_populate);
    populateButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (mFirstName.getText().length() == 0) {
          mFirstName.setText(getSharedPreferences().getString(FIRST_NAME_PREFERENCE_KEY, SAMPLE_FIRST_NAME));
        }
        if (mLastName.getText().length() == 0) {
          mLastName.setText(getSharedPreferences().getString(LAST_NAME_PREFERENCE_KEY, SAMPLE_LAST_NAME));
        }
        if (mEmail.getText().length() == 0) {
          mEmail.setText(getSharedPreferences().getString(EMAIL_PREFERENCE_KEY, SAMPLE_EMAIL));
        }
        if (mBio.getText().length() == 0) {
          mBio.setText(getSharedPreferences().getString(BIO_PREFERENCE_KEY, SAMPLE_BIO));
        }
        if (mGender.getCheckedRadioButtonId() == R.id.unspecified) {
          mGender.check(SAMPLE_GENDER);
        }
        if (mAvatarImageUrl.getText().length() == 0) {
          mAvatarImageUrl.setText(getSharedPreferences().getString(AVATAR_PREFERENCE_KEY, SAMPLE_AVATAR_URL));
        }
      }
    });
    final Button clearButton = (Button) view.findViewById(R.id.user_dialog_button_clear);
    clearButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        mFirstName.getText().clear();
        mLastName.getText().clear();
        mEmail.getText().clear();
        mBio.getText().clear();
        mGender.check(R.id.unspecified);
        mAvatarImageUrl.getText().clear();
      }
    });
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      String firstName = mFirstName.getText().toString();
      String lastName = mLastName.getText().toString();
      String email = mEmail.getText().toString();
      String bio = mBio.getText().toString();
      int genderResourceId = mGender.getCheckedRadioButtonId();
      View genderRadioButton = mGender.findViewById(genderResourceId);
      int genderId = mGender.indexOfChild(genderRadioButton);
      String avatarImageUrl = mAvatarImageUrl.getText().toString();

      if (!StringUtils.isNullOrBlank(email)) {
        Appboy.getInstance(getContext()).changeUser(email);
        // Crittercism limits the length of the username to 32 characters.
        Crittercism.setUsername(email.substring(0, Math.min(email.length(), 31)));
      }

      SharedPreferences.Editor editor = getEditor();
      editor.putString(FIRST_NAME_PREFERENCE_KEY, firstName);
      editor.putString(LAST_NAME_PREFERENCE_KEY, lastName);
      editor.putString(EMAIL_PREFERENCE_KEY, email);
      editor.putString(BIO_PREFERENCE_KEY, bio);
      editor.putInt(GENDER_PREFERENCE_KEY, genderId);
      editor.putString(AVATAR_PREFERENCE_KEY, avatarImageUrl);
      SharedPrefsUtil.persist(editor);

      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      appboyUser.setFirstName(firstName);
      appboyUser.setLastName(lastName);
      appboyUser.setEmail(email);
      appboyUser.setBio(bio);
      switch (genderId) {
        case GENDER_UNSPECIFIED_INDEX:
          appboyUser.setGender(null);
          break;
        case GENDER_MALE_INDEX:
          appboyUser.setGender(Gender.MALE);
          break;
        case GENDER_FEMALE_INDEX:
          appboyUser.setGender(Gender.FEMALE);
          break;
        default:
          Log.w(TAG, "Error parsing gender from user preferences.");
      }
      appboyUser.setAvatarImageUrl(avatarImageUrl);

      // Flushing manually is not recommended in almost all production situations as
      // Appboy automatically flushes data to its servers periodically.  This call
      // is solely for testing purposes.
      if (mRequestFlush.isChecked()) {
        Appboy.getInstance(getContext()).requestImmediateDataFlush();
      }
    }
  }

  private int parseGenderFromSharedPreferences() {
    switch (getSharedPreferences().getInt(GENDER_PREFERENCE_KEY, GENDER_UNSPECIFIED_INDEX)) {
      case GENDER_UNSPECIFIED_INDEX:
        return R.id.unspecified;
      case GENDER_MALE_INDEX:
        return R.id.male;
      case GENDER_FEMALE_INDEX:
        return R.id.female;
      default:
        Log.w(TAG, "Error parsing gender from shared preferences.");
        return R.id.unspecified;
    }
  }
}