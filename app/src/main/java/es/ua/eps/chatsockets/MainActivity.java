package es.ua.eps.chatsockets;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements ServerThread.ServerListener, ClientThread.ClientListener {

    private TextView tvStatus, tvChat;
    private EditText etIp, etPort, etMessage;
    private Button btnStartServer, btnConnectClient, btnSend;

    private ServerThread serverThread;
    private ClientThread clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        tvChat = findViewById(R.id.tvChat);
        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etMessage = findViewById(R.id.etMessage);

        btnStartServer = findViewById(R.id.btnStartServer);
        btnConnectClient = findViewById(R.id.btnConnectClient);
        btnSend = findViewById(R.id.btnSend);

        // Mostrar IP local (puede ser 10.0.2.x en emulador, es normal)
        appendStatus("IP local (dispositivo): " + getLocalIp());

        btnStartServer.setOnClickListener(v -> startServer());
        btnConnectClient.setOnClickListener(v -> connectClient());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void startServer() {
        int port = getPortOrDefault();
        stopServerIfRunning();

        serverThread = new ServerThread(port, this);
        serverThread.start();
    }

    private void connectClient() {
        String ip = etIp.getText().toString().trim();
        int port = getPortOrDefault();

        if (TextUtils.isEmpty(ip)) {
            appendStatus("Cliente: IP vacía");
            return;
        }

        stopClientIfRunning();

        clientThread = new ClientThread(ip, port, this);
        clientThread.start();
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) return;

        appendChat("Yo (Cliente): " + msg);

        if (clientThread == null) {
            appendStatus("Cliente: no conectado");
            return;
        }

        // IMPORTANTE: esto ya NO hace red en UI (solo encola)
        clientThread.sendMessage(msg);

        etMessage.setText("");
    }

    private int getPortOrDefault() {
        String p = etPort.getText().toString().trim();
        if (TextUtils.isEmpty(p)) return 5000;
        try { return Integer.parseInt(p); }
        catch (Exception e) { return 5000; }
    }

    private void stopServerIfRunning() {
        if (serverThread != null) {
            serverThread.stopServer();
            serverThread = null;
        }
    }

    private void stopClientIfRunning() {
        if (clientThread != null) {
            clientThread.stopClient();
            clientThread = null;
        }
    }

    // Callbacks del ServerThread
    @Override
    public void onServerStatus(String msg) {
        runOnUiThread(() -> appendStatus(msg));
    }

    @Override
    public void onServerMessage(String msg) {
        runOnUiThread(() -> appendChat(msg));
    }

    // Callbacks del ClientThread
    @Override
    public void onClientStatus(String msg) {
        runOnUiThread(() -> appendStatus(msg));
    }

    @Override
    public void onClientMessage(String msg) {
        runOnUiThread(() -> appendChat(msg));
    }

    private void appendStatus(String s) {
        tvStatus.setText("Estado: " + s);
    }

    private void appendChat(String s) {
        tvChat.append(s + "\n");
    }

    // Util: IP local
    private String getLocalIp() {
        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "0.0.0.0";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopClientIfRunning();
        stopServerIfRunning();
    }
}
