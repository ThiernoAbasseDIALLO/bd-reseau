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
            <h2>Suivez vos expéditions en temps réel</h2>
            <p>
                La solution la plus fiable pour savoir exactement où se trouve votre commande.
                Consultez la position GPS, l'état d'avancement et l'identité de votre livreur.
            </p>
            <p>
                Identifiez le livreur en charge de votre secteur.
                Accédez à son nom et ses coordonnées pour une livraison sereine.
            </p>
            <p>
                Retrouvez toutes les étapes du parcours de votre commande :
                prise en charge, livraison en cours, et confirmation de réception.
            </p>
        </section>
    </main>

<?php
    require "footer.inc.php";
?>