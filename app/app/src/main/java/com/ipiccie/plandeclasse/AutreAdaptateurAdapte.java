package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class AutreAdaptateurAdapte extends BaseAdapter {
    private final LayoutInflater layoutInflater;
    private final Context context;
    private final String[] eleves;
    private final boolean[] selection;

    public AutreAdaptateurAdapte(Context aContext, String[] eleves, boolean[] selection) {
        this.context = aContext;
        this.eleves = eleves;
        this.selection = selection;
        layoutInflater = LayoutInflater.from(aContext);
    }


    @Override
    public int getCount() {
        return selection.length;
    }

    @Override
    public Object getItem(int position) {
        return eleves[position];
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        AdaptateurAdapte.ViewHolder holder;
        String eleve = eleves[position];
        boolean selectione = selection[position];
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.case_grille, null);
            holder = new AdaptateurAdapte.ViewHolder();
            holder.coche = convertView.findViewById(R.id.coche);
            convertView.setTag(holder);
        } else {
            holder = (AdaptateurAdapte.ViewHolder) convertView.getTag();
        }
        holder.coche.setText(eleve);
        holder.coche.setChecked(selectione);
        holder.coche.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.coche.setOnClickListener(v -> {
            selection[pos] = !selection[pos];
        });
        return convertView;
    }


    static class ViewHolder {
        CheckBox coche;
    }
}
