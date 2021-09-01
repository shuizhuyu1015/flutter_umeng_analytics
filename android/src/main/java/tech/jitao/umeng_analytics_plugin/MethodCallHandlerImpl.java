package tech.jitao.umeng_analytics_plugin;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {
    private final MethodChannel channel;
    private final Context context;

    MethodCallHandlerImpl(MethodChannel channel, Context context) {
        this.channel = channel;
        this.context = context;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "init":
                init(call, result);
                break;
            case "pageStart":
                pageStart(call, result);
                break;
            case "pageEnd":
                pageEnd(call, result);
                break;
            case "event":
                event(call, result);
                break;
            case "getChannel":
                _getChannel(call, result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void init(MethodCall call, MethodChannel.Result result) {
        Boolean logEnabled = call.argument("logEnabled");
        if (logEnabled == null) {
            logEnabled = false;
        }
        UMConfigure.setLogEnabled(logEnabled);

        Boolean encryptEnabled = call.argument("encryptEnabled");
        if (encryptEnabled == null) {
            encryptEnabled = false;
        }
        UMConfigure.setEncryptEnabled(encryptEnabled);

        final String androidKey = call.argument("androidKey");
        String channel = call.argument("channel");
        if(channel == null) {
            channel = getChannelName(context);
        }
        UMConfigure.init(context, androidKey, channel, UMConfigure.DEVICE_TYPE_PHONE, null);

        Integer sessionContinueMillis = call.argument("sessionContinueMillis");
        if (sessionContinueMillis == null) {
            sessionContinueMillis = 30000;
        }
        MobclickAgent.setSessionContinueMillis(sessionContinueMillis);

        Boolean catchUncaughtExceptions = call.argument("catchUncaughtExceptions");
        if (catchUncaughtExceptions == null) {
            catchUncaughtExceptions = true;
        }
        MobclickAgent.setCatchUncaughtExceptions(catchUncaughtExceptions);

        if ("MANUAL".equals(call.argument("pageCollectionMode"))) {
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);
        } else {
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        }

        result.success(true);
    }

    private void pageStart(MethodCall call, MethodChannel.Result result) {
        final String viewName = call.argument("viewName");

        MobclickAgent.onPageStart(viewName);

        result.success(true);
    }

    private void pageEnd(MethodCall call, MethodChannel.Result result) {
        final String viewName = call.argument("viewName");

        MobclickAgent.onPageEnd(viewName);

        result.success(true);
    }

    private void event(MethodCall call, MethodChannel.Result result) {
        final String eventId = call.argument("eventId");
        final String label = call.argument("label");
        final Map<String, String> attributes = call.argument("attributes");
        if (label != null){
            MobclickAgent.onEvent(context, eventId, label);
        }else if (attributes != null){
            MobclickAgent.onEvent(context, eventId, attributes);
        }else{
            MobclickAgent.onEvent(context, eventId);
        }


        result.success(true);
    }

    private void _getChannel(MethodCall call, MethodChannel.Result result) {
        String channelName = getChannelName(context);
        result.success(channelName);
    }

    // 获取渠道工具函数
    public static String getChannelName(Context ctx) {
        if (ctx == null) {
            return null;
        }
        String channelName = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = applicationInfo.metaData.get("UMENG_CHANNEL")+"";
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(channelName) || "null".equalsIgnoreCase(channelName)){
            channelName="Unknown";
        }
        return channelName;
    }
}
