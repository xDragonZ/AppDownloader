package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 19/11/16.
 */

public interface UpdatableAppsListener {

  void foundUpdatableApp(UpdatableAppsListenerInfo info);

  void appUpdated(UpdatableAppsListenerInfo info);

}
