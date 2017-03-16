package org.zywx.wbpalmstar.acedes;

import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class ApkResourceUtils {

    private Resources res;
    private String packageName;

    public ApkResourceUtils(Context context, String apkPath) {
        init(context, apkPath);
    }

    private void init(Context context, String apkPath) {
        try {
            AssetManager am = AssetManager.class.newInstance();
            am.getClass().getMethod("addAssetPath", String.class)
                    .invoke(am, apkPath);
            res = new Resources(am, context.getApplicationContext()
                    .getResources().getDisplayMetrics(), context
                    .getApplicationContext().getResources().getConfiguration());
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                    PackageManager.GET_ACTIVITIES);
            if(info != null){
                packageName = info.packageName;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String getString(String name) {
        if (res == null) {
            return null;
        }
        int id = getId(name);
        return res.getString(id);
    }

    public int getId(String name) {
        return res.getIdentifier(name, "string", packageName);
    }
}
