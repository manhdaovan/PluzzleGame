package com.manhdaovan.pluzzlegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PieceAdapter extends RecyclerView.Adapter<PieceAdapter.NumberViewHolder> {
    private static final String TAG = PieceAdapter.class.getSimpleName();

    private List<String> saveImgs;

    public PieceAdapter(List<String> _saveImgPaths){
        saveImgs = _saveImgPaths;
    }

    @Override
    public NumberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.number_list_piece;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        NumberViewHolder viewHolder = new NumberViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NumberViewHolder holder, int position){
        Log.e(TAG, "#" + saveImgs.get(position));
        String imgPath = saveImgs.get(position);

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
        holder.bind(bitmap);
    }

    @Override
    public int getItemCount(){
        return saveImgs.size();
    }

    class NumberViewHolder extends RecyclerView.ViewHolder{
        ImageView listItemNumberView;

        public NumberViewHolder(View itemView){
            super(itemView);
            listItemNumberView = itemView.findViewById(R.id.tv_piece_number);
        }

        public void bind(Bitmap bitmap){
            listItemNumberView.setImageBitmap(bitmap);
        }
    }
}
