# 保持 Adverge SDK 类
-keep class com.adverge.sdk.** { *; }

# 保持 Mintegral SDK 类
-keep class com.mbridge.msdk.** { *; }
-keep class com.mbridge.nativex.** { *; }
-keep class com.mbridge.videocommon.** { *; }
-keep class com.mbridge.videocommon.download.** { *; }

# 保持 IronSource SDK 类
-keep class com.ironsource.sdk.** { *; }
-keep class com.ironsource.mediationsdk.** { *; }

# 保持 InMobi SDK 类
-keep class com.inmobi.** { *; }
-keep class com.inmobi.ads.** { *; }

# 保持 TopOn SDK 类
-keep class com.anythink.** { *; }
-keep class com.anythink.core.** { *; }
-keep class com.anythink.network.** { *; }

# 保持 AdMob SDK 类
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# 保持 Facebook SDK 类
-keep class com.facebook.ads.** { *; }
-keep class com.facebook.audiencenetwork.** { *; }

# 保持 Vungle SDK 类
-keep class com.vungle.** { *; }
-keep class com.vungle.warren.** { *; }

# 保持 Chartboost SDK 类
-keep class com.chartboost.sdk.** { *; }

# 保持 Unity Ads SDK 类
-keep class com.unity3d.ads.** { *; }

# 保持 Fyber SDK 类
-keep class com.fyber.** { *; }

# 保持 Mahimeta SDK 类
-keep class com.mahimeta.** { *; }

# 保持 Bigo Ads SDK 类
-keep class com.bigo.** { *; }

# 保持 JSON 类
-keep class org.json.** { *; }

# 保持 Glide 类
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# 保持注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# 保持资源
-keep class **.R$* {
    public static <fields>;
}

# 保持序列化
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
} 