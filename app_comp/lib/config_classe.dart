import 'dart:html';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ConfigClasse extends StatefulWidget {

  final int rangees;
  final int colonnes;
  final String nomClasse;

  const ConfigClasse({super.key, required this.rangees, required this.colonnes, required this.nomClasse});

  @override
  State<ConfigClasse> createState() => _ConfigClasseState();
}

class _ConfigClasseState extends State<ConfigClasse> {

  final String libre = "assets/images/place_vide.png";
  final String occupe = "assets/images/place_occup_e.png";
  List<int> configuration = [];
  
  @override
  void initState() {
    super.initState();
    configuration = List.generate(widget.colonnes*widget.rangees, (index) => 0);
    configuration.add(widget.colonnes);
    litConfig();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("Configuration de la classe"),
        ),
        body:Column(
          children: [
            Padding(padding: EdgeInsets.all(8), child:
              Text("Cochez les case correspondant aux tables.", textAlign: TextAlign.center,),),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: SingleChildScrollView(
                  controller: ScrollController(),
                  scrollDirection: Axis.horizontal,
                  physics: const ClampingScrollPhysics(),
                  child:Table(
                    children: construitGrille(widget.colonnes, widget.rangees),
              )),),
          ],
        )
    );
  }

  List<TableRow> construitGrille(int ligne, int colonnes){
    final List<TableRow> mesLignes = [];
    for (int col = 0; col<colonnes; col++){
      final List<TableCell>enfants = [];
      for (int lin = 0; lin<ligne; lin++){
        var image = libre;
        enfants.add(
          TableCell(child:
            GestureDetector(
              onTap:(){
                setState(() {
                  image==libre?image=occupe:image=libre;
                  });
                },
              child: Image.asset(image, width: 20,),
            )
          )
        );
      }
      mesLignes.add(TableRow(children: enfants));
    }
    return mesLignes;
  }

  litConfig() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    List<String> configPS = prefs.getStringList(widget.nomClasse)??[];
    if (configPS.isNotEmpty){
      List<int> configPI = configPS.map((e) => int.parse(e)).toList();
      if(configPI.last==widget.colonnes&& (configPI.length-1)/widget.colonnes==widget.rangees)configuration = configPI;
    }
  }

}