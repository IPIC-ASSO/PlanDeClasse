import 'package:flutter/material.dart';
import 'package:plan_de_classe/config_classe.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'menu.dart';

class NouvelleClasse extends StatefulWidget {

  final String classe;

  const NouvelleClasse({super.key, required this.classe});

  @override
  State<NouvelleClasse> createState() => _NouvelleClasseState();
}

class _NouvelleClasseState extends State<NouvelleClasse> {

  List<String> classes = [];
  TextEditingController nomClasse = TextEditingController();
  TextEditingController colonnes = TextEditingController();
  TextEditingController rangees = TextEditingController();
  TextEditingController commentaires = TextEditingController();
  @override
  void initState() {
    super.initState();
    initInfosClasse();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Configuration de la classe"),
        ),
        body:ListView(
          shrinkWrap: true,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(10, 15, 10, 10),
              child:TextField(
                controller: nomClasse,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Nom de la classe',
                ),
              ),
            ),
            const Text("dimensions de la classe", textAlign: TextAlign.center,),
            Padding(padding: const EdgeInsets.all(10),
            child:Row(
              children: <Widget>[
                Flexible(flex: 1,child:
                SizedBox(
                    width: 1000,
                    child:Padding(padding:const EdgeInsets.only(right: 4),child: TextField(
                      controller: rangees,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                        labelText: 'Rangées',
                      ),
                    ))
                ), ),
                Flexible(flex: 1,child:
                SizedBox(
                    width: 1000,
                    child:Padding(padding:const EdgeInsets.only(right: 4),child: TextField(
                      controller: colonnes,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                        labelText: 'Colonnes',
                      ),
                    ))
                ), ),
              ]
            ),),
            Padding(
              padding: const EdgeInsets.symmetric(vertical:5,horizontal: 10),
              child:TextField(
                controller: commentaires,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Commentaires (facultatif)',
                ),
              ),
            ),
            Padding(padding: const EdgeInsets.all(10),child:
            ElevatedButton(
              onPressed: (){
                if(nomClasse.text.isNotEmpty && rangees.text.isNotEmpty && colonnes.text.isNotEmpty){
                  Navigator.push(context,
                  PageRouteBuilder(
                    pageBuilder: (_, __, ___) => ConfigClasse(rangees: int.parse(rangees.text), colonnes: int.parse(colonnes.text), nomClasse: nomClasse.text, commentaire: commentaires.text,),
                    transitionDuration: const Duration(milliseconds: 500),
                    transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                    ),
                  );
                }else{
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Oups! Veuillez remplir tous les champs'),));
                }
              },
              child: const Text("VOIR",),
            ),),
            const Text("exemple", textAlign: TextAlign.center,style: TextStyle(fontStyle: FontStyle.italic),),
            Image.asset('assets/images/salle_de_classe.png'),

          ],
        ),
      drawer:Menu(widget.classe),
    );
  }

  Future<void> initInfosClasse() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    nomClasse.text = widget.classe;
    commentaires.text = prefs.getString(widget.classe)??"";
    List<String> configPS = prefs.getStringList("\$config\$${widget.classe}")??[];
    if (configPS.isNotEmpty) {
      List<int> configPI = configPS.map((e) => int.parse(e)).toList();
      colonnes.text = configPI.last.toString();
      rangees.text = ((configPI.length-1)~/configPI.last).toString();
    }
  }

}