package jp.juggler.LogcatWidget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.text.format.DateUtils;

public class ActMain extends Activity {
	TextView tvText;
	Button btnTest;
	
	static String getInfoText(Context context){
		long now = System.currentTimeMillis();
		String timestr = DateUtils.formatDateTime(context,now
				,DateUtils.FORMAT_24HOUR
				|DateUtils.FORMAT_SHOW_DATE
				|DateUtils.FORMAT_SHOW_TIME
				|DateUtils.FORMAT_SHOW_WEEKDAY
				|DateUtils.FORMAT_SHOW_YEAR
		);
		return String.format("%s %.03f"
			,timestr
			,(now%(1000*60))/(float)1000
		);
		
	}
	
	void initUI(){
        setContentView(R.layout.main);
        tvText = (TextView)findViewById(R.id.text);
        btnTest = (Button)findViewById(R.id.btnTest);
        
        btnTest.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				tvText.setText(getInfoText(ActMain.this));
			}
		});
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }
}