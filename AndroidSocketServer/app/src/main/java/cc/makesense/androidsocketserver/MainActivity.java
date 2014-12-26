package cc.makesense.androidsocketserver;

import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    public static String TAG = "SERVER";

    private ServerSocket serverSocket;

    Handler updateConversationHandler;

    Thread serverThread = null;

    private TextView text;

    public static final int SERVERPORT = 6000;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textIpAddr = (TextView) findViewById(R.id.textIpAddr);
        textIpAddr.setText(getLocalIpAddress());
        text = (TextView) findViewById(R.id.textMessage);

        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

        ((Button) findViewById(R.id.btnClear)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("");
            }
        });

    }

    public String getLocalIpAddress()
    {
        try {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            return ip;
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            this.serverThread.interrupt();
            this.serverThread = null;
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    Log.d (TAG, "Server listen...");
                    socket = serverSocket.accept();
                    Log.d (TAG, "Server connected");

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            try {
                Toast.makeText(getBaseContext(), this.msg, Toast.LENGTH_SHORT).show();
                text.setText(text.getText().toString() + "Client Says: " + msg + "\n");
            }
            catch (Exception ex) {
                Log.d (TAG, ex.toString());
            }
        }
    }
}
