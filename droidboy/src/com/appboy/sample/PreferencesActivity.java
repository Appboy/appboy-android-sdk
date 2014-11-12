package com.appboy.sample;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import com.android.vending.billing.utils.IabHelper;
import com.android.vending.billing.utils.IabResult;
import com.android.vending.billing.utils.Inventory;
import com.android.vending.billing.utils.Purchase;
import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.Constants;
import com.appboy.enums.SocialNetwork;
import com.appboy.sample.util.SharedPrefsUtil;
import com.appboy.ui.slideups.AppboySlideupManager;
import com.crittercism.app.Crittercism;
import java.math.BigDecimal;

public class PreferencesActivity extends PreferenceActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, PreferencesActivity.class.getName());
  private static final String SKU_ANDROID_TEST_PURCHASED = "android.test.purchased";
  private static final String SKU_ANDROID_TEST_CANCELED = "android.test.canceled";
  private static final String SKU_ANDROID_TEST_REFUNDED = "android.test.refunded";
  private static final String SKU_ANDROID_TEST_UNAVAILABLE = "android.test.item_unavailable";
  private static final int IN_APP_PURCHASE_ACTIVITY_REQUEST_CODE = 12345;
  private static final CustomSlideupViewFactory sCustomSlideupViewFactory = new CustomSlideupViewFactory();

  private IabHelper mHelper;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    Preference facebookSharePreference = findPreference("facebook_share");
    Preference twitterSharePreference = findPreference("twitter_share");
    Preference logPurchasePreference = findPreference("log_purchase");
    Preference dataFlushPreference = findPreference("data_flush");
    Preference requestSlideupPreference = findPreference("request_slideup");
    Preference customAttributeArraySetPreference = findPreference("custom_attribute_array_set");
    Preference customAttributeArrayAddPreference = findPreference("custom_attribute_array_add");
    Preference customAttributeArrayRemPreference = findPreference("custom_attribute_array_rem");
    Preference customAttributeArraySetEmptyPreference = findPreference("custom_attribute_array_empty");
    Preference customAttributeArraySetNullPreference = findPreference("custom_attribute_array_null");
    Preference aboutPreference = findPreference("about");
    Preference logPurchaseWithQuantityPreference = findPreference("log_purchase_with_quantity");

    aboutPreference.setSummary(String.format(getResources().getString(R.string.about_summary), com.appboy.Constants.APPBOY_SDK_VERSION));

    facebookSharePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).logShare(SocialNetwork.FACEBOOK);
        showToast(getString(R.string.facebook_share_toast));
        return true;
      }
    });
    twitterSharePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).logShare(SocialNetwork.TWITTER);
        showToast(getString(R.string.twitter_share_toast));
        return true;
      }
    });
    if (Constants.IS_AMAZON) {
      Appboy.getInstance(PreferencesActivity.this).logPurchase("product_id", 99);
      Toast.makeText(PreferencesActivity.this, "Thank you for your purchase", Toast.LENGTH_LONG).show();
    } else {
      iapGoogleSetup();
      logPurchasePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          mHelper.launchPurchaseFlow(PreferencesActivity.this, SKU_ANDROID_TEST_PURCHASED,
              IN_APP_PURCHASE_ACTIVITY_REQUEST_CODE, mPurchaseFinishedListener);
          return true;
        }
      });
    }
    dataFlushPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).requestImmediateDataFlush();
        showToast(getString(R.string.data_flush_toast));
        return true;
      }
    });
    requestSlideupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).requestSlideupRefresh();
        showToast(getString(R.string.requested_slideup_toast));
        return true;
      }
    });
    customAttributeArraySetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        String[] testSetArray = new String[] { "TestVal2", "TestVal2"};
        Toast.makeText(PreferencesActivity.this, "Set a Custom Attribute Array", Toast.LENGTH_LONG).show();
        AppboyUser appboyUser = Appboy.getInstance(PreferencesActivity.this).getCurrentUser();
        appboyUser.setCustomAttributeArray("custom_attribute_array_test", testSetArray);
        return true;
      }
    });
    customAttributeArrayAddPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        String testAddString = "CustomAttributeArrayTestValue";
        Toast.makeText(PreferencesActivity.this, "Added value to Custom Attribute Array", Toast.LENGTH_LONG).show();
        AppboyUser appboyUser = Appboy.getInstance(PreferencesActivity.this).getCurrentUser();
        appboyUser.addToCustomAttributeArray("custom_attribute_array_test", testAddString);
        return true;
      }
    });
    customAttributeArrayRemPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        String testRemString = "CustomAttributeArrayTestValue";
        Toast.makeText(PreferencesActivity.this, "Removed value from Custom Attribute Array", Toast.LENGTH_LONG).show();
        AppboyUser appboyUser = Appboy.getInstance(PreferencesActivity.this).getCurrentUser();
        appboyUser.removeFromCustomAttributeArray("custom_attribute_array_test", testRemString);
        return true;
      }
    });
    customAttributeArraySetEmptyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        String[] testEmptyArray = new String[] {};
        Toast.makeText(PreferencesActivity.this, "Set Empty Custom Attribute Array", Toast.LENGTH_LONG).show();
        AppboyUser appboyUser = Appboy.getInstance(PreferencesActivity.this).getCurrentUser();
        appboyUser.setCustomAttributeArray("custom_attribute_array_test", testEmptyArray);
        return true;
      }
    });
    customAttributeArraySetNullPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        String[] testNullArray = null;
        Toast.makeText(PreferencesActivity.this, "Set Null Custom Attribute Array", Toast.LENGTH_LONG).show();
        AppboyUser appboyUser = Appboy.getInstance(PreferencesActivity.this).getCurrentUser();
        appboyUser.setCustomAttributeArray("custom_attribute_array_test", testNullArray);
        return true;
      }
    });
    logPurchaseWithQuantityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        // Log a purchase of 3 items each with price of $1.00
        Appboy.getInstance(PreferencesActivity.this).logPurchase(SKU_ANDROID_TEST_PURCHASED, "USD", BigDecimal.ONE, 3);
        Toast.makeText(PreferencesActivity.this, getString(R.string.log_purchase_with_quantity_toast), Toast.LENGTH_LONG).show();
        return true;
      }
    });
  }

  void iapGoogleSetup() {
    /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE";

    // Create the helper, passing it our context and the public key to verify signatures with
    Log.d(TAG, "Creating IAB helper.");
    mHelper = new IabHelper(this, base64EncodedPublicKey);

    // enable debug logging (for a production application, you should set this to false).
    mHelper.enableDebugLogging(true);

    // Start setup. This is asynchronous and the specified listener
    // will be called once setup completes.
    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        Log.d(TAG, "In-app billing helper setup finished.");

        if (!result.isSuccess()) {
          Toast.makeText(PreferencesActivity.this, "Problem setting up in-app billing: " + result, Toast.LENGTH_LONG).show();
          return;
        }

        Log.d(TAG, "In-app billing helper setup successful. Querying inventory.");
        mHelper.queryInventoryAsync(mGotInventoryListener);
      }
    });
  }

  // Displays a toast to the user
  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Pass on the activity result to the helper for handling
    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
      // not handled, so handle it ourselves (here's where you'd
      // perform any handling of activity results not related to in-app
      // billing...
      super.onActivityResult(requestCode, resultCode, data);
    }
    else {
      Log.d(TAG, "onActivityResult handled by IABUtil.");
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session. You can now start logging custom events.
    Appboy.getInstance(this).openSession(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers the AppboySlideupManager for the current Activity. This Activity will now listen for
    // slideup messages from Appboy.
    AppboySlideupManager.getInstance().registerSlideupManager(this);
    Crittercism.leaveBreadcrumb(PreferencesActivity.class.getName());
  }

  @Override
  public void onPause() {
    super.onPause();
    // Unregisters the AppboySlideupManager.
    AppboySlideupManager.getInstance().unregisterSlideupManager(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "Destroying helper.");
    if (mHelper != null) {
      mHelper.dispose();
    }
    mHelper = null;
  }

  // Callback for when a purchase is finished
  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
      Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
      if (result.isFailure()) {
        Log.e(TAG, "Error purchasing: " + result);
        return;
      }
      if (!verifyDeveloperPayload(purchase)) {
        Log.d(TAG, "Error purchasing. Authenticity verification failed.");
        return;
      }

      Log.d(TAG, "Purchase successful.");
      Appboy.getInstance(PreferencesActivity.this).logPurchase("product_id", 99);
      Toast.makeText(PreferencesActivity.this, getString(R.string.log_purchase_toast), Toast.LENGTH_LONG).show();
    }
  };

  // Listener that's called when we finish querying the items and subscriptions we own
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
      Log.d(TAG, "Query inventory finished.");
      if (result.isFailure()) {
        Log.d(TAG, "Failed to query inventory: " + result);
        return;
      }

      Log.d(TAG, "Query inventory was successful.");

      /*
       * Check for items we own. Notice that for each purchase, we check
       * the developer payload to see if it's correct! See
       * verifyDeveloperPayload().
       */

      // Check for gas delivery -- if we own gas, we should fill up the tank immediately
      Purchase testPurchase = inventory.getPurchase(SKU_ANDROID_TEST_PURCHASED);
      if (testPurchase != null && verifyDeveloperPayload(testPurchase)) {
        Log.d(TAG, "Purchase: " + testPurchase);
        mHelper.consumeAsync(inventory.getPurchase(SKU_ANDROID_TEST_PURCHASED), mConsumeFinishedListener);
        return;
      }
    }
  };

  IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
    public void onConsumeFinished(Purchase purchase, IabResult result) {
      Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

      if (result.isSuccess() && SKU_ANDROID_TEST_PURCHASED.equals(purchase.getSku())) {
        Log.d(TAG, "Consumption successful. Provisioning.");
      }
    }
  };

  /** Verifies the developer payload of a purchase. */
  private boolean verifyDeveloperPayload(Purchase p) {
    String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

    return true;
  }
}
