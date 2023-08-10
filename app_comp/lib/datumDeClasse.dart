class DatumDeClasse{
  final String classe;
  List<List<String>> donnees= [];
  List<int> indiceEleves = [];
  List<String> nomsEleves = []; //valeur:nom | indice:élève
  List<int> configurationPlane = [];
  int colonne = 0;
  List<int> placesOccupeesDebase = [];
  List<int> placesOccupees = []; //valeur: indice de l'élève | indice: place | -1 si vide
  List<List<int>> planEnregsitres = [];
  List<int> parametresPlan = [];  //affinites_e = 2 | affinites_i | vue  | taille  | alternanceFG  | alternanceAC  | alternanceFD  |ordre_alpha
  List<int> prioritesDeTraitement = []; //valeur: priorité du traitement de l'élève | indice: élève
  List<List<String>> affiniteElevesE = []; //valeur: [<liste des noms>] | indice: élève
  List<List<String>> affiniteElevesI = []; //valeur: [<liste des noms>] | indice: élève
  int maxTolere = 0;  //niveau de correspondance
  double contrainteAct = 0;
  List<double> reussiteVariante = [];

  DatumDeClasse(this.classe);

}