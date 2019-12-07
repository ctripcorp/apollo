package com.ctrip.framework.apollo.util.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DurationParserTest {
  private Parsers.DurationParser durationParser = Parsers.forDuration();

  @Test
  public void testParseMilliSeconds() throws Exception {
    String text = "345MS";
    long expected = 345;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseMilliSecondsWithNoSuffix() throws Exception {
    String text = "123";
    long expected = 123;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseSeconds() throws Exception {
    String text = "20S";
    long expected = 20 * 1000;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseMinutes() throws Exception {
    String text = "15M";
    long expected = 15 * 60 * 1000;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseHours() throws Exception {
    String text = "10H";
    long expected = 10 * 3600 * 1000;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseDays() throws Exception {
    String text = "2D";
    long expected = 2 * 24 * 3600 * 1000;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseFullText() throws Exception {
    String text = "2D3H4M5S123MS";
    long expected = 2 * 24 * 3600 * 1000 + 3 * 3600 * 1000 + 4 * 60 * 1000 + 5 * 1000 + 123;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseFullTextWithLowerCase() throws Exception {
    String text = "2d3h4m5s123ms";
    long expected = 2 * 24 * 3600 * 1000 + 3 * 3600 * 1000 + 4 * 60 * 1000 + 5 * 1000 + 123;

    checkParseToMillis(expected, text);
  }

  @Test
  public void testParseFullTextWithNoMS() throws Exception {
    String text = "2D3H4M5S123";
    long expected = 2 * 24 * 3600 * 1000 + 3 * 3600 * 1000 + 4 * 60 * 1000 + 5 * 1000 + 123;

    checkParseToMillis(expected, text);
  }

  @Test(expected = ParserException.class)
  public void testParseException() throws Exception {
    String text = "someInvalidText";

    durationParser.parseToMillis(text);
  }

  private void checkParseToMillis(long expected, String text) throws Exception {
    assertEquals(expected, durationParser.parseToMillis(text));
  }
}