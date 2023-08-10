
import 'dart:isolate';
import 'dart:math';
import 'package:collection/collection.dart';
import 'package:csv/csv.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:plan_de_classe/datumDeClasse.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:io';
import 'menu.dart';


class AlgoContraignant extends StatefulWidget {

  final String classe;

  const AlgoContraignant({super.key, required this.classe});

  @override
  State<AlgoContraignant> createState() => _AlgoContraignantState();
}

class _AlgoContraignantState extends State<AlgoContraignant> {

  late Future<bool> pret;
  List<List<String>> donnees= [];
  List<int> indiceEleves = [];
  List<String> nomsEleves = []; //valeur:nom | indice:élève
  List<int> configurationPlane = [];
  int colonne = 0;
  List<int> placesOccupeesDebase = [];
  List<int> placesOccupees = []; //valeur: indice de l'élève | indice: place | -1 si vide
  List<List<int>> planEnregsitres = [];
  //List<int> indicePlaceDansEleves = [];//valeur: indice de la place | indice: élève | -1 si vide
  List<int> parametresPlan = [];  //affinites_e = 2 | affinites_i | vue  | taille  | alternanceFG  | alternanceAC  | alternanceFD  |ordre_alpha
  List<int> prioritesDeTraitement = []; //valeur: priorité du traitement de l'élève | indice: élève
  List<List<String>> affiniteElevesE = []; //valeur: [<liste des noms>] | indice: élève
  List<List<String>> affiniteElevesI = []; //valeur: [<liste des noms>] | indice: élève
  int maxTolere = 10;  //niveau de correspondance
  double contrainteAct = 0;
  List<double> reussiteVariante = [];
  late Isolate isolat;
  final monskrolleur = ScrollController();
  final GlobalKey _cleGlobale = GlobalKey();
  int variante = 0;

  @override
  void initState() {
    super.initState();
    setState(() {
      pret = calculus();
    });

  }

  Future<bool> calculus() async {
    DatumDeClasse monDatum = await _spawnAndReceive();
    planEnregsitres = monDatum.planEnregsitres;
    reussiteVariante = monDatum.reussiteVariante;
    return true;
  }

  Future<DatumDeClasse> _spawnAndReceive() async {
    final resultPort = ReceivePort();
    DatumDeClasse datum  = await graine(DatumDeClasse(widget.classe));
    isolat = await Isolate.spawn(arbreQuiGrandit, [resultPort.sendPort, datum]);
    resultPort.listen((message) {setState(() {
      pret = Future(() => false);
      if (message.runtimeType == int) maxTolere = message;
    });});
    return (await resultPort.first) as DatumDeClasse;
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: ()async{
        if(isolat!=null)isolat.kill();
        return true;
      },
        child:Scaffold(
        appBar: AppBar(
          title: const Text("Plan de classe"),
        ),
        body: FutureBuilder(
            future: pret,
            builder: (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
              print("ATTTENTTTTTION ${snapshot.data} $maxTolere");
              if (snapshot.hasData && snapshot.data == true) {
                return Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Padding(padding: const EdgeInsets.all(15),child: Text("Variante ${variante + 1}/${planEnregsitres.length} \nCorrespondance: ${max(100-reussiteVariante[variante],10)}%", textAlign: TextAlign.center,)),
                    Padding(padding: const EdgeInsets.all(15),child: LinearProgressIndicator(value: max(100-reussiteVariante[variante],10),color: [Colors.green,Colors.lightGreenAccent,Colors.orange,Colors.red][min(reussiteVariante[variante]~/25,3)],)),
                    RepaintBoundary(
                      key: _cleGlobale,
                      child:
                    Scrollbar(
                      thumbVisibility: true,
                      controller: monskrolleur,
                      child:SingleChildScrollView(
                          controller: monskrolleur,
                          scrollDirection: Axis.horizontal,
                          child:Table(
                            defaultColumnWidth: const FixedColumnWidth(100),
                            children: construitGrilleDeChange(setState, planEnregsitres[variante]),
                          )),),),
                    Padding(padding: const EdgeInsets.all(5),child:ElevatedButton.icon(onPressed: (){}, style: ElevatedButton.styleFrom(padding: const EdgeInsets.all(15), backgroundColor: Colors.blue), icon:const Icon(Icons.save),label: const Text("Enregistrer"))),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Visibility(
                          visible: variante>0,
                          child: ElevatedButton.icon(onPressed: (){setState(() {
                            variante-=1;
                          });}, icon:const Icon(Icons.arrow_back),label: const Text("Variante Précédente"),style: ElevatedButton.styleFrom(padding: const EdgeInsets.all(15)),),),
                        Visibility(
                          visible: variante<planEnregsitres.length-1,
                          child:ElevatedButton.icon(onPressed: (){setState(() {
                            variante++;
                          });}, icon:const Icon(Icons.arrow_forward),label: const Text("Variante Suivante"),style: ElevatedButton.styleFrom(padding: const EdgeInsets.all(15)),),)
                    ],)
                  ],
                );
              }else{
                return Center(
                  child: Column(
                    children:  [
                      Padding(padding: EdgeInsets.all(15),child:Text("Chargement... taux de correspondance: ${100-maxTolere}%")),
                      const CircularProgressIndicator()
                    ],
                  ),
                );
              }
            }),
      drawer:Menu(widget.classe),
        ));
  }

  construitGrilleDeChange(StateSetter setState, List<int> placeAvecDesGens) {
    final List<TableRow> mesLignes = [];
    final int lignes = ((configurationPlane.length)~/colonne);
    int comptePlaces = -1;
    for (int lin = 0; lin<lignes; lin++){
      final List<TableCell>enfants = [];
      for (int col = 0; col<colonne; col++){
        final maPlaceDansLaConfig = colonne*lin+col;
        if(configurationPlane[maPlaceDansLaConfig]>=0)comptePlaces++;
        final maPaceDansLeCompte = comptePlaces;
        enfants.add(
          TableCell(child:
          ElevatedButton(
            onPressed:
            configurationPlane[maPlaceDansLaConfig]>=0?
            ((configurationPlane[maPlaceDansLaConfig]>=0 && placeAvecDesGens[maPaceDansLeCompte]>=0)?()=>{
              montreEleve(indiceEleves[placeAvecDesGens[maPaceDansLeCompte]],placeAvecDesGens[maPaceDansLeCompte])
            }
              :
            ()=> {montreEleve(-1,-1)})
                :null,
            style:ElevatedButton.styleFrom(
              backgroundColor: configurationPlane[maPlaceDansLaConfig]>=0?Colors.white:Colors.grey,
              side: BorderSide(
                color: Colors.black,
                width: configurationPlane[maPlaceDansLaConfig]>=0?2.0:1.0,
              ),// Background color
            ),
            child: Text((configurationPlane[maPlaceDansLaConfig]>=0 && placeAvecDesGens[maPaceDansLeCompte]>=0)?nomsEleves[placeAvecDesGens[maPaceDansLeCompte]]:"", style:  const TextStyle(color: Colors.black),)
          ),)
        );
      }
      mesLignes.add(TableRow(children: enfants));
    }
    return mesLignes;
  }

  montreEleve(int indiceElev, int foIndice) {
    String monTexte = "";
    if (indiceElev >= 0) {
      if (donnees[indiceElev][14].isNotEmpty)
        monTexte += donnees[indiceElev][14];
      if (parametresPlan[0] > 0 && affiniteElevesE[foIndice].isNotEmpty)
        monTexte += "Doit éviter: ${affiniteElevesE[foIndice]}\n";
      if (parametresPlan[1] > 0 && donnees[indiceElev][3].isNotEmpty)
        monTexte += "Doit se rapprocher de: ${donnees[indiceElev][3]} \n";
      if (parametresPlan[2] > 0) monTexte +=
      "A une vue: ${["Bonne", "Moyenne", "Mauvaise"][int.parse(
          donnees[indiceElev][5])]} \n";
      if (parametresPlan[3] > 0) monTexte +=
      "Est de ${["grande taille", "moyenne taille", "petite taille "][int.parse(
          donnees[indiceElev][4])]}\n";
      if (parametresPlan[4] > 0) monTexte +=
      "Est de sexe ${["masculin", "féminin"][int.parse(
          donnees[indiceElev][9])]}\n";
      if (parametresPlan[5] > 0) monTexte += "Est habituellement: ${[
        "agité",
        "dans la moyenne (attitude)",
        "calme"
      ][int.parse(donnees[indiceElev][8])]}\n";
      if (parametresPlan[6] > 0) monTexte += "En classe, ${[
        "est à l'aise",
        "a un niveau dans la moyenne",
        "est en difficultés"
      ][int.parse(donnees[indiceElev][7])]}\n";
      monTexte += "Cet élève doit être traité avec un niveau de priorité ${[
        "normal",
        "supérieur",
        "maximal"
      ][int.parse(donnees[indiceElev][13])]}";
    }else{
      monTexte = "Cette place est vide.";
    }

    return showDialog<void>(
        context: context,
        barrierDismissible: true,
        builder: (BuildContext context) {
          return AlertDialog(
              title: Text(indiceElev>=0?donnees[indiceElev][1]:"Détails de la place"),
              content: Text(monTexte)
          );
        });
  }


  Future<DatumDeClasse> graine(DatumDeClasse datum) async {
    donnees = await litBD();
    datum.donnees = donnees;
    datum = await litEleves(datum);
    datum = await litConfig(datum);
    datum = await fonctionInverse(datum);
    print("Places occuppées au départ: ${datum.placesOccupees}");
    return datum;
  }

  litBD() async {
    final Directory appDocumentsDir = await getApplicationSupportDirectory();
    File fichier = File("${appDocumentsDir.path}/donnees.csv");
    List<List<String>> donnees = [];
    if (await fichier.exists()) {
      final csvFichier = await fichier.readAsString().onError((error, stackTrace) => (error).toString());
      donnees = const CsvToListConverter().convert(csvFichier, fieldDelimiter: ',').map((e) => e.map((f) => f.toString()).toList()).toList();
    }else{
      print("pas de donnees");
    }
    return donnees;
  }

  litEleves(DatumDeClasse datum) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    for (var element in (prefs.getStringList("\$liste_eleves\$${datum.classe}") ?? [])) {
      final y = prefs.getStringList(datum.classe + element) ?? ["-1", ""];
      indiceEleves.add(int.parse(y[0]));
      nomsEleves.add(element);
      datum.prioritesDeTraitement.add(int.parse(datum.donnees[indiceEleves.last][13]));
      if(donnees[indiceEleves.last][2].split(";").toList()[0].isNotEmpty)affiniteElevesE.add(donnees[indiceEleves.last][2].split(";").toList());
      else{affiniteElevesE.add([]);}
      if(donnees[indiceEleves.last][3].split(";").toList()[0].isNotEmpty)affiniteElevesI.add(donnees[indiceEleves.last][3].split(";").toList());
      else{affiniteElevesI.add([]);}
    }
    datum.nomsEleves = nomsEleves;
    datum.indiceEleves = indiceEleves;
    return datum;
  }

  litConfig(DatumDeClasse datum) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    List<String> configPS = prefs.getStringList("\$config\$${datum.classe}")??[];
    configurationPlane = configPS.map((e) => int.parse(e)).toList();
    colonne = configurationPlane.last;
    configurationPlane.removeLast();
    configurationPlane = configurationPlane.map((e) => e==0?-1:e).toList();
    int nombrePlaces = 0;
    for (int i =0; i<configurationPlane.length;i++) {
      if(configurationPlane[i]==1){
        configurationPlane[i] = nombrePlaces;
        nombrePlaces++;
      }
    }
    List<String> configPC = (prefs.getStringList("\$placement\$${datum.classe}")??[]).isEmpty?List<String>.generate(nombrePlaces, (index) => ""):prefs.getStringList("\$placement\$${datum.classe}")??[];
    List<int> placesOccupeesDebase = configPC.map((e) => datum.nomsEleves.contains(e)?datum.nomsEleves.indexOf(e):-1).toList();
    final coefsS = prefs.getStringList("\$critères\$${datum.classe}")??["2","1","0","0","0","0","0","1"];
    parametresPlan = coefsS.map((e) => int.parse(e)).toList();
    datum.configurationPlane = configurationPlane;
    datum.colonne = colonne;
    datum.placesOccupeesDebase = placesOccupeesDebase;
    datum.parametresPlan = parametresPlan;
    return datum;
  }

  fonctionInverse(DatumDeClasse datum){
    for(int indiceEleve =0; indiceEleve <affiniteElevesE.length;indiceEleve++){
      String nomDeEleve = nomsEleves[indiceEleve];
      for(String nomEnnemi in affiniteElevesE[indiceEleve]){
        if (nomEnnemi.isNotEmpty && !affiniteElevesE[nomsEleves.indexOf(nomEnnemi)].contains(nomDeEleve))affiniteElevesE[nomsEleves.indexOf(nomEnnemi)].add(nomDeEleve);
        if(nomEnnemi.isNotEmpty && affiniteElevesI[nomsEleves.indexOf(nomEnnemi)].contains(nomDeEleve)){
          affiniteElevesI[nomsEleves.indexOf(nomEnnemi)].remove(nomDeEleve);
          print("DONNEES CONTRADICTOIRES pour $nomDeEleve et $nomEnnemi");
        }
      }
      for(String nomAmi in affiniteElevesI[indiceEleve]){
        if (nomAmi.isNotEmpty && !affiniteElevesI[nomsEleves.indexOf(nomAmi)].contains(nomDeEleve))affiniteElevesI[nomsEleves.indexOf(nomAmi)].add(nomDeEleve);
        //if(affiniteElevesE[nomsEleves.indexOf(nomAmi)].contains(nomDeEleve))affiniteElevesE[nomsEleves.indexOf(nomAmi)].remove(nomDeEleve);
      }
    }
    datum.affiniteElevesE = affiniteElevesE;
    datum.affiniteElevesI = affiniteElevesI;
    return datum;
  }

  /*Future<bool> arbreQuiGrandit() async {
    await graine();//INITIALISATION
    while(planEnregsitres.length<5 && maxTolere<10){//CALCULE
      //print("iteration $monCompteur");
      placesOccupees.clear();
      placesOccupees = List<int>.from(placesOccupeesDebase);
      print("places occuppees: $placesOccupees");
      bool x = await TroncEtBranche(0);
      if(x){
        planEnregsitres.add(List<int>.from(placesOccupees));
      }else{
       maxTolere++;
      }
    }
    print("Liste des plans $planEnregsitres");
    print("fin");
    return true;
  }

  Future<void> graine() async {
    await litBD();
    await litEleves();
    await litConfig();
    await fonctionInverse();
    print("Places occuppées au départ: $placesOccupeesDebase");
  }*/


  EnregistrePlan(){
    
  }
}

Future<bool> arbreQuiGrandit(List<dynamic> args) async {
  final SendPort portMarchand = args[0];
  DatumDeClasse monDatumDeClasse = args[1]; //INITIALISATION

  while (monDatumDeClasse.planEnregsitres.length < 5 && monDatumDeClasse.maxTolere < 60) { //CALCULE
    portMarchand.send(monDatumDeClasse.maxTolere);
    monDatumDeClasse.placesOccupees.clear();
    monDatumDeClasse.placesOccupees = List<int>.from(monDatumDeClasse.placesOccupeesDebase);
    bool x = await TroncEtBranche(0, monDatumDeClasse);
    if (x) {
      monDatumDeClasse.planEnregsitres.add(List<int>.from(monDatumDeClasse.placesOccupees));
    } else {
      monDatumDeClasse.maxTolere++;
    }
  }
  print("Liste des plans ${monDatumDeClasse.planEnregsitres}");
  print("fin");
  Isolate.exit(args[0], monDatumDeClasse);
}

Future<bool> TroncEtBranche(int indiceDeMonEleve, DatumDeClasse datum) async {
  if(indiceDeMonEleve==datum.indiceEleves.length) {//Tous les élèves sont placés :)
    Function eg = const ListEquality().equals;
    for(List<int> uneConfig in datum.planEnregsitres){
      if(eg(uneConfig,datum.placesOccupees))return false;
    }
    datum.reussiteVariante.add(datum.contrainteAct);
    return true;  //Nouvelle config
  }
  if(datum.placesOccupees.contains(indiceDeMonEleve)) {
    double x = estimationPlacement(datum.placesOccupees.indexOf(indiceDeMonEleve),indiceDeMonEleve, datum);
    datum.maxTolere = max(datum.maxTolere,(datum.contrainteAct+x).toInt());
    return TroncEtBranche(indiceDeMonEleve + 1, datum); //Eleve déjà placé :)
  }
  for (int place = 0; place<datum.placesOccupees.length;place++){ //on parcourt toutes les places de la classe
    if(datum.placesOccupees[place]<0){ //place libre
      double poidsDeLaBranche = await CalculeLaContrainte(place,indiceDeMonEleve, datum);
      if(datum.contrainteAct + poidsDeLaBranche <= datum.maxTolere){
        datum.contrainteAct += poidsDeLaBranche;
        datum.placesOccupees[place] = indiceDeMonEleve;
        if(await TroncEtBranche(indiceDeMonEleve + 1,datum)){
          return true;
        }else{
          datum.contrainteAct-=poidsDeLaBranche;
          datum.placesOccupees[place]=-1;
        }
      }
    }
  }
  return false;
}

Future<double> CalculeLaContrainte(int place, int indiceDeMonEleve, DatumDeClasse datum) async {
  List<int> placesOccupees = datum.placesOccupees;
  List<String> nomsEleves = datum.nomsEleves;
  //print("Calcule la cintrainte ${placesOccupees.map((e) => e>-1?nomsEleves[e]:e)} $indiceDeMonEleve $place ${datum.contrainteAct}");
  double maContrainte = 0;
  int importance = datum.prioritesDeTraitement[indiceDeMonEleve] + 1;
  List<int> parametresPlan = datum.parametresPlan;
  if(parametresPlan.last>0)maContrainte+=OrdreAlpha(indiceDeMonEleve,place, parametresPlan.last, placesOccupees, nomsEleves);
  if(parametresPlan[0]>0 || parametresPlan[1]>0)maContrainte+= affineLaFonction(indiceDeMonEleve,place, parametresPlan[0],parametresPlan[1], datum, placesOccupees, nomsEleves);
  if(parametresPlan[3]>0)maContrainte+=laTailleCouteCher(indiceDeMonEleve,place,parametresPlan[3], datum, placesOccupees);
  if(parametresPlan[4]>0 || parametresPlan[5]>0 || parametresPlan[6]>0)maContrainte+=alternanceProfessionnelle(indiceDeMonEleve,place,parametresPlan[4],parametresPlan[5],parametresPlan[6],datum, placesOccupees);
  return maContrainte*importance;
}

double OrdreAlpha(int indiceDeMonEleve, int place, int importanceParam, List<int> placesOccupees, List<String> nomsEleves) {//Le nouvel ordre est en marche
  double points = 0;
  int petiIndice = place-1;
  while(petiIndice>0 && placesOccupees[petiIndice]==-1){
    petiIndice-=1;
  }
  if ( place>0 && placesOccupees[petiIndice]!=-1 && (nomsEleves[indiceDeMonEleve]).toLowerCase().compareTo(nomsEleves[placesOccupees[petiIndice]].toLowerCase())<0){
    points += importanceParam/2;
  }
  petiIndice = place + 1;
  while(petiIndice<placesOccupees.length-1 && placesOccupees[petiIndice]==-1){
    petiIndice+=1;
  }
  if (place!= placesOccupees.length-1 && placesOccupees[petiIndice]!=-1 && (nomsEleves[indiceDeMonEleve]).toLowerCase().compareTo(nomsEleves[placesOccupees[petiIndice]].toLowerCase())>0)
    points+=importanceParam/2;
  //print("ordreAlpha: ${nomsEleves[indiceDeMonEleve]} $indiceDeMonEleve, $place, $points");
  return points;
}

double affineLaFonction(int indiceDeMonEleve, int place, int importanceE, int importanceI, DatumDeClasse datum, List<int> placesOccupees, List<String> nomsEleves) {
  List<int> configurationPlane = datum.configurationPlane;
  int colonne = datum.colonne;
  List<List<String>> affiniteElevesE = datum.affiniteElevesE;
  List<List<String>> affiniteElevesI = datum.affiniteElevesI;
  double points = 0;
  final int indiceDansConfgPlane = configurationPlane.indexOf(place);
  if (place>0 && indiceDansConfgPlane%colonne>0 && configurationPlane[indiceDansConfgPlane-1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane-1]]>=0){   //gauche
    String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane-1]]];
    if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE;
    if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI;
  }
  if (place < placesOccupees.length-1 && (indiceDansConfgPlane)%colonne<colonne -1 && configurationPlane[indiceDansConfgPlane+1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane+1]]>=0){//droite
    String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane+1]]];
    if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE;
    if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI;
  }
  if(indiceDansConfgPlane-colonne>=0) { //AVANT
    int nouvPlace = configurationPlane[indiceDansConfgPlane - colonne];
    if (nouvPlace >= 0 && placesOccupees[nouvPlace] >= 0) {
      String eleve = nomsEleves[placesOccupees[nouvPlace]];
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))
        points += importanceE / 2;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))
        points -= importanceI / 2;
    }

    if (indiceDansConfgPlane%colonne>0 && configurationPlane[indiceDansConfgPlane-colonne -1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane-colonne -1]]>=0){   //avant - gauche
      String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane-colonne -1]]];
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE/4;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI/4;
    }
    if ((indiceDansConfgPlane)%colonne<colonne -1  && configurationPlane[indiceDansConfgPlane-colonne +1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane-colonne +1]]>=0){   //avant-droit
      String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane-colonne +1]]];
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE/4;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI/4;
    }

  }
  if(indiceDansConfgPlane+colonne<configurationPlane.length){//ARRIERE
    int nouvPlace = configurationPlane[indiceDansConfgPlane+colonne];
    if(nouvPlace >= 0 && placesOccupees[nouvPlace]>=0){
      String eleve = nomsEleves[placesOccupees[nouvPlace]];
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE/2;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI/2;
    }
    if (indiceDansConfgPlane%colonne>0 && configurationPlane[indiceDansConfgPlane+colonne -1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane+colonne -1]]>=0){   //arrière - gauche
      String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane+colonne -1]]];
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE/4;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI/4;
    }
    if ((indiceDansConfgPlane)%colonne<colonne -1  && configurationPlane[indiceDansConfgPlane+colonne +1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane+colonne +1]]>=0){   //avant-droit
      String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane+colonne +1]]];
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE/4;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI/4;
    }
  }
  return points;
}

double laTailleCouteCher(int indiceDeMonEleve, int place, int parametresPlan, DatumDeClasse datum, List<int> placesOccupees) {
  double points = 0;
  List<List<String>> donnees = datum.donnees;
  List<int> configurationPlane = datum.configurationPlane;
  int colonne = datum.colonne;
  List<int> indiceEleves = datum.indiceEleves;
  int taille = int.parse(donnees[indiceDeMonEleve][4]);
  final int indiceDansConfgPlane = configurationPlane.indexOf(place);
  for (int rang = 0; rang<indiceDansConfgPlane/colonne;rang++){ //gens devant génants
    if(indiceDansConfgPlane-rang*colonne>=0 && configurationPlane[indiceDansConfgPlane -rang*colonne]>=0 && placesOccupees[configurationPlane[indiceDansConfgPlane -rang*colonne]]>=0){//juste devant
      if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane -rang*colonne]]]][4])<taille)points+=parametresPlan/rang;
    }
    for(int decalage = 1; decalage<=rang; decalage++){
      if((indiceDansConfgPlane-decalage+1)%colonne>0 && decalage+rang*colonne<indiceDansConfgPlane && configurationPlane[indiceDansConfgPlane - decalage-rang*colonne]>-1 && placesOccupees[configurationPlane[indiceDansConfgPlane - decalage-rang*colonne]]>=0){
        if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane - decalage-rang*colonne]]]][4])<taille)points+=parametresPlan/(decalage*rang);
      }
      if((indiceDansConfgPlane+decalage-1)%colonne<colonne-1 && decalage-rang*colonne<configurationPlane.length && configurationPlane[indiceDansConfgPlane + decalage-rang*colonne]>-1 && placesOccupees[configurationPlane[indiceDansConfgPlane + decalage-rang*colonne]]>=0){
        if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane + decalage-rang*colonne]]]][4])<taille)points+=parametresPlan/(decalage*rang);
      }
    }
  }
  for (int rang = 0; rang<(configurationPlane.length-indiceDansConfgPlane)/colonne;rang++){ //gens derrière génés
    if(indiceDansConfgPlane+rang*colonne<configurationPlane.length && configurationPlane[indiceDansConfgPlane +rang*colonne]>=0 && placesOccupees[configurationPlane[indiceDansConfgPlane +rang*colonne]]>=0){//juste derrière
      if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane +rang*colonne]]]][4])<taille)points+=parametresPlan/rang;
    }
    for(int decalage = 1; decalage<=rang; decalage++){
      if((indiceDansConfgPlane-decalage+1)%colonne>0 && indiceDansConfgPlane - decalage+rang*colonne>=0 && configurationPlane[indiceDansConfgPlane - decalage+rang*colonne]>-1 && placesOccupees[configurationPlane[indiceDansConfgPlane - decalage+rang*colonne]]>=0){
        if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane - decalage+rang*colonne]]]][4])>taille)points+=parametresPlan/decalage;
      }
      if((indiceDansConfgPlane+decalage-1)%colonne<colonne-1 && indiceDansConfgPlane + decalage+rang*colonne<configurationPlane.length && indiceDansConfgPlane + decalage+rang*colonne<configurationPlane.length && configurationPlane[indiceDansConfgPlane + decalage+rang*colonne]>-1 && placesOccupees[configurationPlane[indiceDansConfgPlane + decalage+rang*colonne]]>=0){
        if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane + decalage+rang*colonne]]]][4])>taille)points+=parametresPlan/decalage;
      }
    }
  }
  return points;
}

double alternanceProfessionnelle(int indiceDeMonEleve, int place, int importanceFG, int importanceAC, int importanceFD, DatumDeClasse datum, List<int> placesOccupees) {
  double points = 0;
  List<List<String>> donnees = datum.donnees;
  List<int> configurationPlane = datum.configurationPlane;
  int colonne = datum.colonne;
  List<int> indiceEleves = datum.indiceEleves;
  final int indiceDansConfgPlane = configurationPlane.indexOf(place);
  if (indiceDansConfgPlane%colonne>0 && configurationPlane[indiceDansConfgPlane-1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane-1]]>=0) { //gauche
    int indiceVoisin = indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane-1]]];
    if(donnees[indiceVoisin][9]==donnees[indiceEleves[indiceDeMonEleve]][9])points+=importanceFG/2;   //Fille/garçon
    points += (importanceAC/2)*(((int.parse(donnees[indiceVoisin][8])-int.parse(donnees[indiceEleves[indiceDeMonEleve]][8])).abs())-1)*-1; //Agité/calme
    points += (importanceFD/2)*(((int.parse(donnees[indiceVoisin][7])-int.parse(donnees[indiceEleves[indiceDeMonEleve]][7])).abs())-1)*-1; //Fort/Difficultés
  }
  if (place < placesOccupees.length-1 && (indiceDansConfgPlane)%colonne<colonne -1 && configurationPlane[indiceDansConfgPlane+1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane+1]]>=0){//droite
    int indiceVoisin = indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane+1]]];
    if(donnees[indiceVoisin][9]==donnees[indiceEleves[indiceDeMonEleve]][9])points+=importanceFG/2;   //Fille/garçon
    points += (importanceAC/2)*(((int.parse(donnees[indiceVoisin][8])-int.parse(donnees[indiceEleves[indiceDeMonEleve]][8])).abs())-1)*-1; //Agité/calme
    points += (importanceFD/2)*(((int.parse(donnees[indiceVoisin][7])-int.parse(donnees[indiceEleves[indiceDeMonEleve]][7])).abs())-1)*-1; //Fort/Difficultés
  }
  return points;
}

/*double placementFinancier(int indiceDeMonEleve, int place, int importance){
  double points = 0;
  final int indiceDansConfgPlane = configurationPlane.indexOf(place);
  for (int placeAvant = 0; placeAvant<indiceDansConfgPlane;placeAvant++){

  }
  return points;
}*/

double estimationPlacement(int place, int indiceDeMonEleve, DatumDeClasse datum) {
  double points = 0;
  //estimation ordre alpha
  String nomDeEleve = datum.nomsEleves[indiceDeMonEleve];
  int nombreAvant = 0;
  int nombreApres = 0;
  for(String eleve in datum.nomsEleves){
    if(eleve!= nomDeEleve){
      if(nomDeEleve.toLowerCase().compareTo(eleve.toLowerCase())>0){
        nombreAvant++;
      }else{
        nombreApres++;
      }
    }
  }
  int deficitAvant = nombreAvant-indiceDeMonEleve;
  int deficitApres = nombreApres-(datum.placesOccupees.length-(place+1));
  if(deficitAvant>0){
    points+= deficitAvant * (datum.parametresPlan.last/2);
  }
  if(deficitApres>0){
    points+= deficitApres * (datum.parametresPlan.last/2);
  }
  return points;
}