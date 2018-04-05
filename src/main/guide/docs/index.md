# Guide d'utilisation d'Aston Parking
TEST
## Aston Parking, c'est quoi ? 

Aston Parking est un outils de gestion des places de parking utilisé en interne chez [Saagie](http://saagie.com). Elle permet l'attribution automatique et équitable entre tous les participants, ainsi qu'un système d'échange. 

Actuellement, toutes les actions se font sur Slack. Un site web devrait voir le jour prochainement en plus. Un bot "AstonParkingBot" permet de gérer toutes les demandes des participants (toutes ses réponses se font de manière privée afin de ne pas poluer le channel).

## S'enregister

Prémière étape obligatoire pour être enregisté dans l'application : l'enregistrement.
Celui-ci a lieu en deux étape : l'inscription et la confirmation.

Connectez-vous au channel #aston-parking sur Slack et tapez la commande : `/ap-register` (toutes les commandes AstonParking commencent par /ap- afin d'être facilement identifiables). Le bot vous demandera alors de confirmer votre inscription en vous connectant au website (le lien est indiqué dans sa réponse).

Connectez-vous sur le site en utilisant la connection OAuth Slack (cela permet de valider votre inscription).

Vous êtes maintenant enregistré à l'application. Vous pouvez vérifier l'état de votre enregistrement en tapant `/ap-profile`

## Les attributions

Le fonctionnement d'Aston Parking est simple : 

Tous les lundis (à 11H), un tirage au sort est effectué. Toutes les places libres sont affectées. Chaque utilisateur possède un compteur d'attribution (chaque jour où une place est attribuée). La liste des utilisateurs est triée par nombre d'attribution afin d'être équitable dans l'attribution des places.

La liste des attribution est affichée en public sur le channel Slack après le tirage au sort. 

Les personnes sélectionnées ont alors, jusqu'au dimanche pour valider `/ap-accept` ou refuser `/ap-decline` les propositions (on accepte ou on refuse tout, on ne peut pas le faire partiellement pour le moment). De plus, les places sont attribuées à la journée (à voir dans les versions suivantes pour mettre en place les demi-journées).

Dimanche matin, les places non validées sont marquées comme libres et peuvent être réservée par n'importe qui.

Lorsque l'on refuse la proposition, un nouveau tirage au sort pour cette place est faite et une nouvelle proposition est faite à quelqu'un d'autre.

Lorsqu'on accepte les places, son compteur d'attribution augmente alors de 5 (nombre de jours). Il suffit de libérer la place pour les jours où l'on en a pas besoin `/ap-release dd/MM` (dd/MM représente le jour/mois) pour que la place soit marquée comme libre et qu'elle puisse être récupérée par quelqu'un d'autre `/ap-pick dd/MM`. La personne qui a libéré la place voit son compteur d'attribution diminuer de 1 et celui qui a pris la place voit le sien augmenter de 1.

Lorsqu'une place est libérée, un message en public est publiée sur le channel Slack.

## Les commandes

Voici la liste de toutes les commandes possibles : 

* `/ap-command` - Affiche la liste de toutes les commandes.
* `/ap-register` - Permet l'enregistrement à l'application.
* `/ap-unregister` - Lance la suppression du compte.
* `/ap-profile` - Affiche le profile et ses infos.
* `/ap-attribution` - Affiche la liste des propositions d'affection pour la semaine suivante et les attribution pour la semaine en cours.
* `/ap-planning` - Affiche son planning pour la semaine en cours et la semaine suivante.
* `/ap-today` - Affiche les attributions du jour.
* `/ap-inactive-profile` - Désactive le profile (utile pour ne pas être tiré au sort pendant les vacances par exemple).
* `/ap-active-profile` - Active le profile (au retour des vacances par exemple) et permet d'être, à nouveau, tiré au sort.
* `/ap-accept` - Permet d'accepter les propositions d'attribution qu'on a reçues.
* `/ap-decline` - Permet de refuser les propositions d'attribution qu'on a reçues.
* `/ap-release dd/MM` - Permet de libérer la place attribuée pour le jour indiqué.
* `/ap-pick dd/MM` - Permet de s'approprier une place libre pour le jour indiqué.
* `/ap-pick-today` - Permet de s'approprier une place libre aujourd'hui (si disponible).
* `/ap-request dd/MM` - Permet de réserver une place pour le jour "dd/MM" dès qu'une place se libère (attention : compte comme double en cas d'attribution)
* `/ap-request-cancel` - Permet d'annuler la requête en cours.


## Le projet

Ce projet est open-source et disponible sur [Github saagie/aston-parking](https://github.com/saagie/aston-parking)