package com.ctrip.apollo.biz.aop;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Aspect
@Component
public class RepositoryAspect {

  @Pointcut("execution(public * org.springframework.data.repository.Repository+.*(..))")
  public void anyRepositoryMethod() {
  }

  @Around("anyRepositoryMethod()")
  public Object invokeWithCatTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
    String name =
        joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature()
            .getName();
    Transaction catTransaction = Cat.newTransaction("SQL", name);
    try {
      Object result = joinPoint.proceed();
      catTransaction.setStatus(Message.SUCCESS);
      return result;
    } catch (Throwable ex) {
      catTransaction.setStatus(ex);
      throw ex;
    } finally {
      catTransaction.complete();
    }
  }
}
