import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plan_de_classe/AlgoContraignant.dart';
import 'package:plan_de_classe/gestionEleves.dart';
import 'package:plan_de_classe/listeEleves.dart';
import 'package:plan_de_classe/parametresPlan.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:math';
import 'nouvelleClasse.dart';

void main() {
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

  Map<String, String> commentaires = {};
  Map<String, List<int>> configurations = {};
  Map<String, int> nbEleves = {};
  late Future<List<String>> mesClasses;

  @override
  void initState() {
    mesClasses = litClasses();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Plan de Classe"),
          automaticallyImplyLeading: false
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
                  child:Text('Aucune classe enregistrée, appuyez sur le petit bouton vert pour commencer', textAlign: TextAlign.center,)
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
            onPressed: ()=>{montreAmeliorations()},
            tooltip: 'Gallerie',
            backgroundColor: Colors.yellow,
            child: const Icon(Icons.image,),
          ),
          FloatingActionButton(
            heroTag: "btn2",
            onPressed: ()=>{Navigator.push(
            context,
            PageRouteBuilder(
            pageBuilder: (_, __, ___) => const NouvelleClasse(classe: "",),
            transitionDuration: const Duration(milliseconds: 500),
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

  alertAlaClasse(String classe) {
    showDialog(
      context: context,
      builder:(BuildContext context) =>CupertinoAlertDialog(
        title: Column(
          children: <Widget>[
            Text(classe),
            const Icon(
              Icons.directions_walk,
            ),
          ],
        ),
        content: const Text( "Où souhaitez vous allez?"),
        actions: <Widget>[
          CupertinoDialogAction(
            textStyle: const TextStyle(color: Colors.blue),
            child: const Text("Modifier la configuration"),
            onPressed:()=>{Navigator.push(
                context,
                PageRouteBuilder(
                  pageBuilder: (_, __, ___) => NouvelleClasse(classe: classe,),
                  transitionDuration: const Duration(milliseconds: 500),
                  transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                ),
              )},
            ),
          CupertinoDialogAction(
            textStyle: const TextStyle(color: Colors.blue),
            child: const Text("Liste des élèves"),
            onPressed:()=>{Navigator.push(
              context,
              PageRouteBuilder(
                pageBuilder: (_, __, ___) => ListeEleves(classe: classe,),
                transitionDuration: const Duration(milliseconds: 500),
                transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
              ),
            )},
            ),
          CupertinoDialogAction(
            textStyle: const TextStyle(color: Colors.blue),
            child: const Text("Paramètres du plan de classe"),
            onPressed: () =>{
              Navigator.push(
                context,
                PageRouteBuilder(
                  pageBuilder: (_, __, ___) => ParametrePlan(classe: classe,),
                  transitionDuration: const Duration(milliseconds: 500),
                  transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                ),
              )
            },),
          CupertinoDialogAction(
            textStyle: const TextStyle(color: Colors.blue),
            child: const Text("Gestion des élèves"),
            onPressed:()=>{Navigator.push(
              context,
              PageRouteBuilder(
                pageBuilder: (_, __, ___) => GestionEleves(classe: classe,),
                transitionDuration: const Duration(milliseconds: 500),
                transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
              ),
            )},
          ),
          CupertinoDialogAction(
            textStyle: const TextStyle(color: Colors.blue),
            child: const Text("Créer un plan de classe"),
            onPressed:()=>{Navigator.push(
              context,
              PageRouteBuilder(
                pageBuilder: (_, __, ___) => AlgoContraignant(classe: classe,),
                transitionDuration: const Duration(milliseconds: 500),
                transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
              ),
            )},
          ),
        ],
      )
    );
  }

  Widget maBelleClasse(String classe){
    return GestureDetector(
    onTap: ()=> alertAlaClasse(classe),
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
                  onPressed: (){suprClasse(classe);},
                ),
              )),
        ],
      ))
    ));
  }

  montreAmeliorations(){
    return showDialog(
      context: context,
      builder:(BuildContext context) =>CupertinoAlertDialog(
      title: Column(
      children: <Widget>[
        const Text("Avancement"),
        const Icon(
          Icons.directions_walk,
        ),
      ],
    ),
    content: const Text( "La gallerie n'a pas encore été implémentée.\nNous devons en effet encore ajouter la sauvegarde des plans de classe.\nSont aussi au programme l'ajout d'un placement des élève par zone dans la classe, ainsi qu'un tutoriel et un menu d'aide complet."),
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
}