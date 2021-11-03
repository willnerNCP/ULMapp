package de.fs_cse.ulmapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.primitives.UnsignedLong;

import java.nio.file.attribute.UserDefinedFileAttributeView;

import de.fs_cse.core.ObserverMemory;
import de.fs_cse.core.ULM;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.ViewHolder> implements ObserverMemory {
    private static final String TAG = "MemoryAdapter";

    private final Switch lowHighAddresses;
    private RecyclerView recyclerView;

    private static final int NUM_COLS = 8; // how many bytes one recycler row holds

    private static final int DEFAULT = 0;
    private static final int READ = 1;
    private static final int WRITE = 2;

    private boolean ipOnHighAddresses;
    private int[] ipMarker; // [recycler position][0 for first 4 bytes, 1 for second 4 bytes]

    private boolean accessOnHighAddresses;
    private int accessRow; // recycler position of the access
    private int accessCol; // starting byte in the respective view holder
    private int accessNumBytes; // number of bytes accessed
    private int accessType; // whether bytes were read or wrote

    private ULM ulm;
    private Context context;

    public MemoryAdapter(Context context, ULM ulm, Switch addressesSwitch){
        this.context = context;
        this.ulm = ulm;
        //ADDRESSES SWITCH
        lowHighAddresses = addressesSwitch;
        lowHighAddresses.setTextColor(MainActivity.defaultColor);
        addressesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                recyclerView.scrollToPosition(getItemCount() - 1);
                lowHighAddresses.setText("High");
            }
            else {
                lowHighAddresses.setText("Low");
                recyclerView.scrollToPosition(0);
            }
            notifyDataSetChanged();
        });
        initMarkers();
        resetColor();
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void initMarkers(){
        ipMarker = new int[2];
    }

    public void resetColor(){
        accessRow = 0;
        accessCol = 0;
        accessNumBytes = 0;
        accessType = DEFAULT;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memory_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //HIGH LOW SWITCH
        long address;
        if(lowHighAddresses.isChecked()) {
            address = (position - getItemCount()) * NUM_COLS;
        } else {
            address = position * NUM_COLS;
        }

        //IP MARKER
        for (int i = 0; i < NUM_COLS / 4; i++) {
            holder.ip_markers[i].setVisibility(View.INVISIBLE);
        }
        if (lowHighAddresses.isChecked() == ipOnHighAddresses && position == ipMarker[0]) {
            holder.ip_markers[ipMarker[1]].setVisibility(View.VISIBLE);
        }

        //BYTES
        byte[] bytes = Tools.toByteArray(ulm.peekQuad(address), NUM_COLS);
        for(int i = 0; i < bytes.length; i++){
            holder.bytes[i].setText(Tools.getFormatedHex(bytes[i], 1));
            holder.bytes[i].setTextColor(MainActivity.defaultColor);
            int finalI = i;
            holder.bytes[i].setOnClickListener((event) -> {
                Toast.makeText(context, Tools.getFormatedHex(address + finalI, 8), Toast.LENGTH_SHORT).show();
            });
        }

        //ACCESS MARKERS
        if(lowHighAddresses.isChecked() == accessOnHighAddresses && accessRow == position) {
            int color = MainActivity.defaultColor.getDefaultColor();
            if(accessType == READ)
                color = Color.BLUE;
            else if(accessType == WRITE)
                color = Color.RED;
            for(int i = accessCol; i < accessCol+accessNumBytes; i++) {
                holder.bytes[i].setTextColor(color);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 4096;
    }

    public void setIPMarker(long address){
        UnsignedLong uLongAddress = UnsignedLong.fromLongBits(address);
        UnsignedLong uLongNumCols = UnsignedLong.fromLongBits(NUM_COLS);
        int row = (int) Math.floor(address / (NUM_COLS * 1.0F));
        if(address < 0) {
            ipOnHighAddresses = true;
            row += getItemCount();
        }else{
            ipOnHighAddresses = false;
        }
        int col = ((int) uLongAddress.mod(uLongNumCols).floatValue())/4;
        ipMarker[0] = row;
        ipMarker[1] = col;
        notifyDataSetChanged();
    }

    public void setAccessMarker(long address, int numBytes, int accessType){
        UnsignedLong uLongAddress = UnsignedLong.fromLongBits(address);
        UnsignedLong uLongNumCols = UnsignedLong.fromLongBits(NUM_COLS);
        accessRow = (int) Math.floor(address / (NUM_COLS * 1.0F));
        if(address < 0) {
            accessOnHighAddresses = true;
            accessRow += getItemCount();
        } else {
            accessOnHighAddresses = false;
        }
        accessCol = (int) uLongAddress.mod(uLongNumCols).floatValue();
        accessNumBytes = numBytes;
        this.accessType = accessType;
    }

    @Override
    public void onRead(long address, int numBytes, long value) {
        Log.d(TAG, "onRead: "+ Long.toHexString(address));
        setAccessMarker(address, numBytes, READ);
        notifyDataSetChanged();
    }

    @Override
    public void onWrite(long address, int numBytes, long value) {
        Log.d(TAG, "onWrite: "+ Long.toHexString(address));
        setAccessMarker(address, numBytes, WRITE);
        notifyDataSetChanged();
    }

    @Override
    public void onLoadProgram(byte[] program) {
        notifyDataSetChanged();
    }

    @Override
    public void reset() {
        notifyDataSetChanged();
        initMarkers();
        resetColor();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView[] bytes;
        public View[] ip_markers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ip_markers = new View[NUM_COLS/4];
            ip_markers[0] = itemView.findViewById(R.id.ip_0);
            ip_markers[1] = itemView.findViewById(R.id.ip_1);
            bytes = new TextView[NUM_COLS];
            bytes[0] = itemView.findViewById(R.id.byte_0);
            bytes[1] = itemView.findViewById(R.id.byte_1);
            bytes[2] = itemView.findViewById(R.id.byte_2);
            bytes[3] = itemView.findViewById(R.id.byte_3);
            bytes[4] = itemView.findViewById(R.id.byte_4);
            bytes[5] = itemView.findViewById(R.id.byte_5);
            bytes[6] = itemView.findViewById(R.id.byte_6);
            bytes[7] = itemView.findViewById(R.id.byte_7);
        }
    }
}
