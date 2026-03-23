# Keep line numbers/source names for crash readability.
-keepattributes SourceFile,LineNumberTable

# Preserve annotation metadata used by AndroidX/Kotlin libraries.
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

# Keep our launcher entry points and core model classes stable.
-keep class com.smartlauncher.ui.** { *; }
-keep class com.smartlauncher.viewmodel.** { *; }
-keep class com.smartlauncher.data.AppItem { *; }
-keep class com.smartlauncher.data.AppCategory { *; }

# Be conservative with the Solana Mobile Wallet Adapter until we verify
# a minified release end-to-end on device.
-keep class com.solana.mobilewalletadapter.** { *; }
-dontwarn com.solana.mobilewalletadapter.**
