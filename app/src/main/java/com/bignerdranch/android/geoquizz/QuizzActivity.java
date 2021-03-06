package com.bignerdranch.android.geoquizz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizzActivity extends Activity {

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mQuestionTextView;
    private TextView mHintNumberTextView;

    private static final String TAG = "QuizzActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_INDEX_CHEAT = "index_cheat";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };
    private int mCurrentIndex = 0;
    private boolean mIsCheater;
    //Massive with answer results
    private int[] mAnswerBank = new int[mQuestionBank.length];
    //Index to verify if all questions are completed
    private int mIndexCompleteQuizz = 0;
    //Sum of true answers
    private int sumAnswer = 0;
    //Index to verify if cheated
    private int cheat_index = 0;
    private int hint_number = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quizz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            cheat_index = savedInstanceState.getInt(KEY_INDEX_CHEAT, 0);
            //Recreate Bank of answer results
            mAnswerBank = savedInstanceState.getIntArray("score");
            //Recreate the index complete quizz
            mIndexCompleteQuizz = savedInstanceState.getInt("complete_quizz");
            hint_number = savedInstanceState.getInt("hint_number");
        }

        mHintNumberTextView = (TextView) findViewById(R.id.hint_number);
        mHintNumberTextView.setText("Hint: " + hint_number);

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    checkAnswer(true);
                    Log.d(TAG, String.valueOf(mCurrentIndex));
                    //After answer question disable buttons
                    mTrueButton.setEnabled(false);
                    mFalseButton.setEnabled(false);
                    mIndexCompleteQuizz++;
            }

        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    checkAnswer(false);
                    Log.d(TAG, String.valueOf(mCurrentIndex));
                    //After answer question disable buttons
                    mTrueButton.setEnabled(false);
                    mFalseButton.setEnabled(false);
                    mIndexCompleteQuizz++;
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
                Log.d(TAG, String.valueOf(mCurrentIndex));
                Log.d(TAG, String.valueOf(mAnswerBank[mCurrentIndex]));
                //Verify if question was completed or not
                //to enable or disable buttons
                if(mAnswerBank[mCurrentIndex] == 0) {
                    mTrueButton.setEnabled(true);
                    mFalseButton.setEnabled(true);
                } else {
                    mTrueButton.setEnabled(false);
                    mFalseButton.setEnabled(false);
                }
                checkResultQuizz();
                //reset cheat_index when go to another question
                cheat_index = 0;
            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex + (mQuestionBank.length-1)) % mQuestionBank.length;
                updateQuestion();
                Log.d(TAG, String.valueOf(mCurrentIndex));
                Log.d(TAG, String.valueOf(mAnswerBank[mCurrentIndex]));
                //Verify if question was completed or not
                //to enable or disable buttons
                if(mAnswerBank[mCurrentIndex] == 0) {
                    mTrueButton.setEnabled(true);
                    mFalseButton.setEnabled(true);
                } else {
                    mTrueButton.setEnabled(false);
                    mFalseButton.setEnabled(false);
                }
                checkResultQuizz();
                //reset cheat_index when go to another question
                cheat_index = 0;
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start CheatActivity
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizzActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        updateQuestion();

        //Save state of buttons when orientation change
        if(mAnswerBank[mCurrentIndex] == 0) {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        } else {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);

            //Verify if mIsCheater==true then cheat_index=1
            if(mIsCheater) {
                cheat_index = 1;
                hint_number--;
                mHintNumberTextView.setText("Hint: " + hint_number);
                if(hint_number <= 0)
                    mCheatButton.setEnabled(false);
            }
        }
    }

    //Update text question on TextView
    private void updateQuestion() {
        int question  = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    //Check if response right or not
    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = 0;
        if (mIsCheater || cheat_index==1) {
            messageResId = R.string.judgment_toast;
            //If cheated then don't receive result point
            //Actually you don't receive them because after answer buttons are blocked
            mAnswerBank[mCurrentIndex] = 2;
        }
        else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                //1 for correct answer
                mAnswerBank[mCurrentIndex] = 1;
            } else {
                messageResId = R.string.incorrect_toast;
                //2 for incorrect answer
                mAnswerBank[mCurrentIndex] = 2;
            }
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
                .show();
    }

    //Verify if the quizz is completed and make a toast with result
    private void checkResultQuizz() {
        if (mIndexCompleteQuizz == mAnswerBank.length) {
            for (int i = 0; i < mAnswerBank.length; i++)
                if (mAnswerBank[i] == 1)
                    sumAnswer++;
            String message = "Result: " + String.valueOf(sumAnswer) +
                    "/" + String.valueOf(mAnswerBank.length);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        sumAnswer = 0;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        //save cheat_index to not allow cheat when rotate screen
        savedInstanceState.putInt(KEY_INDEX_CHEAT, cheat_index);
        //Save Array of answers
        savedInstanceState.putIntArray("score", mAnswerBank);
        //Save index complete quizz
        savedInstanceState.putInt("complete_quizz", mIndexCompleteQuizz);
        savedInstanceState.putInt("hint_number", hint_number);
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}
