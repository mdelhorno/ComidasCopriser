package miguel.comidas;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Ajustes extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.preferences);
        addPreferencesFromResource(R.layout.preferences);
        Preference setting = findPreference("acerca");
        setting.setIntent(new Intent (getApplicationContext(), Acerca.class));
	}
}
