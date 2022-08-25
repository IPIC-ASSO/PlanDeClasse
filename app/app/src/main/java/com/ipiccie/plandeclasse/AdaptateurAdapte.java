package com.ipiccie.plandeclasse;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

public class AdaptateurAdapte extends BaseAdapter {
    private final LayoutInflater layoutInflater;
    private final Context context;
    private final int[] places;

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
            holder.coche = convertView.findViewById(R.id.coche);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (place == 1){
            holder.coche.setChecked(true);
        }
        holder.coche.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                places[position] = 1;
            }else{
                places[position] = 0;
            }
            
        });
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
