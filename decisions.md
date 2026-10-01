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
