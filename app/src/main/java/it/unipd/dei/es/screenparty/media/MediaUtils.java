package it.unipd.dei.es.screenparty.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.fragment.app.Fragment;

public class MediaUtils {

    public static final int SELECT_MEDIA_REQUEST_CODE = 0;

    public static MediaParams analyzeMedia(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        Bitmap bitmap = retriever.getFrameAtTime();
        retriever.release();
        float aspectRatio = (float)bitmap.getWidth() / (float)bitmap.getHeight();
        return new MediaParams(uri, aspectRatio);
    }

    public static void openMediaPicker(Fragment fragment) {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("video/*");
        fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*"});

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");

        Intent chooserIntent = Intent.createChooser(fileIntent, "Select the media you want to play");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { galleryIntent });

        fragment.startActivityForResult(chooserIntent, SELECT_MEDIA_REQUEST_CODE);
    }
}
