<?php
require_once "db.php";

try {
    $db = new db();
    $pdo = $db->getPDO();

    $utilisateurs = [
        [
            'id' => 'CL0001',
            'nom' => 'Martin',
            'prenom' => 'Paul',
            'email' => 'paul.martin@gmail.com',
            'telephone' => '0612345678',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'CL0002',
            'nom' => 'Durand',
            'prenom' => 'Alice',
            'email' => 'alice.durand@gmail.com',
            'telephone' => '0623456789',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'CL0003',
            'nom' => 'Dupont',
            'prenom' => 'Jean',
            'email' => 'jean.dupont@gmail.com',
            'telephone' => '0634567890',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'CL0004',
            'nom' => 'Petit',
            'prenom' => 'Marie',
            'email' => 'marie.petit@gmail.com',
            'telephone' => '0645678901',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'CL0005',
            'nom' => 'Lemoine',
            'prenom' => 'Lucas',
            'email' => 'lucas.lemoine@gmail.com',
            'telephone' => '0656789012',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'LV0001',
            'nom' => 'Bernard',
            'prenom' => 'Sophie',
            'email' => 'sophie.bernard@gmail.com',
            'telephone' => '0667890123',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'LV0002',
            'nom' => 'Nguyen',
            'prenom' => 'Thierry',
            'email' => 'thierry.nguyen@gmail.com',
            'telephone' => '0678901234',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'LV0003',
            'nom' => 'Sow',
            'prenom' => 'Mamadou',
            'email' => 'smamdou@gmail.com',
            'telephone' => '0667890123',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'LV0004',
            'nom' => 'Bernard',
            'prenom' => 'Lucas',
            'email' => 'lbernard@gmail.com',
            'telephone' => '0678901234',
            'mdp_clair' => 'A123456*'
        ],
        [
            'id' => 'LV0005',
            'nom' => 'Leroy',
            'prenom' => 'Julien',
            'email' => 'julien.leroy@gmail.com',
            'telephone' => '0645678901',
            'mdp_clair' => 'A123456*'
        ],
    ];

    foreach ($utilisateurs as $u) {
        $hash = password_hash($u['mdp_clair'], PASSWORD_DEFAULT);

        $sqlUser = "INSERT INTO Utilisateur (utilisateur_id, nom, prenom, email, telephone, mot_de_passe) 
                    VALUES (:id, :nom, :prenom, :email, :telephone, :mdp)";

        $stmt = $pdo->prepare($sqlUser);
        $stmt->execute([
            ':id' => $u['id'],
            ':nom' => $u['nom'],
            ':prenom' => $u['prenom'],
            ':email' => $u['email'],
            ':telephone' => $u['telephone'],
            ':mdp' => $hash
        ]);
    }

    echo "Terminé ! Les utilisateurs sont créés avec des mots de passe sécurisés.";

} catch (PDOException $e) {
    die("Erreur SQL : " . $e->getMessage());
}
?>