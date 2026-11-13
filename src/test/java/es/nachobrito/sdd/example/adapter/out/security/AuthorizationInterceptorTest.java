package es.nachobrito.sdd.example.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import es.nachobrito.sdd.example.application.port.in.ListOrdersUseCase.ListOrdersQuery;
import es.nachobrito.sdd.example.application.port.in.ListProductsUseCase.ListProductsQuery;
import es.nachobrito.sdd.example.application.port.out.CallerContextPort;
import es.nachobrito.sdd.example.application.port.out.CallerContextPort.CallerContext;
import es.nachobrito.sdd.example.application.security.AuthorizationPolicy;
import es.nachobrito.sdd.example.application.security.AuthorizePolicy;
import es.nachobrito.sdd.example.domain.model.AuthorizationException;
import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import es.nachobrito.sdd.example.domain.model.Role;
import io.micronaut.aop.MethodInvocationContext;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthorizationInterceptorTest {

  private static final Instant NOW = Instant.parse("2026-11-13T12:00:00Z");

  private CallerContextPort callerContextPort;
  private AuthorizationInterceptor interceptor;

  @BeforeEach
  void setUp() {
    callerContextPort = mock(CallerContextPort.class);
    interceptor = new AuthorizationInterceptor(callerContextPort);
  }

  @Test
  void proceedsUnmodifiedWhenPolicyOrCallerMissing() {
    MethodInvocationContext<Object, Object> ctx = mockContext(null, new Object[0], "ok");
    when(callerContextPort.currentCaller()).thenReturn(Optional.empty());
    assertEquals("ok", interceptor.intercept(ctx));

    MethodInvocationContext<Object, Object> withPolicy =
        mockContext(AuthorizationPolicy.PRODUCT_READ, new Object[0], "ok");
    assertEquals("ok", interceptor.intercept(withPolicy));

    when(callerContextPort.currentCaller())
        .thenReturn(Optional.of(new CallerContext(UUID.randomUUID(), Set.of(Role.CUSTOMER))));
    assertEquals("ok", interceptor.intercept(ctx));
    assertEquals(
        "ok",
        interceptor.intercept(
            mockContext(AuthorizationPolicy.PRODUCT_LIST, new Object[0], "ok")));
    assertEquals(
        "ok",
        interceptor.intercept(
            mockContext(AuthorizationPolicy.PRODUCT_LIST, new Object[] {"other"}, "ok")));
    assertEquals(
        "ok",
        interceptor.intercept(
            mockContext(AuthorizationPolicy.ORDER_LIST, new Object[] {"other"}, "ok")));
    assertEquals(
        "ok",
        interceptor.intercept(
            mockContext(AuthorizationPolicy.ORDER_CREATE, new Object[] {"other"}, "ok")));
    assertEquals(
        "other",
        interceptor.intercept(
            mockContext(AuthorizationPolicy.PRODUCT_READ, new Object[0], "other")));
    assertEquals(
        "other",
        interceptor.intercept(
            mockContext(AuthorizationPolicy.ORDER_READ, new Object[0], "other")));
  }

  @Test
  void enforcesProductPolicies() {
    UUID userId = UUID.randomUUID();
    CallerContext customer = new CallerContext(userId, Set.of(Role.CUSTOMER));
    final CallerContext catalog = new CallerContext(userId, Set.of(Role.CATALOG));

    ListProductsQuery query =
        new ListProductsQuery(null, null, ProductStatus.DRAFT, "name", "asc");
    Object[] customerArgs = {query};
    MethodInvocationContext<Object, Object> listCtx =
        mockContext(AuthorizationPolicy.PRODUCT_LIST, customerArgs, List.of());
    when(callerContextPort.currentCaller()).thenReturn(Optional.of(customer));
    interceptor.intercept(listCtx);
    assertEquals(ProductStatus.ACTIVE, ((ListProductsQuery) customerArgs[0]).status());

    Object[] catalogArgs = {query};
    MethodInvocationContext<Object, Object> catalogListCtx =
        mockContext(AuthorizationPolicy.PRODUCT_LIST, catalogArgs, List.of());
    when(callerContextPort.currentCaller()).thenReturn(Optional.of(catalog));
    interceptor.intercept(catalogListCtx);
    assertEquals(ProductStatus.DRAFT, ((ListProductsQuery) catalogArgs[0]).status());

    Product draft =
        Product.create(
            UUID.randomUUID(), "SKU-1", "Name", "Desc", 100, "USD", ProductStatus.DRAFT, NOW);
    Product active =
        Product.create(
            UUID.randomUUID(), "SKU-2", "Name", "Desc", 100, "USD", ProductStatus.ACTIVE, NOW);

    when(callerContextPort.currentCaller()).thenReturn(Optional.of(customer));
    assertThrows(
        AuthorizationException.class,
        () ->
            interceptor.intercept(
                mockContext(AuthorizationPolicy.PRODUCT_READ, new Object[0], draft)));
    assertEquals(
        active,
        interceptor.intercept(
            mockContext(AuthorizationPolicy.PRODUCT_READ, new Object[0], active)));

    when(callerContextPort.currentCaller()).thenReturn(Optional.of(catalog));
    assertEquals(
        draft,
        interceptor.intercept(mockContext(AuthorizationPolicy.PRODUCT_READ, new Object[0], draft)));
  }

  @Test
  void enforcesOrderPolicies() {
    UUID ownerId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    CallerContext customer = new CallerContext(ownerId, Set.of(Role.CUSTOMER));
    final CallerContext admin = new CallerContext(otherId, Set.of(Role.ADMIN));

    ListOrdersQuery query =
        new ListOrdersQuery(otherId, null, null, null, null, "date", "asc");
    Object[] listArgs = {query};
    when(callerContextPort.currentCaller()).thenReturn(Optional.of(customer));
    interceptor.intercept(mockContext(AuthorizationPolicy.ORDER_LIST, listArgs, List.of()));
    assertEquals(ownerId, ((ListOrdersQuery) listArgs[0]).userId());

    Object[] adminListArgs = {query};
    when(callerContextPort.currentCaller()).thenReturn(Optional.of(admin));
    interceptor.intercept(mockContext(AuthorizationPolicy.ORDER_LIST, adminListArgs, List.of()));
    assertEquals(otherId, ((ListOrdersQuery) adminListArgs[0]).userId());

    CreateOrderCommand createCmd =
        new CreateOrderCommand(
            UUID.randomUUID(),
            null,
            "Alice",
            "alice@example.com",
            NOW,
            OrderStatus.PENDING,
            "USD",
            new DeliveryAddress("Alice", "+1", "St", null, "City", "ST", "12345", "US"),
            new InvoiceAddress("Alice", "TAX", "+1", "St", null, "City", "ST", "12345", "US"),
            List.of(new OrderLine(UUID.randomUUID(), 1, 100)));
    Object[] createArgs = {createCmd};
    when(callerContextPort.currentCaller()).thenReturn(Optional.of(customer));
    interceptor.intercept(mockContext(AuthorizationPolicy.ORDER_CREATE, createArgs, null));
    assertEquals(ownerId, ((CreateOrderCommand) createArgs[0]).userId());

    Order ownOrder =
        Order.create(
            new Order.OrderDraft(
                UUID.randomUUID(),
                ownerId,
                "Alice",
                "alice@example.com",
                NOW,
                OrderStatus.PENDING,
                "USD",
                createCmd.deliveryAddress(),
                createCmd.invoiceAddress(),
                createCmd.orderLines()));
    assertEquals(
        ownOrder,
        interceptor.intercept(
            mockContext(AuthorizationPolicy.ORDER_READ, new Object[0], ownOrder)));

    CallerContext otherCustomer = new CallerContext(otherId, Set.of(Role.CUSTOMER));
    when(callerContextPort.currentCaller()).thenReturn(Optional.of(otherCustomer));
    assertThrows(
        AuthorizationException.class,
        () ->
            interceptor.intercept(
                mockContext(AuthorizationPolicy.ORDER_READ, new Object[0], ownOrder)));

    when(callerContextPort.currentCaller()).thenReturn(Optional.of(admin));
    assertEquals(
        ownOrder,
        interceptor.intercept(
            mockContext(AuthorizationPolicy.ORDER_READ, new Object[0], ownOrder)));
  }

  private static MethodInvocationContext<Object, Object> mockContext(
      AuthorizationPolicy policy, Object[] args, Object result) {
    MethodInvocationContext<Object, Object> ctx = mock(MethodInvocationContext.class);
    when(ctx.enumValue(AuthorizePolicy.class, AuthorizationPolicy.class))
        .thenReturn(Optional.ofNullable(policy));
    when(ctx.getParameterValues()).thenReturn(args);
    when(ctx.proceed()).thenReturn(result);
    return ctx;
  }
}
