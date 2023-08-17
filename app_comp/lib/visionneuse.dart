import 'package:flutter/material.dart';


class NotesDeVersion extends StatefulWidget {

  NotesDeVersion({super.key});

  @override
  State<NotesDeVersion> createState() => _NotesDeVersionState();

}
class _NotesDeVersionState extends State<NotesDeVersion> {

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: Row(mainAxisAlignment: MainAxisAlignment.center,children:[
          Icon(Icons.history_edu),
          Text(
          'Notes de version',
          ),
        ]),
        leading: IconButton(
            onPressed: () => Navigator.of(context).pop(),
            icon: const Icon(Icons.arrow_back)),
      ),
      body: Column(
        children: <Widget>[
          const Text("Version 2.1.4 ALPHA",
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            textAlign: TextAlign.center,),
          const Text("mise en ligne le 17/08",
            style: TextStyle(fontSize: 18, fontStyle: FontStyle.italic),
            textAlign: TextAlign.center,),
          const Padding(padding: EdgeInsets.all(15),child:Text("• Ajout de la gallerie et de l'enregistrement des plans de classe\n• Nouvel algorithme de création de plans de classe\n• Possibilité d'importer une liste d'élèves. \n• Résolution de bugs mineurs et amélioration de l'interface."))
        ],
      ),
    );
  }
}