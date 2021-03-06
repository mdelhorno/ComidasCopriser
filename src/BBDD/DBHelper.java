package BBDD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import miguel.comidas.ServicioNotificador;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static String DATABASE_NAME = "database.db";
	private static int DATABASE_VERSION = 5; 
	private Context context;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UsersTable.SQL_CREATE);
		InputStream flujo;
		BufferedReader lector;
		
		flujo = context.getResources().openRawResource(0x7f050001);
		lector = new BufferedReader(new InputStreamReader(flujo));
			
		String linea;
		try {
			while((linea=lector.readLine())!=null){
				db.execSQL(linea);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				flujo.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Calendar calendar = Calendar.getInstance();
		Intent servicio = new Intent(context, ServicioNotificador.class);

		//si queremos que se cree una notificacion de comida pasamos la clave "cena". Esto es debido a que el 
		//ServicioNotificador se ha programado para que lance la siguiente notificaci�n: si la anterior fue cena
		//lanzamos una comida y viceversa.
		if(calendar.get(Calendar.HOUR_OF_DAY)<=13){
			if(calendar.get(Calendar.MINUTE)<30){ //preparar comida de ese d�a
				servicio.putExtra("cena", true);
				servicio.putExtra("id", calendar);
			} else { //preparar cena de ese d�a
				servicio.putExtra("comida", true);
				servicio.putExtra("id", calendar);
			}
		} else if(calendar.get(Calendar.HOUR_OF_DAY)<=20) { //preparar cena
			if(calendar.get(Calendar.MINUTE)<30){ //preparar cena de ese d�a
				servicio.putExtra("comida", true);
				servicio.putExtra("id", calendar);
			} else { //preparar comida del d�a siguiente
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				servicio.putExtra("cena", true);
				servicio.putExtra("id", calendar);
			}
		} else { //preparar comida del d�a siguiente
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			servicio.putExtra("cena", true);
			servicio.putExtra("id", calendar);
		}
		
		context.startService(servicio);
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + UsersTable.TABLE_NAME);
		onCreate(db);
	}

}
