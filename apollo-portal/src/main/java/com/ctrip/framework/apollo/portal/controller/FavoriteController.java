package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.entity.po.Favorite;
import com.ctrip.framework.apollo.portal.service.FavoriteService;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏
 */
@RestController
public class FavoriteController {

  private final FavoriteService favoriteService;

  public FavoriteController(final FavoriteService favoriteService) {
    this.favoriteService = favoriteService;
  }

  /**
   * 收藏
   *
   * @param favorite 应用收藏信息
   * @return 添加的应用收藏信息
   */
  @PostMapping("/favorites")
  public Favorite addFavorite(@RequestBody Favorite favorite) {
    return favoriteService.addFavorite(favorite);
  }

  /**
   * 查询收藏信息列表
   *
   * @param userId 用户id
   * @param appId  应用id
   * @param page   分页信息
   * @return 收藏信息列表
   */
  @GetMapping("/favorites")
  public List<Favorite> findFavorites(
      @RequestParam(value = "userId", required = false) String userId,
      @RequestParam(value = "appId", required = false) String appId,
      Pageable page) {
    return favoriteService.search(userId, appId, page);
  }

  /**
   * 删除指定收藏信息
   *
   * @param favoriteId 收藏id
   */
  @DeleteMapping("/favorites/{favoriteId}")
  public void deleteFavorite(@PathVariable long favoriteId) {
    favoriteService.deleteFavorite(favoriteId);
  }

  /**
   * 置顶
   *
   * @param favoriteId 收藏id
   */
  @PutMapping("/favorites/{favoriteId}")
  public void toTop(@PathVariable long favoriteId) {
    favoriteService.adjustFavoriteToFirst(favoriteId);
  }

}
