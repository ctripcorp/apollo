package com.ctrip.framework.apollo.common.dto;

import java.util.List;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;

/**
 * 分页 DTo
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Getter
public class PageDTO<T> {

  /**
   * 分页列表数据总数
   */
  private final Long total;
  /**
   * 分页列表数据
   */
  private final List<T> content;
  /**
   * 当前页码
   */
  private final Integer page;
  /**
   * 总页码
   */
  private final Integer size;

  /**
   * 构造分页Dto
   *
   * @param content  分页列表数据
   * @param pageable 分页对象
   * @param total    分页列表数据总数
   */
  public PageDTO(List<T> content, Pageable pageable, long total) {
    this.total = total;
    this.content = content;
    this.page = pageable.getPageNumber();
    this.size = pageable.getPageSize();
  }

  /**
   * 是否存在分页数据
   *
   * @return true, 存在分页数据.false 不存在
   */
  public boolean hasContent() {
    return CollectionUtils.isNotEmpty(content);
  }
}
