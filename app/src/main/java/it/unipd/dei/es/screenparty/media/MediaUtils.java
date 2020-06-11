package it.unipd.dei.es.screenparty.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import it.unipd.dei.es.screenparty.R;

/**
 * Utility class for the Media package.
 */
public class MediaUtils {

    public static final int SELECT_MEDIA_REQUEST_CODE = 0;

    /**
     * Retrieve the {@link MediaParams} of the selected video.
     * @param context Context needed for media meta data retriever.
     * @param uri The media's uri.
     * @return MediaParams of the selected video.
     */
    @NotNull
    public static MediaParams analyzeMedia(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        Bitmap bitmap = retriever.getFrameAtTime();
        retriever.release();
        return new MediaParams(uri, bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * Open a window to select the video to be played.
     * @param fragment Fragment needed to start the activity.
     */
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
}
