package com.ctrip.framework.apollo.demo.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Scanner;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class AnnotationApplication {
  public static void main(String[] args) {
    new AnnotationConfigApplicationContext(AnnotationApplication.class.getPackage().getName());
    onKeyExit();
  }

  private static void onKeyExit() {
    System.out.println("Press Enter to exit...");
    new Scanner(System.in).nextLine();
  }
}
