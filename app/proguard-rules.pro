-dontwarn kotlin.Unit
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes Signature
-keepclassmembers class kr.co.hongstudio.sitezip.data** {
    *;
}

# Moshi
-keep @com.squareup.moshi.JsonQualifier interface *
-keep class kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoader
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

# Retrofit2
-dontwarn retrofit2.KotlinExtensions
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# FireBase
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class com.crashlytics** { *; }
-dontwarn com.crashlytics.**

# kakaomap
-keep class net.daum** {*;}
-keep class android.opengl** {*;}
-keep class com.kakao.util.maps.helper** {*;}
-keepattributes Signature
-keepclassmembers class * {
    public static <fields>;
    public *;
}