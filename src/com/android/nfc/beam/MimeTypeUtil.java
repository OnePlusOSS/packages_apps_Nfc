/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.nfc.beam;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

public final class MimeTypeUtil {

    private static final String TAG = "MimeTypeUtil";

    private MimeTypeUtil() {}

    public static String getMimeTypeForUri(Context context, Uri uri) {
        if (uri.getScheme() == null) return null;

        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
            if(mimeType == null){
                 String extension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath()).toLowerCase();
                  if (extension != null) {
                      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                  }
            }
            if (mimeType == null) {
                mimeType = "*.*";
            }
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            /*allow to parse chinese file name*/
            String path = uri.toString().substring("file://".length());
            //String extension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath()).toLowerCase();
            String extension = MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase();
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }

            if (mimeType == null) {
                mimeType = "*.*";
            }
        } else {
            Log.d(TAG, "Could not determine mime type for Uri " + uri);
            mimeType = null;
        }

        return mimeType;
    }
}
