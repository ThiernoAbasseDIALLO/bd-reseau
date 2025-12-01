<?php
    declare(strict_types=1);
    session_start();
    if (!isset($_SESSION['login'])) {
        header('Location: index.php');
        exit;
    }

    require_once "db.php";
    require_once "Suivi.php";

    $dbconnect = new db();
    $manager = new Suivi($dbconnect);

    $id_client_connecte = $_SESSION['login'];
    $colis = $manager->getColisByClientID($id_client_connecte);

    $livreur = null;
    $position = null;
    $historique = [];
//    var_dump($colis);
    if ($colis) {
        $colisID = $colis['colis_id'];

        $livreur = $manager->getLivreurByColisID($colisID);

        $position = $manager->getLastPosition($colisID);

        $historique = $manager->getHistorique($colisID);
    }

//    echo '<pre>';
//    echo "--- DEBUG POSITION ---\n";
//    var_dump($position);
//
//    echo "--- DEBUG HISTORIQUE ---\n";
//    var_dump($livreur);
//    echo '</pre>';

    $title = "Suivi de mon colis";
    require "header.inc.php";
?>
<main class="container">

    <h1 class="page-title">Suivi de votre Commande</h1>

    <?php if ($colis): ?>

        <section>
            <h2>Informations Générales</h2>
            <div>
                <span>Numéro de suivi :</span>
                <strong><?php echo htmlspecialchars($colis['numero_suivi'] ?? 'N/A'); ?></strong>
            </div>

            <div>
                <span>Poids :</span>
                <strong><?php echo htmlspecialchars((string)$colis['poids']); ?> kg</strong>
            </div>
            <div>
                <span>Taille :</span>
                <strong><?php echo htmlspecialchars($colis['taille']); ?></strong>
            </div>
        </section>

        <section>
            <h2>Statut Actuel</h2>
            <p>
                <?php echo htmlspecialchars(strtoupper($colis['etat'] ?: 'EN ATTENTE DE PRISE EN CHARGE')); ?>
            </p>
            <p>
                Dernière mise à jour : <?php echo htmlspecialchars($colis['last_update'] ?? date('Y-m-d H:i:s')); ?>
            </p>
        </section>

        <section>
            <h2>Position en Temps Réel</h2>
            <?php if ($position): ?>
                <div>
                    <p>Latitude : <strong><?php echo htmlspecialchars((string)$position['latitude']); ?></strong></p>
                    <p>Longitude : <strong><?php echo htmlspecialchars((string)$position['longitude']); ?></strong></p>
                    <p>Relevé le : <?php echo $position['horodatage']; ?></p>
                </div>
                <a href="https://www.google.com/maps/search/?api=1&query=<?php echo $position['latitude']; ?>,<?php echo $position['longitude']; ?>"
                   target="_blank" class="btn-map">
                    Voir sur la carte
                </a>
            <?php else: ?>
                <p>
                    Position GPS non disponible pour le moment.
                </p>
            <?php endif; ?>
        </section>

        <section>
            <h2>Détails du Transporteur</h2>
            <?php if ($livreur): ?>
                <p>
                    <strong><?php echo htmlspecialchars($livreur['prenom'] . ' ' . $livreur['nom']); ?></strong>
                </p>
                <?php if (!empty($livreur['telephone'])): ?>
                    <p><?php echo htmlspecialchars($livreur['telephone']); ?></p>
                <?php endif; ?>
                <p>
                    Zone : <?php echo htmlspecialchars($livreur['zone_livraison'] ?? 'Non définie'); ?>
                </p>
            <?php else: ?>
                <em>En attente d'affectation d'un livreur...</em>
            <?php endif; ?>
        </section>

        <section>
            <h2>Historique du parcours</h2>
            <?php if (count($historique) > 0): ?>
                <dl>
                    <?php foreach ($historique as $event): ?>
                        <dt>
                            <?php echo htmlspecialchars($event['date_action']); ?>
                        </dt>
                        <dd>
                            <strong><?php echo htmlspecialchars($event['nouvel_etat']); ?></strong>
                            <?php if (!empty($event['commentaire'])): ?>
                                <p><?php echo htmlspecialchars($event['commentaire']); ?></p>
                            <?php endif; ?>
                        </dd>
                    <?php endforeach; ?>
                </dl>
            <?php else: ?>
                <p>Aucun historique disponible.</p>
            <?php endif; ?>
        </section>

    <?php else: ?>
        <section class="alert" style="background-color: #f8d7da; color: #721c24; padding: 15px; border-radius: 5px; text-align: center;">
            Aucun colis en cours trouvé pour le client #<?php echo htmlspecialchars($id_client_connecte); ?>.
        </section>
    <?php endif; ?>

</main>
<?php
    require "footer.inc.php";
?>
