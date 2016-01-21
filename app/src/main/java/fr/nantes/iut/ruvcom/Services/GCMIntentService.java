package fr.nantes.iut.ruvcom.services;

import android.app.IntentService;
import android.content.Intent;


/**
 * Created by ughostephan on 18/01/2016.
 */
public class GCMIntentService extends IntentService {

    public GCMIntentService() {
        super("");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GCMIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
