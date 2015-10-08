package miguel.comidas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class MyAdapter extends SimpleAdapter {
	Context context;
	ArrayList<HashMap<String, String>> datos;
	
	public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to){
		super(context,data,resource,from,to);
		this.context = context;
		this.datos = (ArrayList<HashMap<String, String>>)data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = super.getView(position, convertView, parent);
		
		TextView comida = (TextView) view.findViewById(R.id.comida);
		TextView merienda = (TextView) view.findViewById(R.id.merienda);
		TextView cena = (TextView) view.findViewById(R.id.cena);

		return view;
	}


}
