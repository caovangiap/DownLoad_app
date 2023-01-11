package com.muicvtools.mutils.downloads;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
public class ScriptUtils {
//    public static final String TIKTOK_SCRIPT = "javascript:function startDownload() { document.getElementById('%s').value = '%s';" +
//            "document.getElementById('%s').click(); } startDownload();";
//    public static final String TIKTOK_SCRIPT = "javascript:function startDownload() { document.getElementById('%s').value = '%s';" +
//            "document.getElementById('%s').click();" +
//            "var counter = 0;" +
//            "var i = setInterval(function(){" +
//            "    counter++;" +
//            "var urlStream = %s;" +
//            "if(urlStream != null && urlStream != undefined){" +
//            "var title = %s;" +
//            "var thumb = %s;" +
//            "const obResult = {title: title,thumb :thumb,urlStream: urlStream};" +
//            "const result = JSON.stringify(obResult); " +
//            "browser.getData(result);"+
//            "clearInterval(i);" +
//            "}" +
//            "    if(counter === 10) {" +
//            "     browser.getData('time_out');"+
//            "        clearInterval(i);" +
//            "    }" +
//            "}, 1000); } startDownload();";

    public static final String SAVETIK_URL = "https://savetik.net/";
    public static final String SAVETIK_SCRIPT = "javascript: function startDownload() {" +
            "    document.getElementById('url').value = '%s';" +
            "document.getElementById('send').scrollIntoView();" +
            "    var counter = 0;" +
            "    var i = setInterval(function() {" +
            "        counter++;" +
            "if(counter == 1) {document.getElementById('send').click();};" +
            "        if (counter === 6) {" +
            "            browser.getData('time_out');" +
            "            clearInterval(i);" +
            "        }" +
            "        var urlStream =  document.getElementsByClassName('abutton is-success is-fullwidth')[0].getAttribute('href');" +
            "        var urlStream2 = document.getElementsByClassName('abutton is-success is-fullwidth')[1].getAttribute('href');" +
            "        if (urlStream != null && urlStream != undefined) {" +
            "            var title = document.getElementsByTagName('img')[0].getAttribute('alt');" +
            "            var thumb = document.getElementsByTagName('img')[0].getAttribute('src');" +
            "            const obResult = {" +
            "                title: title," +
            "                thumb: thumb," +
            "                urlStream: urlStream," +
            "                urlStream2: urlStream" +
            "            };" +
            "            const result = JSON.stringify(obResult);" +
            "            browser.getData(result);" +
            "            clearInterval(i);" +
            "        }" +
            "    }, 1000);" +
            "}" +
            "startDownload();";

    public static final String SNAPTIK_URL = "https://snaptik.app";
    public static final String SNAPTIK_SCRIPT = "javascript: function startDownload() {" +
            "    document.getElementById('url').value = '%s';" +
            "    document.getElementsByClassName('btn btn-go flex-center').click();" +
            "    var counter = 0;" +
            "    var i = setInterval(function() {" +
            "        counter++;" +
            "        if (counter === 6) {" +
            "            browser.getData('time_out');" +
            "            clearInterval(i);" +
            "        }" +
            "        var urlStream = document.getElementsByClassName('btn btn-main active mb-2')[0].getAttribute('href');" +
//            "        var urlStream2 = document.getElementsByClassName('abutton is-success is-fullwidth')[1].getAttribute('href');" +
            "        if (urlStream != null && urlStream != undefined) {" +
            "            var title = document.getElementsByTagName('img')[0].getAttribute('alt');" +
            "            var thumb = document.getElementsByTagName('img')[0].getAttribute('src');" +
            "            const obResult = {" +
            "                title: title," +
            "                thumb: thumb," +
            "                urlStream: urlStream" +
//            "                urlStream2: urlStream" +
            "            };" +
            "            const result = JSON.stringify(obResult);" +
            "            browser.getData(result);" +
            "            clearInterval(i);" +
            "        }" +

            "    }, 1000);" +
            "}" +
            "startDownload();";

    public static final String SSSTIK_URL = "https://ssstik.io";
    public static final String SSSTIK_SCRIPT = "javascript: function startDownload() {" +
            "    document.getElementById('main_page_text').value = '%s';" +
            "    document.getElementById('submit').click();" +
            "    var counter = 0;" +
            "    var i = setInterval(function() {" +
            "        counter++;" +
            "        if (counter === 6) {" +
            "            browser.getData('time_out');" +
            "            clearInterval(i);" +
            "        }" +
            "            var title = document.getElementsByClassName('maintext')[0].textContent;" +
            "            var thumb = document.getElementsByClassName('u-round result_author')[0].getAttribute('src');" +
            "        var urlStream = document.getElementsByClassName('pure-button pure-button-primary is-center u-bl dl-button download_link without_watermark_direct')[0].getAttribute('href');" +
            "        if (urlStream != null && urlStream != undefined) {" +
            "            const obResult = {" +
            "                title: title," +
            "                thumb: thumb," +
            "                urlStream: urlStream," +
            "                urlStream2: urlStream" +
            "            };" +
            "            const result = JSON.stringify(obResult);" +
            "            browser.getData(result);" +
            "            clearInterval(i);" +
            "        }" +

            "    }, 1000);" +
            "}" +
            "startDownload();";

    public static final String TIKMATE_URL = "https://tikmate.online";
    public static final String TIKMATE_SCRIPT = "javascript: function startDownload() {" +
            "    document.getElementById('url').value = '%s';" +
            "    document.getElementById('send').click();" +
            "    var counter = 0;" +
            "    var i = setInterval(function() {" +
            "        counter++;" +
            "        if (counter === 6) {" +
            "            browser.getData('time_out');" +
            "            clearInterval(i);" +
            "        }" +
            "        var urlStream =  document.getElementsByClassName('abutton is-success is-fullwidth')[1].getAttribute('href');" +
            "        var urlStream2 = document.getElementsByClassName('abutton is-success is-fullwidth')[2].getAttribute('href');" +
            "        if (urlStream != null && urlStream != undefined) {" +
            "            var title = document.getElementsByTagName('img')[0].getAttribute('alt');" +
            "            var thumb = document.getElementsByTagName('img')[0].getAttribute('src');" +
            "            const obResult = {" +
            "                title: title," +
            "                thumb: thumb," +
            "                urlStream: urlStream," +
            "                urlStream2: urlStream" +
            "            };" +
            "            const result = JSON.stringify(obResult);" +
            "            browser.getData(result);" +
            "            clearInterval(i);" +
            "        }" +

            "    }, 1000);" +
            "}" +
            "startDownload();";

//    public static String title = "document.getElementsByTagName('img')[0].getAttribute('alt')";
//    public static String thumb = "document.getElementsByTagName('img')[0].getAttribute('src')";
//    public static String urlStream = "window.location.href + document.getElementsByClassName('abutton is-success is-fullwidth')[0].getAttribute('href')";
//
//    private SCRIPT_TYPE current_Script = SCRIPT_TYPE.SAVE_TIK;
//
//    public static String getTiktokScript(String idInput, String url_source, String idSubmit, String urlStream, String title, String thumb) {
//        return String.format(TIKTOK_SCRIPT, idInput, url_source, idSubmit, urlStream, title, thumb);
//    }



    private static final String SCRIPT_PREF = "SCRIPT";

//    private static ScriptUtils instance;
//    public static ScriptUtils getSharedInstance() {
//        if (instance == null) {
//            instance = new ScriptUtils();
//        }
//        return instance;
//    }
//
//    public void initScript() {
//
//    }

    public static ScriptInfo getCurrentScript(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(SCRIPT_PREF, Context.MODE_PRIVATE);
        int current = mPrefs.getInt("current", 0);
        SCRIPT_TYPE current_script = getScriptType(current);

        if (mPrefs.contains(current_script.name())) {
            Gson gson = new GsonBuilder().create();
            ScriptInfo scriptInfo;
            try
            {
                scriptInfo = gson.fromJson(mPrefs.getString(current_script.name(),"error"),ScriptInfo.class);
                return scriptInfo;
            }
            catch (JsonSyntaxException e)
            {
                changeScript(context);
                return getCurrentScript(context);
            }
        } else {

            switch (current_script) {

                case SAVE_TIK:
                    return new ScriptInfo(SAVETIK_URL, SAVETIK_SCRIPT, SCRIPT_TYPE.SAVE_TIK);
                case SNAP_TIK:
                    return new ScriptInfo(SNAPTIK_URL, SNAPTIK_SCRIPT, SCRIPT_TYPE.SNAP_TIK);
                case TIK_MATE:
                    return new ScriptInfo(TIKMATE_URL, TIKMATE_SCRIPT, SCRIPT_TYPE.TIK_MATE);
                case SSS_TIK:
                    return new ScriptInfo(SSSTIK_URL, SSSTIK_SCRIPT, SCRIPT_TYPE.SSS_TIK);
                default:
                    return null;
            }
        }
    }

    public static void changeScript(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(SCRIPT_PREF, Context.MODE_PRIVATE);
        int current = mPrefs.getInt("current", 0);
        current++;
        if(current == 4)
            current = 0;

        mPrefs.edit().putInt("current",current).apply();
    }

    public static void setCurrentScript(Context context,int script) {
        SharedPreferences mPrefs = context.getSharedPreferences(SCRIPT_PREF, Context.MODE_PRIVATE);
        mPrefs.edit().putInt("current",script).apply();
    }

    public static void updateScript(Context context,ScriptInfo scriptInfo) {
        SCRIPT_TYPE script_type = getScriptType(scriptInfo.url_web);
        SharedPreferences mPrefs = context.getSharedPreferences(SCRIPT_PREF, Context.MODE_PRIVATE);
        Gson gson = new GsonBuilder().create();
        mPrefs.edit().putString(script_type.name(),gson.toJson(scriptInfo)).apply();
    }

    private static SCRIPT_TYPE getScriptType(int script) {
        switch (script) {
            case 0:
                return SCRIPT_TYPE.SAVE_TIK;
            case 1:
                return SCRIPT_TYPE.SNAP_TIK;
            case 2:
                return SCRIPT_TYPE.TIK_MATE;
            case 3:
                return SCRIPT_TYPE.SSS_TIK;
            default:
                return SCRIPT_TYPE.OTHER;
        }
    }

    private static SCRIPT_TYPE getScriptType(String url) {
        switch (url) {
            case SAVETIK_URL:
                return SCRIPT_TYPE.SAVE_TIK;
            case SNAPTIK_URL:
                return SCRIPT_TYPE.SNAP_TIK;
            case TIKMATE_URL:
                return SCRIPT_TYPE.TIK_MATE;
            case SSSTIK_URL:
                return SCRIPT_TYPE.SSS_TIK;
            default:
                return SCRIPT_TYPE.OTHER;
        }
    }

}
