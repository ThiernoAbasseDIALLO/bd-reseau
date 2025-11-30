import socket

HOST = '127.0.0.1'
# HOST = '192.168.1.167'
PORT = 5000
TIMEOUT_SECONDS = 30


def main():
    try:
        with socket.create_connection((HOST, PORT), timeout=TIMEOUT_SECONDS) as sock:
            sock_file = sock.makefile("r", encoding="utf-8")

            print(f"Connecté au serveur {HOST} port {PORT}.")
            print("Tapez vos commandes, ou 'quit' pour quitter.\n")

            while True:
                try:
                    cmd = input("Client > ").strip()
                except EOFError:
                    break

                if not cmd:
                    continue

                try:
                    sock.sendall((cmd + "\n").encode('utf-8'))
                except socket.timeout:
                    print("Erreur : Timeout lors de l'envoi.")
                    break

                if cmd.lower() == 'quit':
                    try:
                        response = sock_file.readline().strip()
                        if response:
                            print(f"Réponse serveur : {response}")
                    except:
                        pass
                    print("Déconnexion.")
                    break

                try:
                    response = sock_file.readline()

                    if not response:
                        print("Le serveur a fermé la connexion.")
                        break

                    print(f"Réponse serveur : {response.strip()}")

                except socket.timeout:
                    print(f"Erreur : Pas de réponse après {TIMEOUT_SECONDS}s.")
                    break

    except ConnectionRefusedError:
        print("Impossible de se connecter au serveur. Vérifiez qu'il est lancé.")
    except socket.timeout:
        print(f"Erreur : Délai d'attente dépassé lors de la tentative de connexion initiale après {TIMEOUT_SECONDS} secondes.")
    except Exception as e:
        print(f"Erreur : {e}")

if __name__ == "__main__":
    main()
