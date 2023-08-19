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
    child:Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          Text("Version 2.1.5 ALPHA",
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            textAlign: TextAlign.center,),
          Text("mise en ligne le 19/08",
            style: TextStyle(fontSize: 18, fontStyle: FontStyle.italic),
            textAlign: TextAlign.center,),
          Padding(padding: EdgeInsets.all(15),child:Text("• Nouveau logo et élémnets graphiques\n• Menu d'aide\n• Résolution de bugs mineurs"))
        ],
      ),)
    );
  }
}