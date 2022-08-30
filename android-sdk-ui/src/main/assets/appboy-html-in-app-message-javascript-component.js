var brazeBridge = {
  logCustomEvent: function (name, properties) { brazeInternalBridge.logCustomEventWithJSON(name, JSON.stringify(properties)); },
  logPurchase: function (productId, price, currencyCode, quantity, purchaseProperties) {
    brazeInternalBridge.logPurchaseWithJSON(productId, price, currencyCode, quantity != null ? quantity : 1, JSON.stringify(purchaseProperties));
  },
  closeMessage: function () { window.location = 'appboy://close'; },
  requestImmediateDataFlush: function () { brazeInternalBridge.requestImmediateDataFlush(); },
  display: { showFeed: function () { window.open('appboy://feed'); } },
  logClick: function(buttonId) {
    if (buttonId != null) {
      brazeInternalBridge.logButtonClick(buttonId);
    } else {
      brazeInternalBridge.logClick();
    }
  },
  requestPushPermission: function () { brazeInternalBridge.requestPushPermission() },
  brazeBridgeUserObject: {
    setFirstName: function(firstName) { brazeInternalBridge.getUser().setFirstName(firstName); },
    setLastName: function(lastName) { brazeInternalBridge.getUser().setLastName(lastName); },
    setEmail: function(email) { brazeInternalBridge.getUser().setEmail(email); },
    setGender: function(gender) { brazeInternalBridge.getUser().setGender(gender); },
    setHomeCity: function(homeCity) { brazeInternalBridge.getUser().setHomeCity(homeCity); },
    setEmailNotificationSubscriptionType: function(subscriptionType) {
      brazeInternalBridge.getUser().setEmailNotificationSubscriptionType(subscriptionType);
    },
    setPushNotificationSubscriptionType: function(subscriptionType) {
      brazeInternalBridge.getUser().setPushNotificationSubscriptionType(subscriptionType);
    },
    addToCustomAttributeArray: function(key, value) { brazeInternalBridge.getUser().addToCustomAttributeArray(key, value); },
    removeFromCustomAttributeArray: function(key, value) { brazeInternalBridge.getUser().removeFromCustomAttributeArray(key, value); },
    incrementCustomUserAttribute: function(key) { brazeInternalBridge.getUser().incrementCustomUserAttribute(key); },
    setDateOfBirth: function(year, month, day) { brazeInternalBridge.getUser().setDateOfBirth(year, month, day); },
    setCountry: function(country) { brazeInternalBridge.getUser().setCountry(country); },
    setPhoneNumber: function(phone) { brazeInternalBridge.getUser().setPhoneNumber(phone); },
    setCustomUserAttribute: function(key, value) {
      var isArray = function(value) {
          if (Array.isArray) {
            return Array.isArray(value);
          }
          return Object.prototype.toString.call(value) === '[object Array]';
      };
      if (isArray(value)) {
        brazeInternalBridge.getUser().setCustomUserAttributeArray(key, JSON.stringify(value));
      } else {
        brazeInternalBridge.getUser().setCustomUserAttributeJSON(key, JSON.stringify({"value":value}));
      }
    },
    setLocationCustomUserAttribute: function(key, latitude, longitude) {
      console.log("setLocationCustomUserAttribute() is deprecated and only support on Android. Please use setCustomLocationAttribute() instead.");
      brazeInternalBridge.getUser().setCustomLocationAttribute(key, latitude, longitude);
    },
    setCustomLocationAttribute: function(key, latitude, longitude) { brazeInternalBridge.getUser().setCustomLocationAttribute(key, latitude, longitude); },
    setLanguage: function(language) { brazeInternalBridge.getUser().setLanguage(language); },
    addAlias: function(alias, label) { brazeInternalBridge.getUser().addAlias(alias, label); },
    addToSubscriptionGroup: function(subscriptionGroupId) { brazeInternalBridge.getUser().addToSubscriptionGroup(subscriptionGroupId); },
    removeFromSubscriptionGroup: function(subscriptionGroupId) { brazeInternalBridge.getUser().removeFromSubscriptionGroup(subscriptionGroupId); }
  },
  getUser: function() {
    return this.brazeBridgeUserObject;
  },
  changeUser: function(userId, sdkAuth = null) {
      brazeInternalBridge.changeUser(userId, sdkAuth);
  },
  web: {
    registerAppboyPushMessages: function(successCallback, deniedCallback) { console.log("This method is a no-op on Android."); },
    trackLocation: function() { console.log("This method is a no-op on Android."); },
  }
};
var appboyBridge = brazeBridge;
window.dispatchEvent(new Event("ab.BridgeReady"));
