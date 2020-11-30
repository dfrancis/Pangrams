/* Copyright 2020 David Francis <dfrancis22@gmail.com>
 *
 * Licensed under the MIT License (see LICENSE for details).
 *
 */
package com.github.dfrancis.pangrams

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.dfrancis.pangrams.R
import android.content.res.AssetManager
import android.widget.Toast
import android.widget.EditText
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.TextView
import android.text.Html
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class PangramsActivity() : AppCompatActivity() {
    private var wordMap: HashMap<String, HashSet<Char>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pangrams)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val assetManager = assets
        wordMap = HashMap()
        try {
            val inputStream = assetManager.open("words_alpha.txt")
            val reader = InputStreamReader(inputStream)
            val br = BufferedReader(reader)
            val iterator = br.lineSequence().iterator()
            while (iterator.hasNext()) {
                val line = iterator.next()
                val word = line.trim { it <= ' ' }
                val charSet = HashSet<Char>()
                for (i in 0 until word.length) {
                    charSet.add(word[i])
                }
                wordMap!![word] = charSet
            }
            br.close()
        } catch (e: IOException) {
            val toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG)
            toast.show()
        }
        // Set up the EditText box to process the content of the box when the user hits 'enter'
        val editText = findViewById<View>(R.id.editText) as EditText
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        editText.imeOptions = EditorInfo.IME_ACTION_GO
        editText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_GO || ((actionId == EditorInfo.IME_NULL) && (event != null) && (event.action == KeyEvent.ACTION_DOWN))) {
                processWord(editText)
                handled = true
            }
            handled
        })
    }

    private fun processWord(editText: EditText) {
        val resultView = findViewById<View>(R.id.resultView) as TextView
        val chars = editText.text.toString().trim { it <= ' ' }.toLowerCase()
        if (chars.length == 0) {
            return
        }
        val color = "#cc0029"
        val charSet = HashSet<Char>()
        for (i in 0 until chars.length) {
            charSet.add(chars[i])
        }
        // Log.i(TAG, charSet.toString())
        val wordList = ArrayList<String>()
        val hmIterator: Iterator<*> = wordMap!!.entries.iterator()
        while (hmIterator.hasNext()) {
            val mapElement = hmIterator.next() as Map.Entry<*, *>
            // Log.i(TAG, mapElement.key as String?)
            // Log.i(TAG, mapElement.value.toString())
            if ((mapElement.value == charSet)) {
                wordList.add(mapElement.key as String)
            }
        }
        for (i in wordList.indices) {
            resultView.append(Html.fromHtml(String.format("<font color=%s>%s</font><BR>", color, wordList[i])))
        }
        editText.setText("")
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_pangrams, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    fun defaultAction(view: View?): Boolean {
        var currentWord: String? = null
        val gameStatus = findViewById<View>(R.id.gameStatusView) as TextView
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        val editText = findViewById<View>(R.id.editText) as EditText
        val resultView = findViewById<View>(R.id.resultView) as TextView
        if (currentWord == null) {
            // gameStatus.setText(Html.fromHtml(String.format(START_MESSAGE, currentWord.toUpperCase(), currentWord)));
            fab.setImageResource(android.R.drawable.ic_menu_help)
            fab.hide()
            resultView.text = ""
            editText.setText("")
            editText.isEnabled = true
            editText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        } else {
            editText.setText(currentWord)
            editText.isEnabled = false
            fab.setImageResource(android.R.drawable.ic_media_play)
            currentWord = null
            // resultView.append(TextUtils.join("\n", anagrams));
            gameStatus.append(" Hit 'Play' to start again")
        }
        return true
    }

    companion object {
        val START_MESSAGE = "Enter characters to form a pangram word."
        private val TAG = "PangramsActivity"
    }
}