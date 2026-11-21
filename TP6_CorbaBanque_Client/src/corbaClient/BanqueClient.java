package corbaClient;

import javax.naming.Context;
import javax.naming.InitialContext;
import corbaBanque.Compte;
import corbaBanque.IBanqueRemote;
import corbaBanque.IBanqueRemoteHelper;

public class BanqueClient {
    public static void main(String[] args) {
        try {
            Context contexte = new InitialContext();
            org.omg.CORBA.Object reference = (org.omg.CORBA.Object) contexte.lookup("banque");
            IBanqueRemote banque = IBanqueRemoteHelper.narrow(reference);
            contexte.close();

            banque.creerCompte(new Compte(101, 1000));
            System.out.println("Création 101 : " + banque.getCompte(101).solde);

            banque.verser(200, 101);
            System.out.println("Après versement : " + banque.getCompte(101).solde);

            banque.retirer(150, 101);
            Compte compte = banque.getCompte(101);
            System.out.println("Après retrait : code=" + compte.code + ", solde=" + compte.solde);

            banque.creerCompte(new Compte(102, 500));
            Compte[] liste = banque.getComptes();
            System.out.println("Liste des comptes (" + liste.length + ") :");
            for (Compte c : liste) {
                System.out.println("  code=" + c.code + ", solde=" + c.solde);
            }

            System.out.println("500 euros = " + banque.conversion(500) + " dinars");

            banque.creerCompte(new Compte(101, 999));
            System.out.println("Après doublon : " + banque.getCompte(101).solde);
            banque.retirer(2000, 101);
            System.out.println("Après retrait excessif : " + banque.getCompte(101).solde);
            Compte inconnu = banque.getCompte(999);
            System.out.println("Compte 999 : code=" + inconnu.code + " (absent si -1)");
        } catch (Exception e) {
            System.err.println("Appel du service banque impossible : " + e);
            e.printStackTrace();
        }
    }
}
