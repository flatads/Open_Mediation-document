# Open_Mediation SDK_Integration_Document

## OpenMediation SDK对接说明文档

> 开始之前
>我们支持Android系统版本Version 4.1 (API Level 16) 及以上。请确保满足以下要求：

>使用Android Studio 3.0 版本及以上
>Target Android API level 28
>minSdkVersion level 16 及以上

#### 1. 概述

本手册介绍如何将OpenMediation SDK集成到您的Android应用中。

#### 2. Ads ID

在进行SDK集成之前，您需要得到集成所必要的信息：APP_KEY 和 Placement ID。请联系您的客户经理获得。

APP_KEY: APP_KEY 是给开发者应用分配的唯一标识。Placement ID: 是广告位的唯一标识。

#### 3. 添加SDK到开发项目中

将下面的脚本添加到您的 project-level build.gradle 文件中。
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://dl.openmediation.com/omcenter/' }
    }
}
```
将下面的脚本添加到您的 application-level build.gradle 文件中 dependencies 分段内。
```
implementation 'com.openmediation:om-android-sdk:2.3.2'
```

**从 GitHub 仓库下载**

您可以通过 GitHub 仓库来获取 OpenMediation SDK 源码和演示应用程序：
```
git clone git://github.com/AdTiming/OpenMediation-Android.git
```

#### 4. 更新 AndroidManifest.xml配置

接下来需要添加配置到您的AndroidManifest.xml 文件中，将以下所示的permission脚本添加到<manifest> 中，确保不要放在 <application> 内。
```
<!-- Required permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### 5. Proguard

如果您在应用中使用了Proguard来保护代码，请务必添加下面的配置到您的 Proguard 配置文件 (Android Studio: proguard-rules.pro or Eclipse: proguard-project.txt)，否则SDK将会报错。
```
-dontwarn com.openmediation.sdk.**.*
-dontskipnonpubliclibraryclasses
-keep class com.openmediation.sdk.**{*;}
#R
-keepclassmembers class **.R$* {
  public static <fields>;
}
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepnames class * implements android.os.Parcelable {
  public static final ** CREATOR;
}
```

#### 6. MultiDex

如果您的应用中使用了multiDex，需要添加下面脚本到build.gradle文件。
```
android {
  buildTypes {
    release {
      multiDexKeepProguard file('multidex-config.pro')
      ...
    }
  }
}
```
**相应的 multidex-config.pro 文件的内容如下所示：**
```
-keep class com.openmediation.sdk.**{*;}
-dontwarn com.openmediation.sdk.**.*
```

#### 7. 重载Activity生命周期方法

在开始初始化SDK之前，您需要重写应用的生命周期方法。重载每个Activity对象的onPause()和onResume()方法，调用对应的OmAds.onPause()和onResume()方法，如下所示。
```
protected void onResume() {
     super.onResume();
     OmAds.onResume(this);
  }
protected void onPause() {
     super.onPause();
     OmAds.onPause(this);
  }
```

#### 8. 初始化SDK

完成SDK的下载与集成后，就可以开始对SDK进行初始化了。初始化是在调用SDK进行广告加载展示之前必须做的工作，否则SDK不会进行任何有效的工作。我们建议在应用启动的时候进行SDK初始化，比如在 Application 或 Activity 的 onCreat() 事件方法是一个不错的选择，如下所示：
```
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.utils.error.Error;
...

InitConfiguration configuration = new InitConfiguration.Builder()
          .appKey("Your AppKey")
          .logEnable(false)
          .build();
OmAds.init(configuration, new InitCallback() {

    // Invoked when the initialization is successful.
    @Override
    public void onSuccess() {
    }

    // Invoked when the initialization is failed.
    @Override
    public void onError(Error error) {
    }
});
```
>**注意事项:**
APP KEY需要在OpenMediation前台创建应用时获取的。
SDK的智能库存机制会自动加载和维护激励视频与插屏视频的广告库存，因此不需要手动调用load方法来加载这两种广告。
onError回调方法的error参数包含初始化失败的原因，如遇到异常请参考 错误与诊断 获取更多信息。

#### 最佳实践：按广告类型初始化

v2.0及以上版本的SDK提供了新的初始化方案，可以指定一个或多个广告类型进行初始化。我们建议使用这种新的方法进行初始化，因为按广告类型的初始化方法仅仅会获取对应类型的广告进行预加载。基于这种方法，开发者可以将不同类型广告的初始化和预加载放在应用的不同时间节点上，基于应用的广告场景设计。这种方案的好处是，可以将原先集中在应用启动的时间点进行的广告预加载动作，分散到不同时间点，避免可能的网络拥挤导致广告加载速度缓慢、以致影响应用体验。

下面代码示例了如何通过初始化方法仅仅初始化和预加载激励视频与插屏广告两种类型。

```
// Ad Units should be in the type of OmAds.AD_TYPE.AdUnitName, for example:
InitConfiguration configuration = new InitConfiguration.Builder()
    .appKey("Your AppKey")
    .preloadAdTypes(OmAds.AD_TYPE.INTERSTITIAL, OmAds.AD_TYPE.REWARDED_VIDEO)
    .build();
OmAds.init(configuration, callback);
```
可以在应用的不同时间点分别初始化每一种广告类型，如下所示：
```
// Init with pre-load Rewarded video ads
InitConfiguration configuration = new InitConfiguration.Builder()
    .appKey("Your AppKey")
    .preloadAdTypes(OmAds.AD_TYPE.REWARDED_VIDEO)
    .build();
OmAds.init(configuration, callback);
// Init with pre-load Interstitial
InitConfiguration configuration = new InitConfiguration.Builder()
    .appKey("Your AppKey")
    .preloadAdTypes(OmAds.AD_TYPE.INTERSTITIAL)
    .build();
OmAds.init(configuration, callback);
```

> 如果类型不传入任何参数，意味着会默认预加载激励视频和插屏广告类型 如果不想要做任何预加载，请传入 OmAds.AD_TYPE.NONE 参数

```
// Init with no pre-load
InitConfiguration configuration = new InitConfiguration.Builder()
    .appKey("Your AppKey")
    .preloadAdTypes(OmAds.AD_TYPE.NONE)
    .build();
OmAds.init(configuration, callback);
```
#### 实现回调事件处理

OpenMediation SDK在初始化操作中会出发一系列的事件来通知应用程序，您需要实现InitCallback接口的onSuccess() 和 onError() 回调方法以处理初始化成功和失败的事件。
```
@Override
public void onSuccess() {
    // Add code here to process init success event.
}
@Override
public void onError(Error error) {
    // Add code here to process init failed event.
    // Parameter message tells the error information.
}
```

#### 上报自定义用户标识符

应用可以通过SDK上报自定义的设备标识符，只需要在初始化之前调用setUserId方法设置。该标识符会在用户级的数据中体现。
```
OmAds.setUserId(String userId);
```

#### 9. 添加广告源SDK聚合
将下面的脚本添加到您的 application-level **build.gradle** 文件中 **dependencies** 分段内
```groovy
dependencies{
	implementation 'com.flatads.sdk:flatads:1.1.16'
	implementation 'com.adtiming:adnetwork:6.9.1'
	implementation 'com.mbridge.msdk.oversea:videojs:15.5.31'
    implementation 'com.mbridge.msdk.oversea:mbjscommon:15.5.31'
    implementation 'com.mbridge.msdk.oversea:playercommon:15.5.31'
    implementation 'com.mbridge.msdk.oversea:reward:15.5.31'
    implementation 'com.mbridge.msdk.oversea:videocommon:15.5.31'
    implementation 'com.mbridge.msdk.oversea:same:15.5.31'
    implementation 'com.mbridge.msdk.oversea:interstitialvideo:15.5.31'
    implementation 'com.mbridge.msdk.oversea:mbbanner:15.5.31'
    // for using bidding
    implementation 'com.mbridge.msdk.oversea:mbbid:15.5.31'
}
```
将下面的脚本添加到您工程的 **build.gradle** 文件中 **repositories** 分段内。
```groovy
allprojects {
    repositories {
        jcenter()
        google()
        maven {url "http://maven.flat-ads.com/repository/maven-public/"}
        maven {url 'https://dl.openmediation.com/omcenter/'}
        maven {url "https://dl.adtiming.com/android-sdk"}
        maven { url "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea"}

    }
}
```
**仅针对使用 ProGuard**
```groovy
-keep class com.flatads.sdk.response.* {*;}
-keep class com.adtbid.sdk.** { *;}
-dontwarn com.adtbid.sdk.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep interface androidx.** { *; }
-keep class androidx.** { *; }
-keep public class * extends androidx.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }
```
#### 添加 Adapter
将下面的脚本添加到您的 application-level **build.gradle** 文件中 **dependencies** 分段内
```groovy
implementation 'com.openmediation.adapters:flatads:2.3.3'
implementation 'com.openmediation.adapters:adtiming:2.3.1'
implementation 'com.openmediation.adapters:mintegral:2.3.0'

```


#### 10. 开屏广告
开屏广告以 APP 启动作为曝光时机，提供 3s~5s 的广告展示时间。用户可以点击广告跳转到目标页面， 或者点击右上角的“跳过”按钮，跳转到 APP 内容首页。


**Step 1. 设置开屏广告回调**

SDK 会触发一系列事件来通知应用程序开屏广告的加载、展示等结果。开发者需要通过事件来获知广告是否准备好。所以，设置和实现开屏广告回调 Listener 的接口方法，是使用开屏广告的必要操作。下面的代码片段演示了如何实现 SplashAdListener 接口来接收和处理开屏广告事件。

开屏广告触发的所有事件都可以在下面代码中找到。
```
import com.openmediation.sdk.splash.SplashAd;
import com.openmediation.sdk.splash.SplashAdListener;
import com.openmediation.sdk.utils.error.Error;
...
SplashAd.setSplashAdListener(placementId, new SplashAdListener() {

    /**
     * called when SplashAd loaded
     */
    @Override
    public void onSplashAdLoaded(String placementId) {
    }

    /**
     * called when SplashAd load error
     */
    @Override
    public void onSplashAdFailed(String placementId, Error error) {
    }

    /**
     * called when SplashAd clicked
     */
    @Override
    public void onSplashAdClicked(String placementId) {
    }

    /**
     * called when SplashAd showed
     */
    @Override
    public void onSplashAdShowed(String placementId) {
    }

    /**
     * called when SplashAd show failed
     *
     * @param error SplashAd show error reason
     */
    @Override
    public void onSplashAdShowFailed(String placementId, Error error) {
    }

    /**
     * called when SplashAd countdown
     * @param millisUntilFinished The time until the end of the countdown,ms
     */
    @Override
    public void onSplashAdTick(String placementId, long millisUntilFinished) {
    }

    /**
     * called when SplashAd dismissed
     */
    @Override
    public void onSplashAdDismissed(String placementId) {
    }
});
```
**Step 2. 加载开屏广告**
```
SplashAd.loadAd(String placementId);
```
**Step 3. 展示开屏广告**
```
if (SplashAd.isReady(placementId)) {
    SplashAd.showAd(placementId);
}
```

#### 11. 激励视频

**Step 1. 设置激励视频回调**

SDK会触发一系列事件来通知应用程序激励视频广告的活动，如广告库存状态、广告播放完成、用户获得奖励等事件，开发者需要通过事件来获知广告是否准备好，以及是否需要给用户奖励。所以，设置和实现激励视频回调Listener的接口方法，是使用激励视频广告的必要操作。下面的代码片段演示了如何实现RewardedVideoListener 接口来接收和处理视频广告事件。
激励视频触发的所有事件都可以在下面代码中找到。

```
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoAd;
import com.openmediation.sdk.video.RewardedVideoListener;
...
RewardedVideoAd.setAdListener(new RewardedVideoListener() {

    /**
     * Invoked when the ad availability status is changed.
     *
     * @param available is a boolean.
     *      True: means the rewarded videos is available and
     *          you can show the video by calling RewardedVideoAd.showAd().
     *      False: means no videos are available
     */
    @Override
    public void onRewardedVideoAvailabilityChanged(boolean available) {
        // Change the rewarded video state according to availability in app.
        // You could show ad right after it's was loaded here
    }

    /**
     * Invoked when the RewardedVideo ad view has opened.
     * Your activity will lose focus.
     */
    @Override
    public void onRewardedVideoAdShowed(Scene scene) {
        // Do not perform heavy tasks till the video ad is going to be closed.
    }

    /**
     * Invoked when the call to show a rewarded video has failed
     * @param error contains the reason for the failure:
     */
    @Override
    public void onRewardedVideoAdShowFailed(Scene scene, Error error) {
        // Video Ad show failed
    }

    /**
     * Invoked when the user clicked on the RewardedVideo ad.
     */
    @Override
    public void onRewardedVideoAdClicked(Scene scene) {
        // Video Ad is clicked
    }

    /**
     * Invoked when the RewardedVideo ad is closed.
     * Your activity will regain focus.
     */
    @Override
    public void onRewardedVideoAdClosed(Scene scene) {
        // Video Ad Closed
    }

    /**
     * Invoked when the RewardedVideo ad start to play.
     * NOTE:You may not receive this callback on some AdNetworks.
     */
    @Override
    public void onRewardedVideoAdStarted(Scene scene) {
        // Video Ad Started
    }

    /**
     * Invoked when the RewardedVideo ad play end.
     * NOTE:You may not receive this callback on some AdNetworks.
     */
    @Override
    public void onRewardedVideoAdEnded(Scene scene) {
        // Video Ad play end
    }

    /**
     * Invoked when the video is completed and the user should be rewarded.
     * If using server-to-server callbacks you may ignore this events and wait
     * for the callback from the OpenMediation server.
     */
    @Override
    public void onRewardedVideoAdRewarded(Scene scene) {
        // Here you can reward the user according to your in-app settings.
    }
});
```

**Step 2. 展示激励视频广告**

检查广告可用

OpenMediation SDK 会自动加载广告进行广告库存的维护，您只需正确的完成SDK集成和初始化。通过实现RewardedVideoListener接口，应用程序将会收到广告可用性变化的事件通知，通过 onRewardedVideoAvailabilityChanged 接口的available参数获取到当前广告是否可用。
```
public void onRewardedVideoAvailabilityChanged(boolean available)
```
当然，您也可以通过直接调用isReady()  方法来检查广告库存状态，如下所示。
```
public boolean isReady()
```

展示广告

一旦收到onRewardedVideoAvailabilityChanged 事件的true回调，您就可以调用showAd() 方法进行广告的展示。sceneName参数用于标记当前广告展示的场景和记录激励的位置。
```
public void showAd(String sceneName)
```

>**注意：**
Scene是可选功能，若无需要可忽略该参数，系统会自动将广告匹配到默认的Default_Scene。

我们不建议在onRewardedVideoAvailabilityChanged回调直接展示广告，如下代码所示。Availability事件只有在广告可用性发生变化的时候才会产生，这并不一定符合应用设计的广告场景，而且有可能造成频繁的甚至连续的广告展示。这会给用户产生困扰，影响应用的使用体验。您应该根据应用中的广告场景设计来选择广告展示时机。

```
//if you would like to show ad right after it's was loaded
public void onRewardedVideoAvailabilityChanged(boolean available) {
    if(available) {
        RewardedVideoAd.showAd(sceneName)
    }
}
```

>**警告！**
在 onRewardedVideoAvailabilityChanged 回调中展示广告可能会导致不可预见的行为。一般情况您不应这么做，除非在某些特定的场景并且对showAd方法的调用进行了必要的限制。

如果您只是想在需要的情况下（应用设计的广告场景）进行广告展示，请先通过调用isReady() 方法检查广告是否可用，这是我们建议的方式：
```
//if you would like to show ad when it's required
if (RewardedVideoAd.isReady()) {
    RewardedVideoAd.showAd(sceneName);
}
```
>**注意：**
当您成功的完成步骤2，意味着广告已经展示成功。如果您想展示另外的广告，只需重复步骤2进行展示即可，不需要手动调用loadAd方法进行广告加载。


**Step 3. 奖励用户**

用户完整的看完广告视频后，SDK会触发onRewardedVideoAdRewarded 事件. 所以您的RewardedVideoListener 会收到该事件，然后就可以给用户进行奖励。

>**注意：**
请务必确保设置了listener并实现了onRewardedVideoAdRewarded 回调方法来处理用户奖励。不要仅依赖onRewardedVideoAdClosed  事件来处理奖励，onRewardedVideoAdRewarded 和onRewardedVideoAdClosed 事件是异步处理的。

服务端回调

支持激励视频广告的服务端回调，开发者需要设置一个私有的回调地址（Callback URL，如下图所示），并指定信息用于验证 OpenMediation 向回调地址发送的激励回调，因此 OpenMediation 需要在回调中带上这个验证信息。验证信息由开发者调用SDK的setExtId()方法写入，在产生激励的时候，SDK联系OpenMediation 服务器通知该设备获得奖励，并附带验证信息。OpenMediation 服务器验证后，再通过回调地址将奖励信息和验证信息发给开发者的服务器。由于用户设备不会直接向开发者服务器联系，该方法可以避免虚假的激励回调事件产生。

验证信息ExtId

激励视频广告的服务端交互需要开发者调用setExtId方法来设置用户验证信息。由于设置的ExtId将用于验证激励的过程，因此该方法必须在showAd展示广告之前调用。该方法可以基于scene设置不同的验证信息，以区分不同场景广告，实现按场景的奖励分配。
```
RewardedVideoAd.setExtId(scene, "Your Ext Id");
```

配置激励视频广告服务端回调地址

请联系您的客户经理配置激励视频的回调地址

回调地址格式

回调地址采用正则表达式格式，开发者可以设置自定义的变量名称，变量的值由ExtId写入。如下所示：
http://yourendpoint.com?variable_name_you_define={content}

{content} - 验证信息，由开发者在SDK客户端设置的ExtId填入，并通过 OpenMediation 服务器传递给您的服务端回调接口，用于验证激励的有效性。

#### 12. 插屏广告
插屏广告是一种全屏展现的广告类型，通常用于app中自然的场景切换过程。OpenMediation SDK同时支持静态（图片）和动态（视频）两种形式的插屏广告类型。

**Step 1. 设置插屏广告回调**

SDK会触发一系列事件来通知应用程序插屏广告的活动，如广告库存状态、广告播放完成等事件，开发者可以通过事件来获知广告是否准备好，以及广告是否被正确的展示和点击。所以，设置和实现插屏广告回调Listener的接口方法，是使用插屏广告的必要操作。我们将通过代码片段来演示如何实现InterstitialAdListener接口来接收和处理插屏广告的回调事件。

下面的示例代码中列举了所有可能的插屏广告事件：
```
import com.openmediation.sdk.interstitial.InterstitialAd;
import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;
...
InterstitialAd.setAdListener(new InterstitialAdListener() {

    /**
     * Invoked when the interstitial ad availability status is changed.
     *
     * @param - available is a boolean.
     *          True: means the interstitial ad is available and you can
     *              show the video by calling InterstitialAd.showAd().
     *          False: means no ad are available
     */
    @Override
    public void onInterstitialAdAvailabilityChanged(boolean available) {
        // Change the interstitial ad state in app according to param available.
    }

    /**
     * Invoked when the Interstitial ad view has opened.
     * Your activity will lose focus.
     */
    @Override
    public void onInterstitialAdShowed(Scene scene) {
        // Do not perform heavy tasks till the ad is going to be closed.
    }

    /**
     * Invoked when the Interstitial ad is closed.
     * Your activity will regain focus.
     */
    @Override
    public void onInterstitialAdClosed(Scene scene) {
    }

    /**
     * Invoked when the user clicked on the Interstitial ad.
     */
    @Override
    public void onInterstitialAdClicked(Scene scene) {
    }

    /* Invoked when the Interstitial ad has showed failed
     * @param - error contains the reason for the failure:
     */
    @Override
    public void onInterstitialAdShowFailed(Scene scene, Error error) {
        // Interstitial ad show failed
    }
});
```
>**注意：**
onInterstitialAdShowFailed回调中的参数error包含失败的原因信息，具体可以参考 Error Code 来获取更多信息和错误诊断。

**Step 2. 展示插屏广告**

检查广告可用

OpenMediation SDK智能库存引擎负责广告库存的维护，您只需完成SDK集成和初始化，SDK会自动加载广告。通过实现InterstitialAdListener接口，应用程序将会收到广告可用性变化的事件通知，onInterstitialAd AvailabilityChanged接口的唯一参数available指示当前广告库存状态，true表示当前库存中有一个或多个可用广告，false则意味着库存中没有任何可用的广告。
```
public void onInterstitialAdAvailabilityChanged(boolean available)
```
当然，您也可以通过直接调用isReady()  方法来检查广告库存状态，如下所示。
```
public boolean isReady()
```
展示广告

一旦收到 onInterstitialAdAvailabilityChanged 事件的true回调，您就可以调用showAd() 方法进行广告的展示。
```
public void showAd(String sceneName)
```

>**注意：**
Scene是可选功能，若无需要可忽略该参数，系统会自动将广告匹配到默认的Default_Scene。

在应用设计的广告场景中进行广告展示时，我们强烈建议您在进行广告展示之前，先通过调用isReady() 方法检查广告是否可用，如下所示:

```
//if you would like to show ad when it's required
if (InterstitialAd.isReady()) {
    InterstitialAd.showAd(sceneName);
}
```
当您成功的完成步骤2，意味着广告已经展示成功。如果您想展示另外的广告，只需重复步骤2进行展示即可，不需要手动调用loadAd方法进行广告加载。

#### 13. 横幅广告
横幅广告（Banner）提供一个长方形的横幅广告，一般在应用指定的区域提供展现。

**Step 1. 初始化BannerAd对象**

SDK会触发一系列事件通知应用程序Banner广告的初始化、加载、展示等结果。使用Banner广告的过程，需要创建BannerAd对象、实现并设置监听事件的Listener，然后调用loadAd方法加载并展示广告。Banner广告没有 show 方法， 需要在广告加载成功后马上展示广告。

下面的代码示例展示了如何使用BannerAd 对象和实现BannerAdListener 接口来监听广告事件。Banner广告所有的事件类型都能在下面代码中找到。

```
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.banner.BannerAd;
import com.openmediation.sdk.banner.BannerAdListener;
...
BannerAd bannerAd = new BannerAd(placementId, new BannerAdListener() {

      /**
       * Invoked when Banner Ad are available.
       */
      @Override
      public void onBannerAdLoaded(String placementId, View view) {
          // bannerAd is load success
      }

      /**
       * Invoked when the end user clicked on the Banner Ad
       */
      @Override
      public void onBannerAdClicked(String placementId) {
         // bannerAd clicked
      }

      /**
       * Invoked when the call to load a Banner Ad has failed
       * String error contains the reason for the failure.
      */
      @Override
      public void onBannerAdLoadFailed(String placementId, Error error) {
         // bannerAd fail
      }
});
// default size
bannerAd.setAdSize(AdSize.BANNER);
```

有关我们支持的标准横幅尺寸的详细信息，请参见下表：

| AdSize |	Description |	Dimensions in dp (WxH) |
|  ----  | ----  | ----  |
|AdSize.BANNER|	Standard Banner	|320 x 50|
|AdSize.MEDIUM_RECTANGLE|	Medium Rectangular Banner|	300 x 250|
|AdSize.LEADERBOARD|	LeaderBoard Banner|	728 x 90
|AdSize.SMART	|Smart Banner(Adjusted for both mobile and tablet)	| If (screen height ≤ 720) 320 x 50; If (screen height > 720) |728 x 90

Step 2. 加载Banner广告

您需要在展示广告前调用 loadAd 方法来请求和缓存广告。我们建议您务必在广告展示前提前一定时间进行调用，以免影响广告体验。
```
bannerAd.loadAd();
```

>**注意：**
loadAd 方法可以同时被多次调用，但是我们不建议这么做。因为短时间的连续调用不会增加广告填充率，如果已经有正在进行中的加载，新的请求不会被处理。
警告： 在onAdFailed回调事件中进行广告加载操作是非常危险的，如果您一定要在此处进行加载，请务必设置一个时间间隔限制，避免程序因为无网络等原因造成连续的失败而进入死循环。
请不要在应用程序中定时请求Banner广告，OpenMediation SDK会自动对Banner广告进行定时刷新。

**Step 3. 展示Banner广告**

第二步中Banner广告加载成功后，应用程序将会收到onAdReady事件表明广告缓存成功。一旦收到onAdReady事件，您可以进行广告展示，请参考以下代码示例。
```
 private RelativeLayout adParent;
 ...
 adParent = this.findViewById(R.id.banner_ad_container);
 ...

 @Override
 public void onBannerAdLoaded(String placementId, View view) {
    // bannerAd is loaded successfully
    if (null != view.getParent()) {
        ((ViewGroup) view.getParent()).removeView(view);
    }
    adParent.removeAllViews();
    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    adParent.addView(view, layoutParams);
}
```

■ R.id.banner_ad_container 代码:

```
<RelativeLayout
   android:id="@+id/banner_ad_container"
   android:layout_width="match_parent"
   android:layout_height="wrap_content">

</RelativeLayout>
```

**Step 4. 销毁BannerAd对象**

我们建议您在Activity被销毁之前调用destroy方法来释放BannerAd对象。
```
@Override
public void onDestroy() {
   if (bannerAd != null) {
      bannerAd.destroy();
   }
   super.onDestroy();
}
```

#### 14. 集成测试
测试广告位
启用测试的最快方法是使用 OpenMediation 提供的演示广告位：

AppKey：OtnCjcU7ERE0D21GRoquiQBY6YXR3YLl
横幅广告	：4900
插屏广告	：4901
激励广告	：4903
开屏广告	：8144

#### 15. SDK错误处理机制
在集成SDK进行初始化、广告加载、展示的过程中，如果发生任何的异常或操作失败，如广告无填充、展示失败等，SDK都会在回调接口的失败事件中返回对应的错误信息，包括错误码和错误消息，来指示当前问题的具体原因。

如，初始化操作是通过InitCallback回调接口的onError事件方法，而广告展示操作则是通过各个广告单元的Listener回调接口，如激励视频的RewardedVideoListener 回调接口，通过回调事件方法onRewardedVideoAdShowFailed来通知激励视频加载展示过程中可能出现的失败，事件的唯一参数error是一个Error类的对象，从中解析到错误码和错误消息。具体参见Android SDK下载与集成和广告单元。

由于激励视频和插屏广告由智能库存引擎自动加载和缓存广告，没有load方法和对应的事件回调，广告加载相关的失败提示通过SDK日志返回，您可以在日志中获取到错误码和错误消息。

**错误码**

错误码和对应的错误原因说明：

初始化相关

111	invalid request	初始化请求无效，可能原因是无效的参数（如AppKey错误）或SDK配置错误（如Manifest.xml文件配置错误）
121	network error	无网络连接
131	server error	服务器响应失败或无响应，可能原因是网络不佳导致消息传输出错，或服务器内部错误
151	unknown internal error	不可预见的异常，如Activity被销毁导致的失败

加载相关

211	Invalid request	加载请求无效，可能原因是无效的参数（如placementID 错误）或SDK配置错误（如Manifest.xml文件配置错误）
221	network error	无网络连接
231	server error	服务器响应失败或无响应，可能原因是网络不佳导致消息传输出错，或服务器错误
241	No ad fill	请求已成功送达，但没有可用广告
242	SDK not initialized	SDK 未初始化，请先初始化SDK再加载广告
243	reached request cap	广告请求达到频次控制的限制
244	missing AdNetwork SDK/Adapter	缺少第三方平台SDK或adapter包导致该instance初始化和加载广告失败
245	adapter returned an error or no fill	第三方平台返回广告加载失败或无填充
251	unknown internal error	不可预见的异常，如Activity被销毁导致的失败

展示相关

311	invalid request	展示广告时存在scene参数错误的问题
341	ad not ready	没有可用的广告。展示广告前调用isReady检查库存可避免此错误
342	SDK not initialized	SDK未初始化。在加载展示广告前需要先初始化SDK
343	reached scene impression cap	达到了广告展示的频次控制的限制
345	failure	展示广告失败
351	unknown internal error	不可预见的异常，如Activity被销毁导致的失败

**诊断**

1. **错误码 111 - SDK init: invalid request**
**诊断**：在进行sdk初始化方法init调用时收到”SDK init: invalid request“的失败回调，意味着当前初始化请求存在问题且没有被有效的发出，一般是由于方法调用的参数存在错误或SDK所需的运行环境（如manifest文件的权限配置）存在错误导致，建议根据帮助文档Andrdoid SDK下载与集成对初始化方法的参数和环境配置进行检查确认。可能存在问题的地方有：
无效的AppKey参数：请检查appkey参数格式与内容是否正确，确定所使用的参数与您在OpenMediation UI 前台申请的AppKey完全一致。
Activity无效：请传入一个合法存在的activity对象作为初始化方法的参数，建议在Activity的onCreate事件中进行sdk初始化操作。参考帮助中心 Android SDK下载与集成的初始化部分。
WebView not supported：当前设备不支持WebView，OpenMediation 广告依赖WebView展示。处理方法：请更换设备重试，确保设备支持WebView
AndroidManifest.xml中缺少必须的权限设置：请检查是否添加了INTERNET和ACCESS_NETWORK_STATE权限，SDK需要此两个权限访问网络。处理方法：请在AndroidManifest.xml配置文件中添加这两个权限，具体参考帮助中心 Android SDK下载与集成的更新AndroidManifest.xml配置 部分内容。


2. **错误码 121 - SDK init: network error**
**诊断**： 在进行sdk初始化方法init调用时收到”网络错误“的回调，意味着您当前的设备没有互联网连接。
**解决办法**：建议检查设备并打开wifi或蜂窝网络（并确保设备内SIM卡是可用的），以确保设备具有有效的互联网连接


3. **错误码151 - SDK init: unknown error**
**诊断**：在进行sdk初始化方法init调用时收到”未知错误“回调，意味着程序中存在未知的错误，如activity被destroy，或其他未知的运行时异常。
**解决办法**：
首先检查app是否在初始化期间对activity做了destroy操作。不要将临时的activity对象作为参数传递给SDK，或者在初始化过程中对activity进行finish或destroy操作。SDK的初始化过程包括对第三方mediation network SDK的初始化操作，整个过程都会需要一个合法存在的activity对象，如果使用了临时的activity对象，极可能在未完成初始化的时候就destroy或finished了，就会产生UnKnownException。
确认activity对象为非临时对象且未进行destroy操作之后，如果问题依旧存在，请联系我们的技术支持，通过email或ticket方式，上传日志和设备、媒体相关信息。

4. **错误码131 - SDK init: server error**
**诊断**：初始化时收到”服务器响应失败“回调，可能网络不稳定导致向服务器请求初始化时出错，或服务器发生内部错误。
**解决办法**：请检查网络后重新尝试。如果问题依旧存在，请联系我们的技术支持，通过email或ticket方式，上传日志和设备、媒体相关信息。



**广告加载相关错误码**

5. **错误码241 - Ads loading: No ad fill**
**诊断**：请求成功，没有广告可用。服务器没有返回可用的instance，需要检查mediation rule设置，确认waterfall中有active的instance或weight设置不为0。可能您没有配置任何mediation rules，OpenMediation 在当前地区也没有合适的广告可填充。也可能当前设备或媒体的服务受到限制，需检查publisher账号状态、app状态、设备的广告追踪设置、SDK版本等。
**解决办法**：请按下面步骤逐步操作并尝试
请检查并确认app是否处于激活状态。如app被删除或暂停服务，请联系我们的技术支持，可通过email或ticket方式，务必填写设备和媒体相关信息，并上传SDK日志。
如果您使用了聚合功能，请检查mediation rules配置，确保至少有一个active的instance，并且instance的weight不能为0
分别尝试更换设备、更换IP地址、切换wifi等操作后进行请求，或将当前设备添加到测试模式中，以排除特定设备、投放地域和网络的限制，如该设备设置了限制广告跟踪，或当前地域无匹配广告。
请更新最新版的SDK再进行尝试，以排除低版本SDK被限制服务的情况
如果问题依旧存在，请联系我们的技术支持，可通过email或ticket方式，务必填写设备和媒体相关信息，并上传SDK日志。

6. **错误码242 - Ad loading: SDK not initialized**
**诊断**：App未对SDK进行初始化调用，无法进行加载广告操作，App应首先调用SDK的init()方法并确保成功后才能进行广告的加载和展示。
**解决办法**：请检查代码是否在合适的时候进行了SDK的初始化调用。我们建议在应用的activity的onCreate事件中进行sdk初始化操作，并确保未使用临时的activity或未对activity进行destroy操作。参考帮助中心 Android SDK下载与集成的初始化部分。


7. **错误码243 - Ad loading: reached impression cap**
**诊断**：当前广告请求因展现广告数已达到频次控制的限制。
**解决办法**：请在 OpenMediation 开发者前台检查广告位和instance的频次控制设置，按设置的频次和间隔进行广告请求，或解除频次控制后再次尝试。


8. **错误码244 - Instance loading failure: missing AdNetwork SDK/Adapter**
**诊断**：WaterFall中 instance 所需的 AdNetwork SDK 或 Adapter 没有集成
**解决办法**：添加缺失的第三方平台的 SDK 和 Adapter，参考帮助中心 Add Mediation Networks 向导内容完成所需平台的 SDK 和 adapter 的集成。


9. **错误码245 - Instance loading failure: adapter returned an error**
**诊断**：Instance广告加载失败，具体原因请依据日志中第三方平台sdk返回的错误信息进行判断
**解决办法**：根据返回的adn加载失败信息，查阅第三方平台的帮助文档，获得解决问题的方法。一般性的解决方法为如下：
如果返回nofill 类型的错误： 尝试更换设备、更换IP地址、切换wifi等操作后进行请求，或将当前设备添加到该平台的测试模式中，以排除特定设备、投放地域和网络的限制。
如果在实际运营中该平台的填充率较低，可以尝试降低该instance的底价，如果该问题依旧存在，建议更换其他平台
如果返回频次受限，则请放宽该平台广告的加载频次控制
如果返回平台内部错误，如超时或无效响应、请求广告出错、网络加载数据出错、服务器未知错误等，且多次尝试问题依旧，则建议更换其他平台尝试，或等平台恢复稳定后再次尝试。
如果返回其他错误，如参数错误、无效的请求等，请通过email或ticket方式联系我们技术支持，上传日志和设备、媒体相关信息，帮助排查问题。


**广告展示相关错误码**

10. **错误码：341**
**诊断**：无广告可显示
**解决办法**：当前库存无广告，请等待onavailable事件通知。您可以在每次展示广告前通过isReady()接口检查库存情况，提前获取库存情况，便于应用根据库存情况调整广告场景。

11. **错误码：343**
**诊断**：当前场景已达到当前会话的频次控制上限，展示被限制
**解决办法**：检查您在广告位设置的scene的频次和步长，确认是否正常。

12. **错误码：311**
**诊断**：scene参数填写错误。
**解决办法**：检查您在广告位设置的scene列表，确认代码中所填scene名字与前台配置是否一致。如果您没有设置任何scene，则可以忽略这个错误。

13. **错误码：351**
**诊断**：未知异常。在进行广告展示时收到”未知错误“回调，意味着程序中存在未知的错误，如activity被destroy，或其他未知的运行时异常。
**解决办法**：请参考错误码151解决。

14. **错误码：345**
**诊断**：展示广告失败。
**解决办法**：对于 OpenMediation 的广告，请联系我们技术支持。如果是第三方平台的广告展示失败，请参阅第三方平台帮助文档。
