package com.sec.internal.ims.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.picturetool.BitmapExtractor;
import com.sec.internal.helper.picturetool.ComplexImageExtractor;
import com.sec.internal.helper.picturetool.ContentTypeContextCreator;
import com.sec.internal.helper.picturetool.FullCompressionDescriptor;
import com.sec.internal.helper.picturetool.ICompressionDescriptor;
import com.sec.internal.helper.picturetool.IContentTypeContext;
import com.sec.internal.helper.picturetool.IVideoPreviewExtractor;
import com.sec.internal.helper.picturetool.ImageDimensionsExtractor;
import com.sec.internal.helper.picturetool.PanicCompressionDescriptor;
import com.sec.internal.helper.picturetool.PngLazyCompressionDescriptor;
import com.sec.internal.helper.picturetool.ReadScaleCalculator;
import com.sec.internal.helper.picturetool.UniqueFilePathResolver;
import com.sec.internal.helper.picturetool.VideoPreviewExtractor;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.ims.util.IThumbnailTool;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ThumbnailTool extends Handler implements IThumbnailTool {
    private static final int EVT_THUMBNAIL_CREATE = 1000;
    private static final int EVT_THUMBNAIL_CREATE_FROM_IMAGE = 1001;
    private static final int EVT_THUMBNAIL_CREATE_FROM_IMAGE_AS_SIZE = 1002;
    private static final int EVT_THUMBNAIL_CREATE_FROM_VIDEO = 1003;
    private static final int EVT_THUMBNAIL_CREATE_FROM_VIDEO_AS_SIZE = 1004;
    private static final String EXT_PNG = "image/png";
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet(Arrays.asList(new String[]{"JPG", "JPEG", "BMP", "PNG", "GIF", "WBMP"}));
    private static final String LOG_TAG = "ThumbnailTool";
    private static final String SUBDIR_NAME = ".rcs_thumbnail";
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet(Arrays.asList(new String[]{"3GP", "MP4", "AVI"}));
    private BitmapExtractor mBitmapExtractor;
    private ComplexImageExtractor mComplexImageExtractor = new ComplexImageExtractor();
    private ContentTypeContextCreator mContentTypeContextCreator;
    private Context mContext;
    private ImageDimensionsExtractor mImageDimensionsExtractor = new ImageDimensionsExtractor();
    private ICompressionDescriptor mPanicDescriptor;
    private String mSavedDir = null;
    private VideoPreviewExtractor mVideoPreviewExtractor;

    private static class ThumbnailInfor {
        /* access modifiers changed from: private */
        public Message callback;
        /* access modifiers changed from: private */
        public String destFilePath;
        /* access modifiers changed from: private */
        public int height;
        /* access modifiers changed from: private */
        public long maxSize;
        /* access modifiers changed from: private */
        public String originalFilePath;
        /* access modifiers changed from: private */
        public int width;

        private ThumbnailInfor() {
        }
    }

    private String getFileExtension(String thumbnailFile) {
        int extOffset = thumbnailFile.lastIndexOf(".");
        if (extOffset < 0) {
            return null;
        }
        return thumbnailFile.substring(extOffset + 1).toUpperCase(Locale.ENGLISH);
    }

    public ThumbnailTool(Context context) {
        this.mContext = context;
        this.mPanicDescriptor = new PanicCompressionDescriptor();
        this.mBitmapExtractor = new BitmapExtractor();
        this.mContentTypeContextCreator = new ContentTypeContextCreator();
        this.mVideoPreviewExtractor = new VideoPreviewExtractor(this.mBitmapExtractor);
    }

    public void handleMessage(Message msg) {
        ThumbnailInfor infor = (ThumbnailInfor) msg.obj;
        String thumbnailPath = null;
        Log.d(LOG_TAG, "handleMessage: " + msg.what);
        switch (msg.what) {
            case 1000:
                String fileExtension = getFileExtension(infor.originalFilePath);
                Log.d(LOG_TAG, "handleMessage: original=" + infor.originalFilePath + ", fileExtension=" + fileExtension + ", dest=" + infor.destFilePath);
                if (!IMAGE_EXTENSIONS.contains(fileExtension)) {
                    if (VIDEO_EXTENSIONS.contains(fileExtension)) {
                        thumbnailPath = createThumbFromVideo(new File(infor.originalFilePath), new File(infor.destFilePath), infor.maxSize, Integer.MAX_VALUE, Integer.MAX_VALUE);
                        break;
                    }
                } else {
                    thumbnailPath = createThumbFromImage(new File(infor.originalFilePath), new File(infor.destFilePath), infor.maxSize, Integer.MAX_VALUE, Integer.MAX_VALUE);
                    break;
                }
                break;
            case 1001:
                thumbnailPath = createThumbFromImage(new File(infor.originalFilePath), new File(infor.destFilePath), infor.maxSize, Integer.MAX_VALUE, Integer.MAX_VALUE);
                break;
            case 1002:
                thumbnailPath = createThumbFromImage(new File(infor.originalFilePath), new File(infor.destFilePath), infor.maxSize, infor.width, infor.height);
                break;
            case 1003:
                thumbnailPath = createThumbFromVideo(new File(infor.originalFilePath), new File(infor.destFilePath), infor.maxSize, Integer.MAX_VALUE, Integer.MAX_VALUE);
                break;
            case 1004:
                thumbnailPath = createThumbFromVideo(new File(infor.originalFilePath), new File(infor.destFilePath), infor.maxSize, infor.width, infor.height);
                break;
            default:
                Log.d(LOG_TAG, "Unsupport file format!!!");
                break;
        }
        if (infor.callback != null) {
            AsyncResult.forMessage(infor.callback, thumbnailPath, (Throwable) null);
            infor.callback.sendToTarget();
        }
    }

    public boolean isSupported(String type) {
        Log.d(LOG_TAG, "The thumbnailFile type is " + type);
        if (type.startsWith(CallConstants.ComposerData.IMAGE) || type.startsWith(TMOConstants.CallLogTypes.VIDEO)) {
            return true;
        }
        return false;
    }

    public String getThumbSavedDirectory() {
        if (this.mSavedDir == null) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), SUBDIR_NAME);
            if (file.isDirectory() || file.mkdirs()) {
                this.mSavedDir = file.getAbsolutePath();
            }
            Log.d(LOG_TAG, "getThumbSavedDirectory: " + this.mSavedDir);
        }
        return this.mSavedDir;
    }

    public void createThumb(String originalFilePath, String destFilePath, long maxSize, Message msg) {
        ThumbnailInfor infor = new ThumbnailInfor();
        String unused = infor.originalFilePath = originalFilePath;
        String unused2 = infor.destFilePath = destFilePath;
        long unused3 = infor.maxSize = maxSize;
        Message unused4 = infor.callback = msg;
        sendMessage(obtainMessage(1000, infor));
    }

    public void createThumbFromImage(String originalFile, String destinationDirectory, long maxSize, Message msg) {
        ThumbnailInfor infor = new ThumbnailInfor();
        String unused = infor.originalFilePath = originalFile;
        String unused2 = infor.destFilePath = destinationDirectory;
        long unused3 = infor.maxSize = maxSize;
        Message unused4 = infor.callback = msg;
        sendMessage(obtainMessage(1001, infor));
    }

    public void createThumbFromImage(String originalFile, String destinationDirectory, long maxSize, int maxWidth, int maxHeight, Message msg) {
        ThumbnailInfor infor = new ThumbnailInfor();
        String unused = infor.originalFilePath = originalFile;
        String unused2 = infor.destFilePath = destinationDirectory;
        long unused3 = infor.maxSize = maxSize;
        Message unused4 = infor.callback = msg;
        int unused5 = infor.width = maxWidth;
        int unused6 = infor.height = maxHeight;
        sendMessage(obtainMessage(1002, infor));
    }

    public void createThumbFromVideo(String originalFile, String destinationDirectory, long maxSize, Message msg) {
        ThumbnailInfor infor = new ThumbnailInfor();
        String unused = infor.originalFilePath = originalFile;
        String unused2 = infor.destFilePath = destinationDirectory;
        long unused3 = infor.maxSize = maxSize;
        Message unused4 = infor.callback = msg;
        sendMessage(obtainMessage(1003, infor));
    }

    public void createThumbFromVideo(String originalFile, String destinationDirectory, long maxSize, int maxWidth, int maxHeight, Message msg) {
        ThumbnailInfor infor = new ThumbnailInfor();
        String unused = infor.originalFilePath = originalFile;
        String unused2 = infor.destFilePath = destinationDirectory;
        long unused3 = infor.maxSize = maxSize;
        Message unused4 = infor.callback = msg;
        int unused5 = infor.width = maxWidth;
        int unused6 = infor.height = maxHeight;
        sendMessage(obtainMessage(1004, infor));
    }

    public String createCopyPaste(File originalFile, File destinationDirectory) {
        if (destinationDirectory == null) {
            Log.e(LOG_TAG, "destinationDirectory == null");
            return null;
        }
        File outputFile = UniqueFilePathResolver.getUniqueFile(originalFile.getName(), destinationDirectory);
        Log.d(LOG_TAG, "createCopyPaste:" + outputFile);
        FileUtils.copyFile(originalFile, outputFile);
        return outputFile.getPath();
    }

    private String createThumbFromImage(File originalFile, File destinationDirectory, long maxSize, int maxWidth, int maxHeight) {
        File complexImage;
        ICompressionDescriptor descriptor;
        File file = originalFile;
        File file2 = destinationDirectory;
        long j = maxSize;
        int i = maxWidth;
        Log.d(LOG_TAG, "createThumbFromImage: [originalFile=" + file + ", destinationDirectory=" + file2);
        File complexImage2 = null;
        try {
            complexImage2 = this.mComplexImageExtractor.extractFrom(file);
            this.mComplexImageExtractor.release();
            complexImage = complexImage2;
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "could not extract complex image");
            complexImage = complexImage2;
        }
        if (complexImage == null) {
            Log.e(LOG_TAG, "complexImage == null");
            return null;
        }
        long imageSize = complexImage.length();
        Pair<Integer, Integer> imageDimensions = getImageDimensions(complexImage);
        if (imageDimensions == null) {
            Log.e(LOG_TAG, "imageDimensions == null");
            return null;
        }
        Log.d(LOG_TAG, "createThumbFromImage: imageSize=" + imageSize + ", maxSize=" + j + ", dimension=" + imageDimensions.first + ", maxWidth=" + i);
        if (imageSize > j || ((Integer) imageDimensions.first).intValue() > i) {
            int i2 = maxHeight;
        } else if (((Integer) imageDimensions.second).intValue() <= maxHeight) {
            return createCopyPaste(complexImage, file2);
        }
        Pair<Integer, Integer> imageDimensions2 = imageDimensions;
        long imageSize2 = imageSize;
        int readScale = ReadScaleCalculator.calculate(imageSize, ((Integer) imageDimensions.first).intValue(), ((Integer) imageDimensions.second).intValue(), maxSize, maxWidth, maxHeight);
        String ext = getFileExtension(complexImage.getName());
        if (ext == null) {
            Log.e(LOG_TAG, "ext == null");
            return null;
        }
        String contentType = ContentTypeTranslator.translate(ext);
        if (!contentType.equals(EXT_PNG) || imageSize2 > j) {
            descriptor = new FullCompressionDescriptor(imageSize2, ((Integer) imageDimensions2.first).intValue(), ((Integer) imageDimensions2.second).intValue(), maxSize, maxWidth, maxHeight, this.mPanicDescriptor);
        } else {
            descriptor = new PngLazyCompressionDescriptor(((Integer) imageDimensions2.first).intValue(), ((Integer) imageDimensions2.second).intValue(), maxWidth, maxHeight, this.mPanicDescriptor);
        }
        String str = contentType;
        return createThumbFromStillPicture(complexImage, destinationDirectory, readScale, imageSize2, maxSize, descriptor);
    }

    private Bitmap extractBitmapFromImage(File image, int scale) throws IOException {
        return this.mBitmapExtractor.extractFromImage(image, scale);
    }

    private String createThumbFromStillPicture(File originalFile, File destinationDirectory, int readScale, long imageSize, long maxSize, ICompressionDescriptor descriptor) {
        try {
            try {
                return createThumbFromPicture(originalFile, destinationDirectory, imageSize, maxSize, Pair.create(extractBitmapFromImage(originalFile, readScale), Integer.valueOf(readScale)), descriptor);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (IThumbnailTool.ThumbCreationException e2) {
                e2.printStackTrace();
                return null;
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    private Pair<Integer, Integer> getImageDimensions(File inputImage) {
        return this.mImageDimensionsExtractor.extract(inputImage);
    }

    private IContentTypeContext getContentTypeContext(File imageFile) {
        String ext = getFileExtension(imageFile.getName());
        if (ext == null) {
            Log.e(LOG_TAG, "ext == null");
            return null;
        }
        String mime = ContentTypeTranslator.translate(ext);
        Log.d(LOG_TAG, "getStillContentTypeContext: mime=" + mime);
        return this.mContentTypeContextCreator.getContextByMime(mime);
    }

    private String createThumbFromPicture(File originalFile, File destinationDirectory, long imageSize, long maxSize, Pair<Bitmap, Integer> originalImage, ICompressionDescriptor descriptor) throws IOException, IThumbnailTool.ThumbCreationException {
        File tmpFile;
        int i;
        ThumbnailTool thumbnailTool = this;
        Pair<Bitmap, Integer> pair = originalImage;
        String str = null;
        if (thumbnailTool.mContext == null) {
            File file = originalFile;
            File file2 = destinationDirectory;
            ICompressionDescriptor iCompressionDescriptor = descriptor;
        } else if (pair == null) {
            File file3 = originalFile;
            File file4 = destinationDirectory;
            ICompressionDescriptor iCompressionDescriptor2 = descriptor;
        } else if (pair.first == null) {
            Log.e(LOG_TAG, "originalImage.first == null");
            return null;
        } else {
            IContentTypeContext mContentTypeContext = getContentTypeContext(originalFile);
            if (mContentTypeContext == null) {
                Log.e(LOG_TAG, "mContentTypeContext == null");
                return null;
            }
            mContentTypeContext.validateExtension();
            String fileName = originalFile.getName();
            File outputFile = mContentTypeContext.getFinalFilePath(destinationDirectory, fileName);
            Log.d(LOG_TAG, "createThumbFromPicture: outputFile=" + outputFile);
            List<File> tmpFiles = new ArrayList<>();
            long currentImageSize = imageSize;
            while (true) {
                Pair<Integer, Integer> compressionParams = descriptor.next(currentImageSize);
                File file5 = thumbnailTool.mContext.getCacheDir();
                if (file5 == null) {
                    Log.e(LOG_TAG, "file == null");
                    return str;
                }
                File tmpFile2 = UniqueFilePathResolver.getUniqueFile(fileName, file5);
                tmpFiles.add(0, tmpFile2);
                if (compressionParams != null) {
                    int scale = ((Integer) compressionParams.second).intValue();
                    if (scale == 1) {
                        File file6 = file5;
                        long j = currentImageSize;
                        thumbnailTool.saveBitmapToFile((Bitmap) pair.first, tmpFile2, ((Integer) compressionParams.first).intValue(), mContentTypeContext.getDestinationFormat());
                        tmpFile = tmpFile2;
                        Pair<Integer, Integer> pair2 = compressionParams;
                        i = 1;
                    } else {
                        long j2 = currentImageSize;
                        Pair<Integer, Integer> originalDimensions = getImageDimensions(originalFile);
                        if (originalDimensions == null) {
                            Log.e(LOG_TAG, "originalDimensions == null");
                            return null;
                        }
                        int intValue = ((Integer) compressionParams.first).intValue();
                        i = 1;
                        tmpFile = tmpFile2;
                        Pair<Integer, Integer> pair3 = compressionParams;
                        int i2 = intValue;
                        Bitmap.CompressFormat destinationFormat = mContentTypeContext.getDestinationFormat();
                        Pair<Integer, Integer> pair4 = originalDimensions;
                        saveBitmapToFile((Bitmap) pair.first, tmpFile, i2, destinationFormat, Pair.create(Integer.valueOf(((Integer) originalDimensions.first).intValue() / scale), Integer.valueOf(((Integer) originalDimensions.second).intValue() / scale)));
                    }
                } else {
                    i = 1;
                    tmpFile = tmpFile2;
                    File file7 = file5;
                    Pair<Integer, Integer> pair5 = compressionParams;
                    long j3 = currentImageSize;
                }
                currentImageSize = tmpFile.length();
                StringBuilder sb = new StringBuilder();
                sb.append("createThumbFromPicture: tmpFile=");
                File tmpFile3 = tmpFile;
                sb.append(tmpFile3);
                sb.append(", size=");
                sb.append(currentImageSize);
                Log.d(LOG_TAG, sb.toString());
                if (currentImageSize <= maxSize) {
                    if (currentImageSize > 5120 || tmpFiles.size() <= i) {
                        FileUtils.copyFile(tmpFile3, outputFile);
                    } else {
                        File prevFile = tmpFiles.get(i);
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("createThumbFromPicture: use previous tmpFile= ");
                        sb2.append(prevFile);
                        sb2.append(", size=");
                        long j4 = currentImageSize;
                        sb2.append(prevFile.length());
                        Log.d(LOG_TAG, sb2.toString());
                        FileUtils.copyFile(prevFile, outputFile);
                    }
                    for (File tmpFileToDelete : tmpFiles) {
                        if (!tmpFileToDelete.delete()) {
                            Log.e(LOG_TAG, "tmpFileToDelete.delete() error");
                        }
                    }
                    ((Bitmap) pair.first).recycle();
                    mContentTypeContext.processSpecificData(originalFile, outputFile);
                    return outputFile.getPath();
                }
                File file8 = originalFile;
                long j5 = currentImageSize;
                str = null;
                thumbnailTool = this;
            }
        }
        Log.e(LOG_TAG, "mContext == null && originalImage == null");
        return null;
    }

    private void saveBitmapToFile(Bitmap sourceBitmap, File destFilePath, int quality, Bitmap.CompressFormat format, Pair<Integer, Integer> destDimensions) throws IOException, IThumbnailTool.ThumbCreationException {
        OutputStream dataStream = null;
        try {
            dataStream = new FileOutputStream(destFilePath);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, ((Integer) destDimensions.first).intValue(), ((Integer) destDimensions.second).intValue(), false);
            scaledBitmap.compress(format, quality, dataStream);
            if (!scaledBitmap.sameAs(sourceBitmap)) {
                scaledBitmap.recycle();
            }
            dataStream.flush();
        } finally {
            closeStream(dataStream);
        }
    }

    private void saveBitmapToFile(Bitmap sourceBitmap, File destFilePath, int quality, Bitmap.CompressFormat format) throws IOException, IThumbnailTool.ThumbCreationException {
        OutputStream dataStream = null;
        try {
            dataStream = new FileOutputStream(destFilePath);
            sourceBitmap.compress(format, quality, dataStream);
            dataStream.flush();
        } finally {
            closeStream(dataStream);
        }
    }

    private void closeStream(Closeable cloaseable) throws IOException {
        if (cloaseable != null) {
            try {
                cloaseable.close();
            } catch (IOException e) {
                throw new IOException("Can't close stream: e=" + e);
            }
        }
    }

    private String createThumbFromVideo(File originalFile, File destinationDirectory, long maxSize, int maxWidth, int maxHeight) {
        try {
            return createThumbFromMotionPicture(originalFile, destinationDirectory, maxSize, maxWidth, maxHeight);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (IThumbnailTool.ThumbCreationException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private String createThumbFromMotionPicture(File originalFile, File destinationDirectory, long maxSize, int maxWidth, int maxHeight) throws IOException, IThumbnailTool.ThumbCreationException {
        File file;
        Context context = this.mContext;
        if (context != null) {
            file = context.getCacheDir();
        } else {
            file = null;
        }
        if (file == null) {
            Log.e(LOG_TAG, "file == null");
            return null;
        }
        IVideoPreviewExtractor.IVideoPreview videoPreview = this.mVideoPreviewExtractor.extract(originalFile, file);
        File tmpFile = videoPreview.getFile();
        Bitmap originalThumbnail = this.mBitmapExtractor.extractFromImage(tmpFile, 1);
        Pair<Integer, Integer> imageDimensions = videoPreview.getDimensions();
        long imageSize = videoPreview.getSize();
        Pair<Integer, Integer> pair = imageDimensions;
        File tmpFile2 = tmpFile;
        try {
            String createThumbFromMotionPicture = createThumbFromMotionPicture(tmpFile, destinationDirectory, imageSize, maxSize, originalThumbnail, new FullCompressionDescriptor(imageSize, ((Integer) imageDimensions.first).intValue(), ((Integer) imageDimensions.second).intValue(), maxSize, maxWidth, maxHeight, this.mPanicDescriptor));
            originalThumbnail.recycle();
            if (!tmpFile2.delete()) {
                Log.e(LOG_TAG, "tmpFile.delete() error");
            }
            return createThumbFromMotionPicture;
        } catch (Throwable th) {
            Throwable th2 = th;
            originalThumbnail.recycle();
            if (!tmpFile2.delete()) {
                Log.e(LOG_TAG, "tmpFile.delete() error");
            }
            throw th2;
        }
    }

    private String createThumbFromMotionPicture(File originalFile, File destinationDirectory, long imageSize, long maxSize, Bitmap originalImage, ICompressionDescriptor descriptor) throws IOException, IThumbnailTool.ThumbCreationException {
        return createThumbFromPicture(originalFile, destinationDirectory, imageSize, maxSize, Pair.create(originalImage, Integer.valueOf(getReadScale(imageSize, maxSize))), descriptor);
    }

    private int getReadScale(long size, long maxSize) {
        return ReadScaleCalculator.calculate(size, maxSize);
    }
}
