<?php
    declare(strict_types=1);
    session_start();
    $title = "Se connecter";
    require "header.inc.php";
?>
    <main>
        <h1>Se connecter à votre compte</h1>
        <form method="post" id="loginForm">
            <p class="legend">Se connecter</p>
            <label for="login"> Entrez votre login ou email</label>
            <div class="input-icon">
                <i class="fa-solid fa-id-card"></i>
                <input type="text" id="login" name="login" placeholder="Tapez ici votre login ou email"  required="required"/>
            </div>
            <label for="password">Entrez votre mot de passe</label>
            <div class="input-icon">
                <i class="fa-solid fa-lock" ></i>
                <input type="password" id="password" name="password" placeholder="Tapez ici votre mot de passe" required="required"/>
                <i class="fa-solid fa-eye" id="icon-eye"></i>
            </div>
            <input type="submit" value="Connexion"/>
            <span id="message"></span>
        </form>
    </main>

    <script>
        document.getElementById('loginForm').addEventListener('submit', function(event){
            event.preventDefault();
            registerUser();
        });
        async function registerUser() {
            const login = document.getElementById('login').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('login.php', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({login, password})
                });

                const data = await response.json();
                const messageElem = document.getElementById('message');
                if (data.success) {
                    messageElem.style.color = 'green';
                    messageElem.textContent = data.message;

                    setTimeout(() => {
                        window.location.href = "../index.php";
                    }, 1500);
                } else {
                    messageElem.style.color = 'red';
                    messageElem.textContent = data.error || 'Erreur inconnue';
                }
            }catch (err){
                console.log(err);
            }
        }
    </script>

    <script async src="https://www.google.com/recaptcha/api.js" defer></script>

<?php
    require "footer.inc.php";
?>