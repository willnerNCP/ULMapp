package de.fs_cse.ulmapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class IOActivity extends AppCompatActivity{

    private EditText input;
    private TextView output;

    private static final int PICKFILE_RESULT_CODE = 8778;

    private static final String TAG = "IOActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_o);
        output = findViewById(R.id.ioOutput);
        input = findViewById(R.id.ioInput);
        output.setMovementMethod(new ScrollingMovementMethod());
        input.setMovementMethod(new ScrollingMovementMethod());
        input.setFilters(new InputFilter[]{new InputFilterASCII()});
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                IOBase.input = input.getText().toString();
            }
        });
        Button button = findViewById(R.id.io_load);
        button.setOnClickListener(v -> {
            readFile();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        output.setText(IOBase.output);
        input.setText(IOBase.input);
    }

    private void readFile(){
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
            IOBase.input = content.toString();
            input.setText(IOBase.input);

        }
    }
}