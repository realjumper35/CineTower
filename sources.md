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
  TMDB (`/movies` n'en offre pas, Q11 levée).
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

### Constats de `GET /movies` (réponses réelles relevées par Yohan, 2026-10-09)

Trois réponses : 268 fiches (libellés anglais, `detailPageUrl` en `/Movie/`),
395 fiches (libellés français, `/Film/`), 268 fiches (libellés anglais). Ordre
**supposé** : `language=en`, `language=fr`, requête « 04 tout » sans paramètre
`language`. **À vérifier** : la requête 04 n'est pas encore dans `bruno/`. Si
l'ordre est bon, sans paramètre = `en`.

Comparaisons faites **à la lecture, pas par script** : à refaire sur les
réponses enregistrées.

**Langue de la requête (lève Q1)**
- Même `id` → même `name` en `en` et en `fr` (Verity 38401, Digger 38459,
  Other Mommy 38158, Street Fighter 60523). `language` ne traduit que des
  libellés : `language`, `subtitleLanguage`, `genres`, et le chemin de
  `detailPageUrl` (`/Movie/` ↔ `/Film/`). Les `ratings` (avertissements,
  descriptions) restent en anglais.
- `fr` = les fiches de `en` **plus** les fiches du marché francophone (Autre
  Maman 61857, Verity VF 61440, Hantée 61424, Le jugement social 61788…).
  Aucune fiche de `en` absente de `fr` repérée. `fr` est donc le sur-ensemble.
- Les fiches FR portent des titres traduits : « Avengers: Docteur Doom »,
  « Hunger Games: Lever de soleil sur la moisson », « Raison et sentiments ».

**Champs d'une fiche (lève Q11, par la négative)**
- `id`, `name`, `releaseDate`, `runtimeInMinutes`, `filmUrl`, affiches en trois
  tailles (`null` si `hasPosterImage` est faux), `brightcoveVideoId`, `language`,
  `subtitleLanguage`, `marketLanguageCode`, `genres`, `ratings` par province,
  `distributor`, `detailPageUrl`, `isNowPlaying`, `isComingSoon`, `isRelevant`,
  `hasShowtimes`, `isEvent`, `isEarlyAccess`.
- **Aucun identifiant externe** (IMDb, TMDB). **Aucun champ reliant une VF à
  sa VO.**
- Aucun indice fiable pour les relier : durée différente (Verity 117/115,
  Heart of the Beast 101/104), date de sortie différente (I Play Rocky 11-13 /
  Moi, Rocky 11-20 ; CoComelon 02-26 / 02-19), distributeur différent
  (Whalefall / Au fond de la Baleine). Seul `brightcoveVideoId` est parfois
  partagé (Macbeth EN/FR). Confirme D-012.
- Les drapeaux (`isNowPlaying`, `hasShowtimes`…) sont nationaux, pas par
  cinéma : `/movies` ne dit pas ce qui joue dans un cinéma donné.

**Version linguistique : données peu fiables**
- `language` vide (Dear You, teochew ; Agbara Nla, yoruba ; Always Yours Never
  Mine : « Tagalog » en `en`, vide en `fr`, donc libellé traduit par une table
  incomplète).
- `language` faux : Fjord (Multilingual w/e.s.t.) → `English`/`English` ;
  Andhar (Bengali w/e.s.t.) et Ka Kaa Ki Kii (Nepalese w/e.s.t.) → `English`,
  sans sous-titres ; « Cheval sauvage numéro Neuf » (VF probable de Wild Horse
  Nine) → `English`, marché `EN`.
- Libellés incohérents : `Chinese` ou `Mandarin` pour des titres « Mandarin » ;
  le doublage est dans le libellé (`Spanish (Dubbed)`).
- `marketLanguageCode` incohérent : Haunted (French w/e.s.t.) `EN`, Jim Queen
  (French w/e.s.t.) `FR` ; Concert de Noël d'André Rieu, français sous-titré
  français, `EN`.
- La mention de version dans le titre est aussi irrégulière : « (French
  w/e.s.t.) », « w.e.s.t », « (French w/est) », « (Version française) »,
  « (anglais s.t.f.) », « Italien avec s.-t.fr. », « (Dubbed in Spanish) ».

**Versions originales et sous-titrées**
- Une même œuvre peut avoir jusqu'à quatre fiches dans `fr`. Godzilla Minus
  Zero : 60710 « (Japanese w/e.s.t.) » (japonais/anglais, `EN`), 62199
  « (Japonaise avec s.t.f.) » (japonais/français, `FR`), 62198 « (Version
  française) » (français, `FR`), 62225 « (Japonaise avec s.t.f.) - L'Expérience
  IMAX (version française) » (japonais/français). Dans le titre, « version
  française » ne désigne donc pas toujours un doublage.
- VO anglaise sous-titrée français : « Cours Terry Cours (anglais s.t.f.) »
  61918 / Run Terry Run 61803 ; « Les Diables (anglais s.t.f.) » / Ken
  Russell's The Devils ; « Fjord (anglais s.t.f.) ».
- VO française sous-titrée anglais : « Haunted (French w/e.s.t.) » 61817 /
  Hantée 61424 ; Pauline Julien (deux fiches) ; « Someone's daughter » / « Sans
  témoin ».
- Opéra : une fiche par langue de sous-titres et par reprise (Macbeth 61726
  `w/e.s.t.`, 61725 `s.-t.fr.`, 61727 `ENCORE`, 61728 `REDIFFUSION`).
- Plusieurs langues : « (Mandarin w/Chinese and e.s.t.) » (deux sous-titres,
  un seul dans `subtitleLanguage`), « Paradis (Français et anglais s.t.f.) »
  (deux langues audio, une seule dans `language`).
- Doublage : rien ne distingue un français doublé (Godzilla VF) d'un français
  d'origine (Hantée) ; seul `Spanish (Dubbed)` le dit, dans le libellé.
- Le format peut aussi être une fiche : « … - The IMAX Experience® » (Queen
  Budapest 62097, Godzilla 62225), pas seulement un `experienceTypes` de
  séance.
- `subtitleLanguage` vide = pas de sous-titres dans tous les cas lus ;
  `language` vide = langue inconnue.

**Autres pièges**
- `runtimeInMinutes` sentinelles : 998, 999, 1000 = durée inconnue (la plus
  longue réelle observée : 365, Parsifal).
- `releaseDate` : date sans heure. Une reprise garde l'ancienne date (E.T.
  2019, Back To The Future 2020 ; et « 40th Anniversary » est une fiche à part,
  2025). Ni indicateur de nouveauté, ni année d'origine fiable pour TMDB.
- Fiches de test et de réservation dans le catalogue : « WEAPONS - DR »
  (`test-film-dr`), « UNTITLED PARAMOUNT 06/25/27 », « WB Family Event Film
  (12/18/26) » ; toutes `isRelevant: false`, `hasShowtimes: false`.
- `isEvent` : opéras du Met, concerts, projections uniques. Chaque version
  (`w/e.s.t.` / `s.-t.fr.`) et chaque reprise (`ENCORE` / `REDIFFUSION`) est
  une fiche distincte.
- Texte sale à la source : « SÃO PAULO » (double encodage UTF-8), espaces
  finales dans `distributor`.

**Conclusion pour l'adaptateur** : tout ce qu'il faut au diff et au rendu
(id, titre, version, affiche, durée, `isEvent`, liens) est déjà dans
`/showtimes`. `/movies` n'ajoute que `releaseDate`, `distributor` et les
classements par province, dont l'étape 1 n'a pas besoin.

## Landmark

### Reconnaissance du 2026-10-09 (navigateur, page réelle de Landmark Orleans)

Faite par Claude à la demande de Yohan, empreinte minimale : la page d'accueil,
`/showtimes/`, `/showtimes/orleans`, puis `GetCinemaInfo` et `robots.txt`.

**Accès**
- 45 cinémas selon `/cinemas/22` (AB, BC, MB, ON, SK, YT ; aucun au Québec),
  chacun avec une page `/showtimes/<slug>` (`/showtimes/orleans`).
- Au chargement, les séances arrivent intégrées dans le HTML, dans une
  variable JavaScript `pc.showtimesdata` (environ 755 ko pour Orleans).
- **Il existe aussi une API JSON** (plateforme Peach Digital), lue dans le
  code source du site (`/bundles/scripts`) puis appelée une fois depuis le
  navigateur :
  `GET https://movieapi.landmarkcinemas.com/movies/22/193?expandGenres=true&splitByAttributes=true&expandSessions=true`
  (`22` = `pc.circuit`, la chaîne ; `193` = Orleans). 200, JSON, 60 fiches,
  même structure que `pc.showtimesdata`, avec `Genres`. `Trailer`,
  `ConsumerAdvice` et `Notes` sont présents quand la fiche en a (réponse
  Bruno du 2026-10-09 ; le relevé navigateur disait le contraire, il était
  faux). Aucune clé. Environ 42 ko compressé.
- **`curl` est refusé, Bruno passe.** `curl` sans en-têtes particuliers sur
  l'API et sur la page HTML → `403 Access Denied` servi par Akamai
  (`errors.edgesuite.net`), le 2026-10-09. Le même jour, Bruno (requêtes 01
  et 02, sans en-tête ajouté) obtient `200` sur `/cinemas/22` et
  `/movies/22/193` avec `User-Agent: bruno-runtime/4.2.1` (relevé par Yohan
  dans la chronologie Bruno) : un agent qui se déclare franchement comme
  outil, pas comme navigateur. Le blocage ne vise donc pas tout client
  automatisé, mais une signature précise. Hypothèse la plus probable :
  l'agent `curl/…` figure dans une liste d'outils de moissonnage connus.
  Autre cause possible : l'empreinte TLS du client.
- **Le client HTTP du JDK passe aussi** : `HttpClient` depuis `jshell`, agent
  `CinemaTower/0.1 (projet etudiant)`, `GET /cinemas/22` → 200 (Yohan,
  2026-10-09). La pile réseau et l'agent du futur collecteur sont acceptés.
  Imiter un navigateur reste **exclu**. Voir Q16.
- `GET /umbraco/api/baseapi/GetCinemaInfo?cinemaId=193` : nom, adresse,
  province (`StateCode`), latitude, longitude. **Aucun fuseau horaire.**
- `robots.txt` : `Disallow:` vide, tout est permis. Conditions d'utilisation :
  **à lire**.
- Le site annonce que les séances sont mises en ligne « chaque mercredi matin »
  pour la suite (phrase tronquée dans le relevé, **à relire**). Cineplex
  publierait le vendredi (Q10).

**Structure de `pc.showtimesdata.nowbooking['0']`** (60 fiches, 488 séances à
Orleans)
- Fiche : `FilmId` (entier, ex. Verity 126449), `Title`, `Cert`, `Img`,
  `ReleaseDate`, `RunTime` (chaîne), `Synopsis`, `Cast`, `Director`,
  `FriendlyName` (slug), `Experiences[]`, `Sessions[]`, `IsNowShowing`,
  `IsComingSoon`, `HasSessions`, `Trailer`, `ConsumerAdvice`, `Notes`.
  Aucun identifiant externe.
- `Sessions[]` : un élément par jour (`NewDate` `2026-10-09`), groupé par
  combinaison d'expériences, puis `Times[]`.
- Séance : `StartTime` **`"7:10 PM"`, heure locale sur 12 h, sans date, sans
  décalage, sans UTC** ; `Scheduleid` (`11621578`) ; `ExternalSessionId`
  (`219794`) ; `CinemaId` ; `Screen` (`"EXTRA (6)"`) ; `SoldOut` ;
  `SessionExpired` ; `Experience[]`.
- Les 488 `Scheduleid` et les 488 `ExternalSessionId` sont tous distincts dans
  ce cinéma. Grandeurs différentes : `Scheduleid` ressemble à un compteur
  global, `ExternalSessionId` à un compteur Vista par site. **À vérifier** sur
  un second cinéma.
- Lien de réservation construit par la page, pas fourni :
  `/booking?cinemaId=193&filmId=…&externalSessionId=…&sessionId=<Scheduleid>`.
  Fonctionne-t-il ouvert directement depuis un courriel : **à vérifier**.
- Horizon : séances du 2026-10-09 au 2027-06-05 (opéras du Met) ; préventes
  d'Avengers: Doomsday jusqu'au 2027-01-07. Toutes les semaines publiées
  arrivent dans une seule page.
- Aucune séance après minuit observée ; `Date` = `ActualDate` partout.

**Versions et formats**
- Pas de champ de langue. La version est **dans le titre** : « Godzilla Minus
  Zero (Japanese w EST) », « Paw Patrol: The Dino Movie (French Version) »
  (fiche distincte, 126994). Les sous-titres anglais sont aussi une
  expérience de séance (`684 EnglishSub`), présente sur les 17 fiches « w EST »
  et sur les BTS.
- Le format est souvent une fiche à part, comme chez Cineplex : « Street
  Fighter » 125384 / « Street Fighter (ScreenX) » 127049 ; « Avengers:
  Doomsday (Infinity Vision) » / « (ScreenX) (Infinity Vision) ». Il existe
  aussi en expérience de séance (`743 ScreenX`, `752 Infinity V`, `506 3D`).
- Événements : pas de drapeau `isEvent` sur la fiche. Le signal est une
  expérience de séance (`740 Special Event`, `714 Fan Event`, `705 Early
  Access`, `745 Big Screen Rewind`), ou une fiche dédiée (« Street Fighter:
  Bonus Round Fan Event », « Monster Mia - Early Access Screening »).
- Expériences observées à Orleans : 2D, 3D, EXTRA, ScreenX, Infinity Vision,
  Recliner, Premiere Seats, Shout Out, Closed Caption, Descriptive Video
  Service, English Subtitled, Special Event, Fan Event, Early Access, Big
  Screen Rewind. Une séance en combine plusieurs.

### Réponses Bruno du 2026-10-09 (relevées par Yohan, l'après-midi)

**`GET /cinemas/22`**
- 45 cinémas. Champs : `CinemaId` (193 = Orleans, **189 = Kanata**),
  `CinemaName`, `CinemaUrl`, `CinemaFilmUrl`, `CinemaExternalId` (1164 pour
  Orleans ; identifiant Vista probable, **à vérifier**). **Ni province, ni
  fuseau, ni coordonnées.**
- `CinemaUrl` porte une région (`/region/75/cinema/193`) : 72 = AB, 73 = BC,
  74 = MB, 75 = ON, 76 = SK, 77 = YT d'après les noms. Quatre cinémas n'en ont
  pas (Airdrie 7787, Edson 7782, Fort McMurray 7799, West Kelowna Encore
  7784), et **Calgary Market Mall (7800) est en région 73, celle de la C.-B.**
  La région est un découpage marketing, pas une donnée géographique fiable.
- Fuseaux couverts par ces 45 cinémas (connaissance IANA, **à vérifier**
  cinéma par cinéma) : `America/Vancouver`, `America/Edmonton` (dont
  Cranbrook), `America/Dawson_Creek` (Dawson Creek, Fort St. John : heure
  normale des Rocheuses toute l'année), `America/Regina`, `America/Winnipeg`,
  `America/Toronto`, `America/Whitehorse`. Sept fuseaux pour une seule chaîne.

**`GET /movies/22/193`** (60 fiches)
- Tableau plat de fiches, trié par `ReleaseDate` décroissante.
- **La semaine S+1 n'est publiée que pour les nouveautés.** Relevé un
  vendredi (2026-10-09) : les films à l'affiche (Verity, Digger, Primetime…)
  ont des séances jusqu'au jeudi 15, puis rien ; certains s'arrêtent même au
  lundi 12 (Paw Patrol FR, Avengers Endgame, Forgotten Island ScreenX,
  Resident Evil ScreenX, Pan's Labyrinth). Seules les sorties à venir (Street
  Fighter, Whalefall, Sense and Sensibility, Clayface…) et les événements ont
  des séances au-delà. Un diff calculé ce jour-là sur la semaine du 16
  classerait presque tout en « Parti ». Voir Q10.
- **Les avant-premières du jeudi sont sur la fiche principale** : Avengers:
  Doomsday (sortie 2026-12-18, séances dès le 17), Clayface (23 / 22), Street
  Fighter (16 / 15), Whalefall (16 / 15), Sense and Sensibility (16 / 15). Avec
  une semaine vendredi → jeudi, ces films apparaissent dans la semaine
  *précédant* leur sortie, avec une ou trois séances. Les accès anticipés plus
  tôt dans la semaine sont des fiches à part (« Sense and Sensibility: Early
  Access Movie Party », mercredi 14, expérience `705`).
- Une même fiche mélange des formats de séance : Clayface et Other Mommy ont
  des séances `2D` et `EXTRA` sous le même `FilmId`. Seuls ScreenX et
  Infinity Vision ont des fiches à part. Le découpage en fiches est un choix
  de programmation de Landmark, pas une règle de format.
- Les séances passées du jour restent dans la réponse avec
  `SessionExpired: true` (Forgotten Island 16 h, Resident Evil 15 h 50).
- Les 4 séances « EXTRA » et « 2D » d'un même jour d'Other Mommy sont dans
  trois groupes `ExperienceTypes` différents : la combinaison d'expériences,
  pas le format, sert de clé de regroupement.
- Signal d'événement : `IsNowShowing` et `IsComingSoon` sont tous deux faux
  pour les opéras, concerts et fan events, mais aussi pour toutes les sorties
  à venir ; non discriminant. « Monday Mystery Movie - Oct 19 » n'a aucune
  expérience d'événement : seul le titre le signale.

**Autres pièges**
- Titres à article inversé : « Social Reckoning, The », « Departed - 20th
  Anniversary, The ». Reprises avec l'année dans le titre : « Halloween
  (1978) », « E.T. the Extra-Terrestrial (1982) ».
- Les mêmes films ont des `FilmId` sans rapport avec ceux de Cineplex (Verity :
  126449 / 38401). Même `FilmId` d'un cinéma Landmark à l'autre : **à
  vérifier**.

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
| Q1 | ID de film stable, identique en `en` et `fr` ? | **Levée (2026-10-09)** : même `id`, même `name` ; `fr` est un sur-ensemble de `en`. Stabilité dans le temps : à observer sur les collectes |
| Q1b | Les noms de cinéma changent-ils avec `language` ? | Bruno 01 avec `language=en`, comparer le 9406 |
| Q2 | `vistaSessionId` stable ? Unique par cinéma ou global ? | Bruno 04, deux exécutions à quelques heures d'écart ; puis un second cinéma (9172) |
| Q3 | Quels champs de séance sont volatils ? | Bruno 04, sortie « Champs modifiés » |
| Q4 | `ETag` / `304` pris en charge ? | Bruno 06 juste après 04 |
| Q5 | Forme du 401 : clé absente et clé invalide se distinguent-elles ? | Bruno 07 → 08 |
| Q6 | `showStartDateTime` contient-il un décalage horaire ? | **En grande partie levée (2026-10-09)** : non, mais `showStartDateTimeUtc` donne l'instant. Reste : Bruno 04 sur Vancouver (1422) pour confirmer le champ UTC hors du fuseau de l'Est |
| Q7 | Combien de jours par appel `/showtimes` sans date ? | Bruno 05 |
| Q8 | Comment Landmark expose-t-il ses séances ? | **Levée (2026-10-09)** : API JSON `movieapi…/movies/22/<cinéma>` (et copie dans le HTML), heure locale sans fuseau. Restent : stabilité des `Scheduleid`, `FilmId` d'un cinéma à l'autre, lien `/booking` depuis un courriel, conditions d'utilisation |
| Q16 | Landmark accepte-t-il un accès automatisé ? | **Techniquement levée (2026-10-09)** : `curl` → 403 Akamai ; Bruno (`bruno-runtime/4.2.1`) → 200 ; `HttpClient` du JDK avec `CinemaTower/0.1` → 200. Restent : 1) lire les conditions d'utilisation du site (bloquant avant toute collecte planifiée) ; 2) écrire à Landmark (guestservices@landmarkcinemas.com), recommandé, non bloquant. Landmark n'est pas dans l'étape 1 |
| Q9 | `perfix` de la Cinémathèque stable dans le temps ? | Relever les liens deux jours de suite |
| Q10 | Quand la semaine S+1 apparaît-elle dans `/showtimes` ? D'un bloc ou film par film (préventes, avant-premières) ? Landmark, 2026-10-09 : film par film, les nouveautés d'abord, les films à l'affiche plus tard (mercredi annoncé) | Bruno 05 une fois par jour du lundi au vendredi, noter la dernière date renvoyée et les films de S+1 déjà présents |
| Q11 | `/movies` donne-t-il un identifiant externe (IMDb, TMDB), une date de sortie, ou un champ reliant une VF à sa VO ? (absents de `/showtimes`) | **Levée (2026-10-09)** : date de sortie oui ; identifiant externe et lien VF/VO non |
| Q12 | Le titre québécois (ex. « Rapide et dangereux ») est-il celui que renvoie Cineplex avec `language=fr` ? Et TMDB en `fr-CA` ? | Bruno 03 sur un film à titre traduit ; même film sur TMDB en `fr-CA` et `fr-FR` |
| Q13 | Au Québec, VF et VO : deux fiches ou une ? | **Levée (2026-10-09)** : deux fiches, deux `id`, version dans `languageCode` / `subtitleLanguage`, aucun champ ne les relie |
| Q14 | Quelle URL ouvre la réservation d'une séance **sans clé**, depuis le navigateur d'un abonné ? | Fournies par séance (`ticketingUrl`, `deeplinkUrl`, `seatMapUrl`). Ouvrir chacune dans une fenêtre privée ; méfiance pour `deeplinkUrl` (chemin protégé par la clé) |
| Q15 | `experienceTypes` : liste des valeurs (Regular, 3D, VIP, D-BOX, IMAX, IMAX 70 mm… selon Yohan), combinables sur une même séance ? | Partiellement levée (2026-10-09) : `Regular`, `3D` observés, non traduits avec `language=fr`. Reste : un cinéma avec IMAX et VIP pour les combinaisons |
