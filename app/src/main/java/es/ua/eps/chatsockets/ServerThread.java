package es.ua.eps.chatsockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerThread extends Thread {

    public interface ServerListener {
        void onServerStatus(String msg);
        void onServerMessage(String msg);
    }

    private final int port;
    private final ServerListener listener;

    private volatile boolean running = true;

    private ServerSocket serverSocket;

    // Lista segura para iterar mientras se conectan/desconectan clientes
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public ServerThread(int port, ServerListener listener) {
        this.port = port;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            notifyStatus("Servidor: iniciando en puerto " + port + " ...");
            serverSocket = new ServerSocket(port);
            notifyStatus("Servidor: escuchando en " + port);

            while (running) {
                Socket socket = serverSocket.accept(); // bloquea hasta que llegue un cliente
                if (!running) {
                    safeClose(socket);
                    break;
                }

                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);

                notifyStatus("Servidor: cliente conectado (" + socket.getInetAddress().getHostAddress() + ")");
                handler.start();
            }

        } catch (Exception e) {
            if (running) {
                notifyStatus("Servidor ERROR: " + e.getMessage());
            } else {
                notifyStatus("Servidor: detenido");
            }
        } finally {
            stopServer(); // asegura cierre total
        }
    }

    public void stopServer() {
        running = false;

        // Cierra serverSocket para desbloquear accept()
        safeClose(serverSocket);

        // Cierra todos los clientes
        for (ClientHandler c : clients) {
            c.close();
        }
        clients.clear();

        interrupt();
    }

    private void broadcast(String msg) {
        // Envía a todos los clientes conectados
        for (ClientHandler c : clients) {
            c.send(msg);
        }
    }

    private void removeClient(ClientHandler c) {
        clients.remove(c);
        notifyStatus("Servidor: cliente desconectado (clientes activos: " + clients.size() + ")");
    }

    private void notifyStatus(String s) {
        if (listener != null) listener.onServerStatus(s);
    }

    private void notifyMessage(String s) {
        if (listener != null) listener.onServerMessage(s);
    }

    private static void safeClose(ServerSocket ss) {
        try { if (ss != null) ss.close(); } catch (IOException ignored) {}
    }

    private static void safeClose(Socket s) {
        try { if (s != null) s.close(); } catch (IOException ignored) {}
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private volatile boolean alive = true;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Opcional: mensaje de bienvenida solo para ese cliente
                send("Servidor: conectado. Clientes activos: " + clients.size());

                String line;
                while (alive && running && (line = in.readLine()) != null) {
                    String incoming = "Cliente (" + socket.getInetAddress().getHostAddress() + "): " + line;
                    notifyMessage(incoming);

                    // ECO/BROADCAST
                    broadcast("Servidor: " + line);
                }

            } catch (Exception e) {
                if (running) {
                    notifyStatus("Servidor: error con cliente: " + e.getMessage());
                }
            } finally {
                close();
                removeClient(this);
            }
        }

        void send(String msg) {
            try {
                if (out != null) out.println(msg);
            } catch (Exception ignored) {}
        }

        void close() {
            alive = false;
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            safeClose(socket);
        }
    }
}
