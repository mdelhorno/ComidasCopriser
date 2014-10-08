package miguel.comidas;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;


public class ServicioNotificador extends Service {

	int NOTIFICATION_ID = 0;
	
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
		
		CharSequence from = "Comidas Copriser";
		CharSequence message = null;
		if(comida!=null){
			message = "La comida de hoy es: "+comida;
		} else if(cena!=null){
			message = "La cena de hoy es: "+cena;
		}
		
		Intent inte = new Intent(this.getApplicationContext(), Comidas.class);
		inte.putExtra("Notification", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, inte, PendingIntent.FLAG_CANCEL_CURRENT);
		
		Notification notificacion = new Notification(R.drawable.logotipo, message, System.currentTimeMillis());
		notificacion.setLatestEventInfo(this.getApplicationContext(), from, message, contentIntent);
		notificacion.vibrate = new long[]{1000,500,1000};
		notificacion.ledARGB = Color.WHITE;
		notificacion.ledOnMS = 1;
		notificacion.ledOffMS = 0;
		notificacion.flags = notificacion.flags | Notification.FLAG_SHOW_LIGHTS;
		
		nm.notify(NOTIFICATION_ID, notificacion);
	}

}
