package jp.juggler.LogcatWidget;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.LinkedList;

import jp.juggler.util.LogCategory;
import jp.juggler.util.WorkerBase;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class SrvLogReader extends Service{
	static final LogCategory log = new LogCategory("SrvLogReader");
	
	@Override
	public void onCreate() {
		super.onCreate();
		log.d("onCreate");
		reader_thread = new ReaderThread();
		reader_thread.start();
		
		Intent intent = registerReceiver(receiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		if(intent!=null) receiver.onReceive(this,intent);
	}

	@Override
	public void onDestroy() {
		log.d("onDestroy");
		super.onDestroy();
		if( reader_thread !=null ){
			reader_thread.joinLoop(log,"reader_thread");
			reader_thread = null;
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		int lines = intent.getIntExtra("lines",0);
		if(latest_max<lines) latest_max=lines;
	}

	/////////////////////////////

	@Override
	public IBinder onBind(Intent intent) {
		log.d("onBind");
		return null;
	}

	//////////////////////////////////////////////////

	static float temp = -1;
	
	BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			temp = intent.getIntExtra("temperature",-2) /(float)10;
		}
	};
	
	static float getBatteryTemp(){
		return temp;
	}
	
	
	//////////////////////////////////////////////////
	static int latest_max = 7;
	static LinkedList<String> latest_log = new LinkedList<String>();
	static String[] getLatestLog(){
		synchronized(latest_log){
			return latest_log.toArray(new String[]{});
		}
	}
	
	ReaderThread reader_thread;
	class ReaderThread extends WorkerBase{
		volatile boolean bCancelled = false;
		@Override
		public void cancel() {
			bCancelled = true;
		}
		public void run(){
			try{
				String[] cmd = new String[]{
					"logcat",
					"-v",
					"time",
				};
				Process process;
				process = Runtime.getRuntime().exec(cmd );
				try{
					LineNumberReader reader;
					reader = new LineNumberReader( new InputStreamReader(process.getInputStream(),"UTF-8"));
					try{
						while(!bCancelled){
							String line = reader.readLine();
							if(line==null) break;
							line = line.substring(line.indexOf(' ')+1);
							synchronized(latest_log){
								while(latest_log.size() >= latest_max ) latest_log.removeFirst();
								latest_log.addLast(line);
							}
						}
					}finally{
						reader.close();
					}
				}finally{
					process.destroy();
				}
			}catch(Exception ex){
				return;
			}
		}
	}
}
