import 'package:flutter/material.dart';
import 'package:plan_de_classe/listeEleves.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ConfigClasse extends StatefulWidget {

  final int rangees;
  final int colonnes;
  final String nomClasse;
  final String commentaire;

  const ConfigClasse({super.key, required this.rangees, required this.colonnes, required this.nomClasse, required this.commentaire});

  @override
  State<ConfigClasse> createState() => _ConfigClasseState();
}

class _ConfigClasseState extends State<ConfigClasse> {

  final String libre = "assets/images/place_vide.png";
  final String occupe = "assets/images/place_occup_e.png";
  late Future<List<int>> configuration;
  int compteur = 0;
  final monskrolleur = ScrollController();
  bool modif = false;
  
  @override
  void initState() {
    super.initState();
    var x = List.generate(widget.colonnes*widget.rangees, (index) => 0);
    x.add(widget.colonnes);
    configuration = Future.value(x);
    litConfig();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Configuration de la classe"),
        ),
        body:FutureBuilder(
          future:configuration,
          builder: (context, snapshot){
            if(snapshot.hasData && snapshot.data!=null){
              return ListView(
                children: [
                  const Padding(padding: EdgeInsets.all(8), child:
                  Text("Cochez les case correspondant aux tables (les couloirs sont matérialisés par des places vides)", textAlign: TextAlign.center,),),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Center(child:Scrollbar(
                      thumbVisibility: true,
                      controller: monskrolleur,
                      child:SingleChildScrollView(
                        controller: monskrolleur,
                        scrollDirection: Axis.horizontal,
                        child:Table(
                          defaultColumnWidth: const FixedColumnWidth(50),
                          children: construitGrille(widget.colonnes, widget.rangees, snapshot.data!),
                          )),))
                  )
                ],
              );
            }else{
              return SizedBox(width:MediaQuery.of(context).size.width, child:const LinearProgressIndicator());
            }
          },

        ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: ()=>enregistreConfig(),
        label: const Text('Enregistrer'),
        icon: const Icon(Icons.check_circle_outline),
        backgroundColor: Colors.green[700],
      ),
    );
  }

  List<TableRow> construitGrille(int ligne, int colonnes, List<int> configuration){
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
                  configuration[ligne*col+lin] = (configuration[ligne*col+lin] + 1)%2;
                  image==libre?image=occupe:image=libre;
                  });
                },
              child: Image.asset(configuration[ligne*col+lin]==0?libre:occupe),
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
    List<String> configPS = prefs.getStringList("\$config\$${widget.nomClasse}")??[];
    if (configPS.isNotEmpty){
      List<int> configPI = configPS.map((e) => int.parse(e)).toList();
      if(configPI.last==widget.colonnes&& (configPI.length-1)/widget.colonnes==widget.rangees)configuration = Future(() => configPI);
      else modif =true;
    }
    configPS.forEach((element) {if(element=="1")compteur++;});
    setState(() {
      configuration;
    });
  }

  enregistreConfig() async{
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final z = prefs.getStringList("liste_classes")??[];
    if (!z.contains(widget.nomClasse)){
      z.add(widget.nomClasse);
      prefs.setStringList("liste_classes", z);
    }
    prefs.setString(widget.nomClasse, widget.commentaire);
    final x = await configuration;
    int compteur2 = 0;
    x.forEach((element) { if(element==1)compteur2++;});
    if(compteur!=compteur2)modif=true;
    final y = x.map((i) => i.toString()).toList();
    if(modif)if(prefs.containsKey("\$placement\$${widget.nomClasse}"))prefs.remove("\$placement\$${widget.nomClasse}");
    prefs.setStringList("\$config\$${widget.nomClasse}",y).then((value) => {
      Navigator.push(
        context,
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => ListeEleves(classe: widget.nomClasse,),
          transitionDuration: const Duration(milliseconds: 500),
          transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
        ),
      )
    });
  }
}