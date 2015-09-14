package dlv.nanodegree.popularmovies_2.syncAdapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * *******************************************
 * File copied from https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 * *******************************************
 * Created by daniellujanvillarreal on 9/8/15.
 */
public class MoviesSyncService extends Service {
    /**
     * Define a Service that returns an IBinder for the
     * sync adapter class, allowing the sync adapter framework to call
     * onPerformSync().
     */
        // Storage for an instance of the sync adapter
        private static MoviesSyncAdapter sSyncAdapter = null;
        // Object to use as a thread-safe lock
        private static final Object sSyncAdapterLock = new Object();
        /*
         * Instantiate the sync adapter object.
         */
        @Override
        public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
            synchronized (sSyncAdapterLock) {
                if (sSyncAdapter == null) {
                    sSyncAdapter = new MoviesSyncAdapter(getApplicationContext(), true);
                }
            }
        }
        /**
         * Return an object that allows the system to invoke
         * the sync adapter.
         *
         */
        @Override
        public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
            return sSyncAdapter.getSyncAdapterBinder();
        }
}
