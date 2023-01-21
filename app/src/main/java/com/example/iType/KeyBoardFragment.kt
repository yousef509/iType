package com.example.fragmentstutorial

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.iType.FragmentControler
import com.example.iType.R


class KeyBoardFragment : Fragment(), FragmentControler {
    interface KeyboardFragmentListener {
        fun onInputKeyboardSent(input: CharSequence?)
    }

    private lateinit var upButton : Button
    private lateinit var leftButton : Button
    private lateinit var rightButton : Button
    private lateinit var downButton : Button
    private lateinit var listener : KeyboardFragmentListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val v = inflater.inflate(R.layout.fragment_keyboard, container, false)
        rightButton = v.findViewById(R.id.right_button)
        leftButton = v.findViewById(R.id.left_button)
        upButton = v.findViewById(R.id.up_button)
        downButton = v.findViewById(R.id.down_button)


        upButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputKeyboardSent("1")
            upButton.requestFocusFromTouch()
        })
        rightButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            rightButton.requestFocusFromTouch()
            listener.onInputKeyboardSent("3")

        })
        leftButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            leftButton.requestFocusFromTouch()
            listener.onInputKeyboardSent("2")
        })
        downButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            downButton.requestFocusFromTouch()
            listener.onInputKeyboardSent("4")
        })
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is KeyboardFragmentListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                    .toString() + " must implement FragmentAListener"
            )
        }
    }

    override fun clickRightButton() {
        rightButton.performClick()
    }

    override fun clickLeftButton() {
        leftButton.performClick()
    }

    override fun clickUpButton() {
        upButton.performClick()
    }

    override fun clickDownButton() {
        downButton.performClick()
    }

}