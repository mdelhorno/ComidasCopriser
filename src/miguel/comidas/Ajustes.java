package miguel.comidas;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import miguel.comidas.DBAdapter.LocalBinder;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

public class Ajustes extends PreferenceActivity {
	SharedPreferences preferencias;
	TimePicker comida,cena;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.preferences);
        addPreferencesFromResource(R.layout.preferences);
        
        preferencias = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Preference setting = findPreference("acerca");
        setting.setIntent(new Intent (getApplicationContext(), Acerca.class));

        Preference restart = findPreference("reiniciar");
        restart.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Calendar calendar = Calendar.getInstance();
				Intent servicio = new Intent(getApplicationContext(), ServicioNotificador.class);

				int horaComida = preferencias.getInt("horaComida", 13);
	        	int minutoComida = preferencias.getInt("minutoComida", 30);
	        	int horaCena = preferencias.getInt("horaCena", 20);
	        	int minutoCena = preferencias.getInt("minutoCena", 30);
	        	
				//si queremos que se cree una notificacion de comida pasamos la clave "cena". Esto es debido a que el 
				//ServicioNotificador se ha programado para que lance la siguiente notificación: si la anterior fue cena
				//lanzamos una comida y viceversa.
				if(calendar.get(Calendar.HOUR_OF_DAY)<=horaComida){
					if(calendar.get(Calendar.MINUTE)<minutoComida){ //preparar comida de ese día
						servicio.putExtra("cena", true);
						servicio.putExtra("id", calendar);
					} else { //preparar cena de ese día
						servicio.putExtra("comida", true);
						servicio.putExtra("id", calendar);
					}
				} else if(calendar.get(Calendar.HOUR_OF_DAY)<=horaCena) { //preparar cena
					if(calendar.get(Calendar.MINUTE)<minutoCena){ //preparar cena de ese día
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

        Preference cambiar = findPreference("cambiar");
        cambiar.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {				
				int horaComida = preferencias.getInt("horaComida", 13);
	        	int minutoComida = preferencias.getInt("minutoComida", 30);
	        	int horaCena = preferencias.getInt("horaCena", 20);
	        	int minutoCena = preferencias.getInt("minutoCena", 30);
	        	   
				AlertDialog.Builder builder = new AlertDialog.Builder(Ajustes.this);
			    LayoutInflater inflater = getLayoutInflater();   
			    
			    View layout = inflater.inflate(R.layout.dialog_personal, null);
			    comida = (TimePicker) layout.findViewById(R.id.tiempocomida);
			    comida.setIs24HourView(true);
			    comida.setCurrentHour(horaComida);
			    comida.setCurrentMinute(minutoComida);
				cena = (TimePicker) layout.findViewById(R.id.tiempocena);
				cena.setIs24HourView(true);
				cena.setCurrentHour(horaCena);
			    cena.setCurrentMinute(minutoCena);
				
			    builder.setView(layout)
			       .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   
			        	   int horaComida2 = comida.getCurrentHour();
			        	   int minutoComida2 = comida.getCurrentMinute();
			        	   int horaCena2 = cena.getCurrentHour();
			        	   int minutoCena2 = cena.getCurrentMinute();
			        	   
			        	   Editor edit = preferencias.edit();
			        	   edit.putInt("horaComida", horaComida2)
			        	       .putInt("minutoComida", minutoComida2)
			        	       .putInt("horaCena", horaCena2)
			        	       .putInt("minutoCena", minutoCena2)
			        	       .commit();
			        	   
			        	   //Restaurar notificaciones
			        	   Calendar calendar = Calendar.getInstance();
			        	   Intent servicio = new Intent(getApplicationContext(), ServicioNotificador.class);

			        	   //si queremos que se cree una notificacion de comida pasamos la clave "cena". Esto es debido a que el 
			        	   //ServicioNotificador se ha programado para que lance la siguiente notificación: si la anterior fue cena
			        	   //lanzamos una comida y viceversa.			        	   
			        	   if(calendar.get(Calendar.HOUR_OF_DAY)<=horaComida2){
			        		   if(calendar.get(Calendar.MINUTE)<minutoComida2){ //preparar comida de ese día
			        			   servicio.putExtra("cena", true);
			        			   servicio.putExtra("id", calendar);
			        		   } else { //preparar cena de ese día
			        			   servicio.putExtra("comida", true);
			        			   servicio.putExtra("id", calendar);
			        		   }
			        	   } else if(calendar.get(Calendar.HOUR_OF_DAY)<=horaCena2) { //preparar cena
			        		   if(calendar.get(Calendar.MINUTE)<minutoCena2){ //preparar cena de ese día
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

			        	   Toast.makeText(getApplicationContext(), "Horas de notificaciones salvadas correctamente", 
			        			   Toast.LENGTH_SHORT).show();
			        	   dialog.cancel();
			           }
			        
			    })
			    	.setNegativeButton("Cancelar", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
				});
				
				builder.create().show();
				return true;
			}
		});
        
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
