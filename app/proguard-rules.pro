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

-keepattributes Signature
-keepattributes *Annotation*

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class com.cmtsbsnl.cnmc.** { *; }

-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn javax.xml.bind.DatatypeConverter

-dontwarn org.kxml2.io.KXmlParser,org.kxml2.io.KXmlSerializer


#-dontwarn org.kxml2.io.KXmlParser
#-dontwarn org.kxml2.io.KXmlSerializer

-keep class android.content.res.XmlResourceParser { *; }
-keep class org.xmlpull.v1.XmlPullParser { *; }

-keep class org.xmlpull.v1.** { *; }
-keep class android.content.res.XmlResourceParser { *; }
-keep class org.kxml2.io.** { *; }