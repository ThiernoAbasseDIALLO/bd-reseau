<?php

class Suivi
{
    private $pdo;

    public function __construct(db $dbcon){
        $this->pdo = $dbcon->getPDO();
    }

    public function getColisByClientID(string $clientID):array
    {
        $sql = "SELECT * FROM colis WHERE client_id = ?";
        $stmt = $this->pdo->prepare($sql);
        $stmt->execute([$clientID]);
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    public function getLivreurByColisID(string $colisID): ?array
    {
        $sql = "SELECT U.nom, U.prenom, U.telephone, L.zone_livraison
                FROM Livraison Liv
                JOIN Livreur L ON Liv.livreur_id = L.livreur_id
                JOIN Utilisateur U ON L.livreur_id = U.utilisateur_id
                WHERE Liv.colis_id = ? 
                LIMIT 1";

        $stmt = $this->pdo->prepare($sql);
        $stmt->execute([$colisID]);
        return $stmt->fetch(PDO::FETCH_ASSOC) ?: null;
    }

    public function getLastPosition(string $colisID): ?array {
        $sql = "SELECT * FROM Position WHERE colis_id = ? ORDER BY horodatage DESC LIMIT 1";
        $stmt = $this->pdo->prepare($sql);
        $stmt->execute([$colisID]);
        return $stmt->fetch(PDO::FETCH_ASSOC) ?: null;
    }
    public function getHistorique(string $colisID): array
    {
        $sql = "SELECT * FROM Historique
                WHERE colis_id = ? 
                ORDER BY date_action DESC";

        $stmt = $this->pdo->prepare($sql);
        $stmt->execute([$colisID]);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}