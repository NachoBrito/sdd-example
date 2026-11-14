package es.nachobrito.sdd.example.adapter.out.smtp;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * Micronaut AOP interceptor that records SMTP delivery counts and duration.
 */
@Singleton
@InterceptorBean(SmtpMetrics.class)
public class SmtpMetricsInterceptor implements MethodInterceptor<Object, Object> {

  private static final String COUNTER_NAME = "smtp.messages";
  private static final String TIMER_NAME = "smtp.send.duration";

  private final Provider<MeterRegistry> meterRegistryProvider;

  /**
   * Constructs the interceptor with the Micrometer meter registry provider.
   *
   * @param meterRegistryProvider the meter registry provider
   */
  public SmtpMetricsInterceptor(Provider<MeterRegistry> meterRegistryProvider) {
    this.meterRegistryProvider = meterRegistryProvider;
  }

  @Override
  public Object intercept(MethodInvocationContext<Object, Object> context) {
    MeterRegistry meterRegistry = meterRegistryProvider.get();
    Timer.Sample sample = Timer.start(meterRegistry);
    boolean completed = false;
    try {
      Object result = context.proceed();
      completed = true;
      return result;
    } finally {
      String outcome = completed ? "sent" : "failed";
      meterRegistry.counter(COUNTER_NAME, "outcome", outcome).increment();
      sample.stop(
          Timer.builder(TIMER_NAME)
              .description("SMTP email send duration")
              .tag("outcome", outcome)
              .register(meterRegistry));
    }
  }
}
