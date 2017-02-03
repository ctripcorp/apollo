package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.entity.po.Favorite;
import com.ctrip.framework.apollo.portal.service.FavoriteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FavoriteController {

  @Autowired
  private FavoriteService favoriteService;


  @RequestMapping(value = "/favorites", method = RequestMethod.POST)
  public Favorite addFavorite(@RequestBody Favorite favorite) {
    return favoriteService.addFavorite(favorite);
  }


  @RequestMapping(value = "/favorites", method = RequestMethod.GET)
  public List<Favorite> findFavorites(@RequestParam(value = "userId", required = false) String userId,
                                      @RequestParam(value = "appId", required = false) String appId,
                                      Pageable page) {
    return favoriteService.search(userId, appId, page);
  }


  @RequestMapping(value = "/favorites/{favoriteId}", method = RequestMethod.DELETE)
  public void deleteFavorite(@PathVariable long favoriteId) {
    favoriteService.deleteFavorite(favoriteId);
  }


  @RequestMapping(value = "/favorites/{favoriteId}", method = RequestMethod.PUT)
  public void toTop(@PathVariable long favoriteId) {
    favoriteService.adjustFavoriteToFirst(favoriteId);
  }

}
