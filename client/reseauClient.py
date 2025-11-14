import socket

# HOST = '127.0.0.1'
HOST = '192.168.1.167'
PORT = 5000

def main():
    try:
        with socket.create_connection((HOST, PORT)) as sock:
            sock_file = sock.makefile("r", encoding="utf-8")

            print(f"Connecté au serveur {HOST} port {PORT}.")
            print("Tapez vos commandes, ou 'quit' pour quitter.\n")

            while True:
                response = sock_file.readline().strip()
                if not response:
                    print("Serveur déconnecté.")
                    break
                print(f"Réponse serveur : {response}")

                cmd = input("> ").strip()
                if cmd.lower() == 'quit':
                    print("Déconnexion du serveur.")
                    break

                # Envoi de la commande
                sock.sendall((cmd + "\n").encode('utf-8'))

                # Lecture d'une réponse complète (jusqu'à \n)
                response = ""
                while not response.endswith("\n"):
                    data = sock.recv(1024)
                    if not data:
                        print("Serveur déconnecté.")
                        return
                    response += data.decode('utf-8', errors='replace')

                # Envoi d'une commande
                sock.sendall((cmd + "\n").encode('utf-8'))

    except ConnectionRefusedError:
        print("Impossible de se connecter au serveur. Vérifiez qu'il est lancé.")
    except Exception as e:
        print(f"Erreur : {e}")

if __name__ == "__main__":
    main()
