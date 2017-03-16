package org.zywx.wbpalmstar.platform.decrypt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.input.BOMInputStream;
import org.zywx.wbpalmstar.acedes.ApkResourceUtils;
import org.zywx.wbpalmstar.acedes.DESUtility;
import org.zywx.wbpalmstar.zip.CnZipInputStream;
import org.zywx.wbpalmstar.zip.ZipEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DecryptActivity extends Activity {

    public int FILE_SELECT_CODE = 0;
    private String apkPath = "";
    private String filePath = "";
    private TextView widgetPathView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decrypt_layout);
        widgetPathView = (TextView) findViewById(R.id.widgetPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FILE_SELECT_CODE) {
                Uri uri = data.getData();
                if (null != uri) {
                    String path = uri.getPath().substring(1);
                    if (path.endsWith(".apk")) {
                        apkPath = path;
                        filePath = apkPath.substring(0, apkPath.lastIndexOf('.'));
                    } else {
                        Toast.makeText(this, "请选择APK文件", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openFile(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    public void copyAndDecryptWidget(View view){
        if(TextUtils.isEmpty(apkPath)) {
            return;
        }
        new DecryptAsyncTask().execute();
    }

    private class DecryptAsyncTask extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            boolean isCopyFinish = copyWidget();
            return Boolean.valueOf(isCopyFinish);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (b.booleanValue()) {
                String appkey = new ApkResourceUtils(DecryptActivity.this, apkPath).getString("appkey");
                Log.i("Decrypt", "appkey " + appkey);
                decryptWidget(appkey);
            }
            super.onPostExecute(b);
        }
    }

    private boolean copyWidget() {
        boolean isCopyFinish = false;
        try {
            CnZipInputStream in = new CnZipInputStream(new FileInputStream(apkPath), "UTF-8");
            ZipEntry entry = in.getNextEntry();
            while (entry != null) {
                String zename = entry.getName();
                if (zename.startsWith("assets/widget/")) {
                    String tempFilePath = filePath + "/" + zename;
                    File tempFileDir = new File(tempFilePath).getParentFile();
                    if (!tempFileDir.exists()) {
                        tempFileDir.mkdirs();
                    }

                    FileOutputStream out = new FileOutputStream(new File(tempFilePath));
                    byte[] c = new byte[1024 * 2];
                    int slen;
                    while ((slen = in.read(c, 0, c.length)) != -1)
                        out.write(c, 0, slen);
                }
                entry = in.getNextEntry();
            }
            in.close();
            isCopyFinish = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCopyFinish;
    }

    private void decryptWidget(String appkey) {
        try {
            String widgetPath = filePath + "/" + "assets/widget/";
            String[] lists = new File(widgetPath).list();
            for (int i = 0; i < lists.length; i++) {
                if (lists[i].endsWith(".html") || lists[i].endsWith(".css") || lists[i].endsWith(".js")
                        || lists[i].endsWith(".htm") || lists[i].endsWith(".xml")) {
                    String path = widgetPath + lists[i];
                    File file = new File(path);
                    InputStream input = new FileInputStream(file);
                    InputStream tempInput = new FileInputStream(file);
                    BOMInputStream bomInputStream = new BOMInputStream(tempInput);
                    if (bomInputStream.hasBOM()) {
                        input = bomInputStream;
                    } else {
                        bomInputStream.close();
                        tempInput.close();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = input.read(buffer)) > -1) {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();

                    InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
                    InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
                    boolean isV = DESUtility.isEncrypted(is1);
                    if (isV) {
                        byte[] data = DESUtility.transStreamToBytes(is2, is2.available());
                        String fileName = DESUtility.getFileNameWithNoSuffix(path);
                        String result = DESUtility.htmlDecode(data, fileName, appkey);
                        InputStream is = new ByteArrayInputStream(result.getBytes());
                        if (file.exists()) {
                            file.delete();
                            FileOutputStream out = new FileOutputStream(new File(path));
                            byte[] c = new byte[1024 * 2];
                            int slen;
                            while ((slen = is.read(c, 0, c.length)) != -1)
                                out.write(c, 0, slen);
                        }
                    }
                }
            }
            widgetPathView.setText(widgetPath);
            Toast.makeText(DecryptActivity.this, "解密完成", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
