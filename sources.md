# Sources de données

Chaque constat est daté et dit d'où il vient. « Source tierce » = dépôt public
`yinbri/cineplex-mcp` (capture du 19 au 31 juillet 2026), lu dans son code source
brut. Tout ce qui vient de là est **à vérifier** tant qu'une réponse réelle ne l'a
pas confirmé.

---

## Cineplex

### Authentification

- En-tête `Ocp-Apim-Subscription-Key` : clé d'abonnement Azure API Management.
  Valeur fixe, la même pour tous les visiteurs de cineplex.com. Sert aux quotas,
  pas à l'autorisation. (source tierce)
- Protège seulement `/prod/cpx/theatrical/`. Les plans de salle
  (`/prod/ticketing/.../seat-layout`, `.../seat-availability`) répondent sans clé.
  (source tierce)
- Le JavaScript du site contient plusieurs clés ; la bonne est celle envoyée sur les
  appels `theatrical`. (source tierce)

### Endpoints (source tierce, noms de paramètres **à vérifier** dans l'onglet Réseau)

- `GET /theatres?language=` : répertoire complet.
- `GET /movies?language=` : catalogue, réponse sous `items`.
- `GET /showtimes?language=&locationId=&date=` : toutes les séances d'un cinéma pour
  un jour. Réponse : `[ theatreId, dates[ startDate, movies[ id, experiences[
  experienceTypes[], sessions[ vistaSessionId, showStartDateTime, auditorium,
  seatsRemaining, isSoldOut, ... ] ] ] ] ]`.
- `GET /theatres/playingnearby` : ignore son paramètre `accuracyKm` (15 cinémas les
  plus proches, toujours).

### Constats de `GET /theatres?language=fr` (réponse réelle, 2026-09-29)

- 152 cinémas (7 dans `nearbyTheatres`, 145 dans `otherTheatres`, aucun en double).
  Même nombre qu'en juillet : répertoire stable.
- `theatreId` entier ; le premier chiffre suit la région (9xxx = Québec, 7xxx =
  Ontario…). Convention observée, **pas un contrat** : la province se lit dans
  `location.provinceCode`.
- `theatreUrl` est un slug de marque (`cinema-famous-players-carrefour-angrignon`) :
  inutilisable comme identifiant.
- `distanceToOriginInMeters` est présent sans coordonnées envoyées : origine déduite
  de l'appelant, probablement par IP (**à vérifier** depuis un autre réseau). La
  répartition entre groupes et l'ordre dépendent donc de l'appelant.
- Données sales : `postalCode` `"T5Y 0S5 "` (espace finale). L'adaptateur normalise.
- Noms en français pour les cinémas québécois, en anglais ailleurs avec
  `language=fr`.

## Landmark

Rien de reconnu. Deuxième exploitant au Canada, 36 cinémas, Ontario et Ouest.
Première étape : onglet Réseau sur landmarkcinemas.com (appels de séances,
authentification, format des identifiants et des heures).

## Cinémathèque québécoise

Constats de la page de programmation (2026-09-29) :

- Pas d'API connue : pages HTML. Chaque film a une page avec un slug
  (`/fr/cinema/furyo/`).
- Billetterie externe Omniticket ; chaque lien d'achat porte un `perfix` numérique
  (ex. `perfix=14361`) : identifiant de séance probable, stabilité **à vérifier**.
- Certaines entrées sont des programmes de plusieurs films (« 6 portraits
  néoréalistes »).
- Projections ponctuelles : « entrée/sortie d'affiche » n'a pas le même sens que
  chez Cineplex.
- Une grille PDF mensuelle est aussi publiée.
- À faire avant toute extraction : lire les conditions du site et `robots.txt`, et
  écrire à la Cinémathèque pour demander un flux de programmation.

---

## Questions ouvertes

| # | Question | Comment la lever |
|---|---|---|
| Q1 | ID de film stable, identique en `en` et `fr` ? | Bruno 02 → 03 |
| Q1b | Les noms de cinéma changent-ils avec `language` ? | Bruno 01 avec `language=en`, comparer le 9406 |
| Q2 | `vistaSessionId` stable ? Unique par cinéma ou global ? | Bruno 04, deux exécutions à quelques heures d'écart ; puis un second cinéma (9172) |
| Q3 | Quels champs de séance sont volatils ? | Bruno 04, sortie « Champs modifiés » |
| Q4 | `ETag` / `304` pris en charge ? | Bruno 06 juste après 04 |
| Q5 | Forme du 401 : clé absente et clé invalide se distinguent-elles ? | Bruno 07 → 08 |
| Q6 | `showStartDateTime` contient-il un décalage horaire ? | Bruno 04 sur un cinéma de Vancouver (1422), comparer avec cineplex.com |
| Q7 | Combien de jours par appel `/showtimes` sans date ? | Bruno 05 |
| Q8 | Comment Landmark expose-t-il ses séances ? | Onglet Réseau sur landmarkcinemas.com |
| Q9 | `perfix` de la Cinémathèque stable dans le temps ? | Relever les liens deux jours de suite |
