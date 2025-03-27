# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保留基本配置
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# 保留四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# 保留 Parcelable 序列化的类
-keep class * implements android.os.Parcelable {
    static ** CREATOR;
}

# 保留 Serializable 序列化的类
-keepnames class * implements java.io.Serializable

# 保留 native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 R 文件
-keep class **.R$* {
    *;
}

# 保留 WebView 相关
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 保留自定义 View
-keep public class * extends android.view.View {
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留 Room 数据库相关
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.Database class *

# 保留 Retrofit 相关
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# 保留 Gson 相关
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留 Compose 相关
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keep class * extends androidx.compose.runtime.Composable { *; }
-keepclassmembers class * extends androidx.compose.runtime.Composable { *; }

# 保留 Shizuku 相关
-keep class rikka.shizuku.** { *; }
-keep class rikka.shizuku.server.** { *; }
-keep class rikka.shizuku.client.** { *; }

# 保留 EventBus 相关
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# OkHttp GraalVM 相关警告处理
-dontwarn com.oracle.svm.core.annotate.Delete
-dontwarn com.oracle.svm.core.annotate.Substitute
-dontwarn com.oracle.svm.core.annotate.TargetClass
-dontwarn java.lang.Module
-dontwarn org.graalvm.nativeimage.hosted.Feature$BeforeAnalysisAccess
-dontwarn org.graalvm.nativeimage.hosted.Feature
-dontwarn org.graalvm.nativeimage.hosted.RuntimeResourceAccess

# 保留 DataStore 相关
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }

# 保留 Splitties 相关
-keep class dev.rikka.tools.splitties.** { *; }
-keep class dev.rikka.tools.splitties.appctx.** { *; }
-keep class dev.rikka.tools.splitties.toast.** { *; }
-keep class dev.rikka.tools.splitties.systemservices.** { *; }
-keep class dev.rikka.tools.splitties.alertdialog.** { *; }

# 保留 ThreeTenABP 相关
-keep class org.threeten.bp.** { *; }
-keep class com.jakewharton.threetenabp.** { *; }

# 保留 FreeReflection 相关
-keep class me.weishu.reflection.** { *; }

# 优化配置
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-verbose
-printmapping mapping.txt
-printseeds seeds.txt
-printusage unused.txt

# 混淆配置
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 移除日志
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# 保留 Toast 相关
-keep class android.widget.Toast { *; }
-keep class android.widget.Toast$TN { *; }
-keep class android.widget.Toast$TN$Handler { *; }
-keep class android.widget.Toast$TN$InnerHandler { *; }

# 保留 Response 相关
-keep class com.smart.autodaily.data.entity.resp.Response { *; }
-keep class com.smart.autodaily.data.entity.resp.Response$Companion { *; }
-keepclassmembers class com.smart.autodaily.data.entity.resp.Response {
    public <init>(...);
    public final int getCode();
    public final java.lang.String getMessage();
    public final java.lang.Object getData();
}

# 保留 ToastUtil 相关
-keep class com.smart.autodaily.utils.ToastUtil { *; }
-keepclassmembers class com.smart.autodaily.utils.ToastUtil {
    public static void show(android.content.Context, java.lang.String);
}