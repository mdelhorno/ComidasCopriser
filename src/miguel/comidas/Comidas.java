package miguel.comidas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import miguel.comidas.DBAdapter.LocalBinder;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.DatePicker;
import android.widget.ListView;


public class Comidas extends Activity{

	// hacer en los ajustes que te avise cuando haya una comida x.
	DatePicker calendario;
	ListView lista;
	
	DBAdapter dbAdapter;
	boolean mBound;
	
	int NOTIFICATION_ID = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_comidas);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        if(this.getIntent().getExtras() != null){
        	Boolean notif=getIntent().getExtras().getBoolean("Notification");
        	NotificationManager nm;
        	nm=(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        	
        	if(notif!=null && notif){
        		nm.cancel(NOTIFICATION_ID);
        	}
        }
        
        calendario = (DatePicker) findViewById(R.id.datePicker1);
        calendario.getCalendarView().setOnDateChangeListener(new OnDateChangeListener() {
			
			@Override
			public void onSelectedDayChange(CalendarView view, int year, int month,
					int dayOfMonth) {				
				calendario.init(year, month, dayOfMonth, null);
				mostrarComida(dayOfMonth,month,year);
				
			}
		});
        
        lista = (ListView)findViewById(R.id.listView1);
    }


    protected void mostrarComida(int dayOfMonth, int month, int year) {
    	String [] from = new String[]{"comida","merienda","cena"};
    	int [] to = new int[]{R.id.comida,R.id.merienda,R.id.cena};
    	
     // Creamos los datos {Nombre, Apellidos, Sexo} 
        ArrayList<String[]> data = new ArrayList<String[]>(); 
        int [] referencia = this.obtenerSemana(dayOfMonth,month,year);
        String [] diaAux = dbAdapter.getDia(referencia[0], referencia[1]);
        String [] dia = this.parsear(diaAux);
        data.add(dia);
        
        // Asignamos las claves a los datos en un HashMap 
        ArrayList<HashMap<String, String>> People = new ArrayList<HashMap<String, String>>(); 
        for (String[] person : data) { 
        	HashMap<String, String> personData = new HashMap<String, String>();
        	personData.put("comida", person[0]); 
        	personData.put("merienda", person[1]);  
        	personData.put("cena", person[2]); 
        	People.add(personData); 
        }
        
        MyAdapter ListAdapter = new MyAdapter(this, People, R.layout.list_row, from, to); 
        lista.setAdapter(ListAdapter);
        crearNotificacion(diaAux, dayOfMonth, month, year);
	}

	private void crearNotificacion(String[] dia, int dayOfMonth, int month, int year) {
		// Para la comida
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		calendar.set(Calendar.HOUR_OF_DAY,13);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.AM_PM, Calendar.PM);
		
		Intent intent = new Intent(getApplicationContext(), Notificador.class);
		intent.setAction("Start");
		intent.putExtra("comida", dia[0]);
		
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
		
		//Para la cena
		calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		calendar.set(Calendar.HOUR_OF_DAY,20);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.AM_PM, Calendar.PM);
		
		intent = new Intent(getApplicationContext(), Notificador.class);
		intent.setAction("Start");
		intent.putExtra("cena", dia[1]);
		
		sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}


	private String[] parsear(String[] dia) {
		String [] retorno = {"COMIDA:\n   ","MERIENDA:\n   "+dia[1],"CENA:\n   "};
		int i=0;
		
		while(i<dia[0].length() && dia[0].charAt(i)!=','){
			retorno[0]+=dia[0].charAt(i);
			i++;
		}
		i+=2;
		retorno[0]+="\n   ";
		while(i<dia[0].length() && dia[0].charAt(i)!=','){
			retorno[0]+=dia[0].charAt(i);
			i++;
		}
		i+=2;
		retorno[0]+="\n   ";
		while(i<dia[0].length()){
			retorno[0]+=dia[0].charAt(i);
			i++;
		}
		
		i=0;
		while(i<dia[2].length() && dia[2].charAt(i)!=','){
			retorno[2]+=dia[2].charAt(i);
			i++;
		}
		i+=2;
		retorno[2]+="\n   ";
		while(i<dia[2].length() && dia[2].charAt(i)!=','){
			retorno[2]+=dia[2].charAt(i);
			i++;
		}
		i+=2;
		retorno[2]+="\n   ";
		while(i<dia[2].length()){
			retorno[2]+=dia[2].charAt(i);
			i++;
		}
		
		return retorno;
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


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comidas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	Intent intent = new Intent(getApplicationContext(),Ajustes.class);
        	startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_comidas, container, false);
            return rootView;
        }
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		Intent intent = new Intent(this, DBAdapter.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(this.getIntent().getExtras() != null){
        	Boolean notif=getIntent().getExtras().getBoolean("Notification");
        	NotificationManager nm;
        	nm=(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        	
        	if(notif!=null && notif){
        		nm.cancel(NOTIFICATION_ID);
        	}
        }
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			dbAdapter = binder.getService();
			mBound = true;
			mostrarComida(calendario.getDayOfMonth(), calendario.getMonth(), calendario.getYear());
		}

		
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

}