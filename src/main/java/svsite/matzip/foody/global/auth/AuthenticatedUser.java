package svsite.matzip.foody.global.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import svsite.matzip.foody.global.util.jwt.JwtTokenType;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticatedUser {
  JwtTokenType value() default JwtTokenType.ACCESS;
}

