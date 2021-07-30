# flat-ads-om-document
## **添加 SDK 和 Adapter 到项目中**
### 1.添加 SDK
将下面的脚本添加到您的 application-level **build.gradle** 文件中 **dependencies** 分段内
```groovy
dependencies{
	implementation 'com.flatads.sdk:flatads:1.1.13'
}
```
将下面的脚本添加到您工程的 **build.gradle** 文件中 **repositories** 分段内。
```groovy
allprojects {
    repositories {
        jcenter()
        google()
        maven {url "http://maven.flat-ads.com/repository/maven-public/"}
    }
}
```
**仅针对使用 ProGuard**
```groovy
-keep class com.flatads.sdk.response.* {*;}
```
### 2.添加 Flat Ads Adapter
将下面的脚本添加到您的 application-level **build.gradle** 文件中 **dependencies** 分段内
```groovy
implementation 'com.openmediation.adapters:flatads:2.3.2'
```
将下面的脚本添加到您工程的 **build.gradle** 文件中 **repositories** 分段内。
```groovy
allprojects {
    repositories {
        jcenter()
        google()
        maven {url "http://maven.flat-ads.com/repository/maven-public/"}
        maven {url 'https://dl.openmediation.com/omcenter/'}
    }
}
```