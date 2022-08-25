package com.ipiccie.plandeclasse;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

public class AdaptateurAdapte extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Context context;
    private int[] places;

    public AdaptateurAdapte(Context aContext, int[] places) {
        this.context = aContext;
        this.places = places;
        layoutInflater = LayoutInflater.from(aContext);
    }


    @Override
    public int getCount() {
        return places.length;
    }

    @Override
    public Object getItem(int position) {
        return places[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int place = places[position];
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.case_grille, null);
            holder = new ViewHolder();
            holder.coche = (CheckBox) convertView.findViewById(R.id.coche);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //holder.populationView.setText("" + country.getPopulation());
        if (place ==1){
            holder.coche.setChecked(true);
        }
        return convertView;
    }

    // Find Image ID corresponding to the name of the image (in the directory mipmap).
    public int getMipmapResIdByName(String resName)  {
        String pkgName = context.getPackageName();

        // Return 0 if not found.
        int resID = context.getResources().getIdentifier(resName , "mipmap", pkgName);
        Log.i("CustomGridView", "Res Name: "+ resName+"==> Res ID = "+ resID);
        return resID;
    }

    static class ViewHolder {
        CheckBox coche;
    }

}
