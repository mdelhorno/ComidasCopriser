package miguel.comidas;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

public class Notificador extends BroadcastReceiver {

	int NOTIFICATION_ID = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		String [] dia = bundle.getStringArray("comida");
		
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		CharSequence from = "Comidas Copriser";
		CharSequence message = "La comida de hoy es: "+dia[0];
		
		Intent inte = new Intent(context, Comidas.class);
		inte.putExtra("Notification", true);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, inte, PendingIntent.FLAG_CANCEL_CURRENT);
		
		Notification notificacion = new Notification(R.drawable.logotipo, message, System.currentTimeMillis());
		notificacion.setLatestEventInfo(context, from, message, contentIntent);
		notificacion.vibrate = new long[]{1000,500,1000};
		notificacion.ledARGB = Color.WHITE;
		notificacion.ledOnMS = 1;
		notificacion.ledOffMS = 0;
		notificacion.flags = notificacion.flags | Notification.FLAG_SHOW_LIGHTS;
		
		nm.notify(NOTIFICATION_ID, notificacion);

	}

}
