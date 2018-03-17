package com.ctrip.framework.apollo.spring.processor;

import org.junit.Assert;
import org.junit.Test;
import com.ctrip.framework.apollo.util.ThreadPoolUtils;

/**
 *
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingCollectorTest {

  @Test
  public void testAntMatch() {

    ValueMappingCollector collector = new ValueMappingAntMatchCollector();

    String parter1 = "propery.*";
    String parter2 = "propery.z?";
    String parter3 = "prop?ry.x*xx";
    final Object[][] datas = {{parter1, "propery.abc.ddd", true}, {parter1, "propery.", true},
        {parter1, "propery.xxx", true}, {parter1, "prop12ery.098762", false},
        {parter2, "propery.zz", true}, {parter2, "propery.zX", true},
        {parter2, "propery.zdd", false}, {parter2, "properyz1", false},
        {parter3, "propZry.xyyyasfdasfasfdyyxx", true}, {parter3, "propQry.xyyyxxxxyyyx", false}};

    long time = testFilter(collector, datas);

    System.out.println("Ant match time: " + time);
  }

  @Test
  public void testRegMatch() {

    ValueMappingCollector collector = new ValueMappingRegMatchCollector();

    String parter1 = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    String parter2 = "^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8])|(19[7]))\\d{8}$";
    String parter3 = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}$";
    final Object[][] datas =
        {{parter1, "zhangxuehaod@163.com", true}, {parter1, "123456789@126.com", true},
            {parter1, "zxh1236549@vip.126.com", true}, {parter1, "123456789@@vip.126.com", false},
            {parter2, "13599998888", true}, {parter2, "13345679876", true},
            {parter2, "135999988881", false}, {parter2, "03345679876", false},
            {parter3, "google.com.cn", true}, {parter3, "baidu.com", true},
            {parter3, "163.com", true}, {parter3, "notadomain-.com", false}};

    long time = testFilter(collector, datas);

    System.out.println("Reg match time: " + time);
  }

  private long testFilter(final ValueMappingCollector collector, final Object[][] datas) {

    Runnable task = new Runnable() {

      @Override
      public void run() {
        for (Object[] d : datas) {
          boolean res = collector.filter((String) d[0], (String) d[1], "");
          Assert.assertEquals(res, (boolean) d[2]);
        }
      }

    };

    // test match
    task.run();

    // test thread safe
    long time = System.currentTimeMillis();
    boolean succ = ThreadPoolUtils.concurrentExecute(32, 100000, task);
    Assert.assertTrue(succ);
    time = System.currentTimeMillis() - time;
    return time;
  }

}
