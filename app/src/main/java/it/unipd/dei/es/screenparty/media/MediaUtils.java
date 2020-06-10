package it.unipd.dei.es.screenparty.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.DisplayMetrics;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import it.unipd.dei.es.screenparty.R;

public class MediaUtils {

    public static final int SELECT_MEDIA_REQUEST_CODE = 0;

    @NotNull
    public static MediaParams analyzeMedia(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        Bitmap bitmap = retriever.getFrameAtTime();
        retriever.release();
        return new MediaParams(uri, bitmap.getWidth(), bitmap.getHeight());
    }

    public static void openMediaPicker(@NotNull Fragment fragment) {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("video/*");
        fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*"});
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");
        Intent chooserIntent = Intent.createChooser(fileIntent, fragment.getResources().getString(R.string.media_utils_intent_select_video));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { galleryIntent });
        fragment.startActivityForResult(chooserIntent, SELECT_MEDIA_REQUEST_CODE);
    }

    public static int getNavigationBarHeightPixels(@NotNull Activity activity) {
        Rect rectangle = new Rect();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics.heightPixels - (rectangle.top + rectangle.height());
    }
}
