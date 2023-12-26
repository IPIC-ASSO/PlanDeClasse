import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plan_de_classe/menu.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:window_manager/window_manager.dart';
import 'dart:math';
import 'Gallerie.dart';
import 'nouvelleClasse.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  //await MobileAds.instance.initialize();
  /*await windowManager.ensureInitialized();
  windowManager.waitUntilReadyToShow().then((_) async {
    await windowManager.maximize();
    await windowManager.center();
    await windowManager.show();
    await windowManager.setSkipTaskbar(false);
  });*/
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    SystemChrome.setEnabledSystemUIMode (SystemUiMode.manual, overlays: []);
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Plan De Classe',
      theme: ThemeData(
        primarySwatch: Colors.red,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  final GlobalKey<ScaffoldState> _cleDeLechaffaud = new GlobalKey<ScaffoldState>();
  Map<String, String> commentaires = {};
  Map<String, List<int>> configurations = {};
  Map<String, int> nbEleves = {};
  late Future<List<String>> mesClasses;
  String cestlaclasse = "lalala";

  @override
  void initState() {
    mesClasses = litClasses();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _cleDeLechaffaud,
      appBar: AppBar(
        title: const Text("Plan de Classe"),
        automaticallyImplyLeading: false,
        actions: [
          IconButton(onPressed: ()=>{montrePropos(context)},
            icon: const Icon(Icons.info_outline))
        ],
      ),

      body: FutureBuilder<List<String>>(
          future: mesClasses,
          builder: (context,snapshot){
            if(snapshot.hasData && snapshot.data!=null && snapshot.data!.isNotEmpty){
              List<Widget> listeDeBellesClasses = [];
              for(String classe in snapshot.data!){
                listeDeBellesClasses.add(maBelleClasse(classe));
              }
              return ListView(
                shrinkWrap: true,
                children: listeDeBellesClasses,
              );
            }else{
              return const Center(
                  child:Padding(padding: EdgeInsets.all(15),child:Text('Aucune classe enregistrée, appuyez sur le petit bouton vert pour commencer', textAlign: TextAlign.center,))
              );
            }
          },
      ),
      floatingActionButton: Padding(
        padding:const EdgeInsets.fromLTRB(40,20,20,20),//TODO: ce problème
        child:Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          FloatingActionButton(
            heroTag: "btn1",
            onPressed: ()=>{Navigator.push(
              context,
              PageRouteBuilder(
                pageBuilder: (_, __, ___) => const Gallerie(),
                transitionDuration: const Duration(milliseconds: 500),
                transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
              ),
            )},
            tooltip: 'Gallerie',
            backgroundColor: Colors.orange,
            child: const Icon(Icons.image,shadows: [Shadow(offset: Offset(1, 1), color: Colors.grey)],),
          ),
          FloatingActionButton.extended(
            heroTag: "btn2",
            onPressed: ()=>{Navigator.push(
            context,
            PageRouteBuilder(
            pageBuilder: (_, __, ___) => const NouvelleClasse(classe: "",),
            transitionDuration: const Duration(milliseconds: 500),
            transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
            ),
            )},
            label: Text(MediaQuery.of(context).size.aspectRatio<1?"":"Nouvelle classe"),
            tooltip: 'Créer une nouvelle classe',
            backgroundColor: Colors.green,
            icon:const Icon(Icons.add,),
          ),
        ],
      )),
      drawer: Menu(cestlaclasse),
    );
  }


  Widget maBelleClasse(String classe){
    return GestureDetector(
    onTap: () {
      setState(() {
        cestlaclasse = classe;
      });
      _cleDeLechaffaud.currentState!.openDrawer();
    },
    child:Padding(
      padding: const EdgeInsets.all(10),
      child: Container(
        color: Colors.grey[300],
        padding: const EdgeInsets.all(5),
        child:Row(children: [
          Expanded(
            flex: 0,
            child:CustomPaint(
              painter: ClassePinte(configurations[classe]??[]),
              size: const Size.square(70.0),
              isComplex: true,
              willChange: true,
            )),
          Expanded(
            flex: 1,
            child: Padding(
              padding: const EdgeInsets.all(5),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  Text("classe: $classe", style: const TextStyle(fontWeight: FontWeight.bold,),textAlign: TextAlign.center,),
                  Text("élèves: ${nbEleves[classe]}")
                ],
              )
            )),
          Expanded(
            flex: 1,
            child: Padding(
              padding: const EdgeInsets.all(5),
              child: Text(commentaires[classe]??"",  textAlign: TextAlign.center,),
            )),
          Expanded(
              flex: 1,
              child: Padding(
                padding: const EdgeInsets.all(5),
                child: IconButton(
                  icon: const Icon(Icons.delete_forever),
                  onPressed: ()=>conf_supr(classe),
                ),
              )),
        ],
      ))
    ));
  }

  Future<List<String>>litClasses() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    commentaires.clear();
    configurations.clear();
    final mesClasses = prefs.getStringList("liste_classes")??[];
    mesClasses.forEach((element) async {
      commentaires[element] = prefs.getString(element)??"";
      nbEleves[element] = (prefs.getStringList("\$liste_eleves\$$element")??[]).length;
      final chaine = prefs.getStringList("\$config\$$element")??[];
      configurations[element] = chaine.map((e) => int.parse(e)).toList();
    });
    return mesClasses;
  }

  Future<void> suprClasse(String classe ) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final lesClasses = prefs.getStringList("liste_classes")??[];
    lesClasses.remove(classe);
    prefs.setStringList("liste_classes", lesClasses);
    prefs.remove(classe);
    prefs.remove("\$config\$$classe");
    final List<String> mesEleves = prefs.getStringList("\$liste_eleves\$$classe")??[];
    if(prefs.containsKey("\$liste_eleves\$$classe"))prefs.remove("\$liste_eleves\$$classe");
    for(String eleve in mesEleves){
      if(prefs.containsKey(classe+eleve))prefs.remove(classe+eleve);
    }
    if(prefs.containsKey("\$placement\$$classe"))prefs.remove("\$placement\$$classe");
    if(prefs.containsKey("\$critères\$$classe"))prefs.remove("\$critères\$$classe");
    setState(() {
      mesClasses = litClasses();
    });
    const snackBar = SnackBar(content: Text('Supprimé !'),);
    ScaffoldMessenger.of(context).showSnackBar(snackBar);
    Navigator.of(context).pop();
  }

  conf_supr(String classe) {
    return showCupertinoDialog(
        context: context,
        builder: (context) =>CupertinoAlertDialog(
      title: const Text("Confirmation"),
      content: const Text(
          "Voulez vous vraiment supprimer cette classe?\nCette opération est irréversible."),
      actions: <Widget>[
        CupertinoDialogAction(
          isDestructiveAction: true,
          isDefaultAction: true,
          child: const Text("Supprimer"),
          onPressed: ()=>suprClasse(classe),
        ),
        CupertinoDialogAction(
          child: const Text("Fermer"),
          onPressed: ()=>Navigator.of(context).pop(),
          textStyle: const TextStyle(color: Colors.blue),
        )
      ],
    ));
  }

}

class ClassePinte extends CustomPainter{

  final List<int> configuration;

  ClassePinte(this.configuration);

  @override
  void paint(Canvas canvas, Size size) {
    if(configuration.length>1){
      Paint color = Paint() ;
      color.color = Colors.black;
      Paint color2 = Paint() ;
      color2.color = Colors.white;
      canvas.drawRect(Rect.fromLTWH(0, 0, size.width, size.height), color2);
      int colonnes = configuration.last;
      double dimension = min(size.width/(colonnes)-1,size.width/((configuration.length-1)/colonnes)-1);
      for (int x = 0; x<colonnes; x++){
        for (int y = 0; y<(configuration.length-1)/colonnes; y++){
          if (configuration[x+y*colonnes] == 1){
            Rect rectangle = Rect.fromLTWH(x*dimension+x, y*dimension+y, dimension, dimension);
            canvas.drawRect(rectangle,color);
          }
        }
      }
    }
  }
  @override
  bool shouldRepaint(covariant ClassePinte oldDelegate) {
    return configuration != oldDelegate.configuration;
  }

  dialogons(BuildContext context, String titre, String message) {
    Widget okButton = TextButton(
      child: const Text("Fermer"),
      onPressed: ()=> { Navigator.of(context).pop()},
    );

    AlertDialog alert = AlertDialog(
      title: Text(titre),
      content: Text(message),
      actions: [
        okButton,
      ],
    );

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return alert;
      },
    );
  }


}