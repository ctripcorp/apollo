package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * properties 配置解析器，普通的属性文件解析器。
 * <p>通过创建新的配置项和删除旧项来更新备注和空配置项。
 * <p>更新通过更新实现的常规key/value项。
 */
@Component("propertyResolver")
public class PropertyResolver implements ConfigTextResolver {

  /**
   * kv分隔器
   */
  private static final String KV_SEPARATOR = "=";
  /**
   * 配置项分隔器
   */
  private static final String ITEM_SEPARATOR = "\n";

  @Override
  public ItemChangeSets resolve(long namespaceId, String configText, List<ItemDTO> baseItems) {
    // 创建 Item Map ，以 lineNum 为 键
    Map<Integer, ItemDTO> oldLineNumMapItem = BeanUtils.mapByKey("lineNum", baseItems);
    // 创建 Item Map ，以 key 为 键
    Map<String, ItemDTO> oldKeyMapItem = BeanUtils.mapByKey("key", baseItems);

    // 删除备注和空白配置项.
    oldKeyMapItem.remove("");

    // 按照拆分 Property 配置
    String[] newItems = configText.split(ITEM_SEPARATOR);

    // 校验是否存在重复配置 Key 。若是，抛出 BadRequestException 异常
    if (isHasRepeatKey(newItems)) {
      throw new BadRequestException("config text has repeat key please check.");
    }

    // 创建 ItemChangeSets 对象，并解析配置文件到 ItemChangeSets 中。
    ItemChangeSets changeSets = new ItemChangeSets();
    // 新的lineNum Map对象,用于删除空白和评论项
    Map<Integer, String> newLineNumMapItem = new HashMap<>();
    int lineCounter = 1;
    for (String newItem : newItems) {
      newItem = newItem.trim();
      newLineNumMapItem.put(lineCounter, newItem);
      // 使用行号，获得已存在的 ItemDTO
      ItemDTO oldItemByLine = oldLineNumMapItem.get(lineCounter);

      // 注释 Item
      if (isCommentItem(newItem)) {
        handleCommentLine(namespaceId, oldItemByLine, newItem, lineCounter, changeSets);
      } else if (isBlankItem(newItem)) {
        // 空白 Item
        handleBlankLine(namespaceId, oldItemByLine, lineCounter, changeSets);

      } else {
        // 普通 Item
        handleNormalLine(namespaceId, oldKeyMapItem, newItem, lineCounter, changeSets);
      }
      // 行号计数 + 1
      lineCounter++;
    }

    // 删除注释和空行配置项
    deleteCommentAndBlankItem(oldLineNumMapItem, newLineNumMapItem, changeSets);
    // 删除普通配置项
    deleteNormalKVItem(oldKeyMapItem, changeSets);
    return changeSets;
  }

  /**
   * 校验是否存在重复配置 Key
   *
   * @param newItems 新的配置项
   * @return true, 抛出 BadRequestException 异常
   */
  private boolean isHasRepeatKey(String[] newItems) {
    Set<String> keys = new HashSet<>();
    // 记录行数，用于报错提示，无业务逻辑需要。
    int lineCounter = 1;
    // 计数
    int keyCount = 0;
    for (String item : newItems) {
      // 排除注释和空行的配置项
      if (!isCommentItem(item) && !isBlankItem(item)) {
        keyCount++;
        String[] kv = parseKeyValueFromItem(item);
        if (kv != null) {
          keys.add(kv[0].toLowerCase());
        } else {
          throw new BadRequestException("line:" + lineCounter + " key value must separate by '='");
        }
      }
      lineCounter++;
    }
    return keyCount > keys.size();
  }

  /**
   * 解析一行，生成 [key, value]
   *
   * @param item 配置项信息
   * @return 生成的 [key, value]数组
   */
  private String[] parseKeyValueFromItem(String item) {
    int kvSeparator = item.indexOf(KV_SEPARATOR);
    // 不存在返回空
    if (kvSeparator == -1) {
      return null;
    }

    //构建kv
    String[] kv = new String[2];
    kv[0] = item.substring(0, kvSeparator).trim();
    kv[1] = item.substring(kvSeparator + 1).trim();
    return kv;
  }

  /**
   * 处理空串（""）配置项
   *
   * @param namespaceId   名称空间id
   * @param oldItemByLine 旧配置项
   * @param newItem       新配置项
   * @param lineCounter   行号
   * @param changeSets    变更配置项列表
   */
  private void handleCommentLine(Long namespaceId, ItemDTO oldItemByLine, String newItem,
      int lineCounter, ItemChangeSets changeSets) {
    String oldComment = oldItemByLine == null ? "" : oldItemByLine.getComment();
    // 创建的空串（""）。通过删除旧空串（""）和创建新空串（""）实现更新空串（""）
    if (!(isCommentItem(oldItemByLine) && newItem.equals(oldComment))) {
      changeSets.addCreateItem(buildCommentItem(0L, namespaceId, newItem, lineCounter));
    }
  }

  /**
   * 处理注释配置项
   *
   * @param namespaceId 名称空间id
   * @param oldItem     旧配置项
   * @param lineCounter 行号
   * @param changeSets  变更配置项列表
   */
  private void handleBlankLine(Long namespaceId, ItemDTO oldItem, int lineCounter,
      ItemChangeSets changeSets) {
    // 创建空行 ItemDTO 到 ItemChangeSets 的新增项，若老的不是空行。另外，更新空行配置，通过删除 + 添加的方式
    if (!isBlankItem(oldItem)) {
      changeSets.addCreateItem(buildBlankItem(0L, namespaceId, lineCounter));
    }
  }

  /**
   * 处理普通配置项
   *
   * @param namespaceId   名称空间id
   * @param keyMapOldItem kv旧配置项
   * @param newItem       新配置项
   * @param lineCounter   行号
   * @param changeSets    变更配置项列表
   */
  private void handleNormalLine(Long namespaceId, Map<String, ItemDTO> keyMapOldItem,
      String newItem, int lineCounter, ItemChangeSets changeSets) {

    // 解析一行，生成 [key, value]
    String[] kv = parseKeyValueFromItem(newItem);

    if (kv == null) {
      throw new BadRequestException("line:" + lineCounter + " key value must separate by '='");
    }

    String newKey = kv[0];
    String newValue = kv[1].replace("\\n", "\n"); //handle user input \n

    // 获得老的 ItemDTO 对象
    ItemDTO oldItem = keyMapOldItem.get(newKey);
    // 不存在，则创建 ItemDTO 到 ItemChangeSets 的添加项
    if (oldItem == null) {

      changeSets.addCreateItem(buildNormalItem(0L, namespaceId, newKey, newValue, "", lineCounter));
    } else if (!newValue.equals(oldItem.getValue()) || lineCounter != oldItem.getLineNum()) {
      // 如果值或者行号不相等，则创建 ItemDTO 到 ItemChangeSets 的修改项
      changeSets.addUpdateItem(buildNormalItem(oldItem.getId(), namespaceId, newKey, newValue,
          oldItem.getComment(), lineCounter));
    }
    // 移除老的 ItemDTO 对象
    keyMapOldItem.remove(newKey);
  }

  /**
   * 判断是否为注释配置项
   *
   * @param item 配置项信息
   * @return true, 表示为注释配置文本，否则，false
   */
  private boolean isCommentItem(ItemDTO item) {
    return item != null && "".equals(item.getKey())
        && (item.getComment().startsWith("#") || item.getComment().startsWith("!"));
  }

  /**
   * 判断是否为注释配置项
   *
   * @param line 行字符串
   * @return true, 表示为注释配置文本，否则，false
   */
  private boolean isCommentItem(String line) {
    return line != null && (line.startsWith("#") || line.startsWith("!"));
  }

  /**
   * 判断是否为空配置项
   *
   * @param item 配置项信息
   * @return true, 表示为注释配置文本，否则，false
   */
  private boolean isBlankItem(ItemDTO item) {
    return item != null && "".equals(item.getKey()) && "".equals(item.getComment());
  }

  /**
   * 判断是否为空配置项
   *
   * @param line 行字符串
   * @return true, 表示为注释配置文本，否则，false
   */
  private boolean isBlankItem(String line) {
    return Strings.nullToEmpty(line).trim().isEmpty();
  }

  /**
   * 删除普通配置项
   *
   * @param baseKeyMapItem 基KeyMap配置项
   * @param changeSets     配置项变更列表
   */
  private void deleteNormalKVItem(Map<String, ItemDTO> baseKeyMapItem, ItemChangeSets changeSets) {
    // 将剩余的配置项，添加到 ItemChangeSets 的删除项
    for (Map.Entry<String, ItemDTO> entry : baseKeyMapItem.entrySet()) {
      changeSets.addDeleteItem(entry.getValue());
    }
  }

  /**
   * 删除注释和空行配置项
   *
   * @param oldLineNumMapItem 旧的行号Maps配置项
   * @param newLineNumMapItem 新的行号Maps配置项
   * @param changeSets        配置项变更列表
   */
  private void deleteCommentAndBlankItem(Map<Integer, ItemDTO> oldLineNumMapItem,
      Map<Integer, String> newLineNumMapItem, ItemChangeSets changeSets) {

    for (Map.Entry<Integer, ItemDTO> entry : oldLineNumMapItem.entrySet()) {
      int lineNum = entry.getKey();
      ItemDTO oldItem = entry.getValue();
      String newItem = newLineNumMapItem.get(lineNum);

      //1. old is blank by now is not
      //2.old is comment by now is not exist or modified
      // 添加到 ItemChangeSets 的删除项,老的是空行配置项，新的不是空行配置项, 老的是注释配置项与新的不相等
      if ((isBlankItem(oldItem) && !isBlankItem(newItem))
          || isCommentItem(oldItem) && (newItem == null || !newItem.equals(oldItem.getComment()))) {
        changeSets.addDeleteItem(oldItem);
      }
    }
  }

  /**
   * 创建注释 ItemDTO 对象
   *
   * @param id          主键id
   * @param namespaceId 名称空间id
   * @param comment     注释
   * @param lineNum     行号
   * @return 注释 ItemDTO 对象
   */
  private ItemDTO buildCommentItem(Long id, Long namespaceId, String comment, int lineNum) {
    return buildNormalItem(id, namespaceId, "", "", comment, lineNum);
  }

  /**
   * 创建空ItemDTO对象
   *
   * @param id          主键id
   * @param namespaceId 名称空间id
   * @param lineNum     行号
   * @return 空ItemDTO对象
   */
  private ItemDTO buildBlankItem(Long id, Long namespaceId, int lineNum) {
    return buildNormalItem(id, namespaceId, "", "", "", lineNum);
  }

  /**
   * 构建正常的配置项
   *
   * @param id          主键id
   * @param namespaceId 名称空间id
   * @param key         键
   * @param value       值
   * @param comment     注释
   * @param lineNum     行号
   * @return 配置项信息
   */
  private ItemDTO buildNormalItem(Long id, Long namespaceId, String key, String value,
      String comment, int lineNum) {
    ItemDTO item = new ItemDTO(key, value, comment, lineNum);
    item.setId(id);
    item.setNamespaceId(namespaceId);
    return item;
  }
}
