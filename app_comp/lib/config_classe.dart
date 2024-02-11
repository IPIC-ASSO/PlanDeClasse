import 'dart:io';

import 'package:flutter/material.dart';
import 'package:plan_de_classe/listeEleves.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'usineDeBiscottesGrillees.dart';

class ConfigClasse extends StatefulWidget {

  final int rangees;
  final int colonnes;
  final String nomClasse;
  final String commentaire;

  const ConfigClasse({super.key, required this.rangees, required this.colonnes, required this.nomClasse, required this.commentaire});

  @override
  State<ConfigClasse> createState() => _ConfigClasseState();
}

class _ConfigClasseState extends State<ConfigClasse> with TickerProviderStateMixin{

  final String libre = "assets/images/place_vide.png";
  final String occupe = "assets/images/place_occup_e.png";
  late Future<List<int>> configuration;
  List<int> config = [];
  int compteur = 0;
  final monskrolleur = ScrollController();
  bool modif = false;
  bool enleve = false;
  GlobalKey gridKey = GlobalKey();

  @override
  void initState() {
    super.initState();
    var x = List.generate(widget.colonnes*widget.rangees, (index) => 0);
    x.add(widget.colonnes);
    configuration = Future.value(x);
    litConfig();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Configuration de la classe"),
        ),
        body:FutureBuilder(
          future:configuration,
          builder: (context, snapshot){
            if(snapshot.hasData && snapshot.data!=null){
              config = snapshot.data!;
              return _buildBody();
            }else{
              return SizedBox(width:MediaQuery.of(context).size.width, child:const LinearProgressIndicator());
            }
          },

        ),
    );
  }

  List<TableRow> construitGrille(int ligne, int colonnes, List<int> configuration){
    final List<TableRow> mesLignes = [];
    for (int col = 0; col<colonnes; col++){
      final List<TableCell>enfants = [];
      for (int lin = 0; lin<ligne; lin++){
        enfants.add(_buildGridItems(context,ligne*col+lin));
      }
      mesLignes.add(TableRow(children: enfants));
    }
    return mesLignes;
  }

  litConfig() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    List<String> configPS = prefs.getStringList("\$config\$${widget.nomClasse}")??[];
    if (configPS.isNotEmpty){
      List<int> configPI = configPS.map((e) => int.parse(e)).toList();
      if(configPI.last==widget.colonnes&& (configPI.length-1)/widget.colonnes==widget.rangees)configuration = Future(() => configPI);
      else modif =true;
    }
    configPS.forEach((element) {if(element=="1")compteur++;});
    setState(() {
      configuration;
    });
  }

  enregistreConfig() async{
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final x = await configuration;
    int compteur2 = 0;
    x.forEach((element) { if(element==1)compteur2++;});
    if(compteur!=compteur2)modif=true;
    if(compteur2<5){
      Usine.montreBiscotte(context, "Ajoutez davantages de tables pour créer un plan de classe", this);
      return;
    }
    final z = prefs.getStringList("liste_classes")??[];
    if (!z.contains(widget.nomClasse)){
      z.add(widget.nomClasse);
      prefs.setStringList("liste_classes", z);
    }
    prefs.setString(widget.nomClasse, widget.commentaire);

    final y = x.map((i) => i.toString()).toList();
    if(modif)if(prefs.containsKey("\$placement\$${widget.nomClasse}"))prefs.remove("\$placement\$${widget.nomClasse}");
    prefs.setStringList("\$config\$${widget.nomClasse}",y).then((value) => {
      Navigator.push(
        context,
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => ListeEleves(classe: widget.nomClasse,),
          transitionDuration: const Duration(milliseconds: 500),
          transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
        ),
      )
    });
  }

  Widget _buildBody() {
    return Column(children: <Widget>[
      Expanded(child:
        ListView(
        children: [
         Padding(padding: const EdgeInsets.all(8), child:
          Text("Cochez les case correspondant aux tables (les couloirs sont matérialisés par des places vides) ${Platform.isAndroid || Platform.isIOS?"\nUtilisez deux doigts pour vous déplacer dans la classe ":" "}", textAlign: TextAlign.center,),),
          Padding(
              padding: const EdgeInsets.all(8.0),
              child: Center(child:Scrollbar(
                thumbVisibility: true,
                controller: monskrolleur,
                child:SingleChildScrollView(
                    controller: monskrolleur,
                    scrollDirection: Axis.horizontal,
                    child:Table(
                      key: gridKey,
                      defaultColumnWidth: const FixedColumnWidth(50),
                      children: construitGrille(widget.colonnes, widget.rangees, config),
                    )),))
          ),
          Padding(
            padding: const EdgeInsets.all(15),
            child:ElevatedButton.icon(
              onPressed: ()=>enregistreConfig(),
              label: const Text('Enregistrer'),
              icon: const Icon(Icons.check_circle_outline),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green[700],
                minimumSize:Size(MediaQuery.of(context).size.width/(MediaQuery.of(context).size.aspectRatio>1?2:1),50),
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10.0)
                ),
              ),
            ),
          ),
    ]))]);
  }

  TableCell _buildGridItems(BuildContext context, int index) {
    GlobalKey gridItemKey = GlobalKey();

    return TableCell(child: GestureDetector(
      onTapDown: (details) {
        RenderBox _box = gridItemKey.currentContext?.findRenderObject() as RenderBox;
        RenderBox _boxGrid = gridKey.currentContext?.findRenderObject() as RenderBox;
        Offset position = _boxGrid.localToGlobal(Offset.zero); //this is global position
        double gridLeft = position.dx;
        double gridTop = position.dy;
        double gridPosition = details.globalPosition.dy - gridTop;
        //Get item position
        int indexX = (gridPosition / _box.size.width).floor().toInt();
        int indexY = ((details.globalPosition.dx - gridLeft) / _box.size.width).floor().toInt();
        config[indexY+indexX*widget.colonnes] = (config[indexY+indexX*widget.colonnes]+1)%2;
        setState(() {});
      },
      onHorizontalDragStart: (details){
        if (config[index]==1)enleve=true;
        else enleve = false;
      },
      onVerticalDragStart: (details){
        if (config[index]==1)enleve=true;
        else enleve = false;
      },
      onVerticalDragUpdate: (details) {
        selectItem(gridItemKey, details);
      },
      onHorizontalDragUpdate: (details) {
        selectItem(gridItemKey, details);
      },
      child: GridTile(
        key: gridItemKey,
        child: Container(
          child: Center(
            child: _buildGridItem(index),
          ),
        ),
      ),
    )) ;
  }

  void selectItem(GlobalKey<State<StatefulWidget>> gridItemKey, var details) {
    RenderBox _boxItem = gridItemKey.currentContext?.findRenderObject() as RenderBox;
    RenderBox _boxMainGrid = gridKey.currentContext?.findRenderObject() as RenderBox;
    Offset position = _boxMainGrid.localToGlobal(Offset.zero); //this is global position
    double gridLeft = position.dx;
    double gridTop = position.dy;

    double gridPosition = details.globalPosition.dy - gridTop;

    //Get item position
    int rowIndex = (gridPosition / _boxItem.size.width).floor().toInt();
    int colIndex = ((details.globalPosition.dx - gridLeft) / _boxItem.size.width).floor().toInt();
    if(colIndex<0 || rowIndex<0 ||colIndex>=widget.colonnes || rowIndex>=widget.rangees)return;
    config[colIndex+ rowIndex*widget.colonnes] = enleve?1:0;

    setState(() {});
  }

  Widget _buildGridItem(int indeix) {
    if (config[indeix]==0) {
      return Image.asset(libre);
    }else {
      return Image.asset(occupe);
    }
  }
}
