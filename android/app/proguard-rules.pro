# ProGuard / R8 rules.
# Defaults in proguard-android-optimize.txt cover most cases.

# kotlinx.serialization — keep generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.yortch.confirmationsaints.**$$serializer { *; }
-keepclassmembers class com.yortch.confirmationsaints.** {
    *** Companion;
}
-keepclasseswithmembers class com.yortch.confirmationsaints.** {
    kotlinx.serialization.KSerializer serializer(...);
}
