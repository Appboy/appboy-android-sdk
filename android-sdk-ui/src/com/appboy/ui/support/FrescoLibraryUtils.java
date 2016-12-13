package com.appboy.ui.support;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Since Fresco is a provided dependency, we have to check for its existence at runtime. To safeguard
 * against a major api change, we check for the existence of at least the imported classes used from
 * the Fresco library used in the UI project.
 */
public class FrescoLibraryUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, FrescoLibraryUtils.class.getName());
  private static boolean sCanUseFresco = false;
  private static boolean sCanUseFrescoSet = false;
  private static final String FILE_SCHEME = "file";
  private static final String HTTP_SCHEME = "http";
  private static final String HTTPS_SCHEME = "https";

  private static final String[] USED_FRESCO_CLASSES = {
      "com.facebook.drawee.backends.pipeline.Fresco",
      "com.facebook.drawee.interfaces.DraweeController",
      "com.facebook.drawee.view.SimpleDraweeView",
      "com.facebook.drawee.backends.pipeline.Fresco",
      "com.facebook.drawee.controller.BaseControllerListener",
      "com.facebook.drawee.controller.ControllerListener",
      "com.facebook.imagepipeline.image.ImageInfo"
  };

  /**
   * Returns the configuration value for Fresco enabled status. If the setting is not present, defaults to
   * true.
   */
  private static boolean getIsFrescoEnabled(Context context) {
    AppboyConfigurationProvider appboyConfigurationProvider = new AppboyConfigurationProvider(context);
    return appboyConfigurationProvider.getIsFrescoLibraryUseEnabled();
  }

  /**
   * Checks for the existence of the Facebook Fresco Image Library for use in the UI code. Also checks
   * for the provided xml setting.
   *
   * @return true if the fresco library is on the path AND if use of the fresco library is allowed
   * in the Appboy configuration settings.
   */
  public static boolean canUseFresco(Context context) {
    if (sCanUseFrescoSet) {
      return sCanUseFresco;
    }

    context = context.getApplicationContext();
    boolean isFrescoEnabledFromXml = getIsFrescoEnabled(context);
    if (!isFrescoEnabledFromXml) {
      sCanUseFresco = false;
      sCanUseFrescoSet = true;
      return false;
    }
    try {
      // Check for a subset of classes used from the Fresco library.
      ClassLoader staticClassLoader = FrescoLibraryUtils.class.getClassLoader();
      sCanUseFresco = true;
      for (String classPath : USED_FRESCO_CLASSES) {
        if (Class.forName(classPath, false, staticClassLoader) == null) {
          // The class doesn't exist on the path
          sCanUseFresco = false;
          break;
        }
      }
    } catch (Exception e) {
      sCanUseFresco = false;
    } catch (NoClassDefFoundError ncd) {
      sCanUseFresco = false;
    } catch (Throwable t) {
      sCanUseFresco = false;
    }

    sCanUseFrescoSet = true;
    return sCanUseFresco;
  }

  public static void setDraweeControllerHelper(final SimpleDraweeView simpleDraweeView, final String imageUrl, final float aspectRatio, final boolean respectAspectRatio) {
    setDraweeControllerHelper(simpleDraweeView, imageUrl, aspectRatio, respectAspectRatio, null);
  }

  /**
   * Helper method for setting the controller on a simple Drawee View. By default, gif urls are set
   * to autoplay and tap to retry is on for all images.
   *
   * @param simpleDraweeView   the fresco SimpleDraweeView in which to display the image
   * @param imageUrl           the URL of the image resource
   * @param aspectRatio        the desired aspect ratio of the image
   * @param respectAspectRatio if true, the aspect ratio of the image will be set to that of the value of aspectRatio. If false, the aspect ratio
   *                           will be set to that of the downloaded image dimensions.
   * @param controllerListener the controllerListener to use, or null if the default should be used.
   */
  public static void setDraweeControllerHelper(final SimpleDraweeView simpleDraweeView,
                                               final String imageUrl, final float aspectRatio,
                                               final boolean respectAspectRatio,
                                               ControllerListener<ImageInfo> controllerListener) {
    if (StringUtils.isNullOrBlank(imageUrl)) {
      AppboyLogger.w(TAG, "The url set for the Drawee controller was null. Controller not set.");
      return;
    }

    if (simpleDraweeView == null) {
      AppboyLogger.w(TAG, "The SimpleDraweeView set for the Drawee controller was null. Controller not set.");
      return;
    }

    // Selectively cancel network loading based on the Appboy network state
    ImageRequest.RequestLevel requestLevel = Appboy.getOutboundNetworkRequestsOffline()
        ? ImageRequest.RequestLevel.DISK_CACHE : ImageRequest.RequestLevel.FULL_FETCH;
    AppboyLogger.d(TAG, "Setting Fresco image request level to: " + requestLevel);

    // Create a controller listener to listen for the dimensions of the image once set. Once
    // we get the dimensions, set the aspect ratio of the image based on respectAspectRatio.
    if (controllerListener == null) {
      controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
          if (imageInfo == null) {
            return;
          }

          final float imageAspectRatio;

          if (respectAspectRatio) {
            imageAspectRatio = aspectRatio;
          } else {
            // Get the image aspect ratio from the imageInfo
            imageAspectRatio = imageInfo.getWidth() / imageInfo.getHeight();
          }

          // Set this aspect ratio on the drawee itself on the UI thread
          simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
              simpleDraweeView.setAspectRatio(imageAspectRatio);
            }
          });
        }
      };
    }


    // If the Fresco singleton is shutdown prematurely via Fresco.shutdown() then the Fresco.newDraweeControllerBuilder()
    // will throw a NPE. We catch this below to safeguard against this gracefully.
    try {
      Uri uri = getFrescoUri(imageUrl);
      ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
          .setLowestPermittedRequestLevel(requestLevel)
          .build();
      DraweeController controller = Fresco.newDraweeControllerBuilder()
          .setUri(uri)
          .setAutoPlayAnimations(true)
          .setTapToRetryEnabled(true)
          .setControllerListener(controllerListener)
          .setImageRequest(request)
          .build();
      simpleDraweeView.setController(controller);
    } catch (NullPointerException e) {
      AppboyLogger.e(TAG, "Fresco controller builder could not be retrieved. Fresco most likely prematurely shutdown.", e);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Fresco controller builder could not be retrieved. Fresco most likely prematurely shutdown.", e);
    }
  }

  static Uri getFrescoUri(String uriString) {
    Uri uri = Uri.parse(uriString);
    if (StringUtils.isNullOrBlank(uri.getScheme())) {
      return Uri.parse(FILE_SCHEME + "://" + uriString);
    }
    return uri;
  }
}
