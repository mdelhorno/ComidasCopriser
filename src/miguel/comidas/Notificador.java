package miguel.comidas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Notificador extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		String comida = extras.getString("comida");
		String cena = extras.getString("cena");
		Toast.makeText(context,extras.getString("id"), Toast.LENGTH_SHORT).show();
		Intent servicio = new Intent(context,ServicioNotificador.class);
		
		if(comida!=null){
			servicio.putExtra("comida", comida);
		} else if(cena!=null){
			servicio.putExtra("cena", cena);
		}

		context.startService(servicio);
	}

}
