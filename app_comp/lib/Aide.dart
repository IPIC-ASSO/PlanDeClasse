import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

class Aide extends StatefulWidget {

  final pageDepart;

  Aide({super.key, this.pageDepart = 0});

  @override
  State<Aide> createState() => _AideState();
}

class _AideState extends State<Aide> with SingleTickerProviderStateMixin {

  late TabController controleTable ;

  @override
  void initState() {
    super.initState();
    controleTable= TabController(length:5,vsync: this);
    controleTable.animateTo(widget.pageDepart);
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final TextStyle textStyle = theme.textTheme.bodyMedium!;
    return Scaffold(
      appBar: AppBar(
        bottom: TabBar(
          isScrollable: true,
          controller: controleTable,
          tabs: const [
            Tab(icon: Icon(Icons.outdoor_grill), child:Text("Général",textAlign: TextAlign.center,)),
            Tab(icon: Icon(Icons.move_down), child:Text("Configuration de la Classe",textAlign: TextAlign.center,)),
            Tab(icon: Icon(Icons.manage_accounts_outlined), child:Text("Configuration des élèves",textAlign: TextAlign.center,)),
            Tab(icon: Icon(Icons.settings_input_composite_outlined), child:Text("Configuration du plan",textAlign: TextAlign.center,)),
            Tab(icon: Icon(Icons.deblur_rounded), text:"Avancé"),
          ],
        ),
        title: const Text('Aide'),
        automaticallyImplyLeading: true,
      ),
      body: TabBarView(
        controller: controleTable,
        children: [
          SingleChildScrollView(child:Padding(padding: const EdgeInsets.all(15),
            child:RichText(
              text:  TextSpan(
                style: textStyle,
                children: [
                  const TextSpan(
                    text: "Plan de Classe est une application qui doit vous permettre de créer des plans de classe facilement.\nLes étapes à suivre:"
                  ),
                  TextSpan(
                    style: theme.textTheme.bodyLarge,
                    text: "\n\n1. Créer une classe, en indiquant l'agencement des tables (section Configuration)\n2. Entrez la liste des élèves (section Gestion des élèves)\n3. Indiquez quels sont les critères importants: faut-il avant tout trier les élèves par taille, respecter les affinités ou encore alterner les élèves calmes et agités... \n4. Paramétrez vos élèves: en fonction des critères que vous avez remplis, entrez les caractéristiques de chacun. \n5. C'est bon vous pouvez créer votre plan de classe: l'application vous proposera plusieurs variantes, et il est ensuite possible de modifier à nouveau les caractéristiques du plan de classe.\n 6.(bonus) Vous pouvez enregistrer le plan de classe sous la forme d'une image, sauvegardée dans les téléchargements de votre terminal. Il est également possible de voir toutes les images enregistrées depuis la Gallerie, sur la page principale."
                  ),
                  const TextSpan(
                      text: "\n\nToutes les étapes vous seront proposées successivement par l'application: laissez vous guider! Il est également possible de naviguer entre les écrans en utilisant le bouton de menu en haut à gauche "
                  ),
                  const WidgetSpan(
                    child: Icon(Icons.menu, size: 20, color: Colors.orange,),
                  ),
                  const TextSpan(
                      text: " ou, si vous êtes sur l'écran principal (la liste des classes) en cliquant sur votre classe."
                  ),
                ]
              )
            )
          ),),
          SingleChildScrollView(child:Padding(padding: const EdgeInsets.all(15),
            child:RichText(
              text: TextSpan(
                  style: textStyle,
                  children: const [
                    TextSpan(
                        text: "La configuration de la classe est essentielle pour que le plan généré par l'application soit pertinent. \nLorsque vous entrez le nombre de «rangées» et de «colonnes», il vous faut tenir compte des "
                    ),
                    TextSpan(
                        style: TextStyle(fontWeight: FontWeight.w700),
                        text: "couloirs de circulation. "
                    ),
                    TextSpan(
                        text: "En vous référant au schéma d'exemple vous pourrez constater qu'une «colonne» est laissée vide, pour la circulation.\nAinsi, ne laissez une case vide entre deux tables que s'il existe dans votre classe un espace suffisant pour s'y déplacer. L'application considèrera que cet espace est suffisament large pour que deux élèves placés de chaque côté du-dit espace ne soient pas considérés comme étant «côtes à côtes».\nIl est possible de modifier à tout moment la configuration en revenant sur la page «Modifier la configuration».\n"
                    ),
                    TextSpan(
                        style: TextStyle(fontWeight: FontWeight.w500, color:Color(0xFF0098AD)),
                        text: "Remarque:"
                    ),
                    TextSpan(
                      text: " Modifier la configuration en changeant le nombre de places ou l'organisation de la salle réinitialisera le placement manuel des élèves. Faites également attention à ne pas réduire les places de façon à ce qu'il y ait plus d'élèves que de tables, la création du plan de classe échouerait dans ce cas là."
                    ),
                    TextSpan(
                      text: ""
                    )
                  ]
              )
          ))),
          SingleChildScrollView(child:Padding(padding: const EdgeInsets.all(15),
            child:RichText(
              text: TextSpan(
                  style: textStyle,
                  children: [
                    const TextSpan(
                        text: "Configurer au mieux vos élèves est décisif pour avoir un plan de classe optimal.\nCommencez par entrer la liste des élèves dans la page «Liste des élèves», en entrant le nom de chaque élève dans le champs «Nom de l'élève» et utilisez le bouton «Ajouter» "
                    ),
                    const WidgetSpan(
                      child: Icon(Icons.add, size: 20, color: Colors.orange,),
                    ),
                    const TextSpan(
                        text: " pour que l'élève soit ajouté dans la liste des élèves de votre classe. Veuillez à bien appuyer sur le bouton «Enregistrer» "
                    ),
                    const WidgetSpan(
                      child: Icon(Icons.save, size: 20, color: Colors.orange,),
                    ),
                    const TextSpan(
                        text: " lorsque vous quittez la page!"
                    ),
                    const TextSpan(
                        style: TextStyle(fontWeight: FontWeight.w200),
                        text: "\n\nPour s'y retrouver: "
                    ),
                    const TextSpan(
                        text: "vous pouver ajouter un commentaire à chaque élève, une petite note qui le caractérise. Ce commentaire ne sera pas pris en compte lors de la création du plan de classe, et reste facultatif."
                    ),
                    const TextSpan(
                        style: TextStyle(fontWeight: FontWeight.w500, color: Colors.lightGreen),
                        text: "\nAstuce: "
                    ),
                    const TextSpan(
                        text: "rentrer les élèves un à un peu être fastidieux, surtout si vous avez déjà tous les noms enregistrés sur un fichier. Il vous suffit alors de copier cette liste de noms, et d'utiliser la fonction «Importer des élèves» "
                    ),
                    const WidgetSpan(
                      child: Icon(Icons.import_export, size: 20, color: Colors.orange,),
                    ),
                    const TextSpan(
                      text:". Vous pourrez alors coller votre liste, puis spécifier le délimiteur, le ou les caractères qui séparent vos élèves. Il est à noter que vous ne pouvez pas associer à ce moment de commentaire à vos élèves, mais vous pourrez le faire plus tard, sur la page «Gestion des élèves», ce qui est d'ailleurs la deuxième étape de la confguration de vos élèves."
                    ),
                    const TextSpan(
                        style: TextStyle(fontWeight: FontWeight.w200),
                        text: "\n\nConfigurer les élèves: "
                    ),
                    const TextSpan(
                      text:"Vous avez maintenant la liste des élèves de votre classe. Il vous faut indiquer quels sont leurs carctéristiques, et quelles contraintes s'appliqueront sur eux lors de leur placement.\nLes élèves, lorsqu'ils n'ont pas été configurés, apparaissent sur fond bleu. Il est nécessaire de configurer au moins "
                    ),
                    const TextSpan(
                        style: TextStyle(fontWeight: FontWeight.w800),
                        text: "3 élèves "
                    ),
                    const TextSpan(
                        text: "pour pouvoir créer un plan de classe. Pour configurer un élèves, cliquez dessus, et, en fonction des critères que vous aurez définis sur la page "
                    ),
                    TextSpan(
                      text: 'Paramètres du plan de classe',
                      style: const TextStyle(color: Colors.blue),
                      recognizer: TapGestureRecognizer()
                        ..onTap = () {
                          controleTable.animateTo(3);
                        },
                    ),
                    const TextSpan(
                        text: " les différents citères à renseigner apparaîtront. Ces données seront prises en compte pour la création du plan de classe. Par exemple, si vous avez donné de l'importance au critère «taille», vous pourrez définir pour chaque élève si ils sont grands, petits, ou de taille moyenne, pour qu'ensuite les élèves définis comme petits ne soient pas placés derrières des élèves de moyenne ou grande taille."
                    ),
                    const TextSpan(
                        style: TextStyle(fontWeight: FontWeight.w500, color:Color(0xFF0098AD)),
                        text: "\nRemarques: "
                    ),
                    const TextSpan(
                        text: "Il n'est pas nécessaire de configurer tous les élèves. Vous pouvez également définir la priorité de traitement de vos élèves pour que les contraintes qui s'appliquent sur eux soient respectées en priorité, ou alors les placer vous-même.\nLe placement manuel des élèves a priorité absolue sur tous les autres critères et sera toujours respecté."
                    ),
                  ]
              )
          ))),
          SingleChildScrollView(child:Padding(padding: const EdgeInsets.all(15),
          child:RichText(
            text: TextSpan(
                style: textStyle,
              children: [
                const TextSpan(
                    text: "Pour que le plan de classe réponde à vos attentes, encore faut-il que vous les définissiez. Indiquez sur l'écran «Paramètres du plan» quels critères sont pour vous les plus importants.\nVous pouvez définir l'importance de chaque critère, les critères les plus importants auront ainsi une plus grande influence sur la disposition des élèves.\nPar exemple, si vous indiquez que A doit être avec B, mais qu'il doit éviter C, si il n'y a que deux places possibles, une loin de B et C, et une à côté de B et C, selon l'importance des critères «affinités inclusive» (rapprocher A et B) et «affinités exclusives» (éloigner A et C) le plan de classe sera différent."
                ),
                const TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w500,color:Color(0xFF0098AD)),
                    text: "\n\nRemarque: "
                ),
                const TextSpan(
                    text: "Dans le cas où des contraintes équivalentes s'appliquent sur un élève, celui-ci sera placé «au hasard», ce qui fait que l'application vous proposera plusieurs varaiantes du plan de classe."
                ),
                const TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w200),
                    text: "\n\nVoir le plan de classe: "
                ),
                const TextSpan(
                    text: "si vous avez indiqué quels critères étaient importants, et "
                ),
                TextSpan(
                  text: 'configuré vos élèves',
                  style: const TextStyle(color: Colors.blue),
                  recognizer: TapGestureRecognizer()
                    ..onTap = () {
                      controleTable.animateTo(2);
                    },
                ),
                const TextSpan(
                    text: " alors vous pouvez générer un plan de classe.\nEn arrivant sur la page «Calculer un plan de classe», l'application génère rapidement un plan, et vous propose jusqu'à 10 variantes.\nL'application va analyser différentes configuration et retenir celles qui correspondent le mieux aux contraintes. \nIl est possible de choisir le temps de calcule, de 5 secondes (par défaut) à 1 minutes. Plus vous donnerez de temps à l'application, plus les propositions de plan de classe seront pertinentes."
                ),
                const TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w500, color: Colors.lightGreen),
                    text: "\nAstuce: "
                ),
                const TextSpan(
                    text: "pour chaque configuration, l'application vous indique si toutes les contraintes ont été respectées (correspondance de 100%) ou si certaines n'ont pas pu l'être."
                ),
                const TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w500,color:Color(0xFF0098AD)),
                    text: "\nRemarque:"
                ),
                const TextSpan(
                    text: "Pour une plus grande efficacité, l'algorithme de génération de plan de classe utilise une part d'aléatoire: en effet, tester toutes les combinaisons possible n'est pas envisageable: on aurait alors une complexité supérieur à O(n!). Cela explique donc que, dans des conditions similaires, l'application ne vous proposera peut-être pas des plans de classe tout à fait identiques."
                ),
                const TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w500, color: Colors.lightGreen),
                    text: "\nAstuce: "
                ),
                const TextSpan(
                    text: "En cliquant sur les élèves, dans le plan de classe, vous pourrez voir pour chaque élèves quels critères ont joués dans son placement, et consulter le commentaire que vous aviez associé à votre élève."
                ),
                const TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w800),
                    text: "\n\nEt pour finir "
                ),
                const TextSpan(
                    text: "vous avez maintenant un plan de classe. Pour pouvoir le consulter à loisir, vous pouvez l'enregistrer sous forme d'image. Il ira droit dans vos téléchargement, dans un dossier appelé «plan de classe». \nVous pouvez également consulter tous les plans de classe enregistrés depuis la gallerie, bouton jaune sur l'écran d'accueil. "
                ),
                const WidgetSpan(
                  child: Icon(Icons.image, size: 20, color: Colors.yellow,),
                ),
                const TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w500,color:Color(0xFF0098AD)),
                    text: "\nRemarque "
                ),
                const TextSpan(
                    text: "Si vous déplacez l'image du lieu où elle a été enregistrée, elle ne sera plus disponible dans la gallerie."
                ),
              ]
          )
          ))),
          SingleChildScrollView(child:Padding(padding: const EdgeInsets.all(15),
          child:RichText(
            text: TextSpan(
                style: textStyle,
              children: const [
                TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w200),
                    text: "\nConcrètement, comment fonctionne l'algorithme?"
                ),
                TextSpan(
                    text: "\nL'algorithme fonctionne de manière séquenciel et récursive: Tout d'abord, il trie les élèves en fonctions de leur priorité de traitement, puis du nombre de contraintes qui s'appliquent sur eux. Ensuite, pour chaque élève, il évalue quelle place serait la plus intéressante (la place où le plus de contraintes seraient respectées), place l'élève, et place de la même manière le suivant, en tenant compte du placement du précédent, et ainsi de suite.\nLorsque plusieurs chois sont possibles, l'algorithme choisit aléatoirement le placement de l'élèves entre les places les plus intéressantes."
                ),
                TextSpan(
                    style: TextStyle(fontWeight: FontWeight.w200),
                    text: "\n\nL'application récolte-t-elle des données?"
                ),
                TextSpan(
                    text: "\nNon! L'application ne communique aucune donnée d'aucun type à un tiers: rien ne sort de votre terminal. Cependant, nous apprécierons de recevoir vos retours. Allez dans la section «A propos» pour voir comment nous contacter."
                ),
              ]
            )
          ))),
      ]),
    );
  }
}