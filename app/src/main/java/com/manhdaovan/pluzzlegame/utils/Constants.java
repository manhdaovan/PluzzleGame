package com.manhdaovan.pluzzlegame.utils;

import android.graphics.Bitmap;

public class Constants {
    public static final String INTENT_COLUMN_PIECES = "column_pieces";
    public static final String INTENT_ROW_PIECES = "row_pieces";
    public static final String INTENT_GAME_RESOURCE_FOLDER = "game_resource_folder";

    public static final int REQUEST_SELECT_PICTURE = 0x01;

    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat DEFAULT_IMG_FORMAT = Bitmap.CompressFormat.JPEG;

    public static final String[] ROW_NUM_PIECES = {"3 X", "4 X", "5 X", "6 X", "7 X", "8 X", "9 X", "10 X"};
    public static final int DEFAULT_ROW_NUM_PIECES = 4;
    public static final String[] COLUMN_NUM_PIECES = {"3", "4", "5"};
    public static final int DEFAULT_COLUMN_NUM_PIECES = 4;
    public static final int COLUMN_OFFSET = 3;
    public static final int ROW_OFFSET = 3;

    public static final String TMP_FILE_NAME = "cropped_tmp_file";
    public static final String TMP_FILE_MIME = ".jpg";

    public static final String DEFAULT_FILE_NAME = "cropped_img";
    public static final String DEFAULT_FILE_MIME = ".jpg";
    public static final int DEFAULT_FIRST_PIECE_INDEX = 0;

    public static final float EACH_PIECE_WIDTH_WEIGHT = 5;
    public static final float EACH_ROW_HEIGHT_WEIGHT = 5;
    public static final int PIECE_PADDING_LEFT = 2;
    public static final int PIECE_PADDING_RIGHT = 2;
    public static final int PIECE_PADDING_TOP = 2;
    public static final int PIECE_PADDING_BOTTOM = 2;

    public static final String SHARED_PREF_FILE_NAME = "PUZZLE_GAME_PREF";

    public static String defaultCroppedFileName() {
        return DEFAULT_FILE_NAME + DEFAULT_FILE_MIME;
    }

    public static String defaultFirstPieceName(){
        return DEFAULT_FILE_NAME + "_" + DEFAULT_FIRST_PIECE_INDEX + DEFAULT_FILE_MIME;
    }
}
