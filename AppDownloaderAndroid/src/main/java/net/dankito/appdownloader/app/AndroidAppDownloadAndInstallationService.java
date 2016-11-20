package net.dankito.appdownloader.app;

import android.app.Activity;
import android.content.res.Resources;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.AlertHelper;
import net.dankito.appdownloader.util.web.DownloadResult;
import net.dankito.appdownloader.util.web.IDownloadCompletedCallback;
import net.dankito.appdownloader.util.web.IDownloadManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ganymed on 20/11/16.
 */

public class AndroidAppDownloadAndInstallationService implements IAppDownloadAndInstallationService {

  private static final Logger log = LoggerFactory.getLogger(AndroidAppDownloadAndInstallationService.class);


  protected Activity activity;

  protected List<IAppDownloader> appDownloaders;

  protected IDownloadManager downloadManager;

  protected IAppVerifier appVerifier;

  protected IAppInstaller appInstaller;


  public AndroidAppDownloadAndInstallationService(Activity activity, List<IAppDownloader> appDownloaders, IDownloadManager downloadManager,
                                                  IAppVerifier appVerifier, IAppInstaller appInstaller) {
    this.activity = activity;
    this.appDownloaders = appDownloaders;
    this.downloadManager = downloadManager;
    this.appVerifier = appVerifier;
    this.appInstaller = appInstaller;
  }


  @Override
  public void installApp(AppInfo app) {
    if(app.hasDownloadUrls()) {
      downloadApp(app, getBestAppDownloadUrl(app));
    }
    else {
      getAppDownloadLinkAndDownloadApp(app);
    }
  }

  protected AppDownloadInfo getBestAppDownloadUrl(AppInfo app) {
    if(app.getDownloadInfos().size() == 1) {
      return app.getDownloadInfos().get(0);
    }
    else {
      // TODO: choose best one
      return app.getDownloadInfos().get(0);
    }
  }

  protected void getAppDownloadLinkAndDownloadApp(final AppInfo app) {
    app.setState(AppState.GETTING_DOWNLOAD_URL);

    final AtomicBoolean hasDownloadUrlBeenRetrieved = new AtomicBoolean(false);
    final AtomicBoolean hasDownloadBeenStarted = new AtomicBoolean(false);
    final AtomicInteger countRequestsAppDownloadLinkCompleted = new AtomicInteger(0);

    for(IAppDownloader appDownloader : appDownloaders) {
      appDownloader.getAppDownloadLinkAsync(app, new GetAppDownloadUrlResponseCallback() {
        @Override
        public void completed(GetAppDownloadUrlResponse response) {
          synchronized(hasDownloadUrlBeenRetrieved) {
            getAppDownloadLinkCompleted(app, response, hasDownloadUrlBeenRetrieved, hasDownloadBeenStarted, countRequestsAppDownloadLinkCompleted);
          }
        }
      });
    }
  }

  protected void getAppDownloadLinkCompleted(AppInfo app, GetAppDownloadUrlResponse response, AtomicBoolean hasDownloadUrlBeenRetrieved, AtomicBoolean hasDownloadBeenStarted, AtomicInteger countRequestsAppDownloadLinkCompleted) {
    countRequestsAppDownloadLinkCompleted.incrementAndGet();
    // TODO: move add AppDownloadInfo to AppInfo to here (so it's centrally placed)

    if(response.isSuccessful()) {
      hasDownloadUrlBeenRetrieved.set(true);

      AppDownloadInfo downloadInfo = response.getDownloadInfo();
      if(hasDownloadBeenStarted.get() == false && downloadInfo.getAppDownloader().isTrustworthySource()) {
        hasDownloadBeenStarted.set(true);
        downloadApp(app, downloadInfo);
      }
    }

    if(countRequestsAppDownloadLinkCompleted.get() == appDownloaders.size()) {
      if(hasDownloadUrlBeenRetrieved.get() == false) {
        app.setToItsDefaultState();
        showErrorMessageThreadSafe(activity.getString(R.string.error_message_could_not_download_app, response.getError()), null);
      }
      else if(hasDownloadBeenStarted.get() == false) {
        downloadApp(app, getBestAppDownloadUrl(app));
      }
    }
  }

  protected void downloadApp(AppInfo app, AppDownloadInfo downloadInfo) {
    log.info("Starting to download App " + app + " from " + downloadInfo + " ...");

    app.setState(AppState.DOWNLOADING);

    downloadAppViaAndroidDownloadManager(app, downloadInfo);
  }

  protected void downloadAppViaAndroidDownloadManager(AppInfo app, AppDownloadInfo downloadInfo) {
    downloadManager.downloadUrlAsync(app, downloadInfo, new IDownloadCompletedCallback() {
      @Override
      public void completed(DownloadResult result) {
        appDownloadCompleted(result);
      }
    });
  }

  protected void appDownloadCompleted(DownloadResult result) {
    AppDownloadInfo downloadInfo = result.getDownloadInfo();
    AppInfo app = downloadInfo.getAppInfo();

    if(result.isSuccessful()) {
      AppPackageVerificationResult verificationResult = appVerifier.verifyDownloadedApk(downloadInfo);
      if(verificationResult.wasVerificationSuccessful()) {
        appInstaller.installApp(downloadInfo);
      }
      else {
        Resources resources = activity.getResources();
        String errorMessageTitle = resources.getString(R.string.error_message_title_could_not_verify_app_package, app.getTitle());
        showErrorMessageThreadSafe(verificationResult.getErrorMessage(), errorMessageTitle);
      }
    }
    else if(result.isUserCancelled() == false) {
      showErrorMessageThreadSafe(result.getError(), activity.getResources().getString(R.string.error_message_could_not_download_app, app.getTitle()));
    }
  }

  protected void showErrorMessageThreadSafe(String error, String title) {
    AlertHelper.showErrorMessageThreadSafe(activity, error, title);
  }

}
