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

/** Servicio encargado de lanzar las notificaciones y de preparar la siguiente notificacion. 
 * Recibe un Intent que contiene la comida o cena (dependiendo de la notificacion de la que se trate) y el d�a del que
 * se trata. Una vez mostrada la notificacion, accede a la base de datos para coger la cena de ese d�a (si la notificaci�n
 * es de la comida) o la comida del d�a siguiente (si la notificaci�n es de la cena).
 * 
 * @author Miguel
 *
 */
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
		preferencias = PreferenceManager.getDefaultSharedPreferences(this);
		lanzarNotificacion(bundle);
		prepararSigNotificacion(bundle.getIntArray("id"));
		this.stopSelf();
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
	
	public void onDestroy() {
		db.close();		
		super.onDestroy();
	}
	
	/** Lanza la notificaci�n. Para saber de cual se trata accede a los extras del Intent que ha lanzado el servicio. 
	 * Los extras se le pasan como par�metro (en el objeto Bundle).
	 * 
	 * @param bundle extras del Intent.
	 */
	private void lanzarNotificacion(Bundle bundle){
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
	}
	
	/** Prepara la siguiente notificaci�n, la del d�a que se le pasa como par�metro. Si la notificaci�n que se ha lanzado
	 * es de la comida, prepara la notificaci�n de la cena de ese d�a, mientras que si la notificaci�n es de la cena,
	 * prepara la notificaci�n de la comida del d�a siguiente.
	 * 
	 * @param dia vector de enteros que lleva en la posicion 0 el d�a, en la 1 el mes y en la 2 el a�o.
	 */
	private void prepararSigNotificacion(int[] dia) {					
		if(esComida){
			int [] referencia = this.obtenerSemana(dia[0], dia[1], dia[2]);
			String cena = getDia(referencia[0], referencia[1])[2];
			this.crearNotificacionCena(cena, dia[0], dia[1], dia[2]);
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, dia[1]);
			calendar.set(Calendar.YEAR, dia[2]);
			calendar.set(Calendar.DAY_OF_MONTH, dia[0]);
			calendar.set(Calendar.HOUR_OF_DAY,13);
			calendar.set(Calendar.MINUTE, 30);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.add(Calendar.DAY_OF_MONTH, 1);

			int [] referencia = this.obtenerSemana(dia[0], dia[1], dia[2]);
			String comida = getDia(referencia[0], referencia[1])[0];
			this.crearNotificacionComida(comida,calendar);
		}	
	}
	
	/** Crea la notificaci�n para la comida de un d�a en concreto, el cual se le pasa como par�metro. La notificaci�n
     * saldr� a las 13:30 de ese d�a. Antes de lanzarla comprueba las preferencias del usuario.
	 * 
	 * @param comida
	 * @param calendar Calendario que contiene el d�a y la hora que se lanzar� la notificaci�n
	 */
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

	/** Crea la notificaci�n para la cena de un d�a en concreto, el cual se le pasa como par�metro. La notificaci�n
     * saldr� a las 20:30 de ese d�a. Antes de lanzarla comprueba las preferencias del usuario y adem�s no lanza la
     * notificaci�n si es un s�bado o un domingo.
	 * 
	 * @param cena cena de ese d�a
	 * @param dayOfMonth
	 * @param month
	 * @param year
	 */
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
		if(Calendar.getInstance().getTimeInMillis()<calendar.getTimeInMillis() && comprobarPreferencias(calendar) &&
				calendar.get(Calendar.DAY_OF_WEEK)!=Calendar.SATURDAY && calendar.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY)
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}
	
	/** Devuelve verdadero o falso si las preferencias se cumplen o no. Para ello comprueba si est�n activas las notificaciones
	 * de entre diario y es d�a de diario o si est�n activas las notificaciones de los fines de semana y es fin de semana.
	 * 
	 * @param calendar
	 * @return true si es d�a de diario y tenemos seleccionado el d�a de diario en las preferencias o
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
	
	/** Obtiene la semana y el d�a que hay que obtener de la base de datos a partir de un d�a dado. La base de datos est�
	 * organizada en 6 semanas, cada una de 7 d�as. Para obtener la semana de la que se trata utiliza la semana 6 como 
	 * referencia, pues esta semana es la semana con la cual empieza el men� desde la primera semana.
	 * 
	 * @param dayOfMonth
	 * @param month
	 * @param year
	 * @return
	 */
	private int[] obtenerSemana(int dayOfMonth, int month, int year) {
		GregorianCalendar fecha = new GregorianCalendar(year, month, dayOfMonth);
		
		int semanaDelA�o = fecha.get(Calendar.WEEK_OF_YEAR);	
		int [] retorno = {0,0};
		int referenciaSemana = 6;
		
		retorno[0] = (Math.abs(semanaDelA�o-referenciaSemana)%6);
		
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
	
	/** Obtiene la comida, la merienda y la cena de un d�a que se le pasa por par�metro. Recibe la semana y el d�a que 
	 * tiene que coger de la base de datos.
	 * 
	 * @param semana
	 * @param dia
	 * @return vector de String con la comida, merienda y cena de ese d�a
	 * @throws SQLException
	 */
	public String[] getDia(int semana, int dia) throws SQLException {
		Cursor result = db.query(true, UsersTable.TABLE_NAME, UsersTable.cols,
				UsersColumns.SEMANA+ "=" + semana + " AND " + UsersColumns.DIA + "=" + dia, 
				null, null, null, null,null);
		
		result.moveToFirst();
		String [] resultado = {result.getString(3), result.getString(4), result.getString(5)};
		
		return resultado;
	}

	
	

}
