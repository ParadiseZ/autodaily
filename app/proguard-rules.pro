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
-dontwarn okhttp3.**
-dontwarn okio.**
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

# 保留注册相关的数据类
-keep class com.smart.autodaily.data.entity.request.RegisterByEmailRequest { *; }
-keep class com.smart.autodaily.data.entity.request.LoginByEmailRequest { *; }
-keep class com.smart.autodaily.data.entity.request.RestPwdByEmailRequest { *; }
-keep class com.smart.autodaily.data.entity.LoginResponse { *; }
-keep class com.smart.autodaily.data.entity.UserInfo { *; }

# 保留 Retrofit 接口
-keep,allowobfuscation interface com.smart.autodaily.api.RegisterLoginApi
-keepclassmembers,allowobfuscation interface com.smart.autodaily.api.RegisterLoginApi {
    <methods>;
}

# 保留所有API接口
-keep,allowobfuscation interface com.smart.autodaily.api.** {
    <methods>;
}

# 保留所有实体类
-keep class com.smart.autodaily.data.entity.** { *; }
-keep class com.smart.autodaily.data.entity.request.** { *; }
-keep class com.smart.autodaily.data.entity.resp.** { *; }

# 保留实体类中的序列化字段
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 保留实体类中的getter和setter方法
-keepclassmembers class com.smart.autodaily.data.entity.** {
    public <init>(...);
    public *** get*();
    public void set*(***);
}

# 保留实体类中的序列化相关字段
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#
#-------------------------------------------基本不用动区域开始----------------------------------------------
#
#
# -----------------------------基本 -----------------------------
#
# 指定代码的压缩级别 0 - 7(指定代码进行迭代优化的次数，在Android里面默认是5，这条指令也只有在可以优化时起作用。)
-optimizationpasses 5
# 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类(不跳过library中的非public的类)
-dontskipnonpubliclibraryclasses
# 指定不去忽略包可见的库类的成员
-dontskipnonpubliclibraryclassmembers
#不进行优化，建议使用此选项，
-dontoptimize
 # 不进行预校验,Android不需要,可加快混淆速度。
-dontpreverify
# 屏蔽警告
#-ignorewarnings

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
# 避免混淆泛型, 这在JSON实体映射时非常重要
-keepattributes Signature
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
 #优化时允许访问并修改有修饰符的类和类的成员，这可以提高优化步骤的结果。
# 比如，当内联一个公共的getter方法时，这也可能需要外地公共访问。
# 虽然java二进制规范不需要这个，要不然有的虚拟机处理这些代码会有问题。当有优化和使用-repackageclasses时才适用。
#指示语：不能用这个指令处理库中的代码，因为有的类和类成员没有设计成public ,而在api中可能变成public
-allowaccessmodification
#当有优化和使用-repackageclasses时才适用。
-repackageclasses ''
 # 混淆时记录日志(打印混淆的详细信息)
 # 这句话能够使我们的项目混淆后产生映射文件
 # 包含有类名->混淆后类名的映射关系
-verbose

# 保持哪些类不被混淆
#继承activity,application,service,broadcastReceiver,contentprovider....不进行混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep class android.support.** {*;}## 保留support下的所有类及其内部类

#表示不混淆上面声明的类，最后这两个类我们基本也用不上，是接入Google原生的一些服务时使用的。
#----------------------------------------------------

# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**


#表示不混淆任何包含native方法的类的类名以及native方法名，这个和我们刚才验证的结果是一致
-keepclasseswithmembernames class * {
    native <methods>;
}


#这个主要是在layout 中写的onclick方法android:onclick="onClick"，不进行混淆
#表示不混淆Activity中参数是View的方法，因为有这样一种用法，在XML中配置android:onClick=”buttonClick”属性，
#当用户点击该按钮时就会调用Activity中的buttonClick(View view)方法，如果这个方法被混淆的话就找不到了
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}

#表示不混淆枚举中的values()和valueOf()方法，枚举我用的非常少，这个就不评论了
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#表示不混淆任何一个View中的setXxx()和getXxx()方法，
#因为属性动画需要有相应的setter和getter的方法实现，混淆了就无法工作了。
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#表示不混淆Parcelable实现类中的CREATOR字段，
#毫无疑问，CREATOR字段是绝对不能改变的，包括大小写都不能变，不然整个Parcelable工作机制都会失败。
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
# 这指定了继承Serizalizable的类的如下成员不被移除混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 保留R下面的资源
-keep class **.R$* {
 *;
}
#不混淆资源类下static的
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# 保留我们自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#
#----------------------------- WebView(项目中没有可以忽略) -----------------------------
#
#webView需要进行特殊处理
# ----------------------------- 实体Model不能混淆，否则找不到对应的属性获取不到值 -----------------------------
#
# 实体类，设置成自己的包名路径
-keep class com.smart.autodaily.viewmodel.**{*;}

# ----------------------------- 其他的 -----------------------------
# 删除代码中Log相关的代码
# 保持测试相关的代码
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
#
#-------------------------------------------基本不用动区域结束----------------------------------------------
#
#
#-------------------------------------------第三方不混淆区域开始----------------------------------------------
#
# -dontwarn 结合 -keep 设置第三方不混淆
#

# 沉浸式状态栏
-dontwarn com.gyf.immersionbar.**
-keep class com.gyf.immersionbar.* {*;}

# banner
-dontwarn com.youth.banner.**
-keep class com.youth.banner.**{*;}

# bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-dontwarn com.tencent.tinker.**
-keep public class com.tencent.tinker.**{*;}
-keep class android.support.**{*;}

# RxJava2 & RxAndroid
-dontwarn io.reactivex.rxjava2.**
-keep class io.reactivex.rxjava2.**{*;}

# 微信支付
-dontwarn com.tencent.mm.**
-dontwarn com.tencent.wxop.stat.**
-keep class com.tencent.mm.**{*;}
-keep class com.tencent.wxop.stat.**{*;}

# 支付宝钱包
-dontwarn com.alipay.**
-dontwarn HttpUtils.HttpFetcher
-dontwarn com.ta.utdid2.**
-dontwarn com.ut.device.**
-keep class com.alipay.**{*;}
-keep class com.ta.utdid2.**{*;}
-keep class com.ut.device.**{*;}

# 百度地图
-dontwarn com.baidu.**
-keep class com.baidu.**{*;}

-dontwarn com.smart.autodaily.service.**
-keep class com.smart.autodaily.service.**{*;}

-dontwarn com.smart.autodaily..**
-keep class com.smart.autodaily.service.**{*;}
