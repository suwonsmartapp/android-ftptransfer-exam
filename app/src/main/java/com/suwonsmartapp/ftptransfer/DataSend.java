
package com.suwonsmartapp.ftptransfer;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by junsuk on 14. 12. 19..
 */
public class DataSend extends AsyncTask<String, Void, String> implements
        DialogInterface.OnCancelListener {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private FTPClient mFTPClient;

    public DataSend(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(this);
        mProgressDialog.setTitle("데이터 송신");
        mProgressDialog.setMessage("업로드 중");
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String remoteserver = params[0];                // FTP 서버
        int remoteport = Integer.parseInt(params[1]);   // FTP 서버 포트
        String remotefile = params[2];                  // 서버 폴더
        String userid = params[3];                      // 로그인 id
        String passwd = params[4];                      // 패스워드
        boolean passive = Boolean.valueOf(params[5]);   // passive 모드

        // FTP 파일 전송
        FTP ftp = new FTP(mContext);
        String result = ftp.putData(remoteserver, remoteport, userid, passwd, passive, remotefile);

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        mProgressDialog.dismiss();
        Log.d("test", result);
        if (result == null) {
            Toast.makeText(mContext, "데이터 전송 완료", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "데이터 전송 에러", Toast.LENGTH_SHORT).show();
        }
    }

    // 캔슬 처리
    @Override
    public void onCancel(DialogInterface dialog) {
        this.cancel(true);
    }

    @Override
    protected void onCancelled() {
        try {
            mFTPClient.abort();
        } catch (Exception e) {
        }
        Toast.makeText(mContext, "데이터 전송 취소", Toast.LENGTH_SHORT).show();
    }

    // FTP 클라이언트 : apache commons net 라이브러리 사용
    private class FTP extends ContextWrapper {
        public FTP(Context base) {
            super(base);
        }

        private String putData(String remoteServer, int remotePort,
                String userId, String password, boolean passive, String remoteFile) {
            int reply = 0;
            boolean isLogin = false;
            mFTPClient = new FTPClient();

            try {
                mFTPClient.setConnectTimeout(5000);
                // 접속
                mFTPClient.connect(remoteServer, remotePort);
                reply = mFTPClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    throw new Exception("Connect Status:" + String.valueOf(reply));
                }
                // 로그인
                if (!mFTPClient.login(userId, password)) {
                    throw new Exception("Invalid user/password");
                }
                isLogin = true;
                // 전송 모드
                if (passive) {
                    mFTPClient.enterLocalPassiveMode(); // 패시브
                } else {
                    mFTPClient.enterLocalActiveMode(); // 액티브
                }
                // 파일 전송
                mFTPClient.setDataTimeout(15000);
                mFTPClient.setSoTimeout(15000);
                FileInputStream fileInputStream = this.openFileInput(remoteFile);   // 전송 파일 명
                mFTPClient.storeFile(remoteFile, fileInputStream);
                reply = mFTPClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    throw new Exception("Send Status:" + String.valueOf(reply));
                }
                fileInputStream.close();
                // 로그아웃
                mFTPClient.logout();
                isLogin = false;
                // 접속 끊음
                mFTPClient.disconnect();
            } catch (Exception e) {
                return e.getMessage();
            } finally {
                if (isLogin) {
                    try {
                        mFTPClient.logout();
                    } catch (IOException e) {
                    }
                }
                if (mFTPClient.isConnected()) {
                    try {
                        mFTPClient.disconnect();
                    } catch (IOException e) {
                    }
                }
                mFTPClient = null;
            }
            return null;
        }
    }
}
