package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import com.sec.internal.helper.picturetool.GifDecoder;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;

public class ComplexImageExtractor {
    private static final String CONTENT_TYPE_GIF = "image/gif";
    private static final String LOG_TAG = ComplexImageExtractor.class.getSimpleName();
    private static final String TEMP_FILE_PREFIX = "FT_thumb";
    private GifDecoder mDecoder = null;

    private String getFileExtension(String thumbnailFile) {
        int extOffset = thumbnailFile.lastIndexOf(".");
        if (extOffset < 0) {
            return null;
        }
        return thumbnailFile.substring(extOffset + 1).toUpperCase(Locale.ENGLISH);
    }

    public File extractFrom(File imageFile) {
        if (imageFile == null) {
            Log.e(LOG_TAG, "imageFile == null");
            return null;
        }
        String fileName = getFileExtension(imageFile.getName());
        if (fileName == null) {
            Log.e(LOG_TAG, "fileName == null");
            return null;
        }
        if (ContentTypeTranslator.translate(fileName).contains(CONTENT_TYPE_GIF)) {
            Log.d("ComplexImageExtractor", "Gid decoder: extractFrom, file=" + imageFile.getAbsolutePath());
            GifDecoder gifDecoder = new GifDecoder();
            this.mDecoder = gifDecoder;
            int errorCode = gifDecoder.read(imageFile.getAbsolutePath());
            if (errorCode == 0) {
                Vector<GifDecoder.GifFrame> temp = this.mDecoder.getFrames();
                if (temp.size() > 0) {
                    FileOutputStream tempStream = null;
                    try {
                        File tempFile = File.createTempFile(TEMP_FILE_PREFIX, ".jpg");
                        FileOutputStream tempStream2 = new FileOutputStream(tempFile);
                        if (!temp.get(0).image.compress(Bitmap.CompressFormat.JPEG, 100, tempStream2)) {
                            try {
                                tempStream2.flush();
                                try {
                                    tempStream2.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e2) {
                                e2.printStackTrace();
                                tempStream2.close();
                            } catch (Throwable th) {
                                try {
                                    tempStream2.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                                throw th;
                            }
                            return imageFile;
                        }
                        try {
                            tempStream2.flush();
                            try {
                                tempStream2.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        } catch (IOException e5) {
                            e5.printStackTrace();
                            tempStream2.close();
                        } catch (Throwable th2) {
                            try {
                                tempStream2.close();
                            } catch (IOException e6) {
                                e6.printStackTrace();
                            }
                            throw th2;
                        }
                        return tempFile;
                    } catch (IOException e7) {
                        e7.printStackTrace();
                        if (tempStream != null) {
                            try {
                                tempStream.flush();
                            } catch (IOException e8) {
                                e8.printStackTrace();
                                if (tempStream != null) {
                                    try {
                                        tempStream.close();
                                    } catch (IOException e9) {
                                        e9.printStackTrace();
                                    }
                                }
                            } catch (Throwable th3) {
                                if (tempStream != null) {
                                    try {
                                        tempStream.close();
                                    } catch (IOException e10) {
                                        e10.printStackTrace();
                                    }
                                }
                                throw th3;
                            }
                        }
                        if (tempStream != null) {
                            tempStream.close();
                        }
                    } catch (Throwable th4) {
                        if (tempStream != null) {
                            try {
                                tempStream.flush();
                            } catch (IOException e11) {
                                e11.printStackTrace();
                                if (tempStream != null) {
                                    try {
                                        tempStream.close();
                                    } catch (IOException e12) {
                                        e12.printStackTrace();
                                    }
                                }
                                throw th4;
                            } catch (Throwable th5) {
                                if (tempStream != null) {
                                    try {
                                        tempStream.close();
                                    } catch (IOException e13) {
                                        e13.printStackTrace();
                                    }
                                }
                                throw th5;
                            }
                        }
                        if (tempStream != null) {
                            tempStream.close();
                        }
                        throw th4;
                    }
                } else {
                    throw new IllegalArgumentException(String.format("Requested frame was: 0 but %d only available.", new Object[]{Integer.valueOf(temp.size())}));
                }
            } else {
                throw new IllegalArgumentException("GifDecoder read routine has ended with an error: " + errorCode);
            }
        }
        return imageFile;
    }

    public void release() {
        GifDecoder gifDecoder = this.mDecoder;
        if (gifDecoder != null) {
            gifDecoder.clean();
        }
    }
}
