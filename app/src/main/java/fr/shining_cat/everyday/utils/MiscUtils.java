package fr.shining_cat.everyday.utils;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MiscUtils {


////////////////////////////////////////
//converts color int to hex color string (with alpha)
    public static String convertColorIntToString(int color){
        return "#"+Integer.toHexString(color);
    }

////////////////////////////////////////
// RETURNS AN EMPTY LIST OF SPECIFIED SIZE
    public static ArrayList<Integer> getEmptyList(int size){
        ArrayList<Integer> emptyList = new ArrayList<>();
        for(int i = 0; i < size; i ++){
            emptyList.add(i);
        }
        return emptyList;
    }

////////////////////////////////////////
// GET A BITMAP FROM A VIEW

    public static Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

////////////////////////////////////////
// CHECK FOR PERMISSIONS TO WRITE ON EXTERNAL STORAGE, ASK FOR IT IF NECESSARY (>API24), all via callbacks
    public static void checkExternalAuthorizationAndAskIfNeeded(Context context, OnMiscUtilsListener listener) {
        if(!MiscUtils.weHavePermissionToWriteOnExternalStorage(context)){ // in API > 24 we need to ask for permission at runtime even when they're added in the manifest!
            Log.e("MiscUtils", "checkExternalAuthorizationAndAskIfAdequate::no permission to external storage!!");
            if (Build.VERSION.SDK_INT >= 24) {
                if(listener != null) {
                    listener.onRequestPermissionApi24(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }else{
                    Log.e("MiscUtils", "checkExternalAuthorizationAndAskIfAdequate::no listener to request permission!!");
                }
            }
        }else{
            if(listener != null) {
                listener.onPermissionToWriteOnExternalStorageOk();
            }else{
                Log.e("MiscUtils", "checkExternalAuthorizationAndAskIfAdequate::no listener to request permission!!");
            }
        }
    }

////////////////////////////////////////
// GET A SUBFOLDER IN THE PUBLIC GALLERY FOLDER (creates it if necessary) !Does not check for authorizations!
    public static void getSubFolderInPublicAlbumStorageDir(String albumName, OnMiscUtilsListener listener) {
        // Get the directory for the user's public pictures directory.
        File picturesFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        picturesFolderPath.mkdir();
        if(picturesFolderPath.exists()){
            File mySubFolder = new File(picturesFolderPath, albumName);
            mySubFolder.mkdir();
            if(mySubFolder.exists()) {
                listener.onSubFolderInPublicPicturesStorageDirObtained(mySubFolder);
            }else{
                Log.e("MiscUtils", "getSubFolderInPublicAlbumStorageDir::my SUBFOLDER DOES NOT EXIST!");
            }
        }else{
            Log.e("MiscUtils", "getSubFolderInPublicAlbumStorageDir::NO PICTURES FOLDER!");
        }
    }
////////////////////////////////////////
// GET A SUBFOLDER IN THE PUBLIC DOCUMENT FOLDER (creates it if necessary) !Does not check for authorizations!
    public static void getSubFolderInPublicDocumentStorageDir(String subFolderName, OnMiscUtilsListener listener) {
        // Get the directory for the user's public document directory.
        File documentsFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        documentsFolderPath.mkdir();
        if(documentsFolderPath.exists()){
            File mySubFolder = new File(documentsFolderPath, subFolderName);
            mySubFolder.mkdir();
            if(mySubFolder.exists()) {
                listener.onSubFolderInPublicDocumentStorageDirObtained(mySubFolder);
            }else{
                Log.e("MiscUtils", "getSubFolderInPublicDocumentStorageDir::my SUBFOLDER DOES NOT EXIST!");
            }
        }else{
            Log.e("MiscUtils", "getSubFolderInPublicDocumentStorageDir::NO DOCUMENTS FOLDER!");
        }
    }

////////////////////////////////////////
// CHECKING PERMISSIONS AND THE LIKE

    public static boolean weHavePermissionToWriteOnExternalStorage(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    public static boolean weHavePermissionToReadExternalStorage(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }else{
            Log.e("MiscUtils", "isExternalStorageWritable:: NO !!");
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }else{
            Log.e("MiscUtils", "isExternalStorageReadable:: NO !!");
        }
        return false;
    }

////////////////////////////////////////
//INTERFACE
    public interface OnMiscUtilsListener {
        void onPermissionToWriteOnExternalStorageOk();
        void onRequestPermissionApi24(String[] whichPermission);
        void onSubFolderInPublicDocumentStorageDirObtained(File subFolderInPublicDocumentStorageDir);
        void onSubFolderInPublicPicturesStorageDirObtained(File subFolderInPublicPicturesStorageDir);
    }
}
