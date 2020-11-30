/* Copyright 2020 David Francis <dfrancis22@gmail.com>
 *
 * Licensed under the MIT License (see LICENSE for details).
 *
 */

package com.github.dfrancis.pangrams;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class PangramsActivity extends AppCompatActivity {

    public static final String START_MESSAGE = "Enter characters to form a pangram word.";
    private HashMap<String, HashSet<Character>> wordMap;
    private static final String TAG = "PangramsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pangrams);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AssetManager assetManager = getAssets();
        wordMap = new HashMap<String, HashSet<Character>>();
        try {
            InputStream inputStream = assetManager.open("words_alpha.txt");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                String word = line.trim();
                HashSet<Character> charSet = new HashSet<Character>();
                for (int i = 0; i < word.length(); ++i) {
                    charSet.add(word.charAt(i));
                }
                wordMap.put(word, charSet);
            }
            br.close();
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        // Set up the EditText box to process the content of the box when the user hits 'enter'
        final EditText editText = (EditText) findViewById(R.id.editText);
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setImeOptions(EditorInfo.IME_ACTION_GO);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO || (
                        actionId == EditorInfo.IME_NULL && event != null && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    processWord(editText);
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void processWord(EditText editText) {
        TextView resultView = (TextView) findViewById(R.id.resultView);
        String chars = editText.getText().toString().trim().toLowerCase();
        if (chars.length() == 0) {
            return;
        }
        String color = "#cc0029";
        HashSet<Character> charSet = new HashSet<Character>();
        for (int i = 0; i < chars.length(); ++i) {
            charSet.add(chars.charAt(i));
        }
        // Log.i(TAG, ((HashSet<Character>) charSet).toString());
        String word = "Not found";
        ArrayList<String> wordList = new ArrayList<String>();
        Iterator hmIterator = wordMap.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            // Log.i(TAG, (String) mapElement.getKey());
            // Log.i(TAG, ((HashSet<Character>) mapElement.getValue()).toString());
            if (mapElement.getValue().equals(charSet)) {
                word = (String) mapElement.getKey();
                wordList.add(word);
            }
        }
        for (int i = 0; i < wordList.size(); i++) {
            resultView.append(Html.fromHtml(String.format("<font color=%s>%s</font><BR>", color, wordList.get(i))));
        }
        editText.setText("");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pangrams, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean defaultAction(View view) {
        String currentWord = null;
        TextView gameStatus = (TextView) findViewById(R.id.gameStatusView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        EditText editText = (EditText) findViewById(R.id.editText);
        TextView resultView = (TextView) findViewById(R.id.resultView);
        if (currentWord == null) {
            // gameStatus.setText(Html.fromHtml(String.format(START_MESSAGE, currentWord.toUpperCase(), currentWord)));
            fab.setImageResource(android.R.drawable.ic_menu_help);
            fab.hide();
            resultView.setText("");
            editText.setText("");
            editText.setEnabled(true);
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            editText.setText(currentWord);
            editText.setEnabled(false);
            fab.setImageResource(android.R.drawable.ic_media_play);
            currentWord = null;
            // resultView.append(TextUtils.join("\n", anagrams));
            gameStatus.append(" Hit 'Play' to start again");
        }
        return true;
    }
}
