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

### Calendrier de publication (connaissance métier de Yohan, 2026-10-09, **à vérifier**)

- Semaine cinéma du vendredi au jeudi. Les films sortent le vendredi, sauf
  avant-premières le jeudi.
- L'horaire de la semaine entière serait publié d'un bloc, le vendredi à minuit.
- Incohérence à lever : une avant-première du jeudi doit être publiée avant ce
  jeudi, donc avant la publication « du vendredi » de sa semaine. Les préventes de
  gros films (billets vendus des semaines à l'avance) contredisent aussi une
  publication d'un seul bloc. Voir Q10.

### Versions linguistiques sur cineplex.com (observation de Yohan sur le site, 2026-10-09)

- Une même œuvre apparaît en **plusieurs fiches** dans les cinémas québécois,
  la version étant inscrite dans le titre : « Digger (Version française) » et
  « Digger » (VO anglaise).
- Une VO non anglaise porte une mention dans le titre et une ligne en dessous :
  « Pan's Labyrinth 20th Anniversary (Spanish w/ E.S.T.) », « Espagnol,
  sous-titré en anglais ». La version a donc deux dimensions : langue audio et
  langue des sous-titres.
- Constat sur le **site**, pas sur l'API : la représentation dans `/movies` et
  `/showtimes` (fiches distinctes ? champ dédié ?) reste à établir (Q13).
  → Confirmé par l'API, voir la section suivante.

### Constats de `GET /showtimes?language=fr&locationId=9195` (réponse réelle, Carrefour Angrignon, relevée par Yohan le 2026-10-09)

Un seul jour dans `dates[]` (2026-10-10), 11 fiches. Paramètre `date` envoyé ou
non : **à préciser** (si non envoyé, Q7 = un jour).

**Versions (lève Q13)**
- Chaque version linguistique est une **fiche distincte avec son propre `id`** :
  Verity 38401 (EN) / « Verity (Version française) » 61440 (FR) ; Digger 38459 /
  « Digger (version française) » 61298 ; Forgotten Island 38065 / « L'île de
  l'oubli » 61297. « Hantée » 61424 n'existe qu'en FR.
- La version est portée par la fiche : `languageCode` (`EN`/`FR`), `language`
  (libellé), `subtitleLanguage` (vide partout ici), `marketLanguageCode`.
- **Aucun champ ne relie une VF à sa VO.** Le titre ne le permet pas non plus :
  mention de version irrégulière (« Version française » / « version française »,
  slug `vf-verity-version-francaise` / `digger-version-francaise`), ou titre
  traduit sans mention (« L'île de l'oubli »). Les durées diffèrent parfois
  (Verity : 117 min en VO, 115 min en VF). Seule piste de rapprochement :
  TMDB, ou un champ de `/movies` encore inconnu (Q11).
- Les `id` des VF sont dans les 61xxx, ceux des VO dans les 37–38xxx :
  observation, **pas un contrat**.

**Heures (lève Q6 pour le fuseau de l'Est)**
- Chaque séance porte `showStartDateTime` (heure locale **sans** décalage,
  `2026-10-10T12:30:00`) **et** `showStartDateTimeUtc` (`2026-10-10T16:30:00Z`).
  Écart de 4 h, cohérent avec l'heure avancée de l'Est. L'instant à stocker
  (D-005) se lit directement dans `showStartDateTimeUtc`.
- Reste à confirmer sur un cinéma d'un autre fuseau (Vancouver, 1422) que le
  champ UTC est bien calculé avec le fuseau du cinéma.

**Identifiants de séance (éclaire Q2, Q14)**
- `vistaSessionId` entier ; `showtimeShareKey` = `v:<session>_t:<cinéma>_a:<areaCode>`.
  Cineplex lui-même combine séance + cinéma pour former une clé : indice que
  `vistaSessionId` n'est unique que **par cinéma** (Vista est un système par
  site). Hypothèse, **à vérifier** avec un second cinéma.
- Liens fournis par séance : `ticketingUrl` (sous `/prod/ticketing/`),
  `deeplinkUrl` (sous `/prod/cpx/theatrical/`, **le chemin protégé par la clé**
  selon la source tierce), `seatMapUrl` (www.cineplex.com). Lequel ouvre la
  réservation **sans clé**, depuis le navigateur d'un abonné : **à vérifier**
  dans une fenêtre privée.
- `seatsRemaining`, `isSoldOut`, `isInThePast` : volatils (D-008 : jamais
  affichés).

**Types de séance (éclaire Q15)**
- `experiences[]` groupe les séances par `experienceTypes` (tableau) ; observés :
  `Regular`, `3D`. Avec `language=fr`, le libellé reste `Regular` : **pas
  traduit**. Combinaisons (IMAX + 3D…) non observées ici.

**Autres champs utiles**
- Par fiche : `detailPageUrl`, affiches en trois tailles (`small/medium/large
  PosterImageUrl`), `runtimeInMinutes`, `genres` (traduits), `localRating`,
  `isEvent`, `presentationType` (« Film Presentation »).
- Aucun identifiant externe (IMDb, TMDB) ni date de sortie dans cette réponse.
- `startDate` : date locale sans décalage (`2026-10-10T00:00:00`).

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
| Q6 | `showStartDateTime` contient-il un décalage horaire ? | **En grande partie levée (2026-10-09)** : non, mais `showStartDateTimeUtc` donne l'instant. Reste : Bruno 04 sur Vancouver (1422) pour confirmer le champ UTC hors du fuseau de l'Est |
| Q7 | Combien de jours par appel `/showtimes` sans date ? | Bruno 05 |
| Q8 | Comment Landmark expose-t-il ses séances ? | Onglet Réseau sur landmarkcinemas.com |
| Q9 | `perfix` de la Cinémathèque stable dans le temps ? | Relever les liens deux jours de suite |
| Q10 | Quand la semaine S+1 apparaît-elle dans `/showtimes` ? D'un bloc ou film par film (préventes, avant-premières) ? | Bruno 05 une fois par jour du lundi au vendredi, noter la dernière date renvoyée et les films de S+1 déjà présents |
| Q11 | `/movies` donne-t-il un identifiant externe (IMDb, TMDB), une date de sortie, ou un champ reliant une VF à sa VO ? (absents de `/showtimes`) | Bruno 02 : lister les champs des fiches 38401 et 61440 (Verity VO / VF) |
| Q12 | Le titre québécois (ex. « Rapide et dangereux ») est-il celui que renvoie Cineplex avec `language=fr` ? Et TMDB en `fr-CA` ? | Bruno 03 sur un film à titre traduit ; même film sur TMDB en `fr-CA` et `fr-FR` |
| Q13 | Au Québec, VF et VO : deux fiches ou une ? | **Levée (2026-10-09)** : deux fiches, deux `id`, version dans `languageCode` / `subtitleLanguage`, aucun champ ne les relie |
| Q14 | Quelle URL ouvre la réservation d'une séance **sans clé**, depuis le navigateur d'un abonné ? | Fournies par séance (`ticketingUrl`, `deeplinkUrl`, `seatMapUrl`). Ouvrir chacune dans une fenêtre privée ; méfiance pour `deeplinkUrl` (chemin protégé par la clé) |
| Q15 | `experienceTypes` : liste des valeurs (Regular, 3D, VIP, D-BOX, IMAX, IMAX 70 mm… selon Yohan), combinables sur une même séance ? | Partiellement levée (2026-10-09) : `Regular`, `3D` observés, non traduits avec `language=fr`. Reste : un cinéma avec IMAX et VIP pour les combinaisons |
