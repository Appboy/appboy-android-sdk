package com.appboy.sample;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.Constants;
import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.support.StringUtils;

import java.util.Calendar;

public class UserProfileDialog extends DialogPreference implements View.OnClickListener {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, UserProfileDialog.class.getName());
  private static final int GENDER_UNSPECIFIED_INDEX = 0;
  private static final int GENDER_MALE_INDEX = 1;
  private static final int GENDER_FEMALE_INDEX = 2;

  private static final Calendar mCalendar = Calendar.getInstance();

  private static final String FIRST_NAME_PREFERENCE_KEY = "user.firstname";
  private static final String LAST_NAME_PREFERENCE_KEY = "user.lastname";
  private static final String EMAIL_PREFERENCE_KEY = "user.email";
  private static final String GENDER_PREFERENCE_KEY = "user.gender_resource_id";
  private static final String AVATAR_PREFERENCE_KEY = "user.avatar_image_url";
  private static final String BIRTHDAY_PREFERENCE_KEY = "user.birthday";

  private static final String SAMPLE_FIRST_NAME = "Jane";
  private static final String SAMPLE_LAST_NAME = "Doe";
  private static final String SAMPLE_EMAIL = "jane@appboy.com";
  private static final int SAMPLE_GENDER = R.id.female;
  private static final String SAMPLE_AVATAR_URL = "https://s3.amazonaws.com/appboy-dashboard-uploads/news/default-news-image.png";
  private static final String SAMPLE_BIRTHDAY = Integer.toString(mCalendar.get(Calendar.MONTH) + 1) + "/" + mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.YEAR);

  private EditText mFirstName;
  private EditText mLastName;
  private EditText mEmail;
  private RadioGroup mGender;
  private EditText mAvatarImageUrl;
  private CheckBox mRequestFlush;
  private TextView mBirthday;

  private DatePickerDialog mDatePickerDialog;
  private int mBirthYear;
  private int mBirthMonth;
  private int mBirthDay;
  private boolean isBirthdaySet = false;

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
    mGender = (RadioGroup) view.findViewById(R.id.gender);
    mAvatarImageUrl = (EditText) view.findViewById(R.id.avatar_image_url);
    mRequestFlush = (CheckBox) view.findViewById(R.id.user_dialog_flush_checkbox);
    mBirthday = (TextView) view.findViewById(R.id.birthday);
    return view;
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);

    SharedPreferences sharedPreferences = getSharedPreferences();
    mFirstName.setText(sharedPreferences.getString(FIRST_NAME_PREFERENCE_KEY, null));
    mLastName.setText(sharedPreferences.getString(LAST_NAME_PREFERENCE_KEY, null));
    mEmail.setText(sharedPreferences.getString(EMAIL_PREFERENCE_KEY, null));
    mGender.check(parseGenderFromSharedPreferences());
    mAvatarImageUrl.setText(sharedPreferences.getString(AVATAR_PREFERENCE_KEY, null));
    mBirthday.setText(sharedPreferences.getString(BIRTHDAY_PREFERENCE_KEY, null));
    mRequestFlush.setChecked(false);

    ButtonUtils.setUpPopulateButton(view, R.id.first_name_button, mFirstName, getSharedPreferences().getString(FIRST_NAME_PREFERENCE_KEY, SAMPLE_FIRST_NAME));
    ButtonUtils.setUpPopulateButton(view, R.id.last_name_button, mLastName, getSharedPreferences().getString(LAST_NAME_PREFERENCE_KEY, SAMPLE_LAST_NAME));
    ButtonUtils.setUpPopulateButton(view, R.id.email_button, mEmail, getSharedPreferences().getString(EMAIL_PREFERENCE_KEY, SAMPLE_EMAIL));
    ButtonUtils.setUpPopulateButton(view, R.id.avatar_image_url_button, mAvatarImageUrl, getSharedPreferences().getString(AVATAR_PREFERENCE_KEY, SAMPLE_AVATAR_URL));

    final Button populateButton = (Button) view.findViewById(R.id.user_dialog_button_populate);
    final Button clearButton = (Button) view.findViewById(R.id.user_dialog_button_clear);
    final Button birthdayButton = (Button) view.findViewById(R.id.birthday_button);

    populateButton.setOnClickListener(this);
    clearButton.setOnClickListener(this);
    birthdayButton.setOnClickListener(this);

    mDatePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mBirthYear = year;
        mBirthMonth = monthOfYear;
        mBirthDay = dayOfMonth;
        mBirthday.setText(getBirthday());
        isBirthdaySet = true;
      }
    }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.user_dialog_button_clear:
        clear();
        break;
      case R.id.user_dialog_button_populate:
        populate();
        break;
      case R.id.birthday_button:
        mDatePickerDialog.show();
        break;
      default:
        break;
    }
  }

  private void clear() {
    mFirstName.getText().clear();
    mLastName.getText().clear();
    mEmail.getText().clear();
    mGender.check(R.id.unspecified);
    mAvatarImageUrl.getText().clear();
    mBirthday.setText("");
    isBirthdaySet = false;
  }

  private void populate() {
    if (mFirstName.getText().length() == 0) {
      mFirstName.setText(getSharedPreferences().getString(FIRST_NAME_PREFERENCE_KEY, SAMPLE_FIRST_NAME));
    }
    if (mLastName.getText().length() == 0) {
      mLastName.setText(getSharedPreferences().getString(LAST_NAME_PREFERENCE_KEY, SAMPLE_LAST_NAME));
    }
    if (mEmail.getText().length() == 0) {
      mEmail.setText(getSharedPreferences().getString(EMAIL_PREFERENCE_KEY, SAMPLE_EMAIL));
    }
    if (mGender.getCheckedRadioButtonId() == R.id.unspecified) {
      mGender.check(SAMPLE_GENDER);
    }
    if (mAvatarImageUrl.getText().length() == 0) {
      mAvatarImageUrl.setText(getSharedPreferences().getString(AVATAR_PREFERENCE_KEY, SAMPLE_AVATAR_URL));
    }
    if (mBirthday.getText().length() == 0) {
      mBirthday.setText(getSharedPreferences().getString(BIRTHDAY_PREFERENCE_KEY, SAMPLE_BIRTHDAY));
      isBirthdaySet = true;
    }
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      String firstName = mFirstName.getText().toString();
      String lastName = mLastName.getText().toString();
      String email = mEmail.getText().toString();
      int genderResourceId = mGender.getCheckedRadioButtonId();
      View genderRadioButton = mGender.findViewById(genderResourceId);
      int genderId = mGender.indexOfChild(genderRadioButton);
      String avatarImageUrl = mAvatarImageUrl.getText().toString();

      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      SharedPreferences.Editor editor = getEditor();
      if (!StringUtils.isNullOrBlank(firstName)) {
        appboyUser.setFirstName(firstName);
        editor.putString(FIRST_NAME_PREFERENCE_KEY, firstName);
      }
      if (!StringUtils.isNullOrBlank(lastName)) {
        appboyUser.setLastName(lastName);
        editor.putString(LAST_NAME_PREFERENCE_KEY, lastName);
      }
      if (!StringUtils.isNullOrBlank(email)) {
        editor.putString(EMAIL_PREFERENCE_KEY, email);
        appboyUser.setEmail(email);
      }
      if (!StringUtils.isNullOrBlank(avatarImageUrl)) {
        editor.putString(AVATAR_PREFERENCE_KEY, avatarImageUrl);
        appboyUser.setAvatarImageUrl(avatarImageUrl);
      }
      if (isBirthdaySet) {
        editor.putString(BIRTHDAY_PREFERENCE_KEY, getBirthday());
        appboyUser.setDateOfBirth(mBirthYear, Month.getMonth(mBirthMonth), mBirthDay);
      }

      switch (genderId) {
        case GENDER_UNSPECIFIED_INDEX:
          appboyUser.setGender(null);
          break;
        case GENDER_MALE_INDEX:
          appboyUser.setGender(Gender.MALE);
          editor.putInt(GENDER_PREFERENCE_KEY, genderId);
          break;
        case GENDER_FEMALE_INDEX:
          appboyUser.setGender(Gender.FEMALE);
          editor.putInt(GENDER_PREFERENCE_KEY, genderId);
          break;
        default:
          Log.w(TAG, "Error parsing gender from user preferences.");
      }
      editor.apply();

      // Flushing manually is not recommended in almost all production situations as
      // Appboy automatically flushes data to its servers periodically.  This call
      // is solely for testing purposes.
      if (mRequestFlush.isChecked()) {
        Appboy.getInstance(getContext()).requestImmediateDataFlush();
      }
    }
  }

  private String getBirthday() {
    return Integer.toString(mBirthMonth + 1) + "/" + mBirthDay + "/" + mBirthYear;
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