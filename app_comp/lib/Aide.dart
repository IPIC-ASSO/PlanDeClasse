import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

class Aide extends StatefulWidget {

  const Aide({super.key});

  @override
  State<Aide> createState() => _AideState();
}

class _AideState extends State<Aide> with TickerProviderStateMixin {


  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        bottom: TabBar(
          tabs: [
            Tab(icon: Icon(Icons.outdoor_grill), text:"Général"),
            Tab(icon: Icon(Icons.move_down), text:"Configuration de la Classe"),
            Tab(icon: Icon(Icons.manage_accounts_outlined), text:"Configuration des élèves"),
            Tab(icon: Icon(Icons.settings_input_composite_outlined), text:"Configuration du plan"),
            Tab(icon: Icon(Icons.deblur_rounded), text:"Avancé"),
          ],
        ),
        title: const Text('Aide'),
        automaticallyImplyLeading: true,
      ),
      body: TabBarView(
        children: [
          RichText(
              text: TextSpan(
                children: [
                  TextSpan(
                    text: "Plan de Classe est une appliation qui doit vous permettre de créer des plans de classe facilement.\nLes étapes à suivre:\n1. Créer une classe, en indiquant l'agencement des tables (section Configuration)\n2. Entrez la liste des élèves (section Gestion des élèves)\n3. Indiquez quels sont les critères importants: faut-il avant tout trier les élèves par taille, respecter les affinités ou encore alterner les élèves calmes et agités... \n4. Paramétrez vos élèves: en fonction des critères que vous avez rempli entrez les caractéristiques de chacun. 5. C'est bon vous pouvez créer votre plan de classe: l'application vous proposera plusieurs variantes, et il est ensuite possible de modifier à nouveau les carctéristiques du plan de classe.\n 6.(bonus) Vous pouvez enregistrer le plan de classe sous la forme d'une image, enregistrés dans les téléchargements de votre terminal. Il est également possible de voir toutes les images enregostrées depuis la Aide, sur la page principale."
                  )
                ]
              )
          )
      ]),
    );
  }
}