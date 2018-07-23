package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.Favorite;
import com.ctrip.framework.apollo.portal.repository.FavoriteRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FavoriteService {

  public static final long POSITION_DEFAULT = 10000;

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private FavoriteRepository favoriteRepository;
  @Autowired
  private UserService userService;


  public Favorite addFavorite(Favorite favorite) {
    UserInfo user = userService.findByUserId(favorite.getUserId());
    if (user == null) {
      throw new BadRequestException("user not exist");
    }

    UserInfo loginUser = userInfoHolder.getUser();
    //user can only add himself favorite app
    if (!loginUser.equals(user)) {
      throw new BadRequestException("add favorite fail. "
                                    + "because favorite's user is not current login user.");
    }

    Favorite checkedFavorite = favoriteRepository.findByUserIdAndAppId(loginUser.getUserId(), favorite.getAppId());
    if (checkedFavorite != null) {
      return checkedFavorite;
    }

    favorite.setPosition(POSITION_DEFAULT);
    favorite.setDataChangeCreatedBy(user.getUserId());
    favorite.setDataChangeLastModifiedBy(user.getUserId());

    return favoriteRepository.save(favorite);
  }


  public List<Favorite> search(String userId, String appId, Pageable page) {
    boolean isUserIdEmpty = StringUtils.isEmpty(userId);
    boolean isAppIdEmpty = StringUtils.isEmpty(appId);

    if (isAppIdEmpty && isUserIdEmpty) {
      throw new BadRequestException("user id and app id can't be empty at the same time");
    }

    //search by userId
    if (isAppIdEmpty && !isUserIdEmpty) {
      return favoriteRepository.findByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(userId, page);
    }

    //search by appId
    if (!isAppIdEmpty && isUserIdEmpty) {
      return favoriteRepository.findByAppIdOrderByPositionAscDataChangeCreatedTimeAsc(appId, page);
    }

    //search by userId and appId
    return Arrays.asList(favoriteRepository.findByUserIdAndAppId(userId, appId));
  }


  public void deleteFavorite(long favoriteId) {
    Favorite favorite = favoriteRepository.findOne(favoriteId);

    checkUserOperatePermission(favorite);

    favoriteRepository.delete(favorite);
  }

  public void adjustFavoriteToFirst(long favoriteId) {
    Favorite favorite = favoriteRepository.findOne(favoriteId);

    checkUserOperatePermission(favorite);

    String userId = favorite.getUserId();
    Favorite firstFavorite = favoriteRepository.findFirstByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(userId);
    long minPosition = firstFavorite.getPosition();

    favorite.setPosition(minPosition - 1);

    favoriteRepository.save(favorite);
  }

  private void checkUserOperatePermission(Favorite favorite) {
    if (favorite == null) {
      throw new BadRequestException("favorite not exist");
    }

    if (!Objects.equals(userInfoHolder.getUser().getUserId(), favorite.getUserId())) {
      throw new BadRequestException("can not operate other person's favorite");
    }
  }

  public void batchDeleteByAppId(String appId, String operator) {
    favoriteRepository.batchDeleteByAppId(appId, operator);
  }
}
