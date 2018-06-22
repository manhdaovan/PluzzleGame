package com.manhdaovan.pluzzlegame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;

import com.manhdaovan.pluzzlegame.utils.Constants;
import com.manhdaovan.pluzzlegame.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GamePlayActivity extends AppCompatActivity {
    private class PieceInfo {
        private int correctPos;
        private int currentPos;

        PieceInfo(int correctPosition, int currentPosition) {
          correctPos = correctPosition;
          currentPos = currentPosition;
        }

        public void setCurrentPos(int currentPosition) {
            currentPos = currentPosition;
        }

        public boolean isCorrected() { return correctPos == currentPos; }
    }

    private static final String TAG = GamePlayActivity.class.getSimpleName();

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final HashMap<ImageView, PieceInfo> gameInfo = new HashMap<>();

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private LinearLayout gamePlaySection;
    private List<ImageView> selectingPieces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_play);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        Intent gameSetting = getIntent();
        String resourceFolder = gameSetting.getStringExtra(Constants.INTENT_GAME_RESOURCE_FOLDER);
        int rowPieces = gameSetting.getIntExtra(Constants.INTENT_ROW_PIECES, Constants.DEFAULT_ROW_NUM_PIECES);
        int columnPieces = gameSetting.getIntExtra(Constants.INTENT_COLUMN_PIECES, Constants.DEFAULT_COLUMN_NUM_PIECES);

        gamePlaySection = (LinearLayout) findViewById(R.id.game_play_section);
        initGame(resourceFolder, rowPieces, columnPieces);
        selectingPieces = new ArrayList<>();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(300);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            show();
            delayedHide(3000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO: add confirming box here
        this.finish();
    }

    public void initGame(String resourceFolder, int rowPieces, int columnPieces) {
        List<File> pieceImgs = getAllPieceImgs(resourceFolder, rowPieces, columnPieces);

        if(pieceImgs == null) {
            Utils.alert(getApplicationContext(), "Cannot get pieces");
            return;
        }

        prepareGameInfo(pieceImgs, rowPieces, columnPieces);
        printGamePlayScreen(rowPieces, columnPieces);
    }

    private int calculatePos(int row, int col, int columnPieces) {
        return row * columnPieces + col;
    }

    private void printGamePlayScreen(int rowPieces, int columnPieces){
        gamePlaySection.setWeightSum(Constants.EACH_ROW_HEIGHT_WEIGHT * rowPieces);

        for (int numRow = 0; numRow < rowPieces; numRow ++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(Constants.EACH_PIECE_WIDTH_WEIGHT * columnPieces);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, Constants.EACH_ROW_HEIGHT_WEIGHT);
            row.setLayoutParams(rowParams);

            for (int numCol = 0; numCol < columnPieces; numCol++) {
                int piecePosition = calculatePos(numRow, numCol, columnPieces);
                ImageView piece = getImgView(piecePosition);
                if(piece != null) row.addView(piece);
            }

            gamePlaySection.addView(row);
        }
    }

    private ImageView getImgView(int currentPosition){
        Iterator it = gameInfo.entrySet().iterator();

        while(it.hasNext()){
            HashMap.Entry<ImageView, PieceInfo> pair = (HashMap.Entry)it.next();
            ImageView iv = pair.getKey();
            PieceInfo pi = pair.getValue();
            if(pi.currentPos == currentPosition) return iv;
        }

        return null;
    }

    private int randomPosition(HashMap<Integer, Boolean> randomed) {
        int randomNum = Utils.randomRange(0, randomed.size() - 1);

        while(randomed.get(randomNum)) { randomNum = Utils.randomRange(0, randomed.size() - 1); }

        randomed.put(randomNum, true);
        return randomNum;
    }

    private void prepareGameInfo(List<File> pieceImgs, int rowPieces, int columnPieces){
        HashMap<Integer, Boolean> randomed = new HashMap<>();
        for(int i = 0; i < rowPieces * columnPieces; i++){
            randomed.put(i, false);
        }

        for (int numRow = 0; numRow < rowPieces; numRow ++) {
            for (int numCol = 0; numCol < columnPieces; numCol++) {
                int piecePosition = calculatePos(numRow, numCol, columnPieces);
                ImageView piece = buildPiece(pieceImgs.get(piecePosition).getAbsolutePath());
                int currentPosition = randomPosition(randomed);
                gameInfo.put(piece, new PieceInfo(piecePosition, currentPosition));
            }
        }
    }

    private List<File> getAllPieceImgs(String resourceFolder, int rowPieces, int columnPieces){
        if (resourceFolder == null) {
            Utils.alert(getApplicationContext(), "Cannot find resource folder");
            return null;
        }

        File gamePiecesFolder = new File(getApplicationContext().getFilesDir(), resourceFolder);
        List<File> allFiles = Utils.getDirs(gamePiecesFolder, Utils.MODE_FILE_ONLY);

        List<File> pieceImgs = new ArrayList<>();
        for(File f: allFiles){
            if(!f.getName().equals(Constants.defaultCroppedFileName())) pieceImgs.add(f);
        }

        if (pieceImgs.size() != rowPieces * columnPieces) {
            Utils.alert(getApplicationContext(), "This image has been used before. Please try another image!");
            return null;
        }

        return pieceImgs;
    }

    private ImageView buildPiece(String pieceFilePath){
        final ImageView piece = new ImageView(this);
        piece.setPadding(Constants.PIECE_PADDING_LEFT, Constants.PIECE_PADDING_TOP, Constants.PIECE_PADDING_RIGHT, Constants.PIECE_PADDING_BOTTOM);
        piece.setScaleType(ImageView.ScaleType.FIT_XY);

        LinearLayout.LayoutParams pieceParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Constants.EACH_PIECE_WIDTH_WEIGHT);
        piece.setLayoutParams(pieceParams);

        piece.setImageBitmap(BitmapFactory.decodeFile(pieceFilePath));
        piece.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                piece.setImageAlpha(70);
                selectingPieces.add(piece);

                if(isNeedCheckSwap()) {
                    if(!swap()) Utils.alert(getApplicationContext(), "CANNOT SWAP");
                    else {
                        resetSelectingPiecesAlpha();
                        resetSelectingPieces();
                    }
                }
            }
        });

        return piece;
    }

    private boolean isNeedCheckSwap(){
        return selectingPieces.size() == 2;
    }

    private void resetSelectingPiecesAlpha(){
        for(ImageView imageView: selectingPieces){
            imageView.setImageAlpha(255);
        }
    }

    private void resetSelectingPieces(){
        selectingPieces = new ArrayList<>();
    }

    private boolean swap(){
        if (selectingPieces.size() < 1) return false;

        Log.e(TAG,"Complete ----- " + isGameCompleted());
        ImageView tmpPiece = new ImageView(this);
        ImageView firstPiece = selectingPieces.get(0);
        ImageView secondPiece = selectingPieces.get(1);

        copyBitmap(firstPiece, tmpPiece);
        copyBitmap(secondPiece, firstPiece);
        copyBitmap(tmpPiece, secondPiece);
        updateCurrentPosition(firstPiece, secondPiece);

        return true;
    }

    private boolean isGameCompleted() {
        Log.e(TAG,"isGameCompleted ----- start");
        Iterator it = gameInfo.entrySet().iterator();
        boolean isGameCompleted = true;

        while (it.hasNext()){
            HashMap.Entry<ImageView, PieceInfo> pair = (HashMap.Entry)it.next();
            Log.e(TAG,"position ----- " + pair.getValue().currentPos + "|" + pair.getValue().correctPos);
            isGameCompleted = isGameCompleted && pair.getValue().isCorrected();
        }
        Log.e(TAG,"isGameCompleted ----- end");
        return isGameCompleted;
    }

    private void updateCurrentPosition(ImageView iv1, ImageView iv2) {
        PieceInfo iv1Info = gameInfo.get(iv1);
        PieceInfo iv2Info = gameInfo.get(iv2);
        int tmpPos = iv1Info.currentPos;

        iv1Info.setCurrentPos(iv2Info.currentPos);
        iv2Info.setCurrentPos(tmpPos);
        gameInfo.put(iv1, iv1Info);
        gameInfo.put(iv2, iv2Info);
    }

    private void copyBitmap(ImageView srcImgView, ImageView tgImgView){
        BitmapDrawable drawable = (BitmapDrawable) srcImgView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        tgImgView.setImageBitmap(bitmap);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
