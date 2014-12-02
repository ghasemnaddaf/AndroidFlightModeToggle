package ca.ubc.ece.naddaf.alarm1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class FlightModeDisabler extends BroadcastReceiver {
	private static final String TAG="FlightModeDisabler";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG,"/onReceive");
		int flightModeStatus=Settings.System.getInt				
				(context.getContentResolver(),
						Settings.Global.AIRPLANE_MODE_ON,-1);
		if (flightModeStatus==0)
			Log.d(TAG,"Flight mode is already disabled, nothing to do.");
		else
		{
			Settings.System.putInt(context.getContentResolver(),
					Settings.Global.AIRPLANE_MODE_ON,0);
			Log.d(TAG,"Flight Mode Disabled Successfully.");
		}
	}
}
