package miguel.comidas;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import miguel.comidas.DBAdapter.LocalBinder;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Ajustes extends PreferenceActivity {
	DBAdapter dbAdapter;
	boolean mBound;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.preferences);
        addPreferencesFromResource(R.layout.preferences);

        Preference setting = findPreference("acerca");
        setting.setIntent(new Intent (getApplicationContext(), Acerca.class));
        
        Preference restart = findPreference("reiniciar");
        restart.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Calendar calendar = Calendar.getInstance();
				Intent servicio = new Intent(getApplicationContext(), ServicioNotificador.class);

				//si queremos que se cree una notificacion de comida pasamos la clave "cena". Esto es debido a que el 
				//ServicioNotificador se ha programado para que lance la siguiente notificación: si la anterior fue cena
				//lanzamos una comida y viceversa.
				if(calendar.get(Calendar.HOUR_OF_DAY)<=13){
					if(calendar.get(Calendar.MINUTE)<30){ //preparar comida de ese día
						servicio.putExtra("cena", true);
						servicio.putExtra("id", calendar);
					} else { //preparar cena de ese día
						servicio.putExtra("comida", true);
						servicio.putExtra("id", calendar);
					}
				} else if(calendar.get(Calendar.HOUR_OF_DAY)<=20) { //preparar cena
					if(calendar.get(Calendar.MINUTE)<30){ //preparar cena de ese día
						servicio.putExtra("comida", true);
						servicio.putExtra("id", calendar);
					} else { //preparar comida del día siguiente
						calendar.add(Calendar.DAY_OF_MONTH, 1);
						servicio.putExtra("cena", true);
						servicio.putExtra("id", calendar);
					}
				} else { //preparar comida del día siguiente
					calendar.add(Calendar.DAY_OF_MONTH, 1);
					servicio.putExtra("cena", true);
					servicio.putExtra("id", calendar);
				}

				startService(servicio);
				Toast.makeText(getApplicationContext(), "Notificaciones reparadas correctamente", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
        
//        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
//        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar_settings, root, false);
//        root.addView(bar, 0); // insert at top
//        bar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        
        ActionBar actionBar = getActionBar();
        if(actionBar != null)
        	actionBar.setDisplayHomeAsUpEnabled(true);
        
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
        	finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	
}
