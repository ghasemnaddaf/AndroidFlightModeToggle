package ca.ubc.ece.naddaf.alarm1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class FlightModeEnabler extends BroadcastReceiver {
	private static final String TAG="FlightModeEnabler";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG,"/onReceive");
		int flightModeStatus=Settings.System.getInt				
				(context.getContentResolver(),
						Settings.Global.AIRPLANE_MODE_ON,-1);
		if (flightModeStatus==1)
			Log.d(TAG,"Flight mode is already enabled, nothing to do.");
		else
		{
			Settings.System.putInt(context.getContentResolver(),
					Settings.Global.AIRPLANE_MODE_ON,1);
			Log.d(TAG,"Flight Mode Enabled Successfully.");
		}
	}
}
