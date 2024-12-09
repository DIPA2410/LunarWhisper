package com.diparoy.lunarwhisper.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.diparoy.lunarwhisper.R

class XOXOFragment : Fragment() {
    private var board = Array(3) { IntArray(3) }
    private var isPlayerX = true
    private lateinit var gridLayout: GridLayout
    private lateinit var restButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_xoxo, container, false)
        gridLayout = rootView.findViewById(R.id.gridLayout)

        restButton = rootView.findViewById(R.id.resetButton)

        initializeGameBoard()
        initializeResetButton(rootView)

        return rootView
    }

    private fun initializeGameBoard() {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val button = Button(context)
                button.text = ""
                button.textSize = 24f
                button.setBackgroundResource(R.drawable.cell_background)
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = GridLayout.LayoutParams.WRAP_CONTENT
                params.rowSpec = GridLayout.spec(i, 1f)
                params.columnSpec = GridLayout.spec(j, 1f)
                button.layoutParams = params
                button.setOnClickListener {
                    onCellClick(i, j, button)
                }
                gridLayout.addView(button)
            }
        }
    }

    private fun initializeResetButton(rootView: View) {
        val resetButton = rootView.findViewById<Button>(R.id.resetButton)
        resetButton.setOnClickListener {
            resetGame()
        }
    }

    private fun onCellClick(row: Int, col: Int, button: Button) {
        if (board[row][col] == 0) {
            board[row][col] = if (isPlayerX) 1 else 2
            val mark = if (isPlayerX) "X" else "O"
            button.text = mark
            isPlayerX = !isPlayerX
            val result = checkWin(row, col)
            if (result == 1) {
                showMessage("Player X wins!")
            } else if (result == 2) {
                showMessage("Player O wins!")
            } else if (result == 3) {
                showMessage("It's a draw!")
            }
        }
    }

    private fun resetGame() {
        gridLayout.removeAllViews()

        board = Array(3) { IntArray(3) }
        gridLayout.rowCount = 3
        gridLayout.columnCount = 3

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val button = Button(requireContext())
                button.text = ""
                button.textSize = 24f
                button.setBackgroundResource(R.drawable.cell_background)
                button.setOnClickListener {
                    onCellClick(i, j, button)
                }
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = GridLayout.LayoutParams.WRAP_CONTENT
                params.rowSpec = GridLayout.spec(i, 1f)
                params.columnSpec = GridLayout.spec(j, 1f)
                button.layoutParams = params
                gridLayout.addView(button)
            }
        }

        isPlayerX = true
    }

    private fun checkWin(row: Int, col: Int): Int {
        for (i in 0 until 3) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                if (board[i][0] == 1) {
                    return 1
                } else if (board[i][0] == 2) {
                    return 2
                }
            }
        }

        for (j in 0 until 3) {
            if (board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                if (board[0][j] == 1) {
                    return 1
                } else if (board[0][j] == 2) {
                    return 2
                }
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] ||
            board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[1][1] == 1) {
                return 1
            } else if (board[1][1] == 2) {
                return 2
            }
        }

        if (isBoardFull()) {
            return 3
        }

        return 0
    }

    private fun isBoardFull(): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == 0) {
                    return false
                }
            }
        }
        return true
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
