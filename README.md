[![Build Status](https://travis-ci.org/NYTimes/Register.svg?branch=master)](https://travis-ci.org/NYTimes/Register)

Register is an Android library for easier testing of Google Play billing

### The Problems:

+ In App Billing implementations on Android are  hard to get right
+ When money is involved developers sleep better having a way to test their functionality prior to release
+ Prior to an App being in Alpha within the Play Store we do not have an offical way to test payments

The New York Times Android team developed a fake implementation of Google Play Store's In-App Billing called Register
which runs as a companion app.  Similar to a mock web server, you can then point your app to use Register 
rather than the real Play Store Billing implementation.  Using register you'll be able to validate whether your purchasing flows work correctly.

![Register Sample](https://github.com/nytm/register/blob/master/images/registerCompanion.png?raw=true)



### Overview

Register is a library and companion app that allow proxying and manipulation of calls to Google in app billing. 
The premise is to reimplement the AIDL interfaces that are used by the real play store. 
From a clients perspective there is no difference in how you work with Google's In App Billing or Register's implementation.

### Using Register

**Step 0:** Register needs a configuration file that declares mock purchases and users that you will be testing against.  
Here's a  sample that we use at NYTimes, the format needs to be same as below when creating your own fake purchases
```json
{
	"skus": {
	    "register.sample.iap": {
			"itemType": "IN APP PURCHASE",
			"price" : "1.00",
			"title" : "Sample In App Purchase Item",
			"description" : "This is an in app purchase item for use with Register sample app",
			"package" : "com.nytimes.android.external.register"
	    },
		"register.sample.sub": {
			"itemType": "SUBSCRIPTION",
			"price" : "10.00",
			"title" : "Sample Subscription Item1",
			"description" : "This is a subscription item for use with Register sample app",
			"package" : "com.nytimes.android.external.register"
		}
	},
	"users": [
		"user1@register.nytimes.com",
		"user2@register.nytimes.com"
	]
}

```
**Step 1:** `adb push register.json /sdcard/` where `register.json` is a json file in the same format as above

**Step2:** install `RegisterCompanion` onto the phone that wants to mock the In App Billing, 
you can find the latest version in the [Release Tab](https://github.com/nytm/Register/releases/new)

**Step3:** add Register as a dependency to your client app 
```groovy 
compile 'com.nytimes.android.register:0.0.1'
```

**Step4:** Create a the test google services provider(or a real provider)

```java
 private void initGoogleServiceProvider() {
        if (prefsManager.isUsingTestGoogleServiceProvider()) {
            googleServiceProvider = new GoogleServiceProviderTesting();
        } else {
            googleServiceProvider = new GoogleServiceProviderImpl();
        }
    }
```

**Step 5:** Make a purchase same as regular play store

![Register Sample](https://github.com/nytm/register/blob/master/images/purchase.png?raw=true)

**Step 6:** Go to Your companion app to see the purchase 

![Register Sample](https://github.com/nytm/register/blob/master/images/purchased.png?raw=true)



### Fully Configurable (Configuration App)
Register Companion allows you to see purchases that were successfully or unsuccessfully made.  
Additionally you can control responses back to your client app for values such as `getSkuDetails` 
See image below for all configurable options on a response

![Register Sample](https://github.com/nytm/register/blob/master/images/registerCompanion.png?raw=true)

### Sample App

**SampleApp** is a client app to showcase working with Register 
See [SampleActivity](https://github.com/nytm/Register/blob/master/sampleApp/src/main/java/com/nytimes/android/external/register/sample/SampleActivity.java) for a demo  purchasing flow

### Gradle

```groovy
compile 'com.nytimes.android.register:0.0.1'
```
