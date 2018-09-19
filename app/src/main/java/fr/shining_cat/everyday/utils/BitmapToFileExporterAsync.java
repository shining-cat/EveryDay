package fr.shining_cat.everyday.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapToFileExporterAsync extends AsyncTask <Bitmap, Integer, String>{

    public static final String ERROR_CREATING_EXPORT_FILE   = "error_code_Creating_export_file";
    public static final String ERROR_WRITING_EXPORT_FILE    = "error_code_Writing_export_file";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private BitmapToFileExporterAsyncListener mListener;
    private File mExportPictureFolder;
    private String mExportPictureFileName;
    private int mQuality;

////////////////////////////////////////
//AsyncTask actually handling the job of exporting the database datas to a csv file
//with an interface to dispatch callbacks
//returns String with error_code or file canonical path is success
    public BitmapToFileExporterAsync(String pictureFileName, File exportPictureFolderName, int quality, BitmapToFileExporterAsyncListener listener){
        if (listener == null) {
            throw new RuntimeException(TAG + " LISTENER must implement BitmapToFileExporterAsyncListener");
        }
        mListener = listener;
        mExportPictureFolder = exportPictureFolderName;
        mExportPictureFileName = pictureFileName;
        mQuality = quality;
        // restrain quality
        if (mQuality < 0 || mQuality > 100) mQuality = 50;
    }

    @Override
    protected void onPreExecute() {
        mListener.onExportBitmapStarted();
    }

    @Override
    protected String doInBackground(Bitmap... bitmapToExportToFile) {
        File file = new File(mExportPictureFolder, mExportPictureFileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "doInBackground::could not create new file!");
            e.printStackTrace();
            return ERROR_CREATING_EXPORT_FILE;
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            bitmapToExportToFile[0].compress(Bitmap.CompressFormat.JPEG, mQuality, out);
            out.flush();
            out.close();
            return file.getPath();
        } catch (IOException e) {
            Log.e(TAG, "doInBackground::could not write bitmap: " + e);
            e.printStackTrace();
            return ERROR_WRITING_EXPORT_FILE;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        mListener.onExportBitmapComplete(result);
    }



////////////////////////////////////////
//Listener interface
    public interface BitmapToFileExporterAsyncListener {
        void onExportBitmapStarted();
        void onExportBitmapComplete(String result);
    }
}
