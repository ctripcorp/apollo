package com.ctrip.framework.apollo.common.utils;

import com.ctrip.framework.apollo.common.exception.BeanUtilsException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

/**
 * Bean工具类
 */
public class BeanUtils {

  private BeanUtils() {
  }

  /**
   * 批量转换,将类型转换为T
   * <pre>
   *     List<UserBean> userBeans = userDao.queryUsers();
   *     List<UserDTO> userDTOs = BeanUtil.batchTransform(UserDTO.class, userBeans);
   * </pre>
   *
   * @param clazz   class对象
   * @param srcList 源列表
   * @param <T>     转换后的数据类型
   * @return 转换后的数据类型列表
   */
  public static <T> List<T> batchTransform(final Class<T> clazz, List<?> srcList) {
    if (CollectionUtils.isEmpty(srcList)) {
      return Collections.emptyList();
    }

    List<T> result = new ArrayList<>(srcList.size());
    for (Object srcObject : srcList) {
      result.add(transform(clazz, srcObject));
    }
    return result;
  }

  /**
   * 封装{@link org.springframework.beans.BeanUtils#copyProperties(Object, Object)}，惯用与直接将转换结果返回
   *
   * <pre>
   *      UserBean userBean = new UserBean("username");
   *      return BeanUtil.transform(UserDTO.class, userBean);
   * </pre>
   *
   * @param clazz class对象
   * @param src   源对象
   * @param <T>   转换后的数据类型
   * @return 转换后的数据类型
   */
  public static <T> T transform(Class<T> clazz, Object src) {
    if (src == null) {
      return null;
    }
    T instance;
    try {
      instance = clazz.newInstance();
    } catch (Exception e) {
      throw new BeanUtilsException(e);
    }
    org.springframework.beans.BeanUtils.copyProperties(src, instance, getNullPropertyNames(src));
    return instance;
  }

  /**
   * 获取NULL属性的名称数组
   *
   * @param source 源
   * @return 字段名称数组
   */
  private static String[] getNullPropertyNames(Object source) {
    final BeanWrapper src = new BeanWrapperImpl(source);
    PropertyDescriptor[] pds = src.getPropertyDescriptors();

    Set<String> emptyNames = new HashSet<>();
    // 添加为空的字段名
    for (PropertyDescriptor pd : pds) {
      Object srcValue = src.getPropertyValue(pd.getName());
      if (srcValue == null) {
        emptyNames.add(pd.getName());
      }
    }
    String[] result = new String[emptyNames.size()];
    return emptyNames.toArray(result);
  }

  /**
   * 用于将一个列表转换为列表中的对象的某个属性映射到列表中的对象
   *
   * <pre>
   *      List<UserDTO> userList = userService.queryUsers();
   *      Map<Integer, userDTO> userIdToUser = BeanUtil.mapByKey("userId", userList);
   * </pre>
   *
   * @param key  属性名
   * @param list 源列表
   * @param <K>  字段名
   * @param <V>  字段值
   * @return Map对象
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> mapByKey(String key, List<?> list) {
    Map<K, V> map = new HashMap<>();
    if (CollectionUtils.isEmpty(list)) {
      return map;
    }
    try {
      Class<?> clazz = list.get(0).getClass();
      Field field = deepFindField(clazz, key);
      if (field == null) {
        throw new IllegalArgumentException("Could not find the key");
      }
      field.setAccessible(true);
      for (Object o : list) {
        map.put((K) field.get(o), (V) o);
      }
    } catch (Exception e) {
      throw new BeanUtilsException(e);
    }
    return map;
  }

  /**
   * 根据列表里面的属性聚合
   *
   * <pre>
   *       List<ShopDTO> shopList = shopService.queryShops();
   *       Map<Integer, List<ShopDTO>> city2Shops = BeanUtil.aggByKeyToList("cityId", shopList);
   * </pre>
   *
   * @param key  字段名
   * @param list 列表
   * @param <K>  字段名
   * @param <V>  字段值
   * @return Map对象
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, List<V>> aggByKeyToList(String key, List<?> list) {
    Map<K, List<V>> map = new HashMap<>();
    // 防止外面传入空list
    if (CollectionUtils.isEmpty(list)) {
      return map;
    }
    try {
      Class<?> clazz = list.get(0).getClass();
      Field field = deepFindField(clazz, key);
      if (field == null) {
        throw new IllegalArgumentException("Could not find the key");
      }
      field.setAccessible(true);
      for (Object o : list) {
        K k = (K) field.get(o);
        map.computeIfAbsent(k, k1 -> new ArrayList<>());
        map.get(k).add((V) o);
      }
    } catch (Exception e) {
      throw new BeanUtilsException(e);
    }
    return map;
  }

  /**
   * 用于将一个对象的列表转换为列表中对象的属性集合
   *
   * <pre>
   *     List<UserDTO> userList = userService.queryUsers();
   *     Set<Integer> userIds = BeanUtil.toPropertySet("userId", userList);
   * </pre>
   *
   * @param key  字段名
   * @param list 列表
   * @param <K>  转换后的数据类型
   * @return Set列表
   */
  @SuppressWarnings("unchecked")
  public static <K> Set<K> toPropertySet(String key, List<?> list) {
    Set<K> set = new HashSet<>();
    if (CollectionUtils.isEmpty(list)) {// 防止外面传入空list
      return set;
    }
    try {
      Class<?> clazz = list.get(0).getClass();
      Field field = deepFindField(clazz, key);
      if (field == null) {
        throw new IllegalArgumentException("Could not find the key");
      }
      field.setAccessible(true);
      for (Object o : list) {
        set.add((K) field.get(o));
      }
    } catch (Exception e) {
      throw new BeanUtilsException(e);
    }
    return set;
  }

  /**
   * 深度查询字段
   *
   * @param clazz 字段类型
   * @param key   字段名称
   * @return 字段对象信息
   */
  private static Field deepFindField(Class<?> clazz, String key) {
    Field field = null;
    while (!clazz.getName().equals(Object.class.getName())) {
      try {
        field = clazz.getDeclaredField(key);
        if (field != null) {
          break;
        }
      } catch (Exception e) {
        clazz = clazz.getSuperclass();
      }
    }
    return field;
  }

  /**
   * 获取某个对象的某属性
   *
   * @param obj       对象
   * @param fieldName 字段名
   * @return 字段对象
   */
  public static Object getProperty(Object obj, String fieldName) {
    try {
      Field field = deepFindField(obj.getClass(), fieldName);
      if (field != null) {
        field.setAccessible(true);
        return field.get(obj);
      }
    } catch (Exception e) {
      throw new BeanUtilsException(e);
    }
    return null;
  }

  /**
   * 设置某个对象的某个属性
   *
   * @param obj       对象
   * @param fieldName 字段名
   * @param value     值
   */
  public static void setProperty(Object obj, String fieldName, Object value) {
    try {
      Field field = deepFindField(obj.getClass(), fieldName);
      if (field != null) {
        field.setAccessible(true);
        field.set(obj, value);
      }
    } catch (Exception e) {
      throw new BeanUtilsException(e);
    }
  }

  /**
   * 拷贝数据
   *
   * @param source           源对象
   * @param target           目标对象
   * @param ignoreProperties 忽略的属性
   */
  public static void copyProperties(Object source, Object target, String... ignoreProperties) {
    org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
  }

  /**
   * 拷贝实体属性，这将会忽略<em>BaseEntity</em>字段
   *
   * @param source 源对象
   * @param target 目标对象
   */
  public static void copyEntityProperties(Object source, Object target) {
    org.springframework.beans.BeanUtils.copyProperties(source, target, COPY_IGNORED_PROPERTIES);
  }

  /**
   * 拷贝忽略的属性.
   */
  private static final String[] COPY_IGNORED_PROPERTIES = {"id", "dataChangeCreatedBy",
      "dataChangeCreatedTime", "dataChangeLastModifiedTime"};
}
