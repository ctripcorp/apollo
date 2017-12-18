package com.ctrip.framework.apollo;

import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Config {
  /**
   * Return the property value with the given key, or {@code defaultValue} if the key doesn't exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value
   */
  public String getProperty(String key, String defaultValue);

  /**
   * Return the integer property value with the given key, or {@code defaultValue} if the key
   * doesn't exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value as integer
   */
  public Integer getIntProperty(String key, Integer defaultValue);

  /**
   * Return the long property value with the given key, or {@code defaultValue} if the key doesn't
   * exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value as long
   */
  public Long getLongProperty(String key, Long defaultValue);

  /**
   * Return the short property value with the given key, or {@code defaultValue} if the key doesn't
   * exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value as short
   */
  public Short getShortProperty(String key, Short defaultValue);

  /**
   * Return the float property value with the given key, or {@code defaultValue} if the key doesn't
   * exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value as float
   */
  public Float getFloatProperty(String key, Float defaultValue);

  /**
   * Return the double property value with the given key, or {@code defaultValue} if the key doesn't
   * exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value as double
   */
  public Double getDoubleProperty(String key, Double defaultValue);

  /**
   * Return the byte property value with the given key, or {@code defaultValue} if the key doesn't
   * exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value as byte
   */
  public Byte getByteProperty(String key, Byte defaultValue);

  /**
   * Return the boolean property value with the given key, or {@code defaultValue} if the key
   * doesn't exist.
   *
   * @param key          the property name
   * @param defaultValue the default value when key is not found or any error occurred
   * @return the property value as boolean
   */
  public Boolean getBooleanProperty(String key, Boolean defaultValue);

  /**
   * Return the array property value with the given key, or {@code defaultValue} if the key doesn't exist.
   *
   * @param key          the property name
   * @param delimiter    the delimiter regex
   * @param defaultValue the default value when key is not found or any error occurred
   */
  public String[] getArrayProperty(String key, String delimiter, String[] defaultValue);

  /**
   * Return the Date property value with the given name, or {@code defaultValue} if the name doesn't exist.
   * Will try to parse the date with Locale.US and formats as follows: yyyy-MM-dd HH:mm:ss.SSS,
   * yyyy-MM-dd HH:mm:ss and yyyy-MM-dd
   *
   * @param key          the property name
   * @param defaultValue the default value when name is not found or any error occurred
   * @return the property value
   */
  public Date getDateProperty(String key, Date defaultValue);

  /**
   * Return the Date property value with the given name, or {@code defaultValue} if the name doesn't exist.
   * Will parse the date with the format specified and Locale.US
   *
   * @param key          the property name
   * @param format       the date format, see {@link java.text.SimpleDateFormat} for more
   *                     information
   * @param defaultValue the default value when name is not found or any error occurred
   * @return the property value
   */
  public Date getDateProperty(String key, String format, Date defaultValue);

  /**
   * Return the Date property value with the given name, or {@code defaultValue} if the name doesn't exist.
   *
   * @param key          the property name
   * @param format       the date format, see {@link java.text.SimpleDateFormat} for more
   *                     information
   * @param locale       the locale to use
   * @param defaultValue the default value when name is not found or any error occurred
   * @return the property value
   */
  public Date getDateProperty(String key, String format, Locale locale, Date defaultValue);

  /**
   * Return the Enum property value with the given key, or {@code defaultValue} if the key doesn't exist.
   *
   * @param key          the property name
   * @param enumType     the enum class
   * @param defaultValue the default value when key is not found or any error occurred
   * @param <T>          the enum
   * @return the property value
   */
  public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue);

  /**
   * Return the duration property value(in milliseconds) with the given name, or {@code
   * defaultValue} if the name doesn't exist. Please note the format should comply with the follow
   * example (case insensitive). Examples:
   * <pre>
   *    "123MS"          -- parses as "123 milliseconds"
   *    "20S"            -- parses as "20 seconds"
   *    "15M"            -- parses as "15 minutes" (where a minute is 60 seconds)
   *    "10H"            -- parses as "10 hours" (where an hour is 3600 seconds)
   *    "2D"             -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
   *    "2D3H4M5S123MS"  -- parses as "2 days, 3 hours, 4 minutes, 5 seconds and 123 milliseconds"
   * </pre>
   *
   * @param key          the property name
   * @param defaultValue the default value when name is not found or any error occurred
   * @return the parsed property value(in milliseconds)
   */
  public long getDurationProperty(String key, long defaultValue);

  /**
   * Add change listener to this config instance.
   *
   * @param listener the config change listener
   */
  public void addChangeListener(ConfigChangeListener listener);

  /**
   * Add auto change listener to this config instance.
   * @param listener the config change listener
   */
  public void addAutoChangeListener(AutoConfigChangeListener listener);

  /**
   * Return a set of the property names
   *
   * @return the property names
   */
  public Set<String> getPropertyNames();

}
