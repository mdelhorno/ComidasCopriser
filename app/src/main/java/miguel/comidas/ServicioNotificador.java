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
 * Recibe un Intent que contiene la comida o cena (dependiendo de la notificacion de la que se trate) y el día del que
 * se trata. Una vez mostrada la notificacion, accede a la base de datos para coger la cena de ese día (si la notificación
 * es de la comida) o la comida del día siguiente (si la notificación es de la cena).
 * También se utiliza para reparar o iniciar las notificaciones. Esto lo realiza cuando el intent que inicia este servicio no cuenta
 * con el campo "lanzar" en sus extras.
 * 
 * @author Miguel
 *
 */
public class ServicioNotificador extends Service {

	int NOTIFICATION_ID = 0;	
	SharedPreferences preferencias;
	boolean esComida;
	boolean sumar = false; //cuando esta variable vale true hay que sumar un día al calendario
	private DBHelper dbHelper;

	private SQLiteDatabase db;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public void onStart(Intent intent, int startId){
		Bundle bundle = intent.getExtras();
		preferencias = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(bundle.containsKey("lanzar") && bundle.getBoolean("lanzar")){ 
			//lanzamos la notificación
			lanzarNotificacion(bundle);
			sumar = true; //se suma cuando lanzamos la notificación. Si no, se trataría de un inicio o reinicio de las
			//notificaciones, y en ese caso ya se pasa la fecha correcta.
		}  
		esComida = bundle.containsKey("comida");
		prepararSigNotificacion((Calendar)bundle.getSerializable("id"));
		
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
	
	/** Lanza la notificación. Para saber de cual se trata accede a los extras del Intent que ha lanzado el servicio.
	 * Los extras se le pasan como parámetro (en el objeto Bundle).
	 * 
	 * @param bundle extras del Intent.
	 */
	private void lanzarNotificacion(Bundle bundle){
		String comida = bundle.getString("comida");
		String cena = bundle.getString("cena");		
		Calendar calendar = (Calendar) bundle.getSerializable("id");
		boolean lanzar = true; //expresa si se tiene que lanzar o no la notificación. Valdrá false cuando sea una cena del finde
		CharSequence from = null;
		CharSequence message = null;
		
		if(comida!=null){
			from = "Comida";
			message = comida;
		} else if(cena!=null){
			from = "Cena";
			message = cena;
			if(calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
				lanzar = false;
			}
		}
		
		if(lanzar && comprobarPreferencias(calendar)){
			NotificationManager nm = (NotificationManager)this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
			Intent inte = new Intent(this.getApplicationContext(), Comidas.class);
			inte.putExtra("Notification", true);
			PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, inte, PendingIntent.FLAG_ONE_SHOT 
					| PendingIntent.FLAG_CANCEL_CURRENT);
			
			Builder notification = new Notification.Builder(getApplicationContext());
			notification.setContentTitle(from)
				.setContentText(message)
				.setSmallIcon(R.drawable.logotipo)
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis())
				.setDefaults(Notification.DEFAULT_ALL);
			
			
			nm.notify(NOTIFICATION_ID, notification.getNotification());
		}
		
	}
	
	/** Prepara la siguiente notificación. Si la notificación actual es una comida, preparamos una cena, y viceversa
	 * 
	 * @param calendar fecha de la comida
	 */
	private void prepararSigNotificacion(Calendar calendar) {	
		if(esComida){ //preparamos una cena (la ultima notificación era una comida)
			if(preferencias.contains("horaCena") && preferencias.contains("minutoCena")){
				calendar.set(Calendar.HOUR_OF_DAY, preferencias.getInt("horaCena", 20));
				calendar.set(Calendar.MINUTE, preferencias.getInt("minutoCena", 30));
			} else {
				calendar.set(Calendar.HOUR_OF_DAY,20);
				calendar.set(Calendar.MINUTE, 30);
			}
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			
			int [] referencia = this.obtenerSemana(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), 
					calendar.get(Calendar.YEAR));
			String cena = getDia(referencia[0], referencia[1])[2];
			this.crearNotificacionCena(cena, calendar);
		} else {
			if(sumar)
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			if(preferencias.contains("horaComida") && preferencias.contains("minutoComida")){
				calendar.set(Calendar.HOUR_OF_DAY, preferencias.getInt("horaComida", 13));
				calendar.set(Calendar.MINUTE, preferencias.getInt("minutoComida", 30));
			} else {
				calendar.set(Calendar.HOUR_OF_DAY,13);
				calendar.set(Calendar.MINUTE, 30);
			}
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);

			int [] referencia = this.obtenerSemana(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), 
					calendar.get(Calendar.YEAR));
			String comida = getDia(referencia[0], referencia[1])[0];
			this.crearNotificacionComida(comida,calendar);
		}	
	}
	
	
	/** Crea la notificación para la comida de un día en concreto, el cual se le pasa como parámetro. La notificación
     * saldrá a las 13:30 de ese día. Antes de lanzarla comprueba las preferencias del usuario.
	 * 
	 * @param comida
	 * @param calendar Calendario que contiene el día y la hora que se lanzará la notificación
	 */
	private void crearNotificacionComida(String comida, Calendar calendar) {
		// Para la comida		
		Intent intent = new Intent(getApplicationContext(), Notificador.class);
		intent.setAction("StartComida");
		intent.putExtra("comida", comida);
		intent.putExtra("id", calendar);
		
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT 
				| PendingIntent.FLAG_UPDATE_CURRENT);		
		
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		
		if(Calendar.getInstance().getTimeInMillis()<calendar.getTimeInMillis())
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}

	
	/** Crea la notificación para la cena de un día en concreto, el cual se le pasa como parámetro. La notificación
     * saldrá a las 20:30 de ese día. Antes de lanzarla comprueba las preferencias del usuario y además no lanza la
     * notificación si es un sábado o un domingo.
	 * 
	 * @param cena cena de ese día
	 * @param calendar Calendario que contiene el día y la hora que se lanzará la notificación
	 */
	private void crearNotificacionCena(String cena, Calendar calendar) {	
		//Para la cena
		Intent intent = new Intent(getApplicationContext(), Notificador.class);
		intent.setAction("StartCena");
		intent.putExtra("id", calendar);
		intent.putExtra("cena", cena);
		
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT 
				| PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		if(Calendar.getInstance().getTimeInMillis()<calendar.getTimeInMillis())
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

	}
	
	/** Devuelve verdadero o falso si las preferencias se cumplen o no. Para ello comprueba si están activas las notificaciones
	 * de entre diario y es día de diario o si estín activas las notificaciones de los fines de semana y es fin de semana.
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
	
	/** Obtiene la semana y el día que hay que obtener de la base de datos a partir de un día dado. La base de datos está
	 * organizada en 6 semanas, cada una de 7 días. Para obtener la semana de la que se trata utiliza la semana 6 como
	 * referencia, pues esta semana es la semana con la cual empieza el menú desde la primera semana.
	 * 
	 * @param dayOfMonth
	 * @param month
	 * @param year
	 * @return
	 */
	private int[] obtenerSemana(int dayOfMonth, int month, int year) {
		GregorianCalendar fecha = new GregorianCalendar(year, month, dayOfMonth);
		
		int semanaDelAño = fecha.get(Calendar.WEEK_OF_YEAR);
		int [] retorno = {0,0};
		int referenciaSemana = 1;
		
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
	
	/** Obtiene la comida, la merienda y la cena de un día que se le pasa por parámetro. Recibe la semana y el día que
	 * tiene que coger de la base de datos.
	 * 
	 * @param semana
	 * @param dia
	 * @return vector de String con la comida, merienda y cena de ese día
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
