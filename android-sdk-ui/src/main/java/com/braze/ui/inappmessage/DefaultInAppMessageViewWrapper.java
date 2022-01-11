package com.braze.ui.inappmessage;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.enums.inappmessage.DismissType;
import com.braze.enums.inappmessage.SlideFrom;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.IInAppMessageImmersive;
import com.braze.models.inappmessage.InAppMessageSlideup;
import com.braze.models.inappmessage.MessageButton;
import com.braze.support.BrazeLogger;
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;
import com.braze.ui.inappmessage.listeners.SwipeDismissTouchListener;
import com.braze.ui.inappmessage.listeners.TouchAwareSwipeDismissTouchListener;
import com.braze.ui.inappmessage.views.IInAppMessageImmersiveView;
import com.braze.ui.inappmessage.views.IInAppMessageView;
import com.braze.ui.inappmessage.views.InAppMessageHtmlBaseView;
import com.braze.ui.support.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class DefaultInAppMessageViewWrapper implements IInAppMessageViewWrapper {
  private static final String TAG = BrazeLogger.getBrazeLogTag(DefaultInAppMessageViewWrapper.class);

  protected final View mInAppMessageView;
  protected final IInAppMessage mInAppMessage;
  protected final IInAppMessageViewLifecycleListener mInAppMessageViewLifecycleListener;
  protected final Animation mOpeningAnimation;
  protected final Animation mClosingAnimation;
  protected final BrazeConfigurationProvider mConfigurationProvider;
  protected final InAppMessageCloser mInAppMessageCloser;
  protected boolean mIsAnimatingClose;
  protected Runnable mDismissRunnable;
  protected final View mClickableInAppMessageView;
  protected View mCloseButton;
  protected List<View> mButtonViews;
  /**
   * The {@link View} that previously held focus before a message is displayed as
   * given via {@link Activity#getCurrentFocus()}.
   */
  @Nullable
  protected View mPreviouslyFocusedView = null;
  /**
   * A mapping of the view accessibility flags of views before overriding them.
   * Used in conjunction with {@link com.braze.configuration.BrazeConfig.Builder#setIsInAppMessageAccessibilityExclusiveModeEnabled(boolean)}
   */
  @NonNull
  protected Map<Integer, Integer> mViewAccessibilityFlagMap = new HashMap<>();
  /**
   * The {@link ViewGroup} parent of the in-app message.
   */
  private ViewGroup mContentViewGroupParentLayout;

  /**
   * Constructor for base and slideup view wrappers. Adds click listeners to the in-app message view and
   * adds swipe functionality to slideup in-app messages.
   *
   * @param inAppMessageView                  In-app message top level view.
   * @param inAppMessage                      In-app message model.
   * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
   * @param configurationProvider       Configuration provider.
   * @param clickableInAppMessageView         View for which click actions apply.
   */
  public DefaultInAppMessageViewWrapper(View inAppMessageView,
                                        IInAppMessage inAppMessage,
                                        IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener,
                                        BrazeConfigurationProvider configurationProvider,
                                        Animation openingAnimation,
                                        Animation closingAnimation,
                                        View clickableInAppMessageView) {
    mInAppMessageView = inAppMessageView;
    mInAppMessage = inAppMessage;
    mInAppMessageViewLifecycleListener = inAppMessageViewLifecycleListener;
    mConfigurationProvider = configurationProvider;
    mOpeningAnimation = openingAnimation;
    mClosingAnimation = closingAnimation;
    mIsAnimatingClose = false;
    if (clickableInAppMessageView != null) {
      mClickableInAppMessageView = clickableInAppMessageView;
    } else {
      mClickableInAppMessageView = mInAppMessageView;
    }

    // Only slideup in-app messages can be swiped.
    if (mInAppMessage instanceof InAppMessageSlideup) {
      // Adds the swipe listener to the in-app message View. All slideup in-app messages should be dismissible via a swipe
      // (even auto close slideup in-app messages).
      SwipeDismissTouchListener.DismissCallbacks dismissCallbacks = createDismissCallbacks();
      TouchAwareSwipeDismissTouchListener touchAwareSwipeListener = new TouchAwareSwipeDismissTouchListener(inAppMessageView, dismissCallbacks);
      // We set a custom touch listener that cancel the auto close runnable when touched and adds
      // a new runnable when the touch ends.
      touchAwareSwipeListener.setTouchListener(createTouchAwareListener());
      mClickableInAppMessageView.setOnTouchListener(touchAwareSwipeListener);
    }

    mClickableInAppMessageView.setOnClickListener(createClickListener());
    mInAppMessageCloser = new InAppMessageCloser(this);
  }

  /**
   * Constructor for immersive in-app message view wrappers. Adds listeners to an optional close button and
   * message button views.
   *
   * @param inAppMessageView                  In-app message top level view.
   * @param inAppMessage                      In-app message model.
   * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
   * @param configurationProvider       Configuration provider.
   * @param buttonViews                       List of views corresponding to MessageButton objects stored in the in-app message model object.
   *                                          These views should map one to one with the MessageButton objects.
   * @param closeButton                       The {@link View} responsible for closing the in-app message.
   */
  public DefaultInAppMessageViewWrapper(View inAppMessageView,
                                        IInAppMessage inAppMessage,
                                        IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener,
                                        BrazeConfigurationProvider configurationProvider,
                                        Animation openingAnimation,
                                        Animation closingAnimation,
                                        View clickableInAppMessageView,
                                        List<View> buttonViews,
                                        View closeButton) {
    this(inAppMessageView,
        inAppMessage,
        inAppMessageViewLifecycleListener,
        configurationProvider,
        openingAnimation,
        closingAnimation,
        clickableInAppMessageView);

    // Set close button click listener
    if (closeButton != null) {
      mCloseButton = closeButton;
      mCloseButton.setOnClickListener(createCloseInAppMessageClickListener());
    }

    // Set button click listeners
    if (buttonViews != null) {
      mButtonViews = buttonViews;
      for (View button : mButtonViews) {
        button.setOnClickListener(createButtonClickListener());
      }
    }
  }

  @Override
  public void open(final @NonNull Activity activity) {
    BrazeLogger.v(TAG, "Opening in-app message view wrapper");
    // Retrieve the ViewGroup which will display the in-app message
    final ViewGroup parentViewGroup = getParentViewGroup(activity);
    final int parentViewGroupHeight = parentViewGroup.getHeight();

    if (mConfigurationProvider.isInAppMessageAccessibilityExclusiveModeEnabled()) {
      mContentViewGroupParentLayout = parentViewGroup;
      mViewAccessibilityFlagMap.clear();
      setAllViewGroupChildrenAsNonAccessibilityImportant(mContentViewGroupParentLayout, mViewAccessibilityFlagMap);
    }

    mPreviouslyFocusedView = activity.getCurrentFocus();

    // If the parent ViewGroup's height is 0, that implies it hasn't been drawn yet. We add a
    // ViewTreeObserver to wait until its drawn so we can get a proper measurement.
    if (parentViewGroupHeight == 0) {
      parentViewGroup.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
          parentViewGroup.removeOnLayoutChangeListener(this);
          BrazeLogger.d(TAG, "Detected (bottom - top) of " + (bottom - top) + " in OnLayoutChangeListener");
          parentViewGroup.removeView(mInAppMessageView);
          addInAppMessageViewToViewGroup(parentViewGroup, mInAppMessage, mInAppMessageView, mInAppMessageViewLifecycleListener);
        }
      });

    } else {
      BrazeLogger.d(TAG, "Detected root view height of " + parentViewGroupHeight);
      addInAppMessageViewToViewGroup(parentViewGroup, mInAppMessage, mInAppMessageView, mInAppMessageViewLifecycleListener);
    }
  }

  @Override
  public void close() {
    if (mConfigurationProvider.isInAppMessageAccessibilityExclusiveModeEnabled()) {
      resetAllViewGroupChildrenToPreviousAccessibilityFlagOrAuto(mContentViewGroupParentLayout, mViewAccessibilityFlagMap);
    }
    mInAppMessageView.removeCallbacks(mDismissRunnable);
    mInAppMessageViewLifecycleListener.beforeClosed(mInAppMessageView, mInAppMessage);
    if (mInAppMessage.getAnimateOut()) {
      mIsAnimatingClose = true;
      setAndStartAnimation(false);
    } else {
      closeInAppMessageView();
    }
  }

  @Override
  public View getInAppMessageView() {
    return mInAppMessageView;
  }

  @Override
  public IInAppMessage getInAppMessage() {
    return mInAppMessage;
  }

  @Override
  public boolean getIsAnimatingClose() {
    return mIsAnimatingClose;
  }

  /**
   * Gets the {@link ViewGroup} which will display the in-app message. Note that
   * if this implementation is overridden, then
   * {@link DefaultInAppMessageViewWrapper#getLayoutParams(IInAppMessage)} should
   * also most likely be overridden to match the {@link ViewGroup} subclass
   * returned here.
   */
  @NonNull
  protected ViewGroup getParentViewGroup(@NonNull Activity activity) {
    // The android.R.id.content {@link FrameLayout} contains the
    // {@link Activity}'s top-level layout as its first child.
    return activity.getWindow().getDecorView().findViewById(android.R.id.content);
  }

  /**
   * Creates the {@link ViewGroup.LayoutParams} used for adding the
   * {@link IInAppMessageView} to the {@link ViewGroup} returned by
   * {@link DefaultInAppMessageViewWrapper#getParentViewGroup(Activity)}.
   *
   * Note that the exact subclass of {@link ViewGroup.LayoutParams} should
   * match that of the {@link ViewGroup} returned by
   * {@link DefaultInAppMessageViewWrapper#getParentViewGroup(Activity)}.
   */
  @NonNull
  protected ViewGroup.LayoutParams getLayoutParams(IInAppMessage inAppMessage) {
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    if (inAppMessage instanceof InAppMessageSlideup) {
      InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
      layoutParams.gravity = inAppMessageSlideup.getSlideFrom() == SlideFrom.TOP ? Gravity.TOP : Gravity.BOTTOM;
    }
    return layoutParams;
  }

  /**
   * Adds the {@link IInAppMessageView} to the parent {@link ViewGroup}. Also
   * calls {@link IInAppMessageViewLifecycleListener#beforeOpened(View, IInAppMessage)} and
   * {@link IInAppMessageViewLifecycleListener#afterOpened(View, IInAppMessage)}.
   */
  protected void addInAppMessageViewToViewGroup(ViewGroup parentViewGroup,
                                                IInAppMessage inAppMessage,
                                                final View inAppMessageView,
                                                IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener) {
    inAppMessageViewLifecycleListener.beforeOpened(inAppMessageView, inAppMessage);
    BrazeLogger.d(TAG, "Adding In-app message view to parent view group.");
    parentViewGroup.addView(inAppMessageView, getLayoutParams(inAppMessage));

    if (inAppMessageView instanceof IInAppMessageView) {
      ViewCompat.requestApplyInsets(parentViewGroup);
      ViewCompat.setOnApplyWindowInsetsListener(parentViewGroup, (view, insets) -> {
        if (insets == null) {
          // No margin fixing can be done with a null window inset
          return insets;
        }

        final IInAppMessageView castInAppMessageView = (IInAppMessageView) inAppMessageView;
        if (!castInAppMessageView.hasAppliedWindowInsets()) {
          BrazeLogger.v(TAG, "Calling applyWindowInsets on in-app message view.");
          castInAppMessageView.applyWindowInsets(insets);
        } else {
          BrazeLogger.d(TAG, "Not reapplying window insets to in-app message view.");
        }
        return insets;
      });
    }

    if (inAppMessage.getAnimateIn()) {
      BrazeLogger.d(TAG, "In-app message view will animate into the visible area.");
      setAndStartAnimation(true);
      // The afterOpened lifecycle method gets called when the opening animation ends.
    } else {
      BrazeLogger.d(TAG, "In-app message view will be placed instantly into the visible area.");
      // There is no opening animation, so we call the afterOpened lifecycle method immediately.
      if (inAppMessage.getDismissType() == DismissType.AUTO_DISMISS) {
        addDismissRunnable();
      }
      finalizeViewBeforeDisplay(inAppMessage, inAppMessageView, inAppMessageViewLifecycleListener);
    }
  }

  /**
   * Calls {@link DefaultInAppMessageViewWrapper#announceForAccessibilityIfNecessary(String)} with a
   * preset fallback message.
   */
  protected void announceForAccessibilityIfNecessary() {
    announceForAccessibilityIfNecessary("In app message displayed.");
  }

  /**
   * Calls {@link View#announceForAccessibility(CharSequence)} with the {@link IInAppMessage#getMessage()}
   * if the {@link IInAppMessageView} is {@link IInAppMessageImmersiveView} or the fallback message
   * {@link IInAppMessageView} is a {@link InAppMessageHtmlBaseView}.
   */
  protected void announceForAccessibilityIfNecessary(String fallbackAccessibilityMessage) {
    if (mInAppMessageView instanceof IInAppMessageImmersiveView) {
      final String message = mInAppMessage.getMessage();
      if (mInAppMessage instanceof IInAppMessageImmersive) {
        // Announce the header and message together with a brief pause between them
        final String header = ((IInAppMessageImmersive) mInAppMessage).getHeader();
        mInAppMessageView.announceForAccessibility(header + " . " + message);
      } else {
        mInAppMessageView.announceForAccessibility(message);
      }
    } else if (mInAppMessageView instanceof InAppMessageHtmlBaseView) {
      mInAppMessageView.announceForAccessibility(fallbackAccessibilityMessage);
    }
  }

  /**
   * Creates a {@link View.OnClickListener} that calls
   * {@link IInAppMessageViewLifecycleListener#onClicked(InAppMessageCloser, View, IInAppMessage)}.
   *
   * {@link IInAppMessageViewLifecycleListener#onClicked(InAppMessageCloser, View, IInAppMessage)} is called and
   * can be used to turn off the close animation. Full and modal in-app messages can
   * only be clicked directly when they do not contain buttons.
   * Slideup in-app messages are always clickable.
   */
  protected View.OnClickListener createClickListener() {
    return view -> {
      if (mInAppMessage instanceof IInAppMessageImmersive) {
        IInAppMessageImmersive inAppMessageImmersive = (IInAppMessageImmersive) mInAppMessage;
        if (inAppMessageImmersive.getMessageButtons().isEmpty()) {
          mInAppMessageViewLifecycleListener.onClicked(mInAppMessageCloser, mInAppMessageView, mInAppMessage);
        }
      } else {
        mInAppMessageViewLifecycleListener.onClicked(mInAppMessageCloser, mInAppMessageView, mInAppMessage);
      }
    };
  }

  /**
   * @return A click listener that calls {@link IInAppMessageViewLifecycleListener#onButtonClicked(InAppMessageCloser, MessageButton, IInAppMessageImmersive)}
   * if the clicked {@link View#getId()} matches that of a {@link MessageButton}'s {@link View}.
   */
  protected View.OnClickListener createButtonClickListener() {
    return view -> {
      // The onClicked lifecycle method is called and it can be used to turn off the close animation.
      IInAppMessageImmersive inAppMessageImmersive = (IInAppMessageImmersive) mInAppMessage;
      if (inAppMessageImmersive.getMessageButtons().isEmpty()) {
        BrazeLogger.d(TAG, "Cannot create button click listener since this in-app message does not have message buttons.");
        return;
      }
      for (int i = 0; i < mButtonViews.size(); i++) {
        if (view.getId() == mButtonViews.get(i).getId()) {
          MessageButton messageButton = inAppMessageImmersive.getMessageButtons().get(i);
          mInAppMessageViewLifecycleListener.onButtonClicked(mInAppMessageCloser, messageButton, inAppMessageImmersive);
          return;
        }
      }
    };
  }

  protected View.OnClickListener createCloseInAppMessageClickListener() {
    return view -> BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true);
  }

  protected void addDismissRunnable() {
    if (mDismissRunnable == null) {
      mDismissRunnable = () -> BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true);
      mInAppMessageView.postDelayed(mDismissRunnable, mInAppMessage.getDurationInMilliseconds());
    }
  }

  /**
   * Instantiates and executes the correct animation for the current in-app message. Slideup-type
   * messages slide in from the top or bottom of the view. Other in-app messages fade in
   * and out of view.
   *
   * @param opening
   */
  protected void setAndStartAnimation(boolean opening) {
    Animation animation;
    if (opening) {
      animation = mOpeningAnimation;
    } else {
      animation = mClosingAnimation;
    }
    animation.setAnimationListener(createAnimationListener(opening));
    mInAppMessageView.clearAnimation();
    mInAppMessageView.setAnimation(animation);
    animation.startNow();
    mInAppMessageView.invalidate();
  }

  /**
   * Closes the in-app message view.
   * In this order, the following actions are performed:
   * <ul>
   * <li> The view is removed from the parent. </li>
   * <li> Any WebViews are explicitly paused or frame execution finished in some way. </li>
   * <li> {@link IInAppMessageViewLifecycleListener#afterClosed(IInAppMessage)} is called. </li>
   * </ul>
   */
  protected void closeInAppMessageView() {
    BrazeLogger.d(TAG, "Closing in-app message view");
    ViewUtils.removeViewFromParent(mInAppMessageView);
    // In the case of HTML in-app messages, we need to make sure the
    // WebView stops once the in-app message is removed.
    if (mInAppMessageView instanceof InAppMessageHtmlBaseView) {
      final InAppMessageHtmlBaseView inAppMessageHtmlBaseView = (InAppMessageHtmlBaseView) mInAppMessageView;
      inAppMessageHtmlBaseView.finishWebViewDisplay();
    }

    // Return the focus before closing the message
    if (mPreviouslyFocusedView != null) {
      BrazeLogger.d(TAG, "Returning focus to view after closing message. View: " + mPreviouslyFocusedView);
      mPreviouslyFocusedView.requestFocus();
    }
    mInAppMessageViewLifecycleListener.afterClosed(mInAppMessage);
  }

  /**
   * Performs any last actions before calling
   * {@link IInAppMessageViewLifecycleListener#beforeOpened(View, IInAppMessage)}.
   */
  protected void finalizeViewBeforeDisplay(final IInAppMessage inAppMessage,
                                           final View inAppMessageView,
                                           final IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener) {
    if (ViewUtils.isDeviceNotInTouchMode(inAppMessageView)) {
      // Special behavior usual to TV environments

      // For views with defined directional
      // behavior, don't steal focus from them
      switch (inAppMessage.getMessageType()) {
        case MODAL:
        case FULL:
          break;
        default:
          ViewUtils.setFocusableInTouchModeAndRequestFocus(inAppMessageView);
      }
    } else {
      ViewUtils.setFocusableInTouchModeAndRequestFocus(inAppMessageView);
    }

    announceForAccessibilityIfNecessary();
    inAppMessageViewLifecycleListener.afterOpened(inAppMessageView, inAppMessage);
  }

  protected SwipeDismissTouchListener.DismissCallbacks createDismissCallbacks() {
    return new SwipeDismissTouchListener.DismissCallbacks() {
      @Override
      public boolean canDismiss(Object token) {
        return true;
      }

      @Override
      public void onDismiss(View view, Object token) {
        mInAppMessage.setAnimateOut(false);
        BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true);
      }
    };
  }

  protected TouchAwareSwipeDismissTouchListener.ITouchListener createTouchAwareListener() {
    return new TouchAwareSwipeDismissTouchListener.ITouchListener() {
      @Override
      public void onTouchStartedOrContinued() {
        mInAppMessageView.removeCallbacks(mDismissRunnable);
      }

      @Override
      public void onTouchEnded() {
        if (mInAppMessage.getDismissType() == DismissType.AUTO_DISMISS) {
          addDismissRunnable();
        }
      }
    };
  }

  protected Animation.AnimationListener createAnimationListener(boolean opening) {
    if (opening) {
      return new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        // This lifecycle callback has been observed to not be called during slideup animations
        // on occasion. Do not add any code that *MUST* be executed here.
        @Override
        public void onAnimationEnd(Animation animation) {
          if (mInAppMessage.getDismissType() == DismissType.AUTO_DISMISS) {
            addDismissRunnable();
          }
          BrazeLogger.d(TAG, "In-app message animated into view.");
          finalizeViewBeforeDisplay(mInAppMessage, mInAppMessageView, mInAppMessageViewLifecycleListener);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
      };
    } else {
      return new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
          mInAppMessageView.clearAnimation();
          mInAppMessageView.setVisibility(View.GONE);
          closeInAppMessageView();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
      };
    }
  }

  /**
   * Sets all {@link View} children of the {@link ViewGroup}
   * as {@link ViewCompat#IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS}.
   */
  protected static void setAllViewGroupChildrenAsNonAccessibilityImportant(ViewGroup viewGroup,
                                                                           Map<Integer, Integer> viewAccessibilityFlagMap) {
    if (viewGroup == null) {
      BrazeLogger.w(TAG, "In-app message ViewGroup was null. Not preparing in-app message accessibility for exclusive mode.");
      return;
    }
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);
      if (child != null) {
        viewAccessibilityFlagMap.put(child.getId(), child.getImportantForAccessibility());
        ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
      }
    }
  }

  /**
   * Sets all {@link View} children of the {@link ViewGroup} as their previously
   * mapped accessibility flag, or {@link ViewCompat#IMPORTANT_FOR_ACCESSIBILITY_AUTO} if
   * not found in the mapping.
   */
  protected static void resetAllViewGroupChildrenToPreviousAccessibilityFlagOrAuto(ViewGroup viewGroup,
                                                                                   Map<Integer, Integer> viewAccessibilityFlagMap) {
    if (viewGroup == null) {
      BrazeLogger.w(TAG, "In-app message ViewGroup was null. Not resetting in-app message accessibility for exclusive mode.");
      return;
    }
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);
      if (child != null) {
        final int id = child.getId();
        if (viewAccessibilityFlagMap.containsKey(id)) {
          ViewCompat.setImportantForAccessibility(child, viewAccessibilityFlagMap.get(id));
        } else {
          ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }
      }
    }
  }
}
