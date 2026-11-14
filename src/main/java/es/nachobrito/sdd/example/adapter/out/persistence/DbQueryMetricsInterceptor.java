package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * Micronaut AOP interceptor that records database query execution duration.
 */
@Singleton
@InterceptorBean(DbQueryMetrics.class)
public class DbQueryMetricsInterceptor implements MethodInterceptor<Object, Object> {

  private static final String METRIC_NAME = "db.query.duration";

  private final Provider<MeterRegistry> meterRegistryProvider;

  /**
   * Constructs the interceptor with the Micrometer meter registry provider.
   *
   * @param meterRegistryProvider the meter registry provider
   */
  public DbQueryMetricsInterceptor(Provider<MeterRegistry> meterRegistryProvider) {
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
      sample.stop(
          Timer.builder(METRIC_NAME)
              .description("Database persistence operation duration")
              .tag("adapter", context.getDeclaringType().getSimpleName())
              .tag("method", context.getMethodName())
              .tag("outcome", completed ? "success" : "error")
              .register(meterRegistry));
    }
  }
}
