<?php

class db
{
    private PDO $pdo;
    public function __construct(String $configFile = 'secret/.info_connexion.php'){
        try {
            $config = require $configFile;

            $dsn = sprintf("psql:host=%s;port=%d;dbname=%s",
                $config['host'], $config['port'], $config['dbname']
            );

            $this->pdo = new PDO($dsn, $config['user'], $config['password'],
                [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES => false]);
//            echo("Connexion a la base de donnees reussi");
        }catch (PDOException $e){
            die("Erreur de connexion a la BDD : " . $e->getMessage());
        }
    }

    public function query(String $sql, array $params=[]):PDOStatement|bool{
        try {
            $stmt = $this->pdo->prepare($sql);
            $stmt->execute($params);
            return $stmt;
        }catch (PDOException $e){
            die("Erreur SQL : " . $e->getMessage());
        }
    }

    public function fetchAll(String $sql, array $params=[]):array{
        $stmt = $this->query($sql, $params);
        return $stmt ? $stmt->fetchAll() : [];
    }

    public function close() : void {
//        $this->pdo = null;
        unset($this->pdo);
    }
}