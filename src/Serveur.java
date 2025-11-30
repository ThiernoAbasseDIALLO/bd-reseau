import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Serveur {
    private String ip;
    private int port;
    private Colis colis;
    private DBConnect db;
    private Livreur lv;
    private Position position;
    private final int MAX_CMD_LENGTH = 1024;
    private final int SOCKET_TIMEOUT = 30000;


    public Serveur(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void startServer() {
        DBConfig config = new DBConfig("config.properties");
        db = new DBConnect(config);
        Connection conn = db.connect();
        colis = new Colis(conn);
        lv = new Livreur(conn);

        System.out.println("\n--- Configuration du Serveur ---");
        System.out.println("  IP d'écoute : " + this.ip);
        System.out.println("  Port d'écoute : " + this.port);
        System.out.println("  Longueur max. commande (Sécurité) : " + MAX_CMD_LENGTH + " caractères.");
        System.out.println("  Timeout d'inactivité (Sécurité) : " + (SOCKET_TIMEOUT/1000) + " s.");
        System.out.println("--------------------------------\n");

        try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(ip))) {
            System.out.println("Serveur TCP démarré sur " + ip + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté : " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (BindException b){
            System.err.println("ERREUR GRAVE : Le port " + port + " est déjà utilisé ou inaccessible. (" + b.getMessage() +")");
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
            clientSocket.setSoTimeout(SOCKET_TIMEOUT);
//            printToClient("Bienvenue ! Veuillez vous authentifier (ex: AUTH <idLivreur>)", out);

            String line;
            while ((line = in.readLine()) != null) {

                if (line.length() > MAX_CMD_LENGTH) {
                    String errorMsg = "Erreur : Commande trop longue (" + line.length() + " > " + MAX_CMD_LENGTH + "). Déconnexion.";
                    System.err.println("Client " + clientSocket.getInetAddress() + " : " + errorMsg);
                    printToClient(errorMsg, out);
                    return;
                }

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

                    case "QUIT":
                        response = "Déconnexion du serveur. À bientôt " + lv.getNameLivreur(livreur) + " !";
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

    private String processPos(String[] parts) throws SQLException {
        if (parts.length < 4)
            return "Commande POS incorrecte. Usage : POS <colisId> <lat> <lon>";

        String colisId = parts[1];
        double lat;
        double lon;

        try {
            lat = Double.parseDouble(parts[2]);
            lon = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return "Latitude ou longitude invalide.";
        }

        if (!colis.isValidColis(colisId)) {
            return "Colis inconnu pour " + colisId;
        }

        boolean inserted = position.insertColisPosition(colisId, lat, lon);
        if (inserted) {
            return "Nouvelle position enregistrée pour " + colisId +
                    " → (" + lat + ", " + lon + ")";
        } else {
            return "Échec lors de l'insertion de la position pour " + colisId;
        }
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
            String message = "État du colis mis à jour : " + etat;
            colis.insertNotification(colisId, "STATE", message);
            return "État du colis " + colisId + " mis à jour : " + etat;
        }

        return "Impossible de mettre à jour l'état du colis " + colisId;
    }

    private String processTake(String[] parts) throws SQLException {
        if (parts.length < 3)
            return "Commande TAKE incorrecte. Usage : TAKE <colisId> <livreurId>";

        String colisId = parts[1];
        String livreurId = parts[2];

        if (!colis.isValidColis(colisId))
            return "Colis inconnu : " + colisId;

        if (!lv.isValidLivreur(livreurId))
            return "Livreur inconnu : " + livreurId;

        boolean success = colis.takeColis(colisId, livreurId);
        if (success) {
            String nomLivreur = lv.getNameLivreur(livreurId);
            String message = "Colis pris en charge par le livreur " + nomLivreur;
            colis.insertNotification(colisId, "Depart", message);
            return "Colis " + colisId + " pris en charge par le livreur " + nomLivreur + ".";
        } else {
            return "Échec lors de la prise en charge du colis " + colisId;
        }
    }

    private String processGet(String[] parts) throws SQLException {
        if (parts.length < 2)
            return "Commande GET incorrecte. Usage : GET <colisId>";

        String colisId = parts[1];
        return colis.getColis(colisId);
    }
}