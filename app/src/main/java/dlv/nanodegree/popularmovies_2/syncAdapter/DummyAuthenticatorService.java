package dlv.nanodegree.popularmovies_2.syncAdapter;

/**
 *
 * Created by daniellujanvillarreal on 9/8/15.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
public class DummyAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private DummyAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new DummyAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}