<?php
    declare(strict_types=1);
    session_start();
    $_SESSION = [];

    if (ini_get("session.use_cookies")) {
        $params = session_get_cookie_params();
        setcookie(
            session_name(),
            '',
            time() - 420000000,
            $params['path'],
            $params['domain'],
            $params['secure'],
            $params['httponly']
        );
    }

    session_destroy();
?>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8"/>
    <title>Déconnexion</title>
    <meta http-equiv="refresh" content="2; url=index.php"/>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 100px;
        }
    </style>
</head>
<body>
    <h2>Vous allez être déconnecté...</h2>
    <p>Redirection vers la page d'accueil dans 2 secondes.</p>
</body>
</html>
