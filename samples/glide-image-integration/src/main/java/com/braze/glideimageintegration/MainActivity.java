package com.braze.glideimageintegration;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.braze.Braze;
import com.braze.enums.inappmessage.CropType;
import com.braze.enums.inappmessage.DismissType;
import com.braze.enums.inappmessage.ImageStyle;
import com.braze.models.inappmessage.InAppMessageBase;
import com.braze.models.inappmessage.InAppMessageFull;
import com.braze.models.inappmessage.InAppMessageModal;
import com.braze.models.inappmessage.InAppMessageSlideup;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    // Create an in-app message as soon as the Activity starts
    InAppMessageFull inAppMessageFull = new InAppMessageFull();
    inAppMessageFull.setImageStyle(ImageStyle.GRAPHIC);
    inAppMessageFull.setRemoteImageUrl(getString(R.string.gif_3_url));
    inAppMessageFull.setHeader("Glide Sample App");
    inAppMessageFull.setMessage("It's Amazing");

    final Context context = this.getApplicationContext();
    findViewById(R.id.com_appboy_display_modal_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        InAppMessageModal inAppMessageModal = new InAppMessageModal();
        inAppMessageModal.setRemoteImageUrl(getString(R.string.gif_1_url));
        inAppMessageModal.setImageStyle(ImageStyle.GRAPHIC);
        inAppMessageModal.setCropType(CropType.CENTER_CROP);
        showInAppMessage(inAppMessageModal);
      }
    });
    findViewById(R.id.com_appboy_display_slideup_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        InAppMessageSlideup inAppMessageSlideup = new InAppMessageSlideup();
        inAppMessageSlideup.setRemoteImageUrl(getString(R.string.gif_2_url));
        inAppMessageSlideup.setMessage("This is a slideup with a GIF");
        showInAppMessage(inAppMessageSlideup);
      }
    });
    findViewById(R.id.com_appboy_flush_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Braze.getInstance(context).requestImmediateDataFlush();
      }
    });
  }

  private void showInAppMessage(InAppMessageBase inAppMessage) {
    inAppMessage.setDismissType(DismissType.MANUAL);
    BrazeInAppMessageManager.getInstance().addInAppMessage(inAppMessage);
  }
}
