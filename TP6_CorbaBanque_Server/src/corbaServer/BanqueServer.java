package corbaServer;

import javax.naming.Context;
import javax.naming.InitialContext;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import service.BanqueImpl;

public class BanqueServer {
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            System.out.println("ORB initialisé.");

            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();
            System.out.println("POA activé.");

            BanqueImpl banque = new BanqueImpl();
            org.omg.CORBA.Object reference = poa.servant_to_reference(banque);
            Context contexte = new InitialContext();
            contexte.rebind("banque", reference);
            contexte.close();
            System.out.println("Service banque enregistré avec JNDI.");
            System.out.println("Serveur en attente des clients...");
            orb.run();
        } catch (Exception e) {
            System.err.println("Démarrage du serveur impossible : " + e);
            e.printStackTrace();
        }
    }
}
