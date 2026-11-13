package es.nachobrito.sdd.example.adapter.out.security;

import es.nachobrito.sdd.example.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import es.nachobrito.sdd.example.application.port.in.ListOrdersUseCase.ListOrdersQuery;
import es.nachobrito.sdd.example.application.port.in.ListProductsUseCase.ListProductsQuery;
import es.nachobrito.sdd.example.application.port.out.CallerContextPort;
import es.nachobrito.sdd.example.application.port.out.CallerContextPort.CallerContext;
import es.nachobrito.sdd.example.application.security.AuthorizationPolicy;
import es.nachobrito.sdd.example.application.security.AuthorizePolicy;
import es.nachobrito.sdd.example.domain.model.AuthorizationException;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import es.nachobrito.sdd.example.domain.model.Role;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * Micronaut AOP interceptor that enforces declarative authorization policies.
 */
@Singleton
@InterceptorBean(AuthorizePolicy.class)
public class AuthorizationInterceptor implements MethodInterceptor<Object, Object> {

  private final CallerContextPort callerContextPort;

  /**
   * Constructs the interceptor with the caller context port.
   *
   * @param callerContextPort the caller context port
   */
  public AuthorizationInterceptor(CallerContextPort callerContextPort) {
    this.callerContextPort = callerContextPort;
  }

  @Override
  public Object intercept(MethodInvocationContext<Object, Object> context) {
    AuthorizationPolicy policy =
        context.enumValue(AuthorizePolicy.class, AuthorizationPolicy.class).orElse(null);
    Optional<CallerContext> callerOpt = callerContextPort.currentCaller();
    if (policy == null || callerOpt.isEmpty()) {
      return context.proceed();
    }
    CallerContext caller = callerOpt.get();
    applyBeforePolicy(policy, caller, context.getParameterValues());
    Object result = context.proceed();
    validateAfterPolicy(policy, caller, result);
    return result;
  }

  private static void applyBeforePolicy(
      AuthorizationPolicy policy, CallerContext caller, Object... args) {
    if (args.length == 0) {
      return;
    }
    switch (policy) {
      case PRODUCT_LIST -> scopeProductList(caller, args);
      case ORDER_LIST -> scopeOrderList(caller, args);
      case ORDER_CREATE -> scopeOrderCreate(caller, args);
      default -> {
        // Read policies validate after execution.
      }
    }
  }

  private static void scopeProductList(CallerContext caller, Object... args) {
    if (!caller.hasRole(Role.CATALOG) && args[0] instanceof ListProductsQuery query) {
      args[0] =
          new ListProductsQuery(
              query.sku(),
              query.nameRegex(),
              ProductStatus.ACTIVE,
              query.sortField(),
              query.direction());
    }
  }

  private static void scopeOrderList(CallerContext caller, Object... args) {
    if (!caller.hasRole(Role.ADMIN) && args[0] instanceof ListOrdersQuery query) {
      args[0] =
          new ListOrdersQuery(
              caller.userId(),
              query.status(),
              query.customerEmail(),
              query.startDate(),
              query.endDate(),
              query.sortField(),
              query.direction());
    }
  }

  private static void scopeOrderCreate(CallerContext caller, Object... args) {
    if (args[0] instanceof CreateOrderCommand cmd) {
      args[0] =
          new CreateOrderCommand(
              cmd.id(),
              caller.userId(),
              cmd.customerName(),
              cmd.customerEmail(),
              cmd.date(),
              cmd.status(),
              cmd.currency(),
              cmd.deliveryAddress(),
              cmd.invoiceAddress(),
              cmd.orderLines());
    }
  }

  private static void validateAfterPolicy(
      AuthorizationPolicy policy, CallerContext caller, Object result) {
    switch (policy) {
      case PRODUCT_READ -> validateProductRead(caller, result);
      case ORDER_READ -> validateOrderRead(caller, result);
      default -> {
        // List and create policies are enforced before execution.
      }
    }
  }

  private static void validateProductRead(CallerContext caller, Object result) {
    if (!caller.hasRole(Role.CATALOG)
        && result instanceof Product product
        && product.status() != ProductStatus.ACTIVE) {
      throw new AuthorizationException("Access denied to non-active product: " + product.id());
    }
  }

  private static void validateOrderRead(CallerContext caller, Object result) {
    if (!caller.hasRole(Role.ADMIN)
        && result instanceof Order order
        && !caller.userId().equals(order.userId())) {
      throw new AuthorizationException("Access denied to order: " + order.id());
    }
  }
}

