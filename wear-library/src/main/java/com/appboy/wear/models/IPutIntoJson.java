package com.appboy.wear.models;

// Note: Anything returned from this should be a valid object to be .put() into JSONObject.put
// or JSONArray.put(). Thus, if you do something like create an IPutIntoJson<String> you should
// make sure that whatever is returned is not already encoded once as it'll get encoded again.
public interface IPutIntoJson<T> {
  T forJsonPut();
}
