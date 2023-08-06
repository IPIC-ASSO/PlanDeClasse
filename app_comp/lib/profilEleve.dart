import 'package:csv/csv.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:io';

import 'gestionEleves.dart';

class ProfilEleve extends StatefulWidget {

  final String classe;
  final String eleve;
  final int indice;
  final List<List<String>> donnees;

  const ProfilEleve({super.key, required this.eleve, required this.donnees, required this.classe, required this.indice});

  @override
  State<ProfilEleve> createState() => _ProfilEleveState();
}

class _ProfilEleveState extends State<ProfilEleve> {

  late Future<int> go;
  List<bool> coefsI = [];
  Map<String, List<bool>> mesEleves = {};
  TextEditingController commentaireEleve = TextEditingController();
  List<int> configuration = [];
  List<String> configures = [];
  int vue = 0;
  int genre = 0;
  int placement = 2;
  int taille = 1;
  int attitude = 1;
  int niveau = 1;
  int importance = 0;
  bool plpusVisible = false;
  final monskrolleur = ScrollController();


  @override
  void initState() {
    super.initState();
    go = charge(widget.eleve);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Paramétrage des élèves"),
        ),
        body: FutureBuilder(
          future: go,
          builder: (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
            if (snapshot.hasData) {
              return Padding(padding: const EdgeInsets.all(10), child: ListView(
                  shrinkWrap: true,
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(5),
                      child: Text(style: const TextStyle(fontSize: 20),
                        widget.eleve,
                        textAlign: TextAlign.center,),
                    ),
                    Visibility(
                        visible: coefsI[0]|| coefsI[1],
                        child: Row(children: [
                          const Expanded(flex: 1, child: Padding(
                              padding: EdgeInsets.all(3),
                              child: Text("Doit éviter: "))),
                          Expanded(flex: 1, child: Padding(
                              padding: const EdgeInsets.all(3), child:
                          ElevatedButton(
                              onPressed: () => dialogonsGentillement(true),
                              child: const Text("(LISTE))")))),
                        ],)),
                    Visibility(
                        visible: coefsI[0]|| coefsI[1],
                        child: Row(children: [
                          const Expanded(flex: 1, child: Padding(
                              padding: EdgeInsets.all(3),
                              child: Text("Devrait être avec: "))),
                          Expanded(flex: 1, child: Padding(
                              padding: const EdgeInsets.all(3), child:
                          ElevatedButton(
                              onPressed: () => dialogonsGentillement(false),
                              child: const Text("(LISTE)")))),
                        ],)
                    ),
                    Column(children: [
                      const Padding(
                        padding: EdgeInsets.all(5),
                        child: Text(style: TextStyle(fontWeight: FontWeight
                            .bold), "Placement"),
                      ),
                      Row(children: [
                        Expanded(flex: 1, child: RadioListTile<int>(
                          title: const Text('Devant'),
                          value: 0,
                          groupValue: placement,
                          onChanged: (int? value) {
                            setState(() {
                              placement = value ?? 0;
                            });
                          },
                        ),),
                        Expanded(flex: 1, child: RadioListTile<int>(
                          title: const Text('Au fond'),
                          value: 1,
                          groupValue: placement,
                          onChanged: (int? value) {
                            setState(() {
                              placement = value ?? 0;
                            });
                          },
                        ),),
                        Expanded(flex: 1, child: RadioListTile<int>(
                          title: const Text('Sans importance'),
                          value: 2,
                          groupValue: placement,
                          onChanged: (int? value) {
                            setState(() {
                              placement = value ?? 0;
                            });
                          },
                        ),),
                      ],)
                    ]),
                    Visibility(
                        visible: coefsI[2],
                        child:
                        Column(children: [
                          const Padding(
                            padding: EdgeInsets.all(5),
                            child: Text(style: TextStyle(
                                fontWeight: FontWeight.bold), "Vue"),
                          ),
                          Row(children: [
                            Expanded(flex: 1, child: RadioListTile<int>(
                              title: const Text('Bonne'),
                              value: 0,
                              groupValue: vue,
                              onChanged: (int? value) {
                                setState(() {
                                  go = Future(() => 0);
                                  vue = value ?? 0;
                                });
                              },
                            ),),
                            Expanded(flex: 1, child: RadioListTile<int>(
                              title: const Text('Moyenne'),
                              value: 1,
                              groupValue: vue,
                              onChanged: (int? value) {
                                setState(() {
                                  vue = value ?? 0;
                                });
                              },
                            ),),
                            Expanded(flex: 1, child: RadioListTile<int>(
                              title: const Text('Mauvaise'),
                              value: 2,
                              groupValue: vue,
                              onChanged: (int? value) {
                                setState(() {
                                  vue = value ?? 0;
                                });
                              },
                            ),),
                          ],)
                        ])
                    ),
                    Visibility(
                      visible: coefsI[3],
                      child: Column(children: [
                        const Padding(
                          padding: EdgeInsets.all(5),
                          child: Text(style: TextStyle(
                              fontWeight: FontWeight.bold), "Taille"),
                        ),
                        Row(children: [
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Grand'),
                            value: 0,
                            groupValue: taille,
                            onChanged: (int? value) {
                              setState(() {
                                taille = value ?? 0;
                              });
                            },
                          ),),
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Moyen'),
                            value: 1,
                            groupValue: taille,
                            onChanged: (int? value) {
                              setState(() {
                                taille = value ?? 0;
                              });
                            },
                          ),),
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Petit'),
                            value: 2,
                            groupValue: taille,
                            onChanged: (int? value) {
                              setState(() {
                                taille = value ?? 0;
                              });
                            },
                          ),),
                        ],)
                      ]),
                    ),
                    Visibility(
                      visible: coefsI[4],
                      child: Column(children: [
                        const Padding(
                          padding: EdgeInsets.all(5),
                          child: Text(style: TextStyle(
                              fontWeight: FontWeight.bold), "Genre"),
                        ),
                        Row(children: [
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Garçon'),
                            value: 0,
                            groupValue: genre,
                            onChanged: (int? value) {
                              setState(() {
                                genre = value ?? 0;
                              });
                            },
                          ),),
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Fille'),
                            value: 1,
                            groupValue: genre,
                            onChanged: (int? value) {
                              setState(() {
                                genre = value ?? 0;
                              });
                            },
                          ),),
                        ],)
                      ]),
                    ),
                    Visibility(
                      visible: coefsI[5],
                      child: Column(children: [
                        const Padding(
                          padding: EdgeInsets.all(5),
                          child: Text(style: TextStyle(
                              fontWeight: FontWeight.bold), "Attitude"),
                        ),
                        Row(children: [
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Agité'),
                            value: 0,
                            groupValue: attitude,
                            onChanged: (int? value) {
                              setState(() {
                                attitude = value ?? 0;
                              });
                            },
                          ),),
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Dans la moyenne'),
                            value: 1,
                            groupValue: attitude,
                            onChanged: (int? value) {
                              setState(() {
                                attitude = value ?? 0;
                              });
                            },
                          ),),
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Calme'),
                            value: 2,
                            groupValue: attitude,
                            onChanged: (int? value) {
                              setState(() {
                                attitude = value ?? 0;
                              });
                            },
                          ),),
                        ],)
                      ]),
                    ),
                    Visibility(
                      visible: coefsI[6],
                      child: Column(children: [
                        const Padding(
                          padding: EdgeInsets.all(5),
                          child: Text(style: TextStyle(
                              fontWeight: FontWeight.bold), "Niveau"),
                        ),
                        Row(children: [
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('A l\'aise'),
                            value: 0,
                            groupValue: niveau,
                            onChanged: (int? value) {
                              setState(() {
                                niveau = value ?? 0;
                              });
                            },
                          ),),
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('Dans la moyenne'),
                            value: 1,
                            groupValue: niveau,
                            onChanged: (int? value) {
                              setState(() {
                                niveau = value ?? 0;
                              });
                            },
                          ),),
                          Expanded(flex: 1, child: RadioListTile<int>(
                            title: const Text('En difficultés'),
                            value: 2,
                            groupValue: niveau,
                            onChanged: (int? value) {
                              setState(() {
                                niveau = value ?? 0;
                              });
                            },
                          ),),
                        ],)
                      ]),
                    ),
                    Column(children: [
                      const Padding(
                        padding: EdgeInsets.all(5),
                        child: Text(style: TextStyle(fontWeight: FontWeight
                            .bold), "Importance de traitement"),
                      ),
                      Padding(
                          padding: const EdgeInsets.all(5),
                          child: SliderTheme(
                            data: SliderTheme.of(context).copyWith(
                              valueIndicatorColor: [
                                Colors.grey,
                                Colors.blue,
                                Colors.red
                              ][importance],
                            ),
                            child: Slider(
                              thumbColor: [
                                Colors.grey,
                                Colors.blue,
                                Colors.red
                              ][importance],
                              activeColor: [
                                Colors.grey,
                                Colors.blue,
                                Colors.red
                              ][importance],
                              inactiveColor: Colors.black12,
                              value: importance.toDouble(),
                              min: 0,
                              max: 2,
                              divisions: 2,
                              onChanged: (double newValue) {
                                setState(() {
                                  importance = newValue.toInt();
                                });
                              },
                              label: [
                                "Normal",
                                "Important",
                                "Prioritaire"
                              ][importance],
                            ),
                          )
                      ),
                    ]),
                    Padding(
                        padding: const EdgeInsets.all(5),
                        child: TextField(
                          controller: commentaireEleve,
                          decoration: const InputDecoration(
                            border: OutlineInputBorder(),
                            labelText: 'Commentaire (facultatif)',
                          ),
                        )
                    ),
                    Padding(
                      padding: const EdgeInsets.all(5),
                      child: ElevatedButton.icon(
                        onPressed: () => {parlonsArgentParlonsPlacement()},
                        icon: const Icon(
                            Icons.precision_manufacturing_outlined),
                        label: const Text("Placer manuellement"),
                      ),),
                    Padding(
                      padding: const EdgeInsets.all(5),
                      child: ElevatedButton.icon(
                        onPressed: () {
                          Enregistre();
                          Navigator.push(context,
                            PageRouteBuilder(
                              pageBuilder: (_, __, ___) =>
                                  GestionEleves(classe: widget.classe,),
                              transitionDuration: const Duration(
                                  milliseconds: 500),
                              transitionsBuilder: (_, a, __, c) =>
                                  FadeTransition(opacity: a, child: c),
                            ),
                          );
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blue,
                        ),
                        icon: const Icon(Icons.save),
                        label: const Text("Enregistrer"),
                      ),)
                  ]),
              );
            } else {
              return const Center(
                  child: LinearProgressIndicator()
              );
            }
          },

        )
    );
  }

  initDescendance() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    final coefsS = prefs.getStringList("\$critères\$${widget.classe}") ??
        ["2", "1", "0", "0", "0", "0", "0", "1"];
    if (coefsS.length > 7) {
      coefsI = coefsS.map((e) => int.parse(e) > 0).toList();
    }
  }

  Future<int> charge(String nom) async {
    await initDescendance();
    litEleves();
    litConfig();
    majConversation();
    return Future(() => 1);
  }

  Enregistre() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final Directory appDocumentsDir = await getApplicationSupportDirectory();
    File fichier = File("${appDocumentsDir.path}/donnees.csv",);
    List<String> affinite_e = [];
    List<String> affinite_i = [];
    for (MapEntry<String, List<bool>> Gens in mesEleves.entries) {
      if (Gens.value[0]) affinite_e.add(Gens.key);
      if (Gens.value[1]) affinite_i.add(Gens.key);
    }
    List<String> datum = [
      widget.classe,
      widget.eleve,
      affinite_e.join(';'),
      affinite_i.join(';'),
      taille.toString(),
      vue.toString(),
      placement.toString(),
      niveau.toString(),
      attitude.toString(),
      genre.toString(),
      "false",
      "false",
      "false",
      importance.toString(),
      commentaireEleve.text
    ];
    int indice = widget.indice;
    if (indice < 0) {
      indice = widget.donnees.length;
      widget.donnees.add(datum);
    } else {
      widget.donnees[indice] = datum;
    }
    prefs.setStringList(widget.classe + widget.eleve,
        [indice.toString(), commentaireEleve.text]);
    prefs.setStringList("\$placement\$${widget.classe}", configures);
    String csv = const ListToCsvConverter().convert(widget.donnees);
    await fichier.writeAsString(csv);
  }

  void majConversation() {
    if (widget.indice >= 0) {
      List<String> datum = widget.donnees[widget.indice];
      commentaireEleve.text = datum[14];
      vue = int.parse(datum[5]);
      genre = int.parse(datum[9]);
      placement = int.parse(datum[6]);
      taille = int.parse(datum[4]);
      attitude = int.parse(datum[8]);
      niveau = int.parse(datum[7]);
      importance = int.parse(datum[13]);
    }
  }

  Future<void> dialogonsGentillement(bool evite) async {
    final List<String> listee = mesEleves.keys.toList();
    return showDialog<void>(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text("L'élève doit ${evite ? "éviter" : "être avec"}:"),
          content: StatefulBuilder(
              builder: (BuildContext context, StateSetter setState) {
                return SizedBox(
                    width: double.maxFinite,
                    child: ListView.builder(
                        itemCount: mesEleves.keys.length,
                        shrinkWrap: true,
                        itemBuilder: (BuildContext context, int index) {
                          return CheckboxListTile(
                            title: Text(listee[index]),
                            value: (mesEleves[listee[index]] ?? [
                              false,
                              false
                            ])[evite ? 0 : 1],
                            enabled: !(mesEleves[listee[index]] ??
                                [false, false])[!evite ? 0 : 1],
                            onChanged: (bool? value) {
                              setState(() {
                                (mesEleves[listee[index]] ??
                                    [false, false])[evite ? 0 : 1] =
                                    value ?? false;
                              });
                            },
                          );
                        }));
              }),
          actions: <Widget>[
            TextButton(
              child: const Text('Terminer'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Future<void> parlonsArgentParlonsPlacement() async {
    return showDialog<void>(
        context: context,
        barrierDismissible: true,
        builder: (BuildContext context) {
          return AlertDialog(
            title: const Text("Placez l'élève dans la classe"),
            content: StatefulBuilder(
                builder: (BuildContext context, StateSetter setState) {
                  return Scrollbar(
                    thumbVisibility: true,
                    controller: monskrolleur,
                    child: SingleChildScrollView(
                        controller: monskrolleur,
                        scrollDirection: Axis.horizontal,
                        child: Table(
                          defaultColumnWidth: const FixedColumnWidth(100),
                          children: construitGrilleDeChange(setState),
                        )),);
                }),
            actions: [
              ElevatedButton(onPressed: () => {Navigator.of(context).pop()},
                  child: const Text("Terminer"))
            ],
          );
        });
  }


  //TODO: optimiser la fonction et fusionner avec majConversation
  Future<void> litEleves() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    for (var element in (prefs.getStringList(
        "\$liste_eleves\$${widget.classe}") ?? [])) {
      if (element == widget.eleve)
        commentaireEleve.text =
        (prefs.getStringList(widget.classe + element) ?? ["-1", ""])[1];
      else {
        if (widget.indice < 0) {
          mesEleves[element] = [false, false];
        } else {
          mesEleves[element] = [
            widget.donnees[widget.indice][2].split(";").contains(element),
            widget.donnees[widget.indice][3].contains(element)
          ];
        }
      }
    }
  }

  litConfig() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    List<String> configPS = prefs.getStringList("\$config\$${widget.classe}") ??
        [];
    configuration = configPS.map((e) => int.parse(e)).toList();
    int nombrePlaces = 0;
    for (var element in configuration) {
      if (element == 1) nombrePlaces++;
    }
    List<String> configPC = (prefs.getStringList(
        "\$placement\$${widget.classe}") ?? []).isEmpty ? List<String>.generate(
        nombrePlaces, (index) => "") : prefs.getStringList(
        "\$placement\$${widget.classe}") ?? [];
    configures = configPC;
  }

  construitGrilleDeChange(StateSetter setState) {
    final List<TableRow> mesLignes = [];
    final int colonnes = ((configuration.length - 1) ~/ configuration.last);
    final int lignes = configuration.last;
    int comptePlaces = -1;
    for (int col = 0; col < colonnes; col++) {
      final List<TableCell>enfants = [];
      for (int lin = 0; lin < lignes; lin++) {
        final maPlaceDansLaConfig = lignes * col + lin;
        if (configuration[maPlaceDansLaConfig] == 1) comptePlaces++;
        final maPaceDansLeCompte = comptePlaces;
        enfants.add(
            TableCell(child:
            ElevatedButton(
                onPressed:
                configuration[maPlaceDansLaConfig] == 1 ?
                (configures[maPaceDansLeCompte].isEmpty ? () {
                  if (configures.contains(widget.eleve))
                    configures[configures.indexOf(widget.eleve)] = "";
                  setState(() {
                    configures[maPaceDansLeCompte] = widget.eleve;
                  });
                } : (configures[maPaceDansLeCompte] == widget.eleve ? () {
                  setState(() {
                    configures[maPaceDansLeCompte] = "";
                  });
                } : () {}))
                    : null,
                style: ElevatedButton.styleFrom(
                  backgroundColor: configuration[maPlaceDansLaConfig] == 1
                      ? Colors.white
                      : Colors.grey,
                  side: BorderSide(
                    color: Colors.black,
                    width: configuration[maPlaceDansLaConfig] == 1 ? 2.0 : 1.0,
                  ), // Background color
                ),
                child: Text(configuration[maPlaceDansLaConfig] == 1
                    ? configures[maPaceDansLeCompte]
                    : "", style: const TextStyle(color: Colors.black),)
            ),
            )
        );
      }
      mesLignes.add(TableRow(children: enfants));
    }
    return mesLignes;
  }
}