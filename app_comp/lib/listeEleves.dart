import 'dart:math';
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:plan_de_classe/parametresPlan.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'menu.dart';
import 'usineDeBiscottesGrillees.dart';

class ListeEleves extends StatefulWidget {

  final String classe;

  const ListeEleves({super.key, required this.classe});

  @override
  State<ListeEleves> createState() => _ListeElevesState();
}

class _ListeElevesState extends State<ListeEleves> with TickerProviderStateMixin {

  late Future<Map<String,String>> eleves; //clé: nom | valeur: commentaire
  TextEditingController nomEleve = TextEditingController();
  TextEditingController commentaireEleve = TextEditingController();
  TextEditingController chaineElevesImport = TextEditingController();
  TextEditingController chaineCommentairesImport = TextEditingController();
  TextEditingController delimiteur = TextEditingController();
  bool visible = false;
  bool btnActif = true;
  int maxConfig = 0;

  @override
  void initState() {
    super.initState();
    eleves = litEleves();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
      title: const Text("Création des élèves"),
      ),
      body:SingleChildScrollView(
        physics: const ScrollPhysics(),
        child:
          Column(children:[Table(
            defaultVerticalAlignment: TableCellVerticalAlignment.middle,
            columnWidths: {
              0: const FlexColumnWidth(3),
              1: MediaQuery.of(context).size.width/MediaQuery.of(context).size.height<0.6?const FlexColumnWidth(3):const FlexColumnWidth(1),
            },
            children: [
              TableRow(
                children: [
                  Padding(
                    padding: const EdgeInsets.fromLTRB(5,10,5,2),
                    child:TextField(
                      controller: nomEleve,
                      textInputAction: TextInputAction.next,
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                        labelText: 'Nom de l\'élève',
                      ),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(5),
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        minimumSize: const Size.fromHeight(50), // NEW
                      ),
                      onPressed: btnActif?()=>NouvEleve():null,
                      icon: const Icon(Icons.add_circle_outline),
                      label: const Text("Ajouter"),
                    )
                  ),
                ]
              ),
              TableRow(
                children: [
                  Padding(
                      padding: const EdgeInsets.all(5),
                      child:TextField(
                        controller: commentaireEleve,
                        decoration: const InputDecoration(
                          border: OutlineInputBorder(),
                          labelText: 'Commentaire (facultatif)',
                        ),
                        textInputAction: TextInputAction.done,
                      )
                  ),
                  Padding(
                    padding: const EdgeInsets.all(5),
                    child: ElevatedButton.icon(
                      onPressed: ()=>Enregistre(),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.green,
                        minimumSize: const Size.fromHeight(50), // NEW
                      ),
                      icon: const Icon(Icons.save),
                      label: const Text("Enregistrer"),
                    ),)
                ]
              )
            ],
          ),
          Row(children:[
            Expanded(child: Padding(
              padding: const EdgeInsets.all(5),
              child: ElevatedButton.icon(
                onPressed: btnActif?()=>ImportationIllegale():null,
                style: ElevatedButton.styleFrom(
                  minimumSize: const Size.fromHeight(50),
                ),
                icon: const Icon(Icons.import_export),
                label: const Text("Importer des élèves"),
              ),),),
            Expanded(child:
              Visibility(
                visible: visible,
                child: Padding(
                padding: const EdgeInsets.all(5),
                child:ElevatedButton.icon(
                  onPressed: (){
                    Enregistre();
                    Navigator.push(context,
                      PageRouteBuilder(
                      pageBuilder: (_, __, ___) => ParametrePlan(classe: widget.classe,),
                      transitionDuration: const Duration(milliseconds: 500),
                      transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                      ),
                    );
                  },
                  icon: const Icon(Icons.settings_input_composite_outlined),
                  label: const Text("Paramétrer le plan de classe"),
                  style: ElevatedButton.styleFrom(
                    minimumSize: const Size.fromHeight(50),
                  ),)
          ),))],),
          FutureBuilder(
            future: eleves,
            builder: (context,snapshot){
              if(!snapshot.hasData || snapshot.data==null || snapshot.data!.isEmpty){
                return const Center(child: Text("Ajoutez vos élèves", textAlign: TextAlign.center,),);
              }else{
                List<Widget> mesBeauxEleves = [];
                for (MapEntry<String,String> eleve in snapshot.data!.entries){
                  mesBeauxEleves.add(profilEleve(eleve.key,eleve.value));
                }
                return ListView(
                  physics: const NeverScrollableScrollPhysics(),
                  reverse:true,
                  shrinkWrap: true,
                  children:mesBeauxEleves
                );
              }
            })
          ])
      ),
      drawer:Menu(widget.classe),
    );
    }

  Future<Map<String, String>> litEleves() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final Map<String, String> x = {};
    (prefs.getStringList("\$liste_eleves\$${widget.classe}")??[]).forEach((element) {
      x[element] = (prefs.getStringList(widget.classe+element)??["-1",""])[1];
    });
    if (x.length>3)setState(() {
      visible = true;
    });
    final configPS = prefs.getStringList("\$config\$${widget.classe}")??[];
    List<int> configPI = configPS.map((e) => int.parse(e)).toList();
    configPI.removeLast();
    maxConfig = configPI.where((element) => element==1).length;
    if (x.length==maxConfig)setState(() {
      btnActif = false;
    });
    return x;
  }

  Enregistre() async{
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final mesEleves = await eleves;
    List<String> mesAnciensEleves = prefs.getStringList("\$liste_eleves\$${widget.classe}")??[];
    List<String> configPC = prefs.getStringList("\$placement\$${widget.classe}")??[];
    mesAnciensEleves.forEach((elv) {
      if(!mesEleves.keys.contains(elv)){
        if(configPC.contains(elv))configPC[configPC.indexOf(elv)] = "";
        prefs.remove(widget.classe+elv);
      }
    });
    prefs.setStringList("\$liste_eleves\$${widget.classe}", mesEleves.keys.toList());
    prefs.setStringList("\$placement\$${widget.classe}", configPC);
    mesEleves.entries.forEach((element) {
      final x = prefs.getStringList(widget.classe+element.key)??["-1",""];
      x[1] = element.value;
      prefs.setStringList(widget.classe+element.key, x);
    });

    const snackBar = SnackBar(content: Text('Enregistré!'),);
    ScaffoldMessenger.of(context).showSnackBar(snackBar);
  }

  Widget profilEleve(String nom, String commentaire) {
    return Padding(padding: const EdgeInsets.all(5),child:Container(
      color: Colors.lightBlueAccent,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Expanded(flex:0, child: Padding(padding: EdgeInsets.symmetric(horizontal: 10),child:Icon(Icons.account_circle))),
          Expanded(flex:1, child: Text(nom,)),
          Expanded(flex:1, child: Text(commentaire, style:const TextStyle(fontStyle: FontStyle.italic))),
          Expanded(flex:0, child: IconButton(
            icon: const Icon(Icons.remove_circle),
            color: Colors.red,
            onPressed: ()=>enleveEleve(nom),
          )),
        ],
      ),
    ));
  }

  NouvEleve() async {
    final x = await eleves;
    if (nomEleve.text.isEmpty){
      const snackBar = SnackBar(content: Text('Un élève fantôme?'),);
      ScaffoldMessenger.of(context).showSnackBar(snackBar);
    } else if(x.entries.contains(nomEleve.text)){
      const snackBar = SnackBar(content: Text('Un élève du même nom existe déjà!'),);
      ScaffoldMessenger.of(context).showSnackBar(snackBar);
    }else {
      x[nomEleve.text] = commentaireEleve.text;
      setState(() {
        eleves = Future(() => x);
      });
      nomEleve.clear();
      commentaireEleve.clear();
    }
    if (x.length>3)setState(() {
      visible = true;
    });
    if (x.length==maxConfig)setState(() {
      btnActif = false;
    });
  }

  enleveEleve(String nom) async {
    final x = await eleves;
    x.remove(nom);
    setState(() {
      eleves = Future(() => x);
    });
    const snackBar = SnackBar(content: Text('Supprimé!'),);
    ScaffoldMessenger.of(context).showSnackBar(snackBar);
    if ((await eleves).length<4)setState(() {
      visible = false;
    });
    if ((await eleves).length<maxConfig)setState(() {
      btnActif = true;
    });
  }

  ImportationIllegale() {
    showDialog(
      context: context,
      builder:(BuildContext context) =>Theme(
          data: ThemeData(
              colorSchemeSeed: const Color(0xff4fc2ff), useMaterial3: true),
          child: AlertDialog(
      title: Column(
        children: <Widget>[
        Text("Importer des élèves"),
          const Icon(
          Icons.import_export,
          ),
        ],
      ),
      content: Column(
        children:[
          const Text("Entrez dans le champs la liste de vos élèves, puis indiquez le délimiteur (une virgule, un point, un espace... Les élèves importés seront ajoutés à ceux déjà entrés.", textAlign: TextAlign.center,),
          Padding(padding:const EdgeInsets.all(7),child: TextField(
            controller: chaineElevesImport,
            keyboardType: TextInputType.multiline,
            minLines: 2,
            maxLines: 5,
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              labelText: 'Liste des élèves',
            ),
          )),
          /*Padding(padding:const EdgeInsets.only(right: 4),child: TextField(
            controller: chaineCommentairesImport,
            keyboardType: TextInputType.multiline,
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              labelText: 'Liste des commentaires',
            ),
          )),*/
          Row(
            children: <Widget>[
              Flexible(flex: 1,child:
              SizedBox(
                  width: 1000,
                  child:Padding(padding:const EdgeInsets.all(5),child: Text("Délimiteur: "))
              ), ),
              Flexible(flex: 1,child:
              SizedBox(
                  width: 1000,
                  child:Padding(padding:const EdgeInsets.all(5),child: TextField(
                    controller: delimiteur,
                    keyboardType: TextInputType.text,
                    maxLength: 5,
                    decoration: const InputDecoration(
                      border: OutlineInputBorder(),
                      labelText: 'delimiteur',
                    ),
                  ))
              ), ),
            ]
          ),
        ]
      ),
      actions: <Widget>[
        TextButton(onPressed: ()=>{importDouane()}, child: const Text("Valider", style: TextStyle(fontWeight: FontWeight.bold),),),
        MaterialButton(onPressed: ()=>{Navigator.of(context).pop()}, child: const Text("Annuler"),)
      ])
    ));
  }

  importDouane() async {
    if (chaineElevesImport.text.isNotEmpty && delimiteur.text.isNotEmpty){
      final x = await eleves;
      List<String> elevesImportes = chaineElevesImport.text.split(delimiteur.text);
      if(elevesImportes.length+x.length>maxConfig){
        Usine.montreBiscotte(context, "Pas assez de places disponibles: $maxConfig au total, ${x.length} utilisées et ${elevesImportes.length} à importer.",this);
      }else if(elevesImportes.toSet().length!= elevesImportes.length){
        Usine.montreBiscotte(context, "Des élèves ont le même nom, il faudra pourtant bien pouvoir les différencier!",this);
      }else if(elevesImportes.toSet().intersection(x.keys.toSet()).isNotEmpty){
        Usine.montreBiscotte(context, "Des élèves importés et déjà entrés possèdent le même nom, il faudra pourtant bien pouvoir les différencier!",this);
      }else{
        for (String eleve in chaineElevesImport.text.split(delimiteur.text)){
          x[eleve] = "";
        }
        setState(() {
          eleves = Future(() => x);
        });
        if (x.length>3)setState(() {
          visible = true;
        });
        if (x.length==maxConfig)setState(() {
          btnActif = false;
        });
        chaineElevesImport.clear();
        Navigator.of(context).pop();
      }
    }else{
      print("erreur");
      Usine.montreBiscotte(context, "Remplissez tous les champs",this);
    }
  }

}
