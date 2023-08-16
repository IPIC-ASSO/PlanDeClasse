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
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Configuration de la classe"),
      ),
      body: Column(
        children:[
          const Padding(padding: EdgeInsets.all(15),child:const Text("Plans de classe enregistrés")),
          FutureBuilder(builder:  future: pret,
          builder: (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
            if (snapshot.hasData && snapshot.data == true) {
                return Padding(padding: Edg);
            }else
          }),
        ]
      ));
  }
}