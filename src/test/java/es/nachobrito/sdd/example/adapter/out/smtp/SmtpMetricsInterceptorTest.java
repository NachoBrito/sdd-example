package es.nachobrito.sdd.example.adapter.out.smtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micronaut.aop.MethodInvocationContext;
import org.junit.jupiter.api.Test;

class SmtpMetricsInterceptorTest {

  @Test
  void recordsSentMetricsWhenEmailSucceeds() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    SmtpMetricsInterceptor interceptor = new SmtpMetricsInterceptor(() -> registry);
    MethodInvocationContext<Object, Object> context = mock();
    when(context.proceed()).thenReturn(null);

    interceptor.intercept(context);

    Counter counter = registry.find("smtp.messages").tag("outcome", "sent").counter();
    assertNotNull(counter);
    assertEquals(1.0, counter.count());

    Timer timer = registry.find("smtp.send.duration").tag("outcome", "sent").timer();
    assertNotNull(timer);
    assertEquals(1L, timer.count());
  }

  @Test
  void recordsFailedMetricsWhenEmailThrows() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    SmtpMetricsInterceptor interceptor = new SmtpMetricsInterceptor(() -> registry);
    MethodInvocationContext<Object, Object> context = mock();
    when(context.proceed()).thenThrow(new IllegalStateException("smtp unreachable"));

    assertThrows(IllegalStateException.class, () -> interceptor.intercept(context));

    Counter counter = registry.find("smtp.messages").tag("outcome", "failed").counter();
    assertNotNull(counter);
    assertEquals(1.0, counter.count());

    Timer timer = registry.find("smtp.send.duration").tag("outcome", "failed").timer();
    assertNotNull(timer);
    assertEquals(1L, timer.count());
  }
}
