<?php
    declare(strict_types=1);
    if (session_status() === PHP_SESSION_NONE && isset($_COOKIE[session_name()])) {
        session_start();
    }

    $title = "Accueil";
    require "header.inc.php";
?>

    <main>
        <h1>Content de vous voir, bienvenue dans le site pour le suivi de livraison de votre colis !</h1>
        <section>
            <h2></h2>
            <p>Pour voir les informations du suivi de votre colis, Veuillez d'abord vous authentifier.</p>
        </section>
    </main>

<?php
    require "footer.inc.php";
?>