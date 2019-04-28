# Guide d'utilisation des croissants

## Les croissants, c'est quoi ? 



<b>Principe</b><br />

C'est une application de gestion de croissants.<br />
<br />
<b>Fonctionnement</b><br /><br />
L'application tire au sort une personne de l'équipe tous les mercredis  matins. La personne sera prévenue par Slackbot. Elle pourra accepter ou refuser de livrer les croissants grâce à un joker.<br />
<br />
<br />
Il y aura possibilité de se piéger grâce a une command piège ( /c-trap ). Il faudra se connecter à cette adresse depuis le poste de la victime (il faut aussi que la victime soit encore connecté sur gmail pour que ça marche vu que ça s’appuie sur l'authentification Google).<br />
<br />
Chaque membre possède un coefficient qui représente la probabilité de se faire tirer au sort le jeudi (Quelqu'un coefficient 3 à trois fois plus de chance d'être tiré au sort que les autres). Le coefficient est augmenté par la page de piège et est réduit à chaque fois qu'on est tiré au sort.<br />
<br />
On ne peut être tiré au sort qu'une fois toutes les trois semaines au maximum.
<br />
Personne ne sait qui est sélectionné mis à part celle-ci et tant que elle n'a pas accepté d'apporter les croissants. Si elle refuse personne ne le sait. Et un autre tirage est effectué.<br />
<br />
<br />
Chaque semaine tous le monde voit son coefficient monter de 5 points.<br />
<br />
Adresse : https://7-croissants.public.prod.saagie.io/ <br />
    


## Les commandes

Voici la liste de toutes les commandes possibles : 

    /c-command : this list of all available commands
    /c-profile : to display your Croissants profile
    /c-selected : to display selected for the next friday
    /c-trap : to trap a unlock workstation
    /c-top : to see all coefficient
    /c-top-ten : to see top ten highest coefficient
    /c-propose-next : to propose the croissant for next friday
    /c-inactive-profile : to disable your Croissants profile during holiday
    /c-active-profile : to enable your Croissants profile after holiday
    /c-accept : to accept selection
    /c-decline : to decline selection (+10 coefficient)
    /c-propose dd/MM : to propose the croissant for the specified date (day/month)


## Le projet

Ce projet est open-source et disponible sur [Github saagie/croissants-kotlin](https://github.com/saagie/croissants-kotlin)