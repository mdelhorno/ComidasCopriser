package miguel.comidas;

import BBDD.DBHelper;
import BBDD.UsersColumns;
import BBDD.UsersTable;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Binder;
import android.os.IBinder;

public class DBAdapter extends Service {

	private final IBinder mBinder = new LocalBinder();

	private DBHelper dbHelper;

	private SQLiteDatabase db;
	

	public class LocalBinder extends Binder {
		public DBAdapter getService() {
			return DBAdapter.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
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

	@Override
	public void onDestroy() {
		db.close();
	}
	
	public boolean insertarDia(int semana, int dia, String comida, String merienda, String cena){
		ContentValues newValues = new ContentValues();
				
		newValues.put(UsersColumns.SEMANA, semana);
		newValues.put(UsersColumns.DIA, dia);
		newValues.put(UsersColumns.COMIDA, comida);
		newValues.put(UsersColumns.MERIENDA, merienda);
		newValues.put(UsersColumns.CENA, cena);
				
		long i = db.insert(UsersTable.TABLE_NAME, null, newValues);
		return i > 0;
	}
	
	public boolean actualizarDia(int semana, int dia, String comida, String merienda, String cena){
		ContentValues newValues = new ContentValues();
		newValues.put(UsersColumns.COMIDA, comida);
		newValues.put(UsersColumns.MERIENDA, merienda);
		newValues.put(UsersColumns.CENA, cena);
		long i = db.update(UsersTable.TABLE_NAME, newValues, UsersColumns.SEMANA
				+ "=" + semana + " AND " + UsersColumns.DIA + "=" + dia, null);
		return i > 0;
	}
	
	public String[] getDia(int semana, int dia) throws SQLException {
		Cursor result = db.query(true, UsersTable.TABLE_NAME, UsersTable.cols,
				UsersColumns.SEMANA+ "=" + semana + " AND " + UsersColumns.DIA + "=" + dia, 
				null, null, null, null,null);
		
		result.moveToFirst();
		String [] resultado = {result.getString(3), result.getString(4), result.getString(5)};
		
		return resultado;
	}

	public Cursor getTodosDias() {
		return db.query(UsersTable.TABLE_NAME, UsersTable.cols, null, null,
				null, null, UsersColumns._ID);
	}

	public boolean borrarDia(int semana, int dia) {
		long i = db.delete(UsersTable.TABLE_NAME, UsersColumns.SEMANA
				+ "=" + semana + " AND " + UsersColumns.DIA + "=" + dia, null);
		return i > 0;
	}

}
