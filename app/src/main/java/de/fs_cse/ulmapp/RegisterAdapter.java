package de.fs_cse.ulmapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.fs_cse.core.ObserverALU;

public class RegisterAdapter extends RecyclerView.Adapter<RegisterAdapter.ViewHolder> implements ObserverALU {
    private static final String TAG = "RegisterAdapter";

    private static final int NUM_REGS = 256;

    private boolean[] colorRead;
    private boolean[] colorWrite;

    private String[] register_numbers;
    private String[] register_contents;

    private Context context;

    public RegisterAdapter(Context context){
        this.context = context;
        initStrings();
        resetColor();
    }

    public void resetColor(){
        colorRead = new boolean[NUM_REGS];
        colorWrite = new boolean[NUM_REGS];
        notifyDataSetChanged();
    }

    private void initStrings(){
        register_numbers = new String[NUM_REGS];
        register_contents = new String[NUM_REGS];
        for(int i = 0; i < NUM_REGS; i++){
            String number = "0x" + Tools.getFormatedHex(i, 1);
            register_numbers[i] = number;
            register_contents[i] = Tools.getFormatedHex(0, 8);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.register_element, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.number.setText(register_numbers[position]);
        viewHolder.content.setText(register_contents[position]);
        if(colorRead[position]) viewHolder.number.setTextColor(Color.BLUE);
        if(colorWrite[position]) viewHolder.content.setTextColor(Color.RED);
        //viewHolder.layout.setOnClickListener(v -> {
        //    Toast.makeText(context, "Unsigned Interpretation: 00000", Toast.LENGTH_SHORT).show();
        //});
    }

    @Override
    public int getItemCount() {
        return NUM_REGS;
    }

    @Override
    public void onRead(int regId, long value) {
        colorRead[regId] = true;
        notifyItemChanged(regId);
    }

    @Override
    public void onWrite(int regId, long value) {
        register_contents[regId] = Tools.getFormatedHex(value, 8);
        colorWrite[regId] = true;
        notifyItemChanged(regId);
    }

    @Override
    public void reset() {
        initStrings();
        resetColor();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView number;
        TextView content;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.register_number);
            content = itemView.findViewById(R.id.register_content);
            layout = itemView.findViewById(R.id.register_layout);
        }
    }
}
