package es.nachobrito.sdd.example.application.security;

/**
 * Authorization policies enforced by Micronaut AOP interceptors.
 */
public enum AuthorizationPolicy {
  PRODUCT_READ,
  PRODUCT_LIST,
  ORDER_READ,
  ORDER_LIST,
  ORDER_CREATE
}

