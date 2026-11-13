package es.nachobrito.sdd.example.application.security;

import io.micronaut.aop.Around;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares an authorization policy enforced around an application service method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Around
public @interface AuthorizePolicy {

  /**
   * Returns the authorization policy to enforce.
   *
   * @return the authorization policy
   */
  AuthorizationPolicy value();
}

