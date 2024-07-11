# rn-square-mobile-payments

A React Native wrapper for Square's Mobile Payments SDK. **This is not meant to be a full and complete implementation of all the functionality that is included in the native sdks and will not be actively maintained & supported.** If there is some functionality that is missing here that you need, feel free to contribute a pull request!

Please review the requirements and limitations of the native sdks for [iOS](https://developer.squareup.com/docs/mobile-payments-sdk/ios#requirements-and-limitations) and [Android](https://developer.squareup.com/docs/mobile-payments-sdk/android#requirements-and-limitations).

## Installation

```sh
// npm
npm install rn-square-mobile-payments

// or yarn
yarn add rn-square-mobile-payments
```

### iOS Additional Setup
1. Ensure you have the proper [privacy permissions](https://developer.squareup.com/docs/mobile-payments-sdk/ios#privacy-permissions) in your application's `Info.plist`
2. Install pods from your `ios` directory with:
```
pod install
```
3. Add build phase to your target. On your application targets’ Build Phases settings tab, click the + icon and choose New Run Script Phase. Create a Run Script in which you specify your shell (ex: /bin/sh), and add the following contents to the script area below the shell:
```shell
SETUP_SCRIPT=${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}"/SquareMobilePaymentsSDK.framework/setup"
if [ -f "$SETUP_SCRIPT" ]; then
  "$SETUP_SCRIPT"
fi
```
4. Inside your app's `AppDelegate.m`, initialize the sdk inside the `didFinishLaunchingWithOptions` function:
```
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  ...
  // Initialize with your Square Application Id
  [SQMPMobilePaymentsSDK initializeWithApplicationLaunchOptions:launchOptions squareApplicationID:@"your square application id"];

  ...
}
```

### Android Addtional Setup
1. Ensure you have the proper [device permissions](https://developer.squareup.com/docs/mobile-payments-sdk/android#device-permissions).
2. Add the maven repository to your app's `build.gradle` file (`app/build.gradle`)
```
repositories {
  ...
  maven {
    url "https://sdk.squareup.com/public/android"
  }
}
```
3. Inside your `MainApplication` file, initialize the sdk:
```kotlin
import com.rnsquaremobilepayments.RNSquareMobilePaymentsModule
...

override fun onCreate() {
  super.onCreate()
  ...
  RNSquareMobilePaymentsModule.initializeMobilePaymentsSdk("your square application id", this)
}
```

## Usage

The following functions are provided for you to use as direct implementations of the native sdks.

```js
await authorizeMobilePaymentsSdk(accessToken, locationId);
await deauthorizeMobilePaymentsSdk();
await startPairing();
await stopPairing()
await startPayment(paymentParams);
await cancelPayment()
await addReaderObserver();
await removeReaderObserver();
await presentSettings();
await getReaders();
await getAuthorizationState();
await getAuthorizationLocation();
await forgetReader(readerId);
```
In addition, a `useSquareEventListener` hook has also been provided to tap into Square Reader events (reader added, removed, and changed):
```javascript
import { useSquareEventListener, ReaderEventType } from 'rn-square-mobile-payments';

...

const [pairedReaders, setPairedReaders] = useState([]);

useSquareEventListener(ReaderEventType.READER_ADDED, ({ readerInfo }) => {
  console.log('Reader Added Event with serial: ', readerInfo.serialNumber);
  setPairedReaders((prevReaders) => [
    readerInfo,
    ...prevReaders
  ]);
});
```

## Example
There is also an example app in the `example` folder at the root of this project, where you are able to see most of the functionality in action.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---
