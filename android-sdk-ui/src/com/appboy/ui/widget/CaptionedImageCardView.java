package com.appboy.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.AppboyImageUtils;
import com.appboy.Constants;
import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.facebook.drawee.view.SimpleDraweeView;

public class CaptionedImageCardView extends BaseCardView<CaptionedImageCard> {
    private ImageView mImage;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDomain;
    private SimpleDraweeView mDrawee;
    private IAction mCardAction;
    private static final String TAG = String.format("%s.%s", Constants.APPBOY, CaptionedImageCardView.class.getName());

    // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
    // aspect ratio, then this value will be the aspect ratio of the card on render.
    private float mAspectRatio = 4f / 3f;


    public CaptionedImageCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
        AppboyImageUtils.setRoundingCorners(mDrawee, mContext, getRadius(), getRadius(), 0,0);
    }

    public CaptionedImageCardView(Context context) {
        super(context);
        init(null);
    }

    public void init(CaptionedImageCard card) {

        if (canUseFresco()) {
            mDrawee = (SimpleDraweeView) getProperViewFromInflatedStub(R.id.com_appboy_captioned_image_card_drawee_stub);
//      setRoundingCorners(context, radius);
        } else {
            mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_captioned_image_card_imageview_stub);
            mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImage.setAdjustViewBounds(true);
        }

        String titleTypeFace = getTitleTypeFaceReference();
        String messageTypeFace = getMessageTypeFaceReference();


        mTitle = (TextView) findViewById(R.id.com_appboy_captioned_image_title);
        mDescription = (TextView) findViewById(R.id.com_appboy_captioned_image_description);
        mDomain = (TextView) findViewById(R.id.com_appboy_captioned_image_card_domain);

        if (titleTypeFace != null && !"".equals(titleTypeFace)) {
            titleTypeFace = ensureTypeFaceSuffix(titleTypeFace);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), titleTypeFace);
            mTitle.setTypeface(font);
        }

        if (messageTypeFace != null && !"".equals(messageTypeFace)) {

            messageTypeFace = ensureTypeFaceSuffix(messageTypeFace);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), messageTypeFace);
            mDescription.setTypeface(font);
            mDomain.setTypeface(font);
        }

        if (card != null) {
            setCard(card);
        }

        safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
        backgroundCorners((LayerDrawable) getResources().getDrawable(R.drawable.com_appboy_card_background));
    }


    @Override
    protected int getLayoutResource() {
        return R.layout.com_appboy_captioned_image_card;
    }

    @Override
    public void onSetCard(final CaptionedImageCard card) {
        mTitle.setText(card.getTitle());
        mDescription.setText(card.getDescription());
        setOptionalTextView(mDomain, card.getDomain());
        mCardAction = ActionFactory.createUriAction(getContext(), card.getUrl());
        boolean respectAspectRatio = false;
        if (card.getAspectRatio() != 0f) {
            mAspectRatio = card.getAspectRatio();
            respectAspectRatio = true;
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardClick(mContext, card, mCardAction, TAG);
            }
        });

        if (canUseFresco()) {
            setSimpleDraweeToUrl(mDrawee, card.getImageUrl(), mAspectRatio, respectAspectRatio);
        } else {
            setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio, respectAspectRatio);
        }
    }


}
