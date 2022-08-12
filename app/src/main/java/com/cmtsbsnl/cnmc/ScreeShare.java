package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreeShare {
    private Context context;
    private File imagePath;

    public ScreeShare(Context context) {
        this.context = context;
    }

    public void saveBitmap(Bitmap bitmap, String shareBody) {
        imagePath = new File(Environment.getExternalStorageDirectory() + "/Download/screenshot.png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
//          Share file to the Apps
            shareIt(shareBody);


//            context.getApplicationContext().startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }

    private void shareIt(String shareBody){
        Uri uri = Uri.fromFile(imagePath);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sharingIntent.setType("image/*");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "At a Glance");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        context.getApplicationContext().startActivity(sharingIntent);

    }

}
