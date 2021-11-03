package de.fs_cse.ulmapp;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputFilterASCII implements InputFilter {
    Pattern mPattern;

    public InputFilterASCII(){
        mPattern = Pattern.compile("\\p{ASCII}*");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String formatedSource = dest.subSequence(0, dstart).toString();
        String destPrefix = source.subSequence(start, end).toString();
        String destSuffix = dest.subSequence(dend, dest.length()).toString();
        CharSequence match = TextUtils.concat(formatedSource, destPrefix, destSuffix);
        Matcher matcher = mPattern.matcher(match);
        if (!matcher.matches())
            return "File contains non ASCII-Characters!";
        return null;
    }
}
