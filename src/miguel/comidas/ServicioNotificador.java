package miguel.comidas;

import android.app.Notification;
import android.app.Notification.Builder;
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
		
		CharSequence from = null;
		CharSequence message = null;
		if(comida!=null){
			from = "Comida";
			message = comida;
		} else if(cena!=null){
			from = "Cena";
			message = cena;
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

}
