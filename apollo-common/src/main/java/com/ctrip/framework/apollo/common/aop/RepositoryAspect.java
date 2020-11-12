package com.ctrip.framework.apollo.common.aop;

import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Repsitory切面，拦截数据库操作，用于实时监控告警服务
 */
@Aspect
@Component
public class RepositoryAspect {

  /**
   * 拦截前类和子类的所有方法
   */
  // +表示包括当前类和子类子孙类,
  @Pointcut("execution(public * org.springframework.data.repository.Repository+.*(..))")
  public void anyRepositoryMethod() {
  }

  @Around("anyRepositoryMethod()")
  public Object invokeWithCatTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
    // 方法名
    String name = String.format("%s.%s", joinPoint.getSignature().getDeclaringType()
        .getSimpleName(), joinPoint.getSignature().getName());
    // 记录
    Transaction catTransaction = Tracer.newTransaction("SQL", name);
    try {
      Object result = joinPoint.proceed();
      catTransaction.setStatus(Transaction.SUCCESS);
      return result;
    } catch (Throwable ex) {
      catTransaction.setStatus(ex);
      throw ex;
    } finally {
      catTransaction.complete();
    }
  }
}
