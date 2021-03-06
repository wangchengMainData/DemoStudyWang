package wang.demo.root.camera2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import wang.demo.root.R;

public class Camera2 extends Activity {

    private static final String TAG = "Camera2";

    private static final String CAMERA_BACK = "0";

    private static final String CAMERA_FRONT = "1";

    private CameraManager manager;
    private CameraDevice device;
    private String cameraId = CAMERA_BACK;
    private Size mPreviewSize = new Size(1080,1920);
    private Handler handler;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private TextureView textureView;
    private HandlerThread handlerThread;
    private CaptureRequest.Builder mCapturePreviewRequestBuilder,mCapturePictureRequestBuilder,
            mCaptureVideoRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private Button video_button,pic_button;
    private Looper looper;
    private ImageReader mReader;
    private MediaRecorder mMediaRecorder;

    @Override
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera2_preview);
        textureView = findViewById(R.id.preview);
        video_button = findViewById(R.id.video_camera);
        video_button.setOnClickListener(new MyButtonListener());
        video_button.setTag(1);
        pic_button = findViewById(R.id.pic_camera);
        pic_button.setOnClickListener(new MyButtonListener());
        pic_button.setTag(2);
    }

    public class MyButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v){
                int tag =(Integer) v.getTag();
                switch (tag){
                    case 1:startVideo();break;
                    case 2:takePicture();break;
                }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        createPreviewThread();//camera2?????????????????????
        if(textureView.isAvailable()){//texture??????????????????????????????????????????
                openCamera();
        }else{
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    openCamera();
                }
                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }
                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }
                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }

    }

    @Override
    protected void onPause(){
        Log.d("wc","pause");
        closeCamera();
//        stopPreviewThread();????????????sending message to a Handler on a dead thread
//        ???????????????
        super.onPause();
    }

    public void getService(){
        manager = (CameraManager)getSystemService(CAMERA_SERVICE);
    }
    /**
     ????????????
     */
    private void openCamera() {
        getService();//?????????
        if (ContextCompat.checkSelfPermission(Camera2.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;//??????????????????
        }
        try{
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {//????????????cameraid???????????????
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    setupImageReader();//????????????????????????
                    mCameraOpenCloseLock.release();
                    device = camera;
                    startpreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    mCameraOpenCloseLock.release();
                    if (device != null) {
                        device.close();
                        camera.close();
                        device = null;
                    }
                }
                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    mCameraOpenCloseLock.release();
                    if (device != null) {
                        device.close();
                        camera.close();
                        device = null;
                    }
                }
            }, handler); //????????????
        }
        catch (CameraAccessException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            throw new RuntimeException("");
        }
    }
    /**
     *????????????
     */
    private void closeCamera(){
        try {
            mCameraOpenCloseLock.acquire();
        }catch (Exception e){e.printStackTrace();}
        finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * ????????????????????????
     */
    private void createPreviewThread(){
        handlerThread = new HandlerThread("camera2_1");
        handlerThread.start();
        looper = handlerThread.getLooper();
        handler = new Handler(looper);
    }

    /**
    /**
     * ????????????
     */
    private void stopPreviewThread(){
        handlerThread.quitSafely();
        try{
            handlerThread.quit();
            handlerThread.interrupt();
            handlerThread = null;
            handler = null;
        }catch (Exception e){}

    }

    /**
     * ????????????
     */
    private void startpreview() {
        SurfaceTexture mSurfaceTexture = textureView.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(mSurfaceTexture);
        try {
            mCapturePreviewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//????????????
            mCapturePreviewRequestBuilder.addTarget(surface);
            mCapturePreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            device.createCaptureSession(Arrays.asList(surface,mReader.getSurface()), new CameraCaptureSession.StateCallback() {//????????????
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    toast("??????????????????");
                    mCameraCaptureSession = session;
                    mCaptureRequest = mCapturePreviewRequestBuilder.build();
                    try {
                        mCameraCaptureSession.setRepeatingRequest(mCapturePreviewRequestBuilder.build(), mCaptureCallback, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    toast("?????????????????????");
                }
            }, handler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            logd("onCaptureCompleted"+result.get(CaptureResult.CONTROL_AF_STATE));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (device != null) {
            device.close();
        }
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession = null;
        }

    }

    private void takePicture(){
        Log.d(TAG,"takePicture");
        try {
            mCapturePictureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCapturePictureRequestBuilder.addTarget(mReader.getSurface());
                logd(mReader.getSurface().toString());
                mCameraCaptureSession.capture(mCapturePictureRequestBuilder.build(),mCaptureCallback,handler);//????????????

        }catch (Exception e){e.printStackTrace();}
    }

    private void recordconfig() {
        SurfaceTexture mSurfaceTexture = textureView.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(mSurfaceTexture);
        try {
            mCaptureVideoRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//????????????
            mCaptureVideoRequestBuilder.addTarget(surface);
            mCaptureVideoRequestBuilder.addTarget(mMediaRecorder.getSurface());
            mCaptureVideoRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            device.createCaptureSession(Arrays.asList(surface,mMediaRecorder.getSurface()), new CameraCaptureSession.StateCallback() {//????????????
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    toast("????????????");
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(mCaptureVideoRequestBuilder.build(), mCaptureCallback, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    toast("???????????????");
                }
            }, handler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    public void initMediaRecordConfig(){
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//??????????????????
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//??????????????????
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//??????????????????
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//???????????????????????????????????????????????????????????????app?????????????????????????????????????????????AAC
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//???????????????????????????????????????????????????????????????app?????????????????????????????????????????????H264
        mMediaRecorder.setVideoEncodingBitRate(8*1024*1920);//??????????????? ????????? 1*????????? ??? 10*????????? ???????????????????????????????????????????????????????????????????????????
        mMediaRecorder.setVideoFrameRate(30);//???????????? ?????? 30????????? ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????30?????????
        Size size = getMatchingSize2();
        mMediaRecorder.setVideoSize(size.getWidth(),size.getHeight());
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setPreviewDisplay(new Surface(textureView.getSurfaceTexture()));
        File file = new File("sdcard/DCIM/myVideo.mp4");
        mMediaRecorder.setOutputFile(file);
        try {
            mMediaRecorder.prepare();
        }catch (Exception e){e.printStackTrace();}
        recordconfig();
    }

    public void startVideo(){
        if(video_button.getText().equals("??????")) {
            video_button.setText("??????");
            initMediaRecordConfig();
            mMediaRecorder.start();
        }else{
            video_button.setText("??????");
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            startpreview();
        }
    }

    private void setupImageReader(){
        mReader = ImageReader.newInstance(1080,1920,
                ImageFormat.JPEG, /*maxImages*/2);
        logd(mReader.toString()+","+mReader.getSurface().toString());
        mReader.setOnImageAvailableListener(
                mOnImageAvailableListener, handler);
    }

    ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            toast("??????");
            Image image = reader.acquireLatestImage();
            new Thread(new ImageSaver(image)).start();//???????????????????????????
        }
    };
    private static class ImageSaver implements Runnable {//img??????bitmap????????????

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private File mFile;
        public ImageSaver(Image image) {
            mImage = image;

        }
        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            mFile = new File("sdcard/DCIM/myPicture.jpg");
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void logd(String msg){
        Log.d(TAG,msg);
    }

    private void toast(String str){
        Toast.makeText(getBaseContext(),str,Toast.LENGTH_SHORT).show();
    }

    private Size getMatchingSize2(){
        Size selectSize = null;
        try {
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics(); //???????????????????????????????????????,?????????????????????????????????
            int deviceWidth = displayMetrics.widthPixels; //??????????????????
            int deviceHeigh = displayMetrics.heightPixels; //??????????????????
            Log.e(TAG, "getMatchingSize2: ??????????????????="+deviceWidth);
            Log.e(TAG, "getMatchingSize2: ??????????????????="+deviceHeigh );
            /**
             * ??????40???,????????????????????????????????????,???????????????????????????????????????,
             * ????????????????????????????????????,??????????????????????????????,?????????????????????????????????null???Size?????????
             * ,??????????????????????????????????????????????????????
             */
            for (int j = 1; j < 41; j++) {
                for (int i = 0; i < sizes.length; i++) { //????????????Size
                    Size itemSize = sizes[i];
                    Log.e(TAG,"??????itemSize ???="+itemSize.getWidth()+"???="+itemSize.getHeight());
                    //????????????Size????????????????????????+j*5  &&  ????????????Size????????????????????????-j*5  &&  ????????????Size??????????????????????????????
                    if (itemSize.getHeight() < (deviceWidth + j*5) && itemSize.getHeight() > (deviceWidth - j*5)) {
                        if (selectSize != null){ //?????????????????????????????????????????????
                            if (Math.abs(deviceHeigh-itemSize.getWidth()) < Math.abs(deviceHeigh - selectSize.getWidth())){ //????????????????????????????????????????????????
                                selectSize = itemSize;
                                continue;
                            }
                        }else {
                            selectSize = itemSize;
                        }

                    }
                }
                if (selectSize != null){ //???????????????null ????????????????????? ????????????
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "getMatchingSize2: ????????????????????????="+selectSize.getWidth());
        Log.e(TAG, "getMatchingSize2: ????????????????????????="+selectSize.getHeight());
        return selectSize;
    }
}
//test