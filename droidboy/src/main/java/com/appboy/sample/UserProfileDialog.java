package com.appboy.sample;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.sample.dialog.CustomDialogBase;
import com.appboy.sample.util.ButtonUtils;
import com.braze.Braze;
import com.braze.BrazeUser;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;

import java.util.Calendar;

public class UserProfileDialog extends CustomDialogBase implements View.OnClickListener {
  private static final String TAG = BrazeLogger.getBrazeLogTag(UserProfileDialog.class);
  private static final int GENDER_UNSPECIFIED_INDEX = 0;
  private static final int GENDER_MALE_INDEX = 1;
  private static final int GENDER_FEMALE_INDEX = 2;
  private static final int GENDER_OTHER_INDEX = 3;
  private static final int GENDER_UNKNOWN_INDEX = 4;
  private static final int GENDER_NOT_APPLICABLE_INDEX = 5;
  private static final int GENDER_PREFER_NOT_TO_SAY_INDEX = 6;

  private static final Calendar mCalendar = Calendar.getInstance();

  private static final String FIRST_NAME_PREFERENCE_KEY = "user.firstname";
  private static final String LAST_NAME_PREFERENCE_KEY = "user.lastname";
  private static final String LANGUAGE_PREFERENCE_KEY = "user.language";
  private static final String EMAIL_PREFERENCE_KEY = "user.email";
  private static final String GENDER_PREFERENCE_KEY = "user.gender_resource_id";
  private static final String BIRTHDAY_PREFERENCE_KEY = "user.birthday";

  private static final String SAMPLE_FIRST_NAME = "Jane";
  private static final String SAMPLE_LAST_NAME = "Doe";
  private static final String SAMPLE_LANGUAGE = "hi";
  private static final String SAMPLE_EMAIL = "jane@appboy.com";
  private static final int SAMPLE_GENDER = R.id.female;
  private static final String SAMPLE_BIRTHDAY = (mCalendar.get(Calendar.MONTH) + 1) + "/" + mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.YEAR);

  private EditText mFirstName;
  private EditText mLastName;
  private EditText mEmail;
  private RadioGroup mGender;
  private EditText mLanguage;
  private TextView mBirthday;

  private DatePickerDialog mDatePickerDialog;
  private int mBirthYear;
  private int mBirthMonth;
  private int mBirthDay;
  private boolean isBirthdaySet = false;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.user_preferences, container, false);
    mFirstName = view.findViewById(R.id.first_name);
    mLastName = view.findViewById(R.id.last_name);
    mEmail = view.findViewById(R.id.email);
    mGender = view.findViewById(R.id.gender);
    mLanguage = view.findViewById(R.id.language);
    mBirthday = view.findViewById(R.id.birthday);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    SharedPreferences sharedPreferences = getSharedPreferences();
    mFirstName.setText(sharedPreferences.getString(FIRST_NAME_PREFERENCE_KEY, null));
    mLastName.setText(sharedPreferences.getString(LAST_NAME_PREFERENCE_KEY, null));
    mEmail.setText(sharedPreferences.getString(EMAIL_PREFERENCE_KEY, null));
    mGender.check(parseGenderFromSharedPreferences());
    mLanguage.setText(sharedPreferences.getString(LANGUAGE_PREFERENCE_KEY, null));
    mBirthday.setText(sharedPreferences.getString(BIRTHDAY_PREFERENCE_KEY, null));

    ButtonUtils.setUpPopulateButton(view, R.id.first_name_button, mFirstName, getSharedPreferences().getString(FIRST_NAME_PREFERENCE_KEY, SAMPLE_FIRST_NAME));
    ButtonUtils.setUpPopulateButton(view, R.id.last_name_button, mLastName, getSharedPreferences().getString(LAST_NAME_PREFERENCE_KEY, SAMPLE_LAST_NAME));
    ButtonUtils.setUpPopulateButton(view, R.id.email_button, mEmail, getSharedPreferences().getString(EMAIL_PREFERENCE_KEY, SAMPLE_EMAIL));
    ButtonUtils.setUpPopulateButton(view, R.id.language_button, mLanguage, getSharedPreferences().getString(LANGUAGE_PREFERENCE_KEY, SAMPLE_LANGUAGE));

    final Button populateButton = view.findViewById(R.id.user_dialog_button_populate);
    final Button clearButton = view.findViewById(R.id.user_dialog_button_clear);
    final Button birthdayButton = view.findViewById(R.id.birthday_button);

    populateButton.setOnClickListener(this);
    clearButton.setOnClickListener(this);
    birthdayButton.setOnClickListener(this);

    mDatePickerDialog = new DatePickerDialog(getContext(), (view1, year, monthOfYear, dayOfMonth) -> {
      mBirthYear = year;
      mBirthMonth = monthOfYear;
      mBirthDay = dayOfMonth;
      mBirthday.setText(getBirthday());
      isBirthdaySet = true;
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
    if (mLanguage.getText().length() == 0) {
      mLanguage.setText(getSharedPreferences().getString(LANGUAGE_PREFERENCE_KEY, SAMPLE_LANGUAGE));
    }
    if (mEmail.getText().length() == 0) {
      mEmail.setText(getSharedPreferences().getString(EMAIL_PREFERENCE_KEY, SAMPLE_EMAIL));
    }
    if (mGender.getCheckedRadioButtonId() == R.id.unspecified) {
      mGender.check(SAMPLE_GENDER);
    }
    if (mBirthday.getText().length() == 0) {
      mBirthday.setText(getSharedPreferences().getString(BIRTHDAY_PREFERENCE_KEY, SAMPLE_BIRTHDAY));
      isBirthdaySet = true;
    }
  }

  @Override
  public void onExitButtonPressed(boolean isPositive) {
    String firstName = mFirstName.getText().toString();
    String lastName = mLastName.getText().toString();
    String email = mEmail.getText().toString();
    int genderResourceId = mGender.getCheckedRadioButtonId();
    View genderRadioButton = mGender.findViewById(genderResourceId);
    int genderId = mGender.indexOfChild(genderRadioButton);
    String language = mLanguage.getText().toString();

    BrazeUser brazeUser = Braze.getInstance(getContext()).getCurrentUser();
    SharedPreferences.Editor editor = getSharedPreferences().edit();
    if (!StringUtils.isNullOrBlank(firstName)) {
      brazeUser.setFirstName(firstName);
      editor.putString(FIRST_NAME_PREFERENCE_KEY, firstName);
    }
    if (!StringUtils.isNullOrBlank(lastName)) {
      brazeUser.setLastName(lastName);
      editor.putString(LAST_NAME_PREFERENCE_KEY, lastName);
    }
    if (!StringUtils.isNullOrBlank(language)) {
      brazeUser.setLanguage(language);
      editor.putString(LANGUAGE_PREFERENCE_KEY, language);
    }
    if (!StringUtils.isNullOrBlank(email)) {
      editor.putString(EMAIL_PREFERENCE_KEY, email);
      brazeUser.setEmail(email);
    }
    if (isBirthdaySet) {
      editor.putString(BIRTHDAY_PREFERENCE_KEY, getBirthday());
      brazeUser.setDateOfBirth(mBirthYear, Month.getMonth(mBirthMonth), mBirthDay);
    }

    switch (genderId) {
      case GENDER_UNSPECIFIED_INDEX:
        brazeUser.setGender(null);
        break;
      case GENDER_MALE_INDEX:
        brazeUser.setGender(Gender.MALE);
        editor.putInt(GENDER_PREFERENCE_KEY, genderId);
        break;
      case GENDER_FEMALE_INDEX:
        brazeUser.setGender(Gender.FEMALE);
        editor.putInt(GENDER_PREFERENCE_KEY, genderId);
        break;
      case GENDER_OTHER_INDEX:
        brazeUser.setGender(Gender.OTHER);
        editor.putInt(GENDER_PREFERENCE_KEY, genderId);
        break;
      case GENDER_UNKNOWN_INDEX:
        brazeUser.setGender(Gender.UNKNOWN);
        editor.putInt(GENDER_PREFERENCE_KEY, genderId);
        break;
      case GENDER_NOT_APPLICABLE_INDEX:
        brazeUser.setGender(Gender.NOT_APPLICABLE);
        editor.putInt(GENDER_PREFERENCE_KEY, genderId);
        break;
      case GENDER_PREFER_NOT_TO_SAY_INDEX:
        brazeUser.setGender(Gender.PREFER_NOT_TO_SAY);
        editor.putInt(GENDER_PREFERENCE_KEY, genderId);
        break;
      default:
        Log.w(TAG, "Error parsing gender from user preferences.");
    }
    editor.apply();

    // Flushing manually is not recommended in almost all production situations as
    // Braze automatically flushes data to its servers periodically. This call
    // is solely for testing purposes.
    if (isPositive) {
      Braze.getInstance(getContext()).requestImmediateDataFlush();
    }
    this.dismiss();
  }

  private String getBirthday() {
    return (mBirthMonth + 1) + "/" + mBirthDay + "/" + mBirthYear;
  }

  private int parseGenderFromSharedPreferences() {
    switch (getSharedPreferences().getInt(GENDER_PREFERENCE_KEY, GENDER_UNSPECIFIED_INDEX)) {
      case GENDER_UNSPECIFIED_INDEX:
        return R.id.unspecified;
      case GENDER_MALE_INDEX:
        return R.id.male;
      case GENDER_FEMALE_INDEX:
        return R.id.female;
      case GENDER_OTHER_INDEX:
        return R.id.other;
      case GENDER_UNKNOWN_INDEX:
        return R.id.unknown;
      case GENDER_NOT_APPLICABLE_INDEX:
        return R.id.not_applicable;
      case GENDER_PREFER_NOT_TO_SAY_INDEX:
        return R.id.prefer_not_to_say;
      default:
        Log.w(TAG, "Error parsing gender from shared preferences.");
        return R.id.unspecified;
    }
  }

  private SharedPreferences getSharedPreferences() {
    return this.getContext().getSharedPreferences(getString(R.string.shared_prefs_location), Context.MODE_PRIVATE);
  }
}
