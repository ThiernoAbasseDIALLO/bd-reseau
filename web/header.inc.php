<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta name="author" content="Thierno Abasse DIALLO"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="styles.css"/>
    <title><?=$title?></title>
    <link rel="shortcut icon" type="image/png" href="images/cookies.jpg"/>
</head>
<body>
    <header class="navbar custom-header navbar-expand-lg">
            <figure class="mb-0 ml-2 d-flex align-items-center">
                <a class="navbar-brand" href="index.php">
                    <img src="logo.jpg" alt="Logo du site"/>
                    <span class="ms-2">Suivi de livraison</span>
                </a>
            </figure>

            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarResponsive"
                    aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="nav-mobile collapse navbar-collapse justify-content-between align-items-center" id="navbarResponsive">
                <nav class="my-3 my-lg-0">
                    <ul class="navbar-nav flex-row flex-lg-row gap-3">
                        <?php if (isset($_SESSION['login'])): ?>
                            <li>
                                <a class="nav-link" href="consultation.php">Suivi du colis</a>
                            </li>
                        <?php endif; ?>
                    </ul>
                </nav>

                <div class="right my-3 my-lg-0">
                    <a href="cnx.php" class="nav-link btn1">Se connecter</a>
                </div>
            </div>
        <?php if (isset($_SESSION['login'])): ?>
            <script>
                const btn1 = document.querySelector('.btn1');
                if (btn1) {
                    btn1.innerText = "Déconnexion";
                    btn1.setAttribute("href", "logout.php");
                    btn1.addEventListener("click", function (e) {
                        if (!confirm("Voulez-vous vous déconnecter ?")) {
                            e.preventDefault();
                        }
                    });
                }
            </script>
        <?php endif; ?>
    </header>