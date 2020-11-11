package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * 发布消息 Repository
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseMessageRepository extends PagingAndSortingRepository<ReleaseMessage, Long> {

  /**
   * 查询id大于指定id以id升序的前500条发布消息信息列表
   *
   * @param id 主键id
   * @return 符合条件的发布消息信息列表
   */
  List<ReleaseMessage> findFirst500ByIdGreaterThanOrderByIdAsc(Long id);

  /**
   * 通过id降序查询第一条发布消息信息
   *
   * @return 符合条件的发布消息信息
   */
  ReleaseMessage findTopByOrderByIdDesc();

  /**
   * 通过指定发布消息内容集合查询发布消息信息
   *
   * @param messages 指定发布消息内容集合
   * @return 符合条件的发布消息信息
   */
  ReleaseMessage findTopByMessageInOrderByIdDesc(Collection<String> messages);

  /**
   * 通过指定发布消息内容并且id小于指定id以id升序查询前100条发布消息信息列表
   *
   * @param message 指定发布消息内容集合
   * @param id      主键id
   * @return 符合条件的发布消息信息
   */
  List<ReleaseMessage> findFirst100ByMessageAndIdLessThanOrderByIdAsc(String message, Long id);

  /**
   * 通过指定发布消息内容集合查询发布消息内容与最大的id
   *
   * @param messages 指定发布消息内容集合
   * @return 对象数组列表
   */
  @Query("select message, max(id) as id from ReleaseMessage where message in :messages group by message")
  List<Object[]> findLatestReleaseMessagesGroupByMessages(
      @Param("messages") Collection<String> messages);
}
