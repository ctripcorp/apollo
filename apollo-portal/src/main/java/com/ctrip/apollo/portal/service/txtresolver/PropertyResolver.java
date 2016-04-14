package com.ctrip.apollo.portal.service.txtresolver;

import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.util.BeanUtils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * normal property file resolver.
 * update comment and blank item implement by create new item and delete old item.
 * update normal key/value item implement by update.
 */
@Component
public class PropertyResolver implements ConfigTextResolver {

  private static final String KV_SEPARATOR = "=";
  private static final String ITEM_SEPARATOR = "\n";

  @Override
  public TextResolverResult resolve(long namespaceId, String configText, List<ItemDTO> baseItems) {

    TextResolverResult result = new TextResolverResult();
    if (StringUtils.isEmpty(configText)){
      result.setResolveSuccess(false);
      result.setMsg("config text can not be empty");
      return result;
    }

    Map<Integer, ItemDTO> oldLineNumMapItem = BeanUtils.mapByKey("lineNum", baseItems);
    Map<String, ItemDTO> oldKeyMapItem = BeanUtils.mapByKey("key", baseItems);

    //remove comment and blank item map.
    oldKeyMapItem.remove("");

    String[] newItems = configText.split(ITEM_SEPARATOR);

    if (isHasRepeatKey(newItems)){
      result.setResolveSuccess(false);
      result.setMsg("config text has repeat key please check.");
      return result;
    }

    ItemChangeSets changeSets = new ItemChangeSets();
    result.setChangeSets(changeSets);
    Map<Integer, String> newLineNumMapItem = new HashMap();//use for delete blank and comment item
    int lineCounter = 1;
    for (String newItem : newItems) {
      newItem = newItem.trim();
      newLineNumMapItem.put(lineCounter, newItem);
      ItemDTO oldItemByLine = oldLineNumMapItem.get(lineCounter);

      //comment item
      if (isCommentItem(newItem)) {

        handleCommentLine(namespaceId, oldItemByLine, newItem, lineCounter, changeSets);

        //blank item
      } else if (isBlankItem(newItem)) {

        handleBlankLine(namespaceId, oldItemByLine, lineCounter, changeSets);

        //normal item
      } else {
        if (!handleNormalLine(namespaceId, oldKeyMapItem, newItem, lineCounter, result)) {
          return result;
        }
      }

      lineCounter++;
    }

    deleteCommentAndBlankItem(oldLineNumMapItem, newLineNumMapItem, changeSets);
    deleteNormalKVItem(oldKeyMapItem, changeSets);

    result.setResolveSuccess(true);

    return result;
  }

  private boolean isHasRepeatKey(String[] newItems){
    Set<String> keys = new HashSet<>();
    int keyCount = 0;
    for (String item: newItems){
      if (!isCommentItem(item) && !isBlankItem(item)){
        keyCount ++;
        keys.add(parseKeyValueFromItem(item)[0]);
      }
    }

    return keyCount > keys.size();
  }

  private String[] parseKeyValueFromItem(String item){
    int kvSeparator = item.indexOf(KV_SEPARATOR);
    if (kvSeparator == -1) {
      return null;
    }

    String[] kv = new String[2];
    kv[0] = item.substring(0, kvSeparator).trim();
    kv[1] = item.substring(kvSeparator + 1, item.length()).trim();
    return kv;
  }

  private void handleCommentLine(Long namespaceId, ItemDTO oldItemByLine, String newItem, int lineCounter, ItemChangeSets changeSets) {
    String oldComment = oldItemByLine == null ? "" : oldItemByLine.getComment();
    //create comment. implement update comment by delete old comment and create new comment
    if (!(isCommentItem(oldItemByLine) && newItem.equals(oldComment))) {
      changeSets.addCreateItem(buildCommentItem(0l, namespaceId, newItem, lineCounter));
    }
  }

  private void handleBlankLine(Long namespaceId, ItemDTO oldItem, int lineCounter, ItemChangeSets changeSets) {
    if (!isBlankItem(oldItem)) {
      changeSets.addCreateItem(buildBlankItem(0l, namespaceId, lineCounter));
    }
  }

  private boolean handleNormalLine(Long namespaceId, Map<String, ItemDTO> keyMapOldItem, String newItem,
                                   int lineCounter, TextResolverResult result) {

    ItemChangeSets changeSets = result.getChangeSets();

    String[] kv = parseKeyValueFromItem(newItem);

    if (kv == null) {
      result.setResolveSuccess(false);
      result.setMsg(" line:" + lineCounter + " key value must separate by '='");
      return false;
    }

    String newKey = kv[0];
    String newValue = kv[1];

    ItemDTO oldItem = keyMapOldItem.get(newKey);

    if (oldItem == null) {//new item
      changeSets.addCreateItem(buildNormalItem(0l, namespaceId, newKey, newValue, "", lineCounter));
    } else if (!newValue.equals(oldItem.getValue()) || lineCounter != oldItem.getLineNum()){//update item
      changeSets.addUpdateItem(
          buildNormalItem(oldItem.getId(), namespaceId, newKey, newValue, oldItem.getComment(),
                          lineCounter));
    }
    keyMapOldItem.remove(newKey);
    return true;
  }

  private boolean isCommentItem(ItemDTO item) {
    return item != null && "".equals(item.getKey()) && item.getComment().startsWith("#");
  }

  private boolean isCommentItem(String line) {
    return line != null && line.startsWith("#");
  }

  private boolean isBlankItem(ItemDTO item) {
    return item != null && "".equals(item.getKey()) && "".equals(item.getComment());
  }

  private boolean isBlankItem(String line) {
    return "".equals(line);
  }

  private void deleteNormalKVItem(Map<String, ItemDTO> baseKeyMapItem, ItemChangeSets changeSets) {
    //surplus item is to be deleted
    for (Map.Entry<String, ItemDTO> entry : baseKeyMapItem.entrySet()) {
      changeSets.addDeleteItem(entry.getValue());
    }
  }

  private void deleteCommentAndBlankItem(Map<Integer, ItemDTO> oldLineNumMapItem,
                                         Map<Integer, String> newLineNumMapItem,
                                         ItemChangeSets changeSets) {

    for (Map.Entry<Integer, ItemDTO> entry : oldLineNumMapItem.entrySet()) {
      int lineNum = entry.getKey();
      ItemDTO oldItem = entry.getValue();
      String newItem = newLineNumMapItem.get(lineNum);

      //1. old is blank by now is not
      //2.old is comment by now is not exist or modified
      if ((isBlankItem(oldItem) && !isBlankItem(newItem))
          || isCommentItem(oldItem) && (newItem == null || !newItem.equals(oldItem.getComment()))) {
        changeSets.addDeleteItem(oldItem);
      }
    }
  }

  private ItemDTO buildCommentItem(Long id, Long namespaceId, String comment, int lineNum) {
    return buildNormalItem(id, namespaceId, "", "", comment, lineNum);
  }

  private ItemDTO buildBlankItem(Long id, Long namespaceId, int lineNum) {
    return buildNormalItem(id, namespaceId, "", "", "", lineNum);
  }

  private ItemDTO buildNormalItem(Long id, Long namespaceId, String key, String value, String comment, int lineNum) {
    ItemDTO item = new ItemDTO();
    item.setId(id);
    item.setNamespaceId(namespaceId);
    item.setKey(key);
    item.setValue(value);
    item.setComment(comment);
    item.setLineNum(lineNum);
    return item;
  }
}
