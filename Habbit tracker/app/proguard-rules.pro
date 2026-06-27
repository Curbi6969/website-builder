# kotlinx.serialization — keep generated serializers for @Serializable models.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.rise.app.**$$serializer { *; }
-keepclassmembers class com.rise.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.rise.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor / supabase-kt ship their own consumer rules; nothing extra needed here.
