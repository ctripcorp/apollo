package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.portal.AbstractUnitTest;
import com.ctrip.framework.apollo.portal.environment.Env;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author renjiahua
 * @date 2021/1/17
 */
public class NamespaceBoAsyncServiceTest extends AbstractUnitTest {

    @Mock
    private ReleaseService releaseService;
    @Mock
    private ItemService itemService;
    @InjectMocks
    private NamespaceBoAsyncService namespaceBoAsyncService;

    private String testAppId = "6666";
    private String testClusterName = "default";
    private String testNamespaceName = "application";
    private Env testEnv = Env.DEV;

    @Before
    public void setup() {
    }

    @Test
    public void testGetLatestReleaseAsync() {
        ReleaseDTO someRelease = new ReleaseDTO();
        someRelease.setConfigurations("{\"a\":\"123\",\"b\":\"123\"}");
        when(releaseService.loadLatestRelease(testAppId, testEnv, testClusterName, testNamespaceName))
                .thenReturn(someRelease);
        Future<Map<String, String>> latestReleaseAsync = namespaceBoAsyncService.getLatestReleaseAsync(testAppId, testEnv, testClusterName, testNamespaceName);
        try {
            Map<String, String> asyncRelease = latestReleaseAsync.get();
            assertNotNull(asyncRelease);
            assertEquals(2, asyncRelease.size());
            assertEquals("123", asyncRelease.get("a"));
            assertEquals("123", asyncRelease.get("b"));
        } catch (Exception e) {
            assertTrue((e instanceof ExecutionException || e instanceof InterruptedException));
        }
    }

    @Test
    public void testGetItemsAsync() {
        ItemDTO i1 = new ItemDTO("a", "123", "", 1);
        ItemDTO i2 = new ItemDTO("b", "1", "", 2);
        ItemDTO i3 = new ItemDTO("", "", "#dddd", 3);
        ItemDTO i4 = new ItemDTO("c", "1", "", 4);
        List<ItemDTO> someItems = Arrays.asList(i1, i2, i3, i4);
        when(itemService.findItems(testAppId, testEnv, testClusterName, testNamespaceName))
                .thenReturn(someItems);
        Future<List<ItemDTO>> itemsAsync = namespaceBoAsyncService.getItemsAsync(testAppId, testEnv, testClusterName, testNamespaceName);
        try {
            List<ItemDTO> list = itemsAsync.get();
            assertNotNull(list);
            assertEquals(4, list.size());
            assertEquals("a", list.get(0).getKey());
            assertEquals(4, list.get(3).getLineNum());
            assertEquals("", list.get(2).getValue());
        } catch (Exception e) {
            assertTrue((e instanceof ExecutionException || e instanceof InterruptedException));
        }
    }


}
