import 'package:flutter/material.dart';
import 'package:plan_de_classe/config_classe.dart';

class NouvelleClasse extends StatefulWidget {
  const NouvelleClasse({super.key});

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
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("Configuration de la classe"),
        ),
        body:ListView(
          shrinkWrap: true,
          children: [
            Padding(
              padding: EdgeInsets.all(10),
              child:TextField(
                controller: nomClasse,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Nom de la classe',
                ),
              ),
            ),
            Text("dimensions de la classe", textAlign: TextAlign.center,),
            Padding(padding: EdgeInsets.all(10),
            child:Row(
              children: <Widget>[
                Flexible(flex: 1,child:
                SizedBox(
                    width: 1000,
                    child:Padding(padding:const EdgeInsets.all(4),child: TextField(
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
                    child:Padding(padding:const EdgeInsets.all(4),child: TextField(
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
              padding: EdgeInsets.all(10),
              child:TextField(
                controller: commentaires,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Commentaires',
                ),
              ),
            ),
            Padding(padding: EdgeInsets.all(10),child:
            ElevatedButton(
              onPressed: (){
                if(nomClasse.text.isNotEmpty && rangees.text.isNotEmpty && colonnes.text.isNotEmpty){
                  Navigator.push(context,
                  PageRouteBuilder(
                    pageBuilder: (_, __, ___) => ConfigClasse(rangees: int.parse(rangees.text), colonnes: int.parse(colonnes.text), nomClasse: nomClasse.text),
                    transitionDuration: Duration(milliseconds: 500),
                    transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                    ),
                  );
                }else{
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Oups! Veuillez remplir tous les champs'),));
                }
              },
              child: Text("Voir"),
            ),),
            Text("exemple", textAlign: TextAlign.center,style: TextStyle(fontStyle: FontStyle.italic),),
            Image.asset('assets/images/salle_de_classe.png'),

          ],
        )
    );
  }

}