# CinemaTower

Squelette du produit. Le détail d'un sujet creusé va dans un fichier dédié
(`sources.md`, plus tard `diff.md`), pas ici.

## Produit

Infolettre courriel hebdomadaire, FR ou EN au choix de l'abonné, pour les
cinémas qu'il suit : **nouveau partout** en tête, puis par cinéma **à l'affiche**
(nouveautés marquées) et **parti** (D-008). Le cœur du produit est le **diff**.
Chaque film mène à une page CinemaTower qui liste ses séances, puis à la
réservation chez la source. Le site sert à s'inscrire et à ces pages ; pas de
catalogue à parcourir.

## Périmètre

- Canada.
- Sources initiales : Cineplex (API JSON non documentée), Landmark (pas encore
  reconnue), Cinémathèque québécoise (pages HTML, billetterie externe).
- TMDB : regroupement des fiches d'une même œuvre (VF/VO, entre sources), après
  l'étape 1 (D-009, D-012).

## Technique

Java 25, Spring Boot 4, PostgreSQL, Thymeleaf (D-006).

## Feuille de route

1. Infolettre hebdomadaire (le diff), en tranche verticale sur Cineplex et un
   seul cinéma :
   0. Lever les inconnues bloquantes (Bruno), écrire les décisions qui en
      dépendent.
   1. Squelette : classe d'application, Maven Wrapper.
   2. Domaine pur (fiche, séance, collecte réussie/échouée, diff), sans Spring.
   3. Port `SourceProgrammation` et adaptateur Cineplex, testé sur des réponses
      réelles enregistrées, sans réseau.
   4. Persistance : base `cinematower_test`, profil de test, Flyway `V1`,
      schéma, `JdbcClient`, collecte idempotente, premier test d'intégration.
   5. Collecte quotidienne planifiée, diff journalisé. Y arriver vite : chaque
      jour sans collecte est de l'historique perdu.
   6. Rendu de l'infolettre et de la page film × cinéma (Thymeleaf), sans envoi.
   7. Abonnés, envoi, LCAP.
2. Flux `.ics` (tiré par le client) et alerte immédiate (poussée) : deux
   fonctionnalités distinctes.
3. Surveillance de sièges.

## Contexte

Projet de portfolio : le raisonnement compte autant que le résultat.
