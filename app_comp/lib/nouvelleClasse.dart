import 'package:flutter/material.dart';
import 'package:plan_de_classe/config_classe.dart';
import 'package:plan_de_classe/usineDeBiscottesGrillees.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'menu.dart';

class NouvelleClasse extends StatefulWidget {

  final String classe;

  const NouvelleClasse({super.key, required this.classe});

  @override
  State<NouvelleClasse> createState() => _NouvelleClasseState();
}

class _NouvelleClasseState extends State<NouvelleClasse> with TickerProviderStateMixin{

  List<String> classes = [];
  TextEditingController nomClasse = TextEditingController();
  TextEditingController colonnes = TextEditingController();
  TextEditingController rangees = TextEditingController();
  TextEditingController commentaires = TextEditingController();
  final GlobalKey<TooltipState> cleDerangee = GlobalKey<TooltipState>();
  final GlobalKey<TooltipState> cleDecolonnes = GlobalKey<TooltipState>();
  late FocusNode foyerDeranger;
  late FocusNode foyerDeColonnes;

  @override
  void initState() {
    super.initState();
    initInfosClasse();
    foyerDeranger = FocusNode();
    foyerDeColonnes = FocusNode();
  }

  @override
  void dispose(){
    foyerDeColonnes.dispose();
    foyerDeranger.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Configuration de la classe"),
          actions: [
            IconButton(onPressed: ()=>{montrePropos(context, 1)},
                icon: const Icon(Icons.info_outline))
          ],
        ),
        body:ListView(
          shrinkWrap: true,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(10, 15, 10, 10),
              child:TextField(
                controller: nomClasse,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Nom de la classe',
                ),
              ),
            ),
            const Text("dimensions de la classe", textAlign: TextAlign.center,),
            Padding(padding: const EdgeInsets.all(10),
            child:Row(
              children: <Widget>[
                Flexible(flex: 1,child:
                SizedBox(
                    width: 1000,
                    child:Padding(padding:const EdgeInsets.only(right: 4),child: Tooltip(
                      message: "Maximum 10",
                      key: cleDerangee,
                      showDuration: Duration(seconds: 5),
                      decoration: BoxDecoration(color: Colors.red,borderRadius: BorderRadius.circular(10),),
                      preferBelow: false,
                      child:TextField(
                        focusNode: foyerDeranger,
                        controller: rangees,
                        keyboardType: TextInputType.number,
                        textInputAction: TextInputAction.next,
                        decoration: const InputDecoration(
                          border: OutlineInputBorder(),
                          labelText: 'Rangées',
                        ),
                        onChanged: (txt){
                          Tooltip.dismissAllToolTips();
                        },
                      )))
                ), ),
                Flexible(flex: 1,child:
                SizedBox(
                    width: 1000,
                    child:Padding(padding:const EdgeInsets.only(right: 4),child: Tooltip(
                        message: "Maximum 20",
                        key: cleDecolonnes,
                        showDuration: Duration(seconds: 5),
                        decoration: BoxDecoration(color: Colors.red,borderRadius: BorderRadius.circular(10),),
                        preferBelow: false,
                        child:TextField(
                          focusNode: foyerDeColonnes,
                          controller: colonnes,
                          textInputAction: TextInputAction.next,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(
                            border: OutlineInputBorder(),
                            labelText: 'Colonnes',
                          ),
                      )))
                ), ),
              ]
            ),),
            Padding(
              padding: const EdgeInsets.symmetric(vertical:5,horizontal: 10),
              child:TextField(
                controller: commentaires,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Commentaires (facultatif)',
                ),
              ),
            ),
            Padding(padding: const EdgeInsets.all(10),child:
            ElevatedButton(
              onPressed: (){
                if(nomClasse.text.isNotEmpty && rangees.text.isNotEmpty && colonnes.text.isNotEmpty && isNumeric(rangees.text) && isNumeric(colonnes.text)){
                  if(int.parse(rangees.text)>10) {
                    cleDerangee.currentState?.ensureTooltipVisible();
                    foyerDeranger.requestFocus();
                  }
                  else if(int.parse(colonnes.text)>20) {
                    cleDecolonnes.currentState?.ensureTooltipVisible();
                    foyerDeColonnes.requestFocus();
                  }
                  else Navigator.push(context,
                   PageRouteBuilder(
                    pageBuilder: (_, __, ___) => ConfigClasse(rangees: int.parse(rangees.text), colonnes: int.parse(colonnes.text), nomClasse: nomClasse.text, commentaire: commentaires.text,),
                    transitionDuration: const Duration(milliseconds: 500),
                    transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                    ),
                  );
                }else Usine.montreBiscotte(context, "Oups! Veuillez remplir tous les champs", this);

              },
              child: const Text("VOIR",),
            ),),
            const Text("exemple", textAlign: TextAlign.center,style: TextStyle(fontStyle: FontStyle.italic),),
            Image.asset('assets/images/salle_de_classe.png'),

          ],
        ),
      drawer:Menu(widget.classe),
    );
  }

  Future<void> initInfosClasse() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    nomClasse.text = widget.classe;
    commentaires.text = prefs.getString(widget.classe)??"";
    List<String> configPS = prefs.getStringList("\$config\$${widget.classe}")??[];
    if (configPS.isNotEmpty) {
      List<int> configPI = configPS.map((e) => int.parse(e)).toList();
      colonnes.text = configPI.last.toString();
      rangees.text = ((configPI.length-1)~/configPI.last).toString();
    }
  }

  bool isNumeric(String s) {
    if(s == null) {
      return false;
    }
    return int.tryParse(s) != null;
  }

}