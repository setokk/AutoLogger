package org.setokk.atl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AutoLog {
    LogLevel level() default LogLevel.INFO;
    String onEnterMsgPattern() default CLASS_PLACEHOLDER + "->" + METHOD_PLACEHOLDER + " - ENTER";
    String onExitMsgPattern() default CLASS_PLACEHOLDER + "->" + METHOD_PLACEHOLDER + " - LEAVE";
    String[] excludedMethods() default {};
    boolean ignorePrivateMethods() default true;
    boolean debugEnabled() default false;

    String CLASS_PLACEHOLDER = "%CLASS";
    String METHOD_PLACEHOLDER = "%METHOD";
}
