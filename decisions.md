# Décisions

Format : la décision, la raison, ce qu'elle exclut. Numérotation continue, jamais
réutilisée. Une décision remplacée n'est pas effacée : on la barre et on renvoie à
celle qui la remplace.

> **Reconstitution (2026-09-29).** D-001 à D-005 ont été prises dans une
> conversation antérieure. Leur énoncé vient des garde-fous de `CLAUDE.md` §5 ;
> leurs **raisons ont été reconstituées** et sont **à confirmer** par Yohan
> contre la conversation d'origine. D-004 est perdue.

---

**D-001 — L'UID d'un événement `.ics` est construit à partir des identifiants de la source.**
- Raison : un client de calendrier reconnaît un événement par son UID d'une
  génération du flux à l'autre ; une clé interne change si la base est
  reconstruite, et l'abonné verrait des doublons.
- Exclut : toute clé technique de la base (séquence, UUID généré) dans l'UID.
- ⚠ Dépend de Q2 et Q9 (`sources.md`) : si l'identifiant de séance d'une source
  n'est pas stable, cette décision ne tient pas pour cette source.

**D-002 — Le domaine ne connaît pas les sources.**
- Raison : ajouter une source doit se limiter à écrire un adaptateur ; chaque
  adaptateur traduit le modèle de sa source vers celui du domaine (couche
  anti-corruption).
- Exclut : tout test sur l'origine d'une donnée (`if source == CINEPLEX`) hors des
  adaptateurs.

**D-003 — Un échec de collecte n'est pas une programmation vide.**
- Raison : sinon une panne de la source fait « sortir » tous les films, puis les
  fait « entrer » à la collecte suivante, et l'abonné reçoit un faux diff.
- Exclut : un diff calculé contre une collecte échouée. Le diff ne compare que
  deux collectes réussies d'un même cinéma.

**D-004 — _Perdue._**
- À récupérer dans la conversation d'origine. Numéro réservé, ne pas réutiliser.

**D-005 — Pas d'heure sans fuseau.**
- Raison : le Canada couvre six fuseaux ; une heure locale sans fuseau est ambiguë
  dès qu'on compare deux cinémas ou qu'on produit un `.ics`.
- Exclut : les heures locales « nues » en base. On stocke un instant, et un fuseau
  IANA par cinéma (ex. `America/Toronto`).

**D-006 — Socle : Spring Boot 4.1 et Java 25, au lieu de Spring Boot 3 et Java 21.** _(2026-10-01)_
- Raison : le support libre de Spring Boot 3.5, dernière version 3.x, a pris fin
  le 2026-06-30 (constaté sur endoflife.date et Maven Central le 2026-10-01).
  Démarrer sur une branche déjà hors support, c'est s'imposer la migration 3 → 4
  (Jackson 3, starters modulaires) avant même d'avoir du code. Java 25 est la LTS
  courante et fait partie des versions prises en charge par Boot 4.1 (17 à 26).
- Exclut : Spring Boot 3.x ; un JDK hors de la plage prise en charge par Boot
  (le JDK 27 installé par IntelliJ compile en `--release 25`, mais n'est pas
  une cible d'exécution prise en charge).

**D-007 — PostgreSQL partout, installé directement sur le poste ; pas de H2, pas de Docker pour l'instant.** _(2026-10-01)_
- Raison : H2 diverge de PostgreSQL précisément sur ce qui est irréversible ici
  (sémantique de `timestamptz` pour D-005, syntaxe des migrations Flyway,
  `ON CONFLICT`, `jsonb`). Docker Desktop écarté pour son coût d'installation ;
  une installation native (PostgreSQL 18) suffit en développement.
- Exclut : H2 sous toutes ses formes (dev et tests) ; Testcontainers. Les tests
  d'intégration tournent contre une base locale dédiée (`cinematower_test`),
  distincte de la base de développement, qu'on remet à zéro par Flyway
  (`clean` + `migrate`) avant la suite de tests.
- À revoir si : un second poste ou une CI doit exécuter les tests d'intégration
  — sans conteneur, chaque environnement doit fournir son propre PostgreSQL.

**D-008 — Structure de l'infolettre : « Nouveau partout » en tête, puis un bloc par cinéma (« À l'affiche », « Parti »).** _(2026-10-09)_
- Structure :
  - En tête, « Nouveau partout » : les films nouveaux dans **tous** les cinémas
    suivis (sortie large, type Avengers). Affichée seulement si l'abonné suit
    au moins deux cinémas et si elle n'est pas vide.
  - Puis un bloc par cinéma suivi : « À l'affiche » (liste complète de la
    semaine, nouveautés marquées d'une étiquette) et « Parti ». Un film présent
    dans deux cinémas apparaît dans les deux blocs.
  - Chaque film renvoie vers une page CinemaTower (film × cinéma) qui liste ses
    séances ; chaque séance renvoie vers la réservation chez la source. Le lien
    de réservation est fourni par l'adaptateur (D-002) ; le domaine le traite
    comme opaque.
  - Depuis « Nouveau partout » (pas de cinéma précis), la page liste les
    cinémas suivis par l'abonné qui jouent le film. L'URL porte les
    identifiants de ces cinémas, jamais l'identité de l'abonné.
  - L'URL d'une page est permanente (un courriel envoyé ne se retire pas) :
    construite à partir d'identifiants de la source, comme l'UID `.ics`
    (D-001), jamais d'une clé interne de la base. Format à fixer avant le
    premier envoi.
  - La page affiche la date de la collecte dont viennent les horaires, et
    jamais les places restantes : la source fait foi au moment de réserver.
- Raison : l'abonné doit tout lire dans le courriel. La tête répond à « qu'est-ce
  qui sort partout ? », les blocs servent de référence complète ; l'étiquette
  évite de lister deux fois un film dans un même bloc. L'unité du diff est le
  couple (cinéma, film) ; on compare l'ensemble des films d'une semaine cinéma
  (vendredi → jeudi) à celui de la semaine précédente, entre deux collectes
  réussies (D-003).
- Exclut : un catalogue à consulter sur le site ; une section « Nouveau » séparée
  dans chaque bloc ; le diff au niveau de la séance (horaire déplacé, salle
  changée) ; pour l'instant, l'annonce prospective (« dernière semaine pour voir
  X »).
- En attente de Q10 (`sources.md`) : le jour d'envoi (premier jour où la semaine
  S+1 est complète) et le rattachement des avant-premières du jeudi.

**D-009 — Un film du domaine porte un identifiant TMDB quand le rapprochement réussit.** _(2026-10-09)_
- Raison : au sein d'une source, l'identifiant de la source suffit à regrouper
  un film entre cinémas (sous réserve de Q1). TMDB sert, plus tard, à
  regrouper à l'affichage les fiches d'une même œuvre (VF/VO, D-012) et à
  reconnaître le même film entre sources (Landmark, Cinémathèque), qui n'ont
  aucun identifiant commun. _(Corrigé le 2026-10-09 : l'affiche n'est plus une
  raison, Cineplex la fournit en trois tailles. TMDB sort de l'étape 1.)_ Rapprochement en cascade :
  identifiant externe fourni par la source (IMDb, Q11) ; sinon titre + année,
  retenu seulement si le résultat est unique ; sinon film « non rapproché »,
  affiché quand même avec les données de la source et placé dans une file de
  correction manuelle.
- Exclut : la recherche par titre seul (homonymes, ressorties type « Akira 4K »,
  événements) ; un envoi bloqué par un rapprochement raté ; un identifiant de
  source comme clé de film hors de son adaptateur (D-002).

**D-010 — Titres : par défaut, le titre français et le titre anglais, tels que la source les publie.** _(2026-10-09)_
- Raison : au Québec les titres diffèrent (« Rapide et dangereux » / « Fast and
  Furious ») ; le titre de la source est celui que l'abonné retrouve au cinéma
  et sur la page de réservation. Si les deux titres sont identiques, on
  l'affiche une fois. Une préférence d'abonné (un seul titre) pourra s'ajouter.
  Les libellés (en-têtes, objet, désabonnement, page CinemaTower) sont dans la
  langue que l'abonné choisit à l'inscription.
- Exclut : les titres TMDB à l'affichage (probablement ceux de France, Q12) ;
  un courriel aux libellés bilingues.
- ⚠ À réécrire (2026-10-09) : D-012 montre que la source ne donne pas un titre
  FR et un titre EN par film, mais une fiche par version avec son propre titre.
  « Les deux titres » ne peut venir que du regroupement des fiches VF/VO, donc
  de TMDB. En attendant, chaque fiche s'affiche avec son titre. Revoir après Q1.

**D-011 — Collecte quotidienne ; le courriel et la page ne lisent que la base.** _(2026-10-09)_
- Raison : la lecture ne dépend pas de la disponibilité de la source, et
  l'empreinte réseau ne dépend pas du nombre d'abonnés (§5.8). L'historique
  des collectes est nécessaire au diff, que l'API ne donne plus une fois la
  semaine passée. Le rythme quotidien mesure le moment de publication et la
  fréquence des changements (Q3, Q10) au lieu de les supposer, et une collecte
  échouée est rattrapée le lendemain (D-003). La réponse brute est conservée
  (`jsonb`) pour recalculer si l'adaptateur avait un bogue.
- Exclut : tout appel à une source pendant le rendu d'un courriel ou d'une page ;
  l'écrasement d'une collecte par la suivante.
- À revoir : après trois ou quatre semaines de mesures, passer à un rythme
  hebdomadaire si l'horaire publié ne change pas.

**D-012 — L'unité du diff est la fiche de programmation, pas l'œuvre.** _(2026-10-09)_
- Définition : une fiche est ce qu'une source programme comme une unité — une
  œuvre dans une version donnée (« Verity » et « Verity (Version française) »
  sont deux fiches), ou un programme de plusieurs films (Cinémathèque). Elle
  porte sa version : langue audio et langue des sous-titres, chacune
  facultative (« inconnue » si la source ne la donne pas).
- Raison : constaté chez Cineplex le 2026-10-09 (`sources.md`), chaque version
  est une fiche distincte et rien ne relie une VF à sa VO ; fusionner
  obligerait à deviner, et une fusion fausse affiche les séances d'un autre
  film. La fiche est aussi le modèle commun le plus sûr entre sources : si une
  source (Landmark ?) porte la version sur la séance, son adaptateur **découpe**
  les séances par version, opération déterministe ; l'inverse (fusionner) ne
  l'est jamais. « Parti : Verity (Version française) » devient une information
  utile, pas un faux positif.
- Exclut : la fusion des versions dans le domaine ou dans un adaptateur.
  Le regroupement VF/VO sur une même ligne viendra plus tard, à l'affichage
  seulement, à partir de TMDB (D-009), sans toucher au diff.

---

## En attente

Propositions non validées par Yohan. Pas de numéro tant qu'elles ne sont pas
tranchées ; on les promeut en `D-NNN` au moment de la validation.

- **Types de séance** (proposition du 2026-10-09) : stocker les libellés
  `experienceTypes` tels que la source les donne, avec une table de traduction
  pour l'affichage (`Regular` → « Standard ») et le libellé brut pour un type
  inconnu ; pas de liste de valeurs typée dans le domaine tant qu'aucune règle
  ne s'en sert (filtre IMAX, surveillance de sièges). Raison : YAGNI ; les
  marques changent (ScreenX, 4DX…) et `Regular` n'est pas traduit par l'API.
- **Liens des films « Parti »** : pas de lien (la page source peut avoir
  disparu). Non discuté.
- **Cinémas collectés** : seulement ceux qui ont au moins un abonné (empreinte
  minimale) ; un cinéma nouvellement suivi n'a que « À l'affiche » la première
  semaine. À confirmer à l'étape 5.

Prochaines vérifications, par ordre de priorité (détail dans `sources.md`) :
1. Paramètre `date` envoyé ou non dans la requête 9195 du 2026-10-09 (Q7).
2. Q14 : `ticketingUrl` / `deeplinkUrl` en fenêtre privée.
3. Q1 : `name` et `id` d'une fiche avec `language=en` (débloque D-010).
4. Q6 : `showStartDateTimeUtc` sur Vancouver (1422).
5. Q11 : champs de `/movies` pour Verity VO / VF.
6. Q10 : Bruno 05 chaque jour du lundi au vendredi.
7. Q5 : forme du 401.
