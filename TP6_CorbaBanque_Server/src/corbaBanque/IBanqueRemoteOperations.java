package corbaBanque;

public interface IBanqueRemoteOperations
{
  void creerCompte (corbaBanque.Compte cpte);
  void verser (float mt, int code);
  void retirer (float mt, int code);
  corbaBanque.Compte getCompte (int code);
  corbaBanque.Compte[] getComptes ();
  double conversion (float mt);
}
