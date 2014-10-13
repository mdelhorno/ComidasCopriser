package miguel.comidas;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import miguel.comidas.DBAdapter.LocalBinder;
import BBDD.DBHelper;
import BBDD.UsersColumns;
import BBDD.UsersTable;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class ServicioNotificador extends Service {

	int NOTIFICATION_ID = 0;	
	boolean esComida;
	SharedPreferences preferencias;
	
	private DBHelper dbHelper;

	private SQLiteDatabase db;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		
		Bundle bundle = intent.getExtras();
		String comida = bundle.getString("comida");
		String cena = bundle.getString("cena");
		
		NotificationManager nm = (NotificationManager)this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
		CharSequence from = null;
		CharSequence message = null;
		if(comida!=null){
			from = "Comida";
			message = comida;
			esComida = true;
		} else if(cena!=null){
			from = "Cena";
			message = cena;
			esComida = false;
		}
		
		Intent inte = new Intent(this.getApplicationContext(), Comidas.class);
		inte.putExtra("Notification", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, inte, PendingIntent.FLAG_CANCEL_CURRENT);
		
		Builder notification = new Notification.Builder(getApplicationContext());
		notification.setContentTitle(from)
			.setContentText(message)
			.setSmallIcon(R.drawable.logotipo)
			.setContentIntent(contentIntent)
			.setWhen(System.currentTimeMillis())
			.setDefaults(Notification.DEFAULT_ALL);
		
		nm.notify(NOTIFICATION_ID, notification.getNotification());	
		
		preferencias = PreferenceManager.getDefaultSharedPreferences(this);
		prepararSigNotificacion(bundle.getIntArray("id"));
	}
	
	@Override
	public void onCreate() {
		dbHelper = new DBHelper(this);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}
	
	private void prepararSigNotificacion(int[] is) {					
		if(esComida){
			int [] referencia = this.obtenerSemana(is[0], is[1], is[2]);
			String cena = getDia(referencia[0], referencia[1])[2];
			this.crearNotificacionCena(cena, is[0], is[1], is[2]);
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, is[1]);
			calendar.set(Calendar.YEAR, is[2]);
			calendar.set(Calendar.DAY_OF_MONTH, is[0]);
			calendar.set(Calendar.HOUR_OF_DAY,13);
			calendar.set(Calendar.MINUTE, 30);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.add(Calendar.DAY_OF_MONTH, 1);

			int [] referencia = this.obtenerSemana(is[0], is[1], is[2]);
			String comida = getDia(referencia[0], referencia[1])[0];
			this.crearNotificacionComida(comida,calendar);
		}
		
	}
	
	
	
	private void crearNotificacionComida(String comida, Calendar calendar) {
		// Para la comida		
		Intent intent = new Intent(getApplicationContext(), Notificador.class);
		intent.setAction("StartComida");
		intent.putExtra("comida", comida);
		intent.putExtra("id", new int[]{calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH),
				calendar.get(Calendar.YEAR)});
		
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);		
		
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		
		if(Calendar.getInstance().getTimeInMillis()<calendar.getTimeInMillis() && comprobarPreferencias(calendar))
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}

	private void crearNotificacionCena(String cena, int dayOfMonth, int month, int year) {	
		//Para la cena
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		calendar.set(Calendar.HOUR_OF_DAY,20);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		//calendar.set(Calendar.AM_PM, Calendar.PM);
		
		Intent intent = new Intent(getApplicationContext(), Notificador.class);
		intent.setAction("StartCena");
		intent.putExtra("id", new int[]{dayOfMonth,month,year});
		intent.putExtra("cena", cena);
		
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		if(Calendar.getInstance().getTimeInMillis()<calendar.getTimeInMillis() && comprobarPreferencias(calendar))
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}
	
	/**
	 * 
	 * @param calendar
	 * @return true si es día de diario y tenemos seleccionado el día de diario en las preferencias o
	 * si es fin de semana y tenemos seleccionado fin de semana en las preferencias. Devuelve false en caso contrario
	 */
	private boolean comprobarPreferencias(Calendar calendar) {
		Set<String> a = new HashSet<String>();
		a.add("diary");
		a.add("weekend");
		Set<String> s = preferencias.getStringSet("notificaciones", a);		
		return (s.contains("weekend") && (calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)) 
				|| (s.contains("diary") && (calendar.get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY || calendar.get(Calendar.DAY_OF_WEEK)==Calendar.TUESDAY 
				|| calendar.get(Calendar.DAY_OF_WEEK)==Calendar.WEDNESDAY || calendar.get(Calendar.DAY_OF_WEEK)==Calendar.THURSDAY 
				|| calendar.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY));
	}
	
	private int[] obtenerSemana(int dayOfMonth, int month, int year) {
		GregorianCalendar fecha = new GregorianCalendar(year, month, dayOfMonth);
		
		int semanaDelAño = fecha.get(Calendar.WEEK_OF_YEAR);	
		int [] retorno = {0,0};
		int referenciaSemana = 6;
		
		retorno[0] = (Math.abs(semanaDelAño-referenciaSemana)%6);
		
		if(retorno[0]==0){
			retorno[0]=6;
		}
		
		int diaSemana = fecha.get(Calendar.DAY_OF_WEEK);
		if(diaSemana==1){
			retorno[1]=7;
		} else {
			retorno[1] = diaSemana-1;
		}
		
		return retorno;
	}
	
	public String[] getDia(int semana, int dia) throws SQLException {
		Cursor result = db.query(true, UsersTable.TABLE_NAME, UsersTable.cols,
				UsersColumns.SEMANA+ "=" + semana + " AND " + UsersColumns.DIA + "=" + dia, 
				null, null, null, null,null);
		
		result.moveToFirst();
		String [] resultado = {result.getString(3), result.getString(4), result.getString(5)};
		
		return resultado;
	}

	
	

}
