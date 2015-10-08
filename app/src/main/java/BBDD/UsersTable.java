package BBDD;

public class UsersTable implements UsersColumns {
	public static final String TABLE_NAME = "comidas";
	public static final String [] cols = {_ID, SEMANA, DIA, COMIDA, MERIENDA, CENA};
	
	public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ SEMANA + " INTEGER, " 
			+ DIA + " INTEGER, "
			+ COMIDA + " TEXT NOT NULL, "
			+ MERIENDA + "  NULL, "
			+ CENA + " TEXT NOT NULL); ";
}
