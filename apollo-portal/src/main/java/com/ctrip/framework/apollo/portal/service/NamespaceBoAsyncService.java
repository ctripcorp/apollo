package com.ctrip.framework.apollo.portal.service;


import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.google.gson.Gson;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author renjiahua
 * @date 2020/12/26
 */

@Service
public class NamespaceBoAsyncService {
	/**
	 * releaseService
	 */
	private final ReleaseService releaseService;

	private final ItemService itemService;

	private Gson gson = new Gson();

	public NamespaceBoAsyncService(final ReleaseService releaseService,
																 final ItemService itemService) {
		this.releaseService = releaseService;
		this.itemService = itemService;
	}

	/**
	 * Get the latest release
	 *
	 * @param appId         appId
	 * @param env           env
	 * @param clusterName   cl
	 * @param namespaceName namespaceName
	 * @return Future<Map < String, String>>
	 */
	@Async("queryRemoteExecutor")
	public Future<Map<String, String>> getLatestReleaseAsync(String appId, Env env, String clusterName, String namespaceName) {
		ReleaseDTO latestRelease = releaseService.loadLatestRelease(appId, env, clusterName, namespaceName);
		Map<String, String> releaseItems = new HashMap<>();
		if (latestRelease != null) {
			releaseItems = gson.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG);
		}
		return new AsyncResult<>(releaseItems);
	}

	/**
	 * Get the items
	 *
	 * @param appId         appId
	 * @param env           env
	 * @param clusterName   cl
	 * @param namespaceName namespaceName
	 * @return Future<Map < String, String>>
	 */
	@Async("queryRemoteExecutor")
	public Future<List<ItemDTO>> getItemsAsync(String appId, Env env, String clusterName, String namespaceName) {
		List<ItemDTO> items = itemService.findItems(appId, env, clusterName, namespaceName);
		return new AsyncResult<>(items);
	}
}
