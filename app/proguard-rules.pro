# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontobfuscate
-verbose

# Keep all public classes and methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# TensorFlow Lite
-keep class org.tensorflow.** { *; }

# Room
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
