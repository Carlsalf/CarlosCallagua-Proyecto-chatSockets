package es.ua.eps.chatsockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientThread extends Thread {

    public interface ClientListener {
        void onClientStatus(String msg);
        void onClientMessage(String msg);
    }

    private final String host;
    private final int port;
    private final ClientListener listener;

    private volatile boolean running = true;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Cola de salida: UI -> (enqueue) -> Thread de red (dequeue y escribe)
    private final BlockingQueue<String> outQueue = new LinkedBlockingQueue<>();

    public ClientThread(String host, int port, ClientListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            notifyStatus("Cliente: conectando a " + host + ":" + port + " ...");

            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 4000);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            notifyStatus("Cliente: conectado");

            // Bucle principal: leer y enviar sin bloquear UI
            while (running && !socket.isClosed()) {

                // 1) Enviar si hay mensajes pendientes (no bloqueante)
                String msgToSend = outQueue.poll();
                if (msgToSend != null) {
                    out.println(msgToSend); // Esto ocurre en el hilo del cliente (OK)
                }

                // 2) Leer si hay datos disponibles (no bloqueante)
                if (in.ready()) {
                    String line = in.readLine();
                    if (line == null) {
                        notifyStatus("Cliente: servidor cerró la conexión");
                        break;
                    }
                    notifyMessage(line);
                }

                // 3) Evitar busy-loop
                Thread.sleep(15);
            }

        } catch (Exception e) {
            notifyStatus("Cliente ERROR: " + e.getMessage());
        } finally {
            close();
        }
    }

    // Puede llamarse desde Main/UI sin crashear: solo encola
    public void sendMessage(String msg) {
        if (msg == null) return;
        outQueue.offer(msg);
    }

    public void stopClient() {
        running = false;
        close();
        interrupt();
    }

    private void close() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    private void notifyStatus(String s) {
        if (listener != null) listener.onClientStatus(s);
    }

    private void notifyMessage(String s) {
        if (listener != null) listener.onClientMessage(s);
    }
}
