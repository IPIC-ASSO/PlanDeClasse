
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plan_de_classe/AlgoContraignant.dart';
import 'package:plan_de_classe/gestionEleves.dart';
import 'package:plan_de_classe/listeEleves.dart';
import 'package:plan_de_classe/nouvelleClasse.dart';
import 'package:plan_de_classe/parametresPlan.dart';
import 'package:url_launcher/url_launcher.dart';
import 'main.dart';

class Menu extends StatefulWidget {


  final String classe;

  @override
  _MenuState createState() => _MenuState();

  const Menu(this.classe, {super.key});
}

class _MenuState extends State<Menu> with SingleTickerProviderStateMixin {
  static const _menuTitles = [
    'Modifier la configuration',
    'Liste des élèves',
    'Paramètres du plan de classe',
    'Gestion des élèves',
    'Calculer un plan de classe',
    'Aide',
    'Notes de version'
  ];
  static const _iconMenu = [
    Icons.edit_calendar,
    Icons.format_list_bulleted_outlined,
    Icons.settings_input_composite_outlined,
    Icons.manage_accounts,
    Icons.oil_barrel_rounded,
    Icons.live_help_outlined,
    Icons.history_edu
  ];
  static const _colorMenu = [
    Colors.blue,
    Colors.lightBlueAccent,
    Colors.orange,
    Colors.deepOrangeAccent,
    Colors.green,
    Colors.red,
    Colors.grey,
  ];

  late  var _classes = [
    NouvelleClasse(classe: widget.classe,),
    ListeEleves(classe: widget.classe,),
    ParametrePlan(classe: widget.classe,),
    GestionEleves(classe: widget.classe,),
    AlgoContraignant(classe: widget.classe,),
    AlgoContraignant(classe: widget.classe,),
    AlgoContraignant(classe: widget.classe,),
  ];

  static const _initialDelayTime = Duration(milliseconds: 50);
  static const _itemSlideTime = Duration(milliseconds: 300);
  static const _staggerTime = Duration(milliseconds: 50);
  static const _buttonDelayTime = Duration(milliseconds: 150);
  static const _buttonTime = Duration(milliseconds: 550);
  final _animationDuration = _initialDelayTime +
      (_staggerTime * _menuTitles.length) +
      _buttonDelayTime +
      _buttonTime;

  late AnimationController _staggeredController;
  final List<Interval> _itemSlideIntervals = [];
  late Interval _buttonInterval;

  @override
  void initState() {
    super.initState();
    _createAnimationIntervals();
    _staggeredController = AnimationController(
      vsync: this,
      duration: _animationDuration,
    )
      ..forward();
  }

  void _createAnimationIntervals() {
    for (var i = 0; i < _menuTitles.length+1; ++i) {
      final startTime = _initialDelayTime + (_staggerTime * i);
      final endTime = startTime + _itemSlideTime;
      _itemSlideIntervals.add(
        Interval(
          startTime.inMilliseconds / _animationDuration.inMilliseconds,
          endTime.inMilliseconds / _animationDuration.inMilliseconds,
        ),
      );
    }

    final buttonStartTime =
        Duration(milliseconds: (_menuTitles.length * 50)) + _buttonDelayTime;
    final buttonEndTime = buttonStartTime + _buttonTime;
    _buttonInterval = Interval(
      buttonStartTime.inMilliseconds / _animationDuration.inMilliseconds,
      buttonEndTime.inMilliseconds / _animationDuration.inMilliseconds,
    );
  }

  @override
  void dispose() {
    _staggeredController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.white,
      child: Stack(
        fit: StackFit.expand,
        children: [
          _buildAppLogo(),
          _buildContent(),
        ],
      ),
    );
  }

  Widget _buildAppLogo() {
    return Positioned(
      right: -100,
      bottom: -30,
      child: Opacity(
        opacity: 0.2,
        child: Image.asset(
          "assets/images/logo2.png",
          width: 400,
        ),
      ),
    );
  }

  Widget _buildContent() {
    return ListView(
      shrinkWrap: true,
      children: [
        const SizedBox(height: 16),
        _buildGetStartedButton(),
        ..._buildListItems(),

      ],
    );
  }

  List<Widget> _buildListItems() {
    final ThemeData theme = Theme.of(context);
    final TextStyle textStyle = theme.textTheme.bodyMedium!;
    final List<Widget> aboutBoxChildren = <Widget>[
      const SizedBox(height: 24),
      RichText(
        text: TextSpan(
          children: <TextSpan>[
            TextSpan(
                style: textStyle,
                text: "Application développée par IPIC-ASSO, pour aider les enseignants du primaire et du secondaire.\n"
                    'Pour en savoir plus, poser une question, effectuer une réclamation... '
                    'Ecrivez nous à l\'adresse: '),
            TextSpan(
              text: 'contact@ipic-asso.fr',
              style: const TextStyle(color: Colors.blue),
              recognizer: TapGestureRecognizer()
                ..onTap = () async {
                  await Clipboard.setData(const ClipboardData(text: "contact@ipic-asso.fr"));
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
                    content: Text('copié !'),
                  ));
                },
            ),
            TextSpan(
              text: ' ou visitez notre site: ',
              style: textStyle,
            ),
            TextSpan(
              text: 'https://www.ipic-asso.fr',
              style: const TextStyle(color: Colors.blue),
              recognizer: TapGestureRecognizer()
                ..onTap = () {
                  launchUrl(Uri.parse('https://www.ipic-asso.fr'));
                },
            ),
          ],
        ),
      ),
    ];
    final listItems = <Widget>[];
    for (var i = 0; i < _menuTitles.length; ++i) {
      listItems.add(
        AnimatedBuilder(
          animation: _staggeredController,
          builder: (context, child) {
            final animationPercent = Curves.easeOut.transform(
              _itemSlideIntervals[i].transform(_staggeredController.value),
            );
            final opacity = animationPercent;
            final slideDistance = (1.0 - animationPercent) * 150;

            return Opacity(
              opacity: opacity,
              child: Transform.translate(
                offset: Offset(slideDistance, 0),
                child: child,
              ),
            );
          },
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 36.0, vertical: 16),
            child: ListTile(
              leading: Icon(
                _iconMenu[i],
                color: _colorMenu[i],
              ),
              title: Text(
                  _menuTitles[i],
                  style: TextStyle(
                    fontSize: i<_menuTitles.length-1?20:16,
                    fontWeight: FontWeight.w500,
                  )),
              onTap: () async {
                Navigator.pop(context);
                _staggeredController.animateBack(1);
                Navigator.push(
                  context,
                  PageRouteBuilder(
                    pageBuilder: (_, __, ___) => _classes[i],
                    transitionDuration: const Duration(milliseconds: 500),
                    transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
                  ),
                );

              },
            ),
          ),
        ),
      );
    }
    listItems.add(
      AnimatedBuilder(
        animation: _staggeredController,
        builder: (context, child) {
          final animationPercent = Curves.easeOut.transform(
            _itemSlideIntervals.last.transform(_staggeredController.value),
          );
          final opacity = animationPercent;
          final slideDistance = (1.0 - animationPercent) * 150;

          return Opacity(
            opacity: opacity,
            child: Transform.translate(
              offset: Offset(slideDistance, 0),
              child: child,
            ),
          );
        },
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 36.0, vertical: 16),
          child: AboutListTile(
            icon: const Icon(
              Icons.info,
            ),
            applicationIcon: Tab(icon: Image.asset("assets/images/IPIC_logo_petit.png",width: 40,)),
            applicationName: 'Plan de Classe',
            applicationVersion: '2.1.4',
            applicationLegalese: '© 2023 IPIC-ASSO',
            aboutBoxChildren: aboutBoxChildren,
            child: const Text('A propos'),
          ),
        ),
      ),
    );
    return listItems;
  }

  Widget _buildGetStartedButton() {
    return SizedBox(
      width: double.infinity,
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: AnimatedBuilder(
          animation: _staggeredController,
          builder: (context, child) {
            final animationPercent = Curves.elasticOut.transform(
                _buttonInterval.transform(_staggeredController.value));
            final opacity = animationPercent.clamp(0.0, 1.0);
            final scale = (animationPercent * 0.5) + 0.5;
            return Opacity(
              opacity: opacity,
              child: Transform.scale(
                scale: scale,
                child: child,
              ),
            );
          },
          child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                shape: const StadiumBorder(),
                backgroundColor: Colors.primaries.first,
                padding: const EdgeInsets.symmetric(horizontal: 48, vertical: 14),
              ),
              onPressed: () async {
                Navigator.push(context, MaterialPageRoute(builder: (_) => const MyHomePage()));
              },
              child: Row(
                  children: const [
                    Expanded(flex:0,child: Icon(Icons.home,color: Colors.white,)),
                    Expanded(child: Text(
                      'Classes',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 22,
                      ),
                    ),
                    ),
                  ])
          ),
        ),
      ),
    );
  }
}