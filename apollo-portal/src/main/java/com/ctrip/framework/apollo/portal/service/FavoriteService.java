package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.Favorite;
import com.ctrip.framework.apollo.portal.repository.FavoriteRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 收藏信息 Service层
 */
@Service
public class FavoriteService {

  /**
   * 默认的收藏顺序
   */
  public static final long POSITION_DEFAULT = 10000;

  private final UserInfoHolder userInfoHolder;
  private final FavoriteRepository favoriteRepository;
  private final UserService userService;

  public FavoriteService(
      final UserInfoHolder userInfoHolder,
      final FavoriteRepository favoriteRepository,
      final UserService userService) {
    this.userInfoHolder = userInfoHolder;
    this.favoriteRepository = favoriteRepository;
    this.userService = userService;
  }

  /**
   * 添加应用收藏信息
   *
   * @param favorite 应用收藏信息
   * @return 添加的应用收藏信息
   */
  public Favorite addFavorite(Favorite favorite) {
    UserInfo user = userService.findByUserId(favorite.getUserId());
    if (user == null) {
      throw new BadRequestException("user not exist");
    }

    //判断角色是否当前登录用户
    UserInfo loginUser = userInfoHolder.getUser();
    //user can only add himself favorite app
    if (!loginUser.equals(user)) {
      throw new BadRequestException("add favorite fail. "
          + "because favorite's user is not current login user.");
    }

    // 判断是否已经收藏
    Favorite checkedFavorite = favoriteRepository
        .findByUserIdAndAppId(loginUser.getUserId(), favorite.getAppId());
    if (checkedFavorite != null) {
      return checkedFavorite;
    }

    // 保存
    favorite.setPosition(POSITION_DEFAULT);
    favorite.setDataChangeCreatedBy(user.getUserId());
    favorite.setDataChangeLastModifiedBy(user.getUserId());

    return favoriteRepository.save(favorite);
  }

  /**
   * 通过用户id、应用id查询收藏分页信息
   *
   * @param userId 用户id
   * @param appId  应用id
   * @param page   分页对象
   * @return 查询到的收藏分页信息
   */
  public List<Favorite> search(String userId, String appId, Pageable page) {
    boolean isUserIdEmpty = StringUtils.isBlank(userId);
    boolean isAppIdEmpty = StringUtils.isBlank(appId);

    if (isAppIdEmpty && isUserIdEmpty) {
      throw new BadRequestException("user id and app id can't be empty at the same time");
    }

    if (!isUserIdEmpty) {
      UserInfo loginUser = userInfoHolder.getUser();
      // 用户只能搜索自己收藏的应用程序
      if (!Objects.equals(loginUser.getUserId(), userId)) {
        userId = loginUser.getUserId();
      }
    }

    // 通过用户id查询收藏信息
    if (isAppIdEmpty) {
      return favoriteRepository
          .findByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(userId, page);
    }

    // 通过应用id查询收藏信息
    if (isUserIdEmpty) {
      return favoriteRepository.findByAppIdOrderByPositionAscDataChangeCreatedTimeAsc(appId, page);
    }

    //通过用户id查询收藏信息
    return Collections.singletonList(favoriteRepository.findByUserIdAndAppId(userId, appId));
  }

  /**
   * 通过收藏id删除收藏信息
   *
   * @param favoriteId 收藏id
   */
  public void deleteFavorite(long favoriteId) {
    Favorite favorite = favoriteRepository.findById(favoriteId).orElse(null);

    checkUserOperatePermission(favorite);

    favoriteRepository.delete(favorite);
  }

  /**
   * 将指定收藏信息置顶
   *
   * @param favoriteId 收藏id
   */
  public void adjustFavoriteToFirst(long favoriteId) {
    Favorite favorite = favoriteRepository.findById(favoriteId).orElse(null);

    checkUserOperatePermission(favorite);

    String userId = favorite.getUserId();
    Favorite firstFavorite = favoriteRepository
        .findFirstByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(userId);
    long minPosition = firstFavorite.getPosition();

    favorite.setPosition(minPosition - 1);

    favoriteRepository.save(favorite);
  }

  /**
   * 检查用户操作权限
   *
   * @param favorite 收藏信息
   */
  private void checkUserOperatePermission(Favorite favorite) {
    //收藏信息不存在
    if (favorite == null) {
      throw new BadRequestException("favorite not exist");
    }
    //非当前用户
    if (!Objects.equals(userInfoHolder.getUser().getUserId(), favorite.getUserId())) {
      throw new BadRequestException("can not operate other person's favorite");
    }
  }

  /**
   * 通过应用id指定删除收藏信息
   *
   * @param appId    应用id
   * @param operator 操作者
   */
  public void batchDeleteByAppId(String appId, String operator) {
    favoriteRepository.batchDeleteByAppId(appId, operator);
  }
}
