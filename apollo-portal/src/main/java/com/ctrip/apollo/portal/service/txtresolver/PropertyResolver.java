package com.ctrip.apollo.portal.service.txtresolver;

import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.util.BeanUtils;
import com.sun.tools.javac.util.Assert;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
  public TextResolverResult resolve(String configText, List<ItemDTO> baseItems) {

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

    ItemChangeSets changeSets = new ItemChangeSets();
    result.setChangeSets(changeSets);
    Map<Integer, String> newLineNumMapItem = new HashedMap();//use for delete blank and comment item
    int lineCounter = 1;
    for (String newItem : newItems) {
      newItem = newItem.trim();
      newLineNumMapItem.put(lineCounter, newItem);
      ItemDTO oldItemByLine = oldLineNumMapItem.get(lineCounter);

      //comment item
      if (isCommentItem(newItem)) {

        handleCommentLine(oldItemByLine, newItem, lineCounter, changeSets);

        //blank item
      } else if (isBlankItem(newItem)) {

        handleBlankLine(oldItemByLine, lineCounter, changeSets);

        //normal line
      } else {
        if (!handleNormalLine(oldKeyMapItem, newItem, lineCounter, result)) {
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

  private void handleCommentLine(ItemDTO oldItemByLine, String newItem, int lineCounter, ItemChangeSets changeSets) {
    String oldComment = oldItemByLine == null ? "" : oldItemByLine.getComment();
    //create comment. implement update comment by delete old comment and create new comment
    if (!(isCommentItem(oldItemByLine) && newItem.equals(oldComment))) {
      changeSets.addCreatedItem(buildCommentItem(0l, newItem, lineCounter));
    }
  }

  private void handleBlankLine(ItemDTO oldItem, int lineCounter, ItemChangeSets changeSets) {
    if (!isBlankItem(oldItem)) {
      changeSets.addCreatedItem(buildBlankItem(0l, lineCounter));
    }
  }

  private boolean handleNormalLine(Map<String, ItemDTO> keyMapOldItem, String newItem,
                                   int lineCounter, TextResolverResult result) {

    ItemChangeSets changeSets = result.getChangeSets();

    int kvSeparator = newItem.indexOf(KV_SEPARATOR);
    if (kvSeparator == -1) {
      result.setResolveSuccess(false);
      result.setMsg(" line:" + lineCounter + " key value must separate by '='");
      return false;
    }

    String newKey = newItem.substring(0, kvSeparator).trim();
    String newValue = newItem.substring(kvSeparator + 1, newItem.length()).trim();

    ItemDTO oldItem = keyMapOldItem.get(newKey);

    if (oldItem == null) {//new item
      changeSets.addCreatedItem(buildNormalItem(0l, newKey, newValue, "", lineCounter));
    } else {//update item
      changeSets.addUpdateItem(
          buildNormalItem(oldItem.getId(), newKey, newValue, oldItem.getComment(),
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
      changeSets.addDeletedItem(entry.getValue());
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
          || isCommentItem(oldItem) && (newItem == null || !newItem.equals(oldItem))) {
        changeSets.addDeletedItem(oldItem);
      }
    }
  }

  private ItemDTO buildCommentItem(Long id, String comment, int lineNum) {
    return buildNormalItem(id, "", "", comment, lineNum);
  }

  private ItemDTO buildBlankItem(Long id, int lineNum) {
    return buildNormalItem(id, "", "", "", lineNum);
  }

  private ItemDTO buildNormalItem(Long id, String key, String value, String comment, int lineNum) {
    ItemDTO item = new ItemDTO();
    item.setId(id);
    item.setKey(key);
    item.setValue(value);
    item.setComment(comment);
    item.setLineNum(lineNum);
    return item;
  }
}
