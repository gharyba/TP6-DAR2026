package service;

import java.util.LinkedHashMap;
import java.util.Map;
import corbaBanque.Compte;
import corbaBanque.IBanqueRemotePOA;

// Le traitement des comptes se fait uniquement sur le serveur.
public class BanqueImpl extends IBanqueRemotePOA {
    private final Map<Integer, Compte> comptes = new LinkedHashMap<Integer, Compte>();

    @Override
    public synchronized void creerCompte(Compte cpte) {
        if (cpte.code <= 0 || cpte.solde < 0 || Float.isNaN(cpte.solde) || Float.isInfinite(cpte.solde)) {
            System.out.println("Création refusée : compte invalide.");
        } else if (comptes.containsKey(cpte.code)) {
            System.out.println("Création refusée : le compte " + cpte.code + " existe déjà.");
        } else {
            comptes.put(cpte.code, new Compte(cpte.code, cpte.solde));
            System.out.println("Compte " + cpte.code + " créé.");
        }
    }

    @Override
    public synchronized void verser(float mt, int code) {
        Compte compte = comptes.get(code);
        if (compte == null || mt <= 0 || Float.isNaN(mt) || Float.isInfinite(mt)
                || Float.isInfinite(compte.solde + mt)) {
            System.out.println("Versement refusé pour le compte " + code + ".");
        } else {
            compte.solde += mt;
            System.out.println("Versement de " + mt + " sur le compte " + code + ".");
        }
    }

    @Override
    public synchronized void retirer(float mt, int code) {
        Compte compte = comptes.get(code);
        if (compte == null || mt <= 0 || Float.isNaN(mt) || Float.isInfinite(mt)
                || mt > compte.solde) {
            System.out.println("Retrait refusé pour le compte " + code + ".");
        } else {
            compte.solde -= mt;
            System.out.println("Retrait de " + mt + " sur le compte " + code + ".");
        }
    }

    @Override
    public synchronized Compte getCompte(int code) {
        Compte compte = comptes.get(code);
        if (compte == null) {
            // Le code -1 indique qu'aucun compte n'a été trouvé.
            return new Compte(-1, 0);
        }
        return new Compte(compte.code, compte.solde);
    }

    @Override
    public synchronized Compte[] getComptes() {
        Compte[] resultat = new Compte[comptes.size()];
        int i = 0;
        for (Compte compte : comptes.values()) {
            resultat[i++] = new Compte(compte.code, compte.solde);
        }
        return resultat;
    }

    @Override
    public double conversion(float mt) {
        return mt * 3.0;
    }
}
