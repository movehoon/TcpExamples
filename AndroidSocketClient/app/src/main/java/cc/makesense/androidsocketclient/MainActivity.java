package cc.makesense.androidsocketclient;

import android.support.v7.app.ActionBarActivity;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.Thread;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {

    private Socket socket;

    private static final int SERVERPORT = 6000;
    private static final String SERVER_IP = "192.168.0.24";

    private Thread clientThread = null;
    private Button btnConnect = null;
    private Button btnSend = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clientThread == null) {
                    clientThread = new Thread(new ClientThread());
                    clientThread.start();
                    btnConnect.setText("Disconnect");
                } else {
                    clientThread.interrupt();
                    clientThread = null;
                    btnConnect.setText("Connect");
                }
            }
        });
        btnConnect.setText("Connect");

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clientThread != null) {
                    try {
                        EditText et = (EditText) findViewById(R.id.EditText01);
                        String str = et.getText().toString();
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(str);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}
