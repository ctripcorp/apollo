package com.ctrip.framework.foundation.internals;

import org.junit.Test;

import java.util.ServiceConfigurationError;

import static org.junit.Assert.assertTrue;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ServiceBootstrapTest {
  @Test
  public void loadFirstSuccessfully() throws Exception {
    Interface1 service = ServiceBootstrap.loadFirst(Interface1.class);
    assertTrue(service instanceof Interface1Impl);
  }

  @Test(expected = IllegalStateException.class)
  public void loadFirstWithNoServiceFileDefined() throws Exception {
    ServiceBootstrap.loadFirst(Interface2.class);
  }

  @Test(expected = IllegalStateException.class)
  public void loadFirstWithServiceFileButNoServiceImpl() throws Exception {
    ServiceBootstrap.loadFirst(Interface3.class);
  }

  @Test(expected = ServiceConfigurationError.class)
  public void loadFirstWithWrongServiceImpl() throws Exception {
    ServiceBootstrap.loadFirst(Interface4.class);
  }

  @Test(expected = ServiceConfigurationError.class)
  public void loadFirstWithServiceImplNotExists() throws Exception {
    ServiceBootstrap.loadFirst(Interface5.class);
  }

  private interface Interface1 {
  }

  public static class Interface1Impl implements Interface1 {
  }

  private interface Interface2 {
  }

  private interface Interface3 {
  }

  private interface Interface4 {
  }

  private interface Interface5 {
  }
}
