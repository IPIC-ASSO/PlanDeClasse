import 'package:flutter/material.dart';


class NotesDeVersion extends StatefulWidget {

  const NotesDeVersion({super.key});

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
        title: const Row(mainAxisAlignment: MainAxisAlignment.center,children:[
          Icon(Icons.history_edu),
          Text(
          'Notes de version',
          ),
        ]),
        leading: IconButton(
            onPressed: () => Navigator.of(context).pop(),
            icon: const Icon(Icons.arrow_back)),
      ),
      body: Container(
        width: double.infinity,
    child:const Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          Text("Version 2.1.7 BETA",
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            textAlign: TextAlign.center,),
          Text("mise en ligne le 22/10",
            style: TextStyle(fontSize: 18, fontStyle: FontStyle.italic),
            textAlign: TextAlign.center,),
          Padding(padding: EdgeInsets.all(15),child:Text("• Améliorations mineures de l'interface"))
        ],
      ),)
    );
  }
}