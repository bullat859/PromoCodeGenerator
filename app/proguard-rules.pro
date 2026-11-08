# Add project specific ProGuard rules here.

-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep Compose Runtime and Compiler primitives
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep our data classes and database helper
-keep class com.m.payra.promo.generator.app.data.** { *; }
-keep class com.m.payra.promo.generator.app.engine.** { *; }
-keep class com.m.payra.promo.generator.app.export.** { *; }
