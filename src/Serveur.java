import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Serveur {
    private String ip;
    private int port;
    private Colis colis;
    private DBConnect db;
    private Livreur lv;

    private ArrayList<String[]> colisData = new ArrayList<>();
    private ArrayList<String[]> livreursData = new ArrayList<>();
    private ArrayList<String[]> positionsData = new ArrayList<>();

    public Serveur(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private void loadCSV(String fileName, ArrayList<String[]> stockage) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                stockage.add(line.split(",", -1));
            }
            System.out.println(fileName + " chargé avec " + stockage.size() + " lignes valides.");
        } catch (IOException e) {
            System.err.println("Erreur lecture CSV " + fileName + " : " + e.getMessage());
        }
    }

    public void startServer() {
        loadCSV("src/colis.csv", colisData);
        loadCSV("src/livreurs.csv", livreursData);
        loadCSV("src/positions.csv", positionsData);
        DBConfig config = new DBConfig("config.properties");
        db = new DBConnect(config);
        Connection conn = db.connect();
        colis = new Colis(conn);
        lv = new Livreur(conn);

        try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(ip))) {
            System.out.println("Serveur TCP démarré sur " + ip + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté : " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur : " + e.getMessage());
        }finally {
            db.closeConnection();
        }
    }

    private void terminaisonConnection(Socket socketClient) {
        try {
            if (socketClient != null && socketClient.isConnected()) {
                socketClient.close();
                System.out.println("Connexion client fermée proprement.");
            }
        }catch (IOException e) {
            System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
        }
    }

    private void printToClient(String message, BufferedWriter writer) {
        try {
            writer.write(message + "\n");
            writer.flush();
            System.out.println("Serveur → Client : " + message);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi du message : " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        boolean authenticated = false;
        String livreur = null;

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            clientSocket.setSoTimeout(30000);
            printToClient("Bienvenue ! Veuillez vous authentifier (ex: AUTH <idLivreur>)", out);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Message reçu du client : " + line);
                String response = "";

                String[] parts = line.trim().split(" ");
                String cmd = parts[0].toUpperCase();

                if (!authenticated && !cmd.equals("AUTH") && !cmd.equals("QUIT")) {
                    response = "Veuillez vous authentifier d'abord (AUTH <idLivreur>).";
                    printToClient(response, out);
                    continue;
                }

                if (cmd.equals("AUTH") && parts.length >= 2) {
                    livreur = parts[1];
                    if (lv.isValidLivreur(livreur)) {
                        authenticated = true;
                        response = "Livreur " + livreur + " "+ lv.getNameLivreur(livreur) + " authentifié.";
                    } else {
                        response = "Livreur inconnu : " + livreur;
                    }
                    printToClient(response, out);
                    continue;
                }

                switch (cmd) {
                    case "POS":
                        response = processPos(parts);
                        break;

                    case "STATE":
                        response = processState(line, parts);
                        break;

                    case "TAKE":
                        response = processTake(parts);
                        break;

                    case "GET":
                        response = processGet(parts);
                        break;

                    case "NOTIF":
                        response = processNotif(line);
                        break;

                    case "QUIT":
                        response = "Déconnexion du serveur. À bientôt " + livreur + " !";
                        printToClient(response, out);
                        return;

                    default:
                        response = "Commande inconnue : " + cmd;
                }

                printToClient(response, out);
            }
        }catch (SocketTimeoutException e) {
            System.out.println("Client inactif trop longtemps, déconnexion automatique." + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur client : " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            terminaisonConnection(clientSocket);
        }
    }

    private String processPos(String[] parts) {
        if (parts.length < 4)
            return "Commande POS incorrecte. Usage : POS <colisId> <lat> <lon>";

        String colisId = parts[1];
        String lat = parts[2];
        String lon = parts[3];

        // Mettre à jour la liste positionsData
        boolean found = false;
        for (String[] p : positionsData) {
            if (p[4].equals(colisId)) {
                p[1] = lat;
                p[2] = lon;
                p[3] = java.time.LocalDateTime.now().toString();
                found = true;
                break;
            }
        }
        if (!found) {
            positionsData.add(new String[]{"PO" + (positionsData.size() + 1), lat, lon, java.time.LocalDateTime.now().toString(), colisId});
        }

        return "Position mise à jour pour le colis " + colisId + " → (" + lat + ", " + lon + ")";
    }

    private String processState(String line, String[] parts) throws SQLException {
        if (parts.length < 3)
            return "Commande STATE incorrecte. Usage : STATE <colisId> <état>";

        String colisId = parts[1];
        String etat = line.substring(line.indexOf(parts[2]));

        if (!colis.isValidColis(colisId))
            return "Colis inconnu : " + colisId;

        boolean update = false;
        update = colis.updateEtatColis(colisId, etat);

        if (update) {
            return "État du colis " + colisId + " mis à jour : " + etat;
        }

        return "Impossible de mettre à jour l'état du colis " + colisId;
    }

    private String processTake(String[] parts) throws SQLException {
        if (parts.length < 2)
            return "Commande TAKE incorrecte. Usage : TAKE <colisId>";

        String colisId = parts[1];
        if (!colis.isValidColis(colisId))
            return "Colis inconnu : " + colisId;

        for (String[] c : colisData) {
            if (c[0].equalsIgnoreCase(colisId)) {
                c[5] = "pris en charge";
                break;
            }
        }

        return "Colis " + colisId + " pris en charge.";
    }

    private String processGet(String[] parts) {
        if (parts.length < 2)
            return "Commande GET incorrecte. Usage : GET <colisId>";

        String colisId = parts[1];

        try{
            return colis.getColis(colisId);
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur lors de la récupération du colis " + colisId;
        }
    }

    private String processNotif(String line) {
        String notif = line.substring(line.indexOf(" ") + 1);
        return "Notification reçue : " + notif;
    }
}