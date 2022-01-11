package com.appboy.sample.logging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.sample.R;
import com.appboy.sample.util.ButtonUtils;
import com.braze.Braze;
import com.braze.models.outgoing.BrazeProperties;
import com.braze.support.StringUtils;

import java.math.BigDecimal;

public class CustomPurchaseDialog extends CustomLogger {
  private static final String DEFAULT_CURRENCY_CODE = "USD";
  private static final String DEFAULT_PRICE = "10.0";
  private EditText mCustomPurchaseQuantity;
  private EditText mCustomPurchaseCurrencyCodeName;
  private EditText mCustomPurchasePrice;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.custom_purchase, container, false);
    mCustomPurchaseQuantity = view.findViewById(R.id.purchase_qty);
    mCustomPurchaseCurrencyCodeName = view.findViewById(R.id.custom_purchase_currency_code);
    mCustomPurchasePrice = view.findViewById(R.id.custom_purchase_price_code);

    ButtonUtils.setUpPopulateButton(view, R.id.purchase_qty_button, mCustomPurchaseQuantity, "5");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_purchase_currency_code_button, mCustomPurchaseCurrencyCodeName, "JPY");
    ButtonUtils.setUpPopulateButton(view, R.id.custom_purchase_price_button, mCustomPurchasePrice, "5.0");
    return view;
  }

  @Override
  protected void customLog(String name, BrazeProperties properties) {
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
      Braze.getInstance(getContext()).logPurchase(name, currencyCode, new BigDecimal(price), properties);
      return;
    }
    Braze.getInstance(getContext()).logPurchase(name, currencyCode, new BigDecimal(price), Integer.parseInt(quantity), properties);
  }
}
