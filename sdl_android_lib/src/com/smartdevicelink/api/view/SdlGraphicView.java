package com.smartdevicelink.api.view;

import android.util.Log;

import com.smartdevicelink.api.file.SdlFile;
import com.smartdevicelink.api.file.SdlFileManager;
import com.smartdevicelink.api.file.SdlImage;
import com.smartdevicelink.api.interfaces.SdlContext;
import com.smartdevicelink.proxy.rpc.Image;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.enums.ImageType;

public class SdlGraphicView extends SdlView {

    private static final String TAG = SdlGraphicView.class.getSimpleName();
    private static final String CLEAR_NAME = "";

    private SdlImage mSdlImage;
    private boolean isImagePresent = false;
    private boolean isWaitingForUpload = false;

    private boolean isSecondaryGraphic = false;

    public void setGraphic(SdlImage graphic){
        if(graphic != null) {
            mSdlImage = graphic;
            if(mSdlContext != null) {
                checkImagePresence();
            }
        }
        isChanged = true;
    }

    @Override
    public void setSdlContext(SdlContext sdlContext) {
        super.setSdlContext(sdlContext);
        if(mSdlImage != null) {
            checkImagePresence();
        }
    }

    public SdlImage getGraphic(){
        return mSdlImage;
    }

    private void checkImagePresence() {
        SdlFileManager fileManager = mSdlContext.getSdlFileManager();
        isImagePresent = fileManager.isFileOnModule(mSdlImage.getSdlName());
        Log.i(TAG, "SdlImage: " + mSdlImage.getSdlName());
        Log.i(TAG, "is " + (isImagePresent ? "" : "not ") + "present on module");
        if (!isWaitingForUpload && !isImagePresent) {
            fileManager.uploadSdlImage(mSdlImage, mFileReadyListener);
            isWaitingForUpload = true;
        }
    }

    public void setSecondaryGraphic(boolean secondaryGraphic) {
        isSecondaryGraphic = secondaryGraphic;
        isChanged = true;
    }

    @Override
    public void clear() {
        mSdlImage = null;
        isChanged = true;
        redraw();
    }

    @Override
    boolean decorate(Show show) {
        boolean sendShow = super.decorate(show);
        if(mSdlImage == null){
            Image image = new Image();
            image.setImageType(ImageType.DYNAMIC);
            image.setValue(CLEAR_NAME);
            if(!isSecondaryGraphic) {
                show.setGraphic(image);
            } else {
                show.setSecondaryGraphic(image);
            }
        } else if(isImagePresent) {
            Image image = new Image();
            image.setImageType(ImageType.DYNAMIC);
            image.setValue(mSdlImage.getSdlName());
            if(!isSecondaryGraphic) {
                show.setGraphic(image);
            } else {
                show.setSecondaryGraphic(image);
            }
        }
        return sendShow;
    }

    @Override
    void uploadRequiredImages() {
        if(!isImagePresent && mSdlImage != null){
            checkImagePresence();
        }
    }

    private SdlFileManager.FileReadyListener mFileReadyListener = new SdlFileManager.FileReadyListener() {
        @Override
        public void onFileReady(SdlFile sdlFile) {
            isImagePresent = true;
            isWaitingForUpload = false;
            Log.d(TAG, "Graphic ready.");
            if(isVisible) {
                isChanged = true;
                redraw();
            }
        }

        @Override
        public void onFileError(SdlFile sdlFile) {

        }
    };

}
