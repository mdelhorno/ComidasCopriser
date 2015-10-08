package miguel.comidas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import miguel.comidas.DBAdapter.LocalBinder;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import com.google.android.gms.ads.*;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class Comidas extends ActionBarActivity{

	private static final String MY_AD_UNIT_ID = "ca-app-pub-7843685751582225/5840237790";
	DatePicker calendario;
	ListView lista;
	private AdView adView;
	 
	DBAdapter dbAdapter;
	boolean mBound;
	
	int NOTIFICATION_ID = 0;
	
	String [] comidaDeHoy;
	    
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
        System.out.println(calendario.getCalendarViewShown());
        
        calendario.getCalendarView().setOnDateChangeListener(new OnDateChangeListener() {
			
			@Override
			public void onSelectedDayChange(CalendarView view, int year, int month,
					int dayOfMonth) {				
				calendario.init(year, month, dayOfMonth, null);
				mostrarComida(dayOfMonth,month,year);
				
			}
		});
        
        lista = (ListView)findViewById(R.id.listView1); 
        
     // Crear adView.
        adView = new AdView(this);
        adView.setAdUnitId(MY_AD_UNIT_ID);
        adView.setAdSize(AdSize.SMART_BANNER);

        // Buscar LinearLayout suponiendo que se le ha asignado
        // el atributo android:id="@+id/mainLayout".
        LinearLayout layout = (LinearLayout)findViewById(R.id.layoutPublicidad);
        
        // A�adirle adView.
        layout.addView(adView);

        // Iniciar una solicitud genérica.
        AdRequest adRequest = new AdRequest.Builder()
        	//.addTestDevice("57AF9A674E2ADCBFB1F632125D1FFC06")
        	.build();
        
        // Cargar adView con la solicitud de anuncio.
        adView.loadAd(adRequest);
    }
    
    
    
    /** Recupera y muestra la comida de un día determinado, el que se pasa como parámetro. Retorna la comida de ese día
     * en un vector de Strings.
     * 
     * @param dayOfMonth 
     * @param month
     * @param year
     * @return vector de String con la comida del día indicado (sin parsear).
     */
    protected String [] mostrarComida(int dayOfMonth, int month, int year) {
    	String [] from = new String[]{"comida","merienda","cena"};
    	int [] to = new int[]{R.id.comida,R.id.merienda,R.id.cena};
    	
        ArrayList<String[]> data = new ArrayList<String[]>(); 
        int [] referencia = this.obtenerSemana(dayOfMonth,month,year);
        String [] diaAux = dbAdapter.getDia(referencia[0], referencia[1]);
        String [] dia = this.parsear(diaAux);
        data.add(dia);
         
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
        return diaAux;
	}

	
	/** Parsea un día anteponiendo COMIDA, MERIENDA o CENA, dependiendo de lo que se trate, y quita las comas. Desde la base
	 * de datos nos viene: {"Ensalada campera, Muslo de pollo asado, Fruta","Bocadillo de queso","Sopa juliana, Marrajo, Lácteo"}.
	 * La salida sería {"COMIDA:\n   Ensalada campera\n   Muslo de pollo asado\n   Fruta","MERIENDA:\n   Bocadillo de queso",
	 * "CENA:\n   Sopa juliana\n   Marrajo\n   Lácteo"}
	 * 
	 * @param dia
	 * @return 
	 */
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


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comidas, menu);
        if(comidaDeHoy == null){
        	Calendar calendar = Calendar.getInstance();
        	int [] referencia = this.obtenerSemana(calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH),
        			calendar.get(Calendar.YEAR));
        	comidaDeHoy = dbAdapter.getDia(referencia[0], referencia[1]);
            
        }
        String[] retorno = this.parsear(comidaDeHoy); 
         
        //Botón compartir toda la comida del día
        MenuItem compartir = menu.findItem(R.id.compartir);
        ShareActionProvider actionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(compartir);
        String mensaje = retorno[0]+"\n"+retorno[1]+"\n"+retorno[2];;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,mensaje);
        actionProvider.setShareIntent(intent);                      
        
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
		adView.resume();
		
		if(!mBound){
			Intent intent = new Intent(this, DBAdapter.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	 @Override
	  public void onPause() {
		 adView.pause();
		 super.onPause();
		 if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	  }

	  @Override
	  public void onDestroy() {
		  adView.destroy();
		  super.onDestroy();
	  }
 

	private ServiceConnection mConnection = new ServiceConnection() {

		
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			dbAdapter = binder.getService();
			mBound = true;
			comidaDeHoy = mostrarComida(calendario.getDayOfMonth(), calendario.getMonth(), calendario.getYear());
		}

		
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

}