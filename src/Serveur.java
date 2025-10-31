import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

public class Serveur {
    private String ip;
    private int port;

    private ArrayList<String[]> colisData = new ArrayList<>();
    private ArrayList<String[]> livreursData = new ArrayList<>();
    private ArrayList<String[]> positionsData = new ArrayList<>();
//    private static HashMap<String, String> colisEtat = new HashMap<>();
//    private static HashMap<String, Boolean> colisPris = new HashMap<>();
//    private static final HashMap<String, String> colisPosition = new HashMap<>();

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

        try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(ip))) {
            System.out.println("Serveur TCP démarré sur " + ip + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté : " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur : " + e.getMessage());
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
                    if (isValidLivreur(livreur)) {
                        authenticated = true;
                        response = "Livreur " + livreur + " authentifié.";
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
        } finally {
            terminaisonConnection(clientSocket);
        }
    }

    private boolean isValidLivreur(String livreurId) {
        for (String[] l : livreursData) {
            if (l.length > 0 && l[0].equalsIgnoreCase(livreurId)) return true;
        }
        return false;
    }

    private boolean isValidColis(String colisId) {
        for (String[] c : colisData) {
            if (c.length > 0 && c[0].equalsIgnoreCase(colisId)) return true;
        }
        return false;
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
            if (p[4].equals(colisId)) { // index 4 = colis_id dans ton CSV positions
                p[1] = lat;  // latitude
                p[2] = lon;  // longitude
                p[3] = java.time.LocalDateTime.now().toString(); // horodatage
                found = true;
                break;
            }
        }
        if (!found) {
            // Ajouter nouvelle position
            positionsData.add(new String[]{"PO" + (positionsData.size() + 1), lat, lon, java.time.LocalDateTime.now().toString(), colisId});
        }

        return "Position mise à jour pour le colis " + colisId + " → (" + lat + ", " + lon + ")";
    }

    private String processState(String line, String[] parts) {
        if (parts.length < 3)
            return "Commande STATE incorrecte. Usage : STATE <colisId> <état>";

        String colisId = parts[1];
        String etat = line.substring(line.indexOf(parts[2]));

        if (!isValidColis(colisId))
            return "Colis inconnu : " + colisId;

        for (String[] c : colisData) {
            if (c[0].equalsIgnoreCase(colisId)) {
                c[4] = etat;
                break;
            }
        }

        return "État du colis " + colisId + " mis à jour : " + etat;
    }

    private String processTake(String[] parts) {
        if (parts.length < 2)
            return "Commande TAKE incorrecte. Usage : TAKE <colisId>";

        String colisId = parts[1];
        if (!isValidColis(colisId))
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
        String etat = "inconnu";
        boolean pris = false;

        for (String[] c : colisData) {
            if (c[0].equalsIgnoreCase(colisId)) {
                etat = (c.length > 4 && c[4] != null && !c[4].isEmpty()) ? c[4] : "inconnu";
                pris = (c.length > 5 && "pris en charge".equalsIgnoreCase(c[5]));
                break;
            }
        }

        String pos = "non renseignée";
        for (String[] p : positionsData) {
            if (p[4].equalsIgnoreCase(colisId)) {
                pos = p[1] + "," + p[2];
                break;
            }
        }

        return "Colis " + colisId + " → état=" + etat + ", position=" + pos + ", pris=" + pris;
    }

    private String processNotif(String line) {
        String notif = line.substring(line.indexOf(" ") + 1);
        return "Notification reçue : " + notif;
    }

//    private static String processCommand(String line) {
//        if (line == null || line.trim().isEmpty()) return "Commande vide.";
//
//        String[] parts = line.trim().split(" ");
//        String command = parts[0].toUpperCase();
//
//        switch (command) {
//            case "AUTH":
//                if (parts.length >= 2)
//                    return "Livreur " + parts[1] + " authentifié.";
//                else
//                    return "Commande AUTH incorrecte. Usage : AUTH <nom>";
//
//            case "POS":
//                if (parts.length >= 4)
//                    return "Position du colis " + parts[1] + " reçue : lat=" + parts[2] + ", lon=" + parts[3];
//                else
//                    return "Commande POS incorrecte. Usage : POS <colis_id> <lat> <lon>";
//
//            case "TAKE":
//                if (parts.length >= 2) {
//                    colisPris.put(parts[1], true);
//                    return "Colis " + parts[1] + " pris en charge.";
//                } else
//                    return "Commande TAKE incorrecte. Usage : TAKE <colis_id>";
//
//            case "STATE":
//                if (parts.length >= 3) {
//                    String statut = line.substring(line.indexOf(parts[2]));
//                    colisEtat.put(parts[1], statut);
//                    return "Colis " + parts[1] + " mis à jour : " + statut;
//                } else
//                    return "Commande STATE incorrecte. Usage : STATE <colis_id> <état>";
//
//            case "GET":
//                if (parts.length >= 2) {
//                    String etat = colisEtat.getOrDefault(parts[1], "inconnu");
//                    return "Colis " + parts[1] + " état actuel : " + etat + ".";
//                } else
//                    return "Commande GET incorrecte. Usage : GET <colis_id>";
//
//            case "QUIT":
//                return "Déconnexion demandée.";
//
//            default:
//                return "Commande inconnue : " + command;
//        }
//    }
}