package distj14.seoms.activityrec;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    IRec activity;

    public Receiver(IRec a){
        super();
        activity = a;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        activity.InsertActivity(intent);
    }
}
