var appboyBridge = {
  logCustomEvent: function (name, properties) { appboyInternalBridge.logCustomEventWithJSON(name, JSON.stringify(properties)); },
  logPurchase: function (productId, price, currencyCode, quantity, purchaseProperties) {
    appboyInternalBridge.logPurchaseWithJSON(productId, price, currencyCode, quantity != null ? quantity : 1, JSON.stringify(purchaseProperties));
  },
  closeMessage: function () { window.location = 'appboy://close'; },
  requestImmediateDataFlush: function () { appboyInternalBridge.requestImmediateDataFlush() },
  display: { showFeed: function () {  window.open('appboy://feed'); } },
  appboyBridgeUserObject: {
    setFirstName: function(firstName) { appboyInternalBridge.getUser().setFirstName(firstName); },
    setLastName: function(lastName) { appboyInternalBridge.getUser().setLastName(lastName); },
    setEmail: function(email) { appboyInternalBridge.getUser().setEmail(email); },
    setGender: function(gender) { appboyInternalBridge.getUser().setGender(gender); },
    setHomeCity: function(homeCity) { appboyInternalBridge.getUser().setHomeCity(homeCity); },
    setEmailNotificationSubscriptionType: function(subscriptionType) {
      appboyInternalBridge.getUser().setEmailNotificationSubscriptionType(subscriptionType);
    },
    setPushNotificationSubscriptionType: function(subscriptionType) {
      appboyInternalBridge.getUser().setPushNotificationSubscriptionType(subscriptionType);
    },
    addToCustomAttributeArray: function(key, value) { appboyInternalBridge.getUser().addToCustomAttributeArray(key, value); },
    removeFromCustomAttributeArray: function(key, value) { appboyInternalBridge.getUser().removeFromCustomAttributeArray(key, value); },
    incrementCustomUserAttribute: function(key) { appboyInternalBridge.getUser().incrementCustomUserAttribute(key); },
    setDateOfBirth: function(year, month, day) { appboyInternalBridge.getUser().setDateOfBirth(year, month, day); },
    setCountry: function(country) { appboyInternalBridge.getUser().setCountry(country); },
    setPhoneNumber: function(phone) { appboyInternalBridge.getUser().setPhoneNumber(phone); },
    setCustomUserAttribute: function(key, value) {
      var isArray = function(value) {
          if (Array.isArray) {
            return Array.isArray(value);
          }
          return Object.prototype.toString.call(value) === '[object Array]';
      };
      if (isArray(value)) {
        appboyInternalBridge.getUser().setCustomUserAttributeArray(key, JSON.stringify(value));
      } else {
        appboyInternalBridge.getUser().setCustomUserAttributeJSON(key, JSON.stringify({"value":value}));
      }
    },
    setLocationCustomUserAttribute: function(key, latitude, longitude) { appboyInternalBridge.getUser().setLocationCustomUserAttribute(key, latitude, longitude); }
  },
  getUser : function() {
    return this.appboyBridgeUserObject;
  }
};
window.dispatchEvent(new Event("ab.BridgeReady"));
