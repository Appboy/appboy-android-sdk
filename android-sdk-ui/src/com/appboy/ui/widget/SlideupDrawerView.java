/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appboy.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.TextView;
import com.appboy.Appboy;
import com.appboy.IAppboy;
import com.appboy.models.Slideup;
import com.appboy.ui.R;

/**
 * SlideupDrawerView is a view that hides content out of the screen and can expand to show its
 * contents by calling animateOpen(). The content of a SlideupDrawerView consists of a Slideup
 * and an actionable button. Use setSlideup(Slideup) to set the content of the slideup.
 *
 * The SlideupDrawerView is based off of the deprecated {@link android.widget.SlidingDrawer}
 *
 * The maximum height of the SlideupDrawerView can be configured by setting the 'maxHeight'
 * attribute in the layout resource file.
 *
 * This class is not thread-safe and should only be used from the UI thread.
 */
public class SlideupDrawerView extends ViewGroup {
  // Constraining the maximum height: http://chrisharrington1.wordpress.com/2012/02/06/android-modifying-the-slidingdrawer-widget-to-use-a-max-height/
  // This is a modified version of the SlideupDrawer class. It has been modified to limit the height of the drawer,
  // disables the ability to slide the drawer open (or to touch open the drawer), and hides the handle.
  private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
  private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
  private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
  private static final float MAXIMUM_ACCELERATION = 1000.0f;
  private static final int VELOCITY_UNITS = 1000;
  private static final int MSG_ANIMATE = 1000;
  private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

  private static final int EXPANDED_FULL_OPEN = -10001;
  private static final int COLLAPSED_FULL_CLOSED = -10002;

  private final Context mContext;
  private final int mHandleId;
  private final int mContentId;

  private View mHandle;
  private View mContent;
  private Button mSlideupArrow;
  private TextView mMessageTextView;
  private Slideup mSlideup;

  private final Rect mFrame = new Rect();
  private final Rect mInvalidate = new Rect();
  private boolean mTracking;
  private boolean mLocked;

  private VelocityTracker mVelocityTracker;

  private boolean mExpanded;
  private int mTopOffset;
  private int mHandleHeight;
  private int mHandleWidth;

  private OnSlideupDrawerOpenListener mOnSlideupDrawerOpenListener;
  private OnSlideupDrawerCloseListener mOnSlideupDrawerCloseListener;

  private final Handler mHandler = new SlidingHandler();
  private float mAnimatedAcceleration;
  private float mAnimatedVelocity;
  private float mAnimationPosition;
  private long mAnimationLastTime;
  private long mCurrentAnimationTime;
  private boolean mAnimating;
  private int mMaxHeight;

  private final int mMaximumTapVelocity;
  private final int mMaximumMinorVelocity;
  private final int mMaximumMajorVelocity;
  private final int mMaximumAcceleration;
  private final int mVelocityUnits;

  /**
   * Callback invoked when the drawer is opened.
   */
  public static interface OnSlideupDrawerOpenListener {
    /**
     * Invoked when the drawer becomes fully open.
     */
    public void onDrawerOpened();
  }

  /**
   * Callback invoked when the drawer is closed.
   */
  public static interface OnSlideupDrawerCloseListener {
    /**
     * Invoked when the drawer becomes fully closed.
     */
    public void onDrawerClosed();
  }

  /**
   * Creates a new SlidingDrawer from a specified set of attributes defined in XML.
   *
   * @param context The application's environment.
   * @param attrs The attributes defined in XML.
   */
  public SlideupDrawerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  /**
   * Creates a new SlidingDrawer from a specified set of attributes defined in XML.
   *
   * @param context The application's environment.
   * @param attrs The attributes defined in XML.
   * @param defStyle The style to apply to this widget.
   */
  public SlideupDrawerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideupDrawerView, defStyle, 0);
    int handleId = typedArray.getResourceId(R.styleable.SlideupDrawerView_handle, 0);
    int contentId = typedArray.getResourceId(R.styleable.SlideupDrawerView_content, 0);
    typedArray.recycle();

    if (handleId == 0) {
      throw new IllegalArgumentException("The handle attribute is required and must refer to a valid child.");
    } else if (contentId == 0) {
      throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
    } else if (handleId == contentId) {
      throw new IllegalArgumentException("The content and handle attributes must refer to different children.");
    }

    final float density = getResources().getDisplayMetrics().density;
    mMaximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);
    mMaximumMinorVelocity = (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
    mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
    mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);
    mVelocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);

    mHandleId = handleId;
    mContentId = contentId;
    setAlwaysDrawnWithCacheEnabled(false);
  }

  public Slideup getSlideup() {
    return mSlideup;
  }

  public void setSlideup(Slideup slideup) {
    mSlideup = slideup;
    mMessageTextView.setText(slideup.getMessage());
  }

  @Override
  protected void onFinishInflate() {
    mHandle = findViewById(mHandleId);
    mContent = findViewById(mContentId);

    if (mHandle == null) {
      throw new IllegalArgumentException("The handle attribute is must refer to an existing child.");
    } else if (mContent == null) {
      throw new IllegalArgumentException("The content attribute is must refer to an existing child.");
    }
    mContent.setVisibility(View.GONE);

    mContent.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Appboy.getInstance(mContext).logSlideupClicked();
        sendSlideupClickedBroadcast();
      }
    });
    mMessageTextView = (TextView) findViewById(R.id.com_appboy_slideup_drawer_message);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

    if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
      throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
    }

    measureChild(mContent, widthMeasureSpec, heightMeasureSpec);
    mMaxHeight = mContent.getMeasuredHeight();

    mTopOffset = heightSpecSize - mMaxHeight;

    final View handle = mHandle;
    measureChild(handle, widthMeasureSpec, heightMeasureSpec);

    int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
    mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

    setMeasuredDimension(widthSpecSize, heightSpecSize);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    final long drawingTime = getDrawingTime();
    final View handle = mHandle;

    drawChild(canvas, handle, drawingTime);

    if (mTracking || mAnimating) {
      final Bitmap cache = mContent.getDrawingCache();
      if (cache != null) {
        canvas.drawBitmap(cache, 0, handle.getBottom(), null);
      } else {
        canvas.save();
        canvas.translate(0, handle.getTop() - mTopOffset);
        drawChild(canvas, mContent, drawingTime);
        canvas.restore();
      }
    } else if (mExpanded) {
      drawChild(canvas, mContent, drawingTime);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (mTracking) {
      return;
    }

    final int width = r - l;
    final int height = b - t;

    final View handle = mHandle;

    int childWidth = handle.getMeasuredWidth();
    int childHeight = handle.getMeasuredHeight();

    int childLeft;
    int childTop;

    final View content = mContent;

    childLeft = (width - childWidth) / 2;
    childTop = mExpanded ? mTopOffset : height - childHeight;

    content.layout(0, mTopOffset + childHeight, content.getMeasuredWidth(),
      mTopOffset + childHeight + content.getMeasuredHeight());

    handle.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    mHandleHeight = handle.getHeight();
    mHandleWidth = handle.getWidth();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return false;
  }

  private void sendSlideupClickedBroadcast() {
    Context context = getContext();
    String sllideupClickedAction = context.getPackageName() + ".intent.APPBOY_SLIDEUP_CLICKED";
    Intent slideupClickedIntent = new Intent(sllideupClickedAction);
    context.sendBroadcast(slideupClickedIntent);
  }

  private void animateClose(int position) {
    prepareTracking(position);
    performFling(position, mMaximumAcceleration, true);
  }

  private void animateOpen(int position) {
    prepareTracking(position);
    performFling(position, -mMaximumAcceleration, true);
  }

  private void performFling(int position, float velocity, boolean always) {
    mAnimationPosition = position;
    mAnimatedVelocity = velocity;

    if (mExpanded) {
      if (always || (velocity > mMaximumMajorVelocity ||
        (position > mTopOffset + mHandleHeight && velocity > -mMaximumMajorVelocity))) {
        // We are expanded, but they didn't move sufficiently to cause
        // us to retract.  Animate back to the expanded position.
        mAnimatedAcceleration = mMaximumAcceleration;
        if (velocity < 0) {
          mAnimatedVelocity = 0;
        }
      } else {
        // We are expanded and are now going to animate away.
        mAnimatedAcceleration = -mMaximumAcceleration;
        if (velocity > 0) {
          mAnimatedVelocity = 0;
        }
      }
    } else {
      if (!always && (velocity > mMaximumMajorVelocity ||
        (position > getHeight() / 2 && velocity > -mMaximumMajorVelocity))) {
        // We are collapsed, and they moved enough to allow us to expand.
        mAnimatedAcceleration = mMaximumAcceleration;
        if (velocity < 0) {
          mAnimatedVelocity = 0;
        }
      } else {
        // We are collapsed, but they didn't move sufficiently to cause
        // us to retract.  Animate back to the collapsed position.
        mAnimatedAcceleration = -mMaximumAcceleration;
        if (velocity > 0) {
          mAnimatedVelocity = 0;
        }
      }
    }

    long now = SystemClock.uptimeMillis();
    mAnimationLastTime = now;
    mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
    mAnimating = true;
    mHandler.removeMessages(MSG_ANIMATE);
    mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
    stopTracking();
  }

  private void prepareTracking(int position) {
    mTracking = true;
    mVelocityTracker = VelocityTracker.obtain();
    boolean opening = !mExpanded;
    if (opening) {
      mAnimatedAcceleration = mMaximumAcceleration;
      mAnimatedVelocity = mMaximumMajorVelocity;
      mAnimationPosition =  getHeight() - mHandleHeight;
      moveHandle((int) mAnimationPosition);
      mAnimating = true;
      mHandler.removeMessages(MSG_ANIMATE);
      long now = SystemClock.uptimeMillis();
      mAnimationLastTime = now;
      mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
      mAnimating = true;
    } else {
      if (mAnimating) {
        mAnimating = false;
        mHandler.removeMessages(MSG_ANIMATE);
      }
      moveHandle(position);
    }
  }

  private void moveHandle(int position) {
    final View handle = mHandle;

    if (position == EXPANDED_FULL_OPEN) {
      handle.offsetTopAndBottom(mTopOffset - handle.getTop());
      invalidate();
    } else if (position == COLLAPSED_FULL_CLOSED) {
      handle.offsetTopAndBottom(getBottom() - getTop() -
        mHandleHeight - handle.getTop());
      invalidate();
    } else {
      final int top = handle.getTop();
      int deltaY = position - top;
      if (position < mTopOffset) {
        deltaY = mTopOffset - top;
      } else if (deltaY > getBottom() - getTop() - mHandleHeight - top) {
        deltaY = getBottom() - getTop() - mHandleHeight - top;
      }
      handle.offsetTopAndBottom(deltaY);

      final Rect frame = mFrame;
      final Rect region = mInvalidate;

      handle.getHitRect(frame);
      region.set(frame);

      region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
      region.union(0, frame.bottom - deltaY, getWidth(), frame.bottom - deltaY + mContent.getHeight());

      invalidate(region);
    }
  }

  private void prepareContent() {
    if (mAnimating) {
      return;
    }

    // Something changed in the content, we need to honor the layout request
    // before creating the cached bitmap
    final View content = mContent;
    if (content.isLayoutRequested()) {
      // Vertical
      final int childHeight = mHandleHeight;
      int height = getBottom() - getTop() - childHeight - mTopOffset;
      content.measure(MeasureSpec.makeMeasureSpec(getRight() - getLeft(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
      content.layout(0, mTopOffset + childHeight, content.getMeasuredWidth(),
        mTopOffset + childHeight + content.getMeasuredHeight());
    }
    // Try only once... we should really loop but it's not a big deal
    // if the draw was cancelled, it will only be temporary anyway
    content.getViewTreeObserver().dispatchOnPreDraw();
    buildDrawingCacheIfNotHardwareAccelerated(content);

    content.setVisibility(View.GONE);
  }

  @TargetApi(11)
  private void buildDrawingCacheIfNotHardwareAccelerated(View view) {
    if (android.os.Build.VERSION.SDK_INT < 11 || !view.isHardwareAccelerated()) {
      view.buildDrawingCache();
    }
  }

  private void stopTracking() {
    mHandle.setPressed(false);
    mTracking = false;

    if (mVelocityTracker != null) {
      mVelocityTracker.recycle();
      mVelocityTracker = null;
    }
  }

  private void doAnimation() {
    if (mAnimating) {
      incrementAnimation();
      if (mAnimationPosition >= getHeight() - 1) {
        mAnimating = false;
        closeDrawer();
      } else if (mAnimationPosition < mTopOffset) {
        mAnimating = false;
        openDrawer();
      } else {
        moveHandle((int) mAnimationPosition);
        mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
      }
    }
  }

  private void incrementAnimation() {
    long now = SystemClock.uptimeMillis();
    float t = (now - mAnimationLastTime) / 1000.0f;                   // ms -> s
    final float position = mAnimationPosition;
    final float v = mAnimatedVelocity;                                // px/s
    final float a = mAnimatedAcceleration;                            // px/s/s
    mAnimationPosition = position + (v * t) + (0.5f * a * t * t);     // px
    mAnimatedVelocity = v + (a * t);                                  // px/s
    mAnimationLastTime = now;                                         // ms
  }

  /**
   * Opens the drawer immediately.
   *
   * @see #close()
   * @see #animateOpen()
   */
  public void open() {
    openDrawer();
    invalidate();
    requestLayout();

    sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
  }

  /**
   * Closes the drawer immediately.
   *
   * @see #open()
   * @see #animateClose()
   */
  public void close() {
    closeDrawer();
    invalidate();
    requestLayout();
  }

  /**
   * Closes the drawer with an animation.
   *
   * @see #close()
   * @see #open()
   * @see #animateOpen()
   */
  public void animateClose() {
    prepareContent();
    animateClose(mHandle.getTop());
  }

  /**
   * Opens the drawer with an animation.
   *
   * @see #close()
   * @see #open()
   * @see #animateClose()
   */
  public void animateOpen() {
    prepareContent();
    animateOpen(mHandle.getTop());
    sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
  }

  @TargetApi(14)
  @Override
  public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setClassName(SlideupDrawerView.class.getName());
  }

  @TargetApi(14)
  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(SlideupDrawerView.class.getName());
  }

  private void closeDrawer() {
    moveHandle(COLLAPSED_FULL_CLOSED);
    mContent.setVisibility(View.GONE);
    mContent.destroyDrawingCache();

    if (!mExpanded) {
      return;
    }

    mExpanded = false;
    if (mOnSlideupDrawerCloseListener != null) {
      mOnSlideupDrawerCloseListener.onDrawerClosed();
    }
  }

  private void openDrawer() {
    moveHandle(EXPANDED_FULL_OPEN);
    mContent.setVisibility(View.VISIBLE);

    if (mExpanded) {
      return;
    }

    mExpanded = true;

    if (mOnSlideupDrawerOpenListener != null) {
      mOnSlideupDrawerOpenListener.onDrawerOpened();
    }
  }

  /**
   * Sets the listener that receives a notification when the drawer becomes open.
   *
   * @param onDrawerOpenListener The listener to be notified when the drawer is opened.
   */
  public void setOnDrawerOpenListener(OnSlideupDrawerOpenListener onDrawerOpenListener) {
    mOnSlideupDrawerOpenListener = onDrawerOpenListener;
  }

  /**
   * Sets the listener that receives a notification when the drawer becomes close.
   *
   * @param onDrawerCloseListener The listener to be notified when the drawer is closed.
   */
  public void setOnDrawerCloseListener(OnSlideupDrawerCloseListener onDrawerCloseListener) {
    mOnSlideupDrawerCloseListener = onDrawerCloseListener;
  }

  /**
   * Unlocks the SlidingDrawer so that touch events are processed.
   *
   * @see #lock()
   */
  public void unlock() {
    mLocked = false;
  }

  /**
   * Locks the SlidingDrawer so that touch events are ignores.
   *
   * @see #unlock()
   */
  public void lock() {
    mLocked = true;
  }

  /**
   * Indicates whether the drawer is currently fully opened.
   *
   * @return True if the drawer is opened, false otherwise.
   */
  public boolean isOpened() {
    return mExpanded;
  }

  /**
   * Indicates whether the drawer is scrolling or flinging.
   *
   * @return True if the drawer is scroller or flinging, false otherwise.
   */
  public boolean isMoving() {
    return mTracking || mAnimating;
  }

  private class SlidingHandler extends Handler {
    public void handleMessage(Message message) {
      switch (message.what) {
        case MSG_ANIMATE:
          doAnimation();
          break;
      }
    }
  }
}
