public class Main {
    public static void main(String[] args) {
        Serveur serveur = new Serveur();
        serveur.loadServerConfig("server_config.txt");
        serveur.startServer();
//        System.out.println("Hello, World!");//192.168.1.167
    }
}