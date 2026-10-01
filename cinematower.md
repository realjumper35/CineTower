# CinemaTower

Squelette du produit. Le détail d'un sujet creusé va dans un fichier dédié
(`sources.md`, plus tard `diff.md`), pas ici.

## Produit

Infolettre courriel hebdomadaire, bilingue FR/EN : les films qui **entrent** et
**sortent** de l'affiche dans les cinémas choisis par l'abonné. Le cœur du produit
est le **diff**, pas un catalogue.

## Périmètre

- Canada.
- Sources initiales : Cineplex (API JSON non documentée), Landmark (pas encore
  reconnue), Cinémathèque québécoise (pages HTML, billetterie externe).
- Métadonnées de films : TMDB.

## Technique

Java 25, Spring Boot 4, PostgreSQL, Thymeleaf (D-006).

## Feuille de route

1. Infolettre hebdomadaire (le diff).
2. Flux `.ics` (tiré par le client) et alerte immédiate (poussée) : deux
   fonctionnalités distinctes.
3. Surveillance de sièges.

## Contexte

Projet de portfolio : le raisonnement compte autant que le résultat.
