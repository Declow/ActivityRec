package distj14.seoms.activityrec;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , IRec {

    ArrayList<String> activities = new ArrayList<>();
    Receiver receiver;
    ArrayAdapter<String> adapter;
    ArrayList<Integer> activity = new ArrayList<>();
    GoogleApiClient googleApiClient;
    private XYPlot mySimpleXYPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intf = new IntentFilter();
        intf.addAction("distj14.seoms.activity");

        receiver = new Receiver(this);
        this.registerReceiver(receiver, intf);
        start();


        mySimpleXYPlot = (XYPlot) findViewById(R.id.plot);

        mySimpleXYPlot.getGraph().setPaddingLeft(125);

        Number[] series1Numbers = {1, 2, 3, 4, 2, 3, 6, 7, 8, 2, 3, 4, 2, 3, 2, 2,5,6};

        // create our series from our array of nums:
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "Thread #1");

        final int screenHeightPx = getWindowManager().getDefaultDisplay().getHeight();
        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, screenHeightPx, Color.WHITE, Color.BLUE, Shader.TileMode.MIRROR));

        StepFormatter stepFormatter  = new StepFormatter(Color.WHITE, Color.BLUE);
        stepFormatter.setVertexPaint(null); // don't draw individual points
        stepFormatter.getLinePaint().setStrokeWidth(PixelUtils.dpToPix(3));

        stepFormatter.getLinePaint().setAntiAlias(false);
        stepFormatter.setFillPaint(lineFill);
        mySimpleXYPlot.addSeries(series2, stepFormatter);

        // adjust the domain/range ticks to make more sense; label per line for range and label per 5 ticks domain:
        mySimpleXYPlot.setRangeStep(StepMode.INCREMENT_BY_VAL, 1);
        mySimpleXYPlot.setDomainStep(StepMode.INCREMENT_BY_VAL, 1);
        mySimpleXYPlot.setLinesPerRangeLabel(1);
        mySimpleXYPlot.setLinesPerDomainLabel(5);


        mySimpleXYPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("0"));


        initxy();

        //ListView view = (ListView) findViewById(R.id.list1);
        //adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activities);

        //view.setAdapter(adapter);

    }

    private void initxy() {
        // create a custom getFormatter to draw our state names as range tick labels:
        mySimpleXYPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo,
                                       @NonNull FieldPosition pos) {
                Number num = (Number) obj;
                switch (num.intValue()) {
                    case 1:
                        toAppendTo.append("VEHICLE");
                        break;
                    case 2:
                        toAppendTo.append("BICYCLE");
                        break;
                    case 3:
                        toAppendTo.append("STILL");
                        break;
                    case 4:
                        toAppendTo.append("TILTING");
                        break;
                    case 5:
                        toAppendTo.append("RUNNING");
                        break;
                    case 6:
                        toAppendTo.append("ON_FOOT");
                        break;
                    case 7:
                        toAppendTo.append("WALKING");
                        break;
                    default:
                        toAppendTo.append("UNKNOWN");
                        break;
                }
                return toAppendTo;
            }

            @Override
            public Object parseObject(String source, @NonNull ParsePosition pos) {
                return null;
            }
        });
    }

    private void start() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this) //this is refer to connectionCallbacks interface implementation.
                .addOnConnectionFailedListener(this) //this is refer to onConnectionFailedListener interface implementation.
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("Hello", "connected");

        Intent intent = new Intent( this, IntentRec.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, 3000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("Hello", "Connection suspended");

        Toast toast = Toast.makeText(this, "connection suspended", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("Hello", "Connection failed");
        Toast toast = Toast.makeText(this, "Failed connection", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void InsertActivity(Intent i) {
        int activityType = (int)i.getExtras().get(Util.ACTIVITY_TYPE);
        int confidence = (int)i.getExtras().get(Util.CONFIDENCE);
        long time = (long)i.getExtras().get(Util.TIME);

        //activities.add(new MyActivity(activityType, confidence, time).toString());

        activity.add(activityType);
        updateGraph();

        Toast toast = Toast.makeText(this, "Got activity", Toast.LENGTH_SHORT);
        toast.show();
        Log.v("hello", "Got the activity");
        //adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intf = new IntentFilter();
        intf.addAction("distj14.seoms.activity");

        receiver = new Receiver(this);
        this.registerReceiver(receiver, intf);
    }

    private void updateGraph() {
        mySimpleXYPlot.clear();

        XYSeries series2 = new SimpleXYSeries(
                activity,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "Activities");

        final int screenHeightPx = getWindowManager().getDefaultDisplay().getHeight();
        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, screenHeightPx, Color.WHITE, Color.BLUE, Shader.TileMode.MIRROR));

        StepFormatter stepFormatter  = new StepFormatter(Color.WHITE, Color.BLUE);
        stepFormatter.setVertexPaint(null); // don't draw individual points
        stepFormatter.getLinePaint().setStrokeWidth(PixelUtils.dpToPix(3));

        stepFormatter.getLinePaint().setAntiAlias(false);
        stepFormatter.setFillPaint(lineFill);
        mySimpleXYPlot.addSeries(series2, stepFormatter);

        mySimpleXYPlot.redraw();
    }
}
