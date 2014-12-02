package ca.ubc.ece.naddaf.alarm1;

import java.util.Calendar;

import android.support.v7.app.ActionBarActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TimePicker;


public class MainActivity extends ActionBarActivity {
	private static final String TAG = "AlarmMainActivity";
	private Switch mySwitch;
	private TimePicker startTime, stopTime;
	private Button saveButton;
	private int startHour, startMinute, stopHour, stopMinute;
	private AlarmManager alarmMgr;
	Intent startIntent, stopIntent;
	private Calendar startCal,stopCal;
	private PendingIntent startPi,stopPi;
	private SharedPreferences sharedPrefs;

	@Override
    protected void onCreate(Bundle savedInstanceState) {		
		Log.d(TAG,"onCreate started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //get views:
   	    startTime = (TimePicker) findViewById(R.id.timePicker1);
   	    stopTime  = (TimePicker) findViewById(R.id.timePicker2);
   	    saveButton = (Button) findViewById(R.id.button1);
        mySwitch = (Switch) findViewById(R.id.switch1);
   	    
        //attach a listener to check for changes in the switch
        Log.d(TAG,"Attaching OnCheckedChangeListener to Switch");
        mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, 
					boolean isChecked) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onCheckedChanged to "+isChecked);
				scheduleFlightMode(isChecked);
				//save switch status:
				SharedPreferences.Editor spEditor = sharedPrefs.edit();
				spEditor.putBoolean("schedulerIsEnabled", isChecked);
				spEditor.commit();
				setEnableOtherControls(isChecked);
			}
		});
        
        //attach a listener on save button
        Log.d(TAG,"Attaching OnClickListener to Button");
        saveButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
        		Log.d(TAG,"onClick...");
    		  	startHour = startTime.getCurrentHour();
    		  	startMinute = startTime.getCurrentMinute();
    		  	stopHour = stopTime.getCurrentHour();
    		  	stopMinute = stopTime.getCurrentMinute();
        		scheduleFlightMode(true); //true because button cannot be clicked when not active
        	}
        });

        //read prefs / set defaults:
   	    Log.d(TAG,"Read prefs / Set defaults");
        sharedPrefs = getPreferences(Context.MODE_PRIVATE);
		boolean schedulerIsEnabled = sharedPrefs.getBoolean("schedulerIsEnabled", false);	 	        	       
    	//default start time: 00:00, default stop time: 06:00
		loadTimeSharedPrefs();
		syncTimePickers();
    	
    	Log.d(TAG,"schedulerIsEnabled="+schedulerIsEnabled);
        mySwitch.setChecked(schedulerIsEnabled);
        if (!schedulerIsEnabled)
        	setEnableOtherControls(schedulerIsEnabled);    	
    }

 
	private void setEnableOtherControls(boolean isChecked) {
		Log.d(TAG,(isChecked?"en":"dis")+"abling other controls.");
		startTime.setEnabled(isChecked);
		stopTime.setEnabled(isChecked);
		saveButton.setEnabled(isChecked);		
	}


	public void scheduleFlightMode(boolean isChecked) {       
        if(isChecked){
        	//schedule flight mode enable/disable at start/stop times
        	//switchStatus.setText("Switch is currently ON");
        	Log.d(TAG,"scheduleFlightMode...");
		  	alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		  	if (alarmMgr==null)
		  		Log.e(TAG,"Unable to access AlarmManager");
		  	Log.d(TAG,"scheduleFlightMode/Start...");
		  	startCal = Calendar.getInstance();
		  	startCal.set(Calendar.HOUR_OF_DAY, startHour);
		  	startCal.set(Calendar.MINUTE, startMinute);
		  	startIntent = new Intent(this, FlightModeEnabler.class);		  	
		  	startPi = PendingIntent.getBroadcast(this, 0, startIntent, 0);
		  	Log.d(TAG,"Calling alarmMgr.setInexactRepeating for Enabler");
		  	alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startPi);
		        	  
		  	Log.d(TAG,"scheduleFlightMode/Stop...");
		  	stopCal = Calendar.getInstance();
		  	stopCal.set(Calendar.HOUR_OF_DAY,stopHour);
		  	stopCal.set(Calendar.MINUTE, stopMinute);
		  	stopIntent = new Intent(this, FlightModeDisabler.class); 
      	  	stopPi = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
      	  	Log.d(TAG,"Calling alarmMgr.setInexactRepeating for Disabler");
  	  		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, stopCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, stopPi);
  		  	
  	  		//save
  	  		if (saveTimeSharedPrefs())
  		  		Log.d(TAG,"Status save success.");
  		  	else
  		  		Log.e(TAG,"Status save failure.");
        }
        else{
      	  //cancel the scheduled alarm managers
        	Log.d(TAG,"scheduleFlightMode/Canceling PIs...");
        	syncTimePickers();
      	  	alarmMgr.cancel(startPi);
      	  	alarmMgr.cancel(stopPi);
        }      
   } //scheduleFlightMode
       	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	Log.d(TAG,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	Log.d(TAG,"onOptionsItemSelected");
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean saveTimeSharedPrefs()
    {
  	  	//save status of timers
  	  	Log.d(TAG,"scheduleFlightMode/Status saves...");
  	  	SharedPreferences.Editor spEditor = sharedPrefs.edit();
	  	spEditor.putInt("startHour", startHour);
	  	spEditor.putInt("startMinute", startMinute);
	  	spEditor.putInt("stopHour", stopHour);
	  	spEditor.putInt("stopMinute", stopMinute);
	  	return spEditor.commit();
    }
    
    private void loadTimeSharedPrefs()
    {
    	startHour = sharedPrefs.getInt("startHour", 0);
    	startMinute = sharedPrefs.getInt("startMinute", 0);
    	stopHour = sharedPrefs.getInt("stopHour", 6);
    	stopMinute = sharedPrefs.getInt("stopMinute", 0);    	
    }
    
    private void syncTimePickers()
    {
		startTime.setCurrentHour(startHour);
    	startTime.setCurrentMinute(startMinute);
    	stopTime.setCurrentHour(stopHour);
    	stopTime.setCurrentMinute(stopMinute);
    }
}
