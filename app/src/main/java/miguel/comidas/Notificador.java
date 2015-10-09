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

/** BroadcastReceiver que se lanza a la hora que será lanzada la notificación (13:30 para la comida o 20:30 para la cena).
 * Cuando se recive, lanza el ServicioNotificador, preparando previamente el Intent que lo lanza.
 * 
 * @author Miguel
 *
 */
public class Notificador extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		String comida = extras.getString("comida");
		String cena = extras.getString("cena");
		Intent servicio = new Intent(context,ServicioNotificador.class);
		
		if(comida!=null){
			servicio.putExtra("comida", comida);
		}

        if(cena!=null){
			servicio.putExtra("cena", cena);
		}
		
		servicio.putExtra("lanzar", true);
		servicio.putExtra("id", extras.getSerializable("id"));
		
		context.startService(servicio);
	}
	
	


}
