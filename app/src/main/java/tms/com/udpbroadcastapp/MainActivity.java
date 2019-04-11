package tms.com.udpbroadcastapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    private EditText mEditTextIP;
    private EditText mEditTextPort;
    private EditText mEditTextMsg;
    private TextView mTextViewResp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ClickListener clickListener = new ClickListener();
        findViewById(R.id.button).setOnClickListener(clickListener);
        mEditTextIP = (EditText) findViewById(R.id.editTextIP);
        mEditTextPort = (EditText) findViewById(R.id.editTextPort);
        mEditTextMsg = (EditText) findViewById(R.id.editTextMsg);
        mTextViewResp = (TextView) findViewById(R.id.textViewResp);
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final String ip = mEditTextIP.getText().toString();
            final String port = mEditTextPort.getText().toString();
            final String message = mEditTextMsg.getText().toString();
            mTextViewResp.setText("Sending...");
            Log.i(TAG, "onClick(): send " + ip + ":" + port + " " + message);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    send(ip, port, message);
                }
            }).start();
        }
    }

    public void send(String ip, String port, String message) {
        message = (message == null ? "Hello IdeasAndroid!" : message);
        int server_port = Integer.parseInt(port);
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            updateResp(e.toString());
        }
        InetAddress local = null;
        try {
            // 换成服务器端IP
            local = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            updateResp(e.toString());
        }
        int msg_length = message.length();
        byte[] messageByte = message.getBytes();
        DatagramPacket p = new DatagramPacket(messageByte, msg_length, local, server_port);
        try {
            s.send(p);
        } catch (IOException e) {
            e.printStackTrace();
            updateResp(e.toString());
        }

        DatagramPacket packetResp = new DatagramPacket(new byte[2048], 2048);
        try {
            s.setSoTimeout(2000);
            s.receive(packetResp);
            String data = new String(packetResp.getData(), 0 , packetResp.getLength());
            Log.i(TAG, "send(): resp=\n" + data);
            updateResp(data);
        } catch (IOException e) {
            e.printStackTrace();
            updateResp(e.toString());
        }

        s.close();
    }

    public void updateResp(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextViewResp.setText(message);
            }
        });
    }
}
