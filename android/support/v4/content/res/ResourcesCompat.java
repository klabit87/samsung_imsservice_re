package android.support.v4.content.res;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.FontResourcesParserCompat;
import android.support.v4.graphics.TypefaceCompat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public final class ResourcesCompat {
    private static final String TAG = "ResourcesCompat";

    public static Drawable getDrawable(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 21) {
            return res.getDrawable(id, theme);
        }
        return res.getDrawable(id);
    }

    public static Drawable getDrawableForDensity(Resources res, int id, int density, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 21) {
            return res.getDrawableForDensity(id, density, theme);
        }
        if (Build.VERSION.SDK_INT >= 15) {
            return res.getDrawableForDensity(id, density);
        }
        return res.getDrawable(id);
    }

    public static int getColor(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 23) {
            return res.getColor(id, theme);
        }
        return res.getColor(id);
    }

    public static ColorStateList getColorStateList(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 23) {
            return res.getColorStateList(id, theme);
        }
        return res.getColorStateList(id);
    }

    public static Typeface getFont(Context context, int id) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, new TypedValue(), 0, (TextView) null);
    }

    public static Typeface getFont(Context context, int id, TypedValue value, int style, TextView targetView) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, value, style, targetView);
    }

    private static Typeface loadFont(Context context, int id, TypedValue value, int style, TextView targetView) {
        Resources resources = context.getResources();
        resources.getValue(id, value, true);
        Typeface typeface = loadFont(context, resources, value, id, style, targetView);
        if (typeface != null) {
            return typeface;
        }
        throw new Resources.NotFoundException("Font resource ID #0x" + Integer.toHexString(id));
    }

    private static Typeface loadFont(Context context, Resources wrapper, TypedValue value, int id, int style, TextView targetView) {
        Resources resources = wrapper;
        TypedValue typedValue = value;
        int i = id;
        int i2 = style;
        if (typedValue.string != null) {
            String file = typedValue.string.toString();
            if (!file.startsWith("res/")) {
                return null;
            }
            Typeface cached = TypefaceCompat.findFromCache(resources, i, i2);
            if (cached != null) {
                return cached;
            }
            try {
                if (file.toLowerCase().endsWith(".xml")) {
                    FontResourcesParserCompat.FamilyResourceEntry familyEntry = FontResourcesParserCompat.parse(resources.getXml(i), resources);
                    if (familyEntry != null) {
                        return TypefaceCompat.createFromResourcesFamilyXml(context, familyEntry, wrapper, id, style, targetView);
                    }
                    Log.e(TAG, "Failed to find font-family tag");
                    return null;
                }
                try {
                    return TypefaceCompat.createFromResourcesFontFile(context, resources, i, file, i2);
                } catch (XmlPullParserException e) {
                    e = e;
                    Log.e(TAG, "Failed to parse xml resource " + file, e);
                    return null;
                } catch (IOException e2) {
                    e = e2;
                    Log.e(TAG, "Failed to read xml resource " + file, e);
                    return null;
                }
            } catch (XmlPullParserException e3) {
                e = e3;
                Context context2 = context;
                Log.e(TAG, "Failed to parse xml resource " + file, e);
                return null;
            } catch (IOException e4) {
                e = e4;
                Context context3 = context;
                Log.e(TAG, "Failed to read xml resource " + file, e);
                return null;
            }
        } else {
            Context context4 = context;
            throw new Resources.NotFoundException("Resource \"" + resources.getResourceName(i) + "\" (" + Integer.toHexString(id) + ") is not a Font: " + typedValue);
        }
    }

    private ResourcesCompat() {
    }
}
