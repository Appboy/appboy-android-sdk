package com.appboy.sample.logging;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.sample.R;
import com.appboy.support.StringUtils;
import com.braze.support.BrazeLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PropertyManager implements AdapterView.OnItemSelectedListener {
  private static final String[] propertyTypes = {"integer", "double", "string", "boolean", "date", "long"};
  private final Map<String, Object> mProperties = new HashMap<>();
  private final List<String> mKeys = new ArrayList<>();
  private int selectedPropertyType;
  private Date lastDatePicked;
  private final LinearLayout mLinearLayout;
  private final EditText mPropertyKey;
  private final EditText mPropertyValue;
  private final Context mContext;

  public PropertyManager(Context context, LinearLayout linearLayout, EditText propertyKey, EditText propertyValue, Spinner propertyTypeSpinner, Button addPropertyButton) {
    mContext = context;
    mLinearLayout = linearLayout;
    mPropertyKey = propertyKey;
    mPropertyValue = propertyValue;
    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, propertyTypes);
    propertyTypeSpinner.setAdapter(adapter);
    propertyTypeSpinner.setOnItemSelectedListener(this);
    addPropertyButton.setOnClickListener(view -> {
      String key = mPropertyKey.getText().toString();
      Object value;
      switch (selectedPropertyType) {
        case 0:
          value = getIntegerProperty();
          break;
        case 1:
          value = getDoubleProperty();
          break;
        case 2:
          value = getStringProperty();
          break;
        case 3:
          value = getBooleanProperty();
          break;
        case 4:
          value = lastDatePicked;
          break;
        case 5:
          value = getLongProperty();
          break;
        default:
          value = null;
          break;
      }
      if (value != null) {
        mPropertyKey.setText("");
        mPropertyValue.setText("");
        addPropertyView(key, value);
      }
    });
  }

  public AppboyProperties getAppboyProperties() {
    AppboyProperties appboyProperties = new AppboyProperties();
    for (String key : mKeys) {
      Object value = mProperties.get(key);
      if (value instanceof Integer) {
        appboyProperties.addProperty(key, (int) value);
      } else if (value instanceof Double) {
        appboyProperties.addProperty(key, (double) value);
      } else if (value instanceof Boolean) {
        appboyProperties.addProperty(key, (boolean) value);
      } else if (value instanceof String) {
        appboyProperties.addProperty(key, (String) value);
      } else if (value instanceof Date) {
        appboyProperties.addProperty(key, (Date) value);
      } else if (value instanceof Long) {
        appboyProperties.addProperty(key, (long) value);
      } else {
        BrazeLogger.w(this.getClass().toString(), "invalid property type");
      }
    }
    return appboyProperties;
  }

  private void addPropertyView(String key, Object value) {
    mKeys.add(key);
    mProperties.put(key, value);
    View view = View.inflate(mContext, R.layout.property_list_item, null);
    TextView keyTextView = view.findViewById(R.id.key_text_view);
    TextView valTextView = view.findViewById(R.id.value_text_view);
    keyTextView.setText(key);
    if (value instanceof Date) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime((Date) value);
      valTextView.setText(String.format(Locale.getDefault(), "%d/%d/%d",
              calendar.get(Calendar.MONTH) + 1,
              calendar.get(Calendar.DAY_OF_MONTH),
              calendar.get(Calendar.YEAR)));
    } else {
      valTextView.setText(value.toString());
    }
    mLinearLayout.addView(view);
  }

  private Object getIntegerProperty() {
    try {
      return Integer.parseInt(mPropertyValue.getText().toString());
    } catch (NumberFormatException nfe) {
      Toast.makeText(mContext, "Make sure the value is an integer", Toast.LENGTH_LONG).show();
      return null;
    }
  }

  private Object getLongProperty() {
    try {
      return Long.parseLong(mPropertyValue.getText().toString());
    } catch (NumberFormatException nfe) {
      Toast.makeText(mContext, "Make sure the value is a long", Toast.LENGTH_LONG).show();
      return null;
    }
  }

  private Object getDoubleProperty() {
    try {
      return Double.parseDouble(mPropertyValue.getText().toString());
    } catch (NumberFormatException nfe) {
      Toast.makeText(mContext, "Make sure the value is a double", Toast.LENGTH_LONG).show();
      return null;
    }
  }

  private Object getStringProperty() {
    String value = mPropertyValue.getText().toString();
    if (StringUtils.isNullOrBlank(value)) {
      Toast.makeText(mContext, "value should not be blank", Toast.LENGTH_LONG).show();
      return null;
    } else {
      return value;
    }
  }

  private Object getBooleanProperty() {
    String value = mPropertyValue.getText().toString();
    if (value.equals("true") || value.equals("True")) {
      return true;
    } else if (value.equals("false") || value.equals("False")) {
      return false;
    }
    Toast.makeText(mContext, "boolean should either be true or false", Toast.LENGTH_LONG).show();
    return null;
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    selectedPropertyType = position;
    List<String> properties = Arrays.asList(propertyTypes);
    if (selectedPropertyType == properties.indexOf("date")) {
      Calendar calendar = Calendar.getInstance();
      final DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, (view14, year, monthOfYear, dayOfMonth) -> {
        mPropertyValue.setText(String.format(Locale.getDefault(), "%d/%d/%d", monthOfYear + 1, dayOfMonth, year));
        Calendar pickedCalendar = Calendar.getInstance();
        pickedCalendar.set(year, monthOfYear, dayOfMonth);
        lastDatePicked = pickedCalendar.getTime();
      }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

      mPropertyValue.setOnClickListener(view13 -> {
        if (!datePickerDialog.isShowing()) {
          datePickerDialog.show();
        }
      });

      mPropertyValue.setOnTouchListener((view12, event) -> {
        mPropertyValue.performClick();
        return true;
      });

      mPropertyValue.setOnKeyListener((view1, keyCode, event) -> {
        mPropertyValue.performClick();
        return true;
      });
    } else {
      mPropertyValue.setOnClickListener(null);
      mPropertyValue.setOnTouchListener(null);
      mPropertyValue.setOnKeyListener(null);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }
}
