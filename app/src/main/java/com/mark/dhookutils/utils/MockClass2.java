package com.mark.dhookutils.utils;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import com.mark.dhookutils.reflect.RefInvoke;

import java.lang.reflect.Proxy;


/**
 * @author weishu
 * @date 16/1/7
 */
/* package */ class MockClass2 implements Handler.Callback {

    Handler mBase;

    public MockClass2(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100:
                handleLaunchActivity(msg);
                break;
        }

        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;

        Object obj = msg.obj;

        // 把替身恢复成真身
        Intent raw = (Intent) RefInvoke.getFieldObject(obj, "intent");

        Intent target = raw.getParcelableExtra(HookActivityUtils.EXTRA_TARGET_INTENT);
        raw.setComponent(target.getComponent());

        //修改packageName，这样缓存才能命中
        ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(obj, "activityInfo");
        activityInfo.applicationInfo.packageName = target.getPackage() == null ?
                target.getComponent().getPackageName() : target.getPackage();

        try {
            hookPackageManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hookPackageManager() throws Exception {

        // 这一步是因为 initializeJavaContextClassLoader 这个方法内部无意中检查了这个包是否在系统安装
        // 如果没有安装, 直接抛出异常, 这里需要临时Hook掉 PMS, 绕过这个检查.
        Object currentActivityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");

        // 获取ActivityThread里面原始的 sPackageManager
        Object sPackageManager = RefInvoke.getFieldObject(currentActivityThread, "sPackageManager");

        // 准备好代理对象, 用来替换原始的对象
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[] { iPackageManagerInterface },
                new MockClass3(sPackageManager));

        // 1. 替换掉ActivityThread里面的 sPackageManager 字段
        RefInvoke.setFieldObject(currentActivityThread, "sPackageManager", proxy);
    }
}
