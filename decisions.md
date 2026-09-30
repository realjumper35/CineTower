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
