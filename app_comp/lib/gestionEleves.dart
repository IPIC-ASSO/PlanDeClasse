import 'package:csv/csv.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:plan_de_classe/AlgoContraignant.dart';
import 'package:plan_de_classe/profilEleve.dart';
import 'package:plan_de_classe/usineDeBiscottesGrillees.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:io';

import 'menu.dart';

class GestionEleves extends StatefulWidget {

  final String classe;

  const GestionEleves({super.key, required this.classe});

  @override
  State<GestionEleves> createState() => _GestionElevesState();
}

class _GestionElevesState extends State<GestionEleves> with TickerProviderStateMixin{

  late Future<Map<String, List<dynamic>>> eleves; //clé: nom | valeur: indice;commentaire
  late List<List<String>> donnees;
  int compteElevesRemplis = 0;
  late TabController controleTable;
  List<List<String>> textesCriteres = [["Devant","Au fond", "Sans importance"],["Grand","Moyen","Petit"],["Bonne","Moyenne","Mauvaise"],["Garçon","Fille"],["Agité","Dans la moyenne","Calme"],["A l'aise","Dans la moyenne","En difficultés"]];
  List<String> criteresDeBase = ["Placement","Affinités (éloigner)", "Affinités (rapprocher)", "Taille", "Vue", "Genre", "Attitude", "Niveau", "Importance"];
  List<int> controlesCritereDebase = [2,1,1,0,1,1,1];
  List<List<int>> controlesCritere = [];
  Map<String,Map<String,List<bool>>> affinitesMesEleves = {}; //Nom_eleve:chaque_autre:{aime,aime pas}}
  List<String> criteres = [];
  int indiceCriter = 0;

  @override
  void initState() {
    super.initState();
    criteres = List<String>.from(criteresDeBase);
    setState(() {
      donnees = [];
      eleves = litEleves();
    });
    controleTable = TabController(length: 2, vsync: this);
    verifCritair();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Paramétrage des élèves"),
          actions: [
            IconButton(onPressed: ()=>{montrePropos(context, 2)},
                icon: const Icon(Icons.info_outline))
          ],
            bottom: TabBar(
                isScrollable: true,
                controller: controleTable,
                tabs: const [
                  Tab(icon: Icon(Icons.people),
                      child: Text("Par élève", textAlign: TextAlign.center,)),
                  Tab(icon: Icon(Icons.settings_input_composite_outlined),
                      child: Text("Par critère", textAlign: TextAlign.center,)),
                ]
            )
        ),
        body: FutureBuilder(
            future: eleves,
            builder: (context, snapshot) {
              if (!snapshot.hasData || snapshot.data == null || snapshot.data!.isEmpty) {
                return const Center(child: Text(
                  "Aucun élève disponible.", textAlign: TextAlign.center,),);
              } else {
                List<Widget> mesBeauxEleves = [
                  const Padding(padding: EdgeInsets.all(5),
                      child: Text(
                        "Il n'est pas nécessaire de paramétrer tous les élèves\nN'oubliez d'enregistrer vos modifications avant de changer d'onglet ou de page.", textAlign: TextAlign.center,style: TextStyle(fontStyle: FontStyle.italic),)),
                ];
                List<Widget> mesMochesEleves = List<Widget>.from(mesBeauxEleves);
                int compte=0;
                for (MapEntry<String, List<dynamic>> eleve in snapshot.data!
                    .entries) {
                  mesBeauxEleves.add(profilEleve(eleve.key, eleve.value[1], eleve.value[0]));
                  mesMochesEleves.add(faceEleve(compte,eleve.key,eleve.value[0]));
                  compte++;
                }
                return Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children:[
                        Tooltip(
                          triggerMode: TooltipTriggerMode.tap,
                          showDuration: const Duration(seconds: 2),
                          message: compteElevesRemplis>2?'Étape finale':'Paramétrez davantage d\'élèves',
                          child: Padding(
                          padding: const EdgeInsets.all(10),
                          child: ElevatedButton.icon(
                            onPressed: compteElevesRemplis<3?null:()async{
                              await finitEleves();
                              await Enregistre(context, true);
                              Navigator.push(context,
                                PageRouteBuilder(
                                  pageBuilder: (_, __, ___) => AlgoContraignant(classe: widget.classe,),
                                  transitionDuration: const Duration(milliseconds: 500),
                                  transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                                ),
                              );
                            },
                            label: const Text('Créer un plan de classe'),
                            icon: const Icon(Icons.oil_barrel_rounded),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFF3086E8),
                              minimumSize:Size(MediaQuery.of(context).size.width/(MediaQuery.of(context).size.aspectRatio>1?2:1),50),
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(10.0)
                              ),
                            ),
                          ),
                        ),
                      ),
                      Padding(
                        padding: EdgeInsets.all(10),
                        child:Container(
                          padding: const EdgeInsets.all(2),
                          decoration: BoxDecoration(
                            color: Colors.green,
                              borderRadius: BorderRadius.all(Radius.circular(10))
                          ),
                          child: IconButton(
                            onPressed: (){
                              Enregistre(context);
                            },
                            icon: const Icon(Icons.save),
                            tooltip: "Enregistrer",
                            style: IconButton.styleFrom(


                            ),
                          ),
                        )
                      )]
                  ),
                  Expanded(
                    child:TabBarView(
                      controller: controleTable,
                      children: [
                        ListView(
                          shrinkWrap: true,
                          children: mesBeauxEleves,
                        ),
                        Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Row(
                              children: [
                                Expanded(flex:0,
                                  child: Padding(
                                    padding: const EdgeInsets.all(10),
                                    child: IconButton(
                                      onPressed: indiceCriter==0?null: (){
                                            setState(() {
                                              indiceCriter -= 1;
                                            });},
                                      icon: const Icon(Icons.arrow_back_ios)),
                                  )),
                                Expanded(flex:1,
                                  child: Padding(
                                    padding: const EdgeInsets.all(10),
                                    child: Text(criteres[indiceCriter], textAlign: TextAlign.center,style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),),
                                  )),
                                Expanded(flex:0,
                                  child: Padding(
                                    padding: const EdgeInsets.all(10),
                                    child: IconButton(onPressed: indiceCriter==criteres.length-1?null: (){
                                      setState(() {
                                        indiceCriter++;
                                      });}, icon: const Icon(Icons.arrow_forward_ios)),
                                  ))
                              ],
                            ),Expanded(child:
                            ListView(
                              physics: ScrollPhysics(),
                              shrinkWrap: true,
                              children: mesMochesEleves,
                            ),)
                          ],
                        )
                      ]
                    )),
              ]);
              }
            }),
      drawer:Menu(widget.classe),
    );
  }

  Future<Map<String, List<dynamic>>> litEleves() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final Map<String, List<dynamic>> x = {};
    for (var element in (prefs.getStringList("\$liste_eleves\$${widget.classe}") ?? [])) {
      final y = prefs.getStringList(widget.classe + element) ?? ["-1", ""];
      x[element] = [int.parse(y[0]), y[1]];
      if(int.parse(y[0])>=0)compteElevesRemplis++;
    }
    setState(() {
      compteElevesRemplis;
    });
    await litBD();
    await metAJourValeur(x);
    return x;
  }

  Widget profilEleve(String nom, String commentaire, int indice) {
    return GestureDetector(
        onTap: () =>
        {Navigator.push(context,
          PageRouteBuilder(
            pageBuilder: (_, __, ___) => ProfilEleve(classe: widget.classe,
                eleve: nom,
                donnees: donnees,
                indice: indice),
            transitionDuration: const Duration(milliseconds: 500),
            transitionsBuilder: (_, a, __, c) =>
                FadeTransition(opacity: a, child: c),
          ),
        )},
        child: Padding(padding: const EdgeInsets.all(5), child: Container(
          padding: const EdgeInsets.all(5),
          color: indice >= 0 ? Colors.grey[350] : Colors.lightBlueAccent,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Expanded(flex: 0,
                  child: Padding(padding: EdgeInsets.symmetric(horizontal: 10),
                      child: Icon(Icons.account_circle))),
              Expanded(flex: 1, child: Text(nom,)),
              Expanded(flex: 1,
                  child: Text(commentaire,
                      style: const TextStyle(fontStyle: FontStyle.italic))),
              Expanded(flex: 0, child:
              Icon(
                Icons.adjust,
                color:choisiCouleur(indice),
              ),
              ),
            ],
          ),
        )));
  }

  litBD() async {
    final Directory appDocumentsDir = await getApplicationSupportDirectory();
    File fichier = File("${appDocumentsDir.path}/donnees.csv");
    if (await fichier.exists()) {
      final csvFichier = await fichier.readAsString().onError((error, stackTrace) => (error).toString());
      donnees = const CsvToListConverter().convert(csvFichier, fieldDelimiter: ',').map((e) => e.map((f) => f.toString()).toList()).toList();
    }
  }

  metAJourValeur(Map<String, List<dynamic>> mesElv) {
    controlesCritereDebase.forEach((element) {
      controlesCritere.add(List<int>.generate(mesElv.entries.length, (index) => element));
    });

    for(int i =0; i< mesElv.entries.length;i++){
      int indiceDb = mesElv.entries.toList()[i].value[0];
      String nomElv = mesElv.entries.toList()[i].key;
      if(indiceDb >= 0){
        controlesCritere[0][i] = int.parse(donnees[indiceDb][6]);
        controlesCritere[1][i] = int.parse(donnees[indiceDb][4]);
        controlesCritere[2][i] = int.parse(donnees[indiceDb][5]);
        controlesCritere[3][i] = int.parse(donnees[indiceDb][9]);
        controlesCritere[4][i] = int.parse(donnees[indiceDb][8]);
        controlesCritere[5][i] = int.parse(donnees[indiceDb][7]);
        controlesCritere[6][i] = int.parse(donnees[indiceDb][13]);
        String aimepas = donnees[indiceDb][2];
        String aime = donnees[indiceDb][3];
        affinitesMesEleves[nomElv] = {};
        mesElv.keys.forEach((element) {
          if(element != nomElv){
            affinitesMesEleves[nomElv]![element] =[aimepas.contains(element),aime.contains(element)];
          }
        });
      }else{
        affinitesMesEleves[nomElv] = {};
        mesElv.keys.forEach((element) {
          if(element != nomElv){
            affinitesMesEleves[nomElv]![element] =[false,false];
          }
        });
      }
    }
  }

  Future<Map<String, List<dynamic>>> Enregistre(BuildContext ctx, [bool drapeau = false]) async {
    final mesEleves = await eleves;
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final Directory appDocumentsDir = await getApplicationSupportDirectory();
    File fichier = File("${appDocumentsDir.path}/donnees.csv",);
    for(int i =0; i< mesEleves.entries.length;i++){
      final eleve = mesEleves.entries.toList()[i];
      List<String> affinite_e = [];
      List<String> affinite_i = [];
      for (MapEntry<String, List<bool>> Gens in affinitesMesEleves[eleve.key]!.entries) {
        if (Gens.value[0]) affinite_e.add(Gens.key);
        if (Gens.value[1]) affinite_i.add(Gens.key);
      }
      List<String> datum = [
        widget.classe,//0
        eleve.key,//1
        affinite_e.join(';'),//2
        affinite_i.join(';'),//3
        controlesCritere[1][i].toString(),//4
        controlesCritere[2][i].toString(),//5
        controlesCritere[0][i].toString(),//6
        controlesCritere[5][i].toString(),//7
        controlesCritere[4][i].toString(),//8
        controlesCritere[3][i].toString(),//9
        "false",//10
        "false",//11
        "false",//12
        controlesCritere[6][i].toString(),//13
        eleve.value[1]
      ];
      eleve.value[0];
      if (eleve.value[0] < 0) {
        eleve.value[0] = donnees.length;
        donnees.add(datum);
      } else {
        donnees[eleve.value[0]] = datum;
      }
      prefs.setStringList(widget.classe + eleve.key, [eleve.value[0].toString(), eleve.value[1]]);
    }
    String csv = const ListToCsvConverter().convert(donnees);
    await fichier.writeAsString(csv);
    if(!drapeau)Usine.montreBiscotte(ctx,"Enregistré !",this, true);
    setState(() {
      compteElevesRemplis+=3;
    });
    return eleves;
  }

  choisiCouleur(int indice) {
    if (indice >= 0) {
      if (donnees[indice][10].parseBool() || donnees[indice][11].parseBool() ||
          int.parse(donnees[indice][7]) == 2) {
        return Colors.red;
      } else if (int.parse(donnees[indice][7]) == 0 ||
          donnees[indice][12].parseBool()) {
        return Colors.lightGreen;
      } else {
        return Colors.orange;
      }
    }
    return const Color.fromARGB(0, 0, 0, 0);
  }

  Future<void> finitEleves() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final Directory appDocumentsDir = await getApplicationSupportDirectory();
    File fichier = File("${appDocumentsDir.path}/donnees.csv",);
    Map<String, List<dynamic>> mesEleves = await eleves;
    for(MapEntry<String, List<dynamic>> eleve in mesEleves.entries){
      if(eleve.value[0]<0){
        List<String> datum = [widget.classe,eleve.key,"","","1","1","2","1","1","0","false","false","false","0",eleve.value[1]];
        int indice = donnees.length;
        donnees.add(datum);
        prefs.setStringList(widget.classe+eleve.key, [indice.toString(),eleve.value[1]]);
      }
    }
    String csv = const ListToCsvConverter().convert(donnees);
    await fichier.writeAsString(csv);
  }

  Widget faceEleve(int indice, String nom, int indiceDB) {
    return Padding(padding: const EdgeInsets.all(5), child:
    AnimatedContainer(
      duration: const Duration(milliseconds: 500),
      color: Colors.grey[300],
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Expanded(flex: 0,
              child: Padding(padding: EdgeInsets.symmetric(horizontal: 10),
                  child: Icon(Icons.account_circle))),
          Expanded(flex: 1, child: Text(nom,)),
          Visibility(
            visible: criteres[indiceCriter]==criteresDeBase[1] || criteres[indiceCriter]==criteresDeBase[2],
            child: Expanded(
              child: Padding(
                padding: const EdgeInsets.all(5),
                child:ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10.0)
                    ),
                  ),
                  onPressed: () => showDialog(
                      context: context,
                      barrierDismissible: true,
                      builder: (BuildContext context) =>
                          dialogonsGentillement(context,criteres[indiceCriter]==criteresDeBase[1],affinitesMesEleves[nom]!)).then((value) => value!=null?affinitesMesEleves[nom]:null),
                  child: const Text("Configurer"),
              ),)
            )
          ),
          Visibility(
              visible: criteres[indiceCriter]==criteresDeBase[0] || criteres[indiceCriter]==criteresDeBase[3] || criteres[indiceCriter]==criteresDeBase[4] || criteres[indiceCriter]==criteresDeBase[5] || criteres[indiceCriter]==criteresDeBase[6]|| criteres[indiceCriter]==criteresDeBase[7],
              child: Expanded(
                flex: 2,
                child: MediaQuery.of(context).size.aspectRatio>1?
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: prendRadio(indice, true),
                    ):
                    Column(
                      mainAxisSize: MainAxisSize.min,
                      children: prendRadio(indice, false),
                    )
                ),
              ),
          Visibility(
              visible: criteres[indiceCriter]==criteresDeBase[8],
              child: Expanded(
                child: Padding(
                    padding: const EdgeInsets.all(5),
                    child: SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey, Colors.blue, Colors.red][controlesCritere[6][indice]],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey, Colors.blue, Colors.red][controlesCritere[6][indice]],
                        activeColor: [Colors.grey, Colors.blue, Colors.red][controlesCritere[6][indice]],
                        inactiveColor: Colors.black12,
                        value: controlesCritere[6][indice].toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            controlesCritere[6][indice] = newValue.toInt();
                          });
                        },
                        label: ["Normal", "Important", "Prioritaire"][controlesCritere[6][indice]],
                      ),
                    )
                ),
              )
          ),
          Expanded(flex: 0, child:
          Icon(
            Icons.adjust,
            color:choisiCouleur(indiceDB),
          ),
          ),
        ],
      ),
    ));

  }

  void verifCritair() async{
    SharedPreferences prefs = await SharedPreferences.getInstance();
    final coefsS = prefs.getStringList("\$critères\$${widget.classe}") ??
        ["2", "1", "0", "0", "0", "0", "0", "1"];
    int differenciel = 0;
    if (coefsS.length > 7) {
       for (var inidice = 0; inidice < coefsS.length - 1; inidice++) {
         if(coefsS[inidice]=="0"){
           criteres.removeAt(inidice+1+differenciel);
           differenciel--;
         }
       }
    }
    setState(() {
      criteres;
    });
  }

  prendRadio(int indiceElv, bool ligne) {
    switch (criteresDeBase.indexOf(criteres[indiceCriter])){
      case 3:
        return [
          radioGroupe(1,0,indiceElv, ligne),
          radioGroupe(1,1,indiceElv, ligne),
          radioGroupe(1,2,indiceElv, ligne),
        ];
      case 4:
        return [
          radioGroupe(2,0,indiceElv, ligne),
          radioGroupe(2,1,indiceElv, ligne),
          radioGroupe(2,2,indiceElv, ligne),
        ];
      case 5:
        return [
          radioGroupe(3,0,indiceElv, ligne),
          radioGroupe(3,1,indiceElv, ligne),
        ];
      case 6:
        return [
          radioGroupe(4,0,indiceElv, ligne),
          radioGroupe(4,1,indiceElv, ligne),
          radioGroupe(4,2,indiceElv, ligne),
        ];
      case 7:
        return [
          radioGroupe(5,0,indiceElv, ligne),
          radioGroupe(5,1,indiceElv, ligne),
          radioGroupe(5,2,indiceElv, ligne),
        ];

      default:
        return [
          radioGroupe(0,0,indiceElv, ligne),
          radioGroupe(0,1,indiceElv, ligne),
          radioGroupe(0,2,indiceElv, ligne),
      ];
    }
  }

  Widget radioGroupe(int indiceDucritere, int indiceDuradio, int indiceEleve, bool ligne){
    if(ligne)
    return Expanded(
      child: RadioListTile<int>(
        title: Text(textesCriteres[indiceDucritere][indiceDuradio]),
        contentPadding: EdgeInsets.zero,
        value: indiceDuradio,
        groupValue: controlesCritere[indiceDucritere][indiceEleve],

        onChanged: (int? value) {
          setState(() {
            controlesCritere[indiceDucritere][indiceEleve] = value ?? 0;
          });
        },
      )
    );
    return RadioListTile<int>(
      title: Text(textesCriteres[indiceDucritere][indiceDuradio]),
      contentPadding: EdgeInsets.zero,
      value: indiceDuradio,
      groupValue: controlesCritere[indiceDucritere][indiceEleve],
      visualDensity: const VisualDensity(
          horizontal: VisualDensity.minimumDensity,
          vertical: VisualDensity.minimumDensity),
      onChanged: (int? value) {
        setState(() {
          controlesCritere[indiceDucritere][indiceEleve] = value ?? 0;
        });
      },
    );
  }


}




extension BoolParsing on String {
  bool parseBool() {
    return toLowerCase() == 'true';
  }
}