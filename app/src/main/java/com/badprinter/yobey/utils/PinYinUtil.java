package com.badprinter.yobey.utils;

import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by root on 15-8-26.
 */
public class PinYinUtil {
    private final String TAG = "PinYinUtil";
    /*
     * Output is upper case or lower case
     */
    public static final String UPPER_CASE = "upper_case";
    public static final String LOWER_CASE = "lower_case";

    /*
     * About tone:
     * 1. change tone to number between 1-4
     * 2. without tone
     * 3. with tone mark
     */
    public static final String WITH_TONE_NUMBER = "with_tone_number";
    public static final String WITHOUT_TONE = "without_tone";
    public static final String WITH_TONE_MARK = "with_tone_mark";

    /*
     * About how to express ü in pinyin:
     * 1. turn ü to u
     * 2. turn ü to v
     * 3. ü
     */
    public static final String WITH_U_AND_COLON = "with_u_and_colon";
    public static final String WITH_V = "with_v";
    public static final String WITH_U_UNICODE = "with_u_unicode";

    public static String getPinYinFromHanYu(String hanyu, String caseType,
                                            String toneType, String vType) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        /*
         * Choose caseType
         */
        if (caseType == UPPER_CASE) {
            format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        } else {
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        }
        /*
         * Choose toneType
         */
        if (toneType == WITHOUT_TONE) {
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        } else if (toneType == WITH_TONE_NUMBER) {
            format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
        } else {
            format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
        }
        /*
         * Choose vType
         */

        if (vType == WITH_V) {
            format.setVCharType(HanyuPinyinVCharType.WITH_V);
        } else if (vType == WITH_U_AND_COLON) {
            format.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);
        } else {
            format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        }
        StringBuffer output = new StringBuffer("");
        char[] input = hanyu.trim().toCharArray();
        try {
            for (int i = 0 ; i < input.length ; i++) {
                if (Character.toString(input[i]).matches("[\u4E00-\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    //Log.e("PinYinUtil", "The size of temp is " + Integer.toString(temp.length));
                    // Choose the first one
                    output.append(temp[0]);
                    output.append(" ");
                } else
                    output.append(Character.toString(input[i]));
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            //Log.e("PinYinUtil", "catches");
            e.printStackTrace();
        }
        return output.toString();
    }
}
