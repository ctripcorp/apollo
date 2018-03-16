package com.ctrip.framework.apollo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ThreadPoolUtils for concurrent test
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ThreadPoolUtils {

  /**
   * Execute the task concurrently
   * 
   * @param threadNum The number of thread to execute the task
   * @param executeNum The number of execution
   * @param task The task to execute
   */
  public static void concurrentExecute(int threadNum, int executeNum, final Runnable task) {

    ExecutorService executor = Executors.newFixedThreadPool(threadNum);
    try {
      List<Future<?>> futureList = new ArrayList<>();
      for (int i = 0; i < executeNum; i++) {
        Future<?> future = executor.submit(task);
        futureList.add(future);
      }

      for (Future<?> future : futureList) {
        try {
          future.get();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    } finally {
      executor.shutdown();
    }
  }
}
