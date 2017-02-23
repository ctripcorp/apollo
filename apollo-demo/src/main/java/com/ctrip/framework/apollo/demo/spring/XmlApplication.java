package com.ctrip.framework.apollo.demo.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Scanner;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class XmlApplication {
  public static void main(String[] args) {
    new ClassPathXmlApplicationContext("spring.xml");
    onKeyExit();
  }

  private static void onKeyExit() {
    System.out.println("Press Enter to exit...");
    new Scanner(System.in).nextLine();
  }
}
