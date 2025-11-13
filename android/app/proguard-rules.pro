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

# --- Capacitor / Cordova ---
-keep class com.getcapacitor.** { *; }
-keep class com.getcapacitor.plugin.** { *; }
-keep class org.apache.cordova.** { *; }
-dontwarn org.apache.cordova.**

# --- AndroidX WebKit / WebView ---
-keep class androidx.webkit.** { *; }
-dontwarn androidx.webkit.**

# --- Google Mobile Ads (AdMob) & UMP ---
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# --- Kotlin metadata (avoid stripping used by reflection) ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# --- Keep classes referenced from AndroidManifest and by reflection ---
-keep class **.MainActivity { *; }
-keepclassmembers class * extends android.app.Activity { *; }
-keep class **.App { *; }

# --- Resources: keep asset paths used by Capacitor ---
-keep class **.R$* { *; }

# --- Capacitor Browser plugin ---
-keep class com.capacitorjs.plugins.browser.** { *; }
-dontwarn com.capacitorjs.plugins.browser.**
