# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.glassywidgets.data.** { *** Companion; }
-keepclasseswithmembers class com.glassywidgets.data.** { kotlinx.serialization.KSerializer serializer(...); }
