package com.appboy.sample.logging;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.sample.R;
import com.appboy.sample.util.ButtonUtils;
import com.appboy.support.StringUtils;

import java.math.BigDecimal;

public class CustomPurchaseDialog extends CustomLogger {
  private static final String DEFAULT_CURRENCY_CODE = "USD";
  private static final String DEFAULT_PRICE = "10.0";
  private EditText mCustomPurchaseQuantity;
  private EditText mCustomPurchaseCurrencyCodeName;
  private EditText mCustomPurchasePrice;

  public CustomPurchaseDialog(Context context, AttributeSet attributeSet) {
    super(context, attributeSet, R.layout.custom_purchase);
  }

  @Override
  protected View onCreateDialogView() {
    View view = super.onCreateDialogView();
    mCustomPurchaseQuantity = (EditText) view.findViewById(R.id.purchase_qty);
    mCustomPurchaseCurrencyCodeName = (EditText) view.findViewById(R.id.custom_purchase_currency_code);
    mCustomPurchasePrice = (EditText) view.findViewById(R.id.custom_purchase_price_code);

    ButtonUtils.setUpPopulateButton(view, R.id.purchase_qty_button, mCustomPurchaseQuantity, "5");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_purchase_currency_code_button, mCustomPurchaseCurrencyCodeName, "JPY");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_purchase_price_button, mCustomPurchasePrice, "5.0");

    return view;
  }

  @Override
  protected boolean customLog(String name, AppboyProperties properties) {
    String currencyCode = mCustomPurchaseCurrencyCodeName.getText().toString();
    String quantity = mCustomPurchaseQuantity.getText().toString();
    String price = mCustomPurchasePrice.getText().toString();

    if (StringUtils.isNullOrBlank(currencyCode)) {
      currencyCode = DEFAULT_CURRENCY_CODE;
    }
    if (StringUtils.isNullOrBlank(price)) {
      price = DEFAULT_PRICE;
    }
    if (StringUtils.isNullOrBlank(quantity)) {
      return Appboy.getInstance(getContext()).logPurchase(name, currencyCode, new BigDecimal(price), properties);
    }
    return Appboy.getInstance(getContext()).logPurchase(name, currencyCode, new BigDecimal(price), Integer.parseInt(quantity), properties);
  }
}
