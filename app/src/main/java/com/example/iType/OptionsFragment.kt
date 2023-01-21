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


class OptionsFragment : Fragment() , FragmentControler {
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

    interface OptionsFragmentListener {
        fun onInputOptionsSent(input: CharSequence?)
    }

    private lateinit var upButton : Button
    private lateinit var leftButton : Button
    private lateinit var rightButton : Button
    private lateinit var downButton : Button
    private lateinit var listener : OptionsFragmentListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val v = inflater.inflate(R.layout.fragement_options, container, false)
        rightButton = v.findViewById(R.id.options_right_button)
        leftButton = v.findViewById(R.id.options_left_button)
        upButton = v.findViewById(R.id.options_up_button)
        downButton = v.findViewById(R.id.options_down_button)

        upButton.text = "Speak"
        rightButton.text = "Keyboard"
        leftButton.text = "back"
        downButton.text = ""

        upButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputOptionsSent("1")
        })
        rightButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputOptionsSent("3")
        })
        leftButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputOptionsSent("2")
        })
        downButton.setOnClickListener(View.OnClickListener {
            val input: CharSequence = rightButton.getText()
            listener.onInputOptionsSent("4")
        })
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is OptionsFragmentListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                    .toString() + " must implement FragmentAListener"
            )
        }
    }

}