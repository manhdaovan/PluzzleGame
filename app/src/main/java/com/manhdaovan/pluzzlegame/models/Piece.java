package com.manhdaovan.pluzzlegame.models;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;

public class Piece {
    public int order;
    private int correctOrder;
    private String imgUrl;
    private ImageView displayObj;
    private Context context;

    public Piece(int _correctOrder, String _imgUrl, Context _context){
        correctOrder = _correctOrder;
        imgUrl = _imgUrl;
        context = _context;
        displayObj = new ImageView(context);
        displayObj.setImageURI(Uri.fromFile(new File(imgUrl)));
    }

    public boolean isCorrectOrder(){ return order == correctOrder; }
}
