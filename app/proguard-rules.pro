# Proguard rules for AleClip Android
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep data models for JSON serialization
-keep class com.themanoftalent.aleclip.model.** { *; }
