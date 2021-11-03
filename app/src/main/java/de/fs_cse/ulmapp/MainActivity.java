package de.fs_cse.ulmapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;

import de.fs_cse.core.IODevice;
import de.fs_cse.core.ObserverULM;
import de.fs_cse.core.ULM;

public class MainActivity extends AppCompatActivity implements IODevice, ObserverULM {
    private static final String TAG = "MainActivity";
    private static final int PICKFILE_RESULT_CODE = 1234;

    private RecyclerView registerRecycler;
    private RegisterAdapter registerAdapter;
    private RecyclerView memoryRecycler;
    private MemoryAdapter memoryAdapter;


    private TextView output;
    private TextView input;

    private TextView[] flags;
    private static final int ZF = 0;
    private static final int OF = 1;
    private static final int CF = 2;
    private static final int SF = 3;

    private TextView nextInstruction;
    private TextView disassembly;

    private ULM ulm;

    public static ColorStateList defaultColor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //TEXTFIELDS
        nextInstruction = findViewById(R.id.next_content);
        disassembly = findViewById(R.id.dis_content);
        //FLAGS
        flags = new TextView[4];
        flags[ZF] = findViewById(R.id.zf);
        flags[OF] = findViewById(R.id.of);
        flags[CF] = findViewById(R.id.cf);
        flags[SF] = findViewById(R.id.sf);
        //IO
        input = findViewById(R.id.input);
        output = findViewById(R.id.output);
        //DEFAULT COLOR
        if(defaultColor == null) defaultColor = disassembly.getTextColors();
        //MODEL
        ulm = new ULM(this);
        ulm.addObserver(this);
        //RECYCLERS
        initRegisterRecycler();
        initMemoryRecycler();
        //REGISTER OBSERVERS
        ulm.addObserverALU(registerAdapter);
        ulm.addObserverMemory(memoryAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inputChanged();
        outputChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_run) {
            while(ulm.step());
            registerAdapter.resetColor();
            memoryAdapter.resetColor();
            return true;
        }else if(itemId == R.id.action_step){
            registerAdapter.resetColor();
            memoryAdapter.resetColor();
            ulm.step();
            return true;
        }else if(itemId == R.id.action_reset){
            ulm.reset();
            resetIO();
            return true;
        }else if(itemId == R.id.action_load){
            ulm.reset();
            IOBase.output = "";
            loadProgram();
            return true;
        }else if(itemId == R.id.action_io){
            Intent intent = new Intent(this, IOActivity.class);
            startActivity(intent);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
        //TODO: Reload (reload program and input)
    }

    private void resetIO(){
        IOBase.output = "";
        IOBase.input = "";
    }

    private void loadProgram(){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("text/plain");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            String content = Tools.readURI(data.getData(), getContentResolver());
            byte[] program = Tools.parseProgramm(content);
            ulm.loadProgram(program);
        }

    }

    private void initRegisterRecycler(){
        registerRecycler = findViewById(R.id.register_recycler);
        registerAdapter = new RegisterAdapter(this);
        registerRecycler.setAdapter(registerAdapter);
        registerRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initMemoryRecycler(){
        memoryRecycler = findViewById(R.id.memory_recycler);
        memoryAdapter = new MemoryAdapter(this, ulm, findViewById(R.id.switchLowHigh));
        memoryRecycler.setAdapter(memoryAdapter);
        memoryRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void putc(byte c) {
        IOBase.output += (char) (c & 0xFF);
        outputChanged();
    }

    @Override
    public byte getc() {
        char result = IOBase.inputPop();
        inputChanged();
        return (byte)result;
    }

    public void outputChanged(){
        if(IOBase.output.equals("")){
            output.setText("Output");
            return;
        }
        String trimmed = IOBase.output.trim();
        int index = trimmed.lastIndexOf('\n');
        output.setText(index >= 0 ? trimmed.substring(index+1) : trimmed);
    }

    public void inputChanged(){
        if(IOBase.input.equals("")){
            input.setText("Input");
            return;
        }
        int index = IOBase.input.indexOf('\n');
        input.setText(index >= 0 ? IOBase.input.substring(0, index) : IOBase.input);
    }

    @Override
    public boolean hasNextChar() {
        return IOBase.input.length() != 0;
    }

    @Override
    public void nextInstruction(long address, int opfield, String disassembly) {
        memoryAdapter.setIPMarker(address);
        nextInstruction.setText(Tools.getFormatedHex(opfield, 4));
        this.disassembly.setText(disassembly);
    }

    @Override
    public void onHalt(int exitCode, String errorMessage) {
        if(errorMessage == null){
            errorMessage = "There was no error message.";
        }else{
            errorMessage = "Error message: " + errorMessage;
        }
        new AlertDialog.Builder(this)
                .setTitle("ULM halted")
                .setMessage("ULM halted with exitcode " + exitCode + ".\n" + errorMessage)
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    public void onBlock() {
        new AlertDialog.Builder(this)
                .setTitle("ULM blocked")
                .setMessage("The execution was paused, because 'getc' was called, when the input buffer was empty.\n" +
                        "Please enter new input to continue!")
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    public void flagCallback(boolean zf, boolean of, boolean cf, boolean sf) {
        setFlag(ZF, zf);
        setFlag(OF, of);
        setFlag(CF, cf);
        setFlag(SF, sf);
    }

    private void setFlag(int flag, boolean status){
        flags[flag].setTextColor(status ? Color.GREEN : defaultColor.getDefaultColor());
    }

    @Override
    public void reset() {
        nextInstruction.setText(R.string.standard_instruction);
        disassembly.setText(R.string.please_load);
        flagCallback(false, false, false, false);
        outputChanged();
        inputChanged();
    }
}