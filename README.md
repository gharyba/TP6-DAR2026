# TP6 – Service Banque en CORBA

Faculté des Sciences de Sfax – LSI3 – DAR 2026-2027
Responsable du cours : Mme Amal ABID – Enseignante des TP : Mme Zeineb DAOUD

## 1. Présentation du TP

J'ai réalisé un service bancaire client/serveur avec CORBA. Le contrat est écrit en IDL, puis `idlj` génère le stub du client et le skeleton du serveur. L'ORB transporte les appels distants ; le POA relie le servant Java à sa référence CORBA. Le Naming Service conserve cette référence sous un nom et JNDI permet au serveur de l'enregistrer et au client de la retrouver.

## 2. Organisation du projet

```text
TP6-DAR2026/
├── TP6_CorbaBanque_Server/
│   └── src/
│       ├── Banque.idl
│       ├── jndi.properties
│       ├── corbaBanque/       (11 sources générées par idlj)
│       ├── service/BanqueImpl.java
│       └── corbaServer/BanqueServer.java
├── TP6_CorbaBanque_Client/
│   └── src/
│       ├── Banque.idl
│       ├── jndi.properties
│       ├── corbaBanque/       (11 sources générées par idlj)
│       └── corbaClient/BanqueClient.java
├── docs/test-results/
├── docs/screenshots/
├── .gitignore
└── README.md
```

Les deux projets sont indépendants. Les fichiers `corbaBanque/` ont été produits dans chaque projet par le vrai compilateur `idlj -fall -v Banque.idl`. Les fichiers `bin/` peuvent être régénérés et ne sont pas conservés dans Git.

## 3. Environnement

Le TP demande **JDK 8**. JDK 21 est également installé sur la machine, mais il ne fournit plus `idlj`, `tnameserv` ni l'API CORBA classique. Pour ce TP, j'ai utilisé **Eclipse Temurin 8u512-b09** extrait sans installation globale dans `tp6\.tools\jdk8u512-b09`. Les commandes ci-dessous appellent ses exécutables par leur chemin ; le JDK 21 et les variables Windows restent inchangés.

Pour reproduire le travail sur un autre poste, télécharger l'archive **Windows x64 JDK 8** depuis [Eclipse Adoptium](https://adoptium.net/temurin/releases/?os=windows&arch=x64&version=8&package=jdk), l'extraire, puis adapter la variable `$jdk` ci-dessous. La copie du JDK ne fait pas partie du dépôt Git.

## 4. Développement du serveur

`Banque.idl` définit le module `corbaBanque`, la structure `Compte` (`long code`, `float solde`), le type `tabComptes` (`sequence<Compte>`) et les six opérations de `IBanqueRemote` : `creerCompte`, `verser`, `retirer`, `getCompte`, `getComptes` et `conversion`. Le `long` IDL devient un `int` Java. Comme l'énoncé ne précise pas le résultat des trois premières opérations, elles retournent `void`.

`BanqueImpl` hérite du `IBanqueRemotePOA` généré. Les comptes sont gardés dans une `Map` en mémoire. Les méthodes qui accèdent à cette collection sont synchronisées. Un code déjà utilisé ne remplace pas le compte existant ; un versement invalide ou un retrait supérieur au solde est refusé. Puisque le contrat IDL n'a pas d'exception métier, ces refus sont affichés sur le serveur et les soldes restent inchangés. Pour `getCompte`, le code `-1` dans la structure retournée signifie « compte inconnu » ; `getComptes` retourne un tableau vide si aucun compte n'existe.

`conversion` calcule `mt × 3` sur le serveur. C'est le taux simplifié utilisé dans le cours de Mme Amal ABID, **pas un taux de change réel**.

`BanqueServer` lance l'ORB avec `ORB.init(args, null)`, récupère `RootPOA`, active son `POAManager`, crée `BanqueImpl`, transforme le servant avec `poa.servant_to_reference`, publie la référence sous `banque` avec `InitialContext.rebind`, puis attend les requêtes avec `orb.run()`.

## 5. Développement du client

Le client possède sa propre copie de `Banque.idl` et des classes générées. `BanqueClient` lit la configuration JNDI, effectue `InitialContext.lookup("banque")` et utilise `IBanqueRemoteHelper.narrow()` pour obtenir le stub typé. Il exécute ensuite les six opérations. Les valeurs affichées pour les soldes viennent du service distant.

Les deux fichiers `jndi.properties` contiennent `com.sun.jndi.cosnaming.CNCtxFactory` et `iiop://localhost:900`. Ils sont copiés à la racine de chaque dossier `bin/` pour être lus depuis le classpath.

## 6. Compilation et exécution

Ouvrir PowerShell dans `TP6-DAR2026`. Ces commandes utilisent le JDK 8 portable réellement employé :

```powershell
$jdk = 'C:\Users\Dell\Desktop\lsi3_2026\DAR\tp\tp6\.tools\jdk8u512-b09\bin'
$s = 'TP6_CorbaBanque_Server'
$c = 'TP6_CorbaBanque_Client'

New-Item -ItemType Directory -Force "$s/bin", "$c/bin" | Out-Null

Push-Location "$s/src"
& "$jdk/idlj.exe" -fall -v Banque.idl
Pop-Location
& "$jdk/javac.exe" -encoding UTF-8 -d "$s/bin" (Get-ChildItem "$s/src" -Recurse -Filter '*.java').FullName
Copy-Item "$s/src/jndi.properties" "$s/bin/jndi.properties"

Push-Location "$c/src"
& "$jdk/idlj.exe" -fall -v Banque.idl
Pop-Location
& "$jdk/javac.exe" -encoding UTF-8 -d "$c/bin" (Get-ChildItem "$c/src" -Recurse -Filter '*.java').FullName
Copy-Item "$c/src/jndi.properties" "$c/bin/jndi.properties"
```

Ouvrir ensuite **trois terminaux PowerShell**, tous dans `TP6-DAR2026`, et les lancer dans cet ordre. Dans chaque terminal, définir d'abord :

```powershell
$jdk = 'C:\Users\Dell\Desktop\lsi3_2026\DAR\tp\tp6\.tools\jdk8u512-b09\bin'
```

Terminal 1 – Naming Service :

```powershell
& "$jdk/tnameserv.exe" -ORBInitialPort 900
```

Terminal 2 – serveur :

```powershell
& "$jdk/java.exe" -cp 'TP6_CorbaBanque_Server/bin' corbaServer.BanqueServer
```

Terminal 3 – client :

```powershell
& "$jdk/java.exe" -cp 'TP6_CorbaBanque_Client/bin' corbaClient.BanqueClient
```

Garder les deux premiers terminaux ouverts pendant l'essai. Arrêter ensuite le client, le serveur et l'annuaire avec `Ctrl+C`. Le port `900` doit être libre avant le lancement de `tnameserv`.

## 7. Tests réalisés

Les sorties réelles sont dans `docs/test-results/`. Les compilations IDL serveur/client et Java serveur/client ont toutes réussi. `javac` émet un avertissement dans le stub généré au sujet d'une API interne, sans erreur de compilation.

| Opération appelée à distance | Résultat observé |
| --- | --- |
| `creerCompte(101, 1000)` | Compte 101 créé, solde 1000.0 |
| `verser(200, 101)` | Solde 1200.0 |
| `retirer(150, 101)` | Solde 1050.0 |
| `getCompte(101)` | Code 101, solde 1050.0 |
| `getComptes()` | Deux comptes : 101 (1050.0) et 102 (500.0) |
| `conversion(500)` | 1500.0 dinars |

J'ai aussi vérifié le refus d'un doublon (solde 101 resté à 1050.0), d'un retrait de 2000 (solde inchangé) et le retour `code=-1` pour le compte 999. Sans Naming Service, le client obtient une erreur de connexion CORBA. Avec l'annuaire mais sans le serveur, il obtient `NameNotFoundException`. Après les appels, le serveur était encore actif.

## 8. Captures d'écran

Le dossier `docs/screenshots/` est prêt. Aucune capture n'a été fabriquée. Pour compléter le livrable, prendre des captures réelles montrant : la version du JDK 8 et ses outils ; `idlj` côté serveur et les fichiers générés ; `idlj` côté client ; le terminal `tnameserv` sur le port 900 ; le serveur après publication JNDI ; le client avec la création, le versement, le retrait, la liste et la conversion. Nommer les images `01-verification-jdk8.png` à `10-client-conversion.png` si cet ordre est utilisé, puis insérer les liens Markdown correspondants ici après avoir vérifié les fichiers.

## 9. Synthèse

Ce TP m'a permis de passer d'un contrat IDL à des appels distants réels. `idlj` a généré les classes techniques, tandis que j'ai écrit le traitement bancaire dans le servant. J'ai également utilisé l'ORB, le POA et le Naming Service pour rendre l'objet accessible, puis JNDI et `narrow()` pour le retrouver côté client.

## 10. Dépôt

Le nom prévu du dépôt GitHub est `TP6-DAR2026`. Après l'ajout des captures, son lien devra être déposé sur l'ENT. Aucune publication distante n'est effectuée dans ce dossier.
