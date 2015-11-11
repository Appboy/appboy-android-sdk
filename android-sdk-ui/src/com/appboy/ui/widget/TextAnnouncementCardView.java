package com.appboy.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.models.cards.TextAnnouncementCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;

public class TextAnnouncementCardView extends BaseCardView<TextAnnouncementCard> {
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDomain;
    private IAction mCardAction;
    private static final String TAG = String.format("%s.%s", Constants.APPBOY, TextAnnouncementCardView.class.getName());


    public TextAnnouncementCardView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    private void init(final TextAnnouncementCard card) {
        mTitle = (TextView) findViewById(R.id.com_appboy_text_announcement_card_title);
        mDescription = (TextView) findViewById(R.id.com_appboy_text_announcement_card_description);
        mDomain = (TextView) findViewById(R.id.com_appboy_text_announcement_card_domain);

        if (card != null) {
            setCard(card);
        }
        setTypeFace();
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


    public TextAnnouncementCardView(Context context) {
        super(context);
        init(null);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.com_appboy_text_announcement_card;
    }

    @Override
    public void onSetCard(final TextAnnouncementCard card) {
        mTitle.setText(card.getTitle());
        mDescription.setText(card.getDescription());
        setOptionalTextView(mDomain, card.getDomain());
        mCardAction = ActionFactory.createUriAction(getContext(), card.getUrl());

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardClick(mContext, card, mCardAction, TAG);
            }
        });
    }
}
