package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import java.io.File;
import java.io.IOException;

public interface IContentTypeContext {
    Bitmap.CompressFormat getDestinationFormat();

    File getFinalFilePath(File file, String str) throws NullPointerException, IOException;

    void processSpecificData(File file, File file2) throws IOException;

    void validateExtension() throws IOException;
}
