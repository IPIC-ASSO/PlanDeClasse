
import 'dart:math';
import 'dart:typed_data';

import 'package:csv/csv.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:path_provider/path_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:io';
import 'dart:ui' as ui;

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
  List<String> commentaireEleves = [];
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
  int maxTolere = 9;  //niveau de correspondance
  double contrainteAct = 0;
  final monskrolleur = ScrollController();
  GlobalKey _cleGlobale = new GlobalKey();

  @override
  void initState() {
    super.initState();
    setState(() {
      pret = arbreQuiGrandit();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Plan de classe"),
        ),
        body: FutureBuilder(
            future: pret,
            builder: (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
              if (snapshot.hasData) {
                return Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
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
                            children: construitGrilleDeChange(setState),
                          )),),),
                    Padding(padding: EdgeInsets.all(5),child:ElevatedButton.icon(onPressed: (){}, style: ElevatedButton.styleFrom(padding: EdgeInsets.all(15), backgroundColor: Colors.blue), icon:const Icon(Icons.save),label: const Text("Enregistrer"))),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                      ElevatedButton.icon(onPressed: (){}, icon:const Icon(Icons.arrow_back),label: const Text("Variante Précédente"),style: ElevatedButton.styleFrom(padding: EdgeInsets.all(15)),),
                      ElevatedButton.icon(onPressed: (){}, icon:const Icon(Icons.arrow_forward),label: const Text("Variante Suivante"),style: ElevatedButton.styleFrom(padding: EdgeInsets.all(15)),),
                    ],)
                  ],
                );
              }else{
                return Center(
                  child: Column(
                    children: [
                      const Padding(padding: EdgeInsets.all(15),child:Text("Chargement...")),
                      const CircularProgressIndicator()
                    ],
                  ),
                );
              }
            }),
      drawer:Menu(widget.classe),
    );
  }

  construitGrilleDeChange(StateSetter setState) {
    final List<TableRow> mesLignes = [];
    print("configuration plane: $configurationPlane");
    print("places occupees: $placesOccupees");
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
            ((configurationPlane[maPlaceDansLaConfig]>=0 && placesOccupees[maPaceDansLeCompte]>=0)?()=>{
              montreEleve(indiceEleves[placesOccupees[maPaceDansLeCompte]],placesOccupees[maPaceDansLeCompte])
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
            child: Text((configurationPlane[maPlaceDansLaConfig]>=0 && placesOccupees[maPaceDansLeCompte]>=0)?nomsEleves[placesOccupees[maPaceDansLeCompte]]:"", style:  const TextStyle(color: Colors.black),)
          ),)
        );
      }
      mesLignes.add(TableRow(children: enfants));
    }
    return mesLignes;
  }

  Future<bool> arbreQuiGrandit() async {
    int monCompteur = 0;
    await graine();//INITIALISATION
    while(planEnregsitres.length<5 && maxTolere<10){//CALCULE
      //print("iteration $monCompteur");
      placesOccupees.clear();
      placesOccupees = new List<int>.from(placesOccupeesDebase);
      print("places occuppees: $placesOccupees");
      bool x = await TroncEtBranche(0);
      if(x){
        planEnregsitres.add(new List<int>.from(placesOccupees));
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
  }

  litBD() async {
    final Directory appDocumentsDir = await getApplicationSupportDirectory();
    File fichier = File("${appDocumentsDir.path}/donnees.csv");
    if (await fichier.exists()) {
      final csvFichier = await fichier.readAsString().onError((error, stackTrace) => (error).toString());
      donnees = const CsvToListConverter().convert(csvFichier, fieldDelimiter: ',').map((e) => e.map((f) => f.toString()).toList()).toList();
    }
  }

  litEleves() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    for (var element in (prefs.getStringList("\$liste_eleves\$${widget.classe}") ?? [])) {
      final y = prefs.getStringList(widget.classe + element) ?? ["-1", ""];
      indiceEleves.add(int.parse(y[0]));
      nomsEleves.add(element);
      commentaireEleves.add(y[1]);
      prioritesDeTraitement.add(int.parse(donnees[indiceEleves.last][13]));
      if(donnees[indiceEleves.last][2].split(";").toList()[0].length>0)affiniteElevesE.add(donnees[indiceEleves.last][2].split(";").toList());
      else{affiniteElevesE.add([]);}
      if(donnees[indiceEleves.last][3].split(";").toList()[0].length>0)affiniteElevesI.add(donnees[indiceEleves.last][3].split(";").toList());
      else{affiniteElevesI.add([]);}
    }
  }

  litConfig() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    List<String> configPS = prefs.getStringList("\$config\$${widget.classe}")??[];
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
    List<String> configPC = (prefs.getStringList("\$placement\$${widget.classe}")??[]).isEmpty?List<String>.generate(nombrePlaces, (index) => ""):prefs.getStringList("\$placement\$${widget.classe}")??[];
    placesOccupeesDebase = configPC.map((e) => nomsEleves.contains(e)?nomsEleves.indexOf(e):-1).toList();
    final coefsS = prefs.getStringList("\$critères\$${widget.classe}")??["2","1","0","0","0","0","0","1"];
    parametresPlan = coefsS.map((e) => int.parse(e)).toList();
  }

  fonctionInverse(){
    for(int indiceEleve =0; indiceEleve <affiniteElevesE.length;indiceEleve++){
      String nomDeEleve = nomsEleves[indiceEleve];
      for(String nomEnnemi in affiniteElevesE[indiceEleve]){
        if (nomEnnemi.length>0 && !affiniteElevesE[nomsEleves.indexOf(nomEnnemi)].contains(nomDeEleve))affiniteElevesE[nomsEleves.indexOf(nomEnnemi)].add(nomDeEleve);
        if(nomEnnemi.length>0 && affiniteElevesI[nomsEleves.indexOf(nomEnnemi)].contains(nomDeEleve)){
          affiniteElevesI[nomsEleves.indexOf(nomEnnemi)].remove(nomDeEleve);
          print("DONNEES CONTRADICTOIRES pour $nomDeEleve et $nomEnnemi");
        }
      }
      for(String nomAmi in affiniteElevesI[indiceEleve]){
        if (nomAmi.length>0 && !affiniteElevesI[nomsEleves.indexOf(nomAmi)].contains(nomDeEleve))affiniteElevesI[nomsEleves.indexOf(nomAmi)].add(nomDeEleve);
        //if(affiniteElevesE[nomsEleves.indexOf(nomAmi)].contains(nomDeEleve))affiniteElevesE[nomsEleves.indexOf(nomAmi)].remove(nomDeEleve);
      }

    }
    print(affiniteElevesE);
    print(nomsEleves);
  }

  Future<bool> TroncEtBranche(int indiceDeMonEleve) async {
    if(indiceDeMonEleve==indiceEleves.length) {//Tous les élèves sont placés :)
      if(!planEnregsitres.contains(placesOccupees)) return true;  //Nouvelle config
      else return false; //déjà fait
    }
      if(placesOccupees.contains(indiceDeMonEleve)) {
        double x = estimationPlacement(placesOccupees.indexOf(indiceDeMonEleve),indiceDeMonEleve);
        maxTolere = max(maxTolere,(contrainteAct+x).toInt());
        return TroncEtBranche(indiceDeMonEleve + 1); //Eleve déjà placé :)
      }
      for (int place = 0; place<placesOccupees.length;place++){ //on parcourt toutes les places de la classe
        if(placesOccupees[place]<0){ //place libre
          double poidsDeLaBranche = await CalculeLaContrainte(place,indiceDeMonEleve);
          if(contrainteAct + poidsDeLaBranche <= maxTolere){
            contrainteAct += poidsDeLaBranche;
            placesOccupees[place] = indiceDeMonEleve;
            if(await TroncEtBranche(indiceDeMonEleve + 1)){
              return true;
            }else{
              contrainteAct-=poidsDeLaBranche;
              placesOccupees[place]=-1;
            }
          }
        }
      }
      return false;
    }

  Future<double> CalculeLaContrainte(int place, int indiceDeMonEleve) async {
    print("Calcule la cintrainte ${placesOccupees.map((e) => e>-1?nomsEleves[e]:e)} $indiceDeMonEleve $place $contrainteAct");
    double maContrainte = 0;
    int importance = prioritesDeTraitement[indiceDeMonEleve] + 1;
    if(parametresPlan.last>0)maContrainte+=await OrdreAlpha(indiceDeMonEleve,place, parametresPlan.last);
    if(parametresPlan[0]>0 || parametresPlan[1]>0)maContrainte+= await affineLaFonction(indiceDeMonEleve,place, parametresPlan[0],parametresPlan[1]);
    if(parametresPlan[3]>0)maContrainte+=await laTailleCouteCher(indiceDeMonEleve,place,parametresPlan[3]);
    if(parametresPlan[4]>0 || parametresPlan[5]>0 || parametresPlan[6]>0)maContrainte+=await alternanceProfessionnelle(indiceDeMonEleve,place,parametresPlan[4],parametresPlan[5],parametresPlan[6]);
    return maContrainte*importance;
  }

  double OrdreAlpha(int indiceDeMonEleve, int place, int importanceParam) {//Le nouvel ordre est en marche
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
    print("ordreAlpha: ${nomsEleves[indiceDeMonEleve]} $indiceDeMonEleve, $place, $points, $maxTolere");
    return points;
  }

  double affineLaFonction(int indiceDeMonEleve, int place, int importanceE, int importanceI) {
    double points = 0;
    final int indiceDansConfgPlane = configurationPlane.indexOf(place);
    if (place>0 && indiceDansConfgPlane%colonne>0 && configurationPlane[indiceDansConfgPlane-1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane-1]]>=0){   //gauche
      String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane-1]]];
      print("ACTIVATION GAUCHE: $eleve");
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI;
    }
    if (place < placesOccupees.length-1 && (indiceDansConfgPlane)%colonne<colonne -1 && configurationPlane[indiceDansConfgPlane+1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane+1]]>=0){//droite
      String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane+1]]];
      print("ACTIVATION DROITE: $eleve");
      if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE;
      if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI;
    }
    print("indiceconfigplane $indiceDansConfgPlane $place");
    if(indiceDansConfgPlane-colonne>=0) { //AVANT
      int nouvPlace = configurationPlane[indiceDansConfgPlane - colonne];
      if (nouvPlace >= 0 && placesOccupees[nouvPlace] >= 0) {
        String eleve = nomsEleves[placesOccupees[nouvPlace]];
        print("ACTIVATION AVANT: $eleve");
        if (affiniteElevesE[indiceDeMonEleve].contains(eleve))
          points += importanceE / 2;
        if (affiniteElevesI[indiceDeMonEleve].contains(eleve))
          points -= importanceI / 2;
      }

      if (indiceDansConfgPlane%colonne>0 && configurationPlane[indiceDansConfgPlane-colonne -1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane-colonne -1]]>=0){   //avant - gauche
        String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane-colonne -1]]];
        print("ACTIVATION AVANT-GAUCHE: $eleve");
        if (affiniteElevesE[indiceDeMonEleve].contains(eleve))points+=importanceE/4;
        if (affiniteElevesI[indiceDeMonEleve].contains(eleve))points-=importanceI/4;
      }
      if ((indiceDansConfgPlane)%colonne<colonne -1  && configurationPlane[indiceDansConfgPlane-colonne +1] >=0 && placesOccupees[configurationPlane[indiceDansConfgPlane-colonne +1]]>=0){   //avant-droit
        String eleve = nomsEleves[placesOccupees[configurationPlane[indiceDansConfgPlane-colonne +1]]];
        print("ACTIVATION AVANT-DROIT: $eleve");
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
    print("AFFINITES: ${nomsEleves[indiceDeMonEleve]} $indiceDeMonEleve, $place, $points, $maxTolere");
    return points;
  }

  //TODO:tout devant
  double laTailleCouteCher(int indiceDeMonEleve, int place, int parametresPlan) {
    double points = 0;
    int taille = int.parse(donnees[indiceDeMonEleve][4]);
    final int indiceDansConfgPlane = configurationPlane.indexOf(place);
    for (int rang = 0; rang<indiceDansConfgPlane/colonne;rang++){ //gens devant génants
      if(indiceDansConfgPlane-rang*colonne>=0 && configurationPlane[indiceDansConfgPlane -rang*colonne]>=0 && placesOccupees[configurationPlane[indiceDansConfgPlane -rang*colonne]]>=0){//juste devant
        if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane -rang*colonne]]]][4])<taille)points+=parametresPlan/rang;
      }
      for(int decalage = 1; decalage<=rang; decalage++){
        if((indiceDansConfgPlane-decalage+1)%colonne>0 && decalage+rang*colonne<indiceDansConfgPlane && configurationPlane[indiceDansConfgPlane - decalage-rang*colonne]>-1 && placesOccupees[configurationPlane[indiceDansConfgPlane - decalage-rang*colonne]]>=0){
          print("GAUCHE $decalage: $taille contre ");
          if(int.parse(donnees[indiceEleves[placesOccupees[configurationPlane[indiceDansConfgPlane - decalage-rang*colonne]]]][4])<taille)points+=parametresPlan/(decalage*rang);
        }
        if((indiceDansConfgPlane+decalage-1)%colonne<colonne-1 && decalage-rang*colonne<configurationPlane.length && configurationPlane[indiceDansConfgPlane + decalage-rang*colonne]>-1 && placesOccupees[configurationPlane[indiceDansConfgPlane + decalage-rang*colonne]]>=0){
          print("DROITE $decalage: $taille contre ");
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
    print("TAILLE: $points");
    return points;
  }

  double alternanceProfessionnelle(int indiceDeMonEleve, int place, int importanceFG, int importanceAC, int importanceFD) {
    double points = 0;
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

  double placementFinancier(int indiceDeMonEleve, int place, int importance){
    double points = 0;
    final int indiceDansConfgPlane = configurationPlane.indexOf(place);
    for (int placeAvant = 0; placeAvant<indiceDansConfgPlane;placeAvant++){

    }

    return points;
  }

  montreEleve(int indiceElev, int foIndice) {
    String monTexte = "";
    if (indiceElev >= 0) {
      if (donnees[indiceElev][14].isNotEmpty)
        monTexte += donnees[indiceElev][14];
      if (parametresPlan[0] > 0 && affiniteElevesE[foIndice].length>0)
        monTexte += "Doit éviter: ${affiniteElevesE[foIndice]}\n";
      if (parametresPlan[1] > 0 && donnees[indiceElev][3].length>0)
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

  double estimationPlacement(int place, int indiceDeMonEleve) {
    double points = 0;
    //estimation ordre alpha
    String nomDeEleve = nomsEleves[indiceDeMonEleve];
    int nombreAvant = 0;
    int nombreApres = 0;
    for(String eleve in nomsEleves){
      if(eleve!= nomDeEleve){
        if(nomDeEleve.toLowerCase().compareTo(eleve.toLowerCase())>0){
          nombreAvant++;
        }else{
          nombreApres++;
        }
      }
    }
    int deficitAvant = nombreAvant-indiceDeMonEleve;
    int deficitApres = nombreApres-(placesOccupees.length-(place+1));
    if(deficitAvant>0){
      points+= deficitAvant * (parametresPlan.last/2);
    }
    if(deficitApres>0){
      points+= deficitApres * (parametresPlan.last/2);
    }
    return points;
  }



}