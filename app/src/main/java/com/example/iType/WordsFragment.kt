package com.example.fragmentstutorial

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.iType.FragmentControler
import com.example.iType.R
import com.example.iType.Word
import java.util.*

class WordsFragment : Fragment() , FragmentControler {
    override fun clickRightButton() {
        rightButton.callOnClick()
    }

    override fun clickLeftButton() {
        leftButton.callOnClick()
    }

    override fun clickUpButton() {
        upButton.callOnClick()
    }

    override fun clickDownButton() {
        downButton.callOnClick()
    }

    interface WordsListListener {
        fun getWordlist() : Queue<Word>
    }
    interface WrodsFragmentListenr {
        fun onInputWordsSent(input: CharSequence?)
    }
     private lateinit var upButton : Button
    private lateinit var leftButton : Button
    private lateinit var rightButton : Button
    private lateinit var downButton : Button
    private lateinit var wlListner: WordsListListener
    private lateinit var listener : WrodsFragmentListenr
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragement_words, container, false)
        rightButton = v.findViewById(R.id.words_right_button)
        leftButton = v.findViewById(R.id.words_left_button)
        upButton = v.findViewById(R.id.words_up_button)
        downButton= v.findViewById(R.id.words_down_button)
        rightButton?.setOnClickListener {
            Toast.makeText(container!!.context, "This is right button", Toast.LENGTH_SHORT).show()
        }
        upButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputWordsSent(upButton.text)
        })
        rightButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputWordsSent(rightButton.text)
        })
        leftButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputWordsSent(leftButton.text)
        })
        downButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputWordsSent(downButton.text)
        })
        setWords(wlListner.getWordlist())
        return v
    }

    private fun setWords(wordsList : Queue<Word>){
        if(upButton != null && downButton != null && rightButton != null && leftButton != null) {
            if(wordsList.size >= 4) {
                upButton?.text = wordsList.elementAtOrNull(0)!!.word.toString()
                rightButton?.text = wordsList.elementAtOrNull(1)!!.word.toString()
                leftButton?.text = wordsList.elementAtOrNull(2)!!.word.toString()
                downButton?.text = wordsList.elementAtOrNull(3)!!.word.toString()
            }
            else if (wordsList.size == 3) {
            upButton?.text = wordsList.elementAtOrNull(0)!!.word.toString()
            rightButton?.text = wordsList.elementAtOrNull(1)!!.word.toString()
            leftButton?.text = wordsList.elementAtOrNull(2)!!.word.toString()
                downButton?.text = ""
        } else if (wordsList.size == 2) {
            upButton?.text = wordsList.elementAtOrNull(0)!!.word.toString()
            rightButton?.text = wordsList.elementAtOrNull(1)!!.word.toString()
                leftButton?.text = ""
                downButton?.text = ""
        } else if (wordsList.size == 1) {
            upButton?.text = wordsList.elementAtOrNull(0)!!.word.toString()
                rightButton?.text = ""
                leftButton?.text = ""
                downButton?.text = ""        }
        }
        else
        {

        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        wlListner = if (context is WordsListListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                    .toString() + " must implement FragmentAListener"
            )
        }
        listener = if (context is WrodsFragmentListenr) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                    .toString() + " must implement FragmentAListener"
            )
        }
    }
}
