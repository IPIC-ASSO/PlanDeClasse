import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'nouvelleClasse.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    SystemChrome.setEnabledSystemUIOverlays ([]);
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.red,
      ),      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  List<String> classes = [];

  @override
  void initState() {
    litClasses();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    List<Widget> enfants = [];
    if (classes.length==0){
      enfants.add(
          Center(
            child:Text('Aucune classe enregistrée, appuyez sur le petit bouton vert pour commencer', textAlign: TextAlign.center,)
          )
      );
    }
    return Scaffold(
      appBar: AppBar(
        title: Text("Plan de Classe"),
      ),
      body: Center(
        child: ListView(
          shrinkWrap: true,
          children:enfants
        ),
      ),
      floatingActionButton: Padding(
        padding:EdgeInsets.fromLTRB(40,20,20,20),//TODO: ce problème
        child:Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          FloatingActionButton(
            heroTag: "btn1",
            onPressed: nouvClasse(),
            tooltip: 'Gallerie',
            backgroundColor: Colors.yellow,
            child: const Icon(Icons.image,),
          ),
          FloatingActionButton(
            heroTag: "btn2",
            onPressed: ()=>{Navigator.push(
            context,
            PageRouteBuilder(
            pageBuilder: (_, __, ___) => NouvelleClasse(),
            transitionDuration: Duration(milliseconds: 500),
            transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
            ),
            )},
            tooltip: 'Nouvelle classe',
            backgroundColor: Colors.green,
            child: const Icon(Icons.add,),
          ),
        ],
      ))
    );
  }

  nouvClasse() {}

  Widget? classe(String classe){
    return(Padding(
      padding: EdgeInsets.all(5),
      child: Container(
        child: Padding(
          padding: EdgeInsets.all(5),
          child:Text(classe)
        ),
      ),
    )
    );
  }

  litClasses() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    setState(() {
      classes = prefs.getStringList("liste_classes")??[];
    });
  }
}
