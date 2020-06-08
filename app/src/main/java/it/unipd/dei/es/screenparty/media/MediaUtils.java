package it.unipd.dei.es.screenparty.media;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.loader.content.CursorLoader;

import java.io.File;

public class MediaUtils {

    public static MediaParams analyzeMedia(Context context, Uri uri) {
        String path = getRealPathFromURI(context, uri);
        if(path == null) path = uri.getPath();
        File file = new File(path);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        Bitmap bitmap = retriever.getFrameAtTime();
        retriever.release();
        float aspectRatio = (float)bitmap.getWidth() / (float)bitmap.getHeight();
        return new MediaParams(uri, file, aspectRatio);
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
}
