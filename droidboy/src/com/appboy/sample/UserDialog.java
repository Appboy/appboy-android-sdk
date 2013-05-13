package com.appboy.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.enums.Gender;
import com.appboy.ui.Constants;

public class UserDialog extends DialogPreference {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, DialogPreference.class.getName());

  private EditText mFirstName;
  private EditText mLastName;
  private EditText mEmail;
  private EditText mBio;
  private RadioGroup mGender;
  private EditText mFavoriteColor;

  public UserDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDialogLayoutResource(R.layout.user_preferences);
    setPersistent(false);
  }

  public UserDialog(Context context, AttributeSet attrs, int defStyle) {
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
    mFavoriteColor = (EditText) view.findViewById(R.id.favorite_color);
    return view;
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);

    SharedPreferences sharedPreferences = getSharedPreferences();
    mFirstName.setText(sharedPreferences.getString("user.firstname", null));
    mLastName.setText(sharedPreferences.getString("user.lastname", null));
    mEmail.setText(sharedPreferences.getString("user.email", null));
    mBio.setText(sharedPreferences.getString("user.bio", null));
    mGender.check(sharedPreferences.getInt("user.gender_resource_id", R.id.unspecified));
    mFavoriteColor.setText(sharedPreferences.getString("user.favorite_color", null));
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

      Log.d(TAG, String.format("genderId: %d, genderResourceId: %d", genderId, genderResourceId));
      String favoriteColor = mFavoriteColor.getText().toString();

      SharedPreferences.Editor editor = getEditor();
      editor.putString("user.firstname", firstName);
      editor.putString("user.lastname", lastName);
      editor.putString("user.email", email);
      editor.putString("user.bio", bio);
      editor.putInt("user.gender_resource_id", genderResourceId);
      editor.putString("user.favorite_color", favoriteColor);
      persist(editor);

      AppboyUser appboyUser = Appboy.getInstance(getContext()).getCurrentUser();
      appboyUser.setFirstName(firstName);
      appboyUser.setLastName(lastName);
      appboyUser.setEmail(email);
      appboyUser.setBio(bio);
      switch (genderId) {
        case 0:
          appboyUser.setGender(null);
          break;
        case 1:
          appboyUser.setGender(Gender.MALE);
          break;
        case 2:
          appboyUser.setGender(Gender.FEMALE);
          break;
        default:
          Log.w(TAG, "Error parsing gender from user preferences.");
      }
      appboyUser.setCustomUserAttribute("favorite_color", favoriteColor);
    }
  }

  private void persist(SharedPreferences.Editor editor) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
      editor.commit();
    } else {
      editor.apply();
    }
  }
}