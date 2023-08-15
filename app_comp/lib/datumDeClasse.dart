class DatumDeClasse{
  final String classe;
  List<List<String>> donnees= [];
  List<int> indiceEleves = [];
  List<String> nomsEleves = []; //valeur:nom | indice:élève
  List<int> configurationPlane = [];
  int colonne = 0;
  List<int> placesOccupeesDebase = [];
  List<int> placesOccupees = []; //valeur: indice de l'élève | indice: place | -1 si vide
  Map<int,double> reussitesCalculees = {};
  List<List<int>> plansEnregistres = [];
  List<int> parametresPlan = [];  //affinites_e = 2 | affinites_i | vue  | taille  | alternanceFG  | alternanceAC  | alternanceFD  |ordre_alpha
  List<int> prioritesDeTraitement = []; //valeur: priorité du traitement de l'élève | indice: élève
  List<List<String>> affiniteElevesE = []; //valeur: [<liste des noms>] | indice: élève
  List<List<String>> affiniteElevesI = []; //valeur: [<liste des noms>] | indice: élève
  List<int> listeElevesTriee = [];
  int maxTolere = 0;  //niveau de correspondance
  double contrainteAct = 0;
  List<double> reussiteVariante = [];
  DateTime tempsDebut = DateTime.now();
  int tempsTotalMilli = 10000;

  DatumDeClasse(this.classe);

}