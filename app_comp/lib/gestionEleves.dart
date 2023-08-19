import 'package:csv/csv.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:plan_de_classe/AlgoContraignant.dart';
import 'package:plan_de_classe/profilEleve.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:io';

import 'menu.dart';

class GestionEleves extends StatefulWidget {

  final String classe;

  const GestionEleves({super.key, required this.classe});

  @override
  State<GestionEleves> createState() => _GestionElevesState();
}

class _GestionElevesState extends State<GestionEleves> {

  late Future<Map<String, List<dynamic>>> eleves; //clé: nom | valeur: indice;commentaire
  late List<List<String>> donnees;
  int compteElevesRemplis = 0;

  @override
  void initState() {
    super.initState();
    setState(() {
      donnees = [];
      eleves = litEleves();
    });
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
        ),
        body: FutureBuilder(
            future: eleves,
            builder: (context, snapshot) {
              if (!snapshot.hasData || snapshot.data == null ||
                  snapshot.data!.isEmpty) {
                return const Center(child: Text(
                  "Aucun élève disponible.", textAlign: TextAlign.center,),);
              } else {
                List<Widget> mesBeauxEleves = [
                  const Padding(padding: EdgeInsets.all(5),
                      child: Text(
                        "Élèves enregistrés:", textAlign: TextAlign.center,))
                ];
                for (MapEntry<String, List<dynamic>> eleve in snapshot.data!
                    .entries) {
                  mesBeauxEleves.add(
                      profilEleve(eleve.key, eleve.value[1], eleve.value[0]));
                }
                return ListView(
                    physics: const NeverScrollableScrollPhysics(),
                    shrinkWrap: true,
                    children: mesBeauxEleves
                );
              }
            }),
      floatingActionButton: Visibility(
        visible: compteElevesRemplis>2,
        child: FloatingActionButton.extended(
          onPressed: () async {
            await finitEleves();
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
          backgroundColor: Colors.green[400],
        ),
      ),
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

  choisiCouleur(int indice) {
    if (indice >= 0) {
      if (donnees[indice][10].parseBool() || donnees[indice][11].parseBool() ||
          int.parse(donnees[indice][7]) == 0) {
        return Colors.red;
      } else if (int.parse(donnees[indice][7]) == 2 ||
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
}


extension BoolParsing on String {
  bool parseBool() {
    return toLowerCase() == 'true';
  }
}