public class Main {
    public static void main(String[] args) {
//        String ip = "192.168.1.167";
//        String ip = "10.77.54.63";
        String ip = "127.0.0.1";
//        String ip ="192.168.226.106";
        int port = 5000; // évite le port 80, réservé souvent par le système
        new Serveur(ip, port).startServer();
//        System.out.println("Hello, World!");//192.168.1.167
    }
}
// État du serveur pour chaque client