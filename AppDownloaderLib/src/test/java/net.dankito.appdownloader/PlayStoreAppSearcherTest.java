package net.dankito.appdownloader;

import net.dankito.appdownloader.app.AppDetailsCache;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.responses.SearchAppsResponse;
import net.dankito.appdownloader.responses.callbacks.SearchAppsResponseCallback;
import net.dankito.appdownloader.util.StringUtils;
import net.dankito.appdownloader.util.apps.NoOpInstalledAppsManager;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.OkHttpWebClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 02/11/16.
 */

public class PlayStoreAppSearcherTest {

  protected IPlayStoreAppSearcher underTest;


  @Before
  public void setUp() {
    IWebClient webClient = new OkHttpWebClient();
    underTest = new PlayStoreAppSearcher(webClient, new NoOpInstalledAppsManager(), new AppDetailsCache());
  }


  @Test
  public void searchAsync() {
    final Set<AppInfo> searchResults = new HashSet<>();
    final CountDownLatch countDownLatch = new CountDownLatch(PlayStoreAppSearcher.MAX_SEARCH_RESULT_PAGE_NUMBER);

    underTest.searchAsync("noroot", new SearchAppsResponseCallback() {
      @Override
      public void searchResultsRetrieved(SearchAppsResponse response) {
        searchResults.addAll(response.getSearchResults());
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(15, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(searchResults.size() > 0);
    Assert.assertEquals(80, searchResults.size());

    for(AppInfo appInfo : searchResults) {
      Assert.assertTrue(appInfo.areNecessaryInformationSet());

      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getPackageName()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getTitle()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getDeveloper()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getSmallCoverImageUrl()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getLargeCoverImageUrl()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getAppDetailsPageUrl()));
    }
  }
}
