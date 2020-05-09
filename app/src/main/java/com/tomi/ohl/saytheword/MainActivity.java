package com.tomi.ohl.saytheword;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;
    private static final int RESULT_SPEECH = 0;
    private String felismerendo;
    private boolean felismerve = false;

    private TextView wordTextView;
    private Button sayItBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //checkMicAccess();
        sayItBtn = findViewById(R.id.sayItBtn);
        wordTextView = findViewById(R.id.wordTextView);

        //forgatas kezelese
        if (savedInstanceState != null) {
            felismerve = savedInstanceState.getBoolean("felismerve");
            felismerendo = savedInstanceState.getString("felismerendo");
        } else
            felismerendo = getRandomWord();
        wordTextView.setText(felismerendo);
        if (felismerve) {   //ha felismert szo mellett forgattuk el
            wordTextView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.green));
            sayItBtn.setText(getString(R.string.askNewWord));
        }

    }

    /*TODO: check if needed
    //engedely ellenorzese
    private void checkMicAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    //mi tortenjen, ha a user valasztott az engedelykezelo dialogban
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Engedély megadva", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Az alkalmazás nem műkdik az engedély megadása nélkül!", Toast.LENGTH_LONG).show();
            }
        }
    }
    */

    //string tombbol random szo valasztasa
    private String getRandomWord() {
        String[] szavak = getResources().getStringArray(R.array.szavak);
        Random r = new Random();
        int i = r.nextInt(szavak.length);
        return(szavak[i]);
    }

    //sajat szoveg megadasa dialog a megfelelo gombra kattintva
    public void customTextDialog(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edittext_dialog, null);
        dialogBuilder.setView(dialogView);
        final EditText edt = dialogView.findViewById(R.id.customEdit);
        edt.requestFocus();
        dialogBuilder.setMessage(getResources().getString(R.string.customTextDialog));
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                felismerve = false;
                felismerendo = edt.getText().toString();
                wordTextView.setText(felismerendo);
                wordTextView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.red));
                sayItBtn.setText(getString(R.string.sayItBtn));
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> dialog.cancel());
        final AlertDialog b = dialogBuilder.create();
        b.show();
    }

    //a szo kimondasa gomb
    public void sayItBtn(View v) {
        //ha nincs felismert szo, nyissa meg a felismero dialogot
        if (!felismerve) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.sayItBitch));
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hu-HU");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "hu-HU");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            try {
                startActivityForResult(intent, RESULT_SPEECH);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(), R.string.deviceNotSupported, Toast.LENGTH_SHORT).show();
            }
        } else {
            //egyebkent kerjen uj szavat
            felismerve = false;
            felismerendo = getRandomWord();
            wordTextView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.red));
            wordTextView.setText(felismerendo);
            sayItBtn.setText(getString(R.string.sayItBtn));
        }
    }

    //a felismert szoveg lekerese
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SPEECH) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null) {
                    String result = results.get(0);
                    Toast.makeText(this, getString(R.string.saidToast) + result + "\"", Toast.LENGTH_LONG).show();
                    validateResult(result);
                }
            }
        }
    }

    private void validateResult(String kimondott) {
        if (kimondott.equalsIgnoreCase(felismerendo)) {
            felismerve = true;
            wordTextView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.green));
            sayItBtn.setText(getString(R.string.askNewWord));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("felismerendo", felismerendo);
        outState.putBoolean("felismerve", felismerve);
    }

}
