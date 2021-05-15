package com.sec.internal.helper.userconsent;

import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import java.util.regex.Matcher;

public final class HyperlinkUtils {
    private HyperlinkUtils() {
    }

    public static void processLinks(TextView textView, String message, IHyperlinkOnClickListener hyperlinkOnClickListener) {
        Matcher matcher = HyperlinkPatterns.webUrlPattern.matcher(message);
        StringBuilder messageBuilder = new StringBuilder();
        int startIndex = 0;
        while (matcher.find(startIndex)) {
            String url = matcher.group();
            messageBuilder.append(message.substring(startIndex, matcher.start()));
            messageBuilder.append("<a href='");
            if (!url.contains(OMAGlobalVariables.HTTP) && !url.contains("rtsp")) {
                messageBuilder.append("http://");
            }
            messageBuilder.append(url);
            messageBuilder.append("'>");
            messageBuilder.append(url);
            messageBuilder.append("</a>");
            startIndex = matcher.end() > message.length() ? message.length() : matcher.end();
        }
        messageBuilder.append(message.substring(startIndex, message.length()));
        CharSequence messageSequence = Html.fromHtml(messageBuilder.toString(), 0);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(messageSequence);
        for (URLSpan urlSpan : (URLSpan[]) spannableStringBuilder.getSpans(0, messageSequence.length(), URLSpan.class)) {
            makeLinkClickable(spannableStringBuilder, urlSpan, hyperlinkOnClickListener);
        }
        textView.setText(spannableStringBuilder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static void makeLinkClickable(SpannableStringBuilder stringBuilder, final URLSpan span, final IHyperlinkOnClickListener hyperlinkOnClickListener) {
        stringBuilder.setSpan(new ClickableSpan() {
            public void onClick(View view) {
                IHyperlinkOnClickListener.this.onClick(view, Uri.parse(span.getURL()));
            }
        }, stringBuilder.getSpanStart(span), stringBuilder.getSpanEnd(span), stringBuilder.getSpanFlags(span));
        stringBuilder.removeSpan(span);
    }
}
