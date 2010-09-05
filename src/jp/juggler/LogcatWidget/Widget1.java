package jp.juggler.LogcatWidget;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.SystemClock;
import android.widget.RemoteViews;
import jp.juggler.util.LogCategory;

public class Widget1  extends AppWidgetProvider {
	static final LogCategory log = new LogCategory("Widget1");
	static final String URI_UPDATE = "alarm://jp.juggler.TempWidget/";
	static final long update_interval = 2000;

	void service_start(Context context,int lines){
		Intent intent = new Intent(context,SrvLogReader.class);
		intent.putExtra("lines",lines);
		context.startService(intent);
	}
	void service_stop(Context context){
		context.stopService(new Intent(context,SrvLogReader.class));
	}
	
	@Override public void onEnabled(Context context) {
		super.onEnabled(context);
		service_start(context,3);
	}
	@Override public void onDisabled(Context context) {
		super.onDisabled(context);
		service_stop(context);
	}

	static int temp = -1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if( intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE ) ){
			
			int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			// int[] appWidgetIds= manager.getAppWidgetIds(new ComponentName(context,Widget1.class)); 
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			onUpdate(context,manager,appWidgetIds);
		}else{
			log.d("onReceive %s",intent.getAction());
			super.onReceive(context, intent);
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager manager,int[] appWidgetIds) {
		int lines_max = 0;
        for (int i=0,end=appWidgetIds.length; i<end; i++) {
            int id = appWidgetIds[i];
            // ウィジェットの存在確認
            AppWidgetProviderInfo info = manager.getAppWidgetInfo(id);
            if(info==null) continue;
            // 表示を更新
            update_view(context,manager,id);
            // 再表示スケジュール
    		update_alarm(context,id);
    		//
    		int lines = (480/9);
    		if(lines_max<lines) lines_max = lines;
        }
        if(lines_max>0) service_start(context,lines_max);
	}

	static IntentFilter ifBattery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED );
	
	void update_view(Context context
		,AppWidgetManager manager
		,int id
	){

        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget1);
/*
        // クリック時に処理するインテント
        {
	        Intent intent = new Intent(context, ActMain.class);
	        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
	        rv.setOnClickPendingIntent(R.id.text, pendingIntent);
        }
*/        
        // テキストに表示するテキスト
        {
	        String[] logs = SrvLogReader.getLatestLog();
	
	        StringBuilder sb = new StringBuilder();
	    	sb.append("battery temp="+ SrvLogReader.getBatteryTemp()+"\n");
	    	for(int i=logs.length-1;i>=0;--i){
	    		sb.append(logs[i]);
	    		sb.append("\n");
	    	}
	    	
	    	rv.setTextViewText(R.id.text,sb.toString());
        }

        // Tell the AppWidgetManager to perform an update on the current App Widget
        manager.updateAppWidget(id, rv);
	}
	
	void update_alarm(Context context,int id){
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
    	Intent intent = new Intent(context , Widget1.class );
    	intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,new int[]{ id });
    	intent.setData(Uri.parse(URI_UPDATE + "/" + id ));

    	PendingIntent alarm_pending = PendingIntent.getBroadcast(
    		context
    		,0
    		,intent
    		,PendingIntent.FLAG_UPDATE_CURRENT
    	);
    	alarms.set(
    		AlarmManager.ELAPSED_REALTIME
    		,SystemClock.elapsedRealtime() + update_interval
    		,alarm_pending
    	);
	}
	
}
