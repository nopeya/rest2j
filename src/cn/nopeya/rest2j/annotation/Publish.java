package cn.nopeya.rest2j.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import cn.nopeya.rest2j.core.HTTP;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Publish {
	HTTP value();
}
