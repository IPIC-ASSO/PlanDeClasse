import 'dart:developer';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:photo_view/photo_view.dart';
import 'menu.dart';
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
        title: const Text("Plans enregistrés", textAlign: TextAlign.center,),
        actions: [
          IconButton(onPressed: ()=>{montrePropos(context)},
              icon: const Icon(Icons.info_outline))
        ],
      ),
      body:
          FutureBuilder<Map<String,String>>(
            future: litPhotos(),
            builder: (BuildContext context, AsyncSnapshot<Map<String,String>> snapshot) {
              if (snapshot.hasData) {
                if(snapshot.data!.isNotEmpty){
                  return GridView.builder(
                      padding: const EdgeInsets.all(15),
                      primary: true,
                      physics: const ScrollPhysics(),
                      shrinkWrap: true,
                      gridDelegate:
                      const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 2,
                        crossAxisSpacing: 15,
                        mainAxisSpacing: 15,
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
                                  elevation: 0,
                                  backgroundColor: Colors.transparent,
                                  child:Stack(
                                    children: [
                                      Container(child:
                                        PhotoView(
                                          backgroundDecoration: const BoxDecoration(color: Colors.transparent),
                                          imageProvider: FileImage(File(snapshot.data!.values.toList()[index])),
                                        )
                                      ),Positioned(
                                        right: 0.0,
                                        child: GestureDetector(
                                          onTap: (){
                                            Navigator.of(context).pop();
                                          },
                                          child: const Align(
                                            alignment: Alignment.topRight,
                                            child: CircleAvatar(
                                              radius: 18.0,
                                              backgroundColor: Colors.white,
                                              child: Icon(Icons.close, color: Colors.red),
                                          ),
                                        ),
                                      )
                                      )
                                    ],
                                  )
                                );
                              });
                          },
                        );
                      },
                      itemCount: snapshot.data!.length,
                      );
                }else{
                  return const Center(child: Text("Auncun plan enregistré"),);
                }
              }else{
                return const Center(child: CircularProgressIndicator(),);
              }
            }
          ),

    );
  }

  Future<Map<String,String>> litPhotos() async {
    final Map<String,String> planPhotos = {};
    try{
        final Directory dir;
        if(Platform.isAndroid)dir = Directory('/storage/emulated/0/Download');
        else if(Platform.isMacOS) {
        dir = Directory("${(await getApplicationDocumentsDirectory()).path.split("/").sublist(0,3).join("/")}/Downloads");
        }else if(Platform.isIOS){
          dir = (await getApplicationDocumentsDirectory());
        }
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
      log(e.toString());
    }
    return planPhotos;
  }
}