-dontobfuscate
-dontoptimize

-dontwarn org.codehaus.groovy.**
-dontwarn groovy**

#todo rethink this blanket exclusion
-keep class org.codehaus.groovy.** { *; }

-keepnames class com.levelup.http.okhttp.** { *; }
-keepnames interface com.levelup.http.okhttp.** { *; }

-keepnames class com.squareup.okhttp.** { *; }
-keepnames interface com.squareup.okhttp.** { *; }


-keepclassmembers class ** implements org.codehaus.groovy.reflection.GroovyClassValue* {*;}
#todo rething this blanket exclusion
-keepclassmembers class org.codehaus.groovy.** {*;}
-keepclassmembers class ** implements org.codehaus.groovy.runtime.GeneratedClosure {*;}


#Keep the annotated things annotated
-keepattributes *Annotation*

#Keep the dagger annotation classes themselves
-keep @interface dagger.*,javax.inject.*

#Keep classes annotated with @Module
-keepnames @dagger.Module class *

#-Keep the the fields annotated with @Inject of any class that is not deleted.
-keepclassmembers class * {
  @javax.inject.* <fields>;

}

#-Keep the names of classes that have fields annotated with @Inject and the fields themselves.
-keepclasseswithmembernames class * {
  @javax.inject.* <fields>;

}

# Keep the generated classes by dagger-compile
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection



-ignorewarnings