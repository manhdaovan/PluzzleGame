package com.manhdaovan.pluzzlegame.models;

public class SavedItem {
    private String pieceName;
    private String pieceImgPath;
    private int row;
    private int col;


    public SavedItem(String _pieceName, String _pieceImgPath, int _row, int _col){
        pieceName = _pieceName;
        pieceImgPath = _pieceImgPath;
        row = _row;
        col = _col;
    }

    public String getPieceName() {
        return pieceName;
    }

    public String getPieceImgPath() {
        return pieceImgPath;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
