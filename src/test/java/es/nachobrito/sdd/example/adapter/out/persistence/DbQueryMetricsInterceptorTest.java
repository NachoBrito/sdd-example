package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micronaut.aop.MethodInvocationContext;
import org.junit.jupiter.api.Test;

class DbQueryMetricsInterceptorTest {

  @Test
  void recordsSuccessDurationWhenMethodSucceeds() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    MethodInvocationContext<Object, Object> context = mock();
    doReturn(OrderPersistenceAdapter.class).when(context).getDeclaringType();
    when(context.getMethodName()).thenReturn("findById");
    when(context.proceed()).thenReturn("result");

    DbQueryMetricsInterceptor interceptor = new DbQueryMetricsInterceptor(() -> registry);
    Object result = interceptor.intercept(context);

    assertEquals("result", result);
    Timer timer =
        registry
            .find("db.query.duration")
            .tag("adapter", "OrderPersistenceAdapter")
            .tag("method", "findById")
            .tag("outcome", "success")
            .timer();
    assertNotNull(timer);
    assertEquals(1L, timer.count());
  }

  @Test
  void recordsErrorDurationWhenMethodThrows() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    MethodInvocationContext<Object, Object> context = mock();
    doReturn(ProductPersistenceAdapter.class).when(context).getDeclaringType();
    when(context.getMethodName()).thenReturn("save");
    when(context.proceed()).thenThrow(new IllegalStateException("db failure"));

    DbQueryMetricsInterceptor interceptor = new DbQueryMetricsInterceptor(() -> registry);
    assertThrows(IllegalStateException.class, () -> interceptor.intercept(context));

    Timer timer =
        registry
            .find("db.query.duration")
            .tag("adapter", "ProductPersistenceAdapter")
            .tag("method", "save")
            .tag("outcome", "error")
            .timer();
    assertNotNull(timer);
    assertEquals(1L, timer.count());
  }
}
