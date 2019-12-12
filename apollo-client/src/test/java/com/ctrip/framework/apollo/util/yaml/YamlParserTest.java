package com.ctrip.framework.apollo.util.yaml;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.junit.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.yaml.snakeyaml.parser.ParserException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class YamlParserTest {

  private YamlParser parser = new YamlParser();

  @Test
  public void testValidCases() throws Exception {
    test("case1.yaml");
    test("case3.yaml");
    test("case4.yaml");
    test("case5.yaml");
    test("case6.yaml");
    test("case7.yaml");
  }

  @Test(expected = ParserException.class)
  public void testcase2() throws Exception {
    testInvalid("case2.yaml");
  }

  @Test(expected = ParserException.class)
  public void testcase8() throws Exception {
    testInvalid("case8.yaml");
  }

  private void test(String caseName) throws Exception {
    File file = new File("src/test/resources/yaml/" + caseName);

    String yamlContent = Files.toString(file, Charsets.UTF_8);

    check(yamlContent);
  }

  private void testInvalid(String caseName) throws Exception {
    File file = new File("src/test/resources/yaml/" + caseName);

    String yamlContent = Files.toString(file, Charsets.UTF_8);

    parser.yamlToProperties(yamlContent);
  }

  private void check(String yamlContent) {
    YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
    yamlPropertiesFactoryBean.setResources(new ByteArrayResource(yamlContent.getBytes()));
    Properties expected = yamlPropertiesFactoryBean.getObject();

    LinkedHashMap map = parser.yamlToProperties(yamlContent);
    Properties actual =new Properties();
    actual.putAll(map);

    assertTrue("expected: " + expected + " actual: " + actual, checkPropertiesEquals(expected, actual));
  }

  private boolean checkPropertiesEquals(Properties expected, Properties actual) {
    if (expected == actual)
      return true;

    if (expected.size() != actual.size())
      return false;

    for (Object key : expected.keySet()) {
      if (!expected.getProperty((String) key).equals(actual.getProperty((String) key))) {
        return false;
      }
    }

    return true;
  }
}
