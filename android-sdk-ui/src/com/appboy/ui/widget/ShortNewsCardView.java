package com.appboy.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.AppboyImageUtils;
import com.appboy.Constants;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.facebook.drawee.view.SimpleDraweeView;

public class ShortNewsCardView extends BaseCardView<ShortNewsCard> {
    private ImageView mImage;
    private SimpleDraweeView mDrawee;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDomain;
    private IAction mCardAction;
    private final float mAspectRatio = 1f;
    private static final String TAG = String.format("%s.%s", Constants.APPBOY, ShortNewsCardView.class.getName());


    public ShortNewsCardView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(null);
        AppboyImageUtils.setRoundingCorners(mDrawee, mContext, getRadius(), getRadius(), 0, 0);
    }

    public ShortNewsCardView(Context context) {
        super(context);
        init(null);
    }

    private void init(final ShortNewsCard card) {
        mDescription = (TextView) findViewById(R.id.com_appboy_short_news_card_description);
        mTitle = (TextView) findViewById(R.id.com_appboy_short_news_card_title);
        mDomain = (TextView) findViewById(R.id.com_appboy_short_news_card_domain);

        if (canUseFresco()) {
            mDrawee = (SimpleDraweeView) getProperViewFromInflatedStub(R.id.com_appboy_short_news_card_drawee_stub);
        } else {
            mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_short_news_card_imageview_stub);
        }

        setTypeFace();

        if (card != null) {
            setCard(card);
        }

        safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
        backgroundCorners(((LayerDrawable) getResources().getDrawable(R.drawable.com_appboy_card_background)));
    }

    private void setTypeFace() {
        String titleTypeFace = getTitleTypeFaceReference();
        String messageTypeFace = getMessageTypeFaceReference();
        if (!TextUtils.isEmpty(getTitleTypeFaceReference())) {
            titleTypeFace = ensureTypeFaceSuffix(titleTypeFace);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), titleTypeFace);
            mTitle.setTypeface(font);
        }

        if (!TextUtils.isEmpty(messageTypeFace)) {
            messageTypeFace = ensureTypeFaceSuffix(messageTypeFace);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), messageTypeFace);
            mDescription.setTypeface(font);
            mDomain.setTypeface(font);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.com_appboy_short_news_card;
    }

    @Override
    public void onSetCard(final ShortNewsCard card) {
        mDescription.setText(card.getDescription());
        setOptionalTextView(mTitle, card.getTitle());
        setOptionalTextView(mDomain, card.getDomain());
        mCardAction = ActionFactory.createUriAction(getContext(), card.getUrl());

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardClick(mContext, card, mCardAction, TAG);
            }
        });

        if (canUseFresco()) {
            setSimpleDraweeToUrl(mDrawee, card.getImageUrl(), mAspectRatio, true);
        } else {
            setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio);
        }
    }
}
