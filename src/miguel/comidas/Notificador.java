package miguel.comidas;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import miguel.comidas.DBAdapter.LocalBinder;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Notificador extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		String comida = extras.getString("comida");
		String cena = extras.getString("cena");
		Intent servicio = new Intent(context,ServicioNotificador.class);
		
		if(comida!=null){
			servicio.putExtra("comida", comida);
		} else if(cena!=null){
			servicio.putExtra("cena", cena);
		}
		
		servicio.putExtra("id", extras.getIntArray("id"));
		
		context.startService(servicio);
	}
	
	


}
