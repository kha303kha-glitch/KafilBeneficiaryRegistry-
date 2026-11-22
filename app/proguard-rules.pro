# Keep Room entities & DAOs
-keep class com.kafil.beneficiaries.data.local.** { *; }

# Keep Apache POI & XMLBeans classes used via reflection
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.schemas.** { *; }
-keep class com.fasterxml.aalto.** { *; }
-keep class javax.xml.stream.** { *; }

-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.apache.logging.log4j.**
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.xml.stream.**
