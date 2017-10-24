package distj14.seoms.activityrec;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ditlev on 10/19/17.
 */

public class MyActivity {
    private int Activity;
    private int Confidence;
    private long Time;

    public MyActivity(int activity, int confidence, long time) {
        this.Activity = activity;
        this.Confidence = confidence;
        this.Time = time;
    }

    public int getActivity() {
        return this.Activity;
    }

    public int getConfidence() {
        return this.Confidence;
    }

    public long getTime() {
        return this.Time;
    }

    @Override
    public String toString() {
        Date d = new Date(this.Time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String time = Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + " " + Integer.toString(cal.get(Calendar.MINUTE));

        return this.Activity + ", Confidence: " + this.getConfidence() + ", TimeOfDay: " + time;
    }
}
