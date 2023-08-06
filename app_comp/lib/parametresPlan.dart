import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:plan_de_classe/gestionEleves.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'menu.dart';

class ParametrePlan extends StatefulWidget {

  final String classe;

  const ParametrePlan({super.key, required this.classe});

  @override
  State<ParametrePlan> createState() => _ParametrePlanState();
}

class _ParametrePlanState extends State<ParametrePlan> {


  int affinites_e = 2;
  int affinites_i = 1;
  int vue = 0;
  int taille = 0;
  int alternanceFG = 0;
  int alternanceAC = 0;
  int alternanceFD = 0;
  int ordre_alpha = 1;


  @override
  void initState() {
    super.initState();
    initGlisseurs();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Création des élèves"),
        ),
        body: ListView(
          shrinkWrap: true,
          children: [
            const Padding(
                padding: EdgeInsets.all(10),
                child:Text("Indiquez l\'importance de chaque critère dans la réalisation du plan de classe", textAlign: TextAlign.center,style: TextStyle(fontWeight: FontWeight.bold),),
            ),
        Row(children: const [
            Expanded(
              flex:1,
              child: Padding(
                padding: EdgeInsets.all(2),
                child: Text("Détails du critère", textAlign: TextAlign.center, style: TextStyle(fontStyle: FontStyle.italic),)
            ),),
          Expanded(
            flex:1,
            child: Padding(
                padding: EdgeInsets.all(2),
                child: Text("Importance du critère", textAlign: TextAlign.center, style: TextStyle(fontStyle: FontStyle.italic),)
            ),),
          ],
        ),
        Table(
          defaultVerticalAlignment: TableCellVerticalAlignment.middle,
            columnWidths: const {
              0: FlexColumnWidth(1),
              1: FlexColumnWidth(1),
            },
            children: [
              TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Affinités (exclusion)", "Correspond aux élèves qui devront être éloignés")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Affinités (exclusion)", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(5),
                  child:SliderTheme(
                    data: SliderTheme.of(context).copyWith(
                      valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][affinites_e],
                    ),
                    child: Slider(
                      thumbColor: [Colors.grey,Colors.blue,Colors.red][affinites_e],
                      activeColor: [Colors.grey,Colors.blue,Colors.red][affinites_e],
                      inactiveColor: Colors.black12,
                      value: affinites_e.toDouble(),
                      min: 0,
                      max: 2,
                      divisions: 2,
                      onChanged: (double newValue) {
                        setState(() {
                          affinites_e = newValue.toInt();
                        });
                      },
                      label: ["Désactivé","Secondaire","Principal"][affinites_e],
                    ),
                  )
                )
              ]),
              TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Affinités (inclusion)", "Correspond aux élèves qui devront être réunis")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Affinités (inclusion)", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(5),
                    child:SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][affinites_i],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey,Colors.blue,Colors.red][affinites_i],
                        activeColor: [Colors.grey,Colors.blue,Colors.red][affinites_i],
                        inactiveColor: Colors.black12,
                        value: affinites_i.toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            affinites_i = newValue.toInt();
                          });
                        },
                        label: ["Désactivé","Secondaire","Principal"][affinites_i],
                      ),
                    )
                )
              ]),
              /*TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Vue", "Le plan prendra en compte la taille et la vue des élèves, pour placer ceux avec la moins bonne vue devant, et éviter qu\'un grand élève empêche à un plus petit de voir le tableau.")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Vue", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(5),
                    child:SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][vue],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey,Colors.blue,Colors.red][vue],
                        activeColor: [Colors.grey,Colors.blue,Colors.red][vue],
                        inactiveColor: Colors.black12,
                        value: vue.toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            vue = newValue.toInt();
                          });
                        },
                        label: ["Désactivé","Secondaire","Principal"][vue],
                      ),
                    )
                )
              ]),
               */
              TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Taille", "Le plan prendra en compte la taille des élèves, pour placer les plus petits devant et les plus grands derrière.")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Taille", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(5),
                    child:SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][taille],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey,Colors.blue,Colors.red][taille],
                        activeColor: [Colors.grey,Colors.blue,Colors.red][taille],
                        inactiveColor: Colors.black12,
                        value: taille.toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            taille = newValue.toInt();
                          });
                        },
                        label: ["Désactivé","Secondaire","Principal"][taille],
                      ),
                    )
                )
              ]),
              TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Alternance filles/garçons", "Le plan présentera une alternance de filles et garçons sur chaque rang.")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Alternance filles/garçons", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(5),
                    child:SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][alternanceFG],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey,Colors.blue,Colors.red][alternanceFG],
                        activeColor: [Colors.grey,Colors.blue,Colors.red][alternanceFG],
                        inactiveColor: Colors.black12,
                        value: alternanceFG.toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            alternanceFG = newValue.toInt();
                          });
                        },
                        label: ["Désactivé","Secondaire","Principal"][alternanceFG],
                      ),
                    )
                )
              ]),
              TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Alternance agités/calmes", "Le plan associera les élèves calmes et agités, pour que ces derniers soient moins bavards, du fait de leur voisin calme.")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Alternance agités/calmes", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(5),
                    child:SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][alternanceAC],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey,Colors.blue,Colors.red][alternanceAC],
                        activeColor: [Colors.grey,Colors.blue,Colors.red][alternanceAC],
                        inactiveColor: Colors.black12,
                        value: alternanceAC.toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            alternanceAC = newValue.toInt();
                          });
                        },
                        label: ["Désactivé","Secondaire","Principal"][alternanceAC],
                      ),
                    )
                )
              ]),
              TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Alternance Fort/En difficulté", "Le plan associera les élèves en difficultés avec ceux qui sont le plus à l'aise")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Alternance Fort/En difficulté", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(5),
                    child:SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][alternanceFD],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey,Colors.blue,Colors.red][alternanceFD],
                        activeColor: [Colors.grey,Colors.blue,Colors.red][alternanceFD],
                        inactiveColor: Colors.black12,
                        value: alternanceFD.toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            alternanceFD = newValue.toInt();
                          });
                        },
                        label: ["Désactivé","Secondaire","Principal"][alternanceFD],
                      ),
                    )
                )
              ]),
              TableRow(children: [
                Padding(
                  padding: const EdgeInsets.all(5),
                  child: ElevatedButton(
                    onPressed: ()=>{
                      dialogons(context, "Ordre alphabétique", "Les élèves seront classés par ordre alphabétique.")
                    },
                    style: const ButtonStyle(backgroundColor: MaterialStatePropertyAll(Colors.white)),
                    child: const Text("Ordre alphabétique", style: TextStyle(color: Colors.black), textAlign: TextAlign.center,),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(5),
                    child:SliderTheme(
                      data: SliderTheme.of(context).copyWith(
                        valueIndicatorColor: [Colors.grey,Colors.blue,Colors.red][ordre_alpha],
                      ),
                      child: Slider(
                        thumbColor: [Colors.grey,Colors.blue,Colors.red][ordre_alpha],
                        activeColor: [Colors.grey,Colors.blue,Colors.red][ordre_alpha],
                        inactiveColor: Colors.black12,
                        value: ordre_alpha.toDouble(),
                        min: 0,
                        max: 2,
                        divisions: 2,
                        onChanged: (double newValue) {
                          setState(() {
                            ordre_alpha = newValue.toInt();
                          });
                        },
                        label: ["Désactivé","Secondaire","Principal"][ordre_alpha],
                      ),
                    )
                )
              ]),
            ],),
            Padding(
              padding: const EdgeInsets.all(5),
              child:ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green,),
                onPressed: (){
                  enregistreGlisseurs();
                  Navigator.push(context,
                    PageRouteBuilder(
                      pageBuilder: (_, __, ___) => GestionEleves(classe: widget.classe,),
                      transitionDuration: const Duration(milliseconds: 500),
                      transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                    ),
                  );
                },
                icon: const Icon(Icons.check_circle_outline),
                label: const Text("VALIDER"))
            ),
            const Padding(
              padding: EdgeInsets.all(10),
              child:Text("Il est possible de revenir plus tard sur cette page \n \n Une description de chaque critère est disponible en appuyant quelques secondes sur le texte.", textAlign: TextAlign.center,style: TextStyle(fontStyle: FontStyle.italic),),
            ),
        ]),
      drawer:Menu(widget.classe),
    );
  }

  dialogons(BuildContext context, String titre, String message) {
    Widget okButton = TextButton(
      child: const Text("Fermer"),
      onPressed: ()=> { Navigator.of(context).pop()},
    );

    AlertDialog alert = AlertDialog(
      title: Text(titre),
      content: Text(message),
      actions: [
        okButton,
      ],
    );

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return alert;
      },
    );
  }

  Future<void> initGlisseurs() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    final coefsS = prefs.getStringList("\$critères\$${widget.classe}")??["2","1","0","0","0","0","0","1"];
    if (coefsS.length>7){
      List<int> coefsI = coefsS.map((e) => int.parse(e)).toList();
      setState(() {
        affinites_e = coefsI[0];
        affinites_i = coefsI[1];
        vue = coefsI[2];
        taille = coefsI[3];
        alternanceFG = coefsI[4];
        alternanceAC = coefsI[5];
        alternanceFD = coefsI[6];
        ordre_alpha = coefsI[7];
      });
    }
  }

  enregistreGlisseurs() async{
    SharedPreferences prefs = await SharedPreferences.getInstance();
    final List<int>coefs = [affinites_e,affinites_i,vue, taille, alternanceFG,alternanceAC, alternanceFD, ordre_alpha];
    prefs.setStringList("\$critères\$${widget.classe}", coefs.map((i) => i.toString()).toList());
  }
}