import 'package:flutter/material.dart';
import 'package:guess_the_number/pages/home/home_page.dart';
import 'package:guess_the_number/pages/guessgame/guessgame_page.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Guess the number',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      // home: const HomePage(title: 'Flutter Demo Home Page'),
      initialRoute: '/home',
      routes: {
        '/home': (context) => const HomePage(title: 'Guess the number - Home'),
        '/guess': (context) => const GuessGame(),
      },
      debugShowCheckedModeBanner: false,
    );
  }
}
