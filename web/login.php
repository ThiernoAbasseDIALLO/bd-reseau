<?php
declare(strict_types=1);

session_start();

header('Content-Type: application/json');

require_once "db.php";

try {
    $json_data = file_get_contents('php://input');
    $data = json_decode($json_data, true);

    if (!isset($data['login']) || !isset($data['password'])) {
        echo json_encode(['success' => false, 'error' => 'Veuillez remplir tous les champs.']);
        exit;
    }

    $login = trim($data['login']);
    $password = $data['password'];

    $db = new db();
    $pdo = $db->getPDO();

    $sql = "SELECT * FROM Utilisateur WHERE email = :login OR utilisateur_id = :login";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([':login' => $login]);

    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        if (password_verify($password, $user['mot_de_passe'])) {
            $_SESSION['login'] = $user['utilisateur_id'];

            echo json_encode([
                'success' => true,
                'message' => 'Connexion réussie ! Redirection...'
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'error' => 'Identifiant ou mot de passe incorrect.'
            ]);
        }
    } else {
        echo json_encode([
            'success' => false,
            'error' => 'Identifiant ou mot de passe incorrect.'
        ]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Erreur serveur : ' . $e->getMessage()
    ]);
}
?>