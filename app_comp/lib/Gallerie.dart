import 'dart:io';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:photo_view/photo_view.dart';
import 'usineDeBiscottesGrillees.dart';

class Gallerie extends StatefulWidget {

  const Gallerie({super.key});

  @override
  State<Gallerie> createState() => _GallerieState();
}

class _GallerieState extends State<Gallerie> with TickerProviderStateMixin {


  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Configuration de la classe"),
      ),
      body: Column(
        children:[
          const Padding(padding: EdgeInsets.all(15),child:const Text("Plans de classe enregistrés")),
          FutureBuilder<Map<String,String>>(
            future: litPhotos(),
            builder: (BuildContext context, AsyncSnapshot<Map<String,String>> snapshot) {
              if (snapshot.hasData) {
                if(snapshot.data!.isNotEmpty){
                  return Padding(
                      padding: EdgeInsets.all(10),
                      child:GridView.builder(
                        shrinkWrap: true,
                        gridDelegate:
                        const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          crossAxisSpacing: 5,
                          mainAxisSpacing: 5,
                        ),
                        itemBuilder: (context, index) {
                          return RawMaterialButton(
                            child: Column(
                              children:[
                                Expanded(flex:1,child: Image.file(
                                  File(snapshot.data!.values.toList()[index]),
                                  height: 300,
                                  fit: BoxFit.contain,
                                )),
                                Text(snapshot.data!.keys.toList()[index])
                              ]
                            ),
                            onPressed: () {
                              showDialog(
                                context: context,
                                builder: (BuildContext context) {
                                  return Dialog(
                                    child:
                                      Container(
                                          child: PhotoView(
                                            backgroundDecoration: BoxDecoration(color: Colors.white),
                                            imageProvider: FileImage(File(snapshot.data!.values.toList()[index])),
                                          )
                                      )
                                  );
                                });
                            },
                          );
                        },
                        itemCount: snapshot.data!.length,
                      )
                  );
                }else{
                  return Center(child: const Text("Auncun plan enregistré"),);
                }
                  
              }else{
                return Center(child: CircularProgressIndicator(),);
              }
            }
          ),
        ]
      ),
    );
  }

  Future<Map<String,String>> litPhotos() async {
    final Map<String,String> planPhotos = {};
    try{
        final Directory dir;
        if(Platform.isAndroid)dir = Directory('/storage/emulated/0/Download');
        else dir = (await getDownloadsDirectory())!;
        final Directory directoire = Directory("${dir.path}/plans de classe");
        if (!await directoire.exists()){
          await directoire.create(recursive: true);
        }
        final listeFichiers = directoire.listSync();
        for (FileSystemEntity fichier in listeFichiers){
          if(fichier is File && fichier.path.endsWith(".png")){
            planPhotos[fichier.path.replaceAll("\\", "/").split("/").last.replaceAll(".png", "")] = fichier.path;
          }
        }

    }catch(e){
      Usine.montreBiscotte(context, "Erreur: $e",this);
      print(e);
    }
    return planPhotos;
  }
}