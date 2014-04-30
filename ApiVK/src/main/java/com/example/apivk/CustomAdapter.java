package com.example.apivk;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by Administrator on 03.02.14.
 */
public class CustomAdapter extends ArrayAdapter<String> {

    private String[] arr;
    ViewHolder holder;
    Context context;
    int imagePlay = R.drawable.playnormal;
    int imagePause = R.drawable.pausenormal;
    int imageStop = 0;
    public Integer pos = 0;
    public Boolean play;
    ListView listView;


    public CustomAdapter(Context context, int textViewResourceId, String[] arr) {
        super(context, textViewResourceId, 0, Arrays.asList(arr));

        this.arr = arr;
        this.context = context;
    }

    static class ViewHolder {
        TextView text;
        ImageView image;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //super.getView(position, convertView, parent);

        View v = convertView;

        //Небольшая оптимизация, которая позволяет повторно использовать объекты
        if (v == null) {
            //LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.custom, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) v.findViewById(R.id.textViewCustom);
            holder.image = (ImageView) v.findViewById(R.id.imageViewCustom);
            v.setTag(holder);
        }
        else{
            holder = (ViewHolder) v.getTag();
        }

        holder.text.setText(arr[position]);

        holder.image.setImageResource(imageStop);
        if(position == pos){
            //if(play)
                holder.image.setImageResource(imagePlay);
            //else
            //    holder.image.setImageResource(imagePlay);
        }


        /*holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == pos){
                    if(play)
                        holder.image.setImageResource(imagePause);
                }
            }
        });*/


        return v;
    }
}
